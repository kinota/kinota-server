package com.cgi.kinota.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.core.env.Environment;

import javax.servlet.ServletException;

/**
 * Created by bmiles on 9/19/17.
 */
public class ServletContext implements ServletContextInitializer {

    @Autowired
    private Environment env;

    @Override
    public void onStartup(javax.servlet.ServletContext ctx) throws ServletException {
        // Service settings
        ctx.setInitParameter("ApiVersion",
                env.getProperty("sta.ApiVersion",
                        "v1.0"));
        ctx.setInitParameter("serviceRootUrl",
                env.getProperty("sta.serviceRootUrl",
                        "http://localhost:8080/SensorThingsService"));
        ctx.setInitParameter("defaultCount",
                env.getProperty("sta.defaultCount",
                        "true"));
        ctx.setInitParameter("defaultTop",
                env.getProperty("sta.defaultTop",
                        "100"));
        ctx.setInitParameter("maxTop",
                env.getProperty("sta.maxTop",
                        "10000"));
        ctx.setInitParameter("maxDataSize",
                env.getProperty("sta.maxDataSize",
                        "25000000"));
        ctx.setInitParameter("useAbsoluteNavigationLinks",
                env.getProperty("sta.useAbsoluteNavigationLinks",
                        "true"));
        // MQTT settings
        ctx.setInitParameter("mqtt.mqttServerImplementationClass",
                env.getProperty("sta.mqtt.mqttServerImplementationClass",
                        "de.fraunhofer.iosb.ilt.sensorthingsserver.mqtt.moquette.MoquetteMqttServer"));
        ctx.setInitParameter("mqtt.Enabled",
                env.getProperty("sta.mqtt.Enabled",
                        "true"));
        ctx.setInitParameter("mqtt.Port",
                env.getProperty("sta.mqtt.Port",
                        "1883"));
        ctx.setInitParameter("mqtt.QoS",
                env.getProperty("sta.mqtt.Qos",
                        "0"));
        ctx.setInitParameter("mqtt.SubscribeMessageQueueSize",
                env.getProperty("sta.mqtt.SubscribeMessageQueueSize",
                        "100"));
        ctx.setInitParameter("mqtt.SubscribeThreadPoolSize",
                env.getProperty("sta.mqtt.SubscribeThreadPoolSize",
                        "20"));
        ctx.setInitParameter("mqtt.CreateMessageQueueSize",
                env.getProperty("sta.mqtt.CreateMessageQueueSize",
                        "100"));
        ctx.setInitParameter("mqtt.CreateThreadPoolSize",
                env.getProperty("sta.mqtt.CreateThreadPoolSize",
                        "10"));
        ctx.setInitParameter("mqtt.Host",
                env.getProperty("sta.mqtt.Host",
                        "0.0.0.0"));
        ctx.setInitParameter("mqtt.internalHost",
                env.getProperty("sta.mqtt.internalHost",
                        "localhost"));
        ctx.setInitParameter("mqtt.WebsocketPort",
                env.getProperty("sta.mqtt.WebsocketPort",
                        "9876"));
        // Persistence settings
        ctx.setInitParameter("persistence.persistenceManagerImplementationClass",
                env.getProperty("sta.persistence.persistenceManagerImplementationClass",
                        "de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager"));
        ctx.setInitParameter("persistence.autoUpdateDatabase",
                env.getProperty("sta.persistence.autoUpdateDatabase",
                        "false"));
        ctx.setInitParameter("persistence.alwaysOrderbyId",
                env.getProperty("sta.persistence.alwaysOrderbyId",
                        "false"));
        ctx.setInitParameter("persistence.db_jndi_datasource",
                env.getProperty("sta.persistence.db_jndi_datasource",
                        "jdbc/sensorThings"));
    }
}
