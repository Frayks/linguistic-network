package org.andyou.linguistic_network.gui.frame;

import org.andyou.linguistic_network.gui.api.constant.FrameKey;
import org.andyou.linguistic_network.gui.api.constant.TextConstant;
import org.andyou.linguistic_network.gui.api.frame.SubFrame;
import org.andyou.linguistic_network.gui.util.CommonGUIUtil;
import org.andyou.linguistic_network.lib.api.constant.BoundsType;
import org.andyou.linguistic_network.lib.api.constant.NGramType;
import org.andyou.linguistic_network.lib.api.context.LinguisticNetworkContext;
import org.andyou.linguistic_network.lib.api.context.MainContext;
import org.andyou.linguistic_network.lib.api.data.ElementNode;
import org.andyou.linguistic_network.lib.gui.ProgressBarProcessor;
import org.andyou.linguistic_network.lib.util.CommonUtil;
import org.andyou.linguistic_network.lib.util.ElementNodeGraphUtil;
import org.andyou.linguistic_network.lib.util.LinguisticNetworkUtil;
import org.andyou.linguistic_network.lib.util.TextTokenizerUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MainFrame extends JFrame {

    private JPanel mainPanel;
    private JMenuItem openMenuItem;
    private JMenu saveAsMenu;
    private JMenuItem excelFileMenuItem;
    private JMenuItem textFilesMenuItem;
    private JMenuItem linguisticMetricsMenuItem;
    private JMenuItem keywordExtractionSmallWorldMenuItem;
    private JMenuItem keywordExtractionTextRankMenuItem;
    private JMenuItem keywordExtractionCentralityMeasuresMenuItem;

    private JTextField textFileTextField;
    private JComboBox<NGramType> nGramTypeComboBox;
    private JSpinner nGramSizeSpinner;
    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox includeSpacesCheckBox;
    private JComboBox<BoundsType> boundsTypeComboBox;
    private JLabel sentenceDelimitersLabel;
    private JTextField sentenceDelimitersTextField;
    private JCheckBox useRangeCheckBox;
    private JLabel rangeLabel;
    private JSpinner rangeSizeSpinner;
    private JCheckBox removeStopWordsCheckBox;
    private JPanel stopWordsPanel;
    private JTextField stopWordsFileTextField;
    private JButton chooseStopWordsFileButton;
    private JCheckBox restrictedGraphCheckBox;
    private JLabel jaccardCoefficientLabel;
    private JFormattedTextField jaccardCoefficientFormattedTextField;
    private JCheckBox filterByFrequencyCheckBox;
    private JLabel frequencyLabel;
    private JSpinner filterFrequencySpinner;
    private JCheckBox weightedGraphCheckBox;
    private JButton calculateButton;
    private JTable statisticTable;
    private DefaultTableModel defaultTableModel;
    private JTextField elementCountTextField;
    private JTextField averageClusteringCoefficientTextField;
    private JTextField averagePathLengthTextField;
    private JTextField averageNeighbourCountTextField;
    private JTextField averageMultiplicityTextField;
    private JTextField spentTimeTextField;
    private JProgressBar progressBar;
    private JButton terminateCalculationButton;

    private Map<FrameKey, JFrame> subFrameMap;
    private AtomicReference<Thread> threadAtomicReference;
    private LinguisticNetworkContext linguisticNetworkContext;
    private MainContext mainContext;

    public MainFrame() {
        $$$setupUI$$$();
        setTitle("Linguistic Network");
        setContentPane(mainPanel);
        initContext();

        subFrameMap = new HashMap<>();
        threadAtomicReference = new AtomicReference<>();

        JFileChooser txtFileChooser = new JFileChooser();
        txtFileChooser.setFileFilter(CommonGUIUtil.TXT_FILE_FILTER);
        JFileChooser xlsxFileChooser = new JFileChooser();
        xlsxFileChooser.setFileFilter(CommonGUIUtil.XLSX_FILE_FILTER);
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        Font font = new JLabel().getFont().deriveFont(14f);
        CommonGUIUtil.setComponentsFont(txtFileChooser.getComponents(), font);
        CommonGUIUtil.setComponentsFont(xlsxFileChooser.getComponents(), font);
        CommonGUIUtil.setComponentsFont(directoryChooser.getComponents(), font);

        nGramTypeComboBox.addActionListener(e -> {
            NGramType nGramType = (NGramType) nGramTypeComboBox.getSelectedItem();
            mainContext.setNGramType(nGramType);
            if (!NGramType.WORDS.equals(nGramType)) {
                mainContext.setRemoveStopWords(false);
            }
            if (!NGramType.SYMBOLS.equals(nGramType)) {
                mainContext.setIncludeSpaces(false);
            }
            updateUI(true);
        });
        nGramSizeSpinner.addChangeListener(e -> {
            mainContext.setNGramSize((int) nGramSizeSpinner.getValue());
        });
        caseSensitiveCheckBox.addActionListener(e -> {
            mainContext.setCaseSensitive(caseSensitiveCheckBox.isSelected());
        });
        includeSpacesCheckBox.addActionListener(e -> {
            mainContext.setIncludeSpaces(includeSpacesCheckBox.isSelected());
        });
        boundsTypeComboBox.addActionListener(e -> {
            BoundsType boundsType = (BoundsType) boundsTypeComboBox.getSelectedItem();
            mainContext.setBoundsType(boundsType);
            updateUI(false);
        });
        sentenceDelimitersTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                mainContext.setSentenceDelimiters(sentenceDelimitersTextField.getText());
            }
        });
        useRangeCheckBox.addActionListener(e -> {
            mainContext.setUseRange(useRangeCheckBox.isSelected());
            updateUI(false);
        });
        rangeSizeSpinner.addChangeListener(e -> {
            mainContext.setRangeSize((int) rangeSizeSpinner.getValue());
        });
        removeStopWordsCheckBox.addActionListener(e -> {
            mainContext.setRemoveStopWords(removeStopWordsCheckBox.isSelected());
            updateUI(false);
        });
        chooseStopWordsFileButton.addActionListener(e -> {
            if (txtFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                mainContext.setStopWordsFile(txtFileChooser.getSelectedFile());
                updateUI(false);
            }
        });
        restrictedGraphCheckBox.addActionListener(e -> {
            mainContext.setRestrictedGraph(restrictedGraphCheckBox.isSelected());
            updateUI(false);
        });
        jaccardCoefficientFormattedTextField.addPropertyChangeListener("value", e -> {
            mainContext.setJaccardCoefficient(Double.parseDouble(jaccardCoefficientFormattedTextField.getValue().toString()));
        });
        filterByFrequencyCheckBox.addActionListener(e -> {
            mainContext.setFilterByFrequency(filterByFrequencyCheckBox.isSelected());
            updateUI(false);
        });
        filterFrequencySpinner.addChangeListener(e -> {
            mainContext.setFilterFrequency((int) filterFrequencySpinner.getValue());
        });
        weightedGraphCheckBox.addActionListener(e -> {
            mainContext.setWeightedGraph(weightedGraphCheckBox.isSelected());
        });
        openMenuItem.addActionListener(e -> {
            if (txtFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                clearContext();
                mainContext.setTextFile(txtFileChooser.getSelectedFile());
                updateUI(false);
            }
        });
        excelFileMenuItem.addActionListener(e -> {
            try {
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
            } catch (Exception ex) {
                ex.printStackTrace();
                CommonGUIUtil.showErrorMessageDialog(this, ex);
            }
        });
        textFilesMenuItem.addActionListener(e -> {
            try {
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
                CommonGUIUtil.configureDefaultSubFrame(linguisticMetricsFrame, 400, 400);
                subFrameMap.put(FrameKey.LINGUISTIC_METRICS, linguisticMetricsFrame);
            } else {
                linguisticMetricsSubFrame.setState(JFrame.NORMAL);
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
                CommonGUIUtil.configureDefaultSubFrame(keywordExtractionSmallWorldFrame, 1200, 600);
                subFrameMap.put(FrameKey.KEYWORD_EXTRACTION_SMALL_WORLD, keywordExtractionSmallWorldFrame);
            } else {
                keywordExtractionSmallWorldSubFrame.setState(JFrame.NORMAL);
                keywordExtractionSmallWorldSubFrame.requestFocus();
            }
        });
        keywordExtractionTextRankMenuItem.addActionListener(e -> {
            JFrame keywordExtractionTextRankSubFrame = subFrameMap.get(FrameKey.KEYWORD_EXTRACTION_TEXT_RANK);
            if (keywordExtractionTextRankSubFrame == null) {
                KeywordExtractionTextRankFrame keywordExtractionTextRankFrame = new KeywordExtractionTextRankFrame(linguisticNetworkContext);
                keywordExtractionTextRankFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        subFrameMap.remove(FrameKey.KEYWORD_EXTRACTION_TEXT_RANK);
                    }
                });
                CommonGUIUtil.configureDefaultSubFrame(keywordExtractionTextRankFrame, 1000, 600);
                subFrameMap.put(FrameKey.KEYWORD_EXTRACTION_TEXT_RANK, keywordExtractionTextRankFrame);
            } else {
                keywordExtractionTextRankSubFrame.setState(JFrame.NORMAL);
                keywordExtractionTextRankSubFrame.requestFocus();
            }
        });
        keywordExtractionCentralityMeasuresMenuItem.addActionListener(e -> {
            JFrame keywordExtractionCentralityMeasuresSubFrame = subFrameMap.get(FrameKey.KEYWORD_EXTRACTION_CENTRALITY_MEASURES);
            if (keywordExtractionCentralityMeasuresSubFrame == null) {
                KeywordExtractionCentralityMeasuresFrame keywordExtractionCentralityMeasuresFrame = new KeywordExtractionCentralityMeasuresFrame(linguisticNetworkContext);
                keywordExtractionCentralityMeasuresFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        subFrameMap.remove(FrameKey.KEYWORD_EXTRACTION_CENTRALITY_MEASURES);
                    }
                });
                CommonGUIUtil.configureDefaultSubFrame(keywordExtractionCentralityMeasuresFrame, 1300, 600);
                subFrameMap.put(FrameKey.KEYWORD_EXTRACTION_CENTRALITY_MEASURES, keywordExtractionCentralityMeasuresFrame);
            } else {
                keywordExtractionCentralityMeasuresSubFrame.setState(JFrame.NORMAL);
                keywordExtractionCentralityMeasuresSubFrame.requestFocus();
            }
        });

        statisticTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Integer index = (Integer) statisticTable.getValueAt(statisticTable.getSelectedRow(), 0);
                    if (index != null) {
                        ElementNode elementNode = ElementNodeGraphUtil.getElementNodeByIndex(mainContext.getElementNodeGraph(), index);
                        if (elementNode != null) {
                            ElementNodeInfoFrame elementNodeInfoFrame = new ElementNodeInfoFrame(elementNode);
                            CommonGUIUtil.configureDefaultSubFrame(elementNodeInfoFrame, 400, 400);
                        }
                    }
                }
            }
        });

        Runnable task = () -> {
            try {
                mainContext.setElementNodeGraph(null);
                mainContext.setAverageClusteringCoefficient(0.0);
                mainContext.setAveragePathLength(0.0);
                mainContext.setAverageNeighbourCount(0.0);
                mainContext.setAverageMultiplicity(0.0);
                mainContext.setSpentTime(0);
                updateUI(false);

                File textFile = mainContext.getTextFile();
                File stopWordsFile = mainContext.getStopWordsFile();
                NGramType nGramType = mainContext.getNGramType();
                int nGramSize = mainContext.getNGramSize();
                boolean caseSensitive = mainContext.isCaseSensitive();
                boolean includeSpaces = mainContext.isIncludeSpaces();
                BoundsType boundsType = mainContext.getBoundsType();
                String sentenceDelimiters = mainContext.getSentenceDelimiters();
                boolean useRange = mainContext.isUseRange();
                int rangeSize = mainContext.getRangeSize();
                boolean removeStopWords = mainContext.isRemoveStopWords();
                boolean restrictedGraph = mainContext.isRestrictedGraph();
                double jaccardCoefficient = mainContext.getJaccardCoefficient();
                boolean filterByFrequency = mainContext.isFilterByFrequency();
                int filterFrequency = mainContext.getFilterFrequency();
                boolean weightedGraph = mainContext.isWeightedGraph();

                String text = new String(Files.readAllBytes(textFile.toPath()), StandardCharsets.UTF_8);

                List<Integer> blockSizes = new ArrayList<>();
                blockSizes.add(1);
                if (removeStopWords) {
                    blockSizes.add(3);
                }
                blockSizes.add(2);
                blockSizes.add(18);
                if (restrictedGraph) {
                    blockSizes.add(2);
                }
                if (filterByFrequency) {
                    blockSizes.add(2);
                }
                blockSizes.add(20);
                blockSizes.add(50);
                blockSizes.add(2);
                ProgressBarProcessor progressBarProcessor = new ProgressBarProcessor(progressBar, blockSizes);

                long startTime = System.currentTimeMillis();
                String[][] elementGroups = TextTokenizerUtil.createElementGroups(text, nGramType, caseSensitive, includeSpaces, boundsType, sentenceDelimiters);
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

                if (restrictedGraph) {
                    ElementNodeGraphUtil.filterByJaccardCoefficient(elementNodeGraph, jaccardCoefficient);
                    progressBarProcessor.initAndFinishNextBlock();
                }

                if (filterByFrequency) {
                    ElementNodeGraphUtil.filterByFrequency(elementNodeGraph, filterFrequency);
                    progressBarProcessor.initAndFinishNextBlock();
                }

                double averageClusteringCoefficient = LinguisticNetworkUtil.calcAverageClusteringCoefficient(elementNodeGraph, progressBarProcessor);
                double averagePathLength = LinguisticNetworkUtil.calcAveragePathLength(elementNodeGraph, weightedGraph, false, progressBarProcessor);
                double averageNeighbourCount = LinguisticNetworkUtil.calcAverageNeighbourCount(elementNodeGraph);
                double averageMultiplicity = LinguisticNetworkUtil.calcAverageMultiplicity(elementNodeGraph, weightedGraph);
                progressBarProcessor.initAndFinishNextBlock();

                progressBarProcessor.completed();
                long endTime = System.currentTimeMillis();

                mainContext.setElementNodeGraph(elementNodeGraph);
                mainContext.setAverageClusteringCoefficient(averageClusteringCoefficient);
                mainContext.setAveragePathLength(averagePathLength);
                mainContext.setAverageNeighbourCount(averageNeighbourCount);
                mainContext.setAverageMultiplicity(averageMultiplicity);
                mainContext.setSpentTime(endTime - startTime);

                List<ElementNode> elementNodes = ElementNodeGraphUtil.sortAndSetIndex(elementNodeGraph);
                for (ElementNode elementNode : elementNodes) {
                    SwingUtilities.invokeLater(() -> defaultTableModel.addRow(new Object[]{
                            elementNode.getIndex(),
                            elementNode.getElement(),
                            elementNode.getFrequency(),
                            elementNode.getClusteringCoefficient(),
                            elementNode.getAveragePathLength(),
                            elementNode.getNeighborCount(),
                            elementNode.getMultiplicity()
                    }));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                CommonGUIUtil.showErrorMessageDialog(this, ex);
            } finally {
                updateUI(false, false);
                elementNodeGraphChanged();
            }
        };

        calculateButton.addActionListener(e -> {
            if (mainContext.isRemoveStopWords() && mainContext.getStopWordsFile() == null) {
                CommonGUIUtil.showWarningMessageDialog(this, TextConstant.WARNING_MESSAGE_MISSING_STOP_WORDS);
                return;
            }

            if (BoundsType.ABSENT.equals(mainContext.getBoundsType()) && !mainContext.isUseRange()) {
                int choice = CommonGUIUtil.showWarningConfirmDialog(this, TextConstant.WARNING_MESSAGE_NO_RESTRICTIVE_OPTION_IS_SELECTED);
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

        initUI();
    }

    private void initContext() {
        linguisticNetworkContext = new LinguisticNetworkContext();
        mainContext = linguisticNetworkContext.getMainContext();
        mainContext.setNGramType((NGramType) nGramTypeComboBox.getSelectedItem());
        mainContext.setNGramSize((int) nGramSizeSpinner.getValue());
        mainContext.setCaseSensitive(caseSensitiveCheckBox.isSelected());
        mainContext.setIncludeSpaces(includeSpacesCheckBox.isSelected());
        mainContext.setBoundsType((BoundsType) boundsTypeComboBox.getSelectedItem());
        mainContext.setSentenceDelimiters(sentenceDelimitersTextField.getText());
        mainContext.setUseRange(useRangeCheckBox.isSelected());
        mainContext.setRangeSize((int) rangeSizeSpinner.getValue());
        mainContext.setRemoveStopWords(removeStopWordsCheckBox.isSelected());
        mainContext.setRestrictedGraph(restrictedGraphCheckBox.isSelected());
        mainContext.setJaccardCoefficient((double) jaccardCoefficientFormattedTextField.getValue());
        mainContext.setFilterByFrequency(filterByFrequencyCheckBox.isSelected());
        mainContext.setFilterFrequency((int) filterFrequencySpinner.getValue());
        mainContext.setWeightedGraph(weightedGraphCheckBox.isSelected());
    }

    private void clearContext() {
        mainContext.setTextFile(null);
        mainContext.setElementNodeGraph(null);
        mainContext.setAverageClusteringCoefficient(0.0);
        mainContext.setAveragePathLength(0.0);
        mainContext.setAverageNeighbourCount(0.0);
        mainContext.setAverageMultiplicity(0.0);
        mainContext.setSpentTime(0);
        for (JFrame subFrame : subFrameMap.values()) {
            if (subFrame instanceof SubFrame) {
                ((SubFrame) subFrame).clearContext();
            }
        }
    }

    private void elementNodeGraphChanged() {
        for (JFrame subFrame : subFrameMap.values()) {
            if (subFrame instanceof SubFrame) {
                ((SubFrame) subFrame).elementNodeGraphChanged();
            }
        }
    }

    private void initUI() {
        updateUI(false, true);
    }

    private void updateUI(boolean changedNGramType) {
        Thread thread = threadAtomicReference.get();
        updateUI(thread != null && thread.isAlive(), changedNGramType);
    }

    synchronized private void updateUI(boolean calculationStarted, boolean changedNGramType) {
        if (mainContext.getTextFile() == null) {
            textFileTextField.setText("");
        } else {
            String absolutePath = mainContext.getTextFile().getAbsolutePath();
            if (!textFileTextField.getText().equals(absolutePath)) {
                textFileTextField.setText(absolutePath);
            }
        }

        includeSpacesCheckBox.setVisible(NGramType.SYMBOLS.equals(mainContext.getNGramType()));
        includeSpacesCheckBox.setSelected(mainContext.isIncludeSpaces());

        if (changedNGramType) {
            updateBoundsTypeComboBox(mainContext.getNGramType());
        }

        boundsTypeComboBox.setSelectedItem(mainContext.getBoundsType());

        sentenceDelimitersLabel.setVisible(BoundsType.SENTENCE.equals(mainContext.getBoundsType()));
        sentenceDelimitersTextField.setVisible(BoundsType.SENTENCE.equals(mainContext.getBoundsType()));

        rangeLabel.setVisible(mainContext.isUseRange());
        rangeSizeSpinner.setVisible(mainContext.isUseRange());

        removeStopWordsCheckBox.setVisible(NGramType.WORDS.equals(mainContext.getNGramType()));
        removeStopWordsCheckBox.setSelected(mainContext.isRemoveStopWords());
        stopWordsPanel.setVisible(mainContext.isRemoveStopWords());

        if (mainContext.getStopWordsFile() == null) {
            stopWordsFileTextField.setText("");
        } else {
            String absolutePath = mainContext.getStopWordsFile().getAbsolutePath();
            if (!stopWordsFileTextField.getText().equals(absolutePath)) {
                stopWordsFileTextField.setText(absolutePath);
            }
        }

        jaccardCoefficientLabel.setVisible(mainContext.isRestrictedGraph());
        jaccardCoefficientFormattedTextField.setVisible(mainContext.isRestrictedGraph());

        frequencyLabel.setVisible(mainContext.isFilterByFrequency());
        filterFrequencySpinner.setVisible(mainContext.isFilterByFrequency());

        elementCountTextField.setText(String.valueOf(mainContext.getElementNodeGraph() != null ? mainContext.getElementNodeGraph().size() : 0));
        averageClusteringCoefficientTextField.setText(CommonGUIUtil.DECIMAL_FORMAT.format(mainContext.getAverageClusteringCoefficient()));
        averagePathLengthTextField.setText(CommonGUIUtil.DECIMAL_FORMAT.format(mainContext.getAveragePathLength()));
        averageNeighbourCountTextField.setText(CommonGUIUtil.DECIMAL_FORMAT.format(mainContext.getAverageNeighbourCount()));
        averageMultiplicityTextField.setText(CommonGUIUtil.DECIMAL_FORMAT.format(mainContext.getAverageMultiplicity()));
        spentTimeTextField.setText(CommonUtil.formatDuration(mainContext.getSpentTime()));

        if (mainContext.getElementNodeGraph() == null) {
            defaultTableModel.setRowCount(0);
        }

        openMenuItem.setEnabled(!calculationStarted);
        saveAsMenu.setEnabled(!calculationStarted && mainContext.getElementNodeGraph() != null);
        calculateButton.setEnabled(!calculationStarted && mainContext.getTextFile() != null);
        terminateCalculationButton.setEnabled(calculationStarted);

        for (JFrame subFrame : subFrameMap.values()) {
            if (subFrame instanceof SubFrame) {
                ((SubFrame) subFrame).updateUI();
            }
        }
    }

    private void updateBoundsTypeComboBox(NGramType nGramType) {
        boundsTypeComboBox.removeAllItems();

        if (NGramType.WORDS.equals(nGramType)) {
            boundsTypeComboBox.addItem(BoundsType.ABSENT);
            boundsTypeComboBox.addItem(BoundsType.SENTENCE);
        } else if (NGramType.LETTERS_AND_NUMBERS.equals(nGramType)) {
            boundsTypeComboBox.addItem(BoundsType.ABSENT);
            boundsTypeComboBox.addItem(BoundsType.SENTENCE);
            boundsTypeComboBox.addItem(BoundsType.WORD);
        } else if (NGramType.SYMBOLS.equals(nGramType)) {
            boundsTypeComboBox.addItem(BoundsType.ABSENT);
        }

        boundsTypeComboBox.setSelectedItem(BoundsType.ABSENT);
    }

    private void createUIComponents() {
        NGramType[] nGramTypes = {NGramType.WORDS, NGramType.LETTERS_AND_NUMBERS, NGramType.SYMBOLS};
        nGramTypeComboBox = new JComboBox<>(nGramTypes);

        boundsTypeComboBox = new JComboBox<>();

        nGramSizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));

        rangeSizeSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 1000000000, 1));

        filterFrequencySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000000, 1));

        jaccardCoefficientFormattedTextField = new JFormattedTextField(CommonGUIUtil.DOUBLE_FORMATTER_FACTORY, 2.0);

        statisticTable = new JTable();
        String[] columnIdentifiers = {"Index", "Element", "Frequency", "Clustering Coefficient", "Avg. Path Length", "Neighbor Count", "Multiplicity"};
        defaultTableModel = new DefaultTableModel(0, 7) {
            final Class<?>[] types = {Integer.class, String.class, Integer.class, Double.class, Double.class, Integer.class, Integer.class};

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

        double[] columnWidth = {0.08, 0.17, 0.10, 0.20, 0.17, 0.16, 0.12};
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
        Font openMenuItemFont = this.$$$getFont$$$(null, -1, 14, openMenuItem.getFont());
        if (openMenuItemFont != null) openMenuItem.setFont(openMenuItemFont);
        openMenuItem.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/file.gif")));
        openMenuItem.setText("Open");
        menu1.add(openMenuItem);
        saveAsMenu = new JMenu();
        saveAsMenu.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        Font saveAsMenuFont = this.$$$getFont$$$(null, -1, 14, saveAsMenu.getFont());
        if (saveAsMenuFont != null) saveAsMenu.setFont(saveAsMenuFont);
        saveAsMenu.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/floppy.gif")));
        saveAsMenu.setText("Save As");
        menu1.add(saveAsMenu);
        excelFileMenuItem = new JMenuItem();
        Font excelFileMenuItemFont = this.$$$getFont$$$(null, -1, 14, excelFileMenuItem.getFont());
        if (excelFileMenuItemFont != null) excelFileMenuItem.setFont(excelFileMenuItemFont);
        excelFileMenuItem.setIcon(new ImageIcon(getClass().getResource("/icon/xlsxFileIcon.png")));
        excelFileMenuItem.setText("Excel file");
        saveAsMenu.add(excelFileMenuItem);
        textFilesMenuItem = new JMenuItem();
        Font textFilesMenuItemFont = this.$$$getFont$$$(null, -1, 14, textFilesMenuItem.getFont());
        if (textFilesMenuItemFont != null) textFilesMenuItem.setFont(textFilesMenuItemFont);
        textFilesMenuItem.setIcon(new ImageIcon(getClass().getResource("/icon/txtFileIcon.png")));
        textFilesMenuItem.setText("Text files");
        saveAsMenu.add(textFilesMenuItem);
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
        Font keywordExtractionSmallWorldMenuItemFont = this.$$$getFont$$$(null, -1, 14, keywordExtractionSmallWorldMenuItem.getFont());
        if (keywordExtractionSmallWorldMenuItemFont != null)
            keywordExtractionSmallWorldMenuItem.setFont(keywordExtractionSmallWorldMenuItemFont);
        keywordExtractionSmallWorldMenuItem.setText("Keyword extraction \"Small-world\"");
        menu2.add(keywordExtractionSmallWorldMenuItem);
        keywordExtractionTextRankMenuItem = new JMenuItem();
        Font keywordExtractionTextRankMenuItemFont = this.$$$getFont$$$(null, -1, 14, keywordExtractionTextRankMenuItem.getFont());
        if (keywordExtractionTextRankMenuItemFont != null)
            keywordExtractionTextRankMenuItem.setFont(keywordExtractionTextRankMenuItemFont);
        keywordExtractionTextRankMenuItem.setText("Keyword extraction \"TextRank\"");
        menu2.add(keywordExtractionTextRankMenuItem);
        keywordExtractionCentralityMeasuresMenuItem = new JMenuItem();
        Font keywordExtractionCentralityMeasuresMenuItemFont = this.$$$getFont$$$(null, -1, 14, keywordExtractionCentralityMeasuresMenuItem.getFont());
        if (keywordExtractionCentralityMeasuresMenuItemFont != null)
            keywordExtractionCentralityMeasuresMenuItem.setFont(keywordExtractionCentralityMeasuresMenuItemFont);
        keywordExtractionCentralityMeasuresMenuItem.setText("Keyword extraction \"Centrality Measures\"");
        menu2.add(keywordExtractionCentralityMeasuresMenuItem);
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
        caseSensitiveCheckBox.setSelected(false);
        caseSensitiveCheckBox.setText("Case sensitive");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(caseSensitiveCheckBox, gbc);
        useRangeCheckBox = new JCheckBox();
        useRangeCheckBox.setEnabled(true);
        Font useRangeCheckBoxFont = this.$$$getFont$$$(null, -1, 14, useRangeCheckBox.getFont());
        if (useRangeCheckBoxFont != null) useRangeCheckBox.setFont(useRangeCheckBoxFont);
        useRangeCheckBox.setSelected(true);
        useRangeCheckBox.setText("Use range");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
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
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel5.add(rangeLabel, gbc);
        rangeSizeSpinner.setEnabled(true);
        Font rangeSizeSpinnerFont = this.$$$getFont$$$(null, -1, 14, rangeSizeSpinner.getFont());
        if (rangeSizeSpinnerFont != null) rangeSizeSpinner.setFont(rangeSizeSpinnerFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel5.add(rangeSizeSpinner, gbc);
        removeStopWordsCheckBox = new JCheckBox();
        Font removeStopWordsCheckBoxFont = this.$$$getFont$$$(null, -1, 14, removeStopWordsCheckBox.getFont());
        if (removeStopWordsCheckBoxFont != null) removeStopWordsCheckBox.setFont(removeStopWordsCheckBoxFont);
        removeStopWordsCheckBox.setText("Remove stop words");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(removeStopWordsCheckBox, gbc);
        stopWordsPanel = new JPanel();
        stopWordsPanel.setLayout(new BorderLayout(5, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(stopWordsPanel, gbc);
        stopWordsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        stopWordsFileTextField = new JTextField();
        stopWordsFileTextField.setColumns(10);
        stopWordsFileTextField.setEditable(false);
        stopWordsFileTextField.setEnabled(true);
        Font stopWordsFileTextFieldFont = this.$$$getFont$$$(null, -1, 14, stopWordsFileTextField.getFont());
        if (stopWordsFileTextFieldFont != null) stopWordsFileTextField.setFont(stopWordsFileTextFieldFont);
        stopWordsPanel.add(stopWordsFileTextField, BorderLayout.CENTER);
        chooseStopWordsFileButton = new JButton();
        chooseStopWordsFileButton.setEnabled(true);
        Font chooseStopWordsFileButtonFont = this.$$$getFont$$$(null, -1, 14, chooseStopWordsFileButton.getFont());
        if (chooseStopWordsFileButtonFont != null) chooseStopWordsFileButton.setFont(chooseStopWordsFileButtonFont);
        chooseStopWordsFileButton.setText("File");
        stopWordsPanel.add(chooseStopWordsFileButton, BorderLayout.EAST);
        filterByFrequencyCheckBox = new JCheckBox();
        filterByFrequencyCheckBox.setEnabled(true);
        Font filterByFrequencyCheckBoxFont = this.$$$getFont$$$(null, -1, 14, filterByFrequencyCheckBox.getFont());
        if (filterByFrequencyCheckBoxFont != null) filterByFrequencyCheckBox.setFont(filterByFrequencyCheckBoxFont);
        filterByFrequencyCheckBox.setText("Filter by frequency");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 12;
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
        gbc.gridy = 13;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel5.add(frequencyLabel, gbc);
        filterFrequencySpinner.setEnabled(true);
        Font filterFrequencySpinnerFont = this.$$$getFont$$$(null, -1, 14, filterFrequencySpinner.getFont());
        if (filterFrequencySpinnerFont != null) filterFrequencySpinner.setFont(filterFrequencySpinnerFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 13;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel5.add(filterFrequencySpinner, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 15;
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
        gbc.gridy = 16;
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
        sentenceDelimitersLabel = new JLabel();
        Font sentenceDelimitersLabelFont = this.$$$getFont$$$(null, -1, 14, sentenceDelimitersLabel.getFont());
        if (sentenceDelimitersLabelFont != null) sentenceDelimitersLabel.setFont(sentenceDelimitersLabelFont);
        sentenceDelimitersLabel.setText("Delimiters");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel5.add(sentenceDelimitersLabel, gbc);
        sentenceDelimitersTextField = new JTextField();
        Font sentenceDelimitersTextFieldFont = this.$$$getFont$$$(null, -1, 14, sentenceDelimitersTextField.getFont());
        if (sentenceDelimitersTextFieldFont != null)
            sentenceDelimitersTextField.setFont(sentenceDelimitersTextFieldFont);
        sentenceDelimitersTextField.setText(".!?…");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel5.add(sentenceDelimitersTextField, gbc);
        includeSpacesCheckBox = new JCheckBox();
        Font includeSpacesCheckBoxFont = this.$$$getFont$$$(null, -1, 14, includeSpacesCheckBox.getFont());
        if (includeSpacesCheckBoxFont != null) includeSpacesCheckBox.setFont(includeSpacesCheckBoxFont);
        includeSpacesCheckBox.setSelected(false);
        includeSpacesCheckBox.setText("Include spaces");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(includeSpacesCheckBox, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel5.add(boundsTypeComboBox, gbc);
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, -1, 14, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("Bounds");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel5.add(label4, gbc);
        restrictedGraphCheckBox = new JCheckBox();
        Font restrictedGraphCheckBoxFont = this.$$$getFont$$$(null, -1, 14, restrictedGraphCheckBox.getFont());
        if (restrictedGraphCheckBoxFont != null) restrictedGraphCheckBox.setFont(restrictedGraphCheckBoxFont);
        restrictedGraphCheckBox.setText("Restricted graph");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 0, 2, 0);
        panel5.add(restrictedGraphCheckBox, gbc);
        jaccardCoefficientLabel = new JLabel();
        Font jaccardCoefficientLabelFont = this.$$$getFont$$$(null, -1, 14, jaccardCoefficientLabel.getFont());
        if (jaccardCoefficientLabelFont != null) jaccardCoefficientLabel.setFont(jaccardCoefficientLabelFont);
        jaccardCoefficientLabel.setText("Jaccard coef.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        panel5.add(jaccardCoefficientLabel, gbc);
        Font jaccardCoefficientFormattedTextFieldFont = this.$$$getFont$$$(null, -1, 14, jaccardCoefficientFormattedTextField.getFont());
        if (jaccardCoefficientFormattedTextFieldFont != null)
            jaccardCoefficientFormattedTextField.setFont(jaccardCoefficientFormattedTextFieldFont);
        jaccardCoefficientFormattedTextField.setHorizontalAlignment(4);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 0, 2, 5);
        panel5.add(jaccardCoefficientFormattedTextField, gbc);
        weightedGraphCheckBox = new JCheckBox();
        weightedGraphCheckBox.setText("Weighted graph");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel5.add(weightedGraphCheckBox, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(0, 0));
        panel3.add(panel6, BorderLayout.CENTER);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        panel6.add(panel7, BorderLayout.SOUTH);
        panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$(null, -1, 14, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setText("Element count");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel7.add(label5, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel7.add(spacer2, gbc);
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
        panel7.add(elementCountTextField, gbc);
        final JLabel label6 = new JLabel();
        Font label6Font = this.$$$getFont$$$(null, -1, 14, label6.getFont());
        if (label6Font != null) label6.setFont(label6Font);
        label6.setText("Spent time");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel7.add(label6, gbc);
        spentTimeTextField = new JTextField();
        spentTimeTextField.setColumns(10);
        spentTimeTextField.setEditable(false);
        Font spentTimeTextFieldFont = this.$$$getFont$$$(null, -1, 14, spentTimeTextField.getFont());
        if (spentTimeTextFieldFont != null) spentTimeTextField.setFont(spentTimeTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel7.add(spentTimeTextField, gbc);
        final JLabel label7 = new JLabel();
        Font label7Font = this.$$$getFont$$$(null, -1, 14, label7.getFont());
        if (label7Font != null) label7.setFont(label7Font);
        label7.setText("Average Clustering Coefficient");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel7.add(label7, gbc);
        averageClusteringCoefficientTextField = new JTextField();
        averageClusteringCoefficientTextField.setColumns(10);
        averageClusteringCoefficientTextField.setEditable(false);
        Font averageClusteringCoefficientTextFieldFont = this.$$$getFont$$$(null, -1, 14, averageClusteringCoefficientTextField.getFont());
        if (averageClusteringCoefficientTextFieldFont != null)
            averageClusteringCoefficientTextField.setFont(averageClusteringCoefficientTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel7.add(averageClusteringCoefficientTextField, gbc);
        final JLabel label8 = new JLabel();
        Font label8Font = this.$$$getFont$$$(null, -1, 14, label8.getFont());
        if (label8Font != null) label8.setFont(label8Font);
        label8.setText("Average Path Length");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel7.add(label8, gbc);
        averagePathLengthTextField = new JTextField();
        averagePathLengthTextField.setColumns(10);
        averagePathLengthTextField.setEditable(false);
        Font averagePathLengthTextFieldFont = this.$$$getFont$$$(null, -1, 14, averagePathLengthTextField.getFont());
        if (averagePathLengthTextFieldFont != null) averagePathLengthTextField.setFont(averagePathLengthTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel7.add(averagePathLengthTextField, gbc);
        final JLabel label9 = new JLabel();
        Font label9Font = this.$$$getFont$$$(null, -1, 14, label9.getFont());
        if (label9Font != null) label9.setFont(label9Font);
        label9.setText("Average Neighbour Count");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel7.add(label9, gbc);
        averageNeighbourCountTextField = new JTextField();
        averageNeighbourCountTextField.setColumns(10);
        averageNeighbourCountTextField.setEditable(false);
        Font averageNeighbourCountTextFieldFont = this.$$$getFont$$$(null, -1, 14, averageNeighbourCountTextField.getFont());
        if (averageNeighbourCountTextFieldFont != null)
            averageNeighbourCountTextField.setFont(averageNeighbourCountTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel7.add(averageNeighbourCountTextField, gbc);
        final JLabel label10 = new JLabel();
        Font label10Font = this.$$$getFont$$$(null, -1, 14, label10.getFont());
        if (label10Font != null) label10.setFont(label10Font);
        label10.setText("Average Multiplicity");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        panel7.add(label10, gbc);
        averageMultiplicityTextField = new JTextField();
        averageMultiplicityTextField.setColumns(10);
        averageMultiplicityTextField.setEditable(false);
        Font averageMultiplicityTextFieldFont = this.$$$getFont$$$(null, -1, 14, averageMultiplicityTextField.getFont());
        if (averageMultiplicityTextFieldFont != null)
            averageMultiplicityTextField.setFont(averageMultiplicityTextFieldFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel7.add(averageMultiplicityTextField, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel6.add(scrollPane1, BorderLayout.CENTER);
        statisticTable.setAutoCreateRowSorter(true);
        statisticTable.setCellSelectionEnabled(true);
        Font statisticTableFont = this.$$$getFont$$$(null, -1, 14, statisticTable.getFont());
        if (statisticTableFont != null) statisticTable.setFont(statisticTableFont);
        scrollPane1.setViewportView(statisticTable);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new BorderLayout(5, 0));
        panel1.add(panel8, BorderLayout.SOUTH);
        panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        progressBar = new JProgressBar();
        progressBar.setMaximum(1000);
        panel8.add(progressBar, BorderLayout.CENTER);
        terminateCalculationButton = new JButton();
        terminateCalculationButton.setEnabled(true);
        terminateCalculationButton.setIcon(new ImageIcon(getClass().getResource("/icon/terminatedlaunchIcon.png")));
        terminateCalculationButton.setPreferredSize(new Dimension(30, 30));
        terminateCalculationButton.setText("");
        panel8.add(terminateCalculationButton, BorderLayout.EAST);
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
