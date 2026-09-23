package br.com.fiap.vinsight_api.lead;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    /**
     * Regras da fila (US-35), aplicadas SEMPRE, antes de qualquer filtro do usuario:
     * - lead suprimido nao entra (registro de supressao, ex.: LGPD_OPT_OUT);
     * - cliente sem consentimento ativo nao entra, mesmo que a supressao ainda nao tenha sido
     *   registrada (defesa em profundidade: revogou, sumiu da fila na hora);
     * - escopo da unidade (US-30): :concessionariaId NULL = rede inteira.
     * Filtros opcionais: status, faixa de risco (via limites de score) e cliente.
     */
    String FILTRO_FILA = """
            WHERE l.suprimidoMotivo IS NULL
              AND c.consentimentoAtivo = true
              AND (:concessionariaId IS NULL OR conc.id = :concessionariaId)
              AND (:status IS NULL OR l.status = :status)
              AND (:scoreMinimo IS NULL OR l.score >= :scoreMinimo)
              AND (:scoreMaximo IS NULL OR l.score < :scoreMaximo)
              AND (:clienteId IS NULL OR c.id = :clienteId)
            """;

    // JOIN FETCH traz cliente e veiculo na mesma consulta (sem N+1 ao montar os 20 itens da pagina)
    @Query(value = """
            SELECT l FROM Lead l
            JOIN FETCH l.cliente c JOIN FETCH l.veiculo v LEFT JOIN l.concessionaria conc
            """ + FILTRO_FILA,
            countQuery = """
            SELECT COUNT(l) FROM Lead l
            JOIN l.cliente c LEFT JOIN l.concessionaria conc
            """ + FILTRO_FILA)
    Page<Lead> fila(@Param("status") StatusLead status,
                    @Param("scoreMinimo") Double scoreMinimo,
                    @Param("scoreMaximo") Double scoreMaximo,
                    @Param("clienteId") Long clienteId,
                    @Param("concessionariaId") Long concessionariaId,
                    Pageable paginacao);
}
