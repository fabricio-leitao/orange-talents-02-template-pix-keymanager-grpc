package br.com.zup.edu.pix.integracao.bcb

import br.com.zup.edu.pix.integracao.bcb.enums.AccountType

data class BankAccount (
    val participant: String?,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
    )
