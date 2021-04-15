package br.com.zup.edu.pix.removeChave

import br.com.zup.edu.pix.registrachave.annotations.ValidUUID
import br.com.zup.edu.pix.exceptions.PixKeyNotFoundException
import br.com.zup.edu.pix.registrachave.repositories.ChavePixRepository
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChavePixService(
    @Inject val repository: ChavePixRepository
) {

    @Transactional
    fun remove(
        @NotBlank @ValidUUID(message = "cliente ID com formato inválido") clienteId: String?,
        @NotBlank @ValidUUID(message = "pix ID com formato inválido") pixId: String?
    ){
        val pixIdUUID = UUID.fromString(pixId)
        val clienteIdUUID = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(pixIdUUID, clienteIdUUID)
            .orElseThrow { IllegalStateException("Chave Pix não encontrada!") }

        repository.deleteById(pixIdUUID)
    }
}