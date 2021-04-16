package br.com.zup.edu.pix.integracao.bcb

import br.com.zup.edu.pix.integracao.bcb.enums.OwnerType

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
)
