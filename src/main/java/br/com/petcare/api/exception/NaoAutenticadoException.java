package br.com.petcare.api.exception;

/** Credencial ou token ausente, inválido ou expirado. Vira HTTP 401. */
public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException(String message) {
        super(message);
    }
}
