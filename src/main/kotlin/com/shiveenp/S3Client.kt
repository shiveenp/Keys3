package com.shiveenp

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ListObjectsV2Request
import java.io.File

class S3Client {

    private val bucket = "brows3r-testing"

    private val client = AmazonS3ClientBuilder.standard()
        .withPathStyleAccessEnabled(true)
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:4572", "ap-southeast-2"))
        .build()

    fun listAllKeys(): List<S3Data> {
        val req = ListObjectsV2Request().withBucketName(bucket).withMaxKeys(10)
        val keyList = mutableListOf<S3Data>()
        client.listObjectsV2(req).objectSummaries.forEach {
           keyList.add(S3Data(it.key, it.size/1000, it.lastModified.toString()))
        }
        return keyList
    }

    fun put() {
        val testFile = File("test-file")
        testFile.createNewFile()
        client.putObject(bucket, "test-1", testFile)
    }
}

data class S3Data(
    val key: String,
    val size: Long,
    val lastModifedAt: String
)
