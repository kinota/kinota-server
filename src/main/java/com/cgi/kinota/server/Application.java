package com.cgi.kinota.server;

import com.cgi.kinota.server.config.ServletContext;
import com.cgi.kinota.server.servlet.STAServlet;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.naming.NamingException;
import javax.servlet.Servlet;
import javax.sql.DataSource;

/**
 * Created by bmiles on 9/13/17.
 */
@SpringBootApplication
@ServletComponentScan
public class Application extends SpringBootServletInitializer {

    @Autowired
    private Environment env;

    @Bean
    public ServletContext servletContext() {
        return new ServletContext();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public TomcatEmbeddedServletContainerFactory tomcatFactory() {
        return new TomcatEmbeddedServletContainerFactory() {

            @Override
            protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(
                    Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatEmbeddedServletContainer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {
                ContextResource r = new ContextResource();
                r.setName(env.getProperty("sta.datasource.name", "jdbc/sensorThings"));
                r.setAuth(env.getProperty("sta.datasource.auth", "Container"));
                r.setType(env.getProperty("sta.datasource.type", "javax.sql.DataSource"));
                r.setProperty("factory",
                        env.getProperty("sta.datasource.factory","org.apache.tomcat.jdbc.pool.DataSourceFactory"));
                r.setProperty("driverClassName",
                        env.getProperty("sta.datasource.driverClassName", "org.postgresql.Driver"));
                r.setProperty("url",
                        env.getProperty("sta.datasource.url", "jdbc:postgresql://localhost:5432/sensorthings"));
                r.setProperty("username",
                        env.getProperty("sta.datasource.username", "sensorthings"));
                r.setProperty("password",
                        env.getProperty("sta.datasource.password", "ChangeMe!"));
                r.setProperty("maxTotal",
                        env.getProperty("sta.datasource.maxTotal", "20"));
                r.setProperty("maxIdle",
                        env.getProperty("sta.datasource.maxIdle", "10"));
                r.setProperty("maxWaitMillis",
                        env.getProperty("sta.datasource.maxWaitMillis", "-1"));
                r.setProperty("defaultAutoCommit",
                        env.getProperty("sta.datasource.defaultAutoCommit", "false"));

                context.getNamingResources().addResource(r);
            }
        };
    }

    @Bean(destroyMethod="")
    public DataSource jndiDataSource() throws IllegalArgumentException, NamingException {
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        String jndiName = "java:comp/env/" + env.getProperty("sta.datasource.name", "jdbc/sensorThings");
        bean.setJndiName(jndiName);
        bean.setProxyInterface(DataSource.class);
        bean.setLookupOnStartup(false);
        bean.afterPropertiesSet();
        return (DataSource)bean.getObject();
    }

}
