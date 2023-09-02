package com.knivesandsilk.sk.merchandising;

import java.util.Date;

public final class RecordParams {// KNIVES Consider renaming this class.
    private final Item item;
    private final int numExpiredItems, numLeftoverItems, numActiveListings, aHPrice;
    private int numListingsToSell;

    public RecordParams(Item item, int expiredItems, int leftoverItems, int numActiveListings, int aHPrice)
            throws CancelException {
        this.item = item;
        this.numExpiredItems = expiredItems;
        this.numLeftoverItems = leftoverItems;
        this.numActiveListings = numActiveListings;
        this.aHPrice = aHPrice;
        numListingsToSell = 0;
    }

    public void addRecord(Float energyPrice, Date timeStamp) {
        getItem().addRecord(getNumExpiredItems(), getNumActiveListings(), getAHPrice(), energyPrice, timeStamp);
    }

    public int getAHPrice() {
        return aHPrice;
    }

    public int getEnergyPerSDPurchase() {
        return item.getEnergyPerSDPurchase();
    }

    public int getInitialNumListingsToSell() {
        return item.getNumListingsToSell();
    }

    public Item getItem() {
        return item;
    }

    public int getNumActiveListings() {
        return numActiveListings;
    }

    public int getNumExpiredItems() {
        return numExpiredItems;
    }

    public int getNumItemsPerListing() {
        return item.getNumItemsPerListing();
    }

    public int getNumItemsPerSDPurchase() {
        return item.getNumItemsPerSDPurchase();
    }

    public int getNumLeftoverItems() {
        return numLeftoverItems;
    }

    public int getProfitPerListing() {
        return item.getProfitPerListing();
    }

    public void propogateNumListingsToSell() {
        item.setNumListingsToSell(numListingsToSell);
    }

    public void setNumListingsToSell(int numListingsToSell) {
        this.numListingsToSell = numListingsToSell;
    }
}
