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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@Primary
@EnableConfigurationProperties(DorisProperties.class)
@MapperScan(
    basePackages = "com.bestwo.dataplatform.warehouse.mapper",
    sqlSessionFactoryRef = "dorisSqlSessionFactory"
)
public class DorisDataSourceConfig {

    @Primary
    @Bean(name = "dorisDataSource")
    public DataSource dorisDataSource(DorisProperties dorisProperties) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(dorisProperties.getJdbcUrl());
        dataSource.setUsername(dorisProperties.getUsername());
        dataSource.setPassword(dorisProperties.getPassword());
        return dataSource;
    }

    @Primary
    @Bean(name = "dorisTransactionManager")
    public DataSourceTransactionManager dorisTransactionManager(
        @Qualifier("dorisDataSource") DataSource dorisDataSource
    ) {
        return new DataSourceTransactionManager(dorisDataSource);
    }

    @Primary
    @Bean(name = "dorisSqlSessionFactory")
    public SqlSessionFactory dorisSqlSessionFactory(
        @Qualifier("dorisDataSource") DataSource dorisDataSource
    ) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dorisDataSource);

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath:/mapper/*.xml")
        );
        return factoryBean.getObject();
    }
}
