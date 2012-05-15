//////////////////////////////////////////////////////////////////////
// MainFrame.java			Author: Justin Brown
//
// The main class of the program. Initializes the UI, sets up the
// data, writes to and loads data from the database and responds to UI
// events.
//////////////////////////////////////////////////////////////////////

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JComboBox;


public class MainFrame extends JFrame {	
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
	private JComboBox comboItemName;
	private JButton btnAddItem;
	private JButton btnSavePrint;
	private JTable tableTransactions;	
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
	private JTextField textAddItemName;
	private JTextField textAddItemPrice;
	private JTable tableInventory;
	private JLabel lblReportGeneratedAt;

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
		// Try to create our database manager object
		try {
			dbManager = new DatabaseManager();
			
		// Catch any exceptions and display an error message.
		} catch (InstantiationException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
		} catch (IllegalAccessException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Database driver not found: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
		}
		finally	{
			// If the database wasn't created or we can't connect to it display an error message and exit the application
			if(dbManager == null || !dbManager.connect())
			{
				JOptionPane.showMessageDialog(null, "Unable to connect to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}		
		
		// Set the icon for the program
		setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
		
		// Setup our transaction object, we will reuse this for all new transactions
		currentTransaction = new Transaction();
		
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
        		
        		// Make sure the item combobox is enabled, otherwise there is no item to add
        		if(!comboItemName.isEnabled())
        			return;
        		
        		// Make sure the input data isn't blank
        		String itemName = (String)comboItemName.getSelectedItem();
        		
        		if(itemName.equals("") || textItemPrice.getText().equals("") || textItemQuantity.getText().equals("")){
        			JOptionPane.showMessageDialog(null, "Please enter values for the name, price and quantity.", "Error", JOptionPane.ERROR_MESSAGE);
        		} else {
        			// Try to parse in the inputs
        			try {
	        			// Setup the new item
	        			Item item = new Item();
	        			
	        			// Set the item data to the input data
	        			item.setName(itemName);
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
	        		
	        		// Reset the current transaction
	        		resetTransaction();
	        		
	        		// Refresh transactions table
	        		refreshTransactionsTable();
        		}
        	}
        });
		
		refreshTransactionsTable();		
		refreshInventoryTable();
		refreshItemComboBox();
		
		
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
			JOptionPane.showMessageDialog(null, "Unable to save the transaction to database.", "Error", JOptionPane.ERROR_MESSAGE);
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
		    dbManager.getConnection().close();
		    
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "An error occured: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
				JOptionPane.showMessageDialog(null, "Java Programming, Project #2 by Justin Brown");
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
        
        comboItemName = new JComboBox();
        comboItemName.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		String name = (String)comboItemName.getSelectedItem();
        		
        		// if the selected string is blank return
        		if(name == null || name.equals(""))
        			return;
        		
        		// Try to load the default price for this item
        		try {
        			// Setup the query
        			String query = "SELECT (DEFAULT_PRICE) FROM " + dbManager.getDbName() + "." + dbManager.TABLE_INVENTORY +
        					" WHERE NAME = '" + name + "'";
        			
        			// Execute it
        			PreparedStatement ps = dbManager.getConnection().prepareStatement(query);
        			ResultSet result = ps.executeQuery();
        			
        			
        			while(result.next()) {
        				// Add the default price into the item price text field
        				textItemPrice.setText(String.format("%.2f", result.getDouble("DEFAULT_PRICE")));
        			}
        			
        			// Request the window to set focus to the quantity field
        			textItemQuantity.requestFocusInWindow();
        			
        			result.close();
        			ps.close();
        			
        		} catch(SQLException e) {
        			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        		}
        	}
        });
        comboItemName.setBounds(147, 51, 268, 20);
        tabTransactionPanel.add(comboItemName);
        
        JPanel tabReportPanel = new JPanel();
        tabbedPane.addTab("Report", null, tabReportPanel, null);
        tabReportPanel.setLayout(null);
        
        JLabel lblReportDetails = new JLabel("Generate Report");
        lblReportDetails.setBounds(10, 11, 165, 22);
        lblReportDetails.setFont(new Font("Cambria", Font.BOLD, 18));
        tabReportPanel.add(lblReportDetails);
        
        JSeparator separator_1 = new JSeparator();
        separator_1.setBounds(10, 39, 499, 4);
        tabReportPanel.add(separator_1);
        
        JLabel lblNumberOfTransactions = new JLabel("Number of Transactions Found:");
        lblNumberOfTransactions.setBounds(10, 196, 165, 14);
        tabReportPanel.add(lblNumberOfTransactions);
        
        numberOfTransactionsValue = new JLabel("-");
        numberOfTransactionsValue.setBounds(185, 196, 46, 14);
        tabReportPanel.add(numberOfTransactionsValue);
        
        JLabel lblRevenue = new JLabel("Total Revenue:");
        lblRevenue.setBounds(10, 221, 150, 14);
        tabReportPanel.add(lblRevenue);
        
        lblRevenueValue = new JLabel("$0");
        lblRevenueValue.setBounds(185, 221, 113, 14);
        tabReportPanel.add(lblRevenueValue);
        
        JLabel lblSalesTax_1 = new JLabel("Total Sales Tax:");
        lblSalesTax_1.setBounds(10, 246, 150, 14);
        tabReportPanel.add(lblSalesTax_1);
        
        lblNetSalesTaxValue = new JLabel("$0");
        lblNetSalesTaxValue.setBounds(185, 246, 113, 14);
        tabReportPanel.add(lblNetSalesTaxValue);
        
        JLabel lblNetIncome = new JLabel("Net Income:");
        lblNetIncome.setBounds(10, 271, 150, 14);
        tabReportPanel.add(lblNetIncome);
        
        lblNetIncomeValue = new JLabel("$0");
        lblNetIncomeValue.setBounds(185, 271, 113, 14);
        tabReportPanel.add(lblNetIncomeValue);
        
        String[] strValues = new String[] {
        		"All",
        		"Last Hour",
        		"Last Day",
        		"Last Week",
        		"Last Month",
        		"Last Year"
        };
        final JComboBox comboReportByDate = new JComboBox(strValues);
        comboReportByDate.setBounds(156, 54, 327, 20);
        tabReportPanel.add(comboReportByDate);
        
        JLabel lblGenerateReport = new JLabel("Generate Report By Date:");
        lblGenerateReport.setBounds(10, 57, 165, 14);
        tabReportPanel.add(lblGenerateReport);
        
        JLabel lblGenerateReportBy = new JLabel("Generate Report By Value:");
        lblGenerateReportBy.setBounds(10, 87, 136, 14);
        tabReportPanel.add(lblGenerateReportBy);
        
        strValues = new String[] {
        		"All",
        		"< $100",
        		"$100 - $500",
        		"$500 - $1000",
        		"$1000+"
        };
        final JComboBox comboReportByValue = new JComboBox(strValues);
        comboReportByValue.setBounds(156, 84, 327, 20);
        tabReportPanel.add(comboReportByValue);
        
        JButton btnGenerate = new JButton("Generate");
        
        // Listener to generate a report when the button is clicked
        btnGenerate.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		String query = "SELECT * FROM " + dbManager.getDbName() + "." + dbManager.TABLE_TRANSACTIONS;
        				
        		String byDate = (String)comboReportByDate.getSelectedItem();
        		String byValue = (String)comboReportByValue.getSelectedItem();
        		
        		// Check if we need to use a where clause
        		if(!byDate.equals("All") || !byValue.equals("All"))
        		{
        			query += " WHERE";
        		}
        		
        		// Used to calculate the dates
        		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        		Date pastDate = new Date();
        		long DAY_IN_MS = 1000 * 60 * 60 * 24;
        		long HOUR_IN_MS = 1000 * 60 * 60;
        		
        		// Add the appropriate date where clause if necessary
        		switch(comboReportByDate.getSelectedIndex()) {
        			case 0:
        				// Do nothing, this is the all option
        				break;
        				
        			// Last Hour
        			case 1:        				
        				// Subtract one hour from the current time
        				pastDate = new Date(pastDate.getTime() - HOUR_IN_MS);
        				
        				query += " DATE >= '" + Timestamp.valueOf(dateFormat.format(pastDate)) + "'";
        				break;
        				
        			// Last Day
        			case 2:
        				pastDate = new Date(pastDate.getTime() - DAY_IN_MS);
        				
        				query += " DATE >= '" + Timestamp.valueOf(dateFormat.format(pastDate)) + "'";
        				break;
        				
        			// Last Week
        			case 3:
        				pastDate = new Date(pastDate.getTime() - (7 * DAY_IN_MS));
        				
        				query += " DATE >= '" + Timestamp.valueOf(dateFormat.format(pastDate)) + "'";
        				break;
        				
        			// Last Month
        			case 4:
        				pastDate = new Date(pastDate.getTime() - (30 * DAY_IN_MS));
        				
        				query += " DATE >= '" + Timestamp.valueOf(dateFormat.format(pastDate)) + "'";
        				break;
        				
        			// Last Year
        			case 5:
        				pastDate = new Date(pastDate.getTime() - (365 * DAY_IN_MS));
        				
        				query += " DATE >= '" + Timestamp.valueOf(dateFormat.format(pastDate)) + "'";
        				break;
        		}
        		
        		// Add the AND keyword if we have both a date & value to limit by
        		if(comboReportByDate.getSelectedIndex() != 0 && comboReportByValue.getSelectedIndex() != 0)
					query += " AND";
				
        		
        		switch(comboReportByValue.getSelectedIndex()) {
        			// All
        			case 0:
        				// Do nothing
        				break;
        			
        			// < $100
        			case 1:        				
        				query += " GRAND_TOTAL < 100.00";
        				break;
        				
        			// $100 - $500
        			case 2:
        				query += " GRAND_TOTAL >= 100.00 AND GRAND_TOTAL < 500.00";
        				break;
        				
        			// $500 - $1000
        			case 3:
        				query += " GRAND_TOTAL >= 500.00 AND GRAND_TOTAL < 1000.00";
        				break;
        				
        			// $1000+
        			case 4:
        				query += " GRAND_TOTAL >= 1000.00";
        				break;
        		}
        		
        		PreparedStatement ps;
        		try {
        			// Prepare and run the query
        			ps = dbManager.getConnection().prepareStatement(query);
        			ResultSet results = ps.executeQuery();
        			
        			// Setup the number counters
        			int numberOfTransactions = 0;
            		double netincome = 0;
        			totalRevenue = 0;
        			totalSalesTax = 0;
        			
        			// Loop through and add the numbers to our totals
        			while(results.next()) {
        				numberOfTransactions++;
        				
        				totalRevenue += results.getDouble("GRAND_TOTAL");
        				totalSalesTax += results.getDouble("SALES_TAX");
        			}
        			// Calculate net income
        			netincome = totalRevenue - totalSalesTax;
        		
        			// Update the report ui elements
        			numberOfTransactionsValue.setText(NumberFormat.getIntegerInstance().format(numberOfTransactions));
            		lblRevenueValue.setText(currencyFormat.format(totalRevenue));
            		lblNetSalesTaxValue.setText(currencyFormat.format(totalSalesTax));
            		lblNetIncomeValue.setText(currencyFormat.format(netincome));
            		lblReportGeneratedAt.setText("Generated At: " + new Date().toString());
            	
            	// Catch any SQL errors
        		} catch(SQLException e) {
        			JOptionPane.showMessageDialog(null, "An error occured generating the report:\n\r" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        			return;
        		}        		
        	}
        });
        btnGenerate.setBounds(156, 115, 89, 23);
        tabReportPanel.add(btnGenerate);
        
        JLabel lblReportDetails_1 = new JLabel("Report Details");
        lblReportDetails_1.setFont(new Font("Cambria", Font.BOLD, 18));
        lblReportDetails_1.setBounds(10, 149, 165, 22);
        tabReportPanel.add(lblReportDetails_1);
        
        JSeparator separator_3 = new JSeparator();
        separator_3.setBounds(10, 181, 499, 4);
        tabReportPanel.add(separator_3);
        
        lblReportGeneratedAt = new JLabel("");
        lblReportGeneratedAt.setHorizontalAlignment(SwingConstants.RIGHT);
        lblReportGeneratedAt.setFont(new Font("Calibri", Font.BOLD, 11));
        lblReportGeneratedAt.setBounds(223, 271, 286, 14);
        tabReportPanel.add(lblReportGeneratedAt);
        
        JPanel tabInventoryPanel = new JPanel();
        tabbedPane.addTab("Inventory", null, tabInventoryPanel, null);
        tabInventoryPanel.setLayout(null);
        
        JLabel lblInventory = new JLabel("Item Inventory");
        lblInventory.setFont(new Font("Cambria", Font.BOLD, 18));
        lblInventory.setBounds(10, 11, 174, 22);
        tabInventoryPanel.add(lblInventory);
        
        JSeparator separator_2 = new JSeparator();
        separator_2.setBounds(10, 39, 499, 4);
        tabInventoryPanel.add(separator_2);
        
        JLabel lblAddNewItem = new JLabel("New Item Name:");
        lblAddNewItem.setBounds(20, 54, 104, 14);
        tabInventoryPanel.add(lblAddNewItem);
        
        textAddItemName = new JTextField();
        textAddItemName.setToolTipText("Item Name\r\n");
        textAddItemName.setBounds(108, 51, 86, 20);
        tabInventoryPanel.add(textAddItemName);
        textAddItemName.setColumns(10);
        
        JLabel lblDefaultPrice = new JLabel("Default Price:");
        lblDefaultPrice.setBounds(208, 54, 86, 14);
        tabInventoryPanel.add(lblDefaultPrice);
        
        textAddItemPrice = new JTextField();
        textAddItemPrice.setBounds(280, 51, 86, 20);
        tabInventoryPanel.add(textAddItemPrice);
        textAddItemPrice.setColumns(10);
        
        JButton btnAddItemToInventory = new JButton("Add Item");
        
        // Listen for clicks on the add item to inventory button
        btnAddItemToInventory.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		try {
        			if(textAddItemName.getText().equals("") || textAddItemPrice.getText().equals(""))
        			{
        				JOptionPane.showMessageDialog(null, "You must enter an item name and default price.", "Error", JOptionPane.ERROR_MESSAGE);
        				return;
        			}
        			
        			double price;
        			try {
        				price = Double.parseDouble(textAddItemPrice.getText());
        			} catch(NumberFormatException e) {
        				JOptionPane.showMessageDialog(null, "You must enter a valid default price.", "Error", JOptionPane.ERROR_MESSAGE);
        				return;
        			}
        			
        			String query = "INSERT INTO " + dbManager.getDbName() + "." + dbManager.TABLE_INVENTORY +
        				" (NAME, DEFAULT_PRICE) " +
        				"VALUES('" + textAddItemName.getText() + "', " +
        				price + ")";
        			
        			try {
        				// Get a prepared statement, the 2nd argument allows us to get the id of the inserted row
						PreparedStatement ps = dbManager.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
						ps.executeUpdate();
						
						
						ResultSet rs = ps.getGeneratedKeys();
						
						while(rs.next()) {
							int id = rs.getInt(1);
							
							InventoryItem addedItem = new InventoryItem();
							addedItem.setID(id);
							addedItem.setName(textAddItemName.getText());
							addedItem.setPrice(price);
							addedItem.setActive(true);							
							
							// Set the active column
							tableInventory.getModel().setValueAt(addedItem, 0, 0);
						}
						
						ps.close();
						
						// Clear the fields
						textAddItemName.setText("");
						textAddItemPrice.setText("");
						
						// Refresh the combo box that holds the inventory items
						refreshItemComboBox();
						
					} catch (SQLException e) {
						JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
        		} catch (NumberFormatException e) {
        			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        		}
        	}
        });
        btnAddItemToInventory.setBounds(391, 50, 89, 23);
        tabInventoryPanel.add(btnAddItemToInventory);
        
        tableInventory = new JTable(new InventoryTableModel());
        tableInventory.setFillsViewportHeight(true);        
        tableInventory.setIntercellSpacing(new Dimension(4, 1));
        
        // Set the column widths to custom lengths
        tableInventory.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableInventory.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableInventory.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableInventory.getColumnModel().getColumn(2).setPreferredWidth(81);
        tableInventory.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        // Setup the display of the inventory table
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableInventory.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableInventory.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        // Listen for double clicks on the inventory table
 		// This will allow for editing of the inventory item
 		tableInventory.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				// Only double clicks
 				if(e.getClickCount() == 2) {
 					// Get the row clicked on
 					int row = tableInventory.rowAtPoint(e.getPoint());
 				
 					// Get the inventory item selected
 					InventoryTableModel model = (InventoryTableModel)tableInventory.getModel(); 					
 					InventoryItem item = model.getRow(row);
 					
 					displayEditInventoryTab(item);
 				}
 			}
 		});
        
        JScrollPane scrollPaneInventoryTable = new JScrollPane(tableInventory);
        scrollPaneInventoryTable.setBounds(20, 84, 478, 181);
        tabInventoryPanel.add(scrollPaneInventoryTable);
        
        JButton btnActivateSelected = new JButton("Activate Selected");
        
        // Listener to active items in the inventory
        btnActivateSelected.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		int[] rows = tableInventory.getSelectedRows();
        		
        		if(rows.length <= 0)
        		{
        			JOptionPane.showMessageDialog(null, "Please select the items to activate from above.", "Error", JOptionPane.ERROR_MESSAGE);
        			return;
        		}
        		
        		// Loop through the rows and attempt to update them
        		for(int row : rows) {
        			InventoryTableModel model = (InventoryTableModel)tableInventory.getModel();
        			InventoryItem item = model.getRow(row);
        			
        			// If the item is already active no need to update database, just continue to next row
        			if(item.isActive())
        				continue;
        			
        			String query = "UPDATE " + dbManager.getDbName() + "." + dbManager.TABLE_INVENTORY +
        					" SET ACTIVE = 1" +
        					" WHERE ITEM_ID = " + item.getID();
        			
        			PreparedStatement ps;
					try {
						ps = dbManager.getConnection().prepareStatement(query);
						ps.executeUpdate();
						ps.close();
					} catch (SQLException e) {
						JOptionPane.showMessageDialog(null, "Unable to activate \"" + item.getName() + "\"\n\r" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}        			
        		}
        		
        		// Refresh the ui displays
        		refreshInventoryTable();
        		refreshItemComboBox();
        	}
        });
        btnActivateSelected.setBounds(126, 276, 129, 23);
        tabInventoryPanel.add(btnActivateSelected);
        
        JButton btnDeactivateSelected = new JButton("Deactivate Selected");
        
        // Listener to deactive items in the inventory
        btnDeactivateSelected.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		int[] rows = tableInventory.getSelectedRows();
        		
        		if(rows.length <= 0)
        		{
        			JOptionPane.showMessageDialog(null, "Please select the items to deactivate from above.", "Error", JOptionPane.ERROR_MESSAGE);
        			return;
        		}
        		
        		// Loop through the rows and attempt to update them
        		for(int row : rows) {
        			InventoryTableModel model = (InventoryTableModel)tableInventory.getModel();
        			InventoryItem item = model.getRow(row);
        			
        			// If the item is already inactive no need to update database, just continue to next row
        			if(!item.isActive())
        				continue;
        			
        			String query = "UPDATE " + dbManager.getDbName() + "." + dbManager.TABLE_INVENTORY +
        					" SET ACTIVE = 0" +
        					" WHERE ITEM_ID = " + item.getID();
        			
        			PreparedStatement ps;
					try {
						ps = dbManager.getConnection().prepareStatement(query);
						ps.executeUpdate();
						ps.close();
					} catch (SQLException e) {
						JOptionPane.showMessageDialog(null, "Unable to deactivate \"" + item.getName() + "\"\n\r" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}        			
        		}
        		

        		// Update the ui displays
        		refreshInventoryTable();
        		refreshItemComboBox();
        	}
        });
        btnDeactivateSelected.setBounds(265, 276, 150, 23);
        tabInventoryPanel.add(btnDeactivateSelected);
        
        JLabel lblDoubleClickAn = new JLabel("Double click an item below to edit it");
        lblDoubleClickAn.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDoubleClickAn.setFont(new Font("Calibri", Font.BOLD, 11));
        lblDoubleClickAn.setBounds(223, 19, 286, 14);
        tabInventoryPanel.add(lblDoubleClickAn);
        
        
	}
	
	/*
	 * Takes a transaction as a parameter and creates a new tab on the UI with the receipt details
	 */
	public void displayEditInventoryTab(InventoryItem item) {		
		// Add a new tab
        JPanel editItemPanel = new JPanel();
        tabbedPane.addTab("Edit Item", null, editItemPanel, null);
        
        // Make sure this new tab is selected
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        editItemPanel.setLayout(null);
        
        // Add all the UI components to the tab
        JLabel lblInventoryItem = new JLabel("Edit Inventory Item");
        lblInventoryItem.setBounds(10, 11, 265, 22);
        lblInventoryItem.setFont(new Font("Cambria", Font.BOLD, 18));
        editItemPanel.add(lblInventoryItem);
        
        JSeparator separator1 = new JSeparator();
        separator1.setBounds(10, 39, 495, 4);
        editItemPanel.add(separator1);        
		
		JLabel lblItemID = new JLabel("ID: ");
		lblItemID.setBounds(10, 69, 86, 14);
		editItemPanel.add(lblItemID);
		
		final JTextField textItemID = new JTextField(Integer.toString(item.getID()));
		textItemID.setBounds(100, 67, 86, 20);
		textItemID.setColumns(10);
		textItemID.setEnabled(false);
		editItemPanel.add(textItemID);
		
		JLabel lblItemName = new JLabel("Name:");
		lblItemName.setBounds(10, 94, 86, 14);
		editItemPanel.add(lblItemName);
		
		final JTextField textItemName = new JTextField(item.getName());
		textItemName.setBounds(100, 92, 86, 20);
		textItemName.setColumns(10);
		editItemPanel.add(textItemName);
		
		JLabel lblItemDefaultPrice = new JLabel("Default Price:");
		lblItemDefaultPrice.setBounds(10, 119, 86, 14);
		editItemPanel.add(lblItemDefaultPrice);
		
		final JTextField textItemPrice = new JTextField(Double.toString(item.getPrice()));
		textItemPrice.setBounds(100, 117, 86, 20);
		textItemPrice.setColumns(10);
		editItemPanel.add(textItemPrice);
		
		JLabel lblIsActive = new JLabel("Active?");
		lblIsActive.setBounds(10, 144, 103, 14);
		editItemPanel.add(lblIsActive);
		
		final JCheckBox checkItemActive = new JCheckBox();
		checkItemActive.setBounds(100, 140, 20, 20);
		checkItemActive.setSelected(item.isActive());
		editItemPanel.add(checkItemActive);
		
		// Add a "save" button that saves the selected tab without closing
		JButton btnSaveChanges = new JButton("Save Changes");
		btnSaveChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {				
				if(textItemName.getText().equals("") || textItemPrice.getText().equals(""))
    			{
    				JOptionPane.showMessageDialog(null, "You must enter an item name and default price.", "Error", JOptionPane.ERROR_MESSAGE);
    				return;
    			}				
				
				int id = Integer.parseInt(textItemID.getText());
				int activeValue = checkItemActive.isSelected() ? 1 : 0;
				double price;
				try {
					price = Double.parseDouble(textItemPrice.getText());
				} catch(NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Please enter a valid price. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}			
				
				String query = "UPDATE " + dbManager.getDbName() + "." + dbManager.TABLE_INVENTORY +
						" SET NAME = '" + textItemName.getText() + "', " +
						"DEFAULT_PRICE = " + price + ", " +
						"ACTIVE = " + activeValue + " " +
						"WHERE ITEM_ID = " + id;
				
				PreparedStatement ps;
				try {
					ps = dbManager.getConnection().prepareStatement(query);
					ps.executeUpdate();
					ps.close();
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "Unable to update the inventory item.\n\r" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);					
				}
				
				refreshInventoryTable();
				refreshItemComboBox();
				
				// Close the tab
				tabbedPane.remove(tabbedPane.getSelectedIndex());
				tabbedPane.setSelectedIndex(2);
			}
		});
		btnSaveChanges.setBounds(10, 276, 155, 23);
		editItemPanel.add(btnSaveChanges);
		
		// Add a "close" button that closes the selected tab without saving
		JButton btnCloseNoSaving = new JButton("Close");
		btnCloseNoSaving.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				tabbedPane.remove(tabbedPane.getSelectedIndex());
				tabbedPane.setSelectedIndex(2);
			}
		});
		btnCloseNoSaving.setBounds(175, 276, 155, 23);
		editItemPanel.add(btnCloseNoSaving);
       
	}
	
	private void refreshTransactionsTable() {
		// Load the transactions from the database into the recent transactions display
		try {
			// reset the table
			TransactionsTableModel model = (TransactionsTableModel)tableTransactions.getModel();
			model.reset();
			
			// Our query, select transactions ordered by date
			String strPs = "SELECT * FROM " + dbManager.getDbName() + "." + dbManager.TABLE_TRANSACTIONS +
					" ORDER BY DATE ASC";
			
			// Use a prepared statement to execute on the database
			PreparedStatement ps = dbManager.getConnection().prepareStatement(strPs);
			ResultSet result = ps.executeQuery();
			
			// Loop through the results
			while(result.next()) {
				// Create a new transaction to store the information in
				Transaction t = new Transaction();
				
				// Set the data
				t.setDate(result.getTimestamp("DATE"));
				t.setSubTotal(result.getDouble("SUBTOTAL"));
				t.setSalesTax(result.getDouble("SALES_TAX"));
				t.setGrandTotal(result.getDouble("GRAND_TOTAL"));
				
				// Read in the items
				Clob items = result.getClob("ITEMS");
				
				// Read the items from the clob into a string
				String itemStr = items.getSubString(1, (int) items.length());
				
				// Loop through the items and add them to the transaction
				String[] segments = itemStr.split(",");
				String item;
				for(int i = 0; i < segments.length; i++){
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
			}
			
			// Close the statement
			ps.close();
			
		}
		catch(SQLException ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	private void refreshInventoryTable() {
		// Load the item inventory into the table
		try {
			// reset the table
			InventoryTableModel model = (InventoryTableModel)tableInventory.getModel();
			model.reset();
			
			// Setup the query
			String query = "SELECT * FROM " + dbManager.getDbName() + "." + dbManager.TABLE_INVENTORY;
			
			// Execute it
			PreparedStatement ps = dbManager.getConnection().prepareStatement(query);
			ResultSet results = ps.executeQuery();
			
			// Loop through each row from the query results
			while(results.next()) {
				InventoryItem item = new InventoryItem();
				
				item.setID(results.getInt("ITEM_ID"));
				item.setName(results.getString("NAME"));
				item.setPrice(results.getDouble("DEFAULT_PRICE"));
				item.setActive(results.getBoolean("ACTIVE"));				
				
				tableInventory.getModel().setValueAt(item, 0, 0);				
			}
					
			results.close();
			ps.close();
			
		} catch(SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void refreshItemComboBox() {
		// Load the item inventory into the table
		try {
			// Setup the query
			String query = "SELECT (NAME) FROM " + dbManager.getDbName() + "." + dbManager.TABLE_INVENTORY +
					" WHERE ACTIVE = 1" +
					" ORDER BY NAME ASC";
			
			// Execute it
			PreparedStatement ps = dbManager.getConnection().prepareStatement(query);
			ResultSet results = ps.executeQuery();
			
			// Setup an array list to hold the names
			ArrayList<String> itemNames = new ArrayList<String>();
			
			// Loop through each row from the query results
			while(results.next()) {
				itemNames.add(results.getString("NAME"));
			}
					
			// Remove all the previous items in the combobox
			comboItemName.removeAllItems();

			// Check if we are actually adding any items now
			if(itemNames.size() > 0)
			{			
				// If so make sure the combobox is enabled
				comboItemName.setEnabled(true);
				btnAddItem.setEnabled(true);
				
				// And add the items
				for(String str : itemNames) {
				   comboItemName.addItem(str);
				}
			// Else there are no items to add
			} else {
				// Display a message in the combo box asking for an item to be added to inventory
				comboItemName.addItem("Please add at least 1 active item to the inventory.");
				
				// Disable the combobox
				comboItemName.setEnabled(false);
				btnAddItem.setEnabled(false);
			}
			
			results.close();
			ps.close();
			
		} catch(SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
