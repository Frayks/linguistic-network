package org.andyou.linguistic_network.gui;

import org.andyou.linguistic_network.lib.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.api.node.SWNode;
import org.andyou.linguistic_network.lib.util.SWNodeGraphUtil;
import org.andyou.linguistic_network.lib.util.TextTokenizerUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.*;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MainFrame extends JFrame {

    private static final String WARNING_MESSAGE_MISSING_STOP_WORDS = "You selected option \"Remove stop words\", but didn't select a file with stop words!";
    private static final String A = "You have not selected either the \"Sentence bounds\" or \"Use range\" options.\nIt means that all words will be considered neighbors!\nProcessing such a large number of connections can be time-consuming!\nDo you want to continue?";

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
        $$$setupUI$$$();
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

                List<Integer> blockSizes = new ArrayList<>();
                blockSizes.add(1);
                if (removeStopWords) {
                    blockSizes.add(5);
                }
                blockSizes.add(92);
                if (filterByFrequency) {
                    blockSizes.add(2);
                }
                ProgressBarProcessor progressBarProcessor = new ProgressBarProcessor(progressBar, blockSizes);


                long startTime = System.currentTimeMillis();
                String[][] elementGroups = TextTokenizerUtil.createElementGroups(text, caseSensitive, considerSentenceBounds);
                progressBarProcessor.initAndFinishNextBlock();

                if (removeStopWords) {
                    String stopWordsText = new String(Files.readAllBytes(stopWordsFile.toPath()), StandardCharsets.UTF_8);
                    String[] stopWords = TextTokenizerUtil.splitIntoWords(stopWordsText);
                    elementGroups = TextTokenizerUtil.removeStopWords(elementGroups, stopWords, progressBarProcessor);
                }

                Set<SWNode> swNodeGraph = SWNodeGraphUtil.createSWNodeGraph(elementGroups, useRange, rangeSize, progressBarProcessor);

                if (filterByFrequency) {
                    SWNodeGraphUtil.filterByFrequency(swNodeGraph, filterFrequency);
                    progressBarProcessor.initAndFinishNextBlock();
                }
                long endTime = System.currentTimeMillis();

                elementCountTextField.setText(String.valueOf(swNodeGraph.size()));
                spentTimeTextField.setText(DurationFormatUtils.formatDuration(endTime - startTime, "HH:mm:ss.SSS", true));

                swNodes = new ArrayList<>(swNodeGraph);
                swNodes.sort(Comparator.comparingInt(SWNode::getFrequency).reversed());
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
            progressBar.setValue(0);
        });

    }

    private void showErrorMessageDialog(Exception ex) {
        ex.printStackTrace();
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

        filterFrequencySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));

    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        final JMenuBar menuBar1 = new JMenuBar();
        menuBar1.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        mainPanel.add(menuBar1, BorderLayout.NORTH);
        fileMenu = new JMenu();
        fileMenu.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        Font fileMenuFont = this.$$$getFont$$$(null, -1, 14, fileMenu.getFont());
        if (fileMenuFont != null) fileMenu.setFont(fileMenuFont);
        fileMenu.setSelected(false);
        fileMenu.setText("File");
        menuBar1.add(fileMenu);
        openMenuItem = new JMenuItem();
        openMenuItem.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/file.gif")));
        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);
        saveMenuItem = new JMenuItem();
        saveMenuItem.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/floppy.gif")));
        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        mainPanel.add(panel1, BorderLayout.CENTER);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(5, 0));
        panel1.add(panel2, BorderLayout.NORTH);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 14, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Selected file");
        panel2.add(label1, BorderLayout.WEST);
        textFileTextField = new JTextField();
        textFileTextField.setEditable(false);
        Font textFileTextFieldFont = this.$$$getFont$$$(null, -1, 14, textFileTextField.getFont());
        if (textFileTextFieldFont != null) textFileTextField.setFont(textFileTextFieldFont);
        panel2.add(textFileTextField, BorderLayout.CENTER);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(5, 0));
        panel1.add(panel3, BorderLayout.CENTER);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel3.add(panel4, BorderLayout.WEST);
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        panel4.add(panel5, BorderLayout.CENTER);
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        caseSensitiveCheckBox = new JCheckBox();
        Font caseSensitiveCheckBoxFont = this.$$$getFont$$$(null, -1, 14, caseSensitiveCheckBox.getFont());
        if (caseSensitiveCheckBoxFont != null) caseSensitiveCheckBox.setFont(caseSensitiveCheckBoxFont);
        caseSensitiveCheckBox.setSelected(true);
        caseSensitiveCheckBox.setText("Case sensitive");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(caseSensitiveCheckBox, gbc);
        considerSentenceBoundsCheckBox = new JCheckBox();
        considerSentenceBoundsCheckBox.setEnabled(true);
        Font considerSentenceBoundsCheckBoxFont = this.$$$getFont$$$(null, -1, 14, considerSentenceBoundsCheckBox.getFont());
        if (considerSentenceBoundsCheckBoxFont != null) considerSentenceBoundsCheckBox.setFont(considerSentenceBoundsCheckBoxFont);
        considerSentenceBoundsCheckBox.setText("Sentence bounds");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(considerSentenceBoundsCheckBox, gbc);
        useRangeCheckBox = new JCheckBox();
        Font useRangeCheckBoxFont = this.$$$getFont$$$(null, -1, 14, useRangeCheckBox.getFont());
        if (useRangeCheckBoxFont != null) useRangeCheckBox.setFont(useRangeCheckBoxFont);
        useRangeCheckBox.setSelected(true);
        useRangeCheckBox.setText("Use range");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(useRangeCheckBox, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(5, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(panel6, gbc);
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rangeLabel = new JLabel();
        Font rangeLabelFont = this.$$$getFont$$$(null, -1, 14, rangeLabel.getFont());
        if (rangeLabelFont != null) rangeLabel.setFont(rangeLabelFont);
        rangeLabel.setText("Range");
        panel6.add(rangeLabel, BorderLayout.WEST);
        rangeSizeSpinner.setEnabled(true);
        Font rangeSizeSpinnerFont = this.$$$getFont$$$(null, -1, 14, rangeSizeSpinner.getFont());
        if (rangeSizeSpinnerFont != null) rangeSizeSpinner.setFont(rangeSizeSpinnerFont);
        panel6.add(rangeSizeSpinner, BorderLayout.CENTER);
        removeStopWordsCheckBox = new JCheckBox();
        Font removeStopWordsCheckBoxFont = this.$$$getFont$$$(null, -1, 14, removeStopWordsCheckBox.getFont());
        if (removeStopWordsCheckBoxFont != null) removeStopWordsCheckBox.setFont(removeStopWordsCheckBoxFont);
        removeStopWordsCheckBox.setText("Remove stop words");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(removeStopWordsCheckBox, gbc);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new BorderLayout(5, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(panel7, gbc);
        panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        stopWordsFileTextField = new JTextField();
        stopWordsFileTextField.setColumns(10);
        stopWordsFileTextField.setEditable(false);
        stopWordsFileTextField.setEnabled(false);
        Font stopWordsFileTextFieldFont = this.$$$getFont$$$(null, -1, 14, stopWordsFileTextField.getFont());
        if (stopWordsFileTextFieldFont != null) stopWordsFileTextField.setFont(stopWordsFileTextFieldFont);
        panel7.add(stopWordsFileTextField, BorderLayout.CENTER);
        chooseStopWordsFileButton = new JButton();
        chooseStopWordsFileButton.setEnabled(false);
        Font chooseStopWordsFileButtonFont = this.$$$getFont$$$(null, -1, 14, chooseStopWordsFileButton.getFont());
        if (chooseStopWordsFileButtonFont != null) chooseStopWordsFileButton.setFont(chooseStopWordsFileButtonFont);
        chooseStopWordsFileButton.setText("File");
        panel7.add(chooseStopWordsFileButton, BorderLayout.EAST);
        filterByFrequencyCheckBox = new JCheckBox();
        filterByFrequencyCheckBox.setEnabled(true);
        Font filterByFrequencyCheckBoxFont = this.$$$getFont$$$(null, -1, 14, filterByFrequencyCheckBox.getFont());
        if (filterByFrequencyCheckBoxFont != null) filterByFrequencyCheckBox.setFont(filterByFrequencyCheckBoxFont);
        filterByFrequencyCheckBox.setText("Filter by frequency");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(filterByFrequencyCheckBox, gbc);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new BorderLayout(5, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(panel8, gbc);
        panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        frequencyLabel = new JLabel();
        frequencyLabel.setEnabled(false);
        Font frequencyLabelFont = this.$$$getFont$$$(null, -1, 14, frequencyLabel.getFont());
        if (frequencyLabelFont != null) frequencyLabel.setFont(frequencyLabelFont);
        frequencyLabel.setText("Frequency");
        panel8.add(frequencyLabel, BorderLayout.WEST);
        filterFrequencySpinner.setEnabled(false);
        Font filterFrequencySpinnerFont = this.$$$getFont$$$(null, -1, 14, filterFrequencySpinner.getFont());
        if (filterFrequencySpinnerFont != null) filterFrequencySpinner.setFont(filterFrequencySpinnerFont);
        panel8.add(filterFrequencySpinner, BorderLayout.CENTER);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel5.add(spacer1, gbc);
        calculateButton = new JButton();
        calculateButton.setEnabled(false);
        Font calculateButtonFont = this.$$$getFont$$$(null, -1, 14, calculateButton.getFont());
        if (calculateButtonFont != null) calculateButton.setFont(calculateButtonFont);
        calculateButton.setText("Calculate");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        panel5.add(calculateButton, gbc);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new BorderLayout(0, 0));
        panel3.add(panel9, BorderLayout.CENTER);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridBagLayout());
        panel9.add(panel10, BorderLayout.SOUTH);
        panel10.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label2 = new JLabel();
        label2.setText("Element count");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel10.add(label2, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(spacer2, gbc);
        elementCountTextField = new JTextField();
        elementCountTextField.setColumns(10);
        elementCountTextField.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel10.add(elementCountTextField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Spent time");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel10.add(label3, gbc);
        spentTimeTextField = new JTextField();
        spentTimeTextField.setColumns(10);
        spentTimeTextField.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(spentTimeTextField, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel9.add(scrollPane1, BorderLayout.CENTER);
        statisticTable = new JTable();
        statisticTable.setAutoCreateRowSorter(true);
        statisticTable.setCellSelectionEnabled(true);
        scrollPane1.setViewportView(statisticTable);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new BorderLayout(5, 0));
        panel1.add(panel11, BorderLayout.SOUTH);
        panel11.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        progressBar = new JProgressBar();
        progressBar.setMaximum(1000);
        panel11.add(progressBar, BorderLayout.CENTER);
        terminateCalculationButton = new JButton();
        terminateCalculationButton.setEnabled(false);
        terminateCalculationButton.setIcon(new ImageIcon(getClass().getResource("/icon/terminatedlaunchIcon.png")));
        terminateCalculationButton.setPreferredSize(new Dimension(30, 30));
        terminateCalculationButton.setText("");
        panel11.add(terminateCalculationButton, BorderLayout.EAST);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
