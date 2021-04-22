package br.com.zup.edu.pix.consulta

import br.com.zup.edu.CarregaChavePixResponse
import br.com.zup.edu.TipoDeChave
import br.com.zup.edu.TipoDeConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConsultaChavePixResponseConverter {

    fun convert(chavePixInfo: ChavePixInfo): CarregaChavePixResponse {
        return CarregaChavePixResponse.newBuilder()
            .setClienteId(chavePixInfo.clienteId?.toString() ?: "")
            .setPixId(chavePixInfo.pixId?.toString() ?: "")
            .setChave(
                CarregaChavePixResponse.ChavePix.newBuilder()
                    .setTipo(TipoDeChave.valueOf(chavePixInfo.tipoDeChave.name))
                    .setChave(chavePixInfo.chave)
                    .setConta(
                        CarregaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                            .setTipo(TipoDeConta.valueOf(chavePixInfo.tipoDeConta.name))
                            .setInstituicao(chavePixInfo.conta.instituicao)
                            .setNomeDoTitular(chavePixInfo.conta.titular)
                            .setCpfDoTitular(chavePixInfo.conta.cpf)
                            .setAgencia(chavePixInfo.conta.agencia)
                            .setNumeroDaConta(chavePixInfo.conta.numeroDaConta)
                            .build()
                    )
                    .setCriadaEm(chavePixInfo.registradaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            ).build()
    }
}
