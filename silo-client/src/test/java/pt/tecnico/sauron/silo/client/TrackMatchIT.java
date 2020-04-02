package pt.tecnico.sauron.silo.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.math.BigInteger;
import java.util.Date;


public class TrackMatchIT extends BaseIT {

    static Date date;
    static final long DATE_TOLERANCE = 1;


    // initialization and clean-up for each test

    @BeforeEach
    public void setUp() {
        frontend.ctrlInit(CtrlInitRequest.getDefaultInstance());
        date = new Date();
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

        assertEquals(ObjectType.PERSON, reply.getData(1).getType());
        assertTrue(reply.getData(1).getTimestamp().getSeconds() - date.getTime() < DATE_TOLERANCE);
        assertEquals("123456", reply.getData(1).getId());
        assertEquals("Tagus", reply.getData(1).getCamName());

        assertEquals(ObjectType.PERSON, reply.getData(1).getType());
        assertTrue(reply.getData(1).getTimestamp().getSeconds() - date.getTime() < DATE_TOLERANCE);
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
        BigInteger LARGEID = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        BigInteger LARGEID2 = LARGEID.add(BigInteger.valueOf(42153241));
        String[] personIds = {"-1*", "-7828*", LARGEID.toString() + "*", LARGEID2.toString() + "*", "1.2*",
                "ab*c", "*/", "(Y#(F!H*))", ""/*, null*/};

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
        String[] carIds = {"a*", "aa*aa", "-12*BB", "SS44S*S4", ""/*, null*/};

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
