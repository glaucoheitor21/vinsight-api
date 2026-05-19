package br.com.fiap.vinsight_api.agendamento;

import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
import br.com.fiap.vinsight_api.concessionaria.ConcessionariaRepository;
import br.com.fiap.vinsight_api.infra.exception.EntidadeNaoEncontradaException;
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

    @Transactional
    public DadosDetalheAgendamento cadastrar(DadosCadastroAgendamento dados) {
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
        return repository.buscarComFiltros(dataInicio, dataFim, concessionariaId, status, paginacao)
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
        return repository.findAllByVeiculoId(veiculoId, paginacao)
                .map(DadosListagemAgendamento::new);
    }

    @Transactional
    public Page<DadosListagemAgendamento> listarPorConcessionaria(Long concessionariaId, Pageable paginacao) {
        if (!concessionariaRepository.existsById(concessionariaId)) {
            throw new EntidadeNaoEncontradaException(
                    "Concessionária com id " + concessionariaId + " não encontrada.");
        }
        return repository.findAllByConcessionariaId(concessionariaId, paginacao)
                .map(DadosListagemAgendamento::new);
    }

    private Agendamento buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Agendamento com id " + id + " não encontrado."));
    }
}
