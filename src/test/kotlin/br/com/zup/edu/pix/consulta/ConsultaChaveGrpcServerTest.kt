package br.com.zup.edu.pix.consulta

import br.com.zup.edu.CarregaChavePixRequest
import br.com.zup.edu.KeymanagerConsultaGrpcServiceGrpc
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.Conta
import br.com.zup.edu.pix.integracao.bcb.*
import br.com.zup.edu.pix.integracao.bcb.enums.AccountType
import br.com.zup.edu.pix.integracao.bcb.enums.OwnerType
import br.com.zup.edu.pix.integracao.bcb.enums.PixKeyType
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaChaveGrpcServerTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceBlockingStub
){
    @Inject
    lateinit var bcbClient: BcbClient

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
                numeroDaConta =  "236906",
                cpf = "12345678998",
                titular = "Todoroki",
            )
        )

        repository.save(chavePix)
    }

    @AfterEach
    fun cleanUp(){
        repository.deleteAll()
    }

    @Test
    fun `deve carregar chave por pixId e clienteId`() {

        grpcClient.consulta(
            CarregaChavePixRequest.newBuilder()
                .setPixId(CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(chavePix.id.toString())
                    .setClienteId(CLIENTE_ID.toString())
                    .build())
                .build()
        ).let {
            assertEquals(chavePix.id.toString(), it.pixId)
            assertEquals(chavePix.clienteId.toString(), it.clienteId)
            assertEquals(chavePix.tipoDeChave.name, it.chave.tipo.name)
            assertEquals(chavePix.chave, it.chave.chave)
        }
    }

    @Test
    fun `deve achar chave usando a chave como parametro`() {
        grpcClient.consulta(
            CarregaChavePixRequest.newBuilder()
                .setChave(chavePix.chave)
                .build()
        ).let {
            assertEquals(it.clienteId, chavePix.clienteId.toString())
            assertEquals(it.chave.chave, chavePix.chave)
            assertEquals(it.pixId, chavePix.id.toString())
        }
    }

    @Test
    fun `nao deve achar chave usando clienteId incorreto`() {
        assertThrows<StatusRuntimeException> {
        grpcClient.consulta(
            CarregaChavePixRequest.newBuilder()
                .setPixId(
                    CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setClienteId(chavePix.id.toString())
                        .setPixId(chavePix.id.toString())
                        .build()
                )
                .build()
        )
        }.let {
            assertEquals(it.status.code, Status.NOT_FOUND.code)
            assertEquals(it.status.description, "Chave Pix não encontrada!")
        }
    }
    @Test
    fun `nao deve achar chave usando pixId incorreto`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                CarregaChavePixRequest.newBuilder()
                    .setPixId(
                        CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setClienteId(CLIENTE_ID.toString())
                            .setPixId(CLIENTE_ID.toString())
                            .build()
                    )
                    .build()
            )
        }.let {
            assertEquals(it.status.code, Status.NOT_FOUND.code)
            assertEquals(it.status.description, "Chave Pix não encontrada!")
        }
    }

    @Test
    fun `nao deve achar chave usando ambos parametros incorretos`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                CarregaChavePixRequest.newBuilder()
                    .setPixId(
                        CarregaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setClienteId(UUID.randomUUID().toString())
                            .setPixId(UUID.randomUUID().toString())
                            .build()
                    )
                    .build()
            )
        }.let {
            assertEquals(it.status.code, Status.NOT_FOUND.code)
            assertEquals(it.status.description, "Chave Pix não encontrada!")
        }
    }

    @Test
    fun `deve achar chave no Banco Central do Brasil passando a chave`() {

        val chave = UUID.randomUUID().toString()

        `when`(bcbClient.findByKey(chave)).thenReturn(HttpResponse.ok(detalheChavePixResponse(chave)))

        grpcClient.consulta(
                CarregaChavePixRequest.newBuilder()
                    .setChave(chave)
                    .build()
            ).let {
            assertEquals(it.chave.chave, chave)
            assertEquals(it.chave.tipo.name, TipoDeChave.ALEATORIA.name)
            assertEquals(it.chave.conta.agencia, "0001")
            assertEquals(it.chave.conta.numeroDaConta, "236906")
        }
    }

    @Test
    fun `nao deve achar chave no Banco Central do Brasil passando a chave invalida`() {

        val chave = UUID.randomUUID().toString()

        `when`(bcbClient.findByKey(chave)).thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
        grpcClient.consulta(
            CarregaChavePixRequest.newBuilder()
                .setChave(chave)
                .build()
        )
        }.let {
            assertEquals(it.status.code, Status.NOT_FOUND.code)
            assertEquals(it.status.description, "Chave pix não encontrada!")
        }
    }

    @Test
    fun `nao deve achar nada sem nenhum parametro passado`() {

        val chave = UUID.randomUUID().toString()

        `when`(bcbClient.findByKey(chave)).thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                CarregaChavePixRequest.newBuilder()
                    .build()
            )
        }.let {
            assertEquals(it.status.code, Status.INVALID_ARGUMENT.code)
            assertEquals(it.status.description, "Chave pix inválida ou não informada!")
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerConsultaGrpcServiceGrpc.KeymanagerConsultaGrpcServiceBlockingStub? {
            return KeymanagerConsultaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun detalheChavePixResponse(chave: String): DetalheChavePixResponse? {
        return DetalheChavePixResponse(
            keyType = PixKeyType.RANDOM,
            key = chave,
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "60701190",
            branch = "0001",
            accountNumber = "236906",
            accountType = AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = OwnerType.NATURAL_PERSON,
            name = "Todoroki",
            taxIdNumber = "12345678998"
        )
    }
}