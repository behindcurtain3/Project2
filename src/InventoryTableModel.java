//////////////////////////////////////////////////////////////////////
// InventoryTableModel.java			Author: Justin Brown
//
// Extends the AbstractTableModel class and provides a customized
// table model for use in the UI.
//////////////////////////////////////////////////////////////////////

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;


public class InventoryTableModel extends AbstractTableModel {
	// Hold our currency formatter
	private NumberFormat currencyFormat;
	
	// Our table format and data
	private String[] columnNames;
	
	// Stores the item data
    private List<InventoryItem> data;    
    
    // Constructor
    public InventoryTableModel() {
    	// Seutp the currency format
    	currencyFormat = NumberFormat.getCurrencyInstance();
    	
    	// Set the column names for the table
    	columnNames = new String[]{ "ID", "Name", "Default Price", "Active?" };
    	
    	// Setup a new blank array list that holds the table data
    	data = new ArrayList<InventoryItem>();
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
			// Column 1 is the name
			case 1:
				return data.get(row).getName();
				
			// Column 2 is the default price
			case 2:
				return currencyFormat.format(data.get(row).getPrice());
				
			// Column 3 isActive?
			case 3:
				return data.get(row).isActive();
				
			// Default is used for case 0 and anything else, return the ID
			default:
				return data.get(row).getID();
		}
	}
	
	/*
	 * Used to add a new row to the table 
	 */
	public void setValueAt(Object value, int row, int col) {
		InventoryItem item = (InventoryItem)value;
		
		data.add(item);
		
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
	
	public InventoryItem getRow(int row) {
		return data.get(row);
	}

}
