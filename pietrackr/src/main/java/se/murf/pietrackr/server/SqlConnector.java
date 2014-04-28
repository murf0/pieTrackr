package se.murf.pietrackr.server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;



public class SqlConnector {
	private Connection con = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    Logger LOGGER = Logger.getLogger(SqlConnector.class.getName());
    
    private String url = "jdbc:mysql://localhost:3306/tracking";
    private String user = "server";
    private String password = "d0Ty4dltRbyM";

	public  SqlConnector() {
		try {
            con = DriverManager.getConnection(url, user, password);
           // pst = con.prepareStatement("INSERT INTO raw(timestamp,device,user,topic,latitude,longitude,speed,altitude,comment) VALUES(?,?,?,?,?,?,?,?,?)");
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
					LOGGER.info("Add row to SQL");
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
}
