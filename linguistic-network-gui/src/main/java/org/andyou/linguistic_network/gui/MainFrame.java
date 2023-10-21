package org.andyou.linguistic_network.gui;

import org.andyou.linguistic_network.lib.api.node.SWNode;
import org.andyou.linguistic_network.lib.util.SWNodeGraphUtil;
import org.andyou.linguistic_network.lib.util.TextTokenizerUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private JMenu fileMenu;
    private JMenuItem saveMenuItem;
    private JMenuItem openMenuItem;
    private JTextField textFileTextField;
    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox considerSentenceBoundsCheckBox;
    private JCheckBox useRangeCheckBox;
    private JLabel rangeLabel;
    private JSpinner rangeSizeSpinner;
    private JCheckBox removeStopWordsCheckBox;
    private JTextField stopWordsFileTextField;
    private JButton chooseStopWordsFileButton;
    private JCheckBox filterByFrequencyCheckBox;
    private JLabel frequencyLabel;
    private JSpinner filterFrequencySpinner;
    private JButton calculateButton;
    private JTable statisticTable;
    private JProgressBar progressBar;
    private JButton terminateCalculationButton;

    private File textFile;
    private File stopWordsFile;


    public MainFrame() {
        setContentPane(mainPanel);

        useRangeCheckBox.addActionListener(e -> {
            rangeLabel.setEnabled(useRangeCheckBox.isSelected());
            rangeSizeSpinner.setEnabled(useRangeCheckBox.isSelected());
        });

        removeStopWordsCheckBox.addActionListener(e -> {
            stopWordsFileTextField.setEnabled(removeStopWordsCheckBox.isSelected());
            chooseStopWordsFileButton.setEnabled(removeStopWordsCheckBox.isSelected());
        });

        filterByFrequencyCheckBox.addActionListener(e -> {
            frequencyLabel.setEnabled(filterByFrequencyCheckBox.isSelected());
            filterFrequencySpinner.setEnabled(filterByFrequencyCheckBox.isSelected());
        });

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Normal text file (*.txt)", "txt"));

        openMenuItem.addActionListener(e -> {
            int returnValue = jFileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                textFile = jFileChooser.getSelectedFile();
                textFileTextField.setText(textFile.getAbsolutePath());
                calculateButton.setEnabled(true);
            }
        });

        chooseStopWordsFileButton.addActionListener(e -> {
            int returnValue = jFileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                stopWordsFile = jFileChooser.getSelectedFile();
                stopWordsFileTextField.setText(stopWordsFile.getAbsolutePath());
            }
        });

        String[] columnIdentifiers = {"Element", "Frequency"};
        DefaultTableModel defaultTableModel = new DefaultTableModel(0, 2) {
            final Class<?>[] types = {String.class, Integer.class};

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return this.types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        defaultTableModel.setColumnIdentifiers(columnIdentifiers);
        statisticTable.setModel(defaultTableModel);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        statisticTable.getRowSorter().setSortKeys(sortKeys);

        calculateButton.addActionListener(e -> {
            try {
                String text = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);

                boolean caseSensitive = caseSensitiveCheckBox.isSelected();
                boolean considerSentenceBounds = considerSentenceBoundsCheckBox.isSelected();
                boolean useRange = useRangeCheckBox.isSelected();
                int rangeSize = (int) rangeSizeSpinner.getValue();
                boolean removeStopWords = removeStopWordsCheckBox.isSelected();
                boolean filterByFrequency = filterByFrequencyCheckBox.isSelected();
                int filterFrequency = (int) filterFrequencySpinner.getValue();

                String[][] elementGroups = TextTokenizerUtil.createElementGroups(text, caseSensitive, considerSentenceBounds);

                if (removeStopWords) {
                    String stopWordsText = new String(Files.readAllBytes(stopWordsFile.toPath()), StandardCharsets.UTF_8);
                    String[] stopWords = TextTokenizerUtil.splitIntoWords(stopWordsText);
                    elementGroups = TextTokenizerUtil.removeStopWords(elementGroups, stopWords);
                }

                Set<SWNode> swNodeGraph = SWNodeGraphUtil.createSWNodeGraph(elementGroups, useRange, rangeSize);

                if (filterByFrequency) {
                    SWNodeGraphUtil.filterByFrequency(swNodeGraph, filterFrequency);
                }

                defaultTableModel.setRowCount(0);
                swNodeGraph.forEach(swNode -> {
                    defaultTableModel.addRow(new Object[]{swNode.getElement(), swNode.getFrequency()});
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ExceptionUtils.getStackTrace(ex), "Error!", JOptionPane.ERROR_MESSAGE);
            }
        });

    }

    private void createUIComponents() {
        rangeSizeSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 1000000000, 1));

        filterFrequencySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000000000, 1));


        //statisticTable.getRowSorter().setSortKeys(Collections.singletonList(new RowSorter.SortKey(1, SortOrder.DESCENDING)));

    }
}
