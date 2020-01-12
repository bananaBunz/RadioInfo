import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserTest {

    Parser parser;
    String URL = "http://api.sr.se/api/v2/channels";

    @BeforeEach
    public void setUp(){

        parser = new Parser();

    }

    @AfterEach
    public void tearDown(){
        parser = null;
    }

    /**
     * Test to read the channels.
     */
    @Test
    public void testReadChannels(){
        Call call = new Call();
        int size = parser.readChannels(call.getChannels(URL)).size();
        assertEquals(10, size);
    }

    /**
     * Test to get the channels tableau.
     */
    @Test
    public void testGetChannelTab(){

        Call call = new Call();

        ArrayList<Channel> list = parser.readChannels(call.getChannels(URL));
        assertEquals(10, list.size());

        try {
            ArrayList programs = parser.readChannelTab(call, call.getTableau(list.get(0).getId()));
            //antalet program kan kommas att variera från dag till dag, detta testet kan därför kommas
            //bli ogiltigt.
            assertEquals(49, programs.size());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Test to validate if the stored data is parsed correctly.
     */
    @Test
    public void testReadLocal(){
        ArrayList<Channel> channels = parser.readLocal("channels.xml");
        assertEquals(10, channels.size());
        for(Channel ch : channels){
            assertTrue(ch.getPrograms().size() > 0);
        }
    }
}
