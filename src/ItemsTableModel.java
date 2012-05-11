//////////////////////////////////////////////////////////////////////
// ItemTableModel.java			Author: Justin Brown
//
// Extends the AbstractTableModel class and provides a customized
// table model for use in the UI.
//////////////////////////////////////////////////////////////////////

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;


public class ItemsTableModel extends AbstractTableModel {
	// Hold our currency formatter
	private NumberFormat currencyFormat;
	
	// Our table format and data
	private String[] columnNames;
    private List<Item> data;
    
    // Constructor
    public ItemsTableModel() {
    	// Seutp the currency format
    	currencyFormat = NumberFormat.getCurrencyInstance();
    	
    	// Set the column names for the table
    	columnNames = new String[]{"Name", "Quantity", "Price", "Item Total"};
    	
    	// Setup a new blank array list that holds the table data
    	data = new ArrayList<Item>();
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
		switch(col){
			// Column 1 is the quantity
			case 1:
				return data.get(row).getQuantity();
				
			// Column 2 is the price
			case 2:
				return currencyFormat.format(data.get(row).getPrice());
				
			// Column 3 is the sub total
			case 3:
				return currencyFormat.format(data.get(row).getSubTotal());
				
			// Default is used for case 0 and anything else, return the name
			default:
				return data.get(row).getName();
		}
		
	}
	
	/*
	 * Used to add a new row to the table 
	 */
	public void setValueAt(Object value, int row, int col) {
        
		// Add the new item to our arraylist
		data.add((Item) value);
		
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

}
