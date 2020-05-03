package merch;

import java.util.Date;

public final class RecordParams {// KNIVES Consider renaming this class.
	private final int expiredItems, leftoverItems, numActiveListings, aHPrice;
	private final Item item;
	private int numListingsToSell;

	public RecordParams(Item item, int expiredItems, int leftoverItems, int numActiveListings, int aHPrice)
			throws CancelException{
		this.item = item;
		this.expiredItems = expiredItems;
		this.leftoverItems = leftoverItems;
		this.numActiveListings = numActiveListings;
		this.aHPrice = aHPrice;
		numListingsToSell = 0;
	}

	public void addRecord(Float energyPrice, Date timeStamp){
		getItem().addRecord(getExpiredItems(), getNumActiveListings(), getAHPrice(), energyPrice, timeStamp);
	}

	public int getAHPrice(){ return aHPrice; }

	public int getEnergyPerSDPurchase(){ return item.getEnergyPerSDPurchase(); }

	public int getExpiredItems(){ return expiredItems; }

	public Item getItem(){ return item; }

	public int getLeftoverItems(){ return leftoverItems; }

	public int getNumActiveListings(){ return numActiveListings; }

	public int getProfitPerEnergySpent(boolean lastSDPurchase){
		return item.getProfitPerEnergySpent(lastSDPurchase);
	}

	public int getProfittingFromItemQuantity(boolean lastSDPurchase){
		return item.getProfittingFromItemQuantity(lastSDPurchase);
	}

	public int getQuantityPerListing(){ return item.getQuantityPerListing(); }

	public int getQuantityPerSDPurchase(){ return item.getQuantityPerSDPurchase(); }

	public void propogateNumListingsToSell(){
		item.setNumListingsToSell(numListingsToSell);
	}

	public void setNumListingsToSell(int numListingsToSell){ this.numListingsToSell = numListingsToSell; }
}
