package br.com.fiap.vinsight_api.veiculo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Page<Veiculo> findAllByStatusNot(StatusVeiculo status, Pageable paginacao);

    Optional<Veiculo> findByVin(String vin);

    List<Veiculo> findAllByClienteId(Long clienteId);

    boolean existsByVin(String vin);

    boolean existsByPlaca(String placa);
}
