package br.com.zup.edu.pix.registrachave.exceptions

import io.grpc.Status
import io.grpc.StatusException

class ExistingPixKeyException(message: Status): StatusException(message)