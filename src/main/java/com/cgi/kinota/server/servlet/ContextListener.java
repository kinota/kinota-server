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
public class ContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);
    public static final String TAG_CORE_SETTINGS = "CoreSettings";

    public void contextInitialized(ServletContextEvent sce) {
        if (sce != null && sce.getServletContext() != null) {
            LOGGER.info("Context initialised, loading settings.");
            ServletContext context = sce.getServletContext();
            Properties properties = new Properties();
            Enumeration<String> names = context.getInitParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                properties.put(name, context.getInitParameter(name));
            }
            CoreSettings coreSettings = new CoreSettings(
                    properties,
                    URI.create(properties.getProperty(CoreSettings.TAG_SERVICE_ROOT_URL) + "/" + properties.getProperty(CoreSettings.TAG_API_VERSION)).normalize().toString(),
                    context.getAttribute(ServletContext.TEMPDIR).toString());
            context.setAttribute(TAG_CORE_SETTINGS, coreSettings);
            PersistenceManagerFactory.init(coreSettings);
            MqttManager.init(coreSettings);
            PersistenceManagerFactory.addEntityChangeListener(MqttManager.getInstance());
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Context destroyed, shutting down threads...");
        MqttManager.shutdown();
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException ex) {
            LOGGER.debug("Rude wakeup?", ex);
        }
        LOGGER.info("Context destroyed, done shutting down threads.");
    }
}
