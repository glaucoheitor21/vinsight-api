package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.infra.exception.EntidadeNaoEncontradaException;
import br.com.fiap.vinsight_api.infra.exception.RegraNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    @Transactional
    public DadosDetalheCliente cadastrar(DadosCadastroCliente dados) {
        if (repository.existsByDadosPessoais_Cpf(dados.dadosPessoais().getCpf())) {
            throw new RegraNegocioException("CPF já cadastrado.");
        }
        Cliente cliente = new Cliente(dados);
        repository.save(cliente);
        return new DadosDetalheCliente(cliente);
    }

    public Page<DadosListagemCliente> listar(Pageable paginacao) {
        return repository.findAllByAtivoTrue(paginacao)
                .map(DadosListagemCliente::new);
    }

    public DadosDetalheCliente detalhar(Long id) {
        return new DadosDetalheCliente(buscar(id));
    }

    @Transactional
    public DadosDetalheCliente atualizar(Long id, DadosAtualizacaoCliente dados) {
        Cliente cliente = buscar(id);
        cliente.atualizar(dados);
        return new DadosDetalheCliente(cliente);
    }

    @Transactional
    public void inativar(Long id) {
        Cliente cliente = buscar(id);
        cliente.inativar();
    }

    private Cliente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Cliente com id " + id + " não encontrado."));
    }
}
