package br.com.zup.edu.pix.consulta

import br.com.zup.edu.pix.exceptions.PixKeyNotFoundException
import br.com.zup.edu.pix.integracao.bcb.BcbClient
import br.com.zup.edu.pix.registrachave.annotations.ValidUUID
import br.com.zup.edu.pix.repositories.ChavePixRepository
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro{

    abstract fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String
    ): Filtro(){

        fun pixIdAsUuid() = UUID.fromString(pixId)
        fun clienteIdAsUuid() = UUID.fromString(clienteId)

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            return repository.findById(pixIdAsUuid())
                .filter { it.pertenceAo(clienteIdAsUuid()) }
                .map(ChavePixInfo::of)
                .orElseThrow { PixKeyNotFoundException("Chave Pix não encontrada!") }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String): Filtro(){

        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            return repository.findByChave(chave)
                .map(ChavePixInfo::of)
                .orElseGet {
                    LOGGER.info("Consultando chave Pix '$chave' no Banco Central do Brasil!")

                    val response = bcbClient.findByKey(chave)
                    when (response.status){
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw PixKeyNotFoundException("Chave pix não encontrada!")
                    }
                }
        }
    }

    @Introspected
    class Invalido(): Filtro(){
        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): ChavePixInfo {
            throw IllegalArgumentException("Chave pix inválida ou não informada!")
        }
    }
}
