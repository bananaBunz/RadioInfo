import java.awt.*;
import java.util.Date;

public class Program {

    private String name;
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private boolean ended;
    private Image image;

    /**
     * Object to represent a program.
     * The program object is created form the parser
     * and later used to display the data in the gui.
     * @author dv81mln
     */
    public Program(){
        startTime = null;
        endTime = null;
        image = null;
    }

    /**
     * Getter for the programs title.
     * @return the title as string.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the programs title.
     * @param title the title as string.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for the programs description.
     * @return the description as string.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the programs description.
     * @param description the description as string.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for the programs start time.
     * @return the start time as date object.
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the programs start time.
     * @param startTime the start time.
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Getter for the programs end time.
     * @return the end time as date object.
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the programs end time.
     * @param endTime the end time.
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    /**
     * Getter for the programs name.
     * @return the name as string.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the programs name.
     * @param name the name as string.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter if the program has ended.
     * @return True if ended, false if not.
     */
    public boolean hasEnded() {
        return ended;
    }
    /**
     * Sets if the program has ended.
     * @param ended bool if ended or not.
     */
    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    /**
     * Getter for the programs image.
     * @return the image as image-object.
     */
    public Image getImage() {
        return image;
    }
    /**
     * Sets the programs image.
     * @param image the image as image-object.
     */
    public void setImage(Image image) {
        this.image = image;
    }
}
