package com.cgi.kinota.server.servlet;

import de.fraunhofer.iosb.ilt.sta.Servlet_1_0;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;

/**
 * Created by bmiles on 9/13/17.
 * see de.fraunhofer.iosb.ilt.sta.Servlet_1_0
 */
@WebServlet(
        name = "STA1.0",
        urlPatterns = {"/v1.0", "/v1.0/*"},
        initParams = {
                @WebInitParam(name = "readonly", value = "false")
        }
)
@MultipartConfig()
public class STAServlet extends Servlet_1_0 {}
