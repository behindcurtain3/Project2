//////////////////////////////////////////////////////////////////////
// MainFrame.java			Author: Justin Brown
//
// The main class of the program. Initializes the UI, sets up the
// data, writes to and loads data from files and responds to UI
// events.
//////////////////////////////////////////////////////////////////////

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;


public class MainFrame extends JFrame {
	// Settings
	private Properties properties;
	private final String propertiesName = "settings.properties";
	private final String defaultDataName = "transactions.project1";
	
	// Database
	private DatabaseManager dbManager;
	
	// The transaction currently being entered
	private Transaction currentTransaction;
	
	// Reporting variables
	private double totalRevenue;
	private double totalSalesTax;
	
	// Hold our currency formatter
	private NumberFormat currencyFormat;
	
	// Variables for UI
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private JButton btnAddItem;
	private JButton btnSavePrint;
	private JTable tableTransactions;	
	private JTextField textItemName;
	private JTextField textItemPrice; 
	private JTextField textItemQuantity;
	private JTable tableItems;
	private JLabel lblSubTotalValue;
	private JLabel lblSalesTaxValue;
	private JLabel lblGrandTotalValue;	
	private JLabel numberOfTransactionsValue;
	private JLabel lblRevenueValue;
	private JLabel lblNetSalesTaxValue;
	private JLabel lblNetIncomeValue;
	
	// Files
	private String fileName;
	private Scanner dataFileScanner;
	private FileWriter dataFileStream;
	private BufferedWriter dataFileBuffer;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				// Try to set windows look and feel
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				
				// Setup the MainFrame
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Constructor.
	 */
	public MainFrame() {
		// Create our database manager object
		dbManager = new DatabaseManager();
		
		// Connect to the database
		dbManager.connect();
		
		
		// Set the icon for the program
		setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
		
		// Setup our transaction object, we will reuse this for all new transactions
		currentTransaction = new Transaction();
				
		// Read properties file.
		properties = new Properties();
		do {
			// Try to load the properties file
			try {
			    properties.load(new FileInputStream(propertiesName));		    
			    
			// If it couldn't be found, create one
			} catch (IOException e) {
				try {
					// Set the default file name
					properties.setProperty("file", defaultDataName);
					
					// Save it
				    properties.store(new FileOutputStream(propertiesName), null);
				
				// If there is an error creating the file, throw an error and exit
				} catch (IOException ee) {
					throw new Error("Unable to create the required settings file.");
				}
			}
		
			// Make sure the data file name is set
			if(!properties.containsKey("file")) {
		    	properties.setProperty("file", defaultDataName);
		    }
			
		} while(properties.isEmpty());
			    
		// Read in the data file name, always check to make sure the property value isn't ""
		// If it is "" use the default name
	    fileName = properties.getProperty("file").equals("") ? defaultDataName : properties.getProperty("file"); 
		
		// Setup currency format
		currencyFormat = NumberFormat.getCurrencyInstance();
		
		// Setup UI
		setupUI();
		
		// Setup the event listeners
		
		// Listen for double clicks on the recent transaction table
		// This will display the receipt for the selected transaction
		tableTransactions.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Only double clicks
				if(e.getClickCount() == 2) {
					// Get the row clicked on
					int row = tableTransactions.rowAtPoint(e.getPoint());
				
					// Display the receipt for the selected transaction
					TransactionsTableModel model = (TransactionsTableModel)tableTransactions.getModel();
					displayReceipt(model.getRow(row));
				}
			}
		});
		
		// Listen for clicks on the "Add Item" button
		btnAddItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent a) {
        		
        		// Make sure the input data isn't blank
        		if(textItemName.getText().equals("") || textItemPrice.getText().equals("") || textItemQuantity.getText().equals("")){
        			JOptionPane.showMessageDialog(null, "Please enter values for the name, price and quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        		} else {
        			// Try to parse in the inputs
        			try {
	        			// Setup the new item
	        			Item item = new Item();
	        			
	        			// Set the item data to the input data
	        			item.setName(textItemName.getText());
	        			item.setPrice(Double.parseDouble(textItemPrice.getText()));
	        			item.setQuantity(Integer.parseInt(textItemQuantity.getText()));
	        			
	        			// Add the item to our current transaction
	        			currentTransaction.addItem(item);
	        			
	        			// Run the calculate on the transaction so its current
	        			currentTransaction.calculate();
	        			
	        			// Add the item to the UI table
	        			tableItems.getModel().setValueAt(item, 0, 0);
	        			
	        			// Update the totals on the UI
	        			updateTotals();
	        			
	        			// Clear the input fields
	        			textItemName.setText("");
	        			textItemPrice.setText("");
	        			textItemQuantity.setText("");
	        			
	        		// Catch any parse errors and display a message to the user
        			} catch (NumberFormatException e) {
        				JOptionPane.showMessageDialog(null, "Please enter a valid number for the price and quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        			}        			
        		}
        	}
        });
		
		// Listen for clicks on the save & print receipt button
		btnSavePrint.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		
        		// Make sure the current transaction has at least one item entered, otherwise display an error message
        		if(currentTransaction.getItems().size() <= 0) {
        			JOptionPane.showMessageDialog(null, "Please add items to the transaction before saving it.", "Error", JOptionPane.ERROR_MESSAGE);
        		} else {
        		
	        		// Update the date on the transaction
	        		currentTransaction.setDate(new Date());
	        		
	        		// Add the transaction to the UI        		
	        		tableTransactions.getModel().setValueAt(currentTransaction, 0, 0);
	        		
	        		// Generate the receipt
	        		displayReceipt(currentTransaction);
	        		
	        		// Save the transaction to the data file
	        		saveTransaction(currentTransaction);
	        		
	        		// Update the report tab
	        		updateReport(currentTransaction);
	        		
	        		// Reset the current transaction
	        		resetTransaction();
        		}
        	}
        });
        
		/*
        // Load our data file, this will read previously saved transaction into the program
        try {
        	File f = new File(fileName);
        	Boolean newFile = false;
        	
        	// If the file doesn't exist create it
        	if(!f.exists()){
        		f.createNewFile();
        		newFile = true;
        	}
        	
        	// Setup our writing stream
			dataFileStream = new FileWriter(fileName, true);
			dataFileBuffer = new BufferedWriter(dataFileStream);
			
			// If the file is marked as new, add these lines at the top
			if(newFile) {
				dataFileBuffer.write("# Project 1 Transaction Log");
				dataFileBuffer.newLine();
				dataFileBuffer.write("# Created " + new Date().toString());
				dataFileBuffer.newLine();
				dataFileBuffer.flush();
			}
        	
        	// Use the scanner to load in recent transactions already in the data file
			dataFileScanner = new Scanner(new File(fileName));
			
			// Loop through the lines
			while(dataFileScanner.hasNext()) {
				// Read the line
				String line = dataFileScanner.nextLine();
				String[] segments;
				
				// If it isn't a comment try to load it
				if(line.startsWith("{") && line.endsWith("}")) {
					
					// Trim the { } off
					line = line.substring(1);
					line = line.substring(0, line.length() - 1);
					
					// Split the line into segments based on the , delimiter
					segments = line.split(",");
					
					// Skip any lines that have less than the minimum number of segments
					if(segments.length < 5) {
						continue;
					}
					
					// Create a new transaction that we will add data to
					Transaction t = new Transaction();
					
					// Try to parse in the data
					try {
						t.setDate(DateFormat.getInstance().parse(segments[0]));
						t.setSubTotal(Double.parseDouble(segments[1]));
						t.setSalesTax(Double.parseDouble(segments[2]));
						t.setGrandTotal(Double.parseDouble(segments[3]));
						
					// If there is a parse error continue to the next line
					} catch (ParseException ex) {
						continue;
					}
					
					// Loop through the items and add them to the transaction
					String item;
					for(int i = 4; i < segments.length; i++){
						item = segments[i];
						
						// Make sure its formatted correctly
						if(item.startsWith("[") && item.endsWith("]")) {
							
							// Split off the surrounding [ & ]
							item = item.substring(1);
							item = item.substring(0, item.length() - 1);
							
							// Split the item string into segments
							String itemSegments[] = item.split("~~");
							
							// If there aren't three segments something is wrong, just go the next one
							if(itemSegments.length != 3){
								continue;
							}
							
							// Load the data into a new item
							Item it = new Item();
							it.setName(itemSegments[0]);
							
							// try to parse the data into double & int respectively
							try {
								it.setPrice(Double.parseDouble(itemSegments[1]));
								it.setQuantity(Integer.parseInt(itemSegments[2]));
								
							// If it isn't formatted right go to the next item
							} catch (NumberFormatException ex) {
								continue;
							}
							
							// Add the item to the transaction
							t.addItem(it);
						}
					}					
					
					// Add the transaction to the recent transactions table
					tableTransactions.getModel().setValueAt(t, 0, 0);
					
					// Update the report tab
					updateReport(t);
					
				}
			}			
			
		// Catch any errors
		} catch (FileNotFoundException e1) {
			throw new Error("Unable to create the required data file.");
		} catch (IOException e) {
			throw new Error("Unable to create the required data file.");
		}
        */
		
		try {
			String strPs = "SELECT * FROM " + dbManager.getDbName() + "." + dbManager.TABLE_TRANSACTIONS;
			
			System.out.println(strPs);
			
			PreparedStatement ps = dbManager.getConnection().prepareStatement(strPs);
			ResultSet result = ps.executeQuery();
			
			while(result.next()) {
				
				Clob items = result.getClob("ITEMS");
				String itemStr = items.getSubString(1, (int) items.length());
				
				
				
				
				System.out.println(itemStr);
				Transaction t = new Transaction();
				t.setDate(result.getDate("DATE"));
				t.setSubTotal(result.getDouble("SUBTOTAL"));
				t.setSalesTax(result.getDouble("SALES_TAX"));
				t.setGrandTotal(result.getDouble("GRAND_TOTAL"));
				
				// Add the transaction to the recent transactions table
				tableTransactions.getModel().setValueAt(t, 0, 0);
				
				// Update the report tab
				updateReport(t);
			}
		}
		catch(SQLException ex) {
			System.out.println(ex.getMessage());
		}
        // Add a listener for when the application closes
  		// Use this to save and close files we may have open        
  		this.addWindowListener(new WindowAdapter() {
  			public void windowClosing(WindowEvent e) {
  				exit();
  			}
  		});
  		
	} // End of constructor
	
	/*
	 * This method saves a transaction to the data file.
	 * It formats the data in a way that can be parsed in again later.
	 */
	public void saveTransaction(Transaction transaction) {
		try {
			String itemBlob = "";
			// Loop through each item and add it to the item string
			for(int i = 0; i < transaction.getItems().size(); i++) {
				if(i != 0) {
					itemBlob += ","; // Add comma but not on the first item 	
				}
				
				itemBlob += "["; // Start of item
				
				// Add the data
				itemBlob += transaction.getItems().get(i).getName() + "~~";
				itemBlob += transaction.getItems().get(i).getPrice() + "~~";
				itemBlob += String.valueOf(transaction.getItems().get(i).getQuantity());
				
				itemBlob += "]"; // End of item
			}
			
			String psStr = "INSERT INTO " + dbManager.getDbName() + "." + dbManager.TABLE_TRANSACTIONS +
			"(DATE, SUBTOTAL, SALES_TAX, GRAND_TOTAL, ITEMS) " +
			"VALUES ('" + Timestamp.valueOf(transaction.getDateFormatted()) + "', " +
			transaction.getSubTotal() + ", " +
			transaction.getSalesTax() + ", " +
			transaction.getGrandTotal() + ", " +
			"'" + itemBlob + "')";
			
			PreparedStatement ps = dbManager.getConnection().prepareStatement(psStr);
			ps.executeUpdate();
			ps.close();
			
			System.out.println("Saved to database.");
		}
		catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}
		
		if(dataFileBuffer == null)
			return;
		
		// Write the transaction details to file
		try {
			dataFileBuffer.append("{"); // Start
			dataFileBuffer.append(transaction.getDate() + ","); // Add the date
			dataFileBuffer.append(transaction.getSubTotal() + ","); // Add the subtotal
			dataFileBuffer.append(transaction.getSalesTax() + ","); // Add the sales tax
			dataFileBuffer.append(transaction.getGrandTotal() + ","); // Add the grand total
			
			// Loop through each item and add it
			for(int i = 0; i < transaction.getItems().size(); i++) {
				if(i != 0) {
					dataFileBuffer.append(","); // Add comma but not on the first item 	
				}
				
				dataFileBuffer.append("["); // Start of item
				
				// Add the data
				dataFileBuffer.append(transaction.getItems().get(i).getName() + "~~");
				dataFileBuffer.append(transaction.getItems().get(i).getPrice() + "~~");
				dataFileBuffer.append(String.valueOf(transaction.getItems().get(i).getQuantity()));
				
				dataFileBuffer.append("]"); // End of item
			}
			
			dataFileBuffer.append("}"); // End
			dataFileBuffer.newLine(); // Always add a newline
			dataFileBuffer.flush(); // Flush the data to the file
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unable to save the transaction to file.");
		}
	}

	/*
	 * Resets the current transaction data, removing any items added and resetting the totals
	 */
	public void resetTransaction() {
		// Reset our items table
		((ItemsTableModel) tableItems.getModel()).reset();
		
		// Create a new transaction
		currentTransaction = new Transaction();
		
		// Update the UI
		updateTotals();
	}
	
	/*
	 * Update the totals of the current transaction
	 */
	public void updateTotals() {
		// Update the totals on the UI
		lblSubTotalValue.setText(currencyFormat.format(currentTransaction.getSubTotal()));
		lblSalesTaxValue.setText(currencyFormat.format(currentTransaction.getSalesTax()));
		lblGrandTotalValue.setText(currencyFormat.format(currentTransaction.getGrandTotal()));
	}
	
	/*
	 * Called when the program exits, saves the properties file and closes the data file
	 */
	public void exit() {
		try {
		    properties.store(new FileOutputStream(propertiesName), null);
		    
		    if(dataFileBuffer != null)
		    	dataFileBuffer.close();
		    
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "An error occured: " + e.getMessage());
		}
	}
	
	/*
	 * Takes a transaction as a parameter and creates a new tab on the UI with the receipt details
	 */
	public void displayReceipt(Transaction t) {		
		// Add a new tab
        JPanel receiptPanel = new JPanel();
        tabbedPane.addTab("Receipt", null, receiptPanel, null);
        
        // Make sure this new tab is selected
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        receiptPanel.setLayout(null);
        
        // Add all the UI components to the tab
        JLabel lblReceipt = new JLabel("Receipt Details");
        lblReceipt.setBounds(10, 11, 165, 22);
        lblReceipt.setFont(new Font("Cambria", Font.BOLD, 18));
        receiptPanel.add(lblReceipt);
        
        JSeparator separator1 = new JSeparator();
        separator1.setBounds(10, 39, 495, 4);
        receiptPanel.add(separator1);
        
        JLabel lblDate = new JLabel(t.getDate());
        lblDate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDate.setBounds(247, 18, 258, 14);
		receiptPanel.add(lblDate);
		
		JTable listItems = new JTable(new ItemsTableModel());
		listItems.setFillsViewportHeight(true);
		listItems.setEnabled(false);
		
		// Add items to table
		for(int i = 0; i < t.getItems().size(); i++) {
			listItems.setValueAt(t.getItems().get(i), 0, 0);
		}
		
		JScrollPane scrollPane1 = new JScrollPane(listItems);
		scrollPane1.setBackground(Color.WHITE);
		scrollPane1.setBounds(10, 52, 268, 247);
		receiptPanel.add(scrollPane1);
		
		JLabel lblSubTotal1 = new JLabel("Sub Total:");
		lblSubTotal1.setBounds(301, 69, 86, 14);
		receiptPanel.add(lblSubTotal1);
		
		JLabel lblSalesTax1 = new JLabel("Sales Tax:");
		lblSalesTax1.setBounds(301, 94, 86, 14);
		receiptPanel.add(lblSalesTax1);
		
		JLabel lblGrandTotal1 = new JLabel("Grand Total:");
		lblGrandTotal1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblGrandTotal1.setBounds(301, 135, 86, 14);
		receiptPanel.add(lblGrandTotal1);
		
		JLabel lblGrandTotalValue = new JLabel(currencyFormat.format(t.getGrandTotal()));
		lblGrandTotalValue.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblGrandTotalValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGrandTotalValue.setBounds(402, 135, 103, 14);
		receiptPanel.add(lblGrandTotalValue);
		
		JLabel lblSalesTaxValue = new JLabel(currencyFormat.format(t.getSalesTax()));
		lblSalesTaxValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSalesTaxValue.setBounds(402, 94, 103, 14);
		receiptPanel.add(lblSalesTaxValue);
		
		JLabel lblSubTotalValue = new JLabel(currencyFormat.format(t.getSubTotal()));
		lblSubTotalValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSubTotalValue.setBounds(402, 69, 103, 14);
		receiptPanel.add(lblSubTotalValue);
		
		// Add a "close receipt" button that closes the selected tab
		JButton btnCloseReceipt = new JButton("Close Receipt");
		btnCloseReceipt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				tabbedPane.remove(tabbedPane.getSelectedIndex());
				tabbedPane.setSelectedIndex(0);
			}
		});
		btnCloseReceipt.setBounds(319, 276, 155, 23);
		receiptPanel.add(btnCloseReceipt);
       
	}
	
	/*
	 * Takes a transaction as a parameter and updates the report tab data with the
	 * data from the transaction.
	 */
	public void updateReport(Transaction t) {		
		int numberOfTransactions = Integer.parseInt(numberOfTransactionsValue.getText());
		double netincome = 0;
		
		// Increment transaction count
		numberOfTransactions++;
		
		// Update the total with the new transaction data
		totalRevenue += t.getGrandTotal();
		totalSalesTax += t.getSalesTax(); 
		netincome = totalRevenue - totalSalesTax;
		
		// Update values on the UI
		numberOfTransactionsValue.setText(NumberFormat.getIntegerInstance().format(numberOfTransactions));
		lblRevenueValue.setText(currencyFormat.format(totalRevenue));
		lblNetSalesTaxValue.setText(currencyFormat.format(totalSalesTax));
		lblNetIncomeValue.setText(currencyFormat.format(netincome));
	}
	
	/*
	 * Setup all the default UI elements
	 */
	public void setupUI() {
		// Setup the JFrame
		setResizable(false);		
		setTitle("Project 2");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 550, 550);
		
		// The Menu
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);
		
		JMenuItem menButtonExit = new JMenuItem("Exit");
		menButtonExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				// Call our exit method
				exit();
				
				// Then call System exit
				System.exit(0);
			}
		});
		menuFile.add(menButtonExit);
		
		JMenu menuHelp = new JMenu("Help");
		menuBar.add(menuHelp);
		
		JMenuItem menuButtonAbout = new JMenuItem("About");
		menuButtonAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				JOptionPane.showMessageDialog(null, "Java Programming, Project #1 by Justin Brown");
			}
		});
		menuHelp.add(menuButtonAbout);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		tableTransactions = new JTable(new TransactionsTableModel());
		tableTransactions.setPreferredScrollableViewportSize(new Dimension(500, 30));
		tableTransactions.setFillsViewportHeight(true);		
		
		//Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(tableTransactions);
        scrollPane.setSize(524, 115);
        scrollPane.setLocation(10, 375);
 
        //Add the scroll pane to this panel.
        contentPane.add(scrollPane);
        
        JLabel lblRecentEntries = new JLabel("Recent Transactions");
        lblRecentEntries.setFont(new Font("Calibri", Font.BOLD, 11));
        lblRecentEntries.setBounds(10, 360, 95, 14);
        contentPane.add(lblRecentEntries);
        
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(10, 11, 524, 338);
        contentPane.add(tabbedPane);
        
        JPanel tabTransactionPanel = new JPanel();
        tabbedPane.addTab("Transaction", null, tabTransactionPanel, null);
        tabTransactionPanel.setLayout(null);
        
        JLabel lblTransactionDetails = new JLabel("Transaction Details");
        lblTransactionDetails.setFont(new Font("Cambria", Font.BOLD, 18));
        lblTransactionDetails.setBounds(10, 11, 223, 22);
        tabTransactionPanel.add(lblTransactionDetails);
        
        JSeparator separator = new JSeparator();
        separator.setBounds(10, 39, 495, 4);
        tabTransactionPanel.add(separator);
        
        JLabel lblItemName = new JLabel("Item Name");
        lblItemName.setBounds(10, 54, 85, 14);
        tabTransactionPanel.add(lblItemName);
        
        textItemName = new JTextField();
        textItemName.setBounds(147, 51, 323, 20);
        tabTransactionPanel.add(textItemName);
        textItemName.setColumns(10);
        
        JLabel lblItemPrice = new JLabel("Item Price");
        lblItemPrice.setBounds(10, 79, 100, 14);
        tabTransactionPanel.add(lblItemPrice);
        
        textItemPrice = new JTextField();
        textItemPrice.setBounds(147, 76, 86, 20);
        tabTransactionPanel.add(textItemPrice);
        
        JLabel lblQuantity = new JLabel("Quantity");
        lblQuantity.setBounds(10, 104, 85, 14);
        tabTransactionPanel.add(lblQuantity);
        
        textItemQuantity = new JTextField();
        textItemQuantity.setBounds(147, 101, 86, 20);
        tabTransactionPanel.add(textItemQuantity);
        textItemQuantity.setColumns(10);
        
        JLabel lblSubTotal = new JLabel("Sub Total:");
        lblSubTotal.setBounds(345, 170, 70, 14);
        tabTransactionPanel.add(lblSubTotal);
        
        JLabel lblSalesTax = new JLabel("Sales Tax:");
        lblSalesTax.setBounds(345, 192, 85, 14);
        tabTransactionPanel.add(lblSalesTax);
        
        JLabel lblGrandTotal = new JLabel("Grand Total:");
        lblGrandTotal.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblGrandTotal.setBounds(345, 217, 85, 14);
        tabTransactionPanel.add(lblGrandTotal);
        
        lblSubTotalValue = new JLabel("$0");
        lblSubTotalValue.setHorizontalAlignment(SwingConstants.RIGHT);
        lblSubTotalValue.setBounds(425, 170, 80, 14);
        tabTransactionPanel.add(lblSubTotalValue);
        
        lblSalesTaxValue = new JLabel("$0");
        lblSalesTaxValue.setHorizontalAlignment(SwingConstants.RIGHT);
        lblSalesTaxValue.setBounds(425, 192, 80, 14);
        tabTransactionPanel.add(lblSalesTaxValue);
        
        lblGrandTotalValue = new JLabel("$0");
        lblGrandTotalValue.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblGrandTotalValue.setHorizontalAlignment(SwingConstants.RIGHT);
        lblGrandTotalValue.setBounds(425, 217, 80, 14);
        tabTransactionPanel.add(lblGrandTotalValue);
        
        btnAddItem = new JButton("Add Item");        
        btnAddItem.setBounds(147, 132, 89, 23);
        tabTransactionPanel.add(btnAddItem);
        
        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent a) {
        		// Clear the input fields
    			textItemName.setText("");
    			textItemPrice.setText("");
    			textItemQuantity.setText("");
        	}
        });
        btnClear.setBounds(246, 132, 89, 23);
        tabTransactionPanel.add(btnClear);
        
        tableItems = new JTable(new ItemsTableModel());
        tableItems.setFillsViewportHeight(true);
        
        JScrollPane scrollPaneItemTable = new JScrollPane(tableItems);
        scrollPaneItemTable.setBounds(10, 166, 325, 133);
        tabTransactionPanel.add(scrollPaneItemTable);
        
        btnSavePrint = new JButton("Save & Print Receipt");        
        btnSavePrint.setBounds(345, 242, 160, 23);
        tabTransactionPanel.add(btnSavePrint);
        
        JButton btnClearAllItems = new JButton("Clear All Items");
        btnClearAllItems.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		resetTransaction();
        	}
        });
        btnClearAllItems.setBounds(345, 276, 160, 23);
        tabTransactionPanel.add(btnClearAllItems);
        
        JPanel tabReportPanel = new JPanel();
        tabbedPane.addTab("Report", null, tabReportPanel, null);
        tabReportPanel.setLayout(null);
        
        JLabel lblReportDetails = new JLabel("Report Details");
        lblReportDetails.setBounds(10, 11, 165, 22);
        lblReportDetails.setFont(new Font("Cambria", Font.BOLD, 18));
        tabReportPanel.add(lblReportDetails);
        
        JSeparator separator_1 = new JSeparator();
        separator_1.setBounds(10, 39, 499, 4);
        tabReportPanel.add(separator_1);
        
        JLabel lblNumberOfTransactions = new JLabel("Number of Transactions:");
        lblNumberOfTransactions.setBounds(10, 54, 165, 14);
        tabReportPanel.add(lblNumberOfTransactions);
        
        numberOfTransactionsValue = new JLabel("0");
        numberOfTransactionsValue.setBounds(185, 54, 46, 14);
        tabReportPanel.add(numberOfTransactionsValue);
        
        JLabel lblRevenue = new JLabel("Revenue:");
        lblRevenue.setBounds(10, 79, 150, 14);
        tabReportPanel.add(lblRevenue);
        
        lblRevenueValue = new JLabel("$0");
        lblRevenueValue.setBounds(185, 79, 113, 14);
        tabReportPanel.add(lblRevenueValue);
        
        JLabel lblSalesTax_1 = new JLabel("Sales Tax:");
        lblSalesTax_1.setBounds(10, 104, 150, 14);
        tabReportPanel.add(lblSalesTax_1);
        
        lblNetSalesTaxValue = new JLabel("$0");
        lblNetSalesTaxValue.setBounds(185, 104, 113, 14);
        tabReportPanel.add(lblNetSalesTaxValue);
        
        JLabel lblNetIncome = new JLabel("Net Income:");
        lblNetIncome.setBounds(10, 129, 150, 14);
        tabReportPanel.add(lblNetIncome);
        
        lblNetIncomeValue = new JLabel("$0");
        lblNetIncomeValue.setBounds(185, 129, 113, 14);
        tabReportPanel.add(lblNetIncomeValue);
        
        JLabel lblDetailsAreCurrent = new JLabel("Details are updated after each transaction is saved");
        lblDetailsAreCurrent.setFont(new Font("Calibri", Font.BOLD, 11));
        lblDetailsAreCurrent.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDetailsAreCurrent.setBounds(223, 18, 286, 14);
        tabReportPanel.add(lblDetailsAreCurrent);
	}
}
