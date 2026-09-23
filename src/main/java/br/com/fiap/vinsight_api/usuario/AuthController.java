package br.com.fiap.vinsight_api.usuario;

import br.com.fiap.vinsight_api.infra.security.DadosTokenJWT;
import br.com.fiap.vinsight_api.infra.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Login e renovação de token JWT (endpoints públicos)")
public class AuthController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @PostMapping("/login")
    @Operation(summary = "Autentica por e-mail e senha e devolve access token (15 min) e refresh token (8 h)")
    public ResponseEntity<DadosTokenJWT> login(@RequestBody @Valid DadosLogin dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        Authentication authentication = manager.authenticate(token);
        return ResponseEntity.ok(gerarTokens((Usuario) authentication.getPrincipal()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Troca um refresh token válido por um novo par de tokens")
    public ResponseEntity<DadosTokenJWT> refresh(@RequestBody @Valid DadosRefresh dados) {
        Long usuarioId = tokenService.validarRefreshToken(dados.refreshToken());
        // Usuario apagado ou inativado depois do login nao renova a sessao
        Usuario usuario = repository.findById(usuarioId)
                .filter(Usuario::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Usuário inexistente ou inativo"));
        return ResponseEntity.ok(gerarTokens(usuario));
    }

    private DadosTokenJWT gerarTokens(Usuario usuario) {
        return new DadosTokenJWT(
                tokenService.gerarAccessToken(usuario),
                tokenService.gerarRefreshToken(usuario),
                tokenService.getSegundosExpiracaoAccess(),
                new DadosUsuarioLogado(usuario));
    }
}
