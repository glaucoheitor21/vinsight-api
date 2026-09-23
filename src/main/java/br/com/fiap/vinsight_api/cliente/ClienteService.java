package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.infra.exception.CampoInvalidoException;
import br.com.fiap.vinsight_api.infra.exception.EntidadeNaoEncontradaException;
import br.com.fiap.vinsight_api.infra.exception.RegraNegocioException;
import br.com.fiap.vinsight_api.infra.security.ContextoSeguranca;
import br.com.fiap.vinsight_api.infra.security.MascaradorDados;
import br.com.fiap.vinsight_api.ordemservico.OrdemServicoRepository;
import br.com.fiap.vinsight_api.shared.DadosContato;
import br.com.fiap.vinsight_api.veiculo.DadosListagemVeiculo;
import br.com.fiap.vinsight_api.veiculo.VeiculoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private ContextoSeguranca contexto;

    @Autowired
    private MascaradorDados mascarador;

    @Autowired
    private CarteiraClientes carteira;

    @Transactional
    public DadosDetalheCliente cadastrar(DadosCadastroCliente dados) {
        rejeitarDadosMascarados(dados.contato());
        if (repository.existsByDadosPessoais_Cpf(dados.dadosPessoais().getCpf())) {
            throw new RegraNegocioException("CPF já cadastrado.");
        }
        // Quem cadastra ja passa a ter o cliente na carteira (null para ADMIN)
        Cliente cliente = new Cliente(dados, contexto.usuarioLogado().getConcessionaria());
        repository.save(cliente);
        return new DadosDetalheCliente(cliente, mascarador);
    }

    /**
     * GET /customers?q=. Com letras, busca por nome parcial; so com digitos (e pontuacao), busca
     * por CPF exato ou trecho do telefone. Sem q, lista a carteira inteira.
     */
    @Transactional
    public Page<DadosResumoCliente> buscar(String q, Pageable paginacao) {
        String nome = null;
        String digitos = null;
        if (q != null && !q.isBlank()) {
            if (q.chars().anyMatch(Character::isLetter)) {
                nome = q.trim();
            } else {
                digitos = q.replaceAll("\\D", "");
            }
        }
        return repository.buscar(nome, digitos, contexto.concessionariaEscopo(), paginacao)
                .map(cliente -> new DadosResumoCliente(cliente, mascarador));
    }

    @Transactional
    public DadosDetalheCliente detalhar(Long id) {
        return new DadosDetalheCliente(buscar(id), mascarador);
    }

    /** Visao 360: cadastro, veiculos, consentimento e resumo das ordens, numa chamada so. */
    @Transactional
    public DadosVisao360Cliente visao360(Long id) {
        Cliente cliente = buscar(id);
        return new DadosVisao360Cliente(
                cliente,
                veiculoRepository.findAllByClienteId(id),
                ordemServicoRepository.resumirPorCliente(id),
                mascarador);
    }

    @Transactional
    public List<DadosListagemVeiculo> listarVeiculos(Long id) {
        buscar(id);
        return veiculoRepository.findAllByClienteId(id).stream()
                .map(DadosListagemVeiculo::new)
                .toList();
    }

    @Transactional
    public DadosDetalheCliente atualizar(Long id, DadosAtualizacaoCliente dados) {
        rejeitarDadosMascarados(dados.contato());
        Cliente cliente = buscar(id);
        cliente.atualizar(dados);
        return new DadosDetalheCliente(cliente, mascarador);
    }

    @Transactional
    public void inativar(Long id) {
        Cliente cliente = buscar(id);
        cliente.inativar();
    }

    // Todo acesso individual passa por aqui: 404 se nao existe, 403 se esta fora da carteira
    private Cliente buscar(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Cliente com id " + id + " não encontrado."));
        carteira.verificarAcesso(id);
        return cliente;
    }

    // O consultor recebe e-mail e telefone mascarados. Se o app devolver esse valor num PUT,
    // ele passaria no @Email e sobrescreveria o dado real. O CPF ja e barrado pelo @Pattern.
    private void rejeitarDadosMascarados(DadosContato contato) {
        if (contato == null) return;
        String mensagem = "valor mascarado não pode ser gravado; envie o valor completo ou omita a seção contato";
        if (contato.getEmail() != null && contato.getEmail().contains("*")) {
            throw new CampoInvalidoException("contato.email", mensagem);
        }
        if (contato.getTelefone() != null && contato.getTelefone().contains("*")) {
            throw new CampoInvalidoException("contato.telefone", mensagem);
        }
    }
}
