package merch;

public class RankableParams implements Comparable<RankableParams> {// KNIVES Consider renaming this class.
    private final boolean lastSDPurchase;
    private final int maxSDPurchases, profitPerEnergySpent;
    private final RecordParams recordParams;

    public RankableParams(boolean lastSDPurchase, RecordParams recordParams) {
        this.lastSDPurchase = lastSDPurchase;
        this.recordParams = recordParams;
        int numItemsSoldFromSDPurchase = 0;
        int maxItemsToBuy = getInitialNumListingsToSell() * getNumItemsPerListing() - getNumLeftoverItems();
        if (getInitialNumListingsToSell() != 0 && maxItemsToBuy > 0) {
            if (lastSDPurchase) {
                numItemsSoldFromSDPurchase = maxItemsToBuy % getNumItemsPerSDPurchase();
                maxSDPurchases = 1;
            } else {
                numItemsSoldFromSDPurchase = getNumItemsPerSDPurchase();
                maxSDPurchases = maxItemsToBuy / getNumItemsPerSDPurchase();
            }
        } else {
            maxSDPurchases = 0;
        }
        profitPerEnergySpent
                = (getProfitPerListing())
                / getNumItemsPerListing()
                * numItemsSoldFromSDPurchase
                / getEnergyPerSDPurchase();
    }

    public int authorizeSDPurchases(int energyReserves) {
        int numPurchasesToMake = Math.min(energyReserves / getEnergyPerSDPurchase(), maxSDPurchases);
        energyReserves -= numPurchasesToMake * getEnergyPerSDPurchase();
        if (lastSDPurchase) {
            if (numPurchasesToMake == 1) {
                setNumListingsToSell(getInitialNumListingsToSell());
            }
        } else {
            setNumListingsToSell(Math.min(
                    (getNumLeftoverItems() + numPurchasesToMake * getNumItemsPerSDPurchase()) / getNumItemsPerListing(),
                    getInitialNumListingsToSell()));
        }
        return energyReserves;
    }

    @Override
    public int compareTo(RankableParams toCompareTo) {
        if (getItem() == toCompareTo.getItem()) return lastSDPurchase ? -1 : 1;
        Integer thisProfit = getProfitPerEnergySpent();
        Integer otherProfit = toCompareTo.getProfitPerEnergySpent();
        return thisProfit.compareTo(otherProfit);
    }

    public int getEnergyPerSDPurchase() {
        return recordParams.getEnergyPerSDPurchase();
    }

    private int getInitialNumListingsToSell() {
        return recordParams.getInitialNumListingsToSell();
    }

    private Item getItem() {
        return recordParams.getItem();
    }

    private int getNumItemsPerListing() {
        return recordParams.getNumItemsPerListing();
    }

    private int getNumItemsPerSDPurchase() {
        return recordParams.getNumItemsPerSDPurchase();
    }

    private int getNumLeftoverItems() {
        return recordParams.getNumLeftoverItems();
    }

    public int getProfitPerEnergySpent() {
        return profitPerEnergySpent;
    }

    private int getProfitPerListing() {
        return recordParams.getProfitPerListing();
    }

    public void setNumListingsToSell(int numListingsToSell) {
        recordParams.setNumListingsToSell(numListingsToSell);
    }
}
