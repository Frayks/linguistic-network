package org.andyou.linguistic_network.gui;

import org.andyou.linguistic_network.gui.api.constant.FrameKey;
import org.andyou.linguistic_network.gui.api.constant.MessageText;
import org.andyou.linguistic_network.gui.api.frame.SubFrame;
import org.andyou.linguistic_network.gui.util.CommonGUIUtil;
import org.andyou.linguistic_network.lib.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.api.context.LinguisticNetworkContext;
import org.andyou.linguistic_network.lib.api.context.MainContext;
import org.andyou.linguistic_network.lib.api.node.SWNode;
import org.andyou.linguistic_network.lib.util.CommonUtil;
import org.andyou.linguistic_network.lib.util.SWNodeGraphUtil;
import org.andyou.linguistic_network.lib.util.TextTokenizerUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.*;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MainFrame extends JFrame {

    private JPanel mainPanel;
    private JMenuItem saveMenuItem;
    private JMenuItem openMenuItem;
    private JMenuItem smallWorldMenuItem;
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
    private DefaultTableModel defaultTableModel;
    private TableRowSorter<TableModel> tableRowSorter;
    private JTextField elementCountTextField;
    private JTextField spentTimeTextField;
    private JProgressBar progressBar;
    private JButton terminateCalculationButton;

    private Map<FrameKey, JFrame> subFrameMap;
    private AtomicReference<Thread> threadAtomicReference;
    private LinguisticNetworkContext linguisticNetworkContext;
    private MainContext mainContext;

    public MainFrame() {
        $$$setupUI$$$();
        setContentPane(mainPanel);

        subFrameMap = new HashMap<>();
        threadAtomicReference = new AtomicReference<>();
        initContext();

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Normal text file (*.txt)", "txt"));

        Font font = new JLabel().getFont().deriveFont(14f);
        setComponentsFont(jFileChooser.getComponents(), font);

        caseSensitiveCheckBox.addActionListener(e -> {
            mainContext.setCaseSensitive(caseSensitiveCheckBox.isSelected());
        });
        considerSentenceBoundsCheckBox.addActionListener(e -> {
            mainContext.setConsiderSentenceBounds(considerSentenceBoundsCheckBox.isSelected());
        });
        useRangeCheckBox.addActionListener(e -> {
            mainContext.setUseRange(useRangeCheckBox.isSelected());
            updateUI();
        });
        rangeSizeSpinner.addChangeListener(e -> {
            mainContext.setRangeSize((int) rangeSizeSpinner.getValue());
        });
        removeStopWordsCheckBox.addActionListener(e -> {
            mainContext.setRemoveStopWords(removeStopWordsCheckBox.isSelected());
            updateUI();
        });
        chooseStopWordsFileButton.addActionListener(e -> {
            int returnValue = jFileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                mainContext.setStopWordsFile(jFileChooser.getSelectedFile());
                updateUI();
            }
        });
        filterByFrequencyCheckBox.addActionListener(e -> {
            mainContext.setFilterByFrequency(filterByFrequencyCheckBox.isSelected());
            updateUI();
        });
        filterFrequencySpinner.addChangeListener(e -> {
            mainContext.setFilterFrequency((int) filterFrequencySpinner.getValue());
        });
        openMenuItem.addActionListener(e -> {
            int returnValue = jFileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                clearContext();
                mainContext.setTextFile(jFileChooser.getSelectedFile());
                updateUI();
            }
        });
        smallWorldMenuItem.addActionListener(e -> {
            if (subFrameMap.containsKey(FrameKey.SMALL_WORLD)) {
                return;
            }
            SmallWorldFrame smallWorldFrame = new SmallWorldFrame(linguisticNetworkContext);
            smallWorldFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    subFrameMap.remove(FrameKey.SMALL_WORLD);
                }
            });
            configureDefaultSubFrame(smallWorldFrame, "Linguistic network (Small-world)", 500, 600);
            subFrameMap.put(FrameKey.SMALL_WORLD, smallWorldFrame);
        });

        Runnable task = () -> {
            try {
                mainContext.setSwNodeGraph(null);
                mainContext.setSpentTime(0);
                updateUI();

                File textFile = mainContext.getTextFile();
                File stopWordsFile = mainContext.getStopWordsFile();
                boolean caseSensitive = mainContext.isCaseSensitive();
                boolean considerSentenceBounds = mainContext.isConsiderSentenceBounds();
                boolean useRange = mainContext.isUseRange();
                int rangeSize = mainContext.getRangeSize();
                boolean removeStopWords = mainContext.isRemoveStopWords();
                boolean filterByFrequency = mainContext.isFilterByFrequency();
                int filterFrequency = mainContext.getFilterFrequency();

                String text = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);

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
                progressBarProcessor.completed();
                long endTime = System.currentTimeMillis();

                mainContext.setSwNodeGraph(swNodeGraph);
                mainContext.setSpentTime(endTime - startTime);

                List<SWNode> swNodes = new ArrayList<>(swNodeGraph);
                swNodes.sort(Comparator.comparingInt(SWNode::getFrequency)
                        .thenComparing(SWNode::getNeighborCount)
                        .reversed());
                tableRowSorter.setSortKeys(null);
                for (int i = 0; i < swNodes.size(); i++) {
                    SWNode swNode = swNodes.get(i);
                    defaultTableModel.addRow(new Object[]{i + 1, swNode.getElement(), swNode.getFrequency(), swNode.getNeighborCount()});
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                CommonGUIUtil.showErrorMessageDialog(this, ex);
            } finally {
                updateUI(false);
                swNodeGraphChanged();
            }
        };

        calculateButton.addActionListener(e -> {
            if (mainContext.isRemoveStopWords() && mainContext.getStopWordsFile() == null) {
                CommonGUIUtil.showWarningMessageDialog(this, MessageText.WARNING_MESSAGE_MISSING_STOP_WORDS);
                return;
            }

            if (!mainContext.isConsiderSentenceBounds() && !mainContext.isUseRange()) {
                int choice = CommonGUIUtil.showConfirmDialog(this, MessageText.WARNING_MESSAGE_NOT_SELECTED_OPTIONS);
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            threadAtomicReference.set(new Thread(task));
            threadAtomicReference.get().start();
        });

        terminateCalculationButton.addActionListener(e -> {
            if (threadAtomicReference.get() != null) {
                threadAtomicReference.get().stop();
            }
        });

        updateUI();
    }

    private void setComponentsFont(Component[] comp, Font font) {
        for (Component component : comp) {
            if (component instanceof Container) {
                setComponentsFont(((Container) component).getComponents(), font);
            }
            try {
                component.setFont(font);
            } catch (Exception ignored) {
            }
        }
    }

    private void initContext() {
        linguisticNetworkContext = new LinguisticNetworkContext();
        mainContext = linguisticNetworkContext.getMainContext();
        mainContext.setCaseSensitive(caseSensitiveCheckBox.isSelected());
        mainContext.setConsiderSentenceBounds(considerSentenceBoundsCheckBox.isSelected());
        mainContext.setUseRange(useRangeCheckBox.isSelected());
        mainContext.setRangeSize((int) rangeSizeSpinner.getValue());
        mainContext.setRemoveStopWords(removeStopWordsCheckBox.isSelected());
        mainContext.setFilterByFrequency(filterByFrequencyCheckBox.isSelected());
        mainContext.setFilterFrequency((int) filterFrequencySpinner.getValue());
    }

    private void swNodeGraphChanged() {
        for (JFrame subFrame : subFrameMap.values()) {
            if (subFrame instanceof SubFrame) {
                ((SubFrame) subFrame).swNodeGraphChanged();
            }
        }
    }

    private void clearContext() {
        mainContext.setTextFile(null);
        mainContext.setSwNodeGraph(null);
        mainContext.setSpentTime(0);
        for (JFrame subFrame : subFrameMap.values()) {
            if (subFrame instanceof SubFrame) {
                ((SubFrame) subFrame).clearContext();
            }
        }
    }

    private void updateUI() {
        Thread thread = threadAtomicReference.get();
        updateUI(thread != null && thread.isAlive());
    }

    synchronized private void updateUI(boolean calculationStarted) {
        if (mainContext.getTextFile() == null) {
            textFileTextField.setText("");
        } else {
            String absolutePath = mainContext.getTextFile().getAbsolutePath();
            if (!textFileTextField.getText().equals(absolutePath)) {
                textFileTextField.setText(absolutePath);
            }
        }

        rangeLabel.setEnabled(mainContext.isUseRange());
        rangeSizeSpinner.setEnabled(mainContext.isUseRange());

        stopWordsFileTextField.setEnabled(mainContext.isRemoveStopWords());
        chooseStopWordsFileButton.setEnabled(mainContext.isRemoveStopWords());
        if (mainContext.getStopWordsFile() == null) {
            stopWordsFileTextField.setText("");
        } else {
            String absolutePath = mainContext.getStopWordsFile().getAbsolutePath();
            if (!stopWordsFileTextField.getText().equals(absolutePath)) {
                stopWordsFileTextField.setText(absolutePath);
            }
        }

        frequencyLabel.setEnabled(mainContext.isFilterByFrequency());
        filterFrequencySpinner.setEnabled(mainContext.isFilterByFrequency());

        if (mainContext.getSwNodeGraph() == null) {
            elementCountTextField.setText("0");
        } else {
            elementCountTextField.setText(String.valueOf(mainContext.getSwNodeGraph().size()));
        }

        spentTimeTextField.setText(CommonUtil.formatDuration(mainContext.getSpentTime()));

        if (mainContext.getSwNodeGraph() == null) {
            defaultTableModel.setRowCount(0);
        }

        openMenuItem.setEnabled(!calculationStarted);
        saveMenuItem.setEnabled(!calculationStarted && mainContext.getSwNodeGraph() != null);
        calculateButton.setEnabled(!calculationStarted && mainContext.getTextFile() != null);
        terminateCalculationButton.setEnabled(calculationStarted);

        for (JFrame subFrame : subFrameMap.values()) {
            if (subFrame instanceof SubFrame) {
                ((SubFrame) subFrame).updateUI();
            }
        }
    }


    private void configureDefaultSubFrame(JFrame frame, String title, int width, int height) {
        frame.setTitle(title);
        frame.setIconImage(new ImageIcon(getClass().getResource("/icon/networkIcon.png")).getImage());
        frame.setMinimumSize(new Dimension(width, height));
        frame.setPreferredSize(new Dimension(width, height));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUIComponents() {
        rangeSizeSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 1000000000, 1));

        filterFrequencySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));

        statisticTable = new JTable();

        String[] columnIdentifiers = {"Rank", "Element", "Frequency", "NeighborsCount"};
        defaultTableModel = new DefaultTableModel(0, 4) {
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

        tableRowSorter = new TableRowSorter<>(statisticTable.getModel());
        tableRowSorter.setSortsOnUpdates(false);
        statisticTable.setRowSorter(tableRowSorter);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        List<TableColumn> tableColumns = Collections.list(statisticTable.getColumnModel().getColumns());
        tableColumns.forEach(tableColumn -> tableColumn.setCellRenderer(leftRenderer));
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
        final JMenu menu1 = new JMenu();
        menu1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        Font menu1Font = this.$$$getFont$$$(null, -1, 14, menu1.getFont());
        if (menu1Font != null) menu1.setFont(menu1Font);
        menu1.setSelected(false);
        menu1.setText("File");
        menuBar1.add(menu1);
        openMenuItem = new JMenuItem();
        openMenuItem.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/file.gif")));
        openMenuItem.setText("Open");
        menu1.add(openMenuItem);
        saveMenuItem = new JMenuItem();
        saveMenuItem.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/floppy.gif")));
        saveMenuItem.setText("Save");
        menu1.add(saveMenuItem);
        final JMenu menu2 = new JMenu();
        menu2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        Font menu2Font = this.$$$getFont$$$(null, -1, 14, menu2.getFont());
        if (menu2Font != null) menu2.setFont(menu2Font);
        menu2.setText("Analysis");
        menuBar1.add(menu2);
        smallWorldMenuItem = new JMenuItem();
        Font smallWorldMenuItemFont = this.$$$getFont$$$(null, -1, 14, smallWorldMenuItem.getFont());
        if (smallWorldMenuItemFont != null) smallWorldMenuItem.setFont(smallWorldMenuItemFont);
        smallWorldMenuItem.setText("SmallWorld");
        menu2.add(smallWorldMenuItem);
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
        caseSensitiveCheckBox.setEnabled(true);
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
        useRangeCheckBox.setEnabled(true);
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
        stopWordsFileTextField.setEnabled(true);
        Font stopWordsFileTextFieldFont = this.$$$getFont$$$(null, -1, 14, stopWordsFileTextField.getFont());
        if (stopWordsFileTextFieldFont != null) stopWordsFileTextField.setFont(stopWordsFileTextFieldFont);
        panel7.add(stopWordsFileTextField, BorderLayout.CENTER);
        chooseStopWordsFileButton = new JButton();
        chooseStopWordsFileButton.setEnabled(true);
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
        frequencyLabel.setEnabled(true);
        Font frequencyLabelFont = this.$$$getFont$$$(null, -1, 14, frequencyLabel.getFont());
        if (frequencyLabelFont != null) frequencyLabel.setFont(frequencyLabelFont);
        frequencyLabel.setText("Frequency");
        panel8.add(frequencyLabel, BorderLayout.WEST);
        filterFrequencySpinner.setEnabled(true);
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
        calculateButton.setEnabled(true);
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
        Font label2Font = this.$$$getFont$$$(null, -1, 14, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
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
        Font elementCountTextFieldFont = this.$$$getFont$$$(null, -1, 14, elementCountTextField.getFont());
        if (elementCountTextFieldFont != null) elementCountTextField.setFont(elementCountTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel10.add(elementCountTextField, gbc);
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, -1, 14, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
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
        Font spentTimeTextFieldFont = this.$$$getFont$$$(null, -1, 14, spentTimeTextField.getFont());
        if (spentTimeTextFieldFont != null) spentTimeTextField.setFont(spentTimeTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel10.add(spentTimeTextField, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel9.add(scrollPane1, BorderLayout.CENTER);
        statisticTable.setAutoCreateRowSorter(true);
        statisticTable.setCellSelectionEnabled(true);
        Font statisticTableFont = this.$$$getFont$$$(null, -1, 14, statisticTable.getFont());
        if (statisticTableFont != null) statisticTable.setFont(statisticTableFont);
        scrollPane1.setViewportView(statisticTable);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new BorderLayout(5, 0));
        panel1.add(panel11, BorderLayout.SOUTH);
        panel11.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        progressBar = new JProgressBar();
        progressBar.setMaximum(1000);
        panel11.add(progressBar, BorderLayout.CENTER);
        terminateCalculationButton = new JButton();
        terminateCalculationButton.setEnabled(true);
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
