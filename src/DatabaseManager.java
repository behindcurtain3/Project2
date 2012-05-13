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
	// Unmutable strings to refer to the table names
	public final String TABLE_ITEMS = "items";
	public final String TABLE_TRANSACTIONS = "transactions";
	
	// The driver to connect with
	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:";
	private String dbName = "Project2DB";
	
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
			System.out.println(ex.getMessage());
		}
		
		try {
			createTransactionsTable();
		}
		catch(SQLException ex) {
			System.out.println(ex.getMessage());
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
	
	public Connection getConnection() {
		return connection;
	}
	
	public String getDbName() {
		return dbName;
	}
	
	private void createItemsTable() throws SQLException {
		String createString = 
			"create table " + dbName + "." + TABLE_ITEMS +
			"(ITEM_ID int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
			"NAME varchar(32) NOT NULL, " +
			"PRIMARY KEY (ITEM_ID))";
		
		Statement stmt = connection.createStatement();
	    stmt.executeUpdate(createString);
	    stmt.close();
	    
	    System.out.println("Created Items table.");
	}
	
	private void createTransactionsTable() throws SQLException {
		String createString = "" +
				"create table " + dbName + "." + TABLE_TRANSACTIONS +
				"(TRANSACTION_ID int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
				"DATE date NOT NULL, " + 
				"SUBTOTAL double NOT NULL, " +
				"SALES_TAX double NOT NULL, " +
				"GRAND_TOTAL double NOT NULL, " +
				"ITEMS clob, " +
				"PRIMARY KEY (TRANSACTION_ID))";
		
		Statement stmt = connection.createStatement();
		
		stmt.executeUpdate(createString);
		stmt.close();
		
		System.out.println("Created Transactions table.");
		
	}
}
