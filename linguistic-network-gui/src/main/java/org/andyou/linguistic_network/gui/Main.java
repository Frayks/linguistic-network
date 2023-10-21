package org.andyou.linguistic_network.gui;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        MainFrame mainForm = new MainFrame();
        mainForm.setTitle("Linguistic network");
        mainForm.setMinimumSize(new Dimension(700, 600));
        mainForm.setPreferredSize(new Dimension(700, 600));
        mainForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainForm.pack();
        mainForm.setLocationRelativeTo(null);
        mainForm.setVisible(true);
    }

}
