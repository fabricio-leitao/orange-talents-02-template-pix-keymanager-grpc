package br.com.zup.edu.pix.exceptions.handler

import br.com.zup.edu.pix.exceptions.ClientNotFoundException
import br.com.zup.edu.pix.exceptions.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ClientNotFoundExceptionHandler: ExceptionHandler<ClientNotFoundException> {

    override fun handle(e: ClientNotFoundException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ClientNotFoundException
    }
}