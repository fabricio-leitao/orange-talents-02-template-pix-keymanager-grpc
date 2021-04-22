package br.com.zup.edu.pix.integracao.bcb

import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.Conta
import br.com.zup.edu.pix.integracao.bcb.enums.AccountType
import br.com.zup.edu.pix.integracao.bcb.enums.OwnerType
import br.com.zup.edu.pix.integracao.bcb.enums.PixKeyType
import java.time.LocalDateTime

data class CriaChavepixRequest(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
){
    companion object {
        fun of(chavePix: ChavePix): CriaChavepixRequest {
            return CriaChavepixRequest(
                keyType = PixKeyType.by(chavePix.tipoDeChave),
                key = chavePix.chave,
                bankAccount = BankAccount(
                    participant = Conta.ITAU_UNIBANCO_ISPB,
                    branch = chavePix.conta.agencia,
                    accountNumber = chavePix.conta.numeroDaConta,
                    accountType = AccountType.by(chavePix.tipoDeConta)
                ),
                owner = Owner(
                    type = OwnerType.NATURAL_PERSON,
                    name = chavePix.conta.titular,
                    taxIdNumber = chavePix.conta.cpf
                )
            )
        }
    }
}