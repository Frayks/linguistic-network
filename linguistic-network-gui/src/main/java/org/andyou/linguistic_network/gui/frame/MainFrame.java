package org.andyou.linguistic_network.gui.frame;

import org.andyou.linguistic_network.gui.api.constant.FrameKey;
import org.andyou.linguistic_network.gui.api.constant.TextConstant;
import org.andyou.linguistic_network.gui.api.frame.SubFrame;
import org.andyou.linguistic_network.gui.util.CommonGUIUtil;
import org.andyou.linguistic_network.lib.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.api.constant.FormatType;
import org.andyou.linguistic_network.lib.api.constant.NGramType;
import org.andyou.linguistic_network.lib.api.context.LinguisticNetworkContext;
import org.andyou.linguistic_network.lib.api.context.MainContext;
import org.andyou.linguistic_network.lib.api.node.ElementNode;
import org.andyou.linguistic_network.lib.util.CommonUtil;
import org.andyou.linguistic_network.lib.util.ElementNodeGraphUtil;
import org.andyou.linguistic_network.lib.util.TextTokenizerUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
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
    private JMenuItem linguisticMetricsMenuItem;
    private JMenuItem keywordExtractionSmallWorldMenuItem;
    private JTextField textFileTextField;
    private JComboBox<NGramType> nGramTypeComboBox;
    private JSpinner nGramSizeSpinner;
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

        JFileChooser textFileChooser = new JFileChooser();
        textFileChooser.setFileFilter(CommonGUIUtil.TEXT_FILE_FILTER);
        JFileChooser xlsxFileChooser = new JFileChooser();
        xlsxFileChooser.setFileFilter(CommonGUIUtil.XLSX_FILE_FILTER);
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        Font font = new JLabel().getFont().deriveFont(14f);
        setComponentsFont(textFileChooser.getComponents(), font);
        setComponentsFont(xlsxFileChooser.getComponents(), font);
        setComponentsFont(directoryChooser.getComponents(), font);

        nGramTypeComboBox.addActionListener(e -> {
            NGramType nGramType = (NGramType) nGramTypeComboBox.getSelectedItem();
            mainContext.setNGramType(nGramType);
            if (!NGramType.WORDS.equals(nGramType)) {
                mainContext.setRemoveStopWords(false);
            }
            updateUI();
        });
        nGramSizeSpinner.addChangeListener(e -> {
            mainContext.setNGramSize((int) nGramSizeSpinner.getValue());
        });
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
            if (textFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                mainContext.setStopWordsFile(textFileChooser.getSelectedFile());
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
            if (textFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                clearContext();
                mainContext.setTextFile(textFileChooser.getSelectedFile());
                updateUI();
            }
        });
        saveMenuItem.addActionListener(e -> {
            try {
                FormatType[] formatTypes = FormatType.values();
                int choice = CommonGUIUtil.showQuestionDialog(
                        this,
                        TextConstant.INFORMATION_MESSAGE_CHOOSE_SAVING_FORMAT,
                        TextConstant.TITLE_SAVE_AS,
                        FormatType.values());
                if (choice != JOptionPane.CLOSED_OPTION) {
                    switch (formatTypes[choice]) {
                        case TEXT_FILES: {
                            if (directoryChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                                File directory = directoryChooser.getSelectedFile();
                                if (directory.exists()) {
                                    if (CommonGUIUtil.showWarningConfirmDialog(
                                            this,
                                            String.format(TextConstant.WARNING_MESSAGE_FILE_ALREADY_EXISTS, directory.getName())
                                    ) != JOptionPane.YES_OPTION) {
                                        return;
                                    }
                                }
                                CommonUtil.saveStatisticsToTextFiles(directory, linguisticNetworkContext);
                            }
                            break;
                        }
                        case EXCEL_FILE: {
                            if (xlsxFileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                                File file = xlsxFileChooser.getSelectedFile();
                                String filePath = file.getAbsolutePath();
                                if (!filePath.endsWith(".xlsx")) {
                                    file = new File(filePath + ".xlsx");
                                }
                                if (file.exists()) {
                                    if (CommonGUIUtil.showWarningConfirmDialog(
                                            this,
                                            String.format(TextConstant.WARNING_MESSAGE_FILE_ALREADY_EXISTS, file.getName())
                                    ) != JOptionPane.YES_OPTION) {
                                        return;
                                    }
                                }
                                CommonUtil.saveStatisticsToXlsxFile(file, linguisticNetworkContext);
                            }
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                CommonGUIUtil.showErrorMessageDialog(this, ex);
            }
        });
        linguisticMetricsMenuItem.addActionListener(e -> {
            JFrame linguisticMetricsSubFrame = subFrameMap.get(FrameKey.LINGUISTIC_METRICS);
            if (linguisticMetricsSubFrame == null) {
                LinguisticMetricsFrame linguisticMetricsFrame = new LinguisticMetricsFrame(linguisticNetworkContext);
                linguisticMetricsFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        subFrameMap.remove(FrameKey.LINGUISTIC_METRICS);
                    }
                });
                configureDefaultSubFrame(linguisticMetricsFrame, "Linguistic Metrics", 500, 600);
                subFrameMap.put(FrameKey.LINGUISTIC_METRICS, linguisticMetricsFrame);
            } else {
                linguisticMetricsSubFrame.requestFocus();
            }
        });
        keywordExtractionSmallWorldMenuItem.addActionListener(e -> {
            JFrame keywordExtractionSmallWorldSubFrame = subFrameMap.get(FrameKey.KEYWORD_EXTRACTION_SMALL_WORLD);
            if (keywordExtractionSmallWorldSubFrame == null) {
                KeywordExtractionSmallWorldFrame keywordExtractionSmallWorldFrame = new KeywordExtractionSmallWorldFrame(linguisticNetworkContext);
                keywordExtractionSmallWorldFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        subFrameMap.remove(FrameKey.KEYWORD_EXTRACTION_SMALL_WORLD);
                    }
                });
                configureDefaultSubFrame(keywordExtractionSmallWorldFrame, "Keyword extraction \"Small-world\"", 700, 600);
                subFrameMap.put(FrameKey.KEYWORD_EXTRACTION_SMALL_WORLD, keywordExtractionSmallWorldFrame);
            } else {
                keywordExtractionSmallWorldSubFrame.requestFocus();
            }
        });

        Runnable task = () -> {
            try {
                mainContext.setElementNodeGraph(null);
                mainContext.setSpentTime(0);
                updateUI();

                File textFile = mainContext.getTextFile();
                File stopWordsFile = mainContext.getStopWordsFile();
                NGramType nGramType = mainContext.getNGramType();
                int nGramSize = mainContext.getNGramSize();
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
                blockSizes.add(2);
                blockSizes.add(90);
                if (filterByFrequency) {
                    blockSizes.add(2);
                }
                ProgressBarProcessor progressBarProcessor = new ProgressBarProcessor(progressBar, blockSizes);

                long startTime = System.currentTimeMillis();
                String[][] elementGroups = TextTokenizerUtil.createElementGroups(text, nGramType, caseSensitive, considerSentenceBounds);
                progressBarProcessor.initAndFinishNextBlock();

                if (removeStopWords) {
                    String stopWordsText = new String(Files.readAllBytes(stopWordsFile.toPath()), StandardCharsets.UTF_8);
                    String[] stopWords = TextTokenizerUtil.splitIntoWords(stopWordsText);
                    elementGroups = TextTokenizerUtil.removeStopWords(elementGroups, stopWords, progressBarProcessor);
                }

                String separator = NGramType.WORDS.equals(nGramType) ? " " : "";
                TextTokenizerUtil.combineIntoNGrams(elementGroups, nGramSize, separator);
                progressBarProcessor.initAndFinishNextBlock();

                Set<ElementNode> elementNodeGraph = ElementNodeGraphUtil.createElementNodeGraph(elementGroups, useRange, rangeSize, progressBarProcessor);

                if (filterByFrequency) {
                    ElementNodeGraphUtil.filterByFrequency(elementNodeGraph, filterFrequency);
                    progressBarProcessor.initAndFinishNextBlock();
                }
                progressBarProcessor.completed();
                long endTime = System.currentTimeMillis();

                mainContext.setElementNodeGraph(elementNodeGraph);
                mainContext.setSpentTime(endTime - startTime);

                List<ElementNode> elementNodes = ElementNodeGraphUtil.sortAndSetIndex(elementNodeGraph);
                for (ElementNode elementNode : elementNodes) {
                    SwingUtilities.invokeLater(() -> defaultTableModel.addRow(new Object[]{elementNode.getIndex(), elementNode.getElement(), elementNode.getFrequency(), elementNode.getNeighborCount()}));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                CommonGUIUtil.showErrorMessageDialog(this, ex);
            } finally {
                updateUI(false);
                elementNodeGraphChanged();
            }
        };

        calculateButton.addActionListener(e -> {
            if (mainContext.isRemoveStopWords() && mainContext.getStopWordsFile() == null) {
                CommonGUIUtil.showWarningMessageDialog(this, TextConstant.WARNING_MESSAGE_MISSING_STOP_WORDS);
                return;
            }

            if (!mainContext.isConsiderSentenceBounds() && !mainContext.isUseRange()) {
                int choice = CommonGUIUtil.showWarningConfirmDialog(this, TextConstant.WARNING_MESSAGE_NOT_SELECTED_OPTIONS);
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
        mainContext.setNGramType((NGramType) nGramTypeComboBox.getSelectedItem());
        mainContext.setNGramSize((int) nGramSizeSpinner.getValue());
        mainContext.setCaseSensitive(caseSensitiveCheckBox.isSelected());
        mainContext.setConsiderSentenceBounds(considerSentenceBoundsCheckBox.isSelected());
        mainContext.setUseRange(useRangeCheckBox.isSelected());
        mainContext.setRangeSize((int) rangeSizeSpinner.getValue());
        mainContext.setRemoveStopWords(removeStopWordsCheckBox.isSelected());
        mainContext.setFilterByFrequency(filterByFrequencyCheckBox.isSelected());
        mainContext.setFilterFrequency((int) filterFrequencySpinner.getValue());
    }

    private void elementNodeGraphChanged() {
        for (JFrame subFrame : subFrameMap.values()) {
            if (subFrame instanceof SubFrame) {
                ((SubFrame) subFrame).elementNodeGraphChanged();
            }
        }
    }

    private void clearContext() {
        mainContext.setTextFile(null);
        mainContext.setElementNodeGraph(null);
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

        removeStopWordsCheckBox.setEnabled(NGramType.WORDS.equals(mainContext.getNGramType()));
        removeStopWordsCheckBox.setSelected(mainContext.isRemoveStopWords());
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

        if (mainContext.getElementNodeGraph() == null) {
            elementCountTextField.setText("0");
        } else {
            elementCountTextField.setText(String.valueOf(mainContext.getElementNodeGraph().size()));
        }

        spentTimeTextField.setText(CommonUtil.formatDuration(mainContext.getSpentTime()));

        if (mainContext.getElementNodeGraph() == null) {
            defaultTableModel.setRowCount(0);
        }

        openMenuItem.setEnabled(!calculationStarted);
        saveMenuItem.setEnabled(!calculationStarted && mainContext.getElementNodeGraph() != null);
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
        frame.setIconImage(CommonGUIUtil.ICON.getImage());
        frame.setMinimumSize(new Dimension(width, height));
        frame.setPreferredSize(new Dimension(width, height));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUIComponents() {
        NGramType[] nGramTypes = {NGramType.WORDS, NGramType.LETTERS_AND_NUMBERS, NGramType.SYMBOLS};
        nGramTypeComboBox = new JComboBox<>(nGramTypes);

        nGramSizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));

        rangeSizeSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 1000000000, 1));

        filterFrequencySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));

        statisticTable = new JTable();
        String[] columnIdentifiers = {"Index", "Element", "Frequency", "Neighbors count"};
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
        linguisticMetricsMenuItem = new JMenuItem();
        Font linguisticMetricsMenuItemFont = this.$$$getFont$$$(null, -1, 14, linguisticMetricsMenuItem.getFont());
        if (linguisticMetricsMenuItemFont != null) linguisticMetricsMenuItem.setFont(linguisticMetricsMenuItemFont);
        linguisticMetricsMenuItem.setText("Linguistic Metrics");
        menu2.add(linguisticMetricsMenuItem);
        keywordExtractionSmallWorldMenuItem = new JMenuItem();
        keywordExtractionSmallWorldMenuItem.setText("Keyword extraction \"Small-world\"");
        menu2.add(keywordExtractionSmallWorldMenuItem);
        final JMenuItem menuItem1 = new JMenuItem();
        menuItem1.setText("Keyword extraction \"TextRank\"");
        menu2.add(menuItem1);
        final JMenuItem menuItem2 = new JMenuItem();
        menuItem2.setText("Keyword extraction \"Centrality Measures\"");
        menu2.add(menuItem2);
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
        gbc.gridy = 2;
        gbc.gridwidth = 2;
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
        gbc.gridy = 3;
        gbc.gridwidth = 2;
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
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(useRangeCheckBox, gbc);
        rangeLabel = new JLabel();
        Font rangeLabelFont = this.$$$getFont$$$(null, -1, 14, rangeLabel.getFont());
        if (rangeLabelFont != null) rangeLabel.setFont(rangeLabelFont);
        rangeLabel.setText("Range");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel5.add(rangeLabel, gbc);
        rangeSizeSpinner.setEnabled(true);
        Font rangeSizeSpinnerFont = this.$$$getFont$$$(null, -1, 14, rangeSizeSpinner.getFont());
        if (rangeSizeSpinnerFont != null) rangeSizeSpinner.setFont(rangeSizeSpinnerFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel5.add(rangeSizeSpinner, gbc);
        removeStopWordsCheckBox = new JCheckBox();
        Font removeStopWordsCheckBoxFont = this.$$$getFont$$$(null, -1, 14, removeStopWordsCheckBox.getFont());
        if (removeStopWordsCheckBoxFont != null) removeStopWordsCheckBox.setFont(removeStopWordsCheckBoxFont);
        removeStopWordsCheckBox.setText("Remove stop words");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(removeStopWordsCheckBox, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(5, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(panel6, gbc);
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        stopWordsFileTextField = new JTextField();
        stopWordsFileTextField.setColumns(10);
        stopWordsFileTextField.setEditable(false);
        stopWordsFileTextField.setEnabled(true);
        Font stopWordsFileTextFieldFont = this.$$$getFont$$$(null, -1, 14, stopWordsFileTextField.getFont());
        if (stopWordsFileTextFieldFont != null) stopWordsFileTextField.setFont(stopWordsFileTextFieldFont);
        panel6.add(stopWordsFileTextField, BorderLayout.CENTER);
        chooseStopWordsFileButton = new JButton();
        chooseStopWordsFileButton.setEnabled(true);
        Font chooseStopWordsFileButtonFont = this.$$$getFont$$$(null, -1, 14, chooseStopWordsFileButton.getFont());
        if (chooseStopWordsFileButtonFont != null) chooseStopWordsFileButton.setFont(chooseStopWordsFileButtonFont);
        chooseStopWordsFileButton.setText("File");
        panel6.add(chooseStopWordsFileButton, BorderLayout.EAST);
        filterByFrequencyCheckBox = new JCheckBox();
        filterByFrequencyCheckBox.setEnabled(true);
        Font filterByFrequencyCheckBoxFont = this.$$$getFont$$$(null, -1, 14, filterByFrequencyCheckBox.getFont());
        if (filterByFrequencyCheckBoxFont != null) filterByFrequencyCheckBox.setFont(filterByFrequencyCheckBoxFont);
        filterByFrequencyCheckBox.setText("Filter by frequency");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(filterByFrequencyCheckBox, gbc);
        frequencyLabel = new JLabel();
        frequencyLabel.setEnabled(true);
        Font frequencyLabelFont = this.$$$getFont$$$(null, -1, 14, frequencyLabel.getFont());
        if (frequencyLabelFont != null) frequencyLabel.setFont(frequencyLabelFont);
        frequencyLabel.setText("Frequency");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel5.add(frequencyLabel, gbc);
        filterFrequencySpinner.setEnabled(true);
        Font filterFrequencySpinnerFont = this.$$$getFont$$$(null, -1, 14, filterFrequencySpinner.getFont());
        if (filterFrequencySpinnerFont != null) filterFrequencySpinner.setFont(filterFrequencySpinnerFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel5.add(filterFrequencySpinner, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
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
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        panel5.add(calculateButton, gbc);
        Font nGramTypeComboBoxFont = this.$$$getFont$$$(null, -1, 14, nGramTypeComboBox.getFont());
        if (nGramTypeComboBoxFont != null) nGramTypeComboBox.setFont(nGramTypeComboBoxFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel5.add(nGramTypeComboBox, gbc);
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, -1, 14, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("N-gram type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel5.add(label2, gbc);
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, -1, 14, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("N-gram size");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel5.add(label3, gbc);
        Font nGramSizeSpinnerFont = this.$$$getFont$$$(null, -1, 14, nGramSizeSpinner.getFont());
        if (nGramSizeSpinnerFont != null) nGramSizeSpinner.setFont(nGramSizeSpinnerFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel5.add(nGramSizeSpinner, gbc);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new BorderLayout(0, 0));
        panel3.add(panel7, BorderLayout.CENTER);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridBagLayout());
        panel7.add(panel8, BorderLayout.SOUTH);
        panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, -1, 14, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("Element count");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel8.add(label4, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel8.add(spacer2, gbc);
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
        panel8.add(elementCountTextField, gbc);
        final JLabel label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$(null, -1, 14, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setText("Spent time");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel8.add(label5, gbc);
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
        panel8.add(spentTimeTextField, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel7.add(scrollPane1, BorderLayout.CENTER);
        statisticTable.setAutoCreateRowSorter(true);
        statisticTable.setCellSelectionEnabled(true);
        Font statisticTableFont = this.$$$getFont$$$(null, -1, 14, statisticTable.getFont());
        if (statisticTableFont != null) statisticTable.setFont(statisticTableFont);
        scrollPane1.setViewportView(statisticTable);
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new BorderLayout(5, 0));
        panel1.add(panel9, BorderLayout.SOUTH);
        panel9.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        progressBar = new JProgressBar();
        progressBar.setMaximum(1000);
        panel9.add(progressBar, BorderLayout.CENTER);
        terminateCalculationButton = new JButton();
        terminateCalculationButton.setEnabled(true);
        terminateCalculationButton.setIcon(new ImageIcon(getClass().getResource("/icon/terminatedlaunchIcon.png")));
        terminateCalculationButton.setPreferredSize(new Dimension(30, 30));
        terminateCalculationButton.setText("");
        panel9.add(terminateCalculationButton, BorderLayout.EAST);
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
