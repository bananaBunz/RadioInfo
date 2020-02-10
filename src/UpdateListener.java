import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;

/**
 * Action listener to update the channels and their
 * tableau. The listener reload all channels and their
 * programs: because of this, the listener uses multiple
 * threads to speed up the parsing.
 * @author dv18mln
 */
public class UpdateListener implements ActionListener {

    private Call call;
    private Parser parser;
    private Gui gui;
    private Timer timer;
    private ScheduledUpdate update;

    /**
     * Constructor sets the already created
     * gui, call and parser.
     * @param call Call to be used.
     * @param parser Parser to be used.
     * @param gui Gui to be used.
     */
    public UpdateListener(Timer timer,Call call, Parser parser, Gui gui){

        this.call = call;
        this.parser = parser;
        this.gui = gui;
        this.timer = timer;
        update = new ScheduledUpdate(timer, gui, call, parser);
    }

    /**
     * Run a update throgh scheduleupdate.
     * @param event Event passed from the actionlistener(not used).
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        update.run();
    }
}
