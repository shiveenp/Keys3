package com.shiveenp

import io.kweb.Kweb
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

    Kweb(port = 12000, plugins = listOf(fomanticUIPlugin)) {
        doc.body.new {
            route {
                path("/") {
                    div(fomantic.ui.header).text("Welcome to S3 Browser 💻")
                    div(fomantic.ui.divider)

                    val keyData = KVar(emptyList<S3Data>())

                    val loader = div(mapOf("class" to "ui active centered inline loader"))
                    loader.setAttribute("class", "ui disabled loader")

                    div(fomantic.ui.vertical.segment).new {
                        div(fomantic.ui.input).new {
                            val endpointInput = input(type = InputType.text, placeholder = "Enter S3 Endpoint Url")
                            val bucketInput = input(type = InputType.text, placeholder = "Enter S3 Bucket Name")
                            button(mapOf("class" to "ui primary button")).text("Search").on.click {
                                GlobalScope.launch {
                                    loader.setAttribute("class", "ui active centered inline loader")
                                    val s3Client =
                                        S3Client(endpointInput.getValue().await(), bucketInput.getValue().await())
                                    try {
                                        keyData.value = s3Client.listAllKeys()
                                    } catch (ex: Exception) {
                                        loader.setAttribute("class", "ui disabled loader")
                                    }
                                    if (keyData.value.isNotEmpty()) {
                                        loader.setAttribute("class", "ui disabled loader")
                                    }
                                }
                            }
                        }
                    }

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
            }
        }
    }
}

