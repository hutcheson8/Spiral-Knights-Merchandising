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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;

public class Main implements Serializable {
	private static final long serialVersionUID = -7878541532400694122L;
	private final static String[] DAILY_COLUMNS = { "Expired Items", "Leftovers", "AH Price" };
	private final ArrayList<Item> items = new ArrayList<Item>();

	private final class History extends JFrame {
		private static final long serialVersionUID = 55356975967430018L;

		private final TableModel historyModel;

		public History(String string, TableModel historyModel) {
			super(string);
			this.historyModel = historyModel;
		}

		private final void update() {
			removeTable();
			add(new JTable(historyModel), BorderLayout.CENTER);
			validate();
			pack();
			repaint();
		}

		private final void removeTable() {
			Component[] comps = getComponents();
			for (Component comp : comps) {
				if (comp instanceof JTable)
					remove(comp);
			}
		}
	}

	private final void record(JTextField[] data) throws Exception {
		try {
			Float energyPrice = ((float) Integer.parseInt(data[data.length - 1].getText())) / 100;
			Date timeStamp = new Date();
			for (int x = 0; x < items.size(); x++) {
				items.get(x).addRecord(Integer.parseInt(data[x * DAILY_COLUMNS.length + 0].getText()),
						Integer.parseInt(data[x * DAILY_COLUMNS.length + 1].getText()),
						Integer.parseInt(data[x * DAILY_COLUMNS.length + 2].getText()), energyPrice, timeStamp);
			}
		} catch (NumberFormatException e) {
			throw new Exception("");
		}
	}

	private final void constructDaily(Runnable update) {
		JFrame daily = new JFrame("Daily");
		daily.setLayout(new GridLayout(items.size() + 2, DAILY_COLUMNS.length + 1));
		MouseListener copier = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				HashMap<DataFormat, Object> data = new HashMap<DataFormat, Object>();
				data.put(DataFormat.PLAIN_TEXT, ((JLabel) e.getComponent()).getText());
				Platform.runLater(new Runnable() {
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
		JTextField[] textFields = new JTextField[items.size() * DAILY_COLUMNS.length + 1];
		daily.add(new JLabel("Name"));
		for (int x = 0; x < DAILY_COLUMNS.length; x++) {
			daily.add(new JLabel(DAILY_COLUMNS[x]));
		}
		for (int x = 0; x < items.size(); x++) {
			Item i = items.get(x);
			JLabel itemName = new JLabel(i.getName());
			itemName.addMouseListener(copier);
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
		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					record(textFields);
					update.run();
					daily.setVisible(false);
				} catch (Exception f) {
					f.printStackTrace();
					JOptionPane.showMessageDialog(null, "Incorrect Input!");
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
		for (int x = 0; x < DAILY_COLUMNS.length - 3; x++) {
			daily.add(new JPanel());
		}
		daily.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		daily.pack();
		daily.setLocationRelativeTo(null);
		daily.setResizable(false);
		daily.setVisible(true);
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

	private final void run() {
		new JFXPanel();
		// Allocate objects for all windows
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
						return "# of Listings";
					case 3:
						return "Price";
					case 4:
						return "Cost Plus %";
					case 5:
						return "Undercut Margin";
					}
				Item i = items.get(row - 1);
				switch (column) {
				case 0:
					return i.getName();
				case 1:
					return i.getNetProfitToDate();
				case 2:
					return i.getCurrentListings();
				case 3:
					return i.getCurrentPrice();
				case 4:
					return i.getCurrentCostPlusPercent();
				case 5:
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
			public void actionPerformed(ActionEvent e) {
				reminder.setVisible(false);
				history.setVisible(true);
			}
		});
		reminder.add(reminderDone);
		reminder.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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
		reminder.setLocationRelativeTo(null);
		// TODO Setup window for history/stats
		history.setLayout(new BorderLayout());
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout());
		JButton newItem = new JButton("Add Item");
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Form.request(new FormRequest() {
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
		history.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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