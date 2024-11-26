package org.eurocris.openaire.cris.validator.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(value = "org.eurocris.openaire.cris.validator")
@Import(HibernateConfig.class)
public class AppConfig {
}
