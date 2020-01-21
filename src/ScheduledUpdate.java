import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
    public void run() {
        timer.cancel();
        timer.purge();
        SwingWorker<Void, Void> task = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {

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
                        ArrayList<Program> programs = temp.readChannelTab(call,
                                call.getTableau(ch.getId()));

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
