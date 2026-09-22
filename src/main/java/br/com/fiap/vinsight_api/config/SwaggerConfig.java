package br.com.fiap.vinsight_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "VINSight Ford API",
                version = "1.0",
                description = "Plataforma Inteligente de Pós-Venda Ford — Backend SOA. Challenge FIAP 2026.",
                contact = @Contact(name = "Grupo 02 — Glauco & Pedro", email = "grupo02@fiap.com.br")
        )
)
// Botao "Authorize" do Swagger UI; os controllers protegidos referenciam "bearer-key"
@SecurityScheme(
        name = "bearer-key",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
