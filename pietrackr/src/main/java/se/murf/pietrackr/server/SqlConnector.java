package se.murf.pietrackr.server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqlConnector {
	private Connection con = null;
	private Statement st = null;
    private ResultSet rs = null;

    private String url = "jdbc:mysql://localhost:3306/raw";
    private String user = "server";
    private String password = "d0Ty4dltRbyM";

	public  SqlConnector() {
		try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            rs = st.executeQuery("SELECT VERSION()");

            if (rs.next()) {
                System.out.println(rs.getString(1));
            }

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(SqlConnector.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(SqlConnector.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
	}
	/*
		create table raw (
		id INT AUTO_INCREMENT PRIMARY KEY,
		timestamp TIMESTAMP, INDEX(timestamp),
		latitude VARCHAR(20),
		longitude VARCHAR(20),
		speed VARCHAR(10),
		altitude VARCHAR(10),
		comment VARCHAR(300)
		);
	 */
	public void addRow(String lat, String lon, String speed, String alt, Long time) {
		
	}
}
