package com.cgi.kinota.server.servlet;

import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by bmiles on 9/20/17.
 */
@WebServlet(name = "DatabaseStatus", urlPatterns = {"/DatabaseStatus"})
public class DatabaseStatus extends HttpServlet {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseStatus.class);
    private static final String DESCRIPTION = "Database status and upgrade servlet.";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(ContextListener.TAG_CORE_SETTINGS);
        response.setContentType("text/html;charset=UTF-8");
        LOGGER.info("DatabaseStatus Servlet called.");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet DatabaseStatus</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet DatabaseStatus at " + request.getContextPath() + "</h1>");
            out.println("<p>Checking Database status.</p>");
            out.println("<p><form action='DatabaseStatus' method='post' enctype='application/x-www-form-urlencoded'>");
            out.println("<button name='doupdate' value='Do Update' type='submit'>Do Update</button>");
            out.println("</form></p>");
            out.println("<p><a href='.'>Back...</a></p>");
            try {
                Connection connection = PostgresPersistenceManager.getConnection(coreSettings);

                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                Liquibase liquibase = new liquibase.Liquibase("liquibase/tables.xml", new ClassLoaderResourceAccessor(), database);
                out.println("<pre>");
                liquibase.update(new Contexts(), out);
                out.println("</pre>");
                database.commit();
                database.close();
                connection.close();

            } catch (SQLException | DatabaseException | NamingException ex) {
                LOGGER.error("Could not initialise database.", ex);
                out.println("<p>Failed to initialise database:<br>");
                out.println(ex.getLocalizedMessage());
                out.println("</p>");
            } catch (LiquibaseException ex) {
                LOGGER.error("Could not upgrade database.", ex);
                out.println("<p>Failed to upgrade database:<br>");
                out.println(ex.getLocalizedMessage());
                out.println("</p>");
            }

            out.println("<p>Done. Click the button to execute the listed updates.</p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        CoreSettings coreSettings = (CoreSettings) request.getServletContext().getAttribute(ContextListener.TAG_CORE_SETTINGS);
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet DatabaseStatus</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet DatabaseStatus at " + request.getContextPath() + "</h1><p>Updating Database</p>");

            try {
                Connection connection = PostgresPersistenceManager.getConnection(coreSettings);

                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                Liquibase liquibase = new liquibase.Liquibase("liquibase/tables.xml", new ClassLoaderResourceAccessor(), database);
                out.println("<pre>");
                liquibase.update(new Contexts());
                out.println("</pre>");
                database.commit();
                database.close();
                connection.close();

            } catch (SQLException | DatabaseException | NamingException ex) {
                LOGGER.error("Could not initialise database.", ex);
                out.println("<p>Failed to initialise database:<br>");
                out.println(ex.getLocalizedMessage());
                out.println("</p>");
            } catch (LiquibaseException ex) {
                LOGGER.error("Could not upgrade database.", ex);
                out.println("<p>Failed to upgrade database:<br>");
                out.println(ex.getLocalizedMessage());
                out.println("</p>");
            }

            out.println("<p>Done. <a href='DatabaseStatus'>Back...</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processPostRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return DESCRIPTION;
    }

}
