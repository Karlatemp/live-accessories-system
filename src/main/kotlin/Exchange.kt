package com.kasukusakura.danmu

import com.google.gson.GsonBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedDeque

val connections = ConcurrentLinkedDeque<WebSocketServerSession>()

val gsonx = GsonBuilder().create()

suspend fun pushData(data: Any) = pushMsg(
    Frame.Text(gsonx.toJson(data).also { println(it) })
)

suspend fun pushMsg(msg: Frame) {
    connections.forEach { ws ->
        try {
            ws.send(msg.copy())
        } catch (ignored: IOException) {
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

val scopex = CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
    throwable.printStackTrace()
})

fun main() {
    embeddedServer(Netty, applicationEngineEnvironment {
        this.parentCoroutineContext = scopex.coroutineContext
        developmentMode = true
        connector {
            port = 12421
        }
        module {
            install(WebSockets)
            configureRouting()
        }
    }) {
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Completed")
            println("!")
        }
        get("/trusted-broadcast") {
            val msg = call.parameters["msg"]
            if (msg != null) {
                println(msg)
                launch { pushMsg(Frame.Text(msg)) }
                call.respondText { "Broadcasting to all...: $msg" }
            } else {
                call.respondText { "No msg" }
            }
        }
        get("/settings.js") {
            call.respondOutputStream(
                contentType = ContentType.parse("application/javascript"),
            ) {
                write("dnsettings = ".toByteArray())
                File("settings.json5").inputStream().use { it.copyTo(this) }
            }
        }
        static("/static") {
            staticRootFolder = File("./src/webpage")
            files(".")
        }
        static("/webpriv") {
            staticRootFolder = File("./src/webpriv")
            files(".")
        }
        webSocket("/msgc") {
            println("onConnect")
            connections.add(this)
            try {
                for (frame in incoming) {
                    val text = (frame as Frame.Text).readText()
                    println("onMessage: $text")
                }
            } catch (e: Throwable) {
                println("onError ${closeReason.await()}")
                e.printStackTrace()
            }
            connections.remove(this)
            println("Disconnected")
        }
    }
}
