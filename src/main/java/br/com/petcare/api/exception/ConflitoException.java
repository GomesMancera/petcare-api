package br.com.petcare.api.exception;

/** Conflito com o estado atual do sistema (horário ocupado, e-mail já cadastrado). Vira HTTP 409. */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String message) {
        super(message);
    }
}
