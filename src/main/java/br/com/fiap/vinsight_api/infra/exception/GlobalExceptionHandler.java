package br.com.fiap.vinsight_api.infra.exception;

import br.com.fiap.vinsight_api.infra.security.TokenInvalidoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErroResponse(LocalDateTime timestamp, int status, String erro, String mensagem, Object detalhes) {
    }

    public record CampoInvalido(String campo, String mensagem) {
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handle404(EntidadeNaoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErroResponse(LocalDateTime.now(), 404, "Not Found", e.getMessage(), null));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> handle409(RegraNegocioException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErroResponse(LocalDateTime.now(), 409, "Conflict", e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handle400Validation(MethodArgumentNotValidException e) {
        List<CampoInvalido> erros = e.getFieldErrors().stream()
                .map(fe -> new CampoInvalido(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(
                new ErroResponse(LocalDateTime.now(), 400, "Bad Request", "Dados inválidos", erros));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> handle409Integrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErroResponse(LocalDateTime.now(), 409, "Conflict",
                        "Violação de integridade — registro duplicado ou referência inválida.", null));
    }

    // Refresh token expirado/invalido: a mensagem diz qual dos dois, sem expor dados do usuario
    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErroResponse> handle401Token(TokenInvalidoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErroResponse(LocalDateTime.now(), 401, "Unauthorized", e.getMessage(), null));
    }

    // Login com senha errada, e-mail inexistente ou usuario inativo: mensagem unica e generica,
    // para nao revelar se o e-mail esta cadastrado
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> handle401(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErroResponse(LocalDateTime.now(), 401, "Unauthorized", "Credenciais inválidas.", null));
    }

    // @PreAuthorize negado (US-30). Sem isto, cairia no handle500
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handle403(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErroResponse(LocalDateTime.now(), 403, "Forbidden",
                        "Seu perfil não tem permissão para este recurso.", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handle500(Exception e) {
        return ResponseEntity.internalServerError().body(
                new ErroResponse(LocalDateTime.now(), 500, "Internal Server Error",
                        "Erro inesperado.", null));
    }
}
