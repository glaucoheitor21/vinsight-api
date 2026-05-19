package br.com.fiap.vinsight_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
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
public class SwaggerConfig {
}
