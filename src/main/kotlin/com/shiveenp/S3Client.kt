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

    fun listAllKeys() {
        val req = ListObjectsV2Request().withBucketName(bucket).withMaxKeys(10)
        client.listObjectsV2(req).objectSummaries.forEach {
            println("key: ${it.key}; size: ${it.size}")
        }
    }

    fun put() {
        val testFile = File("test-file")
        testFile.createNewFile()
        client.putObject(bucket, "test-1", testFile)
    }
}
