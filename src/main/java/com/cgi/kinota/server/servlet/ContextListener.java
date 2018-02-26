package com.cgi.kinota.server.servlet;

import de.fraunhofer.iosb.ilt.sta.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by bmiles on 9/13/17.
 * see de.fraunhofer.iosb.ilt.sta.ContextListener
 */
@WebListener
public class ContextListener extends de.fraunhofer.iosb.ilt.sta.ContextListener {}
