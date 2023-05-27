package noMoreInactivity;

import javax.swing.*;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import noMoreInactivity.model.KeyPressInput;
import noMoreInactivity.model.MouseClickInput;
import noMoreInactivity.model.MyInput;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimerApplication extends JFrame {

    private JTextField secondsField;
    private JTextArea inputList;
    private JButton addButton;
    private JButton confirmButton;
    private JButton stopButton;
    private JButton resetButton;
    private JLabel inputLabel;

    private Timer timer;
    private int remainingSeconds;
    private boolean isKeyListenerActive;
    private ArrayList<MyInput> inputQueue = new ArrayList<>();
    private KeyListener keyListener;
    private NativeMouseListener mouseListener;

    public TimerApplication() {
        setTitle("Timer Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        secondsField = new JTextField(10);
        inputList = new JTextArea(10, 30);
        addButton = new JButton("+");
        confirmButton = new JButton("Start Timer");
        stopButton = new JButton("Stop Timer");
        resetButton = new JButton("Reset");
        inputLabel = new JLabel("Waiting for input...");

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Seconds:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        add(secondsField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        add(addButton, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        add(confirmButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JScrollPane(inputList), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        add(inputLabel, gbc);
        inputLabel.setVisible(false);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LAST_LINE_END;
        add(stopButton, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        add(resetButton, gbc);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activateListeners();
            }
        });

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String secondsText = secondsField.getText();
                if (!secondsText.isEmpty() && !inputQueue.isEmpty()) {
                    int seconds = Integer.parseInt(secondsText);
                    remainingSeconds = seconds;
                    updateTimerLabel();

                    timer = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            remainingSeconds--;
                            updateTimerLabel();

                            if (remainingSeconds <= 0) {
                                executeInputs();
                                remainingSeconds = seconds; // Reset the seconds counter
                            }
                        }
                    });
                    timer.start();
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Please enter the number of seconds and add at least one input.");
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopTimer();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetApplication();
            }
        });

        inputList.setEditable(false);
        inputList.setLineWrap(true);
        inputList.setWrapStyleWord(true);

        resetApplication();
    }

    private void updateTimerLabel() {
        confirmButton.setText("Timer (" + remainingSeconds + "s)");
    }

    private void stopTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            timer = null;
            remainingSeconds = 0;
            updateTimerLabel();
        }
    }

    private void resetApplication() {
        stopTimer();
        secondsField.setText("");
        inputList.setText("");
        inputQueue.clear();
        isKeyListenerActive = false;
        addButton.setEnabled(true);
        inputLabel.setVisible(false);
        stopListeners();
    }

    private void activateListeners() {
        if (!isKeyListenerActive) {
            isKeyListenerActive = true;
            addButton.setEnabled(false);
            inputLabel.setVisible(true);

            requestFocusInWindow();

            keyListener = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int keyCode = e.getKeyCode();

                    inputQueue.add(new KeyPressInput(keyCode));

                    String input = "KeyPress: " + KeyEvent.getKeyText(keyCode);
                    String currentText = inputList.getText();
                    inputList.setText(currentText + "\n" + input);

                    stopListeners();
                }
            };

            
            requestFocusInWindow();

            try {
                GlobalScreen.registerNativeHook();
            } catch (NativeHookException ex) {
                Logger.getLogger(TimerApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            mouseListener = new NativeMouseListener() {

                @Override
                public void nativeMouseClicked(NativeMouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    int button = e.getButton();

                    inputQueue.add(new MouseClickInput(x, y, button));

                    String buttonName = (button == NativeMouseEvent.BUTTON1) ? "Left" : "Right";
                    String input = "MouseClick: " + buttonName + " - X: " + x + ", Y: " + y;
                    String currentText = inputList.getText();
                    inputList.setText(currentText + "\n" + input);

                    stopListeners();
                }

                @Override
                public void nativeMousePressed(NativeMouseEvent e) {
                }

                @Override
                public void nativeMouseReleased(NativeMouseEvent e) {
                }
            };

            addKeyListener(keyListener);
            GlobalScreen.addNativeMouseListener(mouseListener);
        }
    }

    private void stopListeners() {
        isKeyListenerActive = false;
        addButton.setEnabled(true);
        inputLabel.setVisible(false);

        if (keyListener != null) {
            removeKeyListener(keyListener);
            keyListener = null;
        }

        if (mouseListener != null) {
            GlobalScreen.removeNativeMouseListener(mouseListener);
            mouseListener = null;
        }
    }

    private void executeInputs() {
        try {
            Robot robot = new Robot();

            for (MyInput input : inputQueue) {
                if (input instanceof KeyPressInput) {
                    KeyPressInput keyPressInput = (KeyPressInput) input;
                    robot.keyPress(keyPressInput.getKeyCode());
                    robot.delay(100);
                    robot.keyRelease(keyPressInput.getKeyCode());
                } else if (input instanceof MouseClickInput) {
                    MouseClickInput mouseClickInput = (MouseClickInput) input;
                    robot.mouseMove(mouseClickInput.getX(), mouseClickInput.getY());
                    robot.delay(100);

                    int button = mouseClickInput.getButton();
                    if (button == NativeMouseEvent.BUTTON1) {
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    } else if (button == NativeMouseEvent.BUTTON3) {
                        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                    }
                }
            }
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
