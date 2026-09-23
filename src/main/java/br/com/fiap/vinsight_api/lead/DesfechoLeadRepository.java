package br.com.fiap.vinsight_api.lead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesfechoLeadRepository extends JpaRepository<DesfechoLead, Long> {

    List<DesfechoLead> findAllByLeadIdOrderByRegistradoEmDescIdDesc(Long leadId);
}
