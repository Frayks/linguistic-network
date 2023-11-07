package org.andyou.linguistic_network.gui.frame;

import org.andyou.linguistic_network.gui.api.frame.SubFrame;
import org.andyou.linguistic_network.gui.util.CommonGUIUtil;
import org.andyou.linguistic_network.lib.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.api.context.LinguisticMetricsContext;
import org.andyou.linguistic_network.lib.api.context.LinguisticNetworkContext;
import org.andyou.linguistic_network.lib.api.context.MainContext;
import org.andyou.linguistic_network.lib.api.node.CDFNode;
import org.andyou.linguistic_network.lib.api.node.ElementNode;
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
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class LinguisticMetricsFrame extends JFrame implements SubFrame {

    private JPanel mainPanel;
    private JTable statisticTable;
    private DefaultTableModel defaultTableModel;
    private JButton calculateButton;
    private JProgressBar progressBar;
    private JButton terminateCalculationButton;
    private JTextField spentTimeTextField;

    private MainContext mainContext;
    private LinguisticMetricsContext linguisticMetricsContext;
    private AtomicReference<Thread> threadAtomicReference;

    public LinguisticMetricsFrame(LinguisticNetworkContext linguisticNetworkContext) {
        this.mainContext = linguisticNetworkContext.getMainContext();
        this.linguisticMetricsContext = linguisticNetworkContext.getLinguisticMetricsContext();

        $$$setupUI$$$();
        setTitle("Linguistic Metrics");
        setContentPane(mainPanel);

        threadAtomicReference = new AtomicReference<>();

        Runnable task = () -> {
            try {
                clearContext();
                updateUI();

                Set<ElementNode> elementNodeGraph = mainContext.getElementNodeGraph();

                ProgressBarProcessor progressBarProcessor = new ProgressBarProcessor(progressBar, Arrays.asList(1, 13, 85, 1));

                long startTime = System.currentTimeMillis();
                List<CDFNode> cdfNodes = LinguisticNetworkUtil.calcCDFNodes(elementNodeGraph, progressBarProcessor);

                progressBarProcessor.initAndFinishNextBlock();
                progressBarProcessor.completed();
                long endTime = System.currentTimeMillis();

                linguisticMetricsContext.setCdfNodes(cdfNodes);
                linguisticMetricsContext.setSpentTime(endTime - startTime);

                for (CDFNode cdfNode : cdfNodes) {
                    SwingUtilities.invokeLater(() -> defaultTableModel.addRow(new Object[]{cdfNode.getK(), cdfNode.getN(), cdfNode.getPdf(), cdfNode.getCdf()}));
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

    @Override
    public void clearContext() {
        linguisticMetricsContext.setCdfNodes(null);
        linguisticMetricsContext.setSpentTime(0);
    }

    @Override
    public void updateUI() {
        Thread thread = threadAtomicReference.get();
        updateUI(thread != null && thread.isAlive());
    }

    synchronized public void updateUI(boolean calculationStarted) {
        spentTimeTextField.setText(CommonUtil.formatDuration(linguisticMetricsContext.getSpentTime()));

        if (linguisticMetricsContext.getCdfNodes() == null) {
            defaultTableModel.setRowCount(0);
        }

        calculateButton.setEnabled(!calculationStarted && mainContext.getElementNodeGraph() != null);
        terminateCalculationButton.setEnabled(calculationStarted);
    }

    private void createUIComponents() {
        statisticTable = new JTable();

        String[] columnIdentifiers = {"K", "N", "PDF", "CDF"};
        defaultTableModel = new DefaultTableModel(0, 4) {
            final Class<?>[] types = {Integer.class, Integer.class, Double.class, Double.class};

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
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, BorderLayout.CENTER);
        statisticTable.setAutoCreateRowSorter(true);
        statisticTable.setCellSelectionEnabled(true);
        Font statisticTableFont = this.$$$getFont$$$(null, -1, 14, statisticTable.getFont());
        if (statisticTableFont != null) statisticTable.setFont(statisticTableFont);
        scrollPane1.setViewportView(statisticTable);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        mainPanel.add(panel1, BorderLayout.SOUTH);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        calculateButton = new JButton();
        Font calculateButtonFont = this.$$$getFont$$$(null, -1, 14, calculateButton.getFont());
        if (calculateButtonFont != null) calculateButton.setFont(calculateButtonFont);
        calculateButton.setText("Calculate");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel1.add(calculateButton, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(5, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        progressBar = new JProgressBar();
        progressBar.setMaximum(1000);
        panel2.add(progressBar, BorderLayout.CENTER);
        terminateCalculationButton = new JButton();
        terminateCalculationButton.setIcon(new ImageIcon(getClass().getResource("/icon/terminatedlaunchIcon.png")));
        terminateCalculationButton.setPreferredSize(new Dimension(30, 30));
        terminateCalculationButton.setText("");
        panel2.add(terminateCalculationButton, BorderLayout.EAST);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 14, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Spent time");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel1.add(label1, gbc);
        spentTimeTextField = new JTextField();
        spentTimeTextField.setEditable(false);
        Font spentTimeTextFieldFont = this.$$$getFont$$$(null, -1, 14, spentTimeTextField.getFont());
        if (spentTimeTextFieldFont != null) spentTimeTextField.setFont(spentTimeTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel1.add(spentTimeTextField, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer1, gbc);
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
