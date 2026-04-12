package com.sporty.betting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sporty Betting API")
                        .description("Sports Betting Settlement Trigger Service — "
                                + "Event outcome handling and bet settlement via Kafka & RocketMQ")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sporty Engineering")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")));
    }
}
