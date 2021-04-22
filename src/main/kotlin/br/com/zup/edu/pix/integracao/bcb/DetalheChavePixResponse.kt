package br.com.zup.edu.pix.integracao.bcb

import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.pix.Conta
import br.com.zup.edu.pix.consulta.ChavePixInfo
import br.com.zup.edu.pix.integracao.bcb.enums.AccountType
import br.com.zup.edu.pix.integracao.bcb.enums.PixKeyType
import java.time.LocalDateTime

data class DetalheChavePixResponse(

    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime

) {

    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipoDeChave = keyType.domainType!!,
            chave = this.key,
            tipoDeConta = when (this.bankAccount.accountType) {
                AccountType.CACC -> TipoDeConta.CONTA_CORRENTE
                AccountType.SVGS -> TipoDeConta.CONTA_POUPANCA
                else -> TipoDeConta.CONTA_DESCONHECIDA

            },
            conta = Conta(
                instituicao = Instituicoes.nome(bankAccount.participant),
                titular = owner.name,
                cpf = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber
            )
        )
    }
}
