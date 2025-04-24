package com.wafflestudio.internhasha.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AwsS3Config {
    @Value("\${amazon.aws.accessKey}")
    private val accessKeyId: String? = null

    @Value("\${amazon.aws.secretKey}")
    private val accessKeySecret: String? = null

    @Value("\${amazon.aws.region}")
    private val s3RegionName: String? = null

    @get:Bean
    val amazonS3Client: AmazonS3
        get() {
            val basicAWSCredentials: BasicAWSCredentials = BasicAWSCredentials(accessKeyId, accessKeySecret)

            // Get Amazon S3 client and return the s3 client object
            return AmazonS3ClientBuilder
                .standard()
                .withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
                .withRegion(s3RegionName)
                .build()
        }
}
