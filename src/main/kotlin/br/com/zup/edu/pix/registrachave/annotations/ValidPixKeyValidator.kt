package br.com.zup.edu.pix.registrachave.annotations

import br.com.zup.edu.pix.registrachave.NovaChavePix
import javax.inject.Singleton

@Singleton
class ValidPixKeyValidator: javax.validation.ConstraintValidator<ValidPixKey, NovaChavePix> {

    override fun isValid(
        value: NovaChavePix?,
        context: javax.validation.ConstraintValidatorContext
    ): Boolean {

        if (value?.tipoDeChave == null){
            return true
        }

        val valid = value.tipoDeChave.validacao(value.chave)
        if(!valid){
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                .addPropertyNode("chave").addConstraintViolation()
        }

        return valid
    }
}