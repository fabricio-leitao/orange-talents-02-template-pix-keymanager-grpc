package br.com.zup.edu.pix.exceptions

import io.grpc.Status
import io.grpc.StatusException
import java.lang.RuntimeException


class ExistingPixKeyException(message: String?): RuntimeException(message)