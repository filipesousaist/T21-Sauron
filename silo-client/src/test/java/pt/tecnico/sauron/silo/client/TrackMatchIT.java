package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.math.BigInteger;
import java.util.Date;

public class TrackMatchIT extends BaseIT {

    static Timestamp ts;
    private static int maxDelay;


    @BeforeAll
    public static void beforeAll() {
        maxDelay = Integer.parseInt(testProps.getProperty("server.maxdelay"));
    }

    @BeforeEach
    public void setUp() {
        ts = Timestamp.newBuilder().setSeconds((new Date()).getTime() / 1000).setNanos(0).build();
        frontend.ctrlInit(CtrlInitRequest.getDefaultInstance());
    }

    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.getDefaultInstance());
    }

    @Test
    public void validTest(){
        ObjectData data = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("12*")
                .build();

        TrackMatchRequest request = TrackMatchRequest.newBuilder().setData(data).build();
        TrackMatchReply reply = frontend.trackMatch(request);

        assertEquals(ObjectType.PERSON, reply.getData(0).getType());
        assertTrue(Timestamps.between(ts, reply.getData(0).getTimestamp()).getSeconds() <= maxDelay);
        assertEquals("123456", reply.getData(0).getId());
        assertEquals("Tagus", reply.getData(0).getCamName());

        assertEquals(ObjectType.PERSON, reply.getData(1).getType());
        assertTrue(Timestamps.between(ts, reply.getData(1).getTimestamp()).getSeconds() <= maxDelay);
        assertEquals("12344321", reply.getData(1).getId());
        assertEquals("Alameda", reply.getData(1).getCamName());


    }

    @Test
    public void idNotFoundTest(){
        ObjectData data = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("3*")
                .build();

        TrackMatchRequest request = TrackMatchRequest.newBuilder().setData(data).build();

        assertEquals(Status.Code.NOT_FOUND,
                assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(request))
                        .getStatus().getCode());
    }

    @Test
    public void invalidPersonIdsTest() {
        String[] personIds = {"-1*", "-7828*", "1.2*", "ab*c", "*/", "(Y#(F!H*))", ""/*, null*/};

        for (String id : personIds){
            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId(id)
                    .build();
            TrackMatchRequest request = TrackMatchRequest.newBuilder().setData(data).build();
            assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(request))
                            .getStatus().getCode());
        }
    }

    @Test
    public void invalidCarIdsTest() {
        String[] carIds = {"a*", "aa*aa", "-12*BB", ""/*, null*/};

        for (String id : carIds) {
            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.CAR)
                    .setId(id)
                    .build();
            TrackMatchRequest request = TrackMatchRequest.newBuilder().setData(data).build();
            assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(request))
                            .getStatus().getCode());
        }
    }

}
