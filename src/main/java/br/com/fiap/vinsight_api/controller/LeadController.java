package br.com.fiap.vinsight_api.controller;

import br.com.fiap.vinsight_api.lead.DadosAtualizacaoStatusLead;
import br.com.fiap.vinsight_api.lead.DadosCadastroLead;
import br.com.fiap.vinsight_api.lead.DadosDetalheLead;
import br.com.fiap.vinsight_api.lead.DadosListagemLead;
import br.com.fiap.vinsight_api.lead.LeadService;
import br.com.fiap.vinsight_api.lead.PrioridadeLead;
import br.com.fiap.vinsight_api.lead.StatusLead;
import br.com.fiap.vinsight_api.shared.DadosPagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/leads")
@Tag(name = "Leads", description = "Leads de retenção gerados pelo modelo de IA (VINSight Core)")
@SecurityRequirement(name = "bearer-key")
public class LeadController {

    @Autowired
    private LeadService service;

    @PostMapping
    @Operation(summary = "Registra um novo lead (gerado pelo modelo de ML)")
    public ResponseEntity<DadosDetalheLead> cadastrar(
            @RequestBody @Valid DadosCadastroLead dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalheLead lead = service.cadastrar(dados);
        var uri = uriBuilder.path("/api/v1/leads/{id}")
                .buildAndExpand(lead.id()).toUri();
        return ResponseEntity.created(uri).body(lead);
    }

    @GetMapping
    @Operation(summary = "Lista leads com filtros opcionais (prioridade, status, clienteId)")
    public ResponseEntity<DadosPagina<DadosListagemLead>> listar(
            @RequestParam(required = false) PrioridadeLead prioridade,
            @RequestParam(required = false) StatusLead status,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(size = 20, sort = "score") Pageable paginacao) {
        return ResponseEntity.ok(new DadosPagina<>(service.listar(prioridade, status, clienteId, paginacao)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um lead por ID")
    public ResponseEntity<DadosDetalheLead> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza o status do lead (NOVO → EM_CONTATO → CONVERTIDO/PERDIDO)")
    public ResponseEntity<DadosDetalheLead> atualizarStatus(
            @PathVariable Long id,
            @RequestBody @Valid DadosAtualizacaoStatusLead dados) {
        return ResponseEntity.ok(service.atualizarStatus(id, dados));
    }

    @PatchMapping("/{id}/conversao")
    @Operation(summary = "Marca o lead como CONVERTIDO e registra a data de conversão")
    public ResponseEntity<DadosDetalheLead> marcarConvertido(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarConvertido(id));
    }
}
