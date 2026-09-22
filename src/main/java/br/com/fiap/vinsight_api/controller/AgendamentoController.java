package br.com.fiap.vinsight_api.controller;

import br.com.fiap.vinsight_api.agendamento.AgendamentoService;
import br.com.fiap.vinsight_api.agendamento.DadosAtualizacaoStatusAgendamento;
import br.com.fiap.vinsight_api.agendamento.DadosCadastroAgendamento;
import br.com.fiap.vinsight_api.agendamento.DadosDetalheAgendamento;
import br.com.fiap.vinsight_api.agendamento.DadosListagemAgendamento;
import br.com.fiap.vinsight_api.agendamento.StatusAgendamento;
import br.com.fiap.vinsight_api.shared.DadosPagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/agendamentos")
@Tag(name = "Agendamentos", description = "Agendamentos de serviços Ford (revisão, reparo, garantia, recall)")
public class AgendamentoController {

    @Autowired
    private AgendamentoService service;

    @PostMapping
    @Operation(summary = "Cria um novo agendamento")
    public ResponseEntity<DadosDetalheAgendamento> cadastrar(
            @RequestBody @Valid DadosCadastroAgendamento dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalheAgendamento agendamento = service.cadastrar(dados);
        var uri = uriBuilder.path("/api/v1/agendamentos/{id}")
                .buildAndExpand(agendamento.id()).toUri();
        return ResponseEntity.created(uri).body(agendamento);
    }

    @GetMapping
    @Operation(summary = "Lista agendamentos com filtros opcionais")
    public ResponseEntity<DadosPagina<DadosListagemAgendamento>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Long concessionariaId,
            @RequestParam(required = false) StatusAgendamento status,
            @PageableDefault(size = 20, sort = "dataHora") Pageable paginacao) {
        return ResponseEntity.ok(new DadosPagina<>(service.listar(dataInicio, dataFim, concessionariaId, status, paginacao)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um agendamento por ID")
    public ResponseEntity<DadosDetalheAgendamento> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza o status de um agendamento (atualização parcial)")
    public ResponseEntity<DadosDetalheAgendamento> atualizarStatus(
            @PathVariable Long id,
            @RequestBody @Valid DadosAtualizacaoStatusAgendamento dados) {
        return ResponseEntity.ok(service.atualizarStatus(id, dados));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela um agendamento (status=CANCELADO)")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        service.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
