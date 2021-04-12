package br.com.zup.edu.pix.registrachave.exceptions

import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(
    @Inject private val handlers: List<ExceptionHandler<Exception>>
) {

    private var defaultHandler: ExceptionHandler<Exception> = DefaultExceptionHandler()

    constructor(handlers: List<ExceptionHandler<Exception>>, defaultHandler: ExceptionHandler<Exception>): this(handlers){
        this.defaultHandler = defaultHandler
    }

    fun resolve(e: Exception): ExceptionHandler<Exception>{
        val buscaHandlers = handlers.filter {  handlers -> handlers.supports(e) }
        if(buscaHandlers.size > 1){
            throw IllegalStateException("Muitos handlers na mesma exceção '${e.javaClass.name}': $buscaHandlers")
        }
        return buscaHandlers.firstOrNull() ?: defaultHandler
    }
}
