package io.silv.server

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.server.Undertow
import org.http4k.server.asServer

fun createApp() = { request: Request ->
    handleRequest(request)
}
    .asServer(Undertow(9000))
    .start()


private fun handleRequest(request: Request): Response = Response(OK).body("Hello")

