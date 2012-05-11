//////////////////////////////////////////////////////////////////////
// Item.java			Author: Justin Brown
//
// Holds data for an item that appears on a transaction. Data
// includes the item name, its price and the quantity of the item
// purchased. Provides a convenient sub total method to quickly get
// the sub total for the item.
//////////////////////////////////////////////////////////////////////

public class Item {
	
	// The variables
	private String name;
	private double price;
	private int quantity;
	
	// Constructor
	public Item() {
		name ="";
		price = 0;
		quantity = 0;
	}
	
	// Setter for the item name
	public void setName(String n) {
		name = n;
	}
	
	// Setter for the item price
	public void setPrice(double p) {
		price = p;
	}
	
	// Setter for the item quantity
	public void setQuantity(int q) {
		quantity = q;
	}
	
	// Getter for the item name
	public String getName() {
		return name;
	}
	
	// Getter for the item price
	public double getPrice() {
		return price;
	}
	
	// Getter for the item quantity
	public int getQuantity() {
		return quantity;
	}
		
	/*
	 * Returns the sub total of the item as a double
	 */
	public double getSubTotal() {
		return price * quantity;
	}
	
	
}
