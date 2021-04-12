package br.com.zup.edu.pix.registrachave.exceptions

import io.grpc.Status

class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): ExceptionHandler.StatusWithDetails {
        return  ExceptionHandler.StatusWithDetails(
            Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {

        return e is DefaultException
    }
}
