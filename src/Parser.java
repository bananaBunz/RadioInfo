import org.w3c.dom.*;
import org.xml.sax.SAXException;

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
import java.util.TimeZone;

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
    private final Object lock;

    /**
     * Constructor initialized the document builder.
     */
    public Parser(){
        lock = new Object();
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
    public ArrayList<Channel> readChannels(Call call, InputStream in){

        ArrayList<Channel> channelList = new ArrayList<>();

        try {
            doc = builder.parse(in);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NodeList pageSizeNode = doc.getElementsByTagName("totalpages");
        int pageSize = Integer.parseInt(pageSizeNode.item(0).getTextContent());

        for(int j = 0; j < pageSize; j++){

            NodeList channels = doc.getElementsByTagName("channel");
            for(int i = 0; i < channels.getLength(); i++){
                Node n = channels.item(i);

                if(n.getNodeType() == Node.ELEMENT_NODE){
                    Channel ch = new Channel();
                    Element element = (Element)n;

                    ch.setName(element.getAttribute("name"));
                    ch.setId(element.getAttribute("id"));
                    Element tagline = (Element)element.
                            getElementsByTagName("tagline").item(0);
                    ch.setTagLine(tagline.getTextContent());

                    Element imageElement = (Element)element.
                            getElementsByTagName("image").item(0);

                    if(imageElement != null){
                        ch.setImage(imageElement.getTextContent());
                    }

                    channelList.add(ch);
                }

            }
            //get next page
            if(j < pageSize-1){
                try {
                    NodeList page = doc.getElementsByTagName("nextpage");
                    doc = builder.parse(call.getChannels(page.
                            item(0).getTextContent()));
                } catch (SAXException e) {
                    System.err.println("Program exited.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
    public ArrayList<Program>  readChannelTab(Call call, InputStream in)
            throws IOException {

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
        int pageSize = Integer.parseInt(pageSizeNode.item(0).
                getTextContent());

        for(int i = 0; i < pageSize; i++){
            NodeList programNode = tabDoc.
                    getElementsByTagName("scheduledepisode");

            for(int j = 0; j < programNode.getLength(); j++){
                boolean shouldBeAdded = true;
                Program program = new Program();
                Node episodeNode = programNode.item(j);

                if(episodeNode.getNodeType() == Node.ELEMENT_NODE){

                    Element element = (Element)episodeNode;

                    Element title = (Element)element.
                            getElementsByTagName("title").item(0);
                    program.setTitle(title.getTextContent());

                    Element programElement = (Element)element.
                            getElementsByTagName("program").item(0);
                    program.setName(programElement.getAttribute("name"));

                    Element description = (Element)element.
                            getElementsByTagName("description").item(0);
                    if(description != null){
                        program.setDescription(description.getTextContent());
                    }

                    Element imageurl = (Element)element.
                            getElementsByTagName("imageurl").item(0);

                    if(imageurl != null){
                        program.setImage(imageurl.getTextContent());
                    }

                    Element startElement = (Element)element.
                            getElementsByTagName("starttimeutc").item(0);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR_OF_DAY, -12);
                    Date beforeDate = calendar.getTime();
                    Date startDate = parseDate(startElement.getTextContent());
                    program.setStartTime(startDate);

                    Element endElement = (Element)element.
                            getElementsByTagName("endtimeutc").item(0);
                    Date currentDate = new Date();
                    Date endDate = parseDate(endElement.
                            getTextContent());
                    if(currentDate.after(endDate)){
                        program.setEnded(true);
                    }
                    if(endDate.before(beforeDate)){
                        shouldBeAdded = false;
                    }
                    calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR_OF_DAY, 12);
                    Date afterDate = calendar.getTime();
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
            if(i < pageSize-1){
                try {
                    NodeList page = tabDoc.getElementsByTagName("nextpage");
                    URL url = new URL(page.item(0).getTextContent());
                    tabDoc = tabBuilder.parse(call.getNextPage(url));
                } catch (SAXException e) {
                    System.err.println("Program exited.");
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

        Calendar convertedDate = null;
        try {
            Date date = new SimpleDateFormat(DATE_FORMAT_PATTERN).
                    parse(timeAsString);
            convertedDate = Calendar.getInstance();
            convertedDate.setTime(date);
            convertedDate.add(Calendar.HOUR, 1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertedDate.getTime();
    }

    /**
     * Method to determine if the local saved data should be
     * updated. If the data has been saved under an hour ago,
     * there is no need to update it again. If it is over an
     * hour ago, the method return true to indicate that
     * there should be a new api call to update the data.
     * @return boolean if data should be updated.
     */
    public boolean shouldUpdate(){
        File file = new File("channels.xml");
        if(file.exists()){

            try {
                doc = builder.parse(file);

                Node node = doc.getDocumentElement();
                Element element = (Element)node;

                String lastupdateAsString = element.getAttribute("lastupdate");

                Calendar date = Calendar.getInstance();
                date.setTime(new SimpleDateFormat(DATE_FORMAT_PATTERN).
                        parse(lastupdateAsString));

                lastUpdate = date;

                Calendar now = Calendar.getInstance();
                long differenceMillis = (now.getTimeInMillis() -
                        date.getTimeInMillis());
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

    /**
     * Method to save the data retrieved from the api
     * call. This is stored in a xml-file which can be parsed
     * quicker than making a the api calls for each time
     * the program is started. At the first start there
     * is no saved channels and the api call has to be done.
     * @param channelList The list of channels and their programs to be saved.
     */
    public void createDoc(ArrayList<Channel> channelList){
        synchronized (lock) {

            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.
                        newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                Document document = docBuilder.newDocument();
                Element rootElement = document.createElement("channels");

                Calendar date = Calendar.getInstance();

                Attr attr = document.createAttribute("lastupdate");
                SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
                attr.setValue(format.format(date.getTime()));
                rootElement.setAttributeNode(attr);
                document.appendChild(rootElement);

                for (Channel ch : channelList) {

                    if (ch.getPrograms() == null) continue;

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
                    for (Program program : programs) {
                        addElement(document, format, programsElement, program);
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

            } catch (ParserConfigurationException | TransformerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds program-elements to the xml-file. The program-tag
     * contains title, description, image, start and end-time.
     * @param document Document where the tag should be added.
     * @param format   Simpledateformat to format the start and end time.
     * @param programsElement Element to append with a program.
     * @param program Program containing the information that should be set
     *                to the program-element.
     */
    private void addElement(Document document, SimpleDateFormat format, Element programsElement, Program program) {

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

    /**
     * Method to read the local saved data.
     * The method takes the filename as argument.
     * The data parsed in stored in a list of channels
     * which can be used by the GUI. This way is faster
     * then retrieving new information every time the
     * program is started.
     * @param fileName Name of file as string.
     * @return Arraylist of parsed channels.
     */
    public ArrayList<Channel> readLocal(String fileName){
        ArrayList<Channel> channels = new ArrayList<>();

        try {
            doc = builder.parse(fileName);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    /**
     * Gets the last time the data was updated.
     * @return
     */
    public Calendar getLastUpdated(){
        return lastUpdate;
    }
}
