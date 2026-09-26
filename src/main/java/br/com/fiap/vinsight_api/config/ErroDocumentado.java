package br.com.fiap.vinsight_api.config;

import br.com.fiap.vinsight_api.infra.exception.TipoProblema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documenta no Swagger um erro de REGRA DE NEGOCIO que o endpoint pode responder (ex.: CPF ja
 * cadastrado, lead ja encerrado). Os erros estruturais (401, 403, 404, 400, 422, 500) nao precisam
 * disto: DocumentacaoRespostas os deduz da assinatura do metodo.
 *
 * Pode repetir: dois erros com o mesmo status viram dois exemplos na mesma resposta.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ErroDocumentado.Lista.class)
public @interface ErroDocumentado {

    TipoProblema tipo();

    /** Quando o erro acontece, em linguagem de negocio. Vira a descricao e o "detail" do exemplo. */
    String quando();

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Lista {
        ErroDocumentado[] value();
    }
}
