import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Class to customize the table used in the gui.
 * @author dv18mln
 */
class RadioTableModel extends AbstractTableModel{

    final String[] columnNames = {"Program", "Start tid","Slut tid"};

    final Class[] columnClasses = {String.class, String.class, String.class};

    final Vector data = new Vector();

    /**
     * Adds a program to the data-list
     * and updates the table.
     * @param program program to insert.
     */
    public void addRow(Program program){
        data.add(program);
        fireTableRowsInserted(data.size()-1, data.size()-1); }

    /**
     * Gets the number of columns.
     * @return The number of columns.
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Gets the number of rows.
     * @return The number of rows.
     */
    public int getRowCount() {
        return data.size();
    }

    /**
     * Gets the name of the column.
     * @param col The column to get the name of.
     * @return The name as string.
     */
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * Gets the class of the column at given index.
     * @param c The column to get.
     * @return The class.
     */
    public Class getColumnClass(int c) {
        return columnClasses.getClass();
    }

    /**
     * Gets the data stored at a specific cell.
     * Depending of the cell the return is different.
     * If it's a cell in the first column, the method return a
     * string. If it's the second or third column, its return a
     * date-object.
     * @param row The row of the cell.
     * @param col The column of the cell.
     * @return Data stored in the cell.
     */
    public Object getValueAt(int row, int col) {
        Program program = (Program) data.elementAt(row);
        if (col == 0) return program.getTitle();

        else if (col == 1){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date startTime = program.getStartTime();
            return sdf.format(startTime);
        }
        else if (col == 2){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date endTime = program.getEndTime();
            return sdf.format(endTime);
        }
        else return null;
    }

    /**
     * The whole program object stored in the row.
     * @param row Index of the row.
     * @return The program stored at the given row.
     */
    public Object getValueAtRow(int row) {
        Program program = (Program) data.elementAt(row);
        return program;
    }

    /**
     * Checks if the given cell is editable.
     * @param row Index of row.
     * @param col Index of column.
     * @return True if editable, false if not.
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }

}
