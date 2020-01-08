import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParserTest {

    Parser parser;

    @BeforeEach
    public void setUp(){

        parser = new Parser();

    }

    @AfterEach
    public void tearDown(){
        parser = null;
    }

    @Test
    public void testReadChannels(){

        Call call = new Call();
        int size = parser.readChannels(call.getChannels()).size();
        assertEquals(10, size);
    }

    @Test
    public void testGetChannelTab(){

        Call call = new Call();

        ArrayList<Channel> list = parser.readChannels(call.getChannels());
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
}
