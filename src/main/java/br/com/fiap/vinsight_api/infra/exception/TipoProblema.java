package br.com.fiap.vinsight_api.infra.exception;

import java.net.URI;

/**
 * Catalogo dos "type" de erro da API (RFC 7807). O app trata erro por este campo, nunca pela
 * mensagem, entao um valor publicado aqui e contrato: renomear quebra o app.
 */
public enum TipoProblema {

    REQUISICAO_INVALIDA("requisicao-invalida", "Requisição inválida"),
    VALIDACAO("validacao", "Erro de validação"),
    NAO_AUTENTICADO("nao-autenticado", "Autenticação necessária"),
    CREDENCIAIS_INVALIDAS("credenciais-invalidas", "Credenciais inválidas"),
    TOKEN_EXPIRADO("token-expirado", "Token expirado"),
    TOKEN_INVALIDO("token-invalido", "Token inválido"),
    PERFIL_SEM_PERMISSAO("perfil-sem-permissao", "Acesso negado"),
    OUTRA_CONCESSIONARIA("outra-concessionaria", "Acesso negado"),
    NAO_ENCONTRADO("nao-encontrado", "Recurso não encontrado"),
    METODO_NAO_PERMITIDO("metodo-nao-permitido", "Método não permitido"),
    FORMATO_NAO_SUPORTADO("formato-nao-suportado", "Formato não suportado"),
    CONFLITO("conflito", "Conflito"),
    ERRO_INTERNO("erro-interno", "Erro interno");

    private static final String BASE = "https://vinsight.ford/errors/";

    private final String codigo;
    private final String titulo;

    TipoProblema(String codigo, String titulo) {
        this.codigo = codigo;
        this.titulo = titulo;
    }

    public URI uri() {
        return URI.create(BASE + codigo);
    }

    public String titulo() {
        return titulo;
    }

    /** Para as excecoes do proprio Spring MVC, que chegam so com o status. */
    public static TipoProblema porStatus(int status) {
        return switch (status) {
            case 400 -> REQUISICAO_INVALIDA;
            case 401 -> NAO_AUTENTICADO;
            case 403 -> PERFIL_SEM_PERMISSAO;
            case 404 -> NAO_ENCONTRADO;
            case 405 -> METODO_NAO_PERMITIDO;
            case 406, 415 -> FORMATO_NAO_SUPORTADO;
            case 409 -> CONFLITO;
            case 422 -> VALIDACAO;
            default -> status >= 500 ? ERRO_INTERNO : REQUISICAO_INVALIDA;
        };
    }
}
