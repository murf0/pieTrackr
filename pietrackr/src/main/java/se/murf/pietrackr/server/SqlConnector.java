package se.murf.pietrackr.server;


import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import org.json.JSONObject;

import se.murf.pietrackr.Configuration;

public class SqlConnector {
	private Connection con = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    
    private ComboPooledDataSource cpds;
    
    Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    private String url;
    private String user;
    private String password;

	public  SqlConnector(Configuration config) throws IOException, SQLException, PropertyVetoException {
		cpds = new ComboPooledDataSource();
        
        // the settings below are optional -- c3p0 can work with defaults
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        cpds.setMaxStatements(180);
        
		url = config.getProperty("sqlUrl");
		user = config.getProperty("sqlUser");
		password = config.getProperty("sqlPassword");
		try {
			cpds.setDriverClass("com.mysql.jdbc.Driver"); //loads the jdbc driver
	        cpds.setJdbcUrl(url);
	        cpds.setUser(user);
	        cpds.setPassword(password);
	        con = cpds.getConnection();
	        pst = con.prepareStatement("INSERT INTO raw(timestamp,device,user,topic,latitude,longitude,speed,altitude) VALUES(?,?,?,?,?,?,?,?)");
	} catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
	}

	/*
		create table raw (
		id INT AUTO_INCREMENT PRIMARY KEY,
		timestamp int(11), INDEX(timestamp),
		device VARCHAR(20),
		user VARCHAR(20),
		topic VARCHAR(20),
		latitude VARCHAR(20),
		longitude VARCHAR(20),
		speed VARCHAR(10),
		altitude VARCHAR(10),
		comment VARCHAR(300)
		);
	 */
	public void addRow(JSONObject obj) {
		try {
			if (obj.getString("_type").equals("location")) {
				try {
					LOGGER.finer("Add row to SQL");
					pst.setLong(1, obj.getInt("tst")); //timestamp
					pst.setString(2, obj.getString("device")); //Device
					pst.setString(3, obj.getString("user")); //user
					pst.setString(4, obj.getString("topic")); //topic
					pst.setString(5, obj.getString("lat")); //latitude
					pst.setString(6, obj.getString("lon")); //longitude
					pst.setString(7, obj.getString("speed")); //speed
					pst.setString(8, obj.getString("alt")); //altitude
					//pst.setString(9, ""); //comment not in sql clause must add above
					pst.executeUpdate();
				} catch (SQLException ex) {
		            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void disconnect() {
		try {
            if (rs != null) {
                rs.close();
            }
            if (pst != null) {
                pst.close();
            }
            if (con != null) {
                con.close();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
	}
	
    public Connection getConnection() throws SQLException {
        return this.cpds.getConnection();
    }

}
