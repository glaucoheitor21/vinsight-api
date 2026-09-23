package br.com.fiap.vinsight_api.lead;

import br.com.fiap.vinsight_api.cliente.Cliente;
import br.com.fiap.vinsight_api.cliente.ClienteRepository;
import br.com.fiap.vinsight_api.infra.exception.EntidadeNaoEncontradaException;
import br.com.fiap.vinsight_api.infra.exception.RegraNegocioException;
import br.com.fiap.vinsight_api.infra.security.ContextoSeguranca;
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

    @Autowired
    private ContextoSeguranca contexto;

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
        // Consultor/gerente veem so a propria unidade; analista/admin, a rede inteira (null)
        return repository.buscarComFiltros(prioridade, status, clienteId,
                        contexto.concessionariaEscopo(), paginacao)
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

    // Todo acesso individual passa por aqui: 404 se nao existe, 403 se e de outra unidade
    private Lead buscar(Long id) {
        Lead lead = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Lead com id " + id + " não encontrado."));
        contexto.verificarAcesso(lead.getConcessionaria());
        return lead;
    }
}
