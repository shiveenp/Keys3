package com.shiveenp

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.SetBucketAclRequest

class S3Client(private val endpoint: String, private val bucketName: String) {


    private val client = AmazonS3ClientBuilder.standard()
        .withPathStyleAccessEnabled(true)
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, "ap-southeast-2"))
        .build()

    init {
        client.setBucketAcl(
            SetBucketAclRequest(
                bucketName, CannedAccessControlList.PublicRead
            )
        )
    }

    fun listAllKeys(): List<S3Data> {
        val req = ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(10)
        val keyList = mutableListOf<S3Data>()
        client.listObjectsV2(req).objectSummaries.forEach {
            keyList.add(S3Data(it.key, "$endpoint/$bucketName/${it.key}", it.size / 1000, it.lastModified.toString()))
        }
        return keyList
    }
}

data class S3Data(
    val key: String,
    val downloadUrl: String,
    val size: Long,
    val lastModifedAt: String
)
