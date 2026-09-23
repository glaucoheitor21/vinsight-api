package br.com.fiap.vinsight_api.veiculo;

import br.com.fiap.vinsight_api.cliente.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    /**
     * Listagem/busca por placa, restrita aos veiculos cujo DONO esta na carteira da unidade
     * (mesma regra dos clientes: ClienteRepository.CARTEIRA, que usa os aliases "c" e "cad").
     * O alias do veiculo e "ve" porque a CARTEIRA ja usa "v" numa subconsulta.
     */
    @Query(value = """
            SELECT ve FROM Veiculo ve JOIN ve.cliente c LEFT JOIN c.concessionariaCadastro cad
            WHERE ve.status <> :statusExcluido
              AND (:placa IS NULL OR ve.placa = :placa)
              AND """ + ClienteRepository.CARTEIRA)
    Page<Veiculo> buscar(@Param("placa") String placa,
                         @Param("statusExcluido") StatusVeiculo statusExcluido,
                         @Param("concessionariaId") Long concessionariaId,
                         Pageable paginacao);

    Optional<Veiculo> findByVin(String vin);

    List<Veiculo> findAllByClienteId(Long clienteId);

    boolean existsByVin(String vin);

    boolean existsByPlaca(String placa);
}
