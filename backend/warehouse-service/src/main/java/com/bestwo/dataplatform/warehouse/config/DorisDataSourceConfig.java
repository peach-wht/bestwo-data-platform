package com.bestwo.dataplatform.warehouse.config;

import javax.sql.DataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@EnableConfigurationProperties(DorisProperties.class)
public class DorisDataSourceConfig {

    @Bean
    public DataSource dorisDataSource(DorisProperties dorisProperties) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(dorisProperties.getJdbcUrl());
        dataSource.setUsername(dorisProperties.getUsername());
        dataSource.setPassword(dorisProperties.getPassword());
        return dataSource;
    }

    @Bean
    public JdbcTemplate dorisJdbcTemplate(DataSource dorisDataSource) {
        return new JdbcTemplate(dorisDataSource);
    }
}
