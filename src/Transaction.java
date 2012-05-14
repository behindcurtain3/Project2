//////////////////////////////////////////////////////////////////////
// Transaction.java			Author: Justin Brown
//
// Holds data for a transaction, either past or present. Data
// includes the list of items purchased, the sub total of the purchase
// the sales tax, the grand total and the date the transaction took
// place.
//////////////////////////////////////////////////////////////////////

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Transaction {
	// Used to calculate sales tax
	private final double SALES_TAX = 0.08;
	
	// Formatter for saving the date to the database
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// Variables
	private ArrayList<Item> items;
	private double subTotal;
	private double salesTax;
	private double grandTotal;
	private Date date;
	
	// Constructor
	public Transaction() {
		
		// Setup a blank arraylist for the items
		items = new ArrayList<Item>();
		
		// Set all the values to zero
		subTotal = 0;
		salesTax = 0;
		grandTotal = 0;
		
		// Set the date to now
		date = new Date();
	}

	// Add an item to the transaction
	public void addItem(Item i) { 
		items.add(i);
	}
	
	// Getter for the items
	public ArrayList<Item> getItems() {
		return items;
	}
	
	/*
	 * Calculate the sub total of all the items
	 */
	public void calculateSubTotal() {
		double runningTotal = 0;
		
		// Loop through the items
		for(Iterator<Item> i = items.iterator(); i.hasNext();) {
			
			// Add the item sub total to the running total
			runningTotal += i.next().getSubTotal();
		}
		
		// Override the previous value we stored with the new total
		subTotal = runningTotal;
	}
	
	/*
	 * Calculate the sales tax
	 */
	public void calculateSalesTax() {
		// Sales tax is equal to sub total multiplied by the tax rate
		salesTax = subTotal * SALES_TAX;
	}
	
	/*
	 * Calculate the grand total
	 */
	public void calculateGrandTotal() { 
		grandTotal = subTotal + salesTax;
	}	

	// Getter for the sub total
	public double getSubTotal() {
		return subTotal;
	}
	
	// Getter for the grand total
	public double getGrandTotal() {
		return grandTotal;
	}

	// Getter for the sales tax
	public double getSalesTax() {
		return salesTax;
	}	
	
	// Getter for the date
	public String getDate() {
		return DateFormat.getInstance().format(date);
	}	
	
	// Return a formatted date string
	public String getDateFormatted() {
		return dateFormat.format(date);
	}
	
	// Setter for the sub total
	public void setSubTotal(double value) {
		subTotal = value;
	}
	
	// Setter for the sales tax
	public void setSalesTax(double value) {
		salesTax = value;
	}
	
	// Setter for the grand total
	public void setGrandTotal(double value) {
		grandTotal = value;
	}
	
	// Setter for the date
	public void setDate(Date d) {
		date = d;
	}
	
	/*
	 * Processes each of the three calculations needed to get the correct totals
	 * for the transaction based on its current items list.
	 */
	public void calculate() {
		calculateSubTotal();
		calculateSalesTax();
		calculateGrandTotal();
	}
	
}
