package br.com.zup.edu.pix.registrachave.annotations

import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.registrachave.NovaChavePix
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton

@Singleton
class ValidPixKeyValidator: ConstraintValidator<ValidPixKey, NovaChavePix> {

    override fun isValid(
        value: NovaChavePix?,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext
    ): Boolean {

        if (value?.tipoDeChave == null){
            return false
        }

        return value.tipoDeChave.validacao(value.chave)
    }
}

