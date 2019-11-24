package com.shiveenp

import com.amazonaws.HttpMethod
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ListObjectsV2Request
import io.ktor.util.date.toGMTDate
import io.ktor.util.date.toJvmDate
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class S3Client(
    private val region: String,
    private val bucketName: String,
    private val awsAccessKey: String,
    private val awsSecretKey: String
) {

    private val endpoint = "https://s3-$region.amazonaws.com"

    private val client = AmazonS3ClientBuilder
        .standard()
        .withPathStyleAccessEnabled(true)
        .withEndpointConfiguration(getEndpointConfiguration())
        .withCredentials(getCredentialsProvider())
        .build()

    fun listAllKeys(maxKeys: Int? = null): Pair<String?, List<S3Data>> {
        val req = ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(maxKeys ?: 10)
        val keyList = mutableListOf<S3Data>()
        val listObjectResponse = client.listObjectsV2(req)
        println(listObjectResponse.nextContinuationToken)
        listObjectResponse.objectSummaries.forEach {
            keyList.add(
                S3Data(
                    it.key,
                    client.generatePresignedUrl(
                        bucketName,
                        it.key,
                        Date.from(LocalDateTime.now().plusSeconds(901L).atZone(ZoneId.systemDefault()).toInstant()),
                        HttpMethod.GET
                    ).toString(),
                    it.size.toString().toDouble() / 1000.0,
                    it.lastModified.toString()
                )
            )
        }
        return Pair(listObjectResponse.nextContinuationToken, keyList)
    }

    private fun getEndpointConfiguration(): AwsClientBuilder.EndpointConfiguration {
        return AwsClientBuilder.EndpointConfiguration(endpoint, region)
    }

    private fun getCredentialsProvider() =
        AWSStaticCredentialsProvider(
            BasicAWSCredentials(
                awsAccessKey,
                awsSecretKey
            )
        )
}

data class S3Data(
    val key: String,
    val downloadUrl: String,
    val size: Double,
    val lastModifiedAt: String
)
