package merch;

import javax.swing.*;
import java.awt.*;
import java.util.function.Predicate;

/**
 * This abstract class extends JPanel to be used as a line in a Form object used
 * for easy validated and well-prompted user input.
 *
 * @author Peter Wesley Hutcheson
 * @version 1.0
 * @since 1.0
 */
public class FormLine extends JPanel {
    private static final long serialVersionUID = 4170647341575661801L;
    private final Predicate<String> predicate;
    private final String help;
    private final JTextField input;
    private final JPanel notifySpot;
    private final JPanel wrong;
    private final JPanel right;

    /**
     * This constructor creates a new FormLine object based on the name and help
     * String passed to it as arguments. It will create a JPanel and fill it with a
     * custom-drawn JPanel for feedback, a JLabel, a JTextField, and a JButton for
     * help.
     *
     * @param name the name of the input field.
     * @param help the help string which appears in a dialog when the help button is
     *             pressed.
     */
    public FormLine(String name, String help, Predicate<String> predicate) {
        super();
        this.help = help;
        this.predicate = predicate;
        setLayout(new FlowLayout());
        notifySpot = new JPanel() {
            private static final long serialVersionUID = -5602302297053566788L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, 60);
            }
        };
        add(notifySpot);
        wrong = new JPanel() {
            private static final long serialVersionUID = 420522625552541384L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, 50);
            }

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0xFF, 0x00, 0x00));
                g2.setStroke(new BasicStroke(10));
                g2.drawLine(0, 0, 50, 50);
                g2.drawLine(0, 50, 50, 0);
            }
        };
        right = new JPanel() {
            private static final long serialVersionUID = -3956457224682491642L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, 50);
            }

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0x00, 0xFF, 0x00));
                g2.setStroke(new BasicStroke(10));
                g2.drawLine(0, 25, 17, 42);
                g2.drawLine(17, 42, 50, 8);
            }
        };
        JLabel inputLabel = new JLabel(name + ": ");
        inputLabel.setPreferredSize(new Dimension(100, 50));
        add(inputLabel);
        input = new JTextField(20);
        add(input);
        JButton helpButton = new JButton("Help");
        helpButton.addActionListener((e) -> {
            help();
        });
        add(helpButton);
    }

    /**
     * This method clears the user feedback area of the check and X marks and also
     * resets the text to empty when the FormLine is done receiving user input.
     */
    public void clear() {
        notifySpot.removeAll();
        input.setText("");
    }

    /**
     * @return the user input in the text field.
     */
    public String getInput() {
        return input.getText();
    }

    private void help() {
        JOptionPane.showMessageDialog(this, help);
    }

    /**
     * This method initializes the text field to the initializationString passed to
     * it as a parameter. This method is used when a form is used to update an
     * existed object.
     *
     * @param initializationString the string to initialize the text field.
     */
    public void initialize(String initializationString) {
        input.setText(initializationString);
    }

    /**
     * This method tests to see if the user input is valid, and also shows feedback
     * to the user if it is or isn't.
     *
     * @return true if the input is valid, false otherwise.
     */
    public boolean showVerify() {
        notifySpot.removeAll();
        boolean value = verify();
        if (value) {
            notifySpot.add(right);
        } else {
            notifySpot.add(wrong);
        }
        return value;
    }

    /**
     * This abstract method, to be implemented on a case by case basis via anonymous
     * implementation, returns true if the input is valid and false if it isn't.
     *
     * @return true, if the input is valid and false if it isn't.
     */
    protected final boolean verify() {
        return this.predicate.test(input.getText());
    }
}
