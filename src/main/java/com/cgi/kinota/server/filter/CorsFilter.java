package com.cgi.kinota.server.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Created by bmiles on 9/13/17.
 *
 *  <filter>
 *    <filter-name>CorsFilter</filter-name>
 *    <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
 *    <init-param>
 *      <param-name>cors.allowed.origins</param-name>
 *      <param-value>*</param-value>
 *    </init-param>
 *    <init-param>
 *      <param-name>cors.allowed.methods</param-name>
 *      <param-value>GET,HEAD,OPTIONS,POST,DELETE,PUT</param-value>
 *    </init-param>
 *    <init-param>
 *      <param-name>cors.support.credentials</param-name>
 *      <param-value>true</param-value>
 *    </init-param>
 *    <init-param>
 *      <param-name>cors.allowed.headers</param-name>
 *      <param-value>Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization</param-value>
 *    </init-param>
 *  </filter>
 *
 *  <filter-mapping>
 *    <filter-name>CorsFilter</filter-name>
 *    <url-pattern>/*</url-pattern>
 *  </filter-mapping>
 *
 *
 */
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,DELETE,PUT");
        response.setHeader("Access-Control-Allow-Headers", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "180");

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {}
}
