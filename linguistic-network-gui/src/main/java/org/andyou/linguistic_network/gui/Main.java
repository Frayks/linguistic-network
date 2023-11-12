package org.andyou.linguistic_network.gui;

import org.andyou.linguistic_network.gui.frame.MainFrame;
import org.andyou.linguistic_network.gui.util.CommonGUIUtil;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        MainFrame mainFrame = new MainFrame();
        mainFrame.setIconImage(CommonGUIUtil.ICON.getImage());
        mainFrame.setMinimumSize(new Dimension(1100, 600));
        mainFrame.setPreferredSize(new Dimension(800, 600));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

}
