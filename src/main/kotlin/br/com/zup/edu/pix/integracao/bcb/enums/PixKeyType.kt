package br.com.zup.edu.pix.integracao.bcb.enums

import br.com.zup.edu.pix.registrachave.enums.TipoDeChave
import br.com.zup.edu.pix.registrachave.enums.TipoDeChave.*
import java.lang.IllegalArgumentException

enum class PixKeyType( val domainType: TipoDeChave?) {

    CPF(TipoDeChave.CPF),
    CNPJ(null),
    PHONE(CELULAR),
    EMAIL(TipoDeChave.EMAIL),
    RANDOM(ALEATORIA);

    companion object {
        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)

        fun by(domainType: TipoDeChave): PixKeyType {
            return mapping[domainType] ?: throw IllegalArgumentException("Tipo de chave pix não encontrado: $domainType")
        }
    }

}
