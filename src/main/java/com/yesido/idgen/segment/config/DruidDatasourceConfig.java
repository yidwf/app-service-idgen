package com.yesido.idgen.segment.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.github.pagehelper.PageInterceptor;

/**
 * 数据源配置类
 * 
 * @author yesido
 * @date 2020年12月24日 上午10:45:25
 */
@Configuration
@PropertySource(value = {"classpath:leaf-${spring.profiles.active}.properties"},
        ignoreResourceNotFound = false)
public class DruidDatasourceConfig {

    @Bean(name = "datasource_leaf", initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "datasource.leaf")
    public DruidXADataSource druidXADataSource() {
        DruidXADataSource datasource = (DruidXADataSource) DataSourceBuilder.create().type(DruidXADataSource.class).build();
        return datasource;
    }

    @Bean(name = "sqlSessionFactory_leaf")
    public SqlSessionFactory sqlSessionFactory_backend(@Qualifier("datasource_leaf") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        pageInterceptor.setProperties(properties);
        bean.setPlugins(new Interceptor[]{pageInterceptor});
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        bean.setMapperLocations(resolver.getResources("classpath*:com/yesido/idgen/segment/mapper/**/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(
            @Qualifier("datasource_leaf") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
