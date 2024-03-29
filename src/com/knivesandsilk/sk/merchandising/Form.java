package com.knivesandsilk.sk.merchandising;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * This class extends JDialog in order to be used as a form for validated user
 * input.
 *
 * @author Peter Wesley Hutcheson
 * @version 1.1
 * @since 1.0
 */
public class Form extends JDialog {
  private static final long serialVersionUID = -1599927448056995832L;
  private static Thread runningThread;
  private final FormLine[] lines;
  private boolean cancelled = false;
  private volatile boolean done = false;

  /**
   * This constructor creates a new Form which contains the FormLine objects
   * passed to it as a parameter.
   *
   * @param someLines the FormLines to be inputed to and validated by Form.
   */
  public Form(FormLine[] someLines) {
    setResizable(false);
    setLayout(new GridLayout(0, 1));
    lines = someLines;
    for(FormLine line : someLines) {
      add(line);
    }
    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new FlowLayout());
    JButton ok = new JButton("OK"), cancel = new JButton("Cancel");
    ActionListener formListener = (e) -> {
      if(((JButton) e.getSource()).getText() == "Cancel") {
        cancelled = true;
      }
      done = true;
    };
    ok.addActionListener(formListener);
    cancel.addActionListener(formListener);
    bottomPanel.add(ok);
    bottomPanel.add(cancel);
    add(bottomPanel);
    pack();
    setLocationRelativeTo(null);
  }

  public static final void request(FormRequest request) {
    if(runningThread == null || !runningThread.isAlive()) {
      runningThread = new Thread(() -> {
        try {
          request.run();
        } catch(Exception e) {
        }
      });
      runningThread.start();
    } else {
      JOptionPane.showMessageDialog(null, "Please finish the form you're currently using.");
    }
  }

  /**
   * This method recursively clears the text fields of all the FormLines, when the
   * form is done accepting user input.
   */
  public void clear() {
    for(FormLine line : lines) {
      line.clear();
    }
  }

  /**
   * This method initializes the textfields of the contained FormLines using
   * initializationVector.
   *
   * @param initializationVector the String array which represents the Strings to
   *                             be inserted in the text fields of the contained
   *                             FormLine objects.
   * @return this Form object, purely for coding convenience.
   */
  public Form initialize(String[] initializationVector) {
    for(int x = 0; x < initializationVector.length; x++) {
      lines[x].initialize(initializationVector[x]);
    }
    return this;
  }

  /**
   * This method performs recursive input validation on all the FormLine objects
   * and only returns once they're all valid, and the user just pressed "OK."
   *
   * @return a String array represented validated user input.
   * @throws CancelException when the user presses "Cancel."
   */
  public String[] result() throws CancelException {
    setVisible(true);
    do {
      validate();
      repaint();
      done = false;
      while(!done) {
      }
      if(cancelled) {
        cancelled = false;
        clear();
        setVisible(false);
        throw new CancelException();
      }
    } while(!verify());
    setVisible(false);
    String[] value = new String[lines.length];
    for(int x = 0; x < value.length; x++) {
      value[x] = lines[x].getInput();
    }
    clear();
    return value;
  }

  /**
   * This is the method which performs the recursive input validation on the
   * FormLine objects. It also makes the FormLine objects show if the input is
   * valid or not.
   *
   * @return true, if every FormLine input is valid, false otherwise.
   */
  public boolean verify() {
    boolean value = true;
    for(FormLine line : lines) {
      boolean isVerified = line.showVerify();
      value &= isVerified;
    }
    return value;
  }
}
