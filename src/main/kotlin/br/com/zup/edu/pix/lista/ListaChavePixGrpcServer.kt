package br.com.zup.edu.pix.lista

import br.com.zup.edu.*
import br.com.zup.edu.pix.exceptions.ErrorHandler
import br.com.zup.edu.pix.repositories.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@ErrorHandler
@Singleton
class ListaChavePixGrpcServer(
    @Inject private val repository: ChavePixRepository
): KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceImplBase() {

    @Transactional
    override fun lista(request: ListaChavesPixRequest, responseObserver: StreamObserver<ListaChavesPixResponse>) {

        if(request.clienteId.isNullOrBlank()){
            throw IllegalArgumentException("ClienteId não pode ser nulo ou vazio")
        }

        val clienteId = UUID.fromString(request.clienteId)
        val chaves = repository.findAllByClienteId(clienteId).map {
            ListaChavesPixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipoDeChave(TipoDeChave.valueOf(it.tipoDeChave.name))
                .setChave(it.chave)
                .setTipoDeConta(TipoDeConta.valueOf(it.tipoDeConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(ListaChavesPixResponse.newBuilder()
            .setClienteId(clienteId.toString())
            .addAllChaves(chaves)
            .build())

        responseObserver.onCompleted()
    }
}