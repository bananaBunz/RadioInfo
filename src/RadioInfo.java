import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Class to represent the controller in the MvC-model.
 * The controller part initialized the gui and
 * starts the model to retrieve the data from
 * the given API. The controller uses multiple
 * threads to retrieve and parse the data for
 * each channel simultaneously.
 * @author dv18mln
 */

public class RadioInfo extends Thread{

    private Gui gui;
    private Call call;
    private Parser parser;

    /**
     * The constructor initializes the gui
     * with a "loading screen".
     * The gui is initialized on a own thread.
     */
    public RadioInfo(){

        call = new Call();
        parser = new Parser();
        //start by setting the menubar before getting data.
        SwingUtilities.invokeLater(()->{
            gui = new Gui();
            gui.setMenuBar();
            gui.setUpdateListener(new UpdateListener(call, parser, gui));
        });

    }

    /**
     * Method to handle the gui and model.
     * Retrieving data is done in a order where,
     * first, every channel is saved and, second,
     * every program scheduled for each channel is
     * retrieved. After the data is parsed
     * the gui is initialized with the respective
     * channel and tableau.
     * Parsing is done on multiple threads to speed
     * up the loading of data.
     *
     */
    public void init() {
        InputStream in = call.getChannels();
        ArrayList<Channel> channelList = parser.readChannels(in);

        //thread for every channel, retrieve data simultaneously.
        Thread[] t = new Thread[channelList.size()];

        for(int i = 0; i < channelList.size(); i++){
            Channel ch = channelList.get(i);
            t[i] = new Thread(() -> {
                try{
                    Parser temp = new Parser();
                    ArrayList<Program> programs = temp.readChannelTab(call, call.getTableau(ch.getId()));
                    ch.setPrograms(programs);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            });
            t[i].start();
        }
        int progress = 0;
        //wait for all
        for(int i = 0; i < channelList.size(); i++){
            try {
                t[i].join();
                progress+=100/channelList.size();
                gui.increaseProgressbar(progress);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {

            for(Channel ch : channelList){
                gui.setChannelTab(ch);
            }

            gui.setLast();

        });
    }

}
