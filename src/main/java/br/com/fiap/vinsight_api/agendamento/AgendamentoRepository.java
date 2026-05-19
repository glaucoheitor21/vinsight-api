package br.com.fiap.vinsight_api.agendamento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("""
            SELECT a FROM Agendamento a
            WHERE (:dataInicio IS NULL OR a.dataHora >= :dataInicio)
              AND (:dataFim IS NULL OR a.dataHora <= :dataFim)
              AND (:concessionariaId IS NULL OR a.concessionaria.id = :concessionariaId)
              AND (:status IS NULL OR a.status = :status)
            """)
    Page<Agendamento> buscarComFiltros(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("concessionariaId") Long concessionariaId,
            @Param("status") StatusAgendamento status,
            Pageable paginacao);

    Page<Agendamento> findAllByVeiculoId(Long veiculoId, Pageable paginacao);

    Page<Agendamento> findAllByConcessionariaId(Long concessionariaId, Pageable paginacao);
}
