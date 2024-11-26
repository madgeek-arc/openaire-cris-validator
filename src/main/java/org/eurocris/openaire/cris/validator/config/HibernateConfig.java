package org.eurocris.openaire.cris.validator.config;

import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {

    private final Environment environment;

    public HibernateConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean("crisDataSource")
    public DataSource crisDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(environment.getRequiredProperty("cris.datasource.driverClassName"));
        dataSource.setJdbcUrl(environment.getRequiredProperty("cris.datasource.url"));
        dataSource.setUsername(environment.getRequiredProperty("cris.datasource.username"));
        dataSource.setPassword(environment.getRequiredProperty("cris.datasource.password"));
        return dataSource;
    }

    @Bean("crisEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean crisEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean sessionFactory = new LocalContainerEntityManagerFactoryBean();
        sessionFactory.setDataSource(crisDataSource());
        sessionFactory.setPackagesToScan("org.eurocris.openaire.cris.validator.model");
        sessionFactory.setPersistenceUnitName("crisEntityManager");
        sessionFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        sessionFactory.setJpaProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean("crisTransactionManager")
    public TransactionManager crisTransactionManager(@Qualifier("crisEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", environment.getProperty("cris.datasource.hibernate.dialect", ""));
        properties.put("hibernate.show_sql", environment.getProperty("cris.datasource.hibernate.show_sql", "false"));
        properties.put("hibernate.format_sql", environment.getProperty("cris.datasource.hibernate.format_sql", "false"));
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("cris.datasource.hibernate.ddl-auto", "validate"));
        return properties;
    }
}