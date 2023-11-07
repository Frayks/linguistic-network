package org.andyou.linguistic_network.gui.frame;

import org.andyou.linguistic_network.gui.util.CommonGUIUtil;
import org.andyou.linguistic_network.lib.api.node.ElementNode;
import org.andyou.linguistic_network.lib.util.ElementNodeGraphUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public class ElementNodeInfoFrame extends JFrame {
    private JPanel mainPanel;
    private JMenu saveAsMenu;
    private JMenuItem excelFileMenuItem;
    private JMenuItem textFileMenuItem;
    private JTable statisticTable;
    private DefaultTableModel defaultTableModel;

    private ElementNode mainElementNode;

    public ElementNodeInfoFrame(ElementNode mainElementNode) throws HeadlessException {
        this.mainElementNode = mainElementNode;

        $$$setupUI$$$();
        setTitle(String.format("Element \"%s\"", mainElementNode.getElement()));
        setContentPane(mainPanel);

        statisticTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Integer index = (Integer) statisticTable.getValueAt(statisticTable.getSelectedRow(), 0);
                    if (index != null) {
                        ElementNode elementNode = ElementNodeGraphUtil.getElementNodeByIndex(mainElementNode.getNeighbors().keySet(), index);
                        if (elementNode != null) {
                            ElementNodeInfoFrame elementNodeInfoFrame = new ElementNodeInfoFrame(elementNode);
                            CommonGUIUtil.configureDefaultSubFrame(elementNodeInfoFrame, 400, 400);
                        }
                    }
                }
            }
        });

        List<Map.Entry<ElementNode, Integer>> neighbors = new ArrayList<>(mainElementNode.getNeighbors().entrySet());
        neighbors.sort(Map.Entry.<ElementNode, Integer>comparingByValue().reversed());
        for (Map.Entry<ElementNode, Integer> neighbor : neighbors) {
            SwingUtilities.invokeLater(() -> defaultTableModel.addRow(new Object[]{neighbor.getKey().getIndex(), neighbor.getKey().getElement(), neighbor.getValue()}));
        }
    }

    private void createUIComponents() {
        statisticTable = new JTable();
        String[] columnIdentifiers = {"Index", "Neighbor", "Neighbor Count"};
        defaultTableModel = new DefaultTableModel(0, 3) {
            final Class<?>[] types = {Integer.class, String.class, Integer.class};

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
        saveAsMenu = new JMenu();
        saveAsMenu.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        Font saveAsMenuFont = this.$$$getFont$$$(null, -1, 14, saveAsMenu.getFont());
        if (saveAsMenuFont != null) saveAsMenu.setFont(saveAsMenuFont);
        saveAsMenu.setIcon(new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/floppy.gif")));
        saveAsMenu.setText("Save As");
        menuBar1.add(saveAsMenu);
        excelFileMenuItem = new JMenuItem();
        Font excelFileMenuItemFont = this.$$$getFont$$$(null, -1, 14, excelFileMenuItem.getFont());
        if (excelFileMenuItemFont != null) excelFileMenuItem.setFont(excelFileMenuItemFont);
        excelFileMenuItem.setText("Excel file");
        saveAsMenu.add(excelFileMenuItem);
        textFileMenuItem = new JMenuItem();
        Font textFileMenuItemFont = this.$$$getFont$$$(null, -1, 14, textFileMenuItem.getFont());
        if (textFileMenuItemFont != null) textFileMenuItem.setFont(textFileMenuItemFont);
        textFileMenuItem.setText("Text file");
        saveAsMenu.add(textFileMenuItem);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        mainPanel.add(panel1, BorderLayout.CENTER);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, BorderLayout.CENTER);
        statisticTable.setAutoCreateRowSorter(true);
        statisticTable.setCellSelectionEnabled(true);
        Font statisticTableFont = this.$$$getFont$$$(null, -1, 14, statisticTable.getFont());
        if (statisticTableFont != null) statisticTable.setFont(statisticTableFont);
        scrollPane1.setViewportView(statisticTable);
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
