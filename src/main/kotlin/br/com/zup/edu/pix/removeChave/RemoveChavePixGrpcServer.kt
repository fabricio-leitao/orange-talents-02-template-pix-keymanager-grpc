package br.com.zup.edu.pix.removeChave

import br.com.zup.edu.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.RemoveChavePixResponse
import br.com.zup.edu.pix.exceptions.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChavePixGrpcServer(
    @Inject private val service: RemoveChavePixService
    ): KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest?, responseObserver: StreamObserver<RemoveChavePixResponse>?) {
        service.remove(clienteId = request?.clienteId, pixId = request?.pixId)

        responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
            .setClienteId(request?.clienteId)
            .setPixId(request?.pixId)
            .build())

        responseObserver?.onCompleted()
    }
}