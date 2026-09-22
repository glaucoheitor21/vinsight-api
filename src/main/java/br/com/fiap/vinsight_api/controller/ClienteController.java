package br.com.fiap.vinsight_api.controller;

import br.com.fiap.vinsight_api.cliente.ClienteService;
import br.com.fiap.vinsight_api.cliente.DadosAtualizacaoCliente;
import br.com.fiap.vinsight_api.cliente.DadosCadastroCliente;
import br.com.fiap.vinsight_api.cliente.DadosDetalheCliente;
import br.com.fiap.vinsight_api.cliente.DadosListagemCliente;
import br.com.fiap.vinsight_api.veiculo.DadosListagemVeiculo;
import br.com.fiap.vinsight_api.veiculo.VeiculoService;
import br.com.fiap.vinsight_api.shared.DadosPagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Cadastro e gestão de clientes Ford")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @Autowired
    private VeiculoService veiculoService;

    @PostMapping
    @Operation(summary = "Cadastra um novo cliente")
    public ResponseEntity<DadosDetalheCliente> cadastrar(
            @RequestBody @Valid DadosCadastroCliente dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalheCliente cliente = service.cadastrar(dados);
        var uri = uriBuilder.path("/api/v1/clientes/{id}")
                .buildAndExpand(cliente.id()).toUri();
        return ResponseEntity.created(uri).body(cliente);
    }

    @GetMapping
    @Operation(summary = "Lista clientes ativos paginados")
    public ResponseEntity<DadosPagina<DadosListagemCliente>> listar(
            @PageableDefault(size = 20, sort = "id") Pageable paginacao) {
        return ResponseEntity.ok(new DadosPagina<>(service.listar(paginacao)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um cliente por ID")
    public ResponseEntity<DadosDetalheCliente> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um cliente")
    public ResponseEntity<DadosDetalheCliente> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid DadosAtualizacaoCliente dados) {
        return ResponseEntity.ok(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa um cliente (soft delete)")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/veiculos")
    @Operation(summary = "Lista os veículos de um cliente")
    public ResponseEntity<List<DadosListagemVeiculo>> listarVeiculos(@PathVariable Long id) {
        return ResponseEntity.ok(veiculoService.listarPorCliente(id));
    }
}
