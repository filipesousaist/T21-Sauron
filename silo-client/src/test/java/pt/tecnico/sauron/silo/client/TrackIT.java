package pt.tecnico.sauron.silo.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TrackIT extends BaseIT {
    //static members
    static final String SERVER_STATUS = "Server has been cleared";
    static Date date;
    static final long DATE_TOLERANCE = 1;

    // initialization and clean-up for each test

    @BeforeEach
    public void setUp() {
        frontend.ctrlInit(CtrlInitRequest.newBuilder().getDefaultInstanceForType());
        date = new Date();
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
        assertTrue(resultData.getTimestamp().getSeconds() - date.getTime() < DATE_TOLERANCE);
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

