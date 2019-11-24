package com.shiveenp

import io.kweb.Kweb
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.plugins.fomanticUI.fomantic
import io.kweb.plugins.fomanticUI.fomanticUIPlugin
import io.kweb.routing.route
import io.kweb.state.KVar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch

fun main() {

    val herokuPort: String? = System.getenv("PORT")

    startApp(herokuPort)
}

private fun startApp(herokuPort: String?) {
    Kweb(port = herokuPort?.toInt() ?: 6300, debug = true, plugins = listOf(fomanticUIPlugin)) {
        doc.body.new {
            route {
                path("") {
                    var getNextPage: DivElement? = null
                    div(fomantic.ui.main.container).new {
                        div(fomantic.ui.vertical.segment).new {
                            div(fomantic.ui.header).text(
                                """
                                Welcome to S3 Browser 💻
                                
                                To start browsing your S3 bucket, enter the details below:
                                """.trimIndent()
                            )
                        }

                        val s3ClientKVar: KVar<S3Client?> = KVar(null)
                        val continuationToken = KVar("")
                        val keyData = KVar(emptyList<S3Data>())
                        val showGetNextPage = KVar(false)

                        val loader = div(mapOf("class" to "ui active text loader")).addText("Retrieving keys...")
                        loader.setAttribute("class", "ui disabled text loader")
                        createInputSegment(s3ClientKVar, loader, getNextPage, keyData, continuationToken)
                        createKeysTable(keyData)
                        div().new {
                            button(mapOf("class" to "ui primary button")).text("Get Next Page").on.click {
                                val continuationTokenValue = continuationToken.value
                                if (continuationTokenValue.isNotBlank()) {
                                    try {
                                        val keyListResponse =
                                            s3ClientKVar.value!!.listAllKeys(continuationToken = continuationTokenValue)
                                        if (keyListResponse.first != null) {
                                            continuationToken.value = keyListResponse.first!!
                                            getNextPage?.setAttribute("visibility", "show;")
                                        }
                                        keyData.value = keyListResponse.second
                                    } catch (ex: Exception) {
                                        println(ex.localizedMessage)
                                        println(ex.printStackTrace())
                                        p().execute(ERROR_TOAST)
                                        loader.setAttribute("class", "ui disabled text loader")
                                        createKeysTable(KVar(emptyList()))
                                    }
                                    if (keyData.value.isNotEmpty()) {
                                        p().execute(SUCCESS_TOAST)
                                        loader.setAttribute("class", "ui disabled text loader")
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Creates the input area where the users can enter the details about the S3 bucket
 */
private fun ElementCreator<DivElement>.createInputSegment(
    s3ClientKvar: KVar<S3Client?>,
    loader: Element,
    getNextPage: Element?,
    keyData: KVar<List<S3Data>>,
    continuationToken: KVar<String>
) {
    div(fomantic.ui.vertical.segment).new {
        var s3Client: S3Client? = null
        val endpointInput = select(fomantic.ui.dropdown)
        endpointInput.new {
            AWS_REGION_OPTIONS.forEach {
                option(mapOf("value" to it.key)).text(it.value)
            }
        }
        div(fomantic.ui.input).new {
            val bucketInput = input(type = InputType.text, placeholder = "Enter S3 Bucket Name")
            val awsKey = input(type = InputType.text, placeholder = "Access Key")
            val awsSecret = input(type = InputType.text, placeholder = "Secret Key")
            button(mapOf("class" to "ui primary button")).text("Search").on.click {
                GlobalScope.launch {
                    loader.setAttribute("class", "ui active text loader")
                    s3ClientKvar.value =
                        S3Client(
                            endpointInput.getValue().await(),
                            bucketInput.getValue().await(),
                            awsKey.getValue().await(),
                            awsSecret.getValue().await()
                        )
                    try {
                        val keyListResponse = s3ClientKvar.value!!.listAllKeys()
                        if (keyListResponse.first != null) {
                            continuationToken.value = keyListResponse.first!!
                            getNextPage?.setAttribute("visibility", "show;")
                        }
                        keyData.value = keyListResponse.second
                    } catch (ex: Exception) {
                        println(ex.localizedMessage)
                        println(ex.printStackTrace())
                        p().execute(ERROR_TOAST)
                        loader.setAttribute("class", "ui disabled text loader")
                        createKeysTable(KVar(emptyList()))
                    }
                    if (keyData.value.isNotEmpty()) {
                        p().execute(SUCCESS_TOAST)
                        loader.setAttribute("class", "ui disabled text loader")
                    }
                }
            }
        }
    }
}

/**
 * Renders the table of s3 keys using the given key data map
 */
private fun ElementCreator<DivElement>.createKeysTable(
    keyData: KVar<List<S3Data>>
) {
    table(mapOf("class" to "ui celled striped table")).new {
        thead().new {
            tr().new {
                th().text("Key")
                th().text("File Size (in KB)")
                th().text("Last Modified At")
            }
        }
        tbody().new {
            keyData.map {
                it.forEach {
                    tr().new {
                        td(mapOf("data-lable" to "Key")).innerHTML("<i class=\"file outline icon\"></i> <a target=\"_blank\" href=${it.downloadUrl} download=${it.key}>${it.key}</a>")
                        td(mapOf("data-lable" to "File Size")).text("${it.size} KB")
                        td(mapOf("data-lable" to "Last Modified At")).text(it.lastModifiedAt)

                    }
                }
            }
        }
    }
}


val SUCCESS_TOAST = """
                            ${'$'}('body')
  .toast({
    class: 'info',
    showIcon: '',
    message: 'Successfully retrieved data'
  })
;
                        """.trimIndent()


val ERROR_TOAST = """
                            ${'$'}('body')
  .toast({
    class: 'error',
    showIcon: '',
    message: 'Unable to perform the operation, are you sure the bucket is public?'
  })
;
                        """.trimIndent()


private val AWS_REGION_OPTIONS = mapOf(
    "ap-southeast-2" to "Asia Pacific (Sydney)",
    "ap-east-1" to "Asia Pacific (Hong Kong)",
    "ap-south-1" to "Asia Pacific (Mumbai)",
    "ap-northeast-3" to "Asia Pacific (Osaka-Local)",
    "ap-northeast-2" to "Asia Pacific (Seoul)",
    "ap-southeast-1" to "Asia Pacific (Singapore)",
    "ap-northeast-1" to "Asia Pacific (Tokyo)"
)
