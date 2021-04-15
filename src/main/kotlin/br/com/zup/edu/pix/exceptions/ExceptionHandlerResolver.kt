package br.com.zup.edu.pix.exceptions

import br.com.zup.edu.pix.exceptions.handler.DefaultExceptionHandler
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(
    @Inject private val handlers: List<ExceptionHandler<*>>
) {

    private var defaultHandler: ExceptionHandler<Exception> = DefaultExceptionHandler()

    constructor(handlers: List<ExceptionHandler<Exception>>, defaultHandler: ExceptionHandler<Exception>): this(handlers){
        this.defaultHandler = defaultHandler
    }

    fun resolve(e: Exception): ExceptionHandler<*> {
        val foundHandlers = handlers.filter {  handlers -> handlers.supports(e) }
        if(foundHandlers.size > 1){
            throw IllegalStateException("Muitos handlers na mesma exceção '${e.javaClass.name}': $foundHandlers")
        }
        return foundHandlers.firstOrNull() ?: defaultHandler
    }
}
