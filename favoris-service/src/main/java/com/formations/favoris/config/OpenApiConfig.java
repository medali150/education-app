package com.formations.favoris.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI favorisOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de gestion des favoris")
                        .description("API pour gérer les favoris des utilisateurs")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Équipe de développement")
                                .email("contact@formations.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}