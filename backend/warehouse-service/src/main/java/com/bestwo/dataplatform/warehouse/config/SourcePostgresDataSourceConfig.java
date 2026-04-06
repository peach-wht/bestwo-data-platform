package com.bestwo.dataplatform.warehouse.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@EnableConfigurationProperties(SourcePostgresProperties.class)
@MapperScan(
    basePackages = "com.bestwo.dataplatform.warehouse.source.mapper",
    sqlSessionFactoryRef = "sourceSqlSessionFactory"
)
public class SourcePostgresDataSourceConfig {

    @Bean(name = "sourceDataSource")
    public DataSource sourceDataSource(SourcePostgresProperties sourcePostgresProperties) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(sourcePostgresProperties.getJdbcUrl());
        dataSource.setUsername(sourcePostgresProperties.getUsername());
        dataSource.setPassword(sourcePostgresProperties.getPassword());
        return dataSource;
    }

    @Bean(name = "sourceTransactionManager")
    public DataSourceTransactionManager sourceTransactionManager(
        @Qualifier("sourceDataSource") DataSource sourceDataSource
    ) {
        return new DataSourceTransactionManager(sourceDataSource);
    }

    @Bean(name = "sourceSqlSessionFactory")
    public SqlSessionFactory sourceSqlSessionFactory(
        @Qualifier("sourceDataSource") DataSource sourceDataSource
    ) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(sourceDataSource);

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);
        return factoryBean.getObject();
    }
}
