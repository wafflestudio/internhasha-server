package com.waffletoy.team1server.coffeeChat.service

import com.waffletoy.team1server.coffeeChat.*
import com.waffletoy.team1server.coffeeChat.controller.*
import com.waffletoy.team1server.coffeeChat.controller.CoffeeChat
import com.waffletoy.team1server.coffeeChat.persistence.CoffeeChatEntity
import com.waffletoy.team1server.coffeeChat.persistence.CoffeeChatRepository
import com.waffletoy.team1server.email.EmailSendFailureException
import com.waffletoy.team1server.email.service.EmailService
import com.waffletoy.team1server.exceptions.*
import com.waffletoy.team1server.post.persistence.PositionEntity
import com.waffletoy.team1server.post.service.PostService
import com.waffletoy.team1server.user.UserRole
import com.waffletoy.team1server.user.dtos.User
import com.waffletoy.team1server.user.persistence.UserEntity
import com.waffletoy.team1server.user.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class CoffeeChatService(
    private val coffeeChatRepository: CoffeeChatRepository,
    @Lazy private val userService: UserService,
    @Lazy private val emailService: EmailService,
    @Lazy private val postService: PostService,
) {
    @Value("\${custom.page.size:12}")
    private val pageSize: Int = 12

    /**
     * Retrieves detailed information of a specific coffeeChat by its ID.
     *
     * @param user The authenticated user.
     * @param coffeeChatId The unique identifier of the coffeeChat.
     * @return The detailed [CoffeeChat] object.
     * @throws CoffeeChatNotFoundException If the coffeeChat with the given ID does not exist.
     * @throws CoffeeChatForbiddenException If the user is not the owner of the coffeeChat.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     */
    fun getCoffeeChatDetail(
        user: User?,
        coffeeChatId: String,
    ): CoffeeChat {
        val validUser = getValidUser(user)
        val coffeeChatEntity = getValidatedCoffeeChat(validUser, coffeeChatId)
        validateCoffeeChatOwnership(validUser, coffeeChatEntity)
        return CoffeeChat.fromEntity(coffeeChatEntity)
    }

    /**
     * Retrieves a list of coffeeChats belonging to the authenticated user.
     *
     * @param user The authenticated user.
     * @return A list of [CoffeeChat] objects.
     * @throws CoffeeChatNotFoundException If the user does not exist.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     */
    fun getCoffeeChats(
        user: User?,
    ): List<CoffeeChat> {
        val validUser = getValidUser(user)
        val coffeeChats = coffeeChatRepository.findAllByUserId(validUser.id)
        return coffeeChats.map { CoffeeChat.fromEntity(it) }
    }

    /**
     * Creates a new coffeeChat associated with a specific post.
     *
     * @param user The authenticated user.
     * @param postId The unique identifier of the post.
     * @param coffee The [Coffee] data containing coffeeChat details.
     * @return The created [CoffeeChat] object.
     * @throws CoffeeChatNotFoundException If the post does not exist.
     * @throws CoffeeChatCreationFailedException If there is an issue creating the coffeeChat.
     * @throws CoffeeChatNotFoundException If the user does not exist.
     * @throws CoffeeChatForbiddenException If the user is not authenticated or has an invalid role.
     */
    @Transactional
    fun postCoffeeChat(
        user: User?,
        postId: String,
        coffeeChatRequest: CoffeeChatRequest,
    ): CoffeeChat {
        val validUser = getValidUser(user)
        val userEntity = getUserEntityOrThrow(validUser.id)
        val positionEntity = getPositionEntityOrThrow(postId)
        val coffeeChatEntity =
            try {
                coffeeChatRepository.save(
                    CoffeeChatEntity(
                        content = coffeeChatRequest.content,
                        phoneNumber = coffeeChatRequest.phoneNumber,
                        position = positionEntity,
                        user = userEntity,
                    ),
                )
            } catch (ex: Exception) {
                throw CoffeeChatCreationFailedException(
                    details =
                        mapOf(
                            "userId" to validUser.id,
                            "postId" to postId,
                            "error" to ex.message.orEmpty(),
                        ),
                )
            }

        val companyEntity = positionEntity.company

        // 이메일 전송
        try {
            emailService.sendEmail(
                to = companyEntity.email,
                subject = "[인턴하샤] 지원자 커피챗이 도착하였습니다.",
                text =
                    """
                    [${companyEntity.companyName}] ${positionEntity.title} 포지션 지원자 정보:
                    
                    - 회사명: ${companyEntity.companyName}
                    - 회사 이메일: ${companyEntity.email}
                    - 직무명: ${positionEntity.title}
                    - 카테고리: ${positionEntity.category.displayName()}
                    - 지원 마감일: ${positionEntity.employmentEndDate
                        ?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        ?: "정보 없음"}
                    
                    지원자 정보:
                    - 이름: ${validUser.name}
                    - 이메일: ${validUser.snuMail ?: "이메일 정보 없음"}
                    - 전화번호: ${coffeeChatEntity.phoneNumber ?: "전화번호 정보 없음"}
                    
                    커피챗 내용:
                    --------------------------------------------
                    ${coffeeChatEntity.content ?: "커피챗 내용이 없습니다."}
                    --------------------------------------------
                    
                    인턴하샤 지원 시스템을 통해 지원자가 회사에 커피챗을 제출하였습니다.
                    """.trimIndent(),
            )
        } catch (ex: Exception) {
            throw EmailSendFailureException(
                details =
                    mapOf(
                        "to" to companyEntity.email,
                        "error" to ex.message.orEmpty(),
                    ),
            )
        }

        return CoffeeChat.fromEntity(coffeeChatEntity)
    }

    /**
     * Deletes a specific coffeeChat.
     *
     * @param user The authenticated user.
     * @param coffeeChatId The unique identifier of the coffeeChat.
     * @throws CoffeeChatNotFoundException If the coffeeChat does not exist.
     * @throws CoffeeChatForbiddenException If the user is not the owner of the coffeeChat.
     * @throws CoffeeChatDeletionFailedException If there is an issue deleting the coffeeChat.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     */
    @Transactional
    fun deleteCoffeeChat(
        user: User?,
        coffeeChatId: String,
    ) {
        val validUser = getValidUser(user)
        val coffeeChatEntity = getValidatedCoffeeChat(validUser, coffeeChatId)
        validateCoffeeChatOwnership(validUser, coffeeChatEntity)
        try {
            coffeeChatRepository.delete(coffeeChatEntity)
        } catch (ex: Exception) {
            throw CoffeeChatDeletionFailedException(
                details =
                    mapOf(
                        "coffeeChatId" to coffeeChatId,
                        "error" to ex.message.orEmpty(),
                    ),
            )
        }
    }

    /**
     * Updates an existing coffeeChat.
     *
     * @param user The authenticated user.
     * @param coffeeChatId The unique identifier of the coffeeChat.
     * @param coffee The [Coffee] data containing updated coffeeChat details.
     * @return The updated [CoffeeChat] object.
     * @throws CoffeeChatNotFoundException If the coffeeChat does not exist.
     * @throws CoffeeChatForbiddenException If the user is not the owner of the coffeeChat.
     * @throws CoffeeChatUpdateFailedException If there is an issue updating the coffeeChat.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     */
    @Transactional
    fun patchCoffeeChat(
        user: User?,
        coffeeChatId: String,
        coffeeChatRequest: CoffeeChatRequest,
    ): CoffeeChat {
        val validUser = getValidUser(user)
        val coffeeChatEntity = getValidatedCoffeeChat(validUser, coffeeChatId)
        validateCoffeeChatOwnership(validUser, coffeeChatEntity)

        // 전달된 데이터로 업데이트
        coffeeChatEntity.phoneNumber = coffeeChatRequest.phoneNumber
        coffeeChatEntity.content = coffeeChatRequest.content

        return try {
            CoffeeChat.fromEntity(coffeeChatRepository.save(coffeeChatEntity))
        } catch (ex: Exception) {
            throw CoffeeChatUpdateFailedException(
                details =
                    mapOf(
                        "coffeeChatId" to coffeeChatId,
                        "error" to ex.message.orEmpty(),
                    ),
            )
        }
    }

    /**
     * Validates the authenticated user.
     *
     * @param user The authenticated user.
     * @return The validated [User] object.
     * @throws InvalidAccessTokenException If the user is not authenticated.
     * @throws CoffeeChatForbiddenException If the user does not have the NORMAL role.
     */
    fun getValidUser(user: User?): User {
        if (user == null) {
            throw InvalidAccessTokenException(
                details = mapOf("user" to "null"),
            )
        }
        if (user.userRole != UserRole.NORMAL) {
            throw CoffeeChatForbiddenException(
                details = mapOf("userId" to user.id, "userRole" to user.userRole),
            )
        }
        return user
    }

    fun getPositionEntityOrThrow(postId: String): PositionEntity =
        postService.getPositionEntityByPostId(postId) ?: throw CoffeeChatNotFoundException(
            details = mapOf("postId" to postId),
        )

    fun getUserEntityOrThrow(userId: String): UserEntity =
        userService.getUserEntityByUserId(userId) ?: throw CoffeeChatNotFoundException(
            details = mapOf("userId" to userId),
        )

    fun validateCoffeeChatOwnership(
        user: User,
        coffeeChat: CoffeeChatEntity,
    ) {
        if (coffeeChat.user.id != user.id) {
            throw CoffeeChatForbiddenException(
                details = mapOf("userId" to user.id, "coffeeChatId" to coffeeChat.id),
            )
        }
    }

    fun getValidatedCoffeeChat(
        validUser: User,
        coffeeChatId: String,
    ): CoffeeChatEntity {
        val coffeeChatEntity =
            coffeeChatRepository.findByIdOrNull(coffeeChatId)
                ?: throw CoffeeChatNotFoundException(
                    details = mapOf("coffeeChatId" to coffeeChatId),
                )
        return coffeeChatEntity
    }

    // normal 유저 탈퇴 시 bookmark 데이터를 삭제
    // curator 유저가 작성한 company, position 데이터는 유지
    @Transactional(propagation = Propagation.REQUIRED)
    fun deleteCoffeeChatByUser(userEntity: UserEntity) {
        coffeeChatRepository.deleteAllByUser(userEntity)
    }
}
