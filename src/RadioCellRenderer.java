import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Class to render the cell.
 * The class is used to paint the cells.
 */
class RadioCellRenderer extends DefaultTableCellRenderer {

    public RadioCellRenderer(){
    }

    /**
     * Inherited method. Used to customize the cells,
     * in this case only the color of the cell is
     * changed. If the program is over the cell
     * is set to a light red color.
     */
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