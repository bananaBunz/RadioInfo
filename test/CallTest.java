import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CallTest {

    private Call call;
    private String url = "http://api.sr.se/api/v2/channels";

    @BeforeEach
    public void setUp(){
        call = new Call();
    }
    @AfterEach
    public void tearDown(){
        call = null;
    }
    @Test
    public void testGetChannels(){
        assertNotNull(call.getChannels(url));
    }
    @Test
    public void testGetChannelTableau(){
        Parser parser = new Parser();

        ArrayList<Channel> list = parser.readChannels(call, call.getChannels(url));
        assertEquals(10, list.size());

        for(Channel ch : list){
            try {
                assertNotNull(call.getTableau(ch.getId()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
