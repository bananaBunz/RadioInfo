import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private RadioInfo info;
    private Object lock;

    /**
     * Constructor sets the already created
     * gui, call and parser.
     * @param call Call to be used.
     * @param parser Parser to be used.
     * @param gui Gui to be used.
     */
    public UpdateListener(RadioInfo info,Call call, Parser parser, Gui gui, Object lock){
        this.info = info;
        this.call = call;
        this.parser = parser;
        this.gui = gui;
        this.lock = lock;
    }

    /**
     * Run a update throgh scheduleupdate.
     * @param event Event passed from the actionlistener(not used).
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        info.cancleTimer();
        ScheduledUpdate update = new ScheduledUpdate(info, gui, call, parser, lock);
        update.run();
    }
}
