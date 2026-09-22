package br.com.fiap.vinsight_api.shared;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope de paginacao do contrato: metadados planos na raiz, ao lado de content.
 * Todo endpoint paginado devolve este record, nunca o Page do Spring direto.
 */
public record DadosPagina<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public DadosPagina(Page<T> pagina) {
        this(pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
