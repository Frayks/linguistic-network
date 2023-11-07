package org.andyou.linguistic_network.gui.util;

import org.andyou.linguistic_network.gui.api.constant.TextConstant;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Objects;

public class CommonGUIUtil {

    public static final ImageIcon ICON = new ImageIcon(Objects.requireNonNull(CommonGUIUtil.class.getResource("/icon/networkIcon.png")));

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00000");

    public static final FileFilter TEXT_FILE_FILTER = new FileNameExtensionFilter("Normal text file (*.txt)", "txt");

    public static final FileFilter XLSX_FILE_FILTER = new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx");

    public static void configureDefaultSubFrame(JFrame frame, int width, int height) {
        frame.setIconImage(CommonGUIUtil.ICON.getImage());
        frame.setMinimumSize(new Dimension(width, height));
        frame.setPreferredSize(new Dimension(width, height));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

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

    public static int showWarningConfirmDialog(Component parentComponent, String message) {
        return JOptionPane.showConfirmDialog(
                parentComponent,
                message,
                TextConstant.TITLE_WARNING,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
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
