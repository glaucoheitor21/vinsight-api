package br.com.fiap.vinsight_api.infra.exception;

/**
 * Violacao de campo detectada no service, e nao pelo Bean Validation.
 * Sai como 422 "validacao", no mesmo formato das violacoes do @Valid.
 */
public class CampoInvalidoException extends RuntimeException {

    private final String campo;

    public CampoInvalidoException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
