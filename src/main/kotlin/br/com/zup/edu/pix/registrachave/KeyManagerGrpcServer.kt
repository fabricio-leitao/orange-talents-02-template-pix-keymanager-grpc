package br.com.zup.edu.pix.registrachave

import br.com.zup.edu.*
import br.com.zup.edu.TipoDeConta.CONTA_DESCONHECIDA
import br.com.zup.edu.TipoDeConta.valueOf
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.integracao.itau.ContaItauClient
import br.com.zup.edu.pix.exceptions.ErrorHandler
import br.com.zup.edu.pix.registrachave.repositories.ChavePixRepository
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@ErrorHandler
@Singleton
class KeyManagerGrpcServer(
    @Inject val service: NovaChavePixService
): KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceImplBase() {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun registra(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>
    ) {
        logger.info("Registrando chave...")

        val novaChavePix: NovaChavePix = request.toModel()
        val chave: ChavePix = service.registra(novaChavePix)

        responseObserver.onNext(
            ChavePixResponse.newBuilder()
                .setClienteId(chave.clienteId.toString())
                .setPixId(chave.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}

private fun ChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        tipoDeChave = when (tipoDeChave){
            TipoDeChave.CHAVE_DESCONHECIDA -> null
            else -> br.com.zup.edu.pix.registrachave.enums.TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta){
            CONTA_DESCONHECIDA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )
}