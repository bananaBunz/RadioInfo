import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UpdateListener implements ActionListener {

    private Call call;
    private Parser parser;
    private Gui gui;

    public UpdateListener(Call call, Parser parser, Gui gui){

        this.call = call;
        this.parser = parser;
        this.gui = gui;

    }

    @Override
    public void actionPerformed(ActionEvent event) {

        SwingWorker update = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {

                gui.showLoadingScreen(true);
                gui.clear();

                InputStream in = call.getChannels();
                ArrayList<Channel> channelList = parser.readChannels(in);

                for(Channel ch : channelList){
                    try{
                        ArrayList<Program> programs = parser.readChannelTab(call, call.getTableau(ch.getId()));
                        ch.setPrograms(programs);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }

                for(Channel ch : channelList){
                    gui.setChannelTab(ch);
                }
                gui.setLast();

                return null;
            }
        };
        update.execute();
    }
}
