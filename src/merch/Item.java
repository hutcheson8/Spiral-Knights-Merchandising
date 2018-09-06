package merch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Item implements Serializable {
	private static final long serialVersionUID = 402088475468107547L;
	private ArrayList<Record> records = new ArrayList<Record>();
	private final static String POSINT = "Please enter a positive integer.";
	private final static Form FORM = new Form(
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
			}, new FormLine("Starting Listings", POSINT) {
				private static final long serialVersionUID = -9110165369158106907L;

				@Override
				protected boolean verify() {
					try {
						return Integer.parseInt(getInput()) > 0;
					} catch (Exception e) {
						return false;
					}
				}
			} });
	private final String name;
	private final int energyPerSDPurchase, quantityPerSDPurchase, quantityPerListing, starLevel, startingListings;

	public final int getRequiredCrownReserves() {
		return getCurrentListings() * mostRecentRecord().getListingPrice();
	}

	public final int getRequiredEnergyReserves() {
		return getCurrentListings() * quantityPerListing * energyPerSDPurchase / quantityPerSDPurchase;
	}

	public final int getNumToSell() {
		return mostRecentRecord().getNumToSell();
	}

	public final boolean isCurrentlyStocked() {
		return mostRecentRecord().isStocked();
	}

	public final int getCurrentPrice() {
		if (mostRecentRecord() != null)
			return mostRecentRecord().getPrice();
		return 0;
	}

	public final float getCurrentCostPlusPercent() {
		if (mostRecentRecord() != null)
			return mostRecentRecord().getCostPlusPercent();
		return 0;
	}

	public final float getCurrentUndercutMargin() {
		if (mostRecentRecord() != null)
			return mostRecentRecord().getUndercutMargin();
		return 0;
	}

	public final int getCurrentListings() {
		if (mostRecentRecord() != null)
			return mostRecentRecord().getNumListings();
		return 0;
	}

	public final int getNetProfitToDate() {
		int cumulate = 0;
		for (Record r : records) {
			cumulate += r.getNetProfit();
		}
		return cumulate;
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
		return (int) (basePrice * quantityPerListing * 2 + .5);
	}

	public final int getSDCRCostPerListing(float energyPrice) {
		return (int) (energyPerSDPurchase * energyPrice * quantityPerListing / quantityPerSDPurchase);
	}

	public final int getQuantityPerListing() {
		return quantityPerListing;
	}

	public final int getStartingListings() {
		return startingListings;
	}

	private final Record mostRecentRecord() {
		if (records.size() == 0)
			return null;
		return records.get(records.size() - 1);
	}

	public final void addRecord(int expiredItems, int leftovers, int aHPrice, Float energyPrice, Date timeStamp)
			throws Exception {
		records.add(new Record(expiredItems, leftovers, aHPrice, energyPrice, timeStamp, mostRecentRecord(), this));
	}

	public final String getName() {
		return name;
	}

	public Item() throws CancelException {
		String[] result = FORM.result();
		name = result[0];
		energyPerSDPurchase = Integer.parseInt(result[1]);
		quantityPerSDPurchase = Integer.parseInt(result[2]);
		quantityPerListing = Integer.parseInt(result[3]);
		starLevel = Integer.parseInt(result[4]);
		startingListings = Integer.parseInt(result[5]);
	}
}