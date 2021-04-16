package br.com.zup.edu.pix.integracao.bcb

import br.com.zup.edu.pix.Conta

data class DeletaChavepixRequest(
    val key: String,
    val participant: String = Conta.ITAU_UNIBANCO_ISPB
)
