package br.com.fiap.vinsight_api.cliente;

import br.com.fiap.vinsight_api.infra.security.AcessoForaDoEscopoException;
import br.com.fiap.vinsight_api.infra.security.ContextoSeguranca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Verificacao da carteira por relacionamento (regra em ClienteRepository.CARTEIRA).
 * Usada por clientes (US-33) e por veiculos (US-34): um veiculo e visivel quando o DONO esta
 * na carteira da unidade do usuario.
 */
@Component
public class CarteiraClientes {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private ContextoSeguranca contexto;

    /** 403 outra-concessionaria se o cliente esta fora da carteira. ANALISTA_FORD e ADMIN passam. */
    public void verificarAcesso(Long clienteId) {
        Long escopo = contexto.concessionariaEscopo();
        if (escopo != null && !repository.pertenceACarteira(clienteId, escopo)) {
            throw new AcessoForaDoEscopoException();
        }
    }
}
