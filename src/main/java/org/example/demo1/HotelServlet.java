package org.example.demo1;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import javax.naming.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/hotels")
public class HotelServlet extends HttpServlet {

    private DataSource ds;

    @Override
    public void init() throws ServletException {
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/jdbc/HotelDB");
        } catch (NamingException e) {
            throw new ServletException("JNDI lookup failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try (Connection con = ds.getConnection()) {
            initTableIfNeeded(con);

            out.println("<h1>Available hotels</h1><ul>");
            try (Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT ID, NOM, VILLE FROM HOTEL")) {
                while (rs.next()) {
                    out.printf("<li>%d - %s (%s)</li>%n",
                            rs.getInt("ID"), rs.getString("NOM"), rs.getString("VILLE"));
                }
            }
            out.println("</ul>");
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private synchronized void initTableIfNeeded(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS HOTEL (" +
                    "ID INT PRIMARY KEY, NOM VARCHAR(100), VILLE VARCHAR(100))");
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM HOTEL")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    st.executeUpdate("INSERT INTO HOTEL VALUES (1, 'Atlas Palace', 'Marrakech')");
                    st.executeUpdate("INSERT INTO HOTEL VALUES (2, 'Sahara Inn', 'Khouribga')");
                }
            }
        }
    }
}