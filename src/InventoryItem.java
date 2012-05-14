//////////////////////////////////////////////////////////////////////
// InventoryItem.java			Author: Justin Brown
//
// Holds data for an inventory item that is stored in the database
// and used to allow quicker processing of items on transactions.
//////////////////////////////////////////////////////////////////////

public class InventoryItem {
	
	// Variables
	private int id;
	private String name;
	private double defaultPrice;
	private Boolean isActive;
	
	// Constructor
	public InventoryItem() {
		id = 0;
		name = "";
		defaultPrice = 0;
		isActive = true;
	}
	
	// Getters and setters
	public int getID() {
		return id;
	}
	
	public void setID(int value) {
		id = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String value) {
		name = value;
	}
	
	public double getPrice() {
		return defaultPrice;
	}
	
	public void setPrice(double value) {
		defaultPrice = value;
	}
	
	public Boolean isActive() {
		return isActive;
	}
	
	public void setActive(Boolean value) {
		isActive = value;
	}

}
