package br.com.zup.edu.pix.exceptions.handler

import br.com.zup.edu.pix.exceptions.ExceptionHandler
import br.com.zup.edu.pix.exceptions.PixKeyNotFoundException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PixKeyNotFoundExceptionHandler: ExceptionHandler<PixKeyNotFoundException> {

    override fun handle(e: PixKeyNotFoundException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is PixKeyNotFoundException
    }
}