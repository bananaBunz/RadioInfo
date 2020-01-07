import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CallTest {

    private Call call;

    @BeforeEach
    public void setUp(){
        call = new Call();
    }

    public void tearDown(){

    }

}
