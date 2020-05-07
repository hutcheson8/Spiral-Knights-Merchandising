package merch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Predicate;

public class Item implements Serializable {
	private final static String POSINT = "Please enter a positive integer.";
	private final static Predicate<String> POSINTPRD = (input) -> {
		try {
			return Integer.parseInt(input) > 0;
		} catch (Exception e) {
			return false;
		}
	};
	private static final Form FORM = new Form(
			new FormLine[] { new FormLine("Name", "Please enter a name for the item.", (input) -> {
				return input.length() > 0;
			}), new FormLine("Energy Price", POSINT, POSINTPRD), new FormLine("Items Per SDPurchase", POSINT, POSINTPRD),
					new FormLine("Items Per Listing", POSINT, POSINTPRD),
					new FormLine("Star Level", "Please enter a number between 0 and 5. (inclusive)", (input) -> {
						try {
							int numInput = Integer.parseInt(input);
							return 0 <= numInput && numInput <= 5;
						} catch (Exception e) {
							return false;
						}
					}),
					new FormLine("Starting Listings", POSINT, POSINTPRD)});
	private static final long serialVersionUID = 402088475468107547L;
	private final int energyPerSDPurchase, numItemsPerSDPurchase, numItemsPerListing, starLevel, numStartingListings;

	public void setNumListingsToSell(int numListingsToSell){
		mostRecentRecord().setNumListingsToSell(numListingsToSell);
	}

	public int getEnergyPerSDPurchase(){ return energyPerSDPurchase; }

	public int getNumItemsPerSDPurchase(){ return numItemsPerSDPurchase; }

	private final String name;
	private ArrayList<Record> records = new ArrayList<Record>();

	public Item() throws CancelException {
		String[] result = FORM.result();
		name = result[0];
		energyPerSDPurchase = Integer.parseInt(result[1]);
		numItemsPerSDPurchase = Integer.parseInt(result[2]);
		numItemsPerListing = Integer.parseInt(result[3]);
		starLevel = Integer.parseInt(result[4]);
		numStartingListings = Integer.parseInt(result[5]);
	}

	public final void addRecord(int expiredItems, int numActiveListings, int aHPrice, Float energyPrice, Date timeStamp){
		records.add(
				new Record(expiredItems, numActiveListings, aHPrice, energyPrice, timeStamp, mostRecentRecord(), this));
	}

	public final float getCostPlusPercent() {
		Record recent = mostRecentRecord();
		return recent == null ? 0 : recent.getCostPlusPercent();
	}

	public final int getMaxListings() {
		Record recent = mostRecentRecord();
		return recent == null ? 0 : recent.getMaxListings();
	}

	public final int getPrice() {
		Record recent = mostRecentRecord();
		return recent == null ? 0 : recent.getPrice();
	}

	public final float getUndercutMargin() {
		Record recent = mostRecentRecord();
		return recent == null ? 0 : recent.getUndercutMargin();
	}

	public int getAHPrice(float energyPrice) {
		Record recent = mostRecentRecord();
		return recent == null ? getSDCRCostPerListing(energyPrice) * 2 : recent.getAHPrice();
	}

	public final String getName() {
		return name;
	}

	public final int getNetProfitToDate() {
		int cumulate = 0;
		for (Record r : records)
			cumulate += r.getNetProfit();
		return cumulate;
	}

	public final int getNetProfitSince(Date date) {
		if (date == null)
			return mostRecentRecord() == null ? 0 : mostRecentRecord().getNetProfit();
		int cumulate = 0;
		for (Record r : records)
			if (r.getTimestamp().after(date))
				cumulate += r.getNetProfit();
		return cumulate;
	}

	public final int getNumListingsToSell() {
		return mostRecentRecord().getNumListingsToSell();
	}

	public final int getNumItemsPerListing() {
		return numItemsPerListing;
	}

	public final int getRequiredCrownReserves() {
		return (getMaxListings() + mostRecentRecord().getNumListingsNotStocked())
				* mostRecentRecord().getListingPrice();
	}

	public final int getRequiredEnergyReserves() {
		return (getMaxListings() + mostRecentRecord().getNumListingsNotStocked()) * numItemsPerListing
				* energyPerSDPurchase / numItemsPerSDPurchase;
	}

	public final int getSDCRCostPerListing(float energyPrice) {
		return (int) (energyPerSDPurchase * energyPrice * numItemsPerListing / numItemsPerSDPurchase);
	}

	public final int getStarLevelBasedListingFee() {
		float basePrice = 0;
		switch (starLevel) {
		case 0:
			basePrice = 5;
			break;
		case 1:
			basePrice = 12.5f;
			break;
		case 2:
			basePrice = 25;
			break;
		case 3:
			basePrice = 50;
			break;
		case 4:
			basePrice = 100;
			break;
		case 5:
			basePrice = 250;
			break;
		}
		return (int) (basePrice * numItemsPerListing * 2 + .5f);
	}

	public final int getNumStartingListings() {
		return numStartingListings;
	}

	public final boolean isStocked() {
		return mostRecentRecord().isStocked();
	}

	private final Record mostRecentRecord() {
		if (records.isEmpty())
			return null;
		return records.get(records.size() - 1);
	}

	public final Date lastUpdate() {
		return mostRecentRecord().getTimestamp();
	}

	public int getCost(){
		Record recent = mostRecentRecord();
		return recent == null ? 0 : recent.getCost();
	}

	public int getProfitPerListing(){ return mostRecentRecord().getProfitPerListing(); }
}