import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;

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
    private boolean running;
    private final String url = "http://api.sr.se/api/v2/channels";
    private ScheduledUpdate scheduledUpdate;
    private Object lock;

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
        lock = new Object();
        running = false;
        //start by setting the menubar before getting data.
        SwingUtilities.invokeLater(()->{
            gui = new Gui();
            gui.setMenuBar();
            gui.setUpdateListener(new UpdateListener(this, call, parser, gui, lock));
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
        setTimer(delay);
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

        //thread for every channel, retrieve data simultaneously.
        Thread[] t = new Thread[channelList.size()];

        for(int i = 0; i < channelList.size(); i++){
            Channel ch = channelList.get(i);
            t[i] = new Thread(() -> {
                try{
                    Parser temp = new Parser();
                    ArrayList<Program> programs = new ArrayList<>();
                    for(int j = -1; j <= 1; j++){
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, j);
                        programs.addAll(temp.readChannelTab(call,
                                call.getTableau(ch.getId(), cal.getTime())));
                    }
                    ch.setPrograms(programs);
                }
                catch (IOException e){
                    System.err.println("Could not get programs for channel ." + ch.getId());
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

            //for some reason prints one hour in adv (utc+2)
            Calendar lastUpdate = parser.getLastUpdated();
            lastUpdate.add(Calendar.HOUR, -1);
            gui.setLastUpdated(lastUpdate);
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

            Calendar lastUpdate = parser.getLastUpdated();
            lastUpdate.add(Calendar.HOUR, -1);
            gui.setLastUpdated(lastUpdate);
            gui.setLast();
            gui.enableUpdate(true);
        });

    }

    /**
     * Sets a new timer with a timertask as scheduleupdate.
     * @param delay Time to wait before performing the update.
     */
    public void setTimer(long delay){
        cancleTimer();
        System.out.println("set timer");
        timer = new Timer();
        timer.schedule(new ScheduledUpdate(this, gui, call, parser, lock), delay);
        running = true;
    }

    /**
     * Cancles the timer if active.
     */
    public void cancleTimer(){
        if(running){
            timer.cancel();
            timer.purge();
            running = false;
        }
    }
}
