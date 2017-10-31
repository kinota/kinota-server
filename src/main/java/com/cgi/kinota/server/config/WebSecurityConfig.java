package com.cgi.kinota.server.config;

import com.cgi.kinota.server.filter.CorsFilter;
import com.cgi.kinota.server.security.jwt.ApiPatternMatcher;
import com.cgi.kinota.server.security.jwt.RestAuthenticationEntryPoint;
import com.cgi.kinota.server.security.jwt.authentication.AgentAuthenticationProvider;
import com.cgi.kinota.server.security.jwt.authentication.AgentLoginProcessingFilter;
import com.cgi.kinota.server.security.jwt.authentication.JwtAuthenticationProvider;
import com.cgi.kinota.server.security.jwt.authentication.JwtTokenAuthenticationProcessingFilter;
import com.cgi.kinota.server.security.jwt.authentication.JwtTokenUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.SessionManagementFilter;

/**
 * Created by bmiles on 9/15/17.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

	private static final String FORM_BASED_LOGIN_ENTRY_POINT = "/auth/login";

	@Autowired
	private Environment env;
	@Autowired
	private RestAuthenticationEntryPoint authenticationEntryPoint;
	@Autowired
	private AuthenticationSuccessHandler successHandler;
	@Autowired
	private AuthenticationFailureHandler failureHandler;
	@Autowired
	private AgentAuthenticationProvider agentAuthenticationProvider;
	@Autowired
	private JwtAuthenticationProvider jwtAuthenticationProvider;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private ObjectMapper objectMapper;

	private final String contextPath;
	private final String apiVersion;
	private final String formBasedAuthEntry;
	private final String apiAuthEntry;
	private final String dbStatusAuthEntry;

	public WebSecurityConfig() {
		contextPath = "/SensorThingsService";
		formBasedAuthEntry = FORM_BASED_LOGIN_ENTRY_POINT;
		apiVersion = "v1.0";
		apiAuthEntry = "/" + apiVersion + "/**";
		dbStatusAuthEntry = "/DatabaseStatus/**";
	}

	protected AgentLoginProcessingFilter buildDeviceLoginProcessingFilter() throws Exception {
		AgentLoginProcessingFilter filter = new AgentLoginProcessingFilter(FORM_BASED_LOGIN_ENTRY_POINT, successHandler,
				failureHandler, objectMapper);
		filter.setAuthenticationManager(this.authenticationManager);
		return filter;
	}

	protected JwtTokenAuthenticationProcessingFilter buildJwtTokenAuthenticationProcessingFilter() throws Exception {

		JwtTokenAuthenticationProcessingFilter filter
				= new JwtTokenAuthenticationProcessingFilter(
				new ApiPatternMatcher(apiAuthEntry), failureHandler, jwtTokenUtil);
		filter.setAuthenticationManager(this.authenticationManager);
		return filter;
	}

	protected JwtTokenAuthenticationProcessingFilter buildJwtTokenAuthenticationProcessingFilterDbStatus() throws Exception {

		JwtTokenAuthenticationProcessingFilter filter
				= new JwtTokenAuthenticationProcessingFilter(
				new ApiPatternMatcher(dbStatusAuthEntry), failureHandler, jwtTokenUtil);
		filter.setAuthenticationManager(this.authenticationManager);
		return filter;
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(agentAuthenticationProvider);
		auth.authenticationProvider(jwtAuthenticationProvider);
	}

	@Bean
	public CorsFilter corsFilter() { return new CorsFilter(); }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
		http
				.csrf().disable()
				.exceptionHandling()
				.authenticationEntryPoint(this.authenticationEntryPoint)
				.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.authorizeRequests()
				.antMatchers(HttpMethod.POST, formBasedAuthEntry).permitAll()
				.antMatchers(HttpMethod.GET, apiAuthEntry).permitAll()
				.antMatchers(apiAuthEntry).authenticated()
				.antMatchers(dbStatusAuthEntry).access("hasIpAddress('127.0.0.1')")
				.anyRequest().permitAll()
				.and()
				.addFilterBefore(corsFilter(), SessionManagementFilter.class)
				.addFilterBefore(buildDeviceLoginProcessingFilter(),
						UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(buildJwtTokenAuthenticationProcessingFilter(),
						UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(buildJwtTokenAuthenticationProcessingFilterDbStatus(),
						UsernamePasswordAuthenticationFilter.class);
    }
}
