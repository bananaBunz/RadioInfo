import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

class RadioTableModel extends AbstractTableModel{

    final String[] columnNames = {"Program", "Start tid","Slut tid"};

    final Class[] columnClasses = {String.class, String.class, String.class};

    final Vector data = new Vector();

    public void addRow(Program program){
        data.add(program);
        fireTableRowsInserted(data.size()-1, data.size()-1);
    }
    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Class getColumnClass(int c) {
        return columnClasses.getClass();
    }

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
    public Object getValueAtRow(int row) {
        Program program = (Program) data.elementAt(row);
        return program;
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

}

class RadioCellRenderer extends DefaultTableCellRenderer {

    JFrame frame;
    public RadioCellRenderer(JFrame frame){
        this.frame = frame;
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
    {
        RadioTableModel rtm = (RadioTableModel) table.getModel();
        Program program = (Program) rtm.getValueAtRow(row);

        if (program.hasEnded()) {
            //255,198,195
            setBackground(new Color(255,198,195));
        }
        else {
            setBackground(Color.white);
        }

        return super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
    }
}
