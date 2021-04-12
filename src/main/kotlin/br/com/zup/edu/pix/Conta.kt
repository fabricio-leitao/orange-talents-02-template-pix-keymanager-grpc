package br.com.zup.edu.pix

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size


@Embeddable
data class Conta(

    @field:NotBlank
    @Column(nullable = false)
    val instituicao: String,
    @field:NotBlank
    @Column(nullable = false)
    val titular: String,
    @field:NotBlank
    @Column(nullable = false, length = 11)
    @field:Size(max = 11)
    val cpf: String,
    @field:NotBlank
    @field:Size(max = 4)
    @Column(nullable = false, length = 4)
    val agencia: String,
    @field:NotBlank
    @field:Size(max = 6)
    @Column(nullable = false, length = 6)
    val numeroDaConta: String
)
