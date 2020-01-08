import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Class to be a part of the model in the MvC.
 * The parser reads xml-inputstreams with a
 * dom-parser.
 * @author dv18mln
 */

public class Parser {

    private Document doc;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

    /**
     * Constructor initialized the document builder.
     */
    public Parser(){
        try{
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e){
            e.printStackTrace();
        }
    }

    /**
     * Parsing the inputstream retrieved by the call-class.
     * The channels and data is parsed and saved to a arraylist.
     * @see Call
     * @param in Inputstream to read from.
     * @return An arraylist with the parsed channels.
     */
    public ArrayList<Channel> readChannels(InputStream in){

        ArrayList<Channel> channelList = new ArrayList<>();

        try {
            doc = builder.parse(in);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NodeList channels = doc.getElementsByTagName("channel");

        for(int i = 0; i < channels.getLength(); i++){
            Node n = channels.item(i);

            if(n.getNodeType() == Node.ELEMENT_NODE){
                Channel ch = new Channel();
                Element element = (Element)n;

                ch.setName(element.getAttribute("name"));
                ch.setId(element.getAttribute("id"));

                Element tagline = (Element)element.getElementsByTagName("tagline").item(0);
                ch.setTagLine(tagline.getTextContent());

                Element imageElement = (Element)element.getElementsByTagName("image").item(0);

                try {

                    URL url = new URL(imageElement.getTextContent());
                    Image image = ImageIO.read(url);
                    ch.setImage(image);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                channelList.add(ch);
            }

        }

        return channelList;
    }

    /**
     * Parses the given channels tableu.
     * Many times the programs is spread across
     * multiple pages which means the input stream
     * need to get switch and read the other pages
     * data. The programs added to the list is only
     * the programs that has occurred in the previous
     * 12h or occurs in the next 12h of that day.
     *
     * @param in Inputstream to read from.
     * @param call Call object to retrieve the next page.
     * @throws IOException If the data could not be read, throws IOException.
     * @return returns arraylist of all programs.
     */
    public ArrayList<Program> readChannelTab(Call call, InputStream in) throws IOException {

        ArrayList<Program> programList = new ArrayList<>();

        Document tabDoc = null;
        DocumentBuilderFactory tabFactory;
        DocumentBuilder tabBuilder = null;

        try {
            tabFactory = DocumentBuilderFactory.newInstance();
            tabBuilder = tabFactory.newDocumentBuilder();
            tabDoc = tabBuilder.parse(in);

        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }

        assert tabDoc != null;

        NodeList pageSizeNode = tabDoc.getElementsByTagName("totalpages");
        int pageSize = Integer.parseInt(pageSizeNode.item(0).getTextContent());

        for(int i = 0; i < pageSize; i++){
            NodeList programNode = tabDoc.getElementsByTagName("scheduledepisode");

            for(int j = 0; j < programNode.getLength(); j++){
                boolean shouldBeAdded = true;
                Program program = new Program();
                Node episodeNode = programNode.item(j);

                if(episodeNode.getNodeType() == Node.ELEMENT_NODE){

                    Element element = (Element)episodeNode;

                    Element title = (Element)element.getElementsByTagName("title").item(0);
                    program.setTitle(title.getTextContent());

                    Element programElement = (Element)element.getElementsByTagName("program").item(0);
                    program.setName(programElement.getAttribute("name"));

                    Element description = (Element)element.getElementsByTagName("description").item(0);
                    program.setDescription(description.getTextContent());

                    Element imageurl = (Element)element.getElementsByTagName("imageurl").item(0);

                    if(imageurl != null){
                        try {
                            URL url = new URL(imageurl.getTextContent());
                            Image image = ImageIO.read(url);
                            program.setImage(image);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Element startElement = (Element)element.getElementsByTagName("starttimeutc").item(0);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR_OF_DAY, -12);
                    Date beforeDate = calendar.getTime();
                    Date startDate = parseDate(startElement.getTextContent());

                    if(startDate.before(beforeDate)){
                        shouldBeAdded = false;
                    }
                    program.setStartTime(startDate);

                    Element endElement = (Element)element.getElementsByTagName("endtimeutc").item(0);
                    Date currentDate = new Date();
                    Date endDate = parseDate(endElement.getTextContent());
                    if(currentDate.after(endDate)){
                        program.setEnded(true);
                    }

                    calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR_OF_DAY, 12);
                    Date afterDate = calendar.getTime();
                    startDate = parseDate(endElement.getTextContent());

                    if(startDate.after(afterDate)){
                        shouldBeAdded = false;
                    }
                    program.setEndTime(endDate);

                }
                if(shouldBeAdded){
                    programList.add(program);
                }
            }

            //get next page
            //No "nextpage" tag exists on the last page.
            if(i < pageSize-1){
                try {
                    NodeList page = tabDoc.getElementsByTagName("nextpage");
                    System.out.println(page.item(0).getTextContent());
                    URL url = new URL(page.item(0).getTextContent());
                    tabDoc = tabBuilder.parse(call.getNextTabPage(url));
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }

        }

        return programList;
    }

    /**
     * Parses a string to a time object. This is used to determine
     * whether the program should be added to the list or not,
     * as the time retrieved from the data/inputstream is read
     * as a string.
     * @param timeAsString The times as a string to be parsed.
     * @return A date-object with the parsed time.
     */
    public Date parseDate(String timeAsString){

        String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

        Date date = null;
        try {
            date = new SimpleDateFormat(DATE_FORMAT_PATTERN).parse(timeAsString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

}
