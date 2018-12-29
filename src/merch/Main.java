package merch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;

public class Main implements Serializable {
	private final class FloatHolder {
		float f;

		private final float getFloat() {
			return f;
		}

		private final void setFloat(float f) {
			this.f = f;
		}
	}

	private final class History extends JFrame {
		private static final long serialVersionUID = 55356975967430018L;

		private final TableModel historyModel;

		public History(String string, TableModel historyModel) {
			super(string);
			this.historyModel = historyModel;
		}

		private final void removeTable() {
			Component[] comps = getComponents();
			for (Component comp : comps) {
				if (comp instanceof JTable)
					remove(comp);
			}
		}

		private final void update() {
			removeTable();
			add(new JTable(historyModel), BorderLayout.CENTER);
			validate();
			pack();
			repaint();
		}
	}

	private final static MouseListener COPIER = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {
			HashMap<DataFormat, Object> data = new HashMap<DataFormat, Object>();
			data.put(DataFormat.PLAIN_TEXT, ((JLabel) e.getComponent()).getText());
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Clipboard.getSystemClipboard().setContent(data);
				}
			});
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	};
	/**
	 * { "Expired Items", "Leftovers", "AH Price" }
	 */
	private final static String[] DAILY_COLUMNS = { "Expired Items", "Leftovers", "AH Price", "AH Items/Listing" };
	private static final long serialVersionUID = -7878541532400694122L;

	private Float energyPrice;

	private final ArrayList<Item> items = new ArrayList<Item>();

	private final void constructDaily(Runnable update) {
		JFrame daily = new JFrame("Daily");
		daily.setLayout(new GridLayout(items.size() + 2, DAILY_COLUMNS.length + 1));
		JTextField[] textFields = new JTextField[items.size() * DAILY_COLUMNS.length + 1];
		daily.add(new JLabel("Name"));
		for (int x = 0; x < DAILY_COLUMNS.length; x++) {
			daily.add(new JLabel(DAILY_COLUMNS[x]));
		}
		for (int x = 0; x < items.size(); x++) {
			Item i = items.get(x);
			JLabel itemName = new JLabel(i.getName());
			itemName.addMouseListener(COPIER);
			daily.add(itemName);
			for (int y = 0; y < DAILY_COLUMNS.length; y++) {
				textFields[x * DAILY_COLUMNS.length + y] = new JTextField();
				daily.add(textFields[x * DAILY_COLUMNS.length + y]);
			}
		}
		JLabel energyPriceLabel = new JLabel("Energy Price: ");
		daily.add(energyPriceLabel);
		JTextField energyPrice = new JTextField();
		textFields[textFields.length - 1] = energyPrice;
		daily.add(energyPrice);
		daily.add(new JPanel());
		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					record(textFields);
					update.run();
					daily.setVisible(false);
					constructSellPrompt();
				} catch (Exception f) {
					if (f instanceof GoodDealException) {
						GoodDealException deal = (GoodDealException) f;
						deal.showDealMessage();
					} else {
						f.printStackTrace();
						JOptionPane.showMessageDialog(null, "Incorrect Input!");
					}
				}
			}
		});
		daily.add(submit);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				daily.setVisible(false);
			}
		});
		daily.add(cancel);
		for (int x = 0; x < DAILY_COLUMNS.length - 4; x++) {
			daily.add(new JPanel());
		}
		daily.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		daily.pack();
		daily.setLocationRelativeTo(null);
		daily.setResizable(false);
		daily.setVisible(true);
	}

	private final void constructSellPrompt() {
		JFrame sellPrompt = new JFrame("Sell");
		sellPrompt.setLayout(new GridLayout(0, 3));
		JLabel name = new JLabel("Name"), listings = new JLabel("# Listings"), price = new JLabel("Price");
		sellPrompt.add(name);
		sellPrompt.add(listings);
		sellPrompt.add(price);
		for (Item i : items) {
			if (i.isCurrentlyStocked() && i.getNumToSell() > 0) {
				JLabel aName = new JLabel(i.getName());
				aName.addMouseListener(COPIER);
				JLabel someListings = new JLabel(i.getNumToSell() + "");
				JLabel aPrice = new JLabel(i.getCurrentPrice() + "");
				aPrice.addMouseListener(COPIER);
				sellPrompt.add(aName);
				sellPrompt.add(someListings);
				sellPrompt.add(aPrice);
			}
		}
		sellPrompt.pack();
		sellPrompt.setLocationRelativeTo(null);
		sellPrompt.setResizable(false);
		sellPrompt.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		sellPrompt.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				constructSpendingOffers();
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowOpened(WindowEvent e) {
			}
		});
		sellPrompt.setVisible(true);
	}

	private final void constructSpendingOffers() {
		JFrame spendingOffers = new JFrame("Spending/Offers");
		spendingOffers.setLayout(new GridLayout(0, 3));
		int crownOffers = 0, energyOffers = 0;
		for (Item i : items) {
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
		combine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					spendingCrowns.setFloat(Integer.parseInt(crownRemain.getText())
							+ Integer.parseInt(energyRemain.getText()) * energyPrice);
				} catch (Exception f) {
					JOptionPane.showMessageDialog(null, "Incorrect Input!");
				}
				crownRemain.setText("");
				energyRemain.setText("");
				spendingOffers.validate();
				spendingOffers.pack();
				spendingOffers.repaint();
			}
		});
		spendingOffers.add(combine);
		spendingOffers.add(new JLabel() {
			private static final long serialVersionUID = 8316659088569282216L;

			@Override
			public String getText() {
				return spendingCrowns.getFloat() + "";
			}
		});
		spendingOffers.add(new JLabel() {
			private static final long serialVersionUID = 8304390475151931293L;

			@Override
			public String getText() {
				return spendingCrowns.getFloat() / energyPrice + "";
			}
		});
		JButton spend = new JButton("Spend");
		spendingOffers.add(spend);
		JTextField spentCrowns = new JTextField();
		spendingOffers.add(spentCrowns);
		JTextField spentEnergy = new JTextField();
		spendingOffers.add(spentEnergy);
		spend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				float spentCrownsOutput;
				try {
					spentCrownsOutput = Integer.parseInt(spentCrowns.getText());
				} catch (Exception f) {
					spentCrownsOutput = 0;
				}
				float spentEnergyOutput;
				try {
					spentEnergyOutput = Integer.parseInt(spentEnergy.getText());
				} catch (Exception f) {
					spentEnergyOutput = 0;
				}
				spendingCrowns
						.setFloat(spendingCrowns.getFloat() - spentCrownsOutput - spentEnergyOutput * energyPrice);
				spentCrowns.setText("");
				spentEnergy.setText("");
				spendingOffers.validate();
				spendingOffers.pack();
				spendingOffers.repaint();
			}
		});
		spendingOffers.pack();
		spendingOffers.setLocationRelativeTo(null);
		spendingOffers.setResizable(false);
		spendingOffers.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		spendingOffers.setVisible(true);
	}

	private final void finish() {
		try {
			ObjectOutputStream savingStream = new ObjectOutputStream(
					new BufferedOutputStream(new FileOutputStream("data.srl")));
			savingStream.writeObject(this);
			savingStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private final void record(JTextField[] data) throws Exception {
		energyPrice = ((float) Integer.parseInt(data[data.length - 1].getText())) / 100;
		Date timeStamp = new Date();
		int[] expiredItems = new int[items.size()];
		int[] leftovers = new int[items.size()];
		int[] aHPrice = new int[items.size()];
		for (int x = 0; x < items.size(); x++) {
			// Throwing potential exceptions at once to avoid partial record creation.
			int aHItems;
			Item i = items.get(x);
			expiredItems[x] = Integer.parseInt(data[x * DAILY_COLUMNS.length + 0].getText());
			leftovers[x] = Integer.parseInt(data[x * DAILY_COLUMNS.length + 1].getText());
			aHItems = Integer.parseInt(data[x * DAILY_COLUMNS.length + 3].getText());
			aHPrice[x] = Integer.parseInt(data[x * DAILY_COLUMNS.length + 2].getText()) / aHItems;
			if (aHPrice[x] == 0)
				aHPrice[x] = (int) (i.getMostRecentAHPrice(energyPrice) * 1.01);
			if (aHPrice[x] * i.getQuantityPerListing() < i.getSDCRCostPerListing(energyPrice))
				throw new GoodDealException(i, aHPrice[x], energyPrice);
		}
		for (int x = 0; x < items.size(); x++) {// Now that input definitely works, create all records at once.
			items.get(x).addRecord(expiredItems[x], leftovers[x], aHPrice[x], energyPrice, timeStamp);
		}
	}

	private final void run() {
		new JFXPanel();
		JFrame reminder = new JFrame("Reminder");
		History history = new History("History", new AbstractTableModel() {
			private static final long serialVersionUID = -160380739584172259L;

			@Override
			public int getColumnCount() {
				return 6;
			}

			@Override
			public int getRowCount() {
				return items.size() + 1;
			}

			@Override
			public Object getValueAt(int row, int column) {
				if (row == 0)
					switch (column) {
					case 0:
						return "Name";
					case 1:
						return "Net Profit to Date";
					case 2:
						return "Net Profit One Week";
					case 3:
						return "Net Profit Today";
					case 4:
						return "# of Listings";
					case 5:
						return "Price";
					case 6:
						return "Cost Plus %";
					case 7:
						return "Undercut Margin";
					}
				Item i = items.get(row - 1);
				switch (column) {
				case 0:
					return i.getName();
				case 1:
					return i.getNetProfitToDate();
				case 2:
					return i.getNetProfitSince(Date.from((new Date()).toInstant().minusSeconds(60 * 60 * 24 * 7)));
				case 3:
					return i.getNetProfitSince(null);
				case 4:
					return i.getCurrentListings();
				case 5:
					return i.getCurrentPrice();
				case 6:
					return i.getCurrentCostPlusPercent();
				case 7:
					return i.getCurrentUndercutMargin();
				}
				return null;
			}
		});
		// Setup Window for small reminder
		reminder.setLayout(new FlowLayout());
		reminder.add(new JLabel("Feed pet and complete prestige mission."));
		JButton reminderDone = new JButton("Done");
		reminderDone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reminder.setVisible(false);
				history.setVisible(true);
			}
		});
		reminder.add(reminderDone);
		reminder.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		reminder.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				history.setVisible(true);
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowOpened(WindowEvent e) {
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
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Form.request(new FormRequest() {
					@Override
					public void run() throws CancelException {
						items.add(new Item());
						history.update();
					}
				});
			}
		});
		bottomPanel.add(newItem);
		JButton startDaily = new JButton("Start Daily");
		startDaily.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				constructDaily(new Runnable() {
					@Override
					public void run() {
						history.update();
					}
				});
			}
		});
		bottomPanel.add(startDaily);
		history.add(bottomPanel, BorderLayout.SOUTH);
		history.update();
		history.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		history.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				finish();
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowOpened(WindowEvent e) {
			}
		});
		history.setLocationRelativeTo(null);
		history.setResizable(false);
		reminder.setVisible(true);
	}

	public static void main(String[] args) {
		Main main;
		try {
			ObjectInputStream loadingStream = new ObjectInputStream(
					new BufferedInputStream(new FileInputStream("data.srl")));
			main = (Main) loadingStream.readObject();
			loadingStream.close();
		} catch (Exception e) {
			main = new Main();
		}
		main.run();
	}
}