import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
/**
 * Class to represent a timer to keep the channels and their tableau
 * up to date. The timer is set to a fixed update rate of one hour.
 * @author dv18mln
 */
public class ScheduledUpdate extends TimerTask {

    private Gui gui;
    private Call call;
    private Parser parser;
    private Object lock;
    private RadioInfo info;
    private ArrayList<Channel> channelList;
    int progress;


    public ScheduledUpdate(RadioInfo info, Gui gui, Call call, Parser parser, Object lock){
        this.info = info;
        this.gui = gui;
        this.call = call;
        this.parser = parser;
        this.lock = lock;
        progress = 0;
    }

    /**
     * Method to be run every hour. The method works the
     * same as the UpdateListener and retrieves new
     * data from the API.
     */
    @Override
    public void run() {

        SwingUtilities.invokeLater(()->{
            gui.showLoadingScreen();
            gui.enableUpdate(false);
            gui.clear();
        });

        SwingWorker<Void, Void> task = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                synchronized (lock) {
                    InputStream in = call.getChannels(
                            "http://api.sr.se/api/v2/channels");
                    channelList = parser.readChannels(call, in);

                    for (int i = 0; i < channelList.size(); i++) {

                        Channel ch = channelList.get(i);

                        try {
                            Parser temp = new Parser();
                            ArrayList<Program> programs = new ArrayList<>();
                            for (int j = -1; j <= 1; j++) {
                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.DATE, j);
                                programs.addAll(temp.readChannelTab(call,
                                        call.getTableau(ch.getId(), cal.getTime())));
                            }
                            ch.setPrograms(programs);
                        } catch (IOException e) {
                            System.err.println("Could not get programs for channel ." + ch.getId());
                        }
                        publish();
                    }
                    return null;
                }
            }

            @Override
            protected void process(List<Void> chunks) {
                progress = (100/52);
                gui.increaseProgressbar(progress);
            }

            @Override
            protected void done() {

                for (Channel ch : channelList) {
                    gui.setChannelTab(ch);
                }

                gui.setLast();
                parser.createDoc(channelList);
                gui.setLastUpdated(Calendar.getInstance());
                gui.enableUpdate(true);
                System.out.println("Done");
            }

        };
        task.execute();
        synchronized (lock){
            //set timer after load is completed.
            info.setTimer(60*60*1000);
        }
    }
}
