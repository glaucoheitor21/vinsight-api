package br.com.fiap.vinsight_api.infra.idempotencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequisicaoIdempotenteRepository extends JpaRepository<RequisicaoIdempotente, Long> {

    Optional<RequisicaoIdempotente> findByChaveAndMetodoAndRecurso(String chave, String metodo, String recurso);
}
