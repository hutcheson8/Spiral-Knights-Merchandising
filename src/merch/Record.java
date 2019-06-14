package merch;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class Record implements Serializable {
	private static final long serialVersionUID = 2941864307935615486L;
	private final int lotsSold, lotsExpired, profit, loss, netProfit, numListings, price, cost, finalListings,
			numToSell, aHPrice, listingPrice;
	private final Record previous;
	private final boolean stocked, usingCostPlus;
	private final Date timestamp;
	private int undercut;
	private final float undercutMargin, costPlusPercent;

	public Record(int expiredItems, int leftovers, int aHPrice, Float energyPrice, Date timestamp, Record previous,
			Item recordItem) {
		this.previous = previous;
		this.timestamp = timestamp;
		this.aHPrice = aHPrice;
		lotsExpired = expiredItems / recordItem.getQuantityPerListing();
		cost = recordItem.getSDCRCostPerListing(energyPrice);
		undercut = 0;
		Record notYetExpired = getNotYetExpired();
		if (previous == null) {
			lotsSold = 0;
			profit = 0;
			loss = 0;
			numListings = 5;// Reasonable default value
			costPlusPercent = 1f;
			undercutMargin = .75f;
		} else {
			lotsSold = previous.getFinalListings() - lotsExpired - leftovers;
			profit = lotsSold * (notYetExpired.getPrice() * 9 / 10 - cost);
			Record justExpired = getJustExpired();
			int expiredListingPrice = 0;
			if (justExpired != null) {
				expiredListingPrice = justExpired.getListingPrice();
			}
			loss = lotsExpired * expiredListingPrice;
			int preNumListings = previous.getNumListings();
			if (lotsExpired > 0) {// Expired
				preNumListings -= lotsExpired;
				if (preNumListings == 0)
					preNumListings = 1;
				if (justExpired != null && justExpired.getUndercut() != 0) {
					float[] expiredCasePrice = justExpired.getExpiredCasePrice();
					undercutMargin = max(min(expiredCasePrice[1], previous.getUndercutMargin()), .05f);
					costPlusPercent = max(min(expiredCasePrice[0], previous.getCostPlusPercent()), .05f);
				} else {
					undercutMargin = previous.getUndercutMargin();
					costPlusPercent = previous.getCostPlusPercent();
				}
			} else if (previous.isStocked() && previous.isRecentEnough(timestamp) && leftovers == 0) {// Sold out
				preNumListings++;
				if (previous.isUsingCostPlus()) {
					undercutMargin = previous.getUndercutMargin();
					costPlusPercent = previous.getCostPlusPercent() + .05f;
				} else {
					undercutMargin = min(previous.getUndercutMargin() + .05f, .75f);
					costPlusPercent = previous.getCostPlusPercent();
				}
			} else {// Any other scenario
				undercutMargin = previous.getUndercutMargin();
				costPlusPercent = previous.getCostPlusPercent();
			}
			numListings = preNumListings;
		}
		int costPlusPrice = (int) (cost * 10 / 9 * (1 + costPlusPercent));
		int undercutMarginPrice = (int) (cost * 10 / 9
				+ (aHPrice * recordItem.getQuantityPerListing() - cost * 10 / 9) * undercutMargin);
		usingCostPlus = costPlusPrice < undercutMarginPrice;
		price = usingCostPlus ? costPlusPrice : undercutMarginPrice;
		listingPrice = max(recordItem.getStarLevelBasedListingPrice(), (int) (price * .1f + .5f));
		if (notYetExpired != null) {
			notYetExpired.setUndercut(aHPrice * recordItem.getQuantityPerListing());
			stocked = !((price < notYetExpired.getPrice() && leftovers > 0) || price < cost * 10 / 9);
		} else {
			stocked = true;
		}
		finalListings = stocked ? numListings : leftovers;
		numToSell = stocked ? numListings - leftovers : 0;
		netProfit = profit - loss;
	}

	public final int getAHPrice() {
		return aHPrice;
	}

	public final float getCostPlusPercent() {
		return costPlusPercent;
	}

	public final int getListingPrice() {
		return listingPrice;
	}

	public final int getNetProfit() {
		return netProfit;
	}

	public final int getNumListings() {
		return numListings;
	}

	public final int getNumToSell() {
		return numToSell;
	}

	public final int getPrice() {
		return price;
	}

	public final float getUndercutMargin() {
		return undercutMargin;
	}

	public boolean isRecentEnough(Date timestamp) {
		return this.timestamp.toInstant().isAfter(timestamp.toInstant().minusSeconds(60 * 60 * 28));
	}

	public final boolean isStocked() {
		return stocked;
	}

	private final float[] getExpiredCasePrice() {
		float[] flo = { ((float) undercut) / cost - 1.01f, (undercut - cost) * undercutMargin / (price - cost) - .01f };
		return flo;
	}

	/*
	 * private final Record getDayOfWeekRecord() { GregorianCalendar converter = new
	 * GregorianCalendar(); converter.setTime(timestamp); int day =
	 * converter.get(Calendar.DAY_OF_WEEK); for (Record toCheck = getPrevious();
	 * toCheck != null; toCheck = toCheck.getPrevious()) {
	 * converter.setTime(toCheck.getTimestamp()); int dayToCheck =
	 * converter.get(Calendar.DAY_OF_WEEK); if (day == dayToCheck) return toCheck; }
	 * return null; }
	 * 
	 * private final Record getLastYearRecord() { GregorianCalendar converter = new
	 * GregorianCalendar(); converter.setTime(timestamp); int day =
	 * converter.get(Calendar.DAY_OF_WEEK); int week =
	 * converter.get(Calendar.WEEK_OF_YEAR); for (Record toCheck = getPrevious();
	 * toCheck != null; toCheck = toCheck.getPrevious()) {
	 * converter.setTime(toCheck.getTimestamp()); int dayToCheck =
	 * converter.get(Calendar.DAY_OF_WEEK); int weekToCheck =
	 * converter.get(Calendar.WEEK_OF_YEAR); if (day == dayToCheck && week ==
	 * weekToCheck) return toCheck; } return null; }
	 */

	public final int getNumListingsNotStocked() {
		return numListings - finalListings;
	}

	private final int getFinalListings() {
		return finalListings;
	}

	private final Record getJustExpired() {
		Record notYet = getNotYetExpired();
		if (notYet == null)
			return null;
		return notYet.getPrevious();
	}

	private final Record getNotYetExpired() {
		GregorianCalendar converter = new GregorianCalendar();
		converter.setTime(timestamp);
		converter.set(Calendar.DAY_OF_YEAR, converter.get(Calendar.DAY_OF_YEAR) - 2);
		Date expiredTime = converter.getTime();
		Record returnValue = this;
		if (returnValue.getPrevious() == null)
			return returnValue;
		do {
			returnValue = returnValue.getPrevious();
			if (returnValue.getPrevious() == null) {
				return returnValue;
			}
		} while (returnValue.getPrevious().getTimestamp().after(expiredTime));
		return returnValue;
	}

	private final Record getPrevious() {
		return previous;
	}

	public final Date getTimestamp() {
		return timestamp;
	}

	private final int getUndercut() {
		return undercut;
	}

	private final boolean isUsingCostPlus() {
		return usingCostPlus;
	}

	private final void setUndercut(int undercut) {
		if ((this.undercut == 0 && undercut < price) || (undercut < this.undercut))
			this.undercut = undercut;
	}
}