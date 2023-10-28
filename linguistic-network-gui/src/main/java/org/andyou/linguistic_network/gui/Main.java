package org.andyou.linguistic_network.gui;

import org.andyou.linguistic_network.gui.frame.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        MainFrame mainFrame = new MainFrame();
        mainFrame.setTitle("Linguistic Network");
        mainFrame.setIconImage(new ImageIcon(Main.class.getResource("/icon/networkIcon.png")).getImage());
        mainFrame.setMinimumSize(new Dimension(800, 600));
        mainFrame.setPreferredSize(new Dimension(800, 600));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

}
