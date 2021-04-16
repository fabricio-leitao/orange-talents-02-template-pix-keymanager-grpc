package br.com.zup.edu.pix.integracao.bcb

import br.com.zup.edu.pix.integracao.bcb.enums.PixKeyType
import java.time.LocalDateTime

data class CriaChavePixResponse(

    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)
