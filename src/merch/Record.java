package merch;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class Record implements Serializable {
	private static final long serialVersionUID = 2941864307935615486L;
	private final Record previous;
	private final boolean stocked, usingCostPlus;
	// private final Float energyPrice;
	private final float undercutMargin, costPlusPercent;
	private final int lotsSold, lotsExpired, profit, loss, netProfit, numListings, price, cost;
	private final Date timestamp;
	private int undercut;

	public final int getNetProfit() {
		return netProfit;
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
		Record returnValue = getPrevious();
		if (returnValue == null)
			return null;
		do {
			if (returnValue.getPrevious() == null) {
				return null;
			}
		} while (returnValue.getPrevious().getTimestamp().after(expiredTime));
		returnValue = returnValue.getPrevious();
		return returnValue;
	}

	private final float getExpiredCasePrice() {
		return usingCostPlus ? ((float) undercut) / cost - 1.01f
				: (undercut - cost) * undercutMargin / (price - cost) - .01f;
	}

	private final int getUndercut() {
		return undercut;
	}

	private final void setUndercut(int undercut) {
		if ((this.undercut == 0 && undercut < price) || (undercut < this.undercut))
			this.undercut = undercut;
	}

	public final float getUndercutMargin() {
		return undercutMargin;
	}

	private final Date getTimestamp() {
		return timestamp;
	}

	/*
	 * private final Record getDayOfWeekRecord() { GregorianCalendar converter =
	 * new GregorianCalendar(); converter.setTime(timestamp); int day =
	 * converter.get(Calendar.DAY_OF_WEEK); for (Record toCheck = getPrevious();
	 * toCheck != null; toCheck = toCheck.getPrevious()) {
	 * converter.setTime(toCheck.getTimestamp()); int dayToCheck =
	 * converter.get(Calendar.DAY_OF_WEEK); if (day == dayToCheck) return
	 * toCheck; } return null; }
	 * 
	 * private final Record getLastYearRecord() { GregorianCalendar converter =
	 * new GregorianCalendar(); converter.setTime(timestamp); int day =
	 * converter.get(Calendar.DAY_OF_WEEK); int week =
	 * converter.get(Calendar.WEEK_OF_YEAR); for (Record toCheck =
	 * getPrevious(); toCheck != null; toCheck = toCheck.getPrevious()) {
	 * converter.setTime(toCheck.getTimestamp()); int dayToCheck =
	 * converter.get(Calendar.DAY_OF_WEEK); int weekToCheck =
	 * converter.get(Calendar.WEEK_OF_YEAR); if (day == dayToCheck && week ==
	 * weekToCheck) return toCheck; } return null; }
	 */

	private final Record getPrevious() {
		return previous;
	}

	public final float getCostPlusPercent() {
		return costPlusPercent;
	}

	private final boolean isUsingCostPlus() {
		return usingCostPlus;
	}

	private final boolean isStocked() {
		return stocked;
	}

	public final int getPrice() {
		return price;
	}

	public final int getNumListings() {
		return stocked ? numListings : 0;
	}

	public Record(int expiredItems, int leftovers, int aHPrice, Float energyPrice, Date timestamp, Record previous,
			Item recordItem) throws Exception {
		// this.energyPrice = energyPrice;
		this.previous = previous;
		this.timestamp = timestamp;
		lotsExpired = expiredItems / recordItem.getQuantityPerListing();
		cost = recordItem.getSDCRCostPerListing(energyPrice);
		undercut = 0;
		if (previous == null) {
			lotsSold = 0;
			profit = 0;
			loss = 0;
			numListings = recordItem.getStartingListings();
			costPlusPercent = 1f;
			undercutMargin = .75f;
		} else {
			lotsSold = previous.getNumListings() - lotsExpired - leftovers;
			profit = lotsSold * (previous.getPrice() * 9 / 10 - cost);
			loss = lotsExpired
					* max(recordItem.getStarLevelBasedListingPrice(), (int) (previous.getPrice() * .05f + .5f));
			int preNumListings = previous.getNumListings();
			if (lotsExpired > 0) {// Expired
				preNumListings -= lotsExpired;
				Record justExpired = getJustExpired();
				if (justExpired.getUndercut() != 0) {
					float expiredCasePrice = justExpired.getExpiredCasePrice();
					if (previous.isUsingCostPlus()) {
						undercutMargin = previous.getUndercutMargin();
						costPlusPercent = max(min(expiredCasePrice, previous.getCostPlusPercent()), .05f);
					} else {
						undercutMargin = max(min(expiredCasePrice, previous.getUndercutMargin()), .05f);
						costPlusPercent = previous.getCostPlusPercent();
					}
				} else {
					undercutMargin = previous.getUndercutMargin();
					costPlusPercent = previous.getCostPlusPercent();
				}
			} else if (previous.isStocked() && leftovers == 0
					&& lotsExpired == 0) {/* Sold out */
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
		int costPlusPrice = (int) (cost * (1 + costPlusPercent));
		int undercutMarginPrice = (int) (cost + (aHPrice - cost) * undercutMargin);
		usingCostPlus = costPlusPrice < undercutMarginPrice;
		price = usingCostPlus ? costPlusPrice : undercutMarginPrice;
		Record notYetExpired = getNotYetExpired();
		if (notYetExpired != null) {
			notYetExpired.setUndercut(aHPrice);
			if (price < notYetExpired.getPrice()) {
				stocked = false;
			} else {
				stocked = true;
			}
		} else {
			stocked = true;
		}
		netProfit = profit - loss;
	}
}