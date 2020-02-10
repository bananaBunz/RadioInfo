import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Timer;

/**
 * Class to represent a timer to keep the channels and their tableau
 * up to date. The timer is set to a fixed update rate of one hour.
 * @author dv18mln
 */
public class ScheduledUpdate extends TimerTask {

    private Timer timer;
    private Gui gui;
    private Call call;
    private Parser parser;

    public ScheduledUpdate(Timer timer, Gui gui, Call call, Parser parser){
        this.timer = timer;
        this.gui = gui;
        this.call = call;
        this.parser = parser;
    }

    /**
     * Method to be run every hour. The method works the
     * same as the UpdateListener and retrieves new
     * data from the API.
     */
    @Override
    synchronized public void run() {
        timer.cancel();
        timer.purge();
        SwingWorker<Void, Void> task = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {

                gui.showLoadingScreen();
                gui.enableUpdate(false);
                gui.clear();

                InputStream in = call.getChannels(
                        "http://api.sr.se/api/v2/channels");
                ArrayList<Channel> channelList = parser.readChannels(call, in);

                for (int i = 0; i < channelList.size(); i++) {

                    Channel ch = channelList.get(i);

                    try {
                        Parser temp = new Parser();
                        ArrayList<Program> programs = new ArrayList<>();
                        for(int j = 0; j <= 1; j++){
                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DATE, j);
                            programs.addAll(temp.readChannelTab(call,
                                    call.getTableau(ch.getId(), cal.getTime())));
                        }
                        ch.setPrograms(programs);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                int progress = 0;
                for (Channel ch : channelList) {
                    progress+=100/channelList.size();
                    gui.increaseProgressbar(progress);
                    gui.setChannelTab(ch);
                }


                gui.setLast();
                parser.createDoc(channelList);
                gui.setLastUpdated(Calendar.getInstance());
                gui.enableUpdate(true);

                return null;
            }

        };
        task.execute();
        timer = new Timer();
        timer.schedule(new ScheduledUpdate(timer, gui, call, parser),
                1000*60*60);
    }
}
