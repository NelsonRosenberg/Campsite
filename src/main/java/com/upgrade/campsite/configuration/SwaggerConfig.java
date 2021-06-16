package com.upgrade.campsite.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://localhost:8080"))
                .info(new Info()
                        .title("Volcano Campsite Booking - REST API")
                        .version("v1.0"));
    }
}
