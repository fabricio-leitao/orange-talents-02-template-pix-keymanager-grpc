package br.com.zup.edu.pix.exceptions

import java.lang.RuntimeException

class ClientNotFoundException(mensagem: String) : RuntimeException(mensagem)