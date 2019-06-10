package com.shiveenp

import io.kweb.Kweb
import io.kweb.dom.attributes.attr
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.new
import io.kweb.plugins.fomanticUI.fomantic
import io.kweb.plugins.fomanticUI.fomanticUIPlugin
import io.kweb.routing.route

fun main() {
    val s3Client = S3Client()
//    s3Client.put()
    s3Client.listAllKeys()

    Kweb(port = 12000, plugins = listOf(fomanticUIPlugin)) {
        doc.body.new {

            route {
                path("/s3") {
                    div(fomantic.ui.header).text("Welcome to Local Amazon S3 Bowser 💻")
                    div(fomantic.ui.divider)

                    table(mapOf("class" to "ui celled striped table")).new {
                        thead().new {
                            tr().new {
                                th().text("Key")
                                th().text("File Size (in KB)")
                                th().text("Last Modified At")
                            }
                        }
                        tbody().new {
                            s3Client.listAllKeys().forEach {
                                tr().new {
                                    td(mapOf("data-lable" to "Key")).text(it.key)
                                    td(mapOf("data-lable" to "File Size (in KB)")).text(it.size.toString())
                                    td(mapOf("data-lable" to "Last Modified At")).text(it.lastModifedAt)

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun ElementCreator<ULElement>.createKeyElement(key: String) = li().text(key)
