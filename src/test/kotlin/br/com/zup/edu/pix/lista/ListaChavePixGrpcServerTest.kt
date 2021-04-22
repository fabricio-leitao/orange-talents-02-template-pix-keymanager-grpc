package br.com.zup.edu.pix.lista

import br.com.zup.edu.KeymanagerListaGrpcServiceGrpc
import br.com.zup.edu.ListaChavesPixRequest
import br.com.zup.edu.ListaChavesPixResponse
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.Conta
import br.com.zup.edu.pix.integracao.bcb.Instituicoes
import br.com.zup.edu.pix.registrachave.enums.TipoDeChave
import br.com.zup.edu.pix.repositories.ChavePixRepository
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
internal class ListaChavePixGrpcServerTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub
){
    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    private lateinit var chavePix: ChavePix

    @BeforeEach
    fun setup(){

        chavePix = ChavePix(
            clienteId = CLIENTE_ID,
            tipoDeChave = TipoDeChave.ALEATORIA,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            chave = UUID.randomUUID().toString(),
            conta = Conta(
                instituicao = Instituicoes.nome("60701190"),
                agencia = "0001",
                numeroDaConta = "236906",
                cpf = "12134567898",
                titular = "Todoroki"
            )
        )

        repository.save(chavePix)
    }

    @AfterEach
    fun cleanUp(){
        repository.deleteAll()
    }

    @Test
    fun `deve devolver uma lista com as chaves cadastradas do clienteId`() {
        grpcClient.lista(
            ListaChavesPixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .build()
        ).let {
            assertEquals(it.chavesList.size, 1)
            assertEquals(it.clienteId, CLIENTE_ID.toString())
            assertEquals(it.chavesList[0].chave, chavePix.chave)
        }
    }

    @Test
    fun `deve devolver uma lista vazia`() {
        val cliente = UUID.randomUUID().toString()

        grpcClient.lista(
            ListaChavesPixRequest.newBuilder()
                .setClienteId(cliente)
                .build()
        ).let {
            assertEquals(it.chavesList.size, 0)
            assertEquals(it.clienteId.toString(), cliente)
        }
    }

    @Test
    fun `nao deve devolver uma lista sem passar clienteId como parametro`() {
        assertThrows<StatusRuntimeException> {
        grpcClient.lista(
            ListaChavesPixRequest.newBuilder()
                .build()
        )
        }.let {
            assertEquals(it.status.code, Status.INVALID_ARGUMENT.code)
            assertEquals(it.status.description, "ClienteId não pode ser nulo ou vazio")
        }
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME)channel: ManagedChannel): KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub? {
            return KeymanagerListaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }


}