package br.com.fiap.vinsight_api.lead;

import br.com.fiap.vinsight_api.cliente.Cliente;
import br.com.fiap.vinsight_api.cliente.ClienteRepository;
import br.com.fiap.vinsight_api.infra.exception.EntidadeNaoEncontradaException;
import br.com.fiap.vinsight_api.infra.exception.RegraNegocioException;
import br.com.fiap.vinsight_api.veiculo.Veiculo;
import br.com.fiap.vinsight_api.veiculo.VeiculoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LeadService {

    @Autowired
    private LeadRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Transactional
    public DadosDetalheLead cadastrar(DadosCadastroLead dados) {
        Cliente cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Cliente com id " + dados.clienteId() + " não encontrado."));

        Veiculo veiculo = veiculoRepository.findById(dados.veiculoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Veículo com id " + dados.veiculoId() + " não encontrado."));

        if (!veiculo.getCliente().getId().equals(cliente.getId())) {
            throw new RegraNegocioException(
                    "Veículo não pertence ao cliente informado.");
        }

        Lead lead = new Lead(dados, cliente, veiculo);
        repository.save(lead);
        return new DadosDetalheLead(lead);
    }

    @Transactional
    public Page<DadosListagemLead> listar(PrioridadeLead prioridade, StatusLead status,
                                           Long clienteId, Pageable paginacao) {
        return repository.buscarComFiltros(prioridade, status, clienteId, paginacao)
                .map(DadosListagemLead::new);
    }

    @Transactional
    public DadosDetalheLead detalhar(Long id) {
        return new DadosDetalheLead(buscar(id));
    }

    @Transactional
    public DadosDetalheLead atualizarStatus(Long id, DadosAtualizacaoStatusLead dados) {
        Lead lead = buscar(id);
        lead.atualizarStatus(dados.status());
        return new DadosDetalheLead(lead);
    }

    @Transactional
    public DadosDetalheLead marcarConvertido(Long id) {
        Lead lead = buscar(id);
        if (lead.getStatus() == StatusLead.CONVERTIDO) {
            throw new RegraNegocioException("Lead já está marcado como convertido.");
        }
        if (lead.getStatus() == StatusLead.PERDIDO) {
            throw new RegraNegocioException("Lead PERDIDO não pode ser convertido.");
        }
        lead.marcarConvertido();
        return new DadosDetalheLead(lead);
    }

    private Lead buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Lead com id " + id + " não encontrado."));
    }
}
