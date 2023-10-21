package org.andyou.linguistic_network.gui;

import org.andyou.linguistic_network.lib.api.node.SWNode;
import org.andyou.linguistic_network.lib.util.SWNodeGraphUtil;
import org.andyou.linguistic_network.lib.util.TextTokenizerUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MainFrame extends JFrame {

    private static final String WARNING_MESSAGE_MISSING_STOP_WORDS = "You selected option \"Remove stop words\", but didn't select a file with stop words!";
    private static final String A = "You have not selected either the \"Sentence bounds\" or \"Use range\" options.\nIt means that all words will be neighbors!\nProcessing such a large number of connections can be time-consuming!\nDo you want to continue?";

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
    private JTextField elementCountTextField;
    private JTextField spentTimeTextField;
    private JProgressBar progressBar;
    private JButton terminateCalculationButton;

    private File textFile;
    private File stopWordsFile;
    private List<SWNode> swNodes;


    public MainFrame() {
        setContentPane(mainPanel);

        String[] columnIdentifiers = {"Rank", "Element", "Frequency", "NeighborsCount"};
        DefaultTableModel defaultTableModel = new DefaultTableModel(0, 4) {
            final Class<?>[] types = {Integer.class, String.class, Integer.class, Integer.class};

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

        TableRowSorter<TableModel> tableRowSorter = new TableRowSorter<>(statisticTable.getModel());
        tableRowSorter.setSortsOnUpdates(false);
        statisticTable.setRowSorter(tableRowSorter);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        List<TableColumn> tableColumns = Collections.list(statisticTable.getColumnModel().getColumns());
        tableColumns.forEach(tableColumn -> tableColumn.setCellRenderer(leftRenderer));

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

                defaultTableModel.setRowCount(0);
                swNodes = null;
                elementCountTextField.setText("");
                spentTimeTextField.setText("");
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

        Runnable task = () -> {
            try {
                elementCountTextField.setText("");
                spentTimeTextField.setText("");
                defaultTableModel.setRowCount(0);

                String text = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);

                boolean caseSensitive = caseSensitiveCheckBox.isSelected();
                boolean considerSentenceBounds = considerSentenceBoundsCheckBox.isSelected();
                boolean useRange = useRangeCheckBox.isSelected();
                int rangeSize = (int) rangeSizeSpinner.getValue();
                boolean removeStopWords = removeStopWordsCheckBox.isSelected();
                boolean filterByFrequency = filterByFrequencyCheckBox.isSelected();
                int filterFrequency = (int) filterFrequencySpinner.getValue();

                String[] stopWords = null;
                if (removeStopWords) {
                    String stopWordsText = new String(Files.readAllBytes(stopWordsFile.toPath()), StandardCharsets.UTF_8);
                    stopWords = TextTokenizerUtil.splitIntoWords(stopWordsText);
                }

                long startTime = System.currentTimeMillis();
                String[][] elementGroups = TextTokenizerUtil.createElementGroups(text, caseSensitive, considerSentenceBounds);

                if (removeStopWords && stopWords != null) {
                    elementGroups = TextTokenizerUtil.removeStopWords(elementGroups, stopWords);
                }

                Set<SWNode> swNodeGraph = SWNodeGraphUtil.createSWNodeGraph(elementGroups, useRange, rangeSize);

                if (filterByFrequency && filterFrequency > 0) {
                    SWNodeGraphUtil.filterByFrequency(swNodeGraph, filterFrequency);
                }
                long endTime = System.currentTimeMillis();

                swNodes = new ArrayList<>(swNodeGraph);
                swNodes.sort(Comparator.comparingInt(SWNode::getFrequency).reversed());

                elementCountTextField.setText(String.valueOf(swNodeGraph.size()));
                spentTimeTextField.setText(DurationFormatUtils.formatDuration(endTime - startTime, "HH:mm:ss.SSS", true));

                tableRowSorter.setSortKeys(null);
                for (int i = 0; i < swNodes.size(); i++) {
                    SWNode swNode = swNodes.get(i);
                    defaultTableModel.addRow(new Object[]{i + 1, swNode.getElement(), swNode.getFrequency(), swNode.getNeighbors().size()});
                }
            } catch (Exception ex) {
                showErrorMessageDialog(ex);
            } finally {
                openMenuItem.setEnabled(true);
                calculateButton.setEnabled(true);
                terminateCalculationButton.setEnabled(false);
            }
        };
        AtomicReference<Thread> atomicReference = new AtomicReference<>();

        calculateButton.addActionListener(e -> {
            boolean removeStopWords = removeStopWordsCheckBox.isSelected();
            if (removeStopWords && stopWordsFile == null) {
                showWarningMessageDialog(WARNING_MESSAGE_MISSING_STOP_WORDS);
                return;
            }

            boolean considerSentenceBounds = considerSentenceBoundsCheckBox.isSelected();
            boolean useRange = useRangeCheckBox.isSelected();
            if (!considerSentenceBounds && !useRange) {
                int choice = showConfirmDialog(A);
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            atomicReference.set(new Thread(task));
            atomicReference.get().start();

            openMenuItem.setEnabled(false);
            calculateButton.setEnabled(false);
            terminateCalculationButton.setEnabled(true);
        });

        terminateCalculationButton.addActionListener(e -> {
            if (atomicReference.get() != null) {
                atomicReference.get().stop();
            }

            openMenuItem.setEnabled(true);
            calculateButton.setEnabled(true);
            terminateCalculationButton.setEnabled(false);
        });

    }

    private void showErrorMessageDialog(Exception ex) {
        JOptionPane.showMessageDialog(this, ExceptionUtils.getStackTrace(ex), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private int showConfirmDialog(String message) {
        return JOptionPane.showConfirmDialog(null, message, "Confirmation", JOptionPane.YES_NO_OPTION);
    }

    private void createUIComponents() {
        rangeSizeSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 1000000000, 1));

        filterFrequencySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000000000, 1));

    }
}
