package br.com.zup.edu.pix.registrachave

import br.com.zup.edu.ChavePixRequest
import br.com.zup.edu.KeymanagerGrpcServiceGrpc
import br.com.zup.edu.TipoDeChave
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.Conta
import br.com.zup.edu.pix.integracao.itau.ContaItauClient
import br.com.zup.edu.pix.integracao.itau.DadosContaResponse
import br.com.zup.edu.pix.integracao.itau.InstituicaoResponse
import br.com.zup.edu.pix.integracao.itau.TitularResponse
import br.com.zup.edu.pix.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional

@MicronautTest(transactional = false)
class KeyManagerGrpcServerTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceBlockingStub
) {
    @Inject
    lateinit var itauClient: ContaItauClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `registra nova chave pix com email`() {
        `when`(itauClient.buscaConta(clienteId = CLIENTE_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosContaResponse()))

        val response = grpcClient.registra(
            ChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("todoroki@gmail.com")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao registra nova chave pix`() {

        repository.save(
            chave(
                tipoDeChave = br.com.zup.edu.pix.registrachave.enums.TipoDeChave.CPF,
                chave = "63657520325",
                clienteId = CLIENTE_ID
            )
        )

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                ChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("63657520325")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '63657520325' existente", status.description)
        }
    }

    @Test
    fun `nao registra nova chave pix quando nao tiver dados do cliente`() {

        `when`(itauClient.buscaConta(clienteId = CLIENTE_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                ChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("todoroki@gmail.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }
    }

    @Test
    fun `nao registra nova chave pix com cpf invalido`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                ChavePixRequest.newBuilder()
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("registra.novaChave.chave: O valor da chave é inválido!", status.description)
        }
    }

    @Test
    fun `nao registra nova chave pix com conta invalida`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                ChavePixRequest.newBuilder()
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("63657520325")
                    .setClienteId(CLIENTE_ID.toString())
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("registra.novaChave.tipoDeConta: não deve ser nulo", status.description)
        }
    }
    @Test
    fun `nao registra nova chave pix com chave invalida`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                ChavePixRequest.newBuilder()
                    .setChave("63657520325")
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("registra.novaChave.tipoDeChave: não deve ser nulo", status.description)
        }
    }

    @Test
    fun `nao registra nova chave pix com cliente em branco`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                ChavePixRequest.newBuilder()
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("63657520325")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("registra.novaChave.clienteId: não deve estar em branco", status.description)
        }
    }


    @MockBean(ContaItauClient::class)
    fun itauClient(): ContaItauClient? {
        return Mockito.mock(ContaItauClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerGrpcServiceGrpc.KeymanagerGrpcServiceBlockingStub {
            return KeymanagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosContaResponse(): DadosContaResponse {
        return DadosContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", Conta.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse("Todoroki Shoto", "12312312323")
        )
    }

    private fun chave(
        tipoDeChave: br.com.zup.edu.pix.registrachave.enums.TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID()

    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoDeChave = tipoDeChave,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = Conta(
                instituicao = "UNIBANCO ITAU",
                titular = "Rafael Ponte",
                cpf = "63657520325",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }

}