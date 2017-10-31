package com.cgi.kinota.server.security.jwt.application;

import com.cgi.kinota.server.Application;
import com.cgi.kinota.server.security.jwt.domain.Agent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static com.cgi.kinota.server.security.jwt.Constants.DEFAULT_ID;
import static com.cgi.kinota.server.security.jwt.Constants.DEFAULT_ID_UPPER;
import static com.cgi.kinota.server.security.jwt.Constants.DEFAULT_KEY;

import static org.junit.Assert.*;

/**
 * Created by bmiles on 10/3/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class})
public class AgentServiceTest {
	private static final Logger logger = LoggerFactory.getLogger(AgentServiceTest.class);

	@Autowired
	AgentService agentService;

	@Test
	public void retrieveDevice() throws Exception {
		try {
			Agent a = agentService.retrieveAgent(DEFAULT_ID);
			assertNotNull(a);
			assertEquals(a.getKey(), DEFAULT_KEY);
		} catch (Exception e){
			logger.error(e.getMessage(),e);
			fail(e.getMessage());
		}
	}

	@Test
	public void retrieveDeviceUpperCaseUUID() throws Exception {
		try {
			Agent a = agentService.retrieveAgent(DEFAULT_ID_UPPER);
			assertNotNull(a);
			assertEquals(a.getKey(), DEFAULT_KEY);
		} catch (Exception e){
			logger.error(e.getMessage(),e);
			fail(e.getMessage());
		}
	}

}
