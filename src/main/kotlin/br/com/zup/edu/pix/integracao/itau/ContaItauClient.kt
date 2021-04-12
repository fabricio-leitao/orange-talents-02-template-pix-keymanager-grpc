package br.com.zup.edu.pix.integracao.itau

import br.com.zup.edu.pix.integracao.itau.DadosContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.util.*

@Client("\${itau.contas.url}")
interface ContaItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscaConta(@PathVariable clienteId: UUID, @QueryValue tipo: String): HttpResponse<DadosContaResponse>
}