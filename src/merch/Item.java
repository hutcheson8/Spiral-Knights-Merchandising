package merch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Item implements Serializable {
	private final static String POSINT = "Please enter a positive integer.";
	private static final Form FORM = new Form(
			new FormLine[] { new FormLine("Name", "Please enter a name for the item.") {
				private static final long serialVersionUID = 3720008655250892788L;

				@Override
				protected boolean verify() {
					return getInput().length() > 0;
				}
			}, new FormLine("Energy Price", POSINT) {
				private static final long serialVersionUID = 7839955311053728024L;

				@Override
				protected boolean verify() {
					try {
						return Integer.parseInt(getInput()) > 0;
					} catch (Exception e) {
						return false;
					}
				}
			}, new FormLine("Buy Quantity", POSINT) {
				private static final long serialVersionUID = -2739686442653137487L;

				@Override
				protected boolean verify() {
					try {
						return Integer.parseInt(getInput()) > 0;
					} catch (Exception e) {
						return false;
					}
				}
			}, new FormLine("Listing Quantity", POSINT) {
				private static final long serialVersionUID = -7950977457271310608L;

				@Override
				protected boolean verify() {
					try {
						return Integer.parseInt(getInput()) > 0;
					} catch (Exception e) {
						return false;
					}
				}
			}, new FormLine("Star Level", "Please enter a number between 0 and 5") {
				private static final long serialVersionUID = -2538422211743533484L;

				@Override
				protected boolean verify() {
					try {
						int input = Integer.parseInt(getInput());
						return 0 <= input && input <= 5;
					} catch (Exception e) {
						return false;
					}
				}
			} });
	private static final long serialVersionUID = 402088475468107547L;
	private final int energyPerSDPurchase, quantityPerSDPurchase, quantityPerListing, starLevel;
	private final String name;
	private ArrayList<Record> records = new ArrayList<Record>();

	public Item() throws CancelException {
		String[] result = FORM.result();
		name = result[0];
		energyPerSDPurchase = Integer.parseInt(result[1]);
		quantityPerSDPurchase = Integer.parseInt(result[2]);
		quantityPerListing = Integer.parseInt(result[3]);
		starLevel = Integer.parseInt(result[4]);
	}

	public final void addRecord(int expiredItems, int leftovers, int aHPrice, Float energyPrice, Date timeStamp) {
		records.add(new Record(expiredItems, leftovers, aHPrice, energyPrice, timeStamp, mostRecentRecord(), this));
	}

	public final float getCurrentCostPlusPercent() {
		Record recent = mostRecentRecord();
		return recent == null ? 0 : recent.getCostPlusPercent();
	}

	public final int getCurrentListings() {
		Record recent = mostRecentRecord();
		return recent == null ? 0 : recent.getNumListings();
	}

	public final int getCurrentPrice() {
		Record recent = mostRecentRecord();
		return recent == null ? 0 : recent.getPrice();
	}

	public final float getCurrentUndercutMargin() {
		Record recent = mostRecentRecord();
		return recent == null ? 0 : recent.getUndercutMargin();
	}

	public int getMostRecentAHPrice(float energyPrice) {
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
			return mostRecentRecord().getNetProfit();
		int cumulate = 0;
		for (Record r : records)
			if (r.getTimestamp().after(date))
				cumulate += r.getNetProfit();
		return cumulate;
	}

	public final int getNumToSell() {
		return mostRecentRecord().getNumToSell();
	}

	public final int getQuantityPerListing() {
		return quantityPerListing;
	}

	public final int getRequiredCrownReserves() {
		return (getCurrentListings() + mostRecentRecord().getNumListingsNotStocked())
				* mostRecentRecord().getListingPrice();
	}

	public final int getRequiredEnergyReserves() {
		return (getCurrentListings() + mostRecentRecord().getNumListingsNotStocked()) * quantityPerListing
				* energyPerSDPurchase / quantityPerSDPurchase;
	}

	public final int getSDCRCostPerListing(float energyPrice) {
		return (int) (energyPerSDPurchase * energyPrice * quantityPerListing / quantityPerSDPurchase);
	}

	public final int getStarLevelBasedListingPrice() {
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
		return (int) (basePrice * quantityPerListing * 2 + .5f);
	}

	public final boolean isCurrentlyStocked() {
		return mostRecentRecord().isStocked();
	}

	private final Record mostRecentRecord() {
		if (records.size() == 0)
			return null;
		return records.get(records.size() - 1);
	}

	public final Date lastUpdate() {
		return mostRecentRecord().getTimestamp();
	}
}