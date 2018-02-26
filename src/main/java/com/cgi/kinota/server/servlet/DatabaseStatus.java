package com.cgi.kinota.server.servlet;

import javax.servlet.annotation.WebServlet;

/**
 * Created by bmiles on 9/20/17.
 */
@WebServlet(name = "DatabaseStatus", urlPatterns = {"/DatabaseStatus"})
public class DatabaseStatus extends de.fraunhofer.iosb.ilt.sta.DatabaseStatus {}
