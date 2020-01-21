import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.Vector;

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
    private Timer timer;
    private final String url = "http://api.sr.se/api/v2/channels";

    /**
     * The constructor initializes the gui
     * with a "loading screen".
     * The gui is initialized on a own thread.
     * After everything is loaded a timer is set
     * to execute an update every hour.
     */
    public RadioInfo(){

        call = new Call();
        parser = new Parser();
        timer = new Timer();
        //start by setting the menubar before getting data.
        SwingUtilities.invokeLater(()->{
            gui = new Gui();
            gui.setMenuBar();
            gui.setUpdateListener(new UpdateListener(timer, call, parser, gui));
        });


    }

    /**
     * Method to initialize the model and parse the data.
     */
    public void init(){

        if(parser.shouldUpdate()){
            initNew();
        }
        else{
            initLocal();
        }

        Calendar nextUpdate = parser.getLastUpdated();
        if(nextUpdate == null){
            nextUpdate = Calendar.getInstance();
        }
        nextUpdate.add(Calendar.HOUR, 1);
        Calendar now = Calendar.getInstance();
        long delay = (nextUpdate.getTimeInMillis()-now.getTimeInMillis());
        if(delay < 0) delay = 0;
        timer.schedule(new ScheduledUpdate(timer, gui, call, parser), delay);

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
    private void initNew() {
        InputStream in = call.getChannels(url);
        ArrayList<Channel> channelList = parser.readChannels(call, in);
        System.out.println(channelList.size());

        //thread for every channel, retrieve data simultaneously.
        Thread[] t = new Thread[channelList.size()];

        for(int i = 0; i < channelList.size(); i++){
            Channel ch = channelList.get(i);
            t[i] = new Thread(() -> {
                try{
                    Parser temp = new Parser();
                    ArrayList<Program> programs = temp.readChannelTab(call,
                            call.getTableau(ch.getId()));
                    ch.setPrograms(programs);
                }
                catch (IOException e){
                }

            });
            t[i].start();
        }
        for(int i = 0; i < channelList.size(); i++){
            try {
                t[i].join();
                gui.increaseProgressbar(100/channelList.size());
            } catch (InterruptedException e) {
                System.err.println("Could not join threads.");
            }
        }

        SwingUtilities.invokeLater(()->{
            for(Channel ch : channelList){
                gui.setChannelTab(ch);
            }

            gui.setLast();
            gui.enableUpdate(true);
        });

        parser.createDoc(channelList);

    }

    /**
     * Method run if there exist a local, saved
     * version of the channels and their scheduled
     * programs updated under an hour ago. If it
     * exits, there is no need to make the API
     * calls to retrieve new data, we can just load
     * the local data.
     */
    private void initLocal(){
        ArrayList<Channel> channelList = parser.readLocal("channels.xml");
        if(channelList == null){
            initNew();
            return;
        }

        SwingUtilities.invokeLater(()->{
            for(Channel ch : channelList){
                gui.setChannelTab(ch);
                gui.increaseProgressbar(100/channelList.size());
            }

            gui.setLast();
            gui.enableUpdate(true);
        });

    }
}
