package br.com.zup.edu.pix.registrachave

import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.integracao.itau.ContaItauClient
import br.com.zup.edu.pix.registrachave.exceptions.ExistingPixKeyException
import br.com.zup.edu.pix.registrachave.repositories.ChavePixRepository
import io.grpc.Status
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContaItauClient
) {
    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave)) {
            throw ExistingPixKeyException(Status.INVALID_ARGUMENT.withDescription("Chave Pix '${novaChave.chave}' existente"))
        }

        val response = itauClient.buscaConta(UUID.fromString(novaChave.clienteId)!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        val chave = novaChave.toModel(conta)
        repository.save(chave)

        return chave
    }
}