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
	public final String TABLE_INVENTORY = "inventory";
	public final String TABLE_TRANSACTIONS = "transactions";
	
	// The driver to connect with
	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:";
	private String dbName = "Project2DB";
	
	private Connection connection;
	
	public DatabaseManager() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName(driver).newInstance();		
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
			createInventoryTable();			
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
		
		return true;		
	}
	
	// Executes a query for the string passed in
	public ResultSet executeQuery(String query) throws SQLException {
		
		Statement smt = connection.createStatement();
		
		return smt.executeQuery(query);
	}
	
	// Return the database connection
	public Connection getConnection() {
		return connection;
	}
	
	// Return the database name
	public String getDbName() {
		return dbName;
	}
	
	// Create the items table
	private void createInventoryTable() throws SQLException {
		String createString = 
			"create table " + dbName + "." + TABLE_INVENTORY +
			"(ITEM_ID int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
			"NAME varchar(32) NOT NULL, " +
			"DEFAULT_PRICE double NOT NULL, " +
			"ACTIVE smallint DEFAULT 1 NOT NULL, " +
			"PRIMARY KEY (ITEM_ID))";
		
		Statement stmt = connection.createStatement();
	    stmt.executeUpdate(createString);
	    stmt.close();
	}
	
	// Create the transactions table
	private void createTransactionsTable() throws SQLException {
		String createString = "" +
				"create table " + dbName + "." + TABLE_TRANSACTIONS +
				"(TRANSACTION_ID int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
				"DATE timestamp NOT NULL, " + 
				"SUBTOTAL double NOT NULL, " +
				"SALES_TAX double NOT NULL, " +
				"GRAND_TOTAL double NOT NULL, " +
				"ITEMS clob, " +
				"PRIMARY KEY (TRANSACTION_ID))";
		
		Statement stmt = connection.createStatement();
		
		stmt.executeUpdate(createString);
		stmt.close();		
	}
}
