package br.com.fiap.vinsight_api.lead;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    @Query("""
            SELECT l FROM Lead l
            WHERE (:prioridade IS NULL OR l.prioridade = :prioridade)
              AND (:status IS NULL OR l.status = :status)
              AND (:clienteId IS NULL OR l.cliente.id = :clienteId)
            """)
    Page<Lead> buscarComFiltros(
            @Param("prioridade") PrioridadeLead prioridade,
            @Param("status") StatusLead status,
            @Param("clienteId") Long clienteId,
            Pageable paginacao);
}
