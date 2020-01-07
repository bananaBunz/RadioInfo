import java.awt.*;
import java.net.URL;
import java.util.ArrayList;

public class Channel {

    private String name;
    private String id;
    private String tagLine;
    private URL audioUrl;
    private Image image;
    private ArrayList programs;

    public Channel(){
        name = null;
        id = null;
        tagLine = null;
        audioUrl = null;
        image = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public URL getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(URL audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ArrayList getPrograms() {
        return programs;
    }

    public void setPrograms(ArrayList programs) {
        this.programs = programs;
    }
}
