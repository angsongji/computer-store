package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLConnect {
    private String user = "root";
    private String password = "";
    private String url = "jdbc:mysql://localhost:3306/computerstore";
    private Connection conn = null;
    private Statement st = null;

    public MySQLConnect() {
    
    }
    
    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = conn = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(MySQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void disConnect() {
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(MySQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ResultSet executeQuery(String sql) {
        ResultSet rs = null;
        try {
            connect();
            st = conn.createStatement();
            rs = st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(MySQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    public void executeUpdate(String sql) {
        try {
            connect();
            st = conn.createStatement();
            st.executeUpdate(sql);
            disConnect();
        } catch (SQLException ex) {
            Logger.getLogger(MySQLConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
