package br.com.fiap.vinsight_api.ordemservico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    // Historico consolidado: todas as ordens de todos os veiculos do cliente, dentro e fora da rede
    @Query("""
            SELECT new br.com.fiap.vinsight_api.ordemservico.ResumoOrdens(
                       COUNT(o), AVG(o.valor), MAX(o.dataServico))
            FROM OrdemServico o
            WHERE o.veiculo.cliente.id = :clienteId
            """)
    ResumoOrdens resumirPorCliente(@Param("clienteId") Long clienteId);
}
