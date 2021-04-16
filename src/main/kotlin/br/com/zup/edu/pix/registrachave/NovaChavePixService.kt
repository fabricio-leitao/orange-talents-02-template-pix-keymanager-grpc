package br.com.zup.edu.pix.registrachave

import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.integracao.itau.ContaItauClient
import br.com.zup.edu.pix.exceptions.ExistingPixKeyException
import br.com.zup.edu.pix.exceptions.PixKeyNotFoundException
import br.com.zup.edu.pix.integracao.bcb.BankAccount
import br.com.zup.edu.pix.integracao.bcb.BcbClient
import br.com.zup.edu.pix.integracao.bcb.CriaChavepixRequest
import br.com.zup.edu.pix.integracao.bcb.Owner
import br.com.zup.edu.pix.integracao.bcb.enums.AccountType
import br.com.zup.edu.pix.integracao.bcb.enums.OwnerType
import br.com.zup.edu.pix.integracao.bcb.enums.PixKeyType
import br.com.zup.edu.pix.repositories.ChavePixRepository
import io.grpc.Status
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid
import kotlin.IllegalStateException

@Validated
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContaItauClient,
    @Inject val bcbClient: BcbClient
) {
    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave)) {
            throw
            ExistingPixKeyException("Chave Pix '${novaChave.chave}' existente")
        }

        val response = itauClient.buscaConta(UUID.fromString(novaChave.clienteId)!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw PixKeyNotFoundException("Cliente não encontrado no Itau")

        val chave = novaChave.toModel(conta)
        repository.save(chave)

        val clienteItau = response.body()!!

        val bcbRequest = CriaChavepixRequest.of(chave)

        val bcbResponse = bcbClient.create(bcbRequest)
        if(bcbResponse.status != HttpStatus.CREATED){
            throw IllegalStateException("Error ao regitrar chave Pix no Banco central do Brasil")
        }

        if(chave.isAletoria()){
            chave.chave = bcbResponse.body()!!.key
        }

        return chave
    }
}