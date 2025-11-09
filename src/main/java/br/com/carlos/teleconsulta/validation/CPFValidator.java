package br.com.carlos.teleconsulta.validation;

import br.com.carlos.teleconsulta.util.CpfCnpjUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPF, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Não valida null/blank aqui — deixe @NotBlank cuidar disso
        if (value == null || value.isBlank()) return true;
        return CpfCnpjUtils.isValidCPF(value);
    }
}
