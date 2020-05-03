package merch;

public class RankableParams implements Comparable<RankableParams> {// KNIVES Consider renaming this class.
	private final boolean lastSDPurchase;
	private final int maxListingsToSell;
	private int maxSDPurchases;
	private final int profittingFromItemQuantity, profitPerEnergySpent;
	private final RecordParams recordParams;

	public RankableParams(boolean lastSDPurchase, RecordParams recordParams){
		this.lastSDPurchase = lastSDPurchase;
		this.recordParams = recordParams;
		profittingFromItemQuantity = recordParams.getProfittingFromItemQuantity(lastSDPurchase);
		profitPerEnergySpent = recordParams.getProfitPerEnergySpent(lastSDPurchase);
		if(lastSDPurchase){
			maxSDPurchases = profittingFromItemQuantity == 0 ? 0 : 1;
		}else{
			maxSDPurchases
					= (recordParams.getItem().getNumListingsToSell() * recordParams.getQuantityPerListing()
							- recordParams.getLeftoverItems()) / recordParams.getQuantityPerSDPurchase();
		}
		this.maxListingsToSell = recordParams.getItem().getNumListingsToSell();
	}

	public int authorizeSDPurchases(int energyReserves){
		int numPurchasesToMake = Math.min(energyReserves / getEnergyPerSDPurchase(), maxSDPurchases);
		int itemStock = getLeftoverItems() + numPurchasesToMake * getQuantityPerSDPurchase();
		energyReserves -= numPurchasesToMake * getEnergyPerSDPurchase();
		if(lastSDPurchase){
			if(numPurchasesToMake == 1){
				setNumListingsToSell(maxListingsToSell);
			}
		}else{
			setNumListingsToSell(Math.min(itemStock / getQuantityPerListing(), maxListingsToSell));
		}
		return energyReserves;
	}

	@Override
	public int compareTo(RankableParams toCompareTo){
		Integer thisProfit = getProfitPerEnergySpent();
		Integer otherProfit = toCompareTo.getProfitPerEnergySpent();
		return thisProfit.compareTo(otherProfit);
	}

	public int getEnergyPerSDPurchase(){ return recordParams.getEnergyPerSDPurchase(); }

	private int getLeftoverItems(){ return recordParams.getLeftoverItems(); }

	public int getProfitPerEnergySpent(){ return profitPerEnergySpent; }

	private int getQuantityPerListing(){ return recordParams.getQuantityPerListing(); }

	private int getQuantityPerSDPurchase(){ return recordParams.getQuantityPerSDPurchase(); }

	public void setNumListingsToSell(int numListingsToSell){
		recordParams.setNumListingsToSell(numListingsToSell);
	}
}
