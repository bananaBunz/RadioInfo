import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * Class to represent the controller in the MvC-model.
 */

public class RadioInfo extends Thread{

    private Gui gui;
    private Call call;
    private Parser p;
    private Object lock;

    public RadioInfo(){


        call = new Call();
        p = new Parser();
        lock = new Object();
        //start by setting the menubar before getting data.
        SwingUtilities.invokeLater(()->{
            gui = new Gui();
            gui.setMenuBar();
            gui.setUpdateListener(new UpdateListener(call, p, gui));
        });


        InputStream in = call.getChannels();
        ArrayList<Channel> channelList = p.readChannels(in);

        Thread[] t = new Thread[channelList.size()];

        for(int i = 0; i < channelList.size(); i++){

            Channel ch = channelList.get(i);

            t[i] = new Thread(() -> {
                try{
                    ArrayList<Program> programs = p.readChannelTab(call, call.getTableau(ch.getId()));
                    ch.setPrograms(programs);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            });
            t[i].start();
        }

        for(int i = 0; i < channelList.size(); i++){
            try {
                t[i].join();
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
