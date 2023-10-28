package org.andyou.linguistic_network.gui.util;

import org.andyou.linguistic_network.gui.api.constant.TextConstant;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class CommonGUIUtil {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00000");

    public static void showErrorMessageDialog(Component parentComponent, Exception ex) {
        JOptionPane.showMessageDialog(
                parentComponent,
                ExceptionUtils.getStackTrace(ex),
                TextConstant.TITLE_ERROR,
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void showWarningMessageDialog(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                TextConstant.TITLE_WARNING,
                JOptionPane.WARNING_MESSAGE
        );
    }

    public static int showConfirmDialog(Component parentComponent, String message) {
        return JOptionPane.showConfirmDialog(
                parentComponent,
                message,
                TextConstant.TITLE_CONFIRMATION,
                JOptionPane.YES_NO_OPTION
        );
    }

    public static int showQuestionDialog(Component parentComponent, String message, String title, Object[] options) {
        return JOptionPane.showOptionDialog(
                parentComponent,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
    }

}
