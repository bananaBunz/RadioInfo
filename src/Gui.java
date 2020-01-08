import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Class to represent the view in the MvC
 */

public class Gui {

    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JMenu menu;
    private JMenuBar menuBar;
    private JPanel loadingScreen;
    private JMenuItem update;
    private JLabel timeLabel;
    private JPanel lastUpdatedPanel;
    private JProgressBar progressBar;


    public Gui(){

        frame = new JFrame("RadioInfo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(new Dimension(700, 700));
        loadingScreen = new JPanel();
        loadingScreen.add(new JLabel("Loading..."), BorderLayout.CENTER);
        progressBar = new JProgressBar(0,100);
        progressBar.setValue(0);
        loadingScreen.add(progressBar, BorderLayout.CENTER);
        timeLabel = new JLabel();
        lastUpdatedPanel = new JPanel();
        lastUpdatedPanel.add(timeLabel);
        frame.add(lastUpdatedPanel, BorderLayout.SOUTH);
        frame.add(loadingScreen);
        frame.setVisible(true);
        tabbedPane = new JTabbedPane();
        update = new JMenuItem("Uppdatera");
    }

    public void setChannelTab(Channel channel){
        increaseProgressbar(0);
        ArrayList programs = channel.getPrograms();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(programTable(programs)));
        ImageIcon imageIcon = new ImageIcon(channel.getImage());
        Image image = imageIcon.getImage();
        Image newimg = image.getScaledInstance(15, 15,  java.awt.Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(newimg);

        tabbedPane.addTab(channel.getName(), imageIcon, panel);
        frame.add(tabbedPane);
    }

    public JTable programTable(ArrayList<Program> programs){

        RadioTableModel model = new RadioTableModel();
        JTable table = new JTable(model);

        table.setDefaultRenderer(table.getColumnClass(0), new RadioCellRenderer(this.frame));
        table.setDefaultRenderer(table.getColumnClass(1), new RadioCellRenderer(this.frame));
        table.setDefaultRenderer(table.getColumnClass(2), new RadioCellRenderer(this.frame));
        for(Program program : programs){

            model.addRow(program);

        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                Program program = (Program)model.getValueAtRow(row);
                ImageIcon imageIcon = null;
                if(program.getImage() != null){
                    imageIcon = new ImageIcon(program.getImage());
                    Image image = imageIcon.getImage();
                    Image newimg = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH);
                    imageIcon = new ImageIcon(newimg);
                }
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Date startTime = program.getStartTime();
                Date endTime = program.getEndTime();
                JOptionPane.showMessageDialog(frame,
                        program.getDescription() +
                                "\nBörjar: " + sdf.format(startTime) +
                                "\nSlutar: " + sdf.format(endTime),
                        program.getName(),
                        JOptionPane.PLAIN_MESSAGE,
                        imageIcon);
            }
        });

        return table;
    }

    public void setLast(){
        showLoadingScreen(false);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date d = new Date();
        timeLabel.setText("Senast uppdaterad: " + format.format(d));
        frame.setVisible(true);
    }

    public void setMenuBar(){

        menu = new JMenu("Meny");
        menu.add(update);

        JMenuItem help = new JMenuItem("Hjälp");
        help.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Applikationen visualiserar de 12 senaste, och 12 kommande timmarna av sveriges radios\n" +
                        "program-schema. Datat är hämtat från sveriges radios API.",
                "Hjälp",
                JOptionPane.PLAIN_MESSAGE));

        menu.add(help);
        JMenuItem about = new JMenuItem("Om");
        menu.add(about);

        about.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Denna applikation är skapad för Umeå universitet.\n" +
                        "Author: Malte Lindgren, Email: dv18mln@cs.umu.se",
                "Om",
                JOptionPane.PLAIN_MESSAGE));

        menuBar = new JMenuBar();
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
    }

    public void showLoadingScreen(boolean b){
        progressBar.setValue(0);
        loadingScreen.setVisible(b);
        loadingScreen.setEnabled(b);
    }

    public void setUpdateListener(ActionListener listener){

        update.addActionListener(listener);

    }

    public void clear(){
        tabbedPane = new JTabbedPane();
    }

    public void increaseProgressbar(int amount){
        progressBar.setValue(amount);
    }

}