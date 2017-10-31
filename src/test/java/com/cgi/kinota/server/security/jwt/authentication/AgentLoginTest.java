package com.cgi.kinota.server.security.jwt.authentication;

import com.cgi.kinota.server.Application;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

import static com.cgi.kinota.server.security.jwt.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by bmiles on 10/4/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class})
public class AgentLoginTest {

	private static final Logger logger = LoggerFactory.getLogger(AgentLoginTest.class);

	@LocalServerPort
	int port;

	@Test
	public void testLogin() {
		try {
			String authUrl = String.format(AUTH_URL_BASE, port);
			JsonObject request = Json.createObjectBuilder()
					.add("id", DEFAULT_ID)
					.add("key", DEFAULT_KEY)
					.build();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
			RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<String> response = rest.exchange(authUrl, HttpMethod.POST, entity, String.class);

			assertEquals(200, response.getStatusCodeValue());
			JsonReader reader = Json.createReader(new StringReader(response.getBody()));
			JsonObject o = reader.readObject();
			String token = o.getString("token");
			assertTrue(!StringUtils.isEmpty(token));
			logger.info(token);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	@Test
	public void testLoginUpperCaseUUIDs() {
		try {
			String authUrl = String.format(AUTH_URL_BASE, port);
			JsonObject request = Json.createObjectBuilder()
					.add("id", DEFAULT_ID_UPPER)
					.add("key", DEFAULT_KEY_UPPER)
					.build();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
			RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<String> response = rest.exchange(authUrl, HttpMethod.POST, entity, String.class);

			assertEquals(200, response.getStatusCodeValue());
			JsonReader reader = Json.createReader(new StringReader(response.getBody()));
			JsonObject o = reader.readObject();
			String token = o.getString("token");
			assertTrue(!StringUtils.isEmpty(token));
			logger.info(token);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	@Test
	public void testInvalidLogin() {
		try {
			String authUrl = String.format(AUTH_URL_BASE, port);
			JsonObject request = Json.createObjectBuilder()
					.add("id", DEFAULT_ID)
					.add("key", "bogus")
					.build();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
			RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			rest.exchange(authUrl, HttpMethod.POST, entity, String.class);

		} catch (HttpClientErrorException e) {
			assertEquals(401, e.getRawStatusCode());
			JsonReader reader = Json.createReader(new StringReader(e.getResponseBodyAsString()));
			JsonObject o = reader.readObject();
			String message = o.getString("message");
			assertEquals("Invalid username or password", message);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}
}
