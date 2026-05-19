package br.com.fiap.vinsight_api.concessionaria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcessionariaRepository extends JpaRepository<Concessionaria, Long> {

    Page<Concessionaria> findAllByAtivoTrue(Pageable paginacao);

    boolean existsByCnpj(String cnpj);
}
