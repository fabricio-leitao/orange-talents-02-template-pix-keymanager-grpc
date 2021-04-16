package br.com.zup.edu.pix.integracao.bcb.enums

import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.TipoDeConta.CONTA_CORRENTE
import br.com.zup.edu.TipoDeConta.CONTA_POUPANCA


enum class AccountType() {

    NOT_FOUND,
    CACC,
    SVGS;

    companion object {
        fun by(domainType: TipoDeConta): AccountType {
            return when (domainType){
                CONTA_CORRENTE -> CACC
                CONTA_POUPANCA -> SVGS
                else -> NOT_FOUND
            }
        }
    }
}
