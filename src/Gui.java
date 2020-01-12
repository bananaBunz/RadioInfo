import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Class to represent the view in the MvC
 * @author dv18mln
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

    /**
     * The constructor initializes a very simple
     * gui with a "loading screen" containing a
     * text and a progress bar to show the user
     * that the program is actually working in
     * the background. In the gui, also, the last time
     * the channel-list was updated is show.
     */
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

    /**
     * Displays the information gotten from the
     * channel object. The data is displayed in
     * a JTabbedPane with the associated channel-name
     * and channel-image.
     * @param channel Channel to be set.
     */
    public void setChannelTab(Channel channel){
        increaseProgressbar(0);
        ArrayList programs = channel.getPrograms();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(programTable(programs)));
        ImageIcon imageIcon = null;
        if(channel.getImage() != null){
            try {
                imageIcon = new ImageIcon(new URL(channel.getImage()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if(imageIcon != null){
                Image image = imageIcon.getImage();
                Image newimg = image.getScaledInstance(15, 15,  java.awt.Image.SCALE_SMOOTH);
                imageIcon = new ImageIcon(newimg);
            }

        }
        tabbedPane.addTab(channel.getName(), imageIcon, panel);
        frame.add(tabbedPane);
    }

    /**
     * Displays the data gotten from the
     * program object. The data is displayed in a JTable
     * with the program-name and associated start and end time.
     * @param programs Arraylist of the programs belonging to a
     *                 certain channel.
     * @return A JTable with the data.
     */
    public JTable programTable(ArrayList<Program> programs){

        RadioTableModel model = new RadioTableModel();
        JTable table = new JTable(model);

        table.setDefaultRenderer(table.getColumnClass(0), new RadioCellRenderer());
        table.setDefaultRenderer(table.getColumnClass(1), new RadioCellRenderer());
        table.setDefaultRenderer(table.getColumnClass(2), new RadioCellRenderer());
        for(Program program : programs){

            model.addRow(program);

        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                Program program = (Program)model.getValueAtRow(row);
                ImageIcon imageIcon = null;
                if(program.getImage().length() > 0){
                    try {
                        URL url = new URL(program.getImage());
                        imageIcon = new ImageIcon(url);
                        if(imageIcon.getImage() != null){
                            Image image = imageIcon.getImage();
                            Image newimg = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH);
                            imageIcon = new ImageIcon(newimg);
                        }
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                        imageIcon = null;
                    }

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

    /**
     * Method containing settings that should be set last.
     * Hides the loading-screen from the gui and sets the
     * last-updated text.
     */
    public void setLast(){
        showLoadingScreen(false);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date d = new Date();
        timeLabel.setText("Senast uppdaterad: " + format.format(d));
        frame.setVisible(true);
    }

    /**
     * Setts the menubar in the gui. The menubar contains
     * simple information about the program and how to use it.
     * The menubar also provides a button to update all channels
     * and their tableau.
     */
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

    /**
     * Setts and removes the loading screen and
     * setts the value of the progressbar.
     * @param b Bool to determine if to show or hide the loading screen.
     */
    public void showLoadingScreen(boolean b){
        progressBar.setValue(0);
        loadingScreen.setVisible(b);
        loadingScreen.setEnabled(b);
    }

    /**
     * Setts the listener for the update-button.
     * @param listener the actionlistener.
     */
    public void setUpdateListener(ActionListener listener){
        update.addActionListener(listener);
    }

    /**
     * Clears the JTabbedPane when updating the
     * channels and their tableau.
     */
    public void clear(){
        tabbedPane = new JTabbedPane();
    }

    /**
     * Setts the value to the progressbar when
     * loading the channels.
     * @param amount The amount to set the progressbar to.
     */
    public void increaseProgressbar(int amount){
        progressBar.setValue(amount);
    }

}