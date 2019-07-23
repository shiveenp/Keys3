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

    startKweb(herokuPort)
}

private fun startKweb(herokuPort: String?) {
    Kweb(port = herokuPort?.toInt() ?: 6300, debug = true, plugins = listOf(fomanticUIPlugin)) {
        doc.body.new {
            route {
                path("") {
                    div(fomantic.ui.main.container).new {
                        div(fomantic.ui.vertical.segment).new {
                            div(fomantic.ui.header).text("Welcome to S3 Browser 💻")
                        }

                        val keyData = KVar(emptyList<S3Data>())

                        val loader = div(mapOf("class" to "ui active text loader")).addText("Retrieving keys...")
                        loader.setAttribute("class", "ui disabled text loader")
                        createInputSegment(loader, keyData)
                        createKeysTable(keyData)
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
    loader: Element,
    keyData: KVar<List<S3Data>>
) {
    div(fomantic.ui.vertical.segment).new {
        div(fomantic.ui.input).new {
            val endpointInput = input(type = InputType.text, placeholder = "Enter S3 Endpoint Url")
            val bucketInput = input(type = InputType.text, placeholder = "Enter S3 Bucket Name")
            button(mapOf("class" to "ui primary button")).text("Search").on.click {
                GlobalScope.launch {
                    loader.setAttribute("class", "ui active text loader")
                    val s3Client =
                        S3Client(endpointInput.getValue().await(), bucketInput.getValue().await())
                    try {
                        keyData.value = s3Client.listAllKeys()
                    } catch (ex: Exception) {
                        p().execute(ERROR_TOAST)
                        loader.setAttribute("class", "ui disabled text loader")
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


val SUCCESS_TOAST =                             """
                            ${'$'}('body')
  .toast({
    class: 'info',
    showIcon: '',
    message: 'Successfully retrieved data'
  })
;
                        """.trimIndent()


val ERROR_TOAST =                             """
                            ${'$'}('body')
  .toast({
    class: 'error',
    showIcon: '',
    message: 'Unable to perform the operation, are you sure the bucket is public?'
  })
;
                        """.trimIndent()
