package br.com.zup.edu.pix.removeChave

import br.com.zup.edu.pix.registrachave.annotations.ValidUUID
import br.com.zup.edu.pix.integracao.bcb.BcbClient
import br.com.zup.edu.pix.integracao.bcb.DeletaChavepixRequest
import br.com.zup.edu.pix.repositories.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank
import kotlin.IllegalStateException

@Validated
@Singleton
class RemoveChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient
) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID clienteId: String?,
        @NotBlank @ValidUUID pixId: String?
    ){
        val pixIdUUID = UUID.fromString(pixId)
        val clienteIdUUID = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(pixIdUUID, clienteIdUUID)
            .orElseThrow { IllegalStateException("Chave Pix não encontrada!") }

        repository.delete(chave)

        val request = DeletaChavepixRequest(chave.chave)

        val bcbDelete = bcbClient.delete(key = chave.chave, request = request)

            if(bcbDelete.status != HttpStatus.OK){
                throw IllegalStateException("Erro ao remover chave pix no banco central!")
            }

        repository.deleteById(pixIdUUID)
    }
}