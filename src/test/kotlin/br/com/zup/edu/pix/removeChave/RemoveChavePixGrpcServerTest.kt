package br.com.zup.edu.pix.removeChave

import br.com.zup.edu.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.Conta
import br.com.zup.edu.pix.registrachave.enums.TipoDeChave
import br.com.zup.edu.pix.registrachave.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChavePixGrpcServerTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub
){
    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup(){
        CHAVE_EXISTENTE = repository.save(chave(
            tipoDeChave = TipoDeChave.EMAIL,
            chave = "todoroki@gmail.com",
            clienteId = UUID.randomUUID()
        ))
    }

    @AfterEach
    fun cleanUp(){
        repository.deleteAll()
    }

    @Test
    fun `remover chave pix existente`() {

        val response = grpcClient.remove(RemoveChavePixRequest.newBuilder()
            .setPixId(CHAVE_EXISTENTE.id.toString())
            .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
            .build())

        assertEquals(CHAVE_EXISTENTE.id.toString(), response.pixId)
        assertEquals(CHAVE_EXISTENTE.clienteId.toString(), response.clienteId)
    }

    @Test
    fun `nao remove chave pix quando nao existe chave`() {
        val pixIdNotExist = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(pixIdNotExist)
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .build())
        }

        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada!", status.description)
        }
    }

    @Test
    fun `nao remove chave pix quando chave existe mas pertence a outro cliente`() {
        val clienteIdDiferente = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .setClienteId(clienteIdDiferente)
                .build())
        }

        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada!", status.description)
        }
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