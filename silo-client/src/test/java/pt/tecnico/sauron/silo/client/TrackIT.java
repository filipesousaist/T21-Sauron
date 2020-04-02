package pt.tecnico.sauron.silo.client;

import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.math.BigInteger;
import com.google.protobuf.Timestamp;
import java.util.Date;

public class TrackIT extends BaseIT {
    //static members

    static Timestamp ts;
    private static int maxDelay;


    // initialization and clean-up for each test
    @BeforeAll
    public static void beforeAll() {
        maxDelay = Integer.parseInt(testProps.getProperty("server.maxdelay"));
    }

    @BeforeEach
    public void setUp() {
        ts = Timestamp.newBuilder().setSeconds((new Date()).getTime() / 1000).setNanos(0).build();
        frontend.ctrlInit(CtrlInitRequest.newBuilder().getDefaultInstanceForType());
    }

    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.newBuilder().getDefaultInstanceForType());
    }

    @Test
    public void validTest(){
        ObjectData data = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("123456")
                .build();
        TrackRequest request = TrackRequest.newBuilder().setData(data).build();

        TrackReply result = frontend.track(request);
        ObservationData resultData = result.getData();

        assertEquals(ObjectType.PERSON, resultData.getType());
        assertTrue(Timestamps.between(ts, resultData.getTimestamp()).getSeconds() <= maxDelay);
        assertEquals("123456", resultData.getId());
        assertEquals("Tagus", resultData.getCamName());
    }

    @Test
    public void idNotFoundTest(){
        ObjectData data = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("1")
                .build();

        TrackRequest request = TrackRequest.newBuilder().setData(data).build();

        assertEquals(Status.Code.NOT_FOUND,
                assertThrows(StatusRuntimeException.class, () -> frontend.track(request))
                        .getStatus().getCode());
    }

    @Test
    public void invalidPersonIdsTest() {
        BigInteger LARGEID = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        BigInteger LARGEID2 = LARGEID.add(BigInteger.valueOf(42153241));
        String[] personIds = {"-1", "-7828426", LARGEID.toString(), LARGEID2.toString(), "1.2",
                "abc", "/", "(Y#(F!H))", ""/*, null*/};

        for (String id : personIds){
            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId(id)
                    .build();
            TrackRequest request = TrackRequest.newBuilder().setData(data).build();
            assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.track(request))
                            .getStatus().getCode());
        }
    }

    @Test
    public void invalidCarIdsTest() {
        String[] carIds = {"A", "aa11aa", "-12AABB", "CCCCFF", "36CF2", "SS44SS4", "K4BB00", ""/*, null*/};

        for (String id : carIds) {
            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.CAR)
                    .setId(id)
                    .build();
            TrackRequest request = TrackRequest.newBuilder().setData(data).build();
            assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.track(request))
                            .getStatus().getCode());
        }
    }
}

