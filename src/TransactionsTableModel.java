//////////////////////////////////////////////////////////////////////
// TransactionTableModel.java			Author: Justin Brown
//
// Extends the AbstractTableModel class and provides a customized
// table model for use in the UI.
//////////////////////////////////////////////////////////////////////

import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;


public class TransactionsTableModel extends AbstractTableModel {

	// Hold our currency formatter
	private NumberFormat currencyFormat;
	
	// Our table format and data
	private String[] columnNames;
    private ArrayList<Transaction> data;
    
    public TransactionsTableModel() {
    	// Setup the currency format
    	currencyFormat = NumberFormat.getCurrencyInstance();
    	
    	// Setup the column names used in this table model
    	columnNames = new String[]{"Date", "Subtotal", "Sales Tax", "Grand Total"};
    	
    	// Blank arraylist to store the list of transactions
    	data = new ArrayList<Transaction>();
    }
	
    /*
     * Returns the number of columns based on the columnNames array length
     */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	/*
	 * Returns the number of rows based on the data arraylist size
	 */
	@Override
	public int getRowCount() {
		return data.size();
	}
	
	/*
	 * Returns a specified column name
	 */	
	@Override
	public String getColumnName(int col){
		return columnNames[col];
	}

	/*
	 * Get the value at a specific spot in the table
	 */
	@Override
	public Object getValueAt(int row, int col) {
		// Invert the row so the last transactions shows first
		// Do this because we want this table to display the most recent first
		row = data.size() - 1 - row;
		
		switch(col){
			// Column 1 return the sub total
			case 1:
				return currencyFormat.format(data.get(row).getSubTotal());
				
			// Column 2 return the sales tax
			case 2:
				return currencyFormat.format(data.get(row).getSalesTax());
				
			// Column 3 return the grand total
			case 3:
				return currencyFormat.format(data.get(row).getGrandTotal());
				
			// Column 0 or default return the date
			default:
				return data.get(row).getDate();
		}
		
	}
	
	/*
	 * Used to add a new row to the table 
	 */
	public void setValueAt(Object value, int row, int col) {
		
		// Add the new item to our arraylist
		data.add((Transaction) value);
		
		// Make sure the table display updates
        fireTableRowsInserted(row, row);
    }
	
	public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
	
	/*
	 * Clear the table
	 */
	public void reset() {
		// Clear the arraylist
		data.clear();
		
		// Make sure the table display updates
		fireTableDataChanged();
	}

	/*
	 * Returns the transaction at a given row
	 */
	public Transaction getRow(int row){
		// Again use the inversion so we return the correct transaction
		return data.get(data.size() - 1- row);
	}
	
	// Getter for the data
	public ArrayList<Transaction> getData() {
		return data;
	}
	
}
