package br.com.zup.edu.pix.consulta

import br.com.zup.edu.CarregaChavePixRequest
import br.com.zup.edu.CarregaChavePixResponse
import br.com.zup.edu.KeymanagerConsultaGrpcServiceGrpc
import br.com.zup.edu.pix.exceptions.ErrorHandler
import br.com.zup.edu.pix.integracao.bcb.BcbClient
import br.com.zup.edu.pix.repositories.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@ErrorHandler
@Singleton
class ConsultaChaveGrpcServer(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BcbClient,
    @Inject private val validator: Validator
): KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceImplBase() {

    override fun consulta(
        request: CarregaChavePixRequest,
        responseObserver: StreamObserver<CarregaChavePixResponse>
    ) {
        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(ConsultaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}

fun CarregaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when(filtroCase!!) {
        CarregaChavePixRequest.FiltroCase.PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        CarregaChavePixRequest.FiltroCase.CHAVE -> Filtro.PorChave(chave)
        CarregaChavePixRequest.FiltroCase.FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filtro
}
