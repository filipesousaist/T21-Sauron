package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TrackIT extends BaseIT {
    //static members
    static final String SERVER_STATUS = "Server has been cleared";
    static Date date;
    static final long DATE_TOLERANCE = 1;

    @BeforeAll
    public static void oneTimeSetUp() {

    }

    @AfterAll
    public static void oneTimeTearDown() {

    }

    // initialization and clean-up for each test

    @BeforeEach
    public void setUp() {
        frontend.ctrlInit(EmptyMessage.newBuilder().getDefaultInstanceForType());
        date = new Date();
    }

    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(EmptyMessage.newBuilder().getDefaultInstanceForType());
    }

    @Test
    public void testOk(){
        ObjectData request = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("123456")
                .build();

        ObservationResponse result = frontend.track(request);
        ObservationData resultData = result.getData(1);

        assertEquals(ObjectType.PERSON, resultData.getType());
        assertTrue(resultData.getTimestamp().getSeconds() - date.getTime() < DATE_TOLERANCE);
        assertEquals("123456", resultData.getId());
        assertEquals("Tagus", resultData.getCamName());
    }

    @Test
    public void testOkIdNotFound(){
        ObjectData request = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("1")
                .build();

        ObservationResponse result = frontend.track(request);
        assertEquals(0, result.getDataCount());
    }

    @Test
    public void invalidType(){

    }
}