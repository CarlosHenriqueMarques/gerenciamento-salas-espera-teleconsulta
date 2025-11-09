package br.com.carlos.teleconsulta.validation;

import br.com.carlos.teleconsulta.util.CpfCnpjUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CNPJValidator implements ConstraintValidator<CNPJ, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        return CpfCnpjUtils.isValidCNPJ(value);
    }
}
