package br.com.petcare.api.exception;

/** Violação de regra de negócio ou dado inválido que a validação de campos não cobre. Vira HTTP 400. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String message) {
        super(message);
    }
}
