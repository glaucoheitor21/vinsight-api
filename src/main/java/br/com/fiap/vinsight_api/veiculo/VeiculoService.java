package br.com.fiap.vinsight_api.veiculo;

import br.com.fiap.vinsight_api.agendamento.AgendamentoService;
import br.com.fiap.vinsight_api.agendamento.DadosListagemAgendamento;
import br.com.fiap.vinsight_api.cliente.CarteiraClientes;
import br.com.fiap.vinsight_api.cliente.Cliente;
import br.com.fiap.vinsight_api.cliente.ClienteRepository;
import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
import br.com.fiap.vinsight_api.concessionaria.ConcessionariaRepository;
import br.com.fiap.vinsight_api.infra.exception.EntidadeNaoEncontradaException;
import br.com.fiap.vinsight_api.infra.exception.RegraNegocioException;
import br.com.fiap.vinsight_api.infra.security.ContextoSeguranca;
import br.com.fiap.vinsight_api.infra.security.MascaradorDados;
import br.com.fiap.vinsight_api.ordemservico.OrdemServicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Vehicle Service (US-34). O veiculo e identificado pelo VIN, e o escopo de acesso e o do DONO:
 * o veiculo e visivel quando o cliente esta na carteira da unidade do usuario (CarteiraClientes).
 */
@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ConcessionariaRepository concessionariaRepository;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private CarteiraClientes carteira;

    @Autowired
    private ContextoSeguranca contexto;

    @Autowired
    private MascaradorDados mascarador;

    @Autowired
    private Clock clock;

    @Transactional
    public DadosDetalheVeiculo cadastrar(DadosCadastroVeiculo dados) {
        // So da para cadastrar veiculo de um cliente que o usuario enxerga
        Cliente cliente = buscarCliente(dados.clienteId());

        if (repository.existsByVin(dados.vin())) {
            throw new RegraNegocioException("VIN já cadastrado.");
        }
        if (repository.existsByPlaca(dados.placa())) {
            throw new RegraNegocioException("Placa já cadastrada.");
        }
        Concessionaria concessionaria = null;
        if (dados.concessionariaCompraId() != null) {
            concessionaria = concessionariaRepository.findById(dados.concessionariaCompraId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException(
                            "Concessionária com id " + dados.concessionariaCompraId() + " não encontrada."));
        }
        Veiculo veiculo = new Veiculo(dados, cliente, concessionaria);
        repository.save(veiculo);
        return new DadosDetalheVeiculo(veiculo, mascarador);
    }

    /** GET /vehicles?placa=. Sem placa, lista os veiculos da carteira (exceto INATIVOs). */
    @Transactional
    public Page<DadosListagemVeiculo> listar(String placa, Pageable paginacao) {
        // Aceita "ABC-1D23" ou "abc1d23": placa e gravada sem hifen, em maiusculas
        String placaNormalizada = placa == null || placa.isBlank()
                ? null
                : placa.replace("-", "").trim().toUpperCase();
        return repository.buscar(placaNormalizada, StatusVeiculo.INATIVO, contexto.concessionariaEscopo(), paginacao)
                .map(DadosListagemVeiculo::new);
    }

    /** Passaporte: dados do carro, garantia, revisao, aderencia a rede, telemetria e historico. */
    @Transactional
    public DadosPassaporteVeiculo passaporte(String vin) {
        Veiculo veiculo = buscar(vin);
        return new DadosPassaporteVeiculo(
                veiculo,
                ordemServicoRepository.historicoDoVeiculo(veiculo.getId()),
                LocalDate.now(clock));
    }

    @Transactional
    public DadosDetalheVeiculo atualizar(String vin, DadosAtualizacaoVeiculo dados) {
        Veiculo veiculo = buscar(vin);
        // Transferencia de dono: o novo dono tambem precisa estar na carteira
        Cliente novoCliente = dados.clienteId() == null ? null : buscarCliente(dados.clienteId());
        veiculo.atualizar(dados, novoCliente);
        return new DadosDetalheVeiculo(veiculo, mascarador);
    }

    @Transactional
    public void inativar(String vin) {
        buscar(vin).inativar();
    }

    @Transactional
    public Page<DadosListagemAgendamento> listarAgendamentos(String vin, Pageable paginacao) {
        return agendamentoService.listarPorVeiculo(buscar(vin).getId(), paginacao);
    }

    // Todo acesso por VIN passa por aqui: 404 se nao existe, 403 se o dono esta fora da carteira
    private Veiculo buscar(String vin) {
        Veiculo veiculo = repository.findByVin(vin)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Veículo com VIN " + vin + " não encontrado."));
        carteira.verificarAcesso(veiculo.getCliente().getId());
        return veiculo;
    }

    private Cliente buscarCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Cliente com id " + clienteId + " não encontrado."));
        carteira.verificarAcesso(clienteId);
        return cliente;
    }
}
