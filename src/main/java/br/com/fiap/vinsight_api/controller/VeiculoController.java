package br.com.fiap.vinsight_api.controller;

import br.com.fiap.vinsight_api.agendamento.AgendamentoService;
import br.com.fiap.vinsight_api.agendamento.DadosListagemAgendamento;
import br.com.fiap.vinsight_api.veiculo.DadosAtualizacaoVeiculo;
import br.com.fiap.vinsight_api.veiculo.DadosCadastroVeiculo;
import br.com.fiap.vinsight_api.veiculo.DadosDetalheVeiculo;
import br.com.fiap.vinsight_api.veiculo.DadosListagemVeiculo;
import br.com.fiap.vinsight_api.veiculo.VeiculoService;
import br.com.fiap.vinsight_api.shared.DadosPagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/veiculos")
@Tag(name = "Veículos", description = "Cadastro e gestão de veículos Ford")
@SecurityRequirement(name = "bearer-key")
public class VeiculoController {

    @Autowired
    private VeiculoService service;

    @Autowired
    private AgendamentoService agendamentoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CONSULTOR', 'GERENTE', 'ADMIN')")
    @Operation(summary = "Cadastra um novo veículo")
    public ResponseEntity<DadosDetalheVeiculo> cadastrar(
            @RequestBody @Valid DadosCadastroVeiculo dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalheVeiculo veiculo = service.cadastrar(dados);
        var uri = uriBuilder.path("/api/v1/veiculos/{id}")
                .buildAndExpand(veiculo.id()).toUri();
        return ResponseEntity.created(uri).body(veiculo);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CONSULTOR', 'GERENTE', 'ADMIN')")
    @Operation(summary = "Lista veículos paginados (exclui INATIVOs)")
    public ResponseEntity<DadosPagina<DadosListagemVeiculo>> listar(
            @PageableDefault(size = 20, sort = "id") Pageable paginacao) {
        return ResponseEntity.ok(new DadosPagina<>(service.listar(paginacao)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONSULTOR', 'GERENTE', 'ADMIN')")
    @Operation(summary = "Detalha um veículo por ID")
    public ResponseEntity<DadosDetalheVeiculo> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @GetMapping("/vin/{vin}")
    @PreAuthorize("hasAnyRole('CONSULTOR', 'GERENTE', 'ADMIN')")
    @Operation(summary = "Detalha um veículo pelo VIN")
    public ResponseEntity<DadosDetalheVeiculo> detalharPorVin(@PathVariable String vin) {
        return ResponseEntity.ok(service.detalharPorVin(vin));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONSULTOR', 'GERENTE', 'ADMIN')")
    @Operation(summary = "Atualiza um veículo")
    public ResponseEntity<DadosDetalheVeiculo> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid DadosAtualizacaoVeiculo dados) {
        return ResponseEntity.ok(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    @Operation(summary = "Inativa um veículo (status=INATIVO)")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/agendamentos")
    @PreAuthorize("hasAnyRole('CONSULTOR', 'GERENTE', 'ADMIN')")
    @Operation(summary = "Lista agendamentos de um veículo (paginado)")
    public ResponseEntity<DadosPagina<DadosListagemAgendamento>> listarAgendamentos(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "dataHora") Pageable paginacao) {
        return ResponseEntity.ok(new DadosPagina<>(agendamentoService.listarPorVeiculo(id, paginacao)));
    }
}
