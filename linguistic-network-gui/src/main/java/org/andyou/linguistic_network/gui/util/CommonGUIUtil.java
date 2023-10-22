package org.andyou.linguistic_network.gui.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class CommonGUIUtil {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat( "0.00000" );

    public static void showErrorMessageDialog(Component parentComponent, Exception ex) {
        JOptionPane.showMessageDialog(parentComponent, ExceptionUtils.getStackTrace(ex), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarningMessageDialog(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static int showConfirmDialog(Component parentComponent, String message) {
        return JOptionPane.showConfirmDialog(parentComponent, message, "Confirmation", JOptionPane.YES_NO_OPTION);
    }

}
