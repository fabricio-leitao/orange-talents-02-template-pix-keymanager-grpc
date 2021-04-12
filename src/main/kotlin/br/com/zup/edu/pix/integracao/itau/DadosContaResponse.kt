package br.com.zup.edu.pix.integracao.itau

import br.com.zup.edu.pix.Conta

data class DadosContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {

    fun toModel(): Conta {
        return Conta(
            instituicao = this.instituicao.nome,
            titular = this.titular.nome,
            cpf = this.titular.cpf,
            agencia = this.agencia,
            numeroDaConta = numero
        )
    }
}
