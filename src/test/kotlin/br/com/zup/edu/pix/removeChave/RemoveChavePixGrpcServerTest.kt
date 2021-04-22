package br.com.zup.edu.pix.removeChave

import br.com.zup.edu.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.Conta
import br.com.zup.edu.pix.integracao.bcb.BcbClient
import br.com.zup.edu.pix.integracao.bcb.DeletaChavePixResponse
import br.com.zup.edu.pix.integracao.bcb.DeletaChavepixRequest
import br.com.zup.edu.pix.registrachave.enums.TipoDeChave
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChavePixGrpcServerTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BcbClient

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup() {

        CHAVE_EXISTENTE = repository.save(
            chave(
                tipoDeChave = TipoDeChave.EMAIL,
                chave = "todoroki@gmail.com",
                clienteId = UUID.randomUUID()
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `remover chave pix existente`() {

        `when`(bcbClient.delete("todoroki@gmail.com", DeletaChavepixRequest("todoroki@gmail.com")))
            .thenReturn(
                HttpResponse.ok(
                    DeletaChavePixResponse(
                        key = "todoroki@gmail.com",
                        participant = Conta.ITAU_UNIBANCO_ISPB,
                        deletedAt = LocalDateTime.now()
                    )
                )
            )

        grpcClient.remove(
            RemoveChavePixRequest.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .build()
        ).let {
            assertNotNull(it.pixId)
            assertNotNull(it.clienteId)
            assertEquals(it.pixId, CHAVE_EXISTENTE.id.toString())
            assertEquals(it.clienteId, CHAVE_EXISTENTE.clienteId.toString())
            assertEquals(
                0, repository.findAll().size
            )

        }
    }

    @Test
    fun `nao remove chave pix quando ocorrer erro no BCB`() {
        `when`(bcbClient.delete("todoroki@gmail.com", DeletaChavepixRequest("todoroki@gmail.com")))
            .thenReturn(HttpResponse.unprocessableEntity())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover chave pix no banco central!", status.description)
        }
    }

    @Test
    fun `nao remove chave pix quando nao existe chave`() {
        val pixIdNotExist = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(pixIdNotExist)
                    .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave Pix não encontrada!", status.description)
        }
    }

    @Test
    fun `nao remove chave pix quando chave existe mas pertence a outro cliente`() {
        val clienteIdDiferente = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .setClienteId(clienteIdDiferente)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave Pix não encontrada!", status.description)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub {
            return KeymanagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipoDeChave: TipoDeChave,
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