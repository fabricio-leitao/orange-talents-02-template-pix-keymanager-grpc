package br.com.zup.edu.pix.registrachave.exceptions

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerInterceptor(
    @Inject private val resolver: ExceptionHandlerResolver
) : MethodInterceptor<BindableService, Any> {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {
        return try {
            context.proceed()
        } catch (e: Exception) {
            logger.error("Exceção '${e.javaClass.name}' na chamada ${context.targetMethod}", e)

            val handler = resolver.resolve(e)
            val status = handler.handle(e)

            GrpcEndpointArguments(context).response()

                .onError(status.asRuntimeException())
            null
        }
    }
}

class GrpcEndpointArguments(val context: MethodInvocationContext<BindableService, Any?>) {
    fun response(): StreamObserver<*> {
        return context.parameterValues[1] as StreamObserver<*>
    }
}
