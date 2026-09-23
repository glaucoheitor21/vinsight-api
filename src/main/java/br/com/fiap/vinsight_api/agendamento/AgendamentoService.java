package br.com.fiap.vinsight_api.agendamento;

import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
import br.com.fiap.vinsight_api.concessionaria.ConcessionariaRepository;
import br.com.fiap.vinsight_api.infra.exception.EntidadeNaoEncontradaException;
import br.com.fiap.vinsight_api.infra.security.ContextoSeguranca;
import br.com.fiap.vinsight_api.veiculo.Veiculo;
import br.com.fiap.vinsight_api.veiculo.VeiculoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository repository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ConcessionariaRepository concessionariaRepository;

    @Autowired
    private ContextoSeguranca contexto;

    @Transactional
    public DadosDetalheAgendamento cadastrar(DadosCadastroAgendamento dados) {
        // Consultor/gerente so agendam na propria unidade. O veiculo pode ser de qualquer
        // unidade: atender carro vendido por outra concessionaria e ganho de Service Share
        contexto.verificarAcesso(dados.concessionariaId());

        Veiculo veiculo = veiculoRepository.findById(dados.veiculoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Veículo com id " + dados.veiculoId() + " não encontrado."));

        Concessionaria concessionaria = concessionariaRepository.findById(dados.concessionariaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Concessionária com id " + dados.concessionariaId() + " não encontrada."));

        Agendamento agendamento = new Agendamento(dados, veiculo, concessionaria);
        repository.save(agendamento);
        return new DadosDetalheAgendamento(agendamento);
    }

    @Transactional
    public Page<DadosListagemAgendamento> listar(LocalDateTime dataInicio, LocalDateTime dataFim,
                                                  Long concessionariaId, StatusAgendamento status,
                                                  Pageable paginacao) {
        // O filtro concessionariaId so vale para analista/admin; consultor/gerente ficam na propria unidade
        Long concessionariaEfetiva = contexto.restringirConcessionaria(concessionariaId);
        return repository.buscarComFiltros(dataInicio, dataFim, concessionariaEfetiva, status, paginacao)
                .map(DadosListagemAgendamento::new);
    }

    @Transactional
    public DadosDetalheAgendamento detalhar(Long id) {
        return new DadosDetalheAgendamento(buscar(id));
    }

    @Transactional
    public DadosDetalheAgendamento atualizarStatus(Long id, DadosAtualizacaoStatusAgendamento dados) {
        Agendamento agendamento = buscar(id);
        agendamento.atualizarStatus(dados.status());
        return new DadosDetalheAgendamento(agendamento);
    }

    @Transactional
    public void cancelar(Long id) {
        Agendamento agendamento = buscar(id);
        agendamento.cancelar();
    }

    @Transactional
    public Page<DadosListagemAgendamento> listarPorVeiculo(Long veiculoId, Pageable paginacao) {
        if (!veiculoRepository.existsById(veiculoId)) {
            throw new EntidadeNaoEncontradaException(
                    "Veículo com id " + veiculoId + " não encontrado.");
        }
        // Historico do veiculo limitado aos agendamentos da unidade do usuario
        return repository.buscarPorVeiculo(veiculoId, contexto.concessionariaEscopo(), paginacao)
                .map(DadosListagemAgendamento::new);
    }

    @Transactional
    public Page<DadosListagemAgendamento> listarPorConcessionaria(Long concessionariaId, Pageable paginacao) {
        contexto.verificarAcesso(concessionariaId);
        if (!concessionariaRepository.existsById(concessionariaId)) {
            throw new EntidadeNaoEncontradaException(
                    "Concessionária com id " + concessionariaId + " não encontrada.");
        }
        return repository.findAllByConcessionariaId(concessionariaId, paginacao)
                .map(DadosListagemAgendamento::new);
    }

    // Todo acesso individual passa por aqui: 404 se nao existe, 403 se e de outra unidade
    private Agendamento buscar(Long id) {
        Agendamento agendamento = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Agendamento com id " + id + " não encontrado."));
        contexto.verificarAcesso(agendamento.getConcessionaria());
        return agendamento;
    }
}
