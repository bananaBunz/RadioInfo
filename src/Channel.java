
import java.net.URL;
import java.util.ArrayList;

/**
 * Class used to store the data when
 * parsing the inputstreams.
 * @author dv18mln
 */
public class Channel {

    private String name;
    private String id;
    private String tagLine;
    private URL audioUrl;
    private String image;
    private ArrayList programs;

    /**
     * Initialize everything to null.
     */
    public Channel(){
        name = null;
        id = null;
        tagLine = null;
        audioUrl = null;
        image = null;
    }

    /**
     * Gets the name of the channel.
     * @return Name of channel as string.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the channel.
     * @param name The name of the channel.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the id of the channel.
     * @return The id of the channel as string.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the channel
     * @param id The id of the channel.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the tagline of the channel.
     * @return The tagline as string.
     */
    public String getTagLine() {
        return tagLine;
    }

    /**
     * Sets the tag line of the channel.
     * @param tagLine The tagline.
     */
    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    /**
     * Gets the url to the live audio.
     * With this url the user can listen to
     * the current live radio. This is not used
     * in the current state of the program.
     * @return Url of the audio.
     */
    public URL getAudioUrl() {
        return audioUrl;
    }

    /**
     * Sets the url of the live audio.
     * @param audioUrl The url of the audio.
     */
    public void setAudioUrl(URL audioUrl) {
        this.audioUrl = audioUrl;
    }

    /**
     * Gets the image associated with the channel.
     * @return The image as a image object.
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the image of the channel.
     * @param image The image.
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets the list of programs of the channel.
     * @return The list of programs as arraylist.
     */
    public ArrayList getPrograms() {
        return programs;
    }

    /**
     * Sets the programs of the current channel.
     * @param programs The list of programs.
     */
    public void setPrograms(ArrayList programs) {
        this.programs = programs;
    }
}
