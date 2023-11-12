package org.andyou.linguistic_network.gui.frame;

import org.andyou.linguistic_network.gui.api.frame.SubFrame;
import org.andyou.linguistic_network.gui.util.CommonGUIUtil;
import org.andyou.linguistic_network.lib.api.constant.StopConditionType;
import org.andyou.linguistic_network.lib.api.context.KeywordExtractionTextRankContext;
import org.andyou.linguistic_network.lib.api.context.LinguisticNetworkContext;
import org.andyou.linguistic_network.lib.api.context.MainContext;
import org.andyou.linguistic_network.lib.api.node.ElementNode;
import org.andyou.linguistic_network.lib.api.node.TRNode;
import org.andyou.linguistic_network.lib.gui.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.util.CommonUtil;
import org.andyou.linguistic_network.lib.util.LinguisticNetworkUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class KeywordExtractionTextRankFrame extends JFrame implements SubFrame {

    private JPanel mainPanel;
    private JComboBox<StopConditionType> stopConditionComboBox;
    private JLabel accuracyLabel;
    private JFormattedTextField accuracyTextField;
    private JLabel iterationCountLabel;
    private JSpinner iterationCountSpinner;
    private JFormattedTextField dampingFactorTextField;
    private JTable statisticTable;
    private DefaultTableModel defaultTableModel;
    private JTextField accuracyAchievedTextField;
    private JTextField iterationsCompletedTextField;
    private JTextField spentTimeTextField;
    private JButton calculateButton;
    private JProgressBar progressBar;
    private JButton terminateCalculationButton;

    private MainContext mainContext;
    private KeywordExtractionTextRankContext keywordExtractionTextRankContext;
    private AtomicReference<Thread> threadAtomicReference;

    public KeywordExtractionTextRankFrame(LinguisticNetworkContext linguisticNetworkContext) {
        this.mainContext = linguisticNetworkContext.getMainContext();
        this.keywordExtractionTextRankContext = linguisticNetworkContext.getKeywordExtractionTextRankContext();

        $$$setupUI$$$();
        setTitle("Keyword extraction \"TextRank\"");
        setContentPane(mainPanel);
        initContext();

        threadAtomicReference = new AtomicReference<>();

        stopConditionComboBox.addActionListener(e -> {
            StopConditionType stopConditionType = (StopConditionType) stopConditionComboBox.getSelectedItem();
            keywordExtractionTextRankContext.setStopConditionType(stopConditionType);
            updateUI(true);
        });
        accuracyTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                keywordExtractionTextRankContext.setAccuracy((double) accuracyTextField.getValue());
            }
        });
        iterationCountSpinner.addChangeListener(e -> {
            keywordExtractionTextRankContext.setIterationCount((int) iterationCountSpinner.getValue());
        });
        dampingFactorTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                keywordExtractionTextRankContext.setDampingFactor((double) dampingFactorTextField.getValue());
            }
        });

        Runnable task = () -> {
            try {
                clearContext();
                updateUI();

                Set<ElementNode> elementNodeGraph = mainContext.getElementNodeGraph();
                boolean weightedGraph = mainContext.isWeightedGraph();

                StopConditionType stopConditionType = keywordExtractionTextRankContext.getStopConditionType();
                double accuracy = keywordExtractionTextRankContext.getAccuracy();
                int iterationCount = keywordExtractionTextRankContext.getIterationCount();
                double dampingFactor = keywordExtractionTextRankContext.getDampingFactor();

                ProgressBarProcessor progressBarProcessor = new ProgressBarProcessor(progressBar, Collections.singletonList(100));

                long startTime = System.currentTimeMillis();
                List<TRNode> trNodes = LinguisticNetworkUtil.calcKeywordStatisticsTextRank(elementNodeGraph, stopConditionType, accuracy, iterationCount, dampingFactor, weightedGraph, progressBarProcessor);
                progressBarProcessor.completed();
                long endTime = System.currentTimeMillis();

                keywordExtractionTextRankContext.setTrNodes(trNodes);
                keywordExtractionTextRankContext.setAccuracyAchieved(0.0);
                keywordExtractionTextRankContext.setIterationsCompleted(0);
                keywordExtractionTextRankContext.setSpentTime(endTime - startTime);

                trNodes.sort(Comparator.comparingDouble(TRNode::getImportance)
                        .thenComparing(swNode -> swNode.getElementNode().getNeighborCount())
                        .thenComparing(swNode -> swNode.getElementNode().getFrequency())
                        .thenComparing(swNode -> swNode.getElementNode().getElement())
                        .reversed());

                for (int i = 0; i < trNodes.size(); i++) {
                    TRNode trNode = trNodes.get(i);
                    int rank = i + 1;
                    SwingUtilities.invokeLater(() -> defaultTableModel.addRow(new Object[]{
                            rank,
                            trNode.getElementNode().getIndex(),
                            trNode.getElementNode().getElement(),
                            trNode.getElementNode().getFrequency(),
                            trNode.getElementNode().getNeighborCount(),
                            trNode.getImportance(),
                            trNode.getNormalizedImportance()
                    }));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                CommonGUIUtil.showErrorMessageDialog(this, ex);
            } finally {
                updateUI(false);
            }
        };

        calculateButton.addActionListener(e -> {
            threadAtomicReference.set(new Thread(task));
            threadAtomicReference.get().start();
        });

        terminateCalculationButton.addActionListener(e -> {
            if (threadAtomicReference.get() != null) {
                threadAtomicReference.get().stop();
            }
        });

        clearContext();
        updateUI();
    }


    @Override
    public void elementNodeGraphChanged() {
        clearContext();
        updateUI();
    }

    private void initContext() {
        keywordExtractionTextRankContext.setStopConditionType((StopConditionType) stopConditionComboBox.getSelectedItem());
        keywordExtractionTextRankContext.setAccuracy((double) accuracyTextField.getValue());
        keywordExtractionTextRankContext.setIterationCount((int) iterationCountSpinner.getValue());
        keywordExtractionTextRankContext.setDampingFactor((double) dampingFactorTextField.getValue());
    }

    @Override
    public void clearContext() {
        keywordExtractionTextRankContext.setTrNodes(null);
        keywordExtractionTextRankContext.setAccuracyAchieved(null);
        keywordExtractionTextRankContext.setIterationsCompleted(0);
        keywordExtractionTextRankContext.setSpentTime(0);
    }

    @Override
    public void updateUI() {
        Thread thread = threadAtomicReference.get();
        updateUI(thread != null && thread.isAlive());
    }

    synchronized public void updateUI(boolean calculationStarted) {
        stopConditionComboBox.setSelectedItem(keywordExtractionTextRankContext.getStopConditionType());

        accuracyLabel.setVisible(StopConditionType.ACCURACY.equals(keywordExtractionTextRankContext.getStopConditionType()));
        accuracyTextField.setVisible(StopConditionType.ACCURACY.equals(keywordExtractionTextRankContext.getStopConditionType()));

        iterationCountLabel.setVisible(StopConditionType.ITERATION_COUNT.equals(keywordExtractionTextRankContext.getStopConditionType()));
        iterationCountSpinner.setVisible(StopConditionType.ITERATION_COUNT.equals(keywordExtractionTextRankContext.getStopConditionType()));

        if (keywordExtractionTextRankContext.getAccuracyAchieved() == null) {
            accuracyAchievedTextField.setText("");
        } else {
            accuracyAchievedTextField.setText(CommonGUIUtil.DECIMAL_FORMAT.format(keywordExtractionTextRankContext.getAccuracyAchieved()));
        }
        iterationsCompletedTextField.setText(String.valueOf(keywordExtractionTextRankContext.getIterationsCompleted()));
        spentTimeTextField.setText(CommonUtil.formatDuration(keywordExtractionTextRankContext.getSpentTime()));

        if (keywordExtractionTextRankContext.getTrNodes() == null) {
            defaultTableModel.setRowCount(0);
        }

        calculateButton.setEnabled(!calculationStarted && mainContext.getElementNodeGraph() != null);
        terminateCalculationButton.setEnabled(calculationStarted);
    }

    private void createUIComponents() {
        StopConditionType[] stopConditionTypes = {StopConditionType.ACCURACY, StopConditionType.ITERATION_COUNT};
        stopConditionComboBox = new JComboBox<>(stopConditionTypes);

        accuracyTextField = new JFormattedTextField(CommonGUIUtil.DOUBLE_FORMATTER_FACTORY, 0.0001);

        iterationCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));

        dampingFactorTextField = new JFormattedTextField(CommonGUIUtil.DOUBLE_FORMATTER_FACTORY, 0.85);

        statisticTable = new JTable();
        String[] columnIdentifiers = {"Rank", "Index", "Element", "Frequency", "Neighbors count", "S", "S Norm."};
        defaultTableModel = new DefaultTableModel(0, 7) {
            final Class<?>[] types = {Integer.class, Integer.class, String.class, Integer.class, Integer.class, Double.class, Double.class};

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

        double[] columnWidth = {0.1, 0.1, 0.19, 0.12, 0.17, 0.16, 0.16};

        CommonGUIUtil.setTableColumnWidth(this, statisticTable, columnWidth);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Double) {
                    value = CommonGUIUtil.DECIMAL_FORMAT.format(value);
                }

                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        List<TableColumn> tableColumns = Collections.list(statisticTable.getColumnModel().getColumns());
        tableColumns.forEach(tableColumn -> tableColumn.setCellRenderer(renderer));
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
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(5, 0));
        mainPanel.add(panel1, BorderLayout.CENTER);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, BorderLayout.WEST);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        panel2.add(panel3, BorderLayout.CENTER);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel spacer1 = new JPanel();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel3.add(spacer1, gbc);
        calculateButton = new JButton();
        calculateButton.setEnabled(true);
        Font calculateButtonFont = this.$$$getFont$$$(null, -1, 14, calculateButton.getFont());
        if (calculateButtonFont != null) calculateButton.setFont(calculateButtonFont);
        calculateButton.setText("Calculate");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel3.add(calculateButton, gbc);
        Font stopConditionComboBoxFont = this.$$$getFont$$$(null, -1, 14, stopConditionComboBox.getFont());
        if (stopConditionComboBoxFont != null) stopConditionComboBox.setFont(stopConditionComboBoxFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel3.add(stopConditionComboBox, gbc);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 14, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Stop condition");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel3.add(label1, gbc);
        iterationCountLabel = new JLabel();
        Font iterationCountLabelFont = this.$$$getFont$$$(null, -1, 14, iterationCountLabel.getFont());
        if (iterationCountLabelFont != null) iterationCountLabel.setFont(iterationCountLabelFont);
        iterationCountLabel.setText("Iteration count");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel3.add(iterationCountLabel, gbc);
        Font iterationCountSpinnerFont = this.$$$getFont$$$(null, -1, 14, iterationCountSpinner.getFont());
        if (iterationCountSpinnerFont != null) iterationCountSpinner.setFont(iterationCountSpinnerFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel3.add(iterationCountSpinner, gbc);
        accuracyLabel = new JLabel();
        Font accuracyLabelFont = this.$$$getFont$$$(null, -1, 14, accuracyLabel.getFont());
        if (accuracyLabelFont != null) accuracyLabel.setFont(accuracyLabelFont);
        accuracyLabel.setText("Accuracy");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel3.add(accuracyLabel, gbc);
        Font accuracyTextFieldFont = this.$$$getFont$$$(null, -1, 14, accuracyTextField.getFont());
        if (accuracyTextFieldFont != null) accuracyTextField.setFont(accuracyTextFieldFont);
        accuracyTextField.setHorizontalAlignment(4);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel3.add(accuracyTextField, gbc);
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, -1, 14, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("Damping factor");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel3.add(label2, gbc);
        Font dampingFactorTextFieldFont = this.$$$getFont$$$(null, -1, 14, dampingFactorTextField.getFont());
        if (dampingFactorTextFieldFont != null) dampingFactorTextField.setFont(dampingFactorTextFieldFont);
        dampingFactorTextField.setHorizontalAlignment(4);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel3.add(dampingFactorTextField, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel1.add(panel4, BorderLayout.CENTER);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        panel4.add(panel5, BorderLayout.SOUTH);
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, -1, 14, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("Spent time");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel5.add(label3, gbc);
        spentTimeTextField = new JTextField();
        spentTimeTextField.setColumns(10);
        spentTimeTextField.setEditable(false);
        Font spentTimeTextFieldFont = this.$$$getFont$$$(null, -1, 14, spentTimeTextField.getFont());
        if (spentTimeTextFieldFont != null) spentTimeTextField.setFont(spentTimeTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(spentTimeTextField, gbc);
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, -1, 14, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("Iterations completed");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel5.add(label4, gbc);
        iterationsCompletedTextField = new JTextField();
        iterationsCompletedTextField.setColumns(10);
        iterationsCompletedTextField.setEditable(false);
        Font iterationsCompletedTextFieldFont = this.$$$getFont$$$(null, -1, 14, iterationsCompletedTextField.getFont());
        if (iterationsCompletedTextFieldFont != null)
            iterationsCompletedTextField.setFont(iterationsCompletedTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(iterationsCompletedTextField, gbc);
        final JLabel label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$(null, -1, 14, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setText("Accuracy achieved");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel5.add(label5, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(spacer2, gbc);
        accuracyAchievedTextField = new JTextField();
        accuracyAchievedTextField.setColumns(10);
        accuracyAchievedTextField.setEditable(false);
        Font accuracyAchievedTextFieldFont = this.$$$getFont$$$(null, -1, 14, accuracyAchievedTextField.getFont());
        if (accuracyAchievedTextFieldFont != null) accuracyAchievedTextField.setFont(accuracyAchievedTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(accuracyAchievedTextField, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel4.add(scrollPane1, BorderLayout.CENTER);
        statisticTable.setAutoCreateRowSorter(true);
        statisticTable.setCellSelectionEnabled(true);
        Font statisticTableFont = this.$$$getFont$$$(null, -1, 14, statisticTable.getFont());
        if (statisticTableFont != null) statisticTable.setFont(statisticTableFont);
        scrollPane1.setViewportView(statisticTable);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(5, 0));
        mainPanel.add(panel6, BorderLayout.SOUTH);
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        progressBar = new JProgressBar();
        progressBar.setMaximum(1000);
        panel6.add(progressBar, BorderLayout.CENTER);
        terminateCalculationButton = new JButton();
        terminateCalculationButton.setEnabled(true);
        terminateCalculationButton.setIcon(new ImageIcon(getClass().getResource("/icon/terminatedlaunchIcon.png")));
        terminateCalculationButton.setPreferredSize(new Dimension(30, 30));
        terminateCalculationButton.setText("");
        panel6.add(terminateCalculationButton, BorderLayout.EAST);
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
