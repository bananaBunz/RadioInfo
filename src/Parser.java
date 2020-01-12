import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private Calendar lastUpdate;
    private String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Constructor initialized the document builder.
     */
    public Parser(){
        lastUpdate = null;
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

                ch.setImage(imageElement.getTextContent());

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
                        program.setImage(imageurl.getTextContent());
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


        Date date = null;
        try {
            date = new SimpleDateFormat(DATE_FORMAT_PATTERN).parse(timeAsString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public boolean shouldUpdate(){

        File file = new File("channels.xml");
        if(file.exists()){

            try {
                doc = builder.parse(file);

                Node node = doc.getDocumentElement();
                Element element = (Element)node;

                String lastupdateAsString = element.getAttribute("lastupdate");

                Calendar date = Calendar.getInstance();
                date.setTime(new SimpleDateFormat(DATE_FORMAT_PATTERN).parse(lastupdateAsString));
                lastUpdate = date;
                Calendar now = Calendar.getInstance();
                long differenceMillis = now.getTimeInMillis() - date.getTimeInMillis();
                long differenceHours = (((differenceMillis)/1000L)/60L)/60L;
                if(differenceHours >= 1){
                    return true;
                }
                else{
                    return false;
                }

            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        lastUpdate = Calendar.getInstance();
        return true;
    }

    public void createDoc(ArrayList<Channel> channelList){

        try{
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document document = docBuilder.newDocument();
            Element rootElement = document.createElement("channels");

            Date date = new Date();

            Attr attr  = document.createAttribute("lastupdate");
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
            attr.setValue(format.format(date));
            rootElement.setAttributeNode(attr);
            document.appendChild(rootElement);

            for(Channel ch : channelList){

                Element channel = document.createElement("channel");
                rootElement.appendChild(channel);
                Attr chName = document.createAttribute("name");
                chName.setValue(ch.getName());
                channel.setAttributeNode(chName);

                Element image = document.createElement("image");
                image.appendChild(document.createTextNode(ch.getImage()));
                channel.appendChild(image);

                Element programsElement = document.createElement("programs");
                channel.appendChild(programsElement);

                ArrayList<Program> programs = ch.getPrograms();
                for(Program program : programs){

                    Element programElement = document.createElement("program");

                    Element progName = document.createElement("title");
                    progName.appendChild(document.createTextNode(program.getTitle()));
                    programElement.appendChild(progName);

                    Element progDesc = document.createElement("description");
                    progDesc.appendChild(document.createTextNode(program.getDescription()));
                    programElement.appendChild(progDesc);
                    Element progImage = document.createElement("image");
                    if(program.getImage() != null){
                        progImage.appendChild(document.createTextNode(program.getImage()));
                    }
                    programElement.appendChild(progImage);

                    Element progStart = document.createElement("starttime");
                    progStart.appendChild(document.createTextNode(format.format(program.getStartTime())));
                    programElement.appendChild(progStart);

                    Element progEnd = document.createElement("endtime");
                    progEnd.appendChild(document.createTextNode(format.format(program.getEndTime())));
                    programElement.appendChild(progEnd);

                    programsElement.appendChild(programElement);
                }
                channel.appendChild(programsElement);
                rootElement.appendChild(channel);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            //Settings found on https://stackoverflow.com/a/13925622/10617362 to
            //format you xml-document.
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("channels.xml"));

            transformer.transform(domSource, streamResult);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Channel> readLocal(String fileName){
        ArrayList<Channel> channels = new ArrayList<>();

        try {
            doc = builder.parse(fileName);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Läst så länge det finns channels
        * för varje channel läst programs*/

        NodeList channelNodes = doc.getElementsByTagName("channel");

        for(int i = 0; i < channelNodes.getLength(); i++){

            Node node = channelNodes.item(i);

            if(node.getNodeType() == Node.ELEMENT_NODE){
                Channel channel = new Channel();
                Element element = (Element)node;
                channel.setName(element.getAttribute("name"));
                channel.setImage(element.getElementsByTagName("image").item(0).getTextContent());

                NodeList programNodes = element.getElementsByTagName("program");
                ArrayList<Program> programs = new ArrayList<>();

                for(int j = 0; j < programNodes.getLength(); j++){

                    Node programNode = programNodes.item(j);

                    if(programNode.getNodeType() == Node.ELEMENT_NODE){

                        Program program = new Program();
                        Element progElement = (Element)programNode;

                        program.setTitle(progElement.getElementsByTagName("title").item(0).getTextContent());
                        program.setDescription(progElement.getElementsByTagName("description").item(0).getTextContent());
                        program.setImage(progElement.getElementsByTagName("image").item(0).getTextContent());

                        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
                        try {
                            Date starttime = format.parse(progElement.getElementsByTagName("starttime").item(0).getTextContent());
                            Date endtime = format.parse(progElement.getElementsByTagName("endtime").item(0).getTextContent());
                            Date now = new Date();
                            if(now.after(endtime)){
                                program.setEnded(true);
                            }
                            program.setStartTime(starttime);
                            program.setEndTime(endtime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        programs.add(program);
                    }

                }
                channel.setPrograms(programs);
                channels.add(channel);
            }

        }

        return channels;
    }
    public Calendar getLastUpdated(){
        return lastUpdate;
    }
}
