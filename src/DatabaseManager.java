//////////////////////////////////////////////////////////////////////
// DatabaseManager.java			Author: Justin Brown
//
// Handles all interactions with the database, from creating the 
// tables to connecting, updating and deleting information.
//////////////////////////////////////////////////////////////////////

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
	public String TABLE_ITEMS = "items";
	public String TABLE_TRANSACTIONS = "transactions";
	
	// The driver to connect with
	public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	public String protocol = "jdbc:derby:";
	public String dbName = "Project2DB";
	
	private Connection connection;
	
	public DatabaseManager() {
		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean connect() {
		try {
			// Connect to the database, creating it if needed
			connection = DriverManager.getConnection(protocol + dbName + ";create=true");
		}
		catch(SQLException sqlEx) {
			System.out.println(sqlEx.getMessage());
			return false;
		}
		
		try {
			// Derby has no built in query to check if a table exists or not
			// So just try to create the tables every time and fail silently if they already exist.
			createItemsTable();
		}
		catch(SQLException ex) {
			
		}
		
		/*
		try {
			// Check to see if the tables already exist
			Statement smt = connection.createStatement();
			ResultSet result = smt.executeQuery("SELECT * FROM " + dbName + "." + TABLE_ITEMS);
		
			if(!result.next()) {
				// Create the table
				System.out.println("Creating the " + TABLE_ITEMS + " table.");
				
				
				createItemsTable();
			}
			smt.close();
		}
		catch(SQLException sqlEx) {
			System.out.println(sqlEx.getMessage());
			return false;
		}
		*/
		return true;
		
	}
	
	public ResultSet executeQuery(String query) throws SQLException {
		
		Statement smt = connection.createStatement();
		
		return smt.executeQuery(query);
	}
	
	private void createItemsTable() throws SQLException {
		String createString = 
			"create table " + dbName + "." + TABLE_ITEMS +
			"(ITEM_ID int NOT NULL, " +
			"NAME varchar(32) NOT NULL, " +
			"PRIMARY KEY (ITEM_ID))";
		
		Statement stmt = connection.createStatement();
	    stmt.executeUpdate(createString);
	    stmt.close(); 
	    
	}
}
