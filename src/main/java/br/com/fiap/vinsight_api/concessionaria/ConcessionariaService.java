package br.com.fiap.vinsight_api.concessionaria;

import br.com.fiap.vinsight_api.infra.exception.EntidadeNaoEncontradaException;
import br.com.fiap.vinsight_api.infra.exception.RegraNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ConcessionariaService {

    @Autowired
    private ConcessionariaRepository repository;

    @Transactional
    public DadosDetalheConcessionaria cadastrar(DadosCadastroConcessionaria dados) {
        if (repository.existsByCnpj(dados.cnpj())) {
            throw new RegraNegocioException("CNPJ já cadastrado.");
        }
        Concessionaria concessionaria = new Concessionaria(dados);
        repository.save(concessionaria);
        return new DadosDetalheConcessionaria(concessionaria);
    }

    public Page<DadosListagemConcessionaria> listar(Pageable paginacao) {
        return repository.findAllByAtivoTrue(paginacao)
                .map(DadosListagemConcessionaria::new);
    }

    public DadosDetalheConcessionaria detalhar(Long id) {
        return new DadosDetalheConcessionaria(buscar(id));
    }

    @Transactional
    public DadosDetalheConcessionaria atualizar(Long id, DadosAtualizacaoConcessionaria dados) {
        Concessionaria concessionaria = buscar(id);
        concessionaria.atualizar(dados);
        return new DadosDetalheConcessionaria(concessionaria);
    }

    @Transactional
    public void inativar(Long id) {
        Concessionaria concessionaria = buscar(id);
        concessionaria.inativar();
    }

    private Concessionaria buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Concessionária com id " + id + " não encontrada."));
    }
}
