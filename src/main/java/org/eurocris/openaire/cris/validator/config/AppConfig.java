package org.eurocris.openaire.cris.validator.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(value = "org.eurocris.openaire.cris.validator")
@PropertySource(value = "cris.properties")
public class AppConfig {
}
