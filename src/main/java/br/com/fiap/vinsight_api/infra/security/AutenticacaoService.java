package br.com.fiap.vinsight_api.infra.security;

import br.com.fiap.vinsight_api.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Usado pelo AuthenticationManager no login. O "username" do Spring Security e o e-mail.
 */
@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    private UsuarioRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // O Spring converte UsernameNotFoundException em BadCredentialsException,
        // entao a resposta nao revela se o e-mail existe
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
