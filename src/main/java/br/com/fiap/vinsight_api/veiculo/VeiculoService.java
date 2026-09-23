package br.com.fiap.vinsight_api.veiculo;

import br.com.fiap.vinsight_api.cliente.Cliente;
import br.com.fiap.vinsight_api.cliente.ClienteRepository;
import br.com.fiap.vinsight_api.concessionaria.Concessionaria;
import br.com.fiap.vinsight_api.concessionaria.ConcessionariaRepository;
import br.com.fiap.vinsight_api.infra.exception.EntidadeNaoEncontradaException;
import br.com.fiap.vinsight_api.infra.exception.RegraNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ConcessionariaRepository concessionariaRepository;

    @Transactional
    public DadosDetalheVeiculo cadastrar(DadosCadastroVeiculo dados) {
        if (repository.existsByVin(dados.vin())) {
            throw new RegraNegocioException("VIN já cadastrado.");
        }
        if (repository.existsByPlaca(dados.placa())) {
            throw new RegraNegocioException("Placa já cadastrada.");
        }

        Cliente cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Cliente com id " + dados.clienteId() + " não encontrado."));

        Concessionaria concessionaria = null;
        if (dados.concessionariaCompraId() != null) {
            concessionaria = concessionariaRepository.findById(dados.concessionariaCompraId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException(
                            "Concessionária com id " + dados.concessionariaCompraId() + " não encontrada."));
        }

        Veiculo veiculo = new Veiculo(dados, cliente, concessionaria);
        repository.save(veiculo);
        return new DadosDetalheVeiculo(veiculo);
    }

    @Transactional
    public Page<DadosListagemVeiculo> listar(Pageable paginacao) {
        return repository.findAllByStatusNot(StatusVeiculo.INATIVO, paginacao)
                .map(DadosListagemVeiculo::new);
    }

    @Transactional
    public DadosDetalheVeiculo detalhar(Long id) {
        return new DadosDetalheVeiculo(buscar(id));
    }

    @Transactional
    public DadosDetalheVeiculo detalharPorVin(String vin) {
        Veiculo veiculo = repository.findByVin(vin)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Veículo com VIN " + vin + " não encontrado."));
        return new DadosDetalheVeiculo(veiculo);
    }

    @Transactional
    public DadosDetalheVeiculo atualizar(Long id, DadosAtualizacaoVeiculo dados) {
        Veiculo veiculo = buscar(id);
        Cliente novoCliente = null;
        if (dados.clienteId() != null) {
            novoCliente = clienteRepository.findById(dados.clienteId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException(
                            "Cliente com id " + dados.clienteId() + " não encontrado."));
        }
        veiculo.atualizar(dados, novoCliente);
        return new DadosDetalheVeiculo(veiculo);
    }

    @Transactional
    public void inativar(Long id) {
        Veiculo veiculo = buscar(id);
        veiculo.inativar();
    }

    private Veiculo buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Veículo com id " + id + " não encontrado."));
    }
}
