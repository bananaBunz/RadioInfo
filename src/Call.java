import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Class to be a part of the model in the MvC.
 */
public class Call {

    private URL url;
    private URLConnection con;

    public Call(){

        url = null;
        con = null;

    }

    public InputStream getChannels(){

        try{
            url = new URL("http://api.sr.se/api/v2/channels");
            con = url.openConnection();
            return con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Gets the scheme for the specific channel. Gets the previous 12h and
     * the upcoming 12h of content.
     * @param id Id of channel to get tableau.
     * @return Inputstream of tableau as xml data.
     */
    public InputStream getTableau(String id){

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-dd");
        String dateString = formatter.format(today);
        try{
            url = new URL("http://api.sr.se/api/v2/scheduledepisodes?channelid="+id+"&date="+dateString);
            con = url.openConnection();
            return con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStream getNextTabPage(URL pageUrl){
        try {
            con = pageUrl.openConnection();
            return con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}