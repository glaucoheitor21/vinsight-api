package br.com.fiap.vinsight_api.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Carteira por relacionamento (escopo da US-30 aplicado a clientes): o cliente pertence a
     * TODA unidade com que tem vinculo. Basta um dos quatro:
     *   1. foi cadastrado nela                        (clientes.concessionaria_cadastro_id)
     *   2. comprou nela algum dos seus veiculos       (veiculos.concessionaria_compra_id)
     *   3. fez servico nela com algum desses veiculos (ordens_servico.concessionaria_id)
     *   4. tem agendamento nela                       (agendamentos.concessionaria_id)
     * :concessionariaId NULL = sem restricao (ANALISTA_FORD e ADMIN).
     *
     * Exige "LEFT JOIN c.concessionariaCadastro cad" na consulta que usar este trecho.
     */
    String CARTEIRA = """
            (:concessionariaId IS NULL
             OR cad.id = :concessionariaId
             OR EXISTS (SELECT v.id FROM Veiculo v
                        WHERE v.cliente = c AND v.concessionariaCompra.id = :concessionariaId)
             OR EXISTS (SELECT o.id FROM OrdemServico o
                        WHERE o.veiculo.cliente = c AND o.concessionaria.id = :concessionariaId)
             OR EXISTS (SELECT a.id FROM Agendamento a
                        WHERE a.veiculo.cliente = c AND a.concessionaria.id = :concessionariaId))
            """;

    /**
     * Busca do contrato (?q=): por nome parcial OU por digitos de CPF/telefone, sempre dentro da
     * carteira. Com os dois filtros nulos, lista a carteira inteira.
     */
    @Query(value = """
            SELECT c FROM Cliente c LEFT JOIN c.concessionariaCadastro cad
            WHERE c.ativo = true
              AND (:nome IS NULL OR LOWER(c.dadosPessoais.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
              AND (:digitos IS NULL
                   OR c.dadosPessoais.cpf = :digitos
                   OR c.contato.telefone LIKE CONCAT('%', :digitos, '%'))
              AND """ + CARTEIRA)
    Page<Cliente> buscar(@Param("nome") String nome,
                         @Param("digitos") String digitos,
                         @Param("concessionariaId") Long concessionariaId,
                         Pageable paginacao);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Cliente c LEFT JOIN c.concessionariaCadastro cad
            WHERE c.id = :clienteId
              AND """ + CARTEIRA)
    boolean pertenceACarteira(@Param("clienteId") Long clienteId,
                              @Param("concessionariaId") Long concessionariaId);

    boolean existsByDadosPessoais_Cpf(String cpf);
}
