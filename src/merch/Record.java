package merch;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class Record implements Serializable {
	private static final long serialVersionUID = 2941864307935615486L;
	private final int netProfit, maxListings, price, cost, aHPrice, listingPrice;
	private final Record previousRecord;
	private boolean stocked;
	private final Date timestamp;
	private int undercut, numFinalListings, numListingsToSell;
	private final float undercutMargin, costPlusPercent;
	private final boolean usingCostPlus;

	public Record(int expiredItems, int numActiveListings, int aHPrice, Float energyPrice, Date timestamp, Record previous,
			Item recordItem) {
		this.previousRecord = previous;
		this.timestamp = timestamp;
		this.aHPrice = aHPrice;
		cost = recordItem.getSDCRCostPerListing(energyPrice);
		undercut = 0;
		Record notYetExpired = getNotYetExpired();
		if (previous == null) {
			maxListings = recordItem.getNumStartingListings();
			costPlusPercent = 1f;
			undercutMargin = .75f;
			netProfit=0;
		} else {
			int numLotsExpired = expiredItems / recordItem.getNumItemsPerListing();
			Record justExpired = getJustExpired();
			int expiredListingPrice = 0;
			if (justExpired != null) {
				expiredListingPrice = justExpired.getListingPrice();
			}
			int previousMaxListings = previous.getMaxListings();
			if (numLotsExpired > 0) {// Expired
				previousMaxListings = previous.getNumFinalListings() - numLotsExpired;
				if (previousMaxListings == 0)
					previousMaxListings = 1;
				if (justExpired != null && justExpired.getUndercut() != 0) {
					float[] expiredCasePrice = justExpired.getExpiredCasePrice();
					undercutMargin = max(min(expiredCasePrice[1], previous.getUndercutMargin()), .05f);
					costPlusPercent = max(min(expiredCasePrice[0], previous.getCostPlusPercent()), .05f);
				} else {
					undercutMargin = previous.getUndercutMargin();
					costPlusPercent = previous.getCostPlusPercent();
				}
			}else if(previous.numFinalListings == previous.maxListings
					&& previous.isRecentEnough(timestamp)
					&& numActiveListings == 0){// Sold out
				previousMaxListings++;
				if (previous.usingCostPlus) {
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
			maxListings = previousMaxListings;
			netProfit
					= ((int) ((previous.getNumFinalListings() - numLotsExpired - numActiveListings)
							* (notYetExpired.getPrice() * 9f / 10 - cost))) - numLotsExpired * expiredListingPrice;
		}
		int costPlusPrice = (int) (cost * 10f / 9 * (1 + costPlusPercent));
		int undercutMarginPrice = (int) (cost * 10f / 9
				+ (aHPrice * recordItem.getNumItemsPerListing() - cost * 10f / 9) * undercutMargin);
		usingCostPlus = costPlusPrice < undercutMarginPrice;
		price = usingCostPlus ? costPlusPrice : undercutMarginPrice;
		listingPrice = max(recordItem.getStarLevelBasedListingFee(), (int) (price * .1f + .5f));
		boolean excludeStocking;
		if(notYetExpired == null){
			excludeStocking = false;
		}else{
			notYetExpired.setUndercut(aHPrice * recordItem.getNumItemsPerListing());
			excludeStocking = ((price < notYetExpired.getPrice() && numActiveListings > 0) || price < cost * 10f / 9);
		}
		numFinalListings = maxListings;
		numListingsToSell = maxListings - numActiveListings;
		if(excludeStocking) setNumListingsToSell(0);
	}
	
	public int getProfitPerListing(){ return (int) (price * 9f / 10 - cost); }
	
	public final int getAHPrice() {
		return aHPrice;
	}

	public final int getCost() {
		return cost;
	}

	public final float getCostPlusPercent() {
		return costPlusPercent;
	}

	private final float[] getExpiredCasePrice() {
		float[] flo = { ((float) undercut) / cost - 1.01f, (undercut - cost) * undercutMargin / (price - cost) - .01f };
		return flo;
	}

	private final int getNumFinalListings() {
		return numFinalListings;
	}

	private final Record getJustExpired() {
		Record notYet = getNotYetExpired();
		if (notYet == null)
			return null;
		return notYet.getPreviousRecord();
	}

	public final int getListingPrice() {
		return listingPrice;
	}

	public final int getMaxListings() {
		return maxListings;
	}

	public final int getNetProfit() {
		return netProfit;
	}

	private final Record getNotYetExpired() {
		GregorianCalendar converter = new GregorianCalendar();
		converter.setTime(timestamp);
		converter.set(Calendar.DAY_OF_YEAR, converter.get(Calendar.DAY_OF_YEAR) - 2);
		Date expiredTime = converter.getTime();
		Record returnValue = this;
		if (returnValue.getPreviousRecord() == null)
			return returnValue;
		do {
			returnValue = returnValue.getPreviousRecord();
			if (returnValue.getPreviousRecord() == null) {
				return returnValue;
			}
		} while (returnValue.getPreviousRecord().timestamp.after(expiredTime));
		return returnValue;
	}

	public final int getNumListingsNotStocked() {
		return maxListings - numFinalListings;
	}

	public final int getNumListingsToSell() {
		return numListingsToSell;
	}

	private final Record getPreviousRecord() {
		return previousRecord;
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

	public final int getPrice() {
		return price;
	}

	public final Date getTimestamp() {
		return timestamp;
	}

	private final int getUndercut() {
		return undercut;
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

	public final void setNumListingsToSell(int numListingsToSell) {
		this.numFinalListings += numListingsToSell - this.numListingsToSell;
		this.numListingsToSell = numListingsToSell;
		stocked = numListingsToSell > 0;
	}

	private final void setUndercut(int undercut) {
		if ((this.undercut == 0 && undercut < price) || (undercut < this.undercut))
			this.undercut = undercut;
	}
}