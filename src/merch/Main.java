package merch;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class Main implements Serializable {
	private static final class FloatHolder {
		float f;

		private float getFloat(){ return f; }

		private void setFloat(float f){ this.f = f; }
	}

	private final class History extends JFrame implements ClipboardOwner {
		private final TableModel historyModel = new AbstractTableModel() {
			@Override
			public int getColumnCount(){ return 9; }

			@Override
			public int getRowCount(){ return items.size() + 2; }

			@Override
			public Object getValueAt(int row, int column){
				if (row == 0) return
						switch (column) {
							case 0 -> "Name";
							case 1 -> "Net Profit to Date";
							case 2 -> "Net Profit One Week";
							case 3 -> "Net Profit Today";
							case 4 -> "# of Listings";
							case 5 -> "Cost";
							case 6 -> "Price";
							case 7 -> "Cost Plus %";
							case 8 -> "Undercut Margin";
							default -> null;
						};
				if(row == getRowCount() - 1){
					int sum = 0;
					return switch(column) {
						case 0 -> "Total";
						case 1 -> items.stream().map(Item::getNetProfitToDate).reduce(Integer::sum);
						case 2 -> items.stream().map(i -> i.getNetProfitSince(Date.from((new Date()).toInstant().minusSeconds(60 * 60 * 24 * 7)))).reduce(Integer::sum);
						case 3 -> items.stream().map(Item::getNetProfitSince).reduce(Integer::sum);
						case 4 -> items.stream().map(Item::getMaxListings).reduce(Integer::sum);
						default -> "N/A";
					};
				}
				Item i = items.get(row - 1);
				return switch (column) {
					case 0 -> i.getName();
					case 1 -> i.getNetProfitToDate();
					case 2 -> i.getNetProfitSince(Date.from((new Date()).toInstant().minusSeconds(60 * 60 * 24 * 7)));
					case 3 -> i.getNetProfitSince(null);
					case 4 -> i.getMaxListings();
					case 5 -> i.getCost();
					case 6 -> i.getPrice();
					case 7 -> i.getCostPlusPercent();
					case 8 -> i.getUndercutMargin();
					default -> null;
				};
			}
		};

		public History(){
			super("History");
		}

		private void removeTable(){
			Component[] comps = getComponents();
			for(Component comp : comps){
				if(comp instanceof JTable) remove(comp);
			}
		}

		private void update(){
			removeTable();
			add(new JTable(historyModel), BorderLayout.CENTER);
			validate();
			pack();
			repaint();
		}

		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
		}
	}

	private final static MouseListener COPIER = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e){
			String toAddToClipboard = ((JLabel) e.getComponent()).getText();
			History history = (History) e.getComponent().getParent();
			StringSelection stringSelection = new StringSelection(toAddToClipboard);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, history);
		}

		@Override
		public void mouseEntered(MouseEvent e){
		}

		@Override
		public void mouseExited(MouseEvent e){
		}

		@Override
		public void mousePressed(MouseEvent e){
		}

		@Override
		public void mouseReleased(MouseEvent e){
		}
	};
	/**
	 * { "Expired Items", "Leftover Items", "Active Listings", "AH Price", "AH
	 * Items/Listing" }
	 */
	private final static String[] DAILY_COLUMNS
			= {"Expired Items", "Leftover Items", "Active Listings", "AH Price", "AH Items/Listing"};

	public static void main(String[] args){
		Main main;
		try{
			ObjectInputStream loadingStream
					= new ObjectInputStream(new BufferedInputStream(new FileInputStream("data.srl")));
			main = (Main) loadingStream.readObject();
			loadingStream.close();
		}catch(Exception e){
			main = new Main();
		}
		main.run();
	}

	private Float energyPrice;
	private final ArrayList<Item> items = new ArrayList<>();

	private void constructDaily(Runnable update){
		JFrame daily = new JFrame("Daily");
		daily.setLayout(new GridLayout(items.size() + 2, DAILY_COLUMNS.length + 1));
		JTextField[] textFields = new JTextField[items.size() * DAILY_COLUMNS.length + 2];
		daily.add(new JLabel("Name"));
		Arrays.stream(DAILY_COLUMNS).forEach(dailyColumn -> daily.add(new JLabel(dailyColumn)));
		for(int x = 0; x < items.size(); x++){
			Item i = items.get(x);
			JLabel itemName = new JLabel(i.getName());
			itemName.addMouseListener(COPIER);
			daily.add(itemName);
			//KNIVES Resume here
			for(int y = 0; y < DAILY_COLUMNS.length; y++){
				textFields[x * DAILY_COLUMNS.length + y] = new JTextField();
				JTextField textField = textFields[x * DAILY_COLUMNS.length + y];
				daily.add(textField);
			}
		}
		JLabel energyPriceLabel = new JLabel("Energy Price: ");
		daily.add(energyPriceLabel);
		JTextField energyPrice = new JTextField();
		textFields[textFields.length - 2] = energyPrice;
		daily.add(energyPrice);
		JLabel energyReservesLabel = new JLabel("Energy Reserves: ");
		daily.add(energyReservesLabel);
		JTextField energyReserves = new JTextField();
		textFields[textFields.length - 1] = energyReserves;
		daily.add(energyReserves);
		JButton submit = new JButton("Submit");
		submit.addActionListener((e)->{
			try{
				record(textFields);
				update.run();
				daily.setVisible(false);
				constructSellPrompt();
			}catch(Exception f){
				if(f instanceof GoodDealException){
					GoodDealException deal = (GoodDealException) f;
					deal.showDealMessage();
				}else{
					f.printStackTrace();
					JOptionPane.showMessageDialog(null, "Incorrect Input!");
				}
			}
		});
		daily.add(submit);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener((e)->daily.setVisible(false));
		daily.add(cancel);
		// for (int x = 0; x < DAILY_COLUMNS.length - ?; x++) {
		// daily.add(new JPanel());
		// }
		daily.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		daily.pack();
		daily.setLocationRelativeTo(null);
		daily.setResizable(false);
		daily.setVisible(true);
	}

	private final void constructSellPrompt(){
		JFrame sellPrompt = new JFrame("Sell");
		sellPrompt.setLayout(new GridLayout(0, 3));
		JLabel name = new JLabel("Name"), numItems = new JLabel("# Items"), price = new JLabel("Price");
		sellPrompt.add(name);
		sellPrompt.add(numItems);
		sellPrompt.add(price);
		for(Item item : items){
			if(item.isStocked() && item.getNumListingsToSell() > 0){
				JLabel aName = new JLabel(item.getName());
				aName.addMouseListener(COPIER);
				JLabel someItems = new JLabel(item.getNumListingsToSell() * item.getNumItemsPerListing() + "");
				JLabel aPrice = new JLabel(item.getPrice() + "");
				aPrice.addMouseListener(COPIER);
				sellPrompt.add(aName);
				sellPrompt.add(someItems);
				sellPrompt.add(aPrice);
			}
		}
		sellPrompt.pack();
		sellPrompt.setLocationRelativeTo(null);
		sellPrompt.setResizable(false);
		sellPrompt.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		sellPrompt.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent e){
			}

			@Override
			public void windowClosed(WindowEvent e){
			}

			@Override
			public void windowClosing(WindowEvent e){
				constructSpendingOffers();
			}

			@Override
			public void windowDeactivated(WindowEvent e){
			}

			@Override
			public void windowDeiconified(WindowEvent e){
			}

			@Override
			public void windowIconified(WindowEvent e){
			}

			@Override
			public void windowOpened(WindowEvent e){
			}
		});
		sellPrompt.setVisible(true);
	}

	private final void constructSpendingOffers(){
		JFrame spendingOffers = new JFrame("Spending/Offers");
		spendingOffers.setLayout(new GridLayout(0, 3));
		int crownOffers = 0, energyOffers = 0;
		for(Item i : items){
			crownOffers += i.getRequiredCrownReserves();
			energyOffers += i.getRequiredEnergyReserves();
		}
		crownOffers = crownOffers / 5000 + 1;
		energyOffers = energyOffers / 100 + 1;
		spendingOffers.add(new JLabel("Operation"));
		spendingOffers.add(new JLabel("Crowns"));
		spendingOffers.add(new JLabel("Energy"));
		spendingOffers.add(new JLabel("Offers: "));
		spendingOffers.add(new JLabel(crownOffers + "x5000 Offers"));
		spendingOffers.add(new JLabel(energyOffers + "x100 Offers"));
		spendingOffers.add(new JLabel("Remainder: "));
		JTextField crownRemain = new JTextField();
		spendingOffers.add(crownRemain);
		JTextField energyRemain = new JTextField();
		spendingOffers.add(energyRemain);
		FloatHolder spendingCrowns = new FloatHolder();
		spendingCrowns.setFloat(0);
		JButton combine = new JButton("Combine");
		combine.addActionListener((e)->{
			try{
				spendingCrowns.setFloat(Integer.parseInt(crownRemain.getText())
						+ Integer.parseInt(energyRemain.getText()) * energyPrice);
			}catch(Exception f){
				JOptionPane.showMessageDialog(null, "Incorrect Input!");
			}
			crownRemain.setText("");
			energyRemain.setText("");
			spendingOffers.validate();
			spendingOffers.pack();
			spendingOffers.repaint();
		});
		spendingOffers.add(combine);
		spendingOffers.add(new JLabel() {
			private static final long serialVersionUID = 8316659088569282216L;

			@Override
			public String getText(){ return spendingCrowns.getFloat() + ""; }
		});
		spendingOffers.add(new JLabel() {
			private static final long serialVersionUID = 8304390475151931293L;

			@Override
			public String getText(){ return spendingCrowns.getFloat() / energyPrice + ""; }
		});
		JButton spend = new JButton("Spend");
		spendingOffers.add(spend);
		JTextField spentCrowns = new JTextField();
		spendingOffers.add(spentCrowns);
		JTextField spentEnergy = new JTextField();
		spendingOffers.add(spentEnergy);
		spend.addActionListener((e)->{
			float spentCrownsOutput;
			try{
				spentCrownsOutput = Integer.parseInt(spentCrowns.getText());
			}catch(Exception f){
				spentCrownsOutput = 0;
			}
			float spentEnergyOutput;
			try{
				spentEnergyOutput = Integer.parseInt(spentEnergy.getText());
			}catch(Exception f){
				spentEnergyOutput = 0;
			}
			spendingCrowns.setFloat(spendingCrowns.getFloat() - spentCrownsOutput - spentEnergyOutput * energyPrice);
			spentCrowns.setText("");
			spentEnergy.setText("");
			spendingOffers.validate();
			spendingOffers.pack();
			spendingOffers.repaint();
		});
		spendingOffers.pack();
		spendingOffers.setLocationRelativeTo(null);
		spendingOffers.setResizable(false);
		spendingOffers.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		spendingOffers.setVisible(true);
	}

	private final void finish(){
		switch(JOptionPane.showConfirmDialog(null, "Save Your Work?")){
		case 0:
			try{
				ObjectOutputStream savingStream
						= new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("data.srl")));
				savingStream.writeObject(this);
				savingStream.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		case 1:
			System.exit(0);
			break;
		default:
		}
	}

	private final String lastUpdateText(){
		if(items.isEmpty())
			return "First Run";
		else
			return "Last Update: "
					+ items.get(0).lastUpdate()
					+ " ("
					+ (((new Date()).getTime() - items.get(0).lastUpdate().getTime()) / 3600000)
					+ " hours ago)";
	}

	private final void record(JTextField[] data) throws Exception{
		energyPrice = ((float) Integer.parseInt(data[data.length - 2].getText())) / 100;
		int energyReserves = Integer.parseInt(data[data.length - 1].getText());
		List<RecordParams> recordParamsList = new ArrayList<>(items.size());
		Date timeStamp = new Date();
		for(int x = 0; x < items.size(); x++){// Throwing potential exceptions at once to avoid partial record creation.
			Item item = items.get(x);
			int aHPrice
					= Integer.parseInt(data[x * DAILY_COLUMNS.length + 3].getText())
							/ Integer.parseInt(data[x * DAILY_COLUMNS.length + 4].getText());
			if(aHPrice == 0) aHPrice = (int) (item.getAHPrice(energyPrice) * 1.01);
			if(aHPrice * item.getNumItemsPerListing() < item.getSDCRCostPerListing(energyPrice))
				throw new GoodDealException(item, aHPrice, energyPrice);
			recordParamsList.add(new RecordParams(
					item,
					Integer.parseInt(data[x * DAILY_COLUMNS.length].getText()),
					Integer.parseInt(data[x * DAILY_COLUMNS.length + 1].getText()),
					Integer.parseInt(data[x * DAILY_COLUMNS.length + 2].getText()),
					aHPrice));
		}
		recordParamsList.forEach((recordParams)->{
			recordParams.addRecord(energyPrice, timeStamp);
		});
		List<RankableParams> rankableParamsList = new ArrayList<>(items.size() * 2);
		recordParamsList.forEach((recordParams)->{
			rankableParamsList.add(new RankableParams(false, recordParams));
			rankableParamsList.add(new RankableParams(true, recordParams));
		});
		rankableParamsList.sort(Collections.reverseOrder());
		for(RankableParams rankableParams : rankableParamsList){
			energyReserves = rankableParams.authorizeSDPurchases(energyReserves);
		}
		recordParamsList.forEach(RecordParams::propogateNumListingsToSell);
	}

	private void run(){
		JFrame reminder = new JFrame("Reminder");
		History history = new History();
		// Setup Window for small reminder
		reminder.setLayout(new FlowLayout());
		reminder.add(new JLabel("Feed pet and complete prestige mission."));
		JButton reminderDone = new JButton("Done");
		reminderDone.addActionListener((e)->{
			reminder.setVisible(false);
			history.setVisible(true);
		});
		reminder.add(reminderDone);
		reminder.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		reminder.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0){
			}

			@Override
			public void windowClosed(WindowEvent e){
			}

			@Override
			public void windowClosing(WindowEvent e){
				history.setVisible(true);
			}

			@Override
			public void windowDeactivated(WindowEvent e){
			}

			@Override
			public void windowDeiconified(WindowEvent e){
			}

			@Override
			public void windowIconified(WindowEvent e){
			}

			@Override
			public void windowOpened(WindowEvent e){
			}
		});
		reminder.pack();
		reminder.setResizable(false);
		reminder.setLocationRelativeTo(null);
		// Setup window for history/stats
		history.setLayout(new BorderLayout());
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout());
		JButton newItem = new JButton("Add Item");
		newItem.addActionListener((e)->{
			Form.request(()->{
				items.add(new Item());
				history.update();
			});
		});
		bottomPanel.add(newItem);
		JLabel lastUpdate = new JLabel(lastUpdateText());
		JButton startDaily = new JButton("Start Daily");
		startDaily.addActionListener((e)->{
			constructDaily(()->{
				history.update();
				lastUpdate.setText(lastUpdateText());
			});
		});
		bottomPanel.add(startDaily);
		bottomPanel.add(lastUpdate);
		history.add(bottomPanel, BorderLayout.SOUTH);
		history.update();
		history.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		history.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0){
			}

			@Override
			public void windowClosed(WindowEvent e){
			}

			@Override
			public void windowClosing(WindowEvent e){
				finish();
			}

			@Override
			public void windowDeactivated(WindowEvent e){
			}

			@Override
			public void windowDeiconified(WindowEvent e){
			}

			@Override
			public void windowIconified(WindowEvent e){
			}

			@Override
			public void windowOpened(WindowEvent e){
			}
		});
		history.setLocationRelativeTo(null);
		history.setResizable(false);
		reminder.setVisible(true);
	}
}
