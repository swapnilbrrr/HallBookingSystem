package ui.components;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A read-only, single-selection table that remembers which object produced each
 * row, so a screen can act on the selected entity without re-parsing cell text.
 *
 * Nine screens in this application show a "view and filter" table; writing that
 * behaviour once here is what keeps each of them short.
 *
 * @param <T> the entity type displayed, one per row
 */
public class TablePanel<T> extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel model;
    private final JTable table;
    private final List<T> rowObjects = new ArrayList<>();
    private final Function<T, Object[]> rowMapper;

    /**
     * @param columns   column headings
     * @param rowMapper turns one entity into its cell values, in column order
     */
    public TablePanel(String[] columns, Function<T, Object[]> rowMapper) {
        super(new BorderLayout());
        this.rowMapper = rowMapper;

        this.model = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // The table reports data; it is not an editor.
            }
        };

        this.table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /** Replaces every row, clearing the current selection. */
    public void setRows(List<T> items) {
        rowObjects.clear();
        model.setRowCount(0);
        for (T item : items) {
            rowObjects.add(item);
            model.addRow(rowMapper.apply(item));
        }
    }

    /** The entity behind the selected row, or null when nothing is selected. */
    public T getSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow < rowObjects.size() ? rowObjects.get(modelRow) : null;
    }

    public boolean hasSelection() {
        return getSelected() != null;
    }

    public int getRowCount() {
        return rowObjects.size();
    }

    public JTable getTable() {
        return table;
    }

    /** Sets a preferred width for one column, by index. */
    public void setColumnWidth(int columnIndex, int width) {
        if (columnIndex < table.getColumnModel().getColumnCount()) {
            table.getColumnModel().getColumn(columnIndex).setPreferredWidth(width);
        }
    }
}
