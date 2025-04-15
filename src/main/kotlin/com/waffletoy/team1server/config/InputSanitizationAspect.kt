package com.waffletoy.team1server.config

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.*
import org.aspectj.lang.annotation.Around
import org.jsoup.Jsoup
import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.safety.Safelist
import org.springframework.stereotype.Component
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Aspect
@Component
class InputSanitizationAspect {
    @Around("(execution(* com.waffletoy.team1server..*(..)) && @within(org.springframework.web.bind.annotation.RestController)) || @within(org.springframework.stereotype.Service)")
    fun sanitizeInputs(joinPoint: ProceedingJoinPoint): Any? {
        val args = joinPoint.args.map { sanitize(it) }.toTypedArray()
        return joinPoint.proceed(args)
    }

    private fun sanitize(obj: Any?): Any? {
        return when (obj) {
            // 문자열 필터링
            is String -> Jsoup.clean(obj, "", Safelist.none(), OutputSettings().prettyPrint(false))
            // 리스트 내부 요소 필터링
            is List<*> -> obj.map { sanitize(it) }
            // Map 내부 값 필터링
            is Map<*, *> -> obj.mapValues { sanitize(it.value) }
            // 객체 내부 필드 필터링
            is Any -> sanitizeObjectFields(obj)
            else -> obj
        }
    }

    private fun sanitizeObjectFields(obj: Any): Any {
        obj::class.memberProperties
            // 문자열 필드만 필터링
            .filter { it.returnType.classifier == String::class }
            .forEach {
                val field = it.javaField ?: return@forEach
                field.isAccessible = true
                val value = field.get(obj) as? String
                if (value != null) {
                    field.set(obj, Jsoup.clean(value, "", Safelist.none(), OutputSettings().prettyPrint(false)))
                }
            }
        return obj
    }
}
