package com.shiveenp

import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.plugins.fomanticUI.fomantic
import io.kweb.plugins.fomanticUI.fomanticUIPlugin

fun main(args : Array<String>) {
    val s3Client = S3Client()
//    s3Client.put()
    s3Client.listAllKeys()

    Kweb(port = 12000, plugins = listOf(fomanticUIPlugin)) {
        doc.body.new {
            val helloText = "text"

            div(fomantic.ui.menu).new {
                this.a(fomantic.ui.item).text("S3").on.click {

                }
                this.a(fomantic.ui.item).text("SQS")
            }

            h1().text("")
        }
    }
}
