package merch;

import javax.swing.JOptionPane;

public class GoodDealException extends Exception {
	private static final long serialVersionUID = 2314804310960270632L;
	private final String dealMessage;

	public GoodDealException(Item i, int aHPrice, float energyPrice) {
		dealMessage = "Item " + i.getName() + " is on sale for " + aHPrice + " crowns. It is worth "
				+ i.getSDCRCostPerListing(energyPrice) + ". Buy it.";
	}

	public final void showDealMessage() {
		JOptionPane.showMessageDialog(null, dealMessage);
	}
}