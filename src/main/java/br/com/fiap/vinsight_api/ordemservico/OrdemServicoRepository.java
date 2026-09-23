package br.com.fiap.vinsight_api.ordemservico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    // Historico consolidado: todas as ordens de todos os veiculos do cliente, dentro e fora da rede
    @Query("""
            SELECT new br.com.fiap.vinsight_api.ordemservico.ResumoOrdens(
                       COUNT(o), AVG(o.valor), MAX(o.dataServico))
            FROM OrdemServico o
            WHERE o.veiculo.cliente.id = :clienteId
            """)
    ResumoOrdens resumirPorCliente(@Param("clienteId") Long clienteId);

    // Historico do passaporte (US-34): mais recente primeiro. LEFT JOIN FETCH traz o nome da
    // concessionaria na mesma consulta e mantem as ordens fora da rede (concessionaria NULL).
    @Query("""
            SELECT o FROM OrdemServico o LEFT JOIN FETCH o.concessionaria
            WHERE o.veiculo.id = :veiculoId
            ORDER BY o.dataServico DESC, o.id DESC
            """)
    List<OrdemServico> historicoDoVeiculo(@Param("veiculoId") Long veiculoId);
}
