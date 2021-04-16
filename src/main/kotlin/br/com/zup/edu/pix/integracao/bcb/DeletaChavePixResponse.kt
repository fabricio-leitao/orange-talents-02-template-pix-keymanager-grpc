package br.com.zup.edu.pix.integracao.bcb

import java.time.LocalDateTime

data class DeletaChavePixResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)
