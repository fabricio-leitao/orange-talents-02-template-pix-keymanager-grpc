package br.com.zup.edu.pix.registrachave.enums

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoDeChaveTest {

    @Nested
    inner class ALEATORIA {

        @Test
        fun `deve ser valido quando a chave aleatoria for nula ou vazia`() {
            with(TipoDeChave.ALEATORIA){
                assertTrue(validacao(null))
                assertTrue(validacao(""))
            }
        }

        @Test
        fun `nao deve ser valido quando a chave alatoria possuir um valor`() {
            with(TipoDeChave.ALEATORIA){
                assertFalse(validacao("valor preenchido"))
            }
        }
    }

    @Nested
    inner class CPF {
        @Test
        fun `deve ser valido quando o cpf for um numero valido`() {
            with(TipoDeChave.CPF){
                assertTrue(validacao("11897471033"))
            }
        }

        @Test
        fun `nao deve ser valido quando o cpf for um numero invalido`() {
            with(TipoDeChave.CPF){
                assertFalse(validacao("11897471032"))
            }
        }

        @Test
        fun `nao deve ser valido quando o cpf for nulo ou invalido`() {
            with(TipoDeChave.CPF){
                assertFalse(validacao(null))
                assertFalse(validacao(""))
            }
        }
    }

    @Nested
    inner class CELULAR {

        @Test
        fun `deve ser valido quando celular for um numero valido`() {
            with(TipoDeChave.CELULAR){
                assertTrue(validacao("+55219999999999"))
            }
        }

        @Test
        fun `nao deve ser valido quando celular for um numero invalido`() {
            with(TipoDeChave.CELULAR){
                assertFalse(validacao("21999999999"))
                assertFalse(validacao("+55fd21999999999"))
            }
        }

        @Test
        fun `nao deve ser valido quando celular for nulo ou vazio`() {
            with(TipoDeChave.CELULAR){
                assertFalse(validacao(null))
                assertFalse(validacao(""))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve ser valido quando o email for valido`() {
            with(TipoDeChave.EMAIL){
                assertTrue(validacao("teste@teste.com.br"))
            }
        }

        @Test
        fun `nao deve ser valido quando o email for invalido`() {
            with(TipoDeChave.EMAIL){
                assertFalse(validacao("testeteste.com.br"))
                assertFalse(validacao("teste@teste.com."))
            }
        }

        @Test
        fun `nao deve ser valido quando o email for nulo ou vazio`() {
            with(TipoDeChave.EMAIL){
                assertFalse(validacao(null))
                assertFalse(validacao(""))
            }
        }
    }
}