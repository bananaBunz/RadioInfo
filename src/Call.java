import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Class to be a part of the model in the MvC.
 * @author dv18mln
 */
public class Call {

    private URL url;
    private URLConnection con;

    /**
     * Initialized everything to null.
     */
    public Call(){

        url = null;
        con = null;

    }

    /**
     * Gets the channels from the Sveriges Radios api.
     * @return The data as an inputstream.
     */
    public InputStream getChannels(String url){

        try{
            this.url = new URL(url);
            con = this.url.openConnection();
            return con.getInputStream();
        } catch (IOException e) {
            System.err.println("Could not connect to the url");
            return null;
        }

    }

    /**
     * Gets the scheme for the specific channel. Gets the previous 12h and
     * the upcoming 12h of content.
     * @param id Id of channel to get tableau.
     * @return Inputstream of tableau as xml data.
     */
    public InputStream getTableau(String id, Date date) throws IOException{

        Vector<InputStream> streams = new Vector<>();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-dd");
        String dateString = formatter.format(date.getTime());
        URL tempUrl = new URL("http://api.sr.se/api/v2/scheduledepisodes?channelid="+id+"&date="+dateString);
        URLConnection tempCon = tempUrl.openConnection();
        InputStream in = new SequenceInputStream(streams.elements());
        return tempCon.getInputStream();
    }

    /**
     * Many channels have multiple pages of upcoming
     * programs. This method gets the next page of
     * the program-list.
     * @param pageUrl The url of the next page.
     * @return The inputstream retrieved from the call.
     */
    public synchronized InputStream getNextPage(URL pageUrl){
        try {
            URLConnection connection = pageUrl.openConnection();
            return connection.getInputStream();
        } catch (IOException e) {
            System.err.println("Could not connect to the next page url");
            return null;
        }
    }

}
