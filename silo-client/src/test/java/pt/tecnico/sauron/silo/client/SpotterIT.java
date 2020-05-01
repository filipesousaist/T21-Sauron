package pt.tecnico.sauron.silo.client;

import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import pt.tecnico.sauron.silo.client.exception.NoServersException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.math.BigInteger;
import com.google.protobuf.Timestamp;
import java.util.Date;
import java.util.List;

public class SpotterIT extends BaseIT {
    //static members
    static Timestamp ts;
    private static int maxDelay;

    // initialization and clean-up for each test
    @BeforeAll
    public static void beforeAll() {
        maxDelay = Integer.parseInt(testProps.getProperty("server.maxdelay"));
    }

    @Nested
    class TrackAndTrackMatchIT {
        @BeforeEach
        public void setUp() {
            ts = Timestamp.newBuilder().setSeconds((new Date()).getTime() / 1000).setNanos(0).build();
            frontend.ctrlInit(CtrlInitRequest.newBuilder());
            frontend.clear();
        }

        // track

        @Test
        public void trackValidTest(){
            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId("123456")
                    .build();
            TrackRequest.Builder requestBuilder = TrackRequest.newBuilder().setData(data);

            try {
                TrackReply result = frontend.track(requestBuilder);
                ObservationData resultData = result.getData();

                assertEquals(ObjectType.PERSON, resultData.getType());
                assertTrue(Timestamps.between(ts, resultData.getTimestamp()).getSeconds() <= maxDelay);
                assertEquals("123456", resultData.getId());
                assertEquals("Tagus", resultData.getCamName());
            } catch (NoServersException e) {
                System.out.println(e.getMessage());
            }
        }

        @Test
        public void trackIdNotFoundTest(){
            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId("1")
                    .build();

            TrackRequest.Builder requestBuilder = TrackRequest.newBuilder().setData(data);

            assertEquals(Status.Code.NOT_FOUND,
                assertThrows(StatusRuntimeException.class, () -> frontend.track(requestBuilder))
                    .getStatus().getCode());
        }

        @Test
        public void trackInvalidPersonIdsTest() {
            BigInteger LARGEID = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
            BigInteger LARGEID2 = LARGEID.add(BigInteger.valueOf(42153241));
            String[] personIds = {"-1", "0", "-7828426", LARGEID.toString(), LARGEID2.toString(), "1.2",
                "abc", "/", "(Y#(F!H))", ""};

            for (String id : personIds){
                ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId(id)
                    .build();
                TrackRequest.Builder requestBuilder = TrackRequest.newBuilder().setData(data);
                assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.track(requestBuilder))
                        .getStatus().getCode());
            }
        }

        @Test
        public void trackInvalidCarIdsTest() {
            String[] carIds = {"A", "aa11aa", "-12AABB", "CCCCFF", "36CF2", "SS44SS4", "K4BB00", ""};

            for (String id : carIds) {
                ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.CAR)
                    .setId(id)
                    .build();
                TrackRequest.Builder requestBuilder = TrackRequest.newBuilder().setData(data);
                assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.track(requestBuilder))
                        .getStatus().getCode());
            }
        }

        // trackMatch

        @Test
        public void trackMatchValidTest() {
            ObjectData data = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("12*")
                .build();

            TrackMatchRequest.Builder requestBuilder = TrackMatchRequest.newBuilder().setData(data);
            try {
                TrackMatchReply reply = frontend.trackMatch(requestBuilder);

                assertEquals(ObjectType.PERSON, reply.getData(0).getType());
                assertTrue(Timestamps.between(ts, reply.getData(0).getTimestamp()).getSeconds() <= maxDelay);
                assertEquals("123456", reply.getData(0).getId());
                assertEquals("Tagus", reply.getData(0).getCamName());

                assertEquals(ObjectType.PERSON, reply.getData(1).getType());
                assertTrue(Timestamps.between(ts, reply.getData(1).getTimestamp()).getSeconds() <= maxDelay);
                assertEquals("12344321", reply.getData(1).getId());
                assertEquals("Alameda", reply.getData(1).getCamName());
            } catch (NoServersException e) {
                System.out.println(e.getMessage());
            }
        }

        @Test
        public void trackMatchIdNotFoundTest(){
            ObjectData data = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("3*")
                .build();

            TrackMatchRequest.Builder requestBuilder = TrackMatchRequest.newBuilder().setData(data);

            assertEquals(Status.Code.NOT_FOUND,
                assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(requestBuilder))
                    .getStatus().getCode());
        }

        @Test
        public void trackMatchInvalidPersonIdsTest() {
            String[] personIds = {"-1*", "-7828*", "1.2*", "ab*c", "*/", "(Y#(F!H*))", ""};

            for (String id : personIds){
                ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId(id)
                    .build();
                TrackMatchRequest.Builder requestBuilder = TrackMatchRequest.newBuilder().setData(data);
                assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(requestBuilder))
                        .getStatus().getCode());
            }
        }

        @Test
        public void trackMatchInvalidCarIdsTest() {
            String[] carIds = {"a*", "aa*aa", "-12*BB", ""};

            for (String id : carIds) {
                ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.CAR)
                    .setId(id)
                    .build();
                TrackMatchRequest.Builder requestBuilder = TrackMatchRequest.newBuilder().setData(data);
                assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(requestBuilder))
                        .getStatus().getCode());
            }
        }
    }

    @Nested
    class TraceIT {
        private static final int NUMINIT = 4;

        @BeforeEach
        public void setup() {
            ts = Timestamp.newBuilder().setSeconds((new Date()).getTime() / 1000).setNanos(0).build();
            for (int i = 0; i < NUMINIT; i ++)
                frontend.ctrlInit(CtrlInitRequest.newBuilder());
        }

        @Test
        public void validTest() {
            TraceRequest.Builder traceRequestBuilder = TraceRequest.newBuilder().setData(
                ObjectData.newBuilder().setType(ObjectType.PERSON).setId("2568628").build());
            try {
                TraceReply traceReply = frontend.trace(traceRequestBuilder);

                List<ObservationData> dataList = traceReply.getDataList();

                assertEquals(NUMINIT, dataList.size());
                assertTrue(isSortedByDate(dataList));

                for (ObservationData data : dataList) {
                    assertEquals(ObjectType.PERSON, data.getType());
                    assertTrue(Timestamps.between(ts, data.getTimestamp()).getSeconds() <= maxDelay);
                    assertEquals("Tagus", data.getCamName());
                    assertEquals("2568628", data.getId());
                }
            } catch (NoServersException e) {
                System.out.println(e.getMessage());
            }
        }

        @Test
        public void nonExistingIdTest() {
            TraceRequest.Builder traceRequestBuilder = TraceRequest.newBuilder().setData(
                ObjectData.newBuilder().setType(ObjectType.CAR).setId("XF4570").build());

            assertEquals(Status.Code.NOT_FOUND,
                assertThrows(StatusRuntimeException.class, () -> frontend.trace(traceRequestBuilder))
                    .getStatus().getCode());
        }

        @Test
        public void invalidPersonIdTest() {
            final BigInteger LARGEID = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
            final BigInteger LARGEID2 = LARGEID.add(BigInteger.valueOf(902084218));
            String[] personIds = {"-1", "0", "-6753825", LARGEID.toString(), LARGEID2.toString(), "0.3",
                "hfF2", ")", "%$&#(HF7Fg8FG8", ""};

            for (String id: personIds) {
                TraceRequest.Builder traceRequestBuilder = TraceRequest.newBuilder().setData(
                    ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id).build());

                assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trace(traceRequestBuilder))
                        .getStatus().getCode());
            }
        }

        @Test
        public void invalidCarIdTest() {
            String[] carIds = {"X", "23jn45", "-3232BG", "123456", "36CF2", "55GG66V", "12G3BK", "L*", ""};

            for (String id: carIds) {
                TraceRequest.Builder traceRequestBuilder = TraceRequest.newBuilder().setData(
                    ObjectData.newBuilder().setType(ObjectType.CAR).setId(id).build());
                assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trace(traceRequestBuilder))
                        .getStatus().getCode());
            }
        }

        private boolean isSortedByDate(List<ObservationData> list) {
            for (int i = 0; i < list.size() - 1; i ++)
                if (Timestamps.between(list.get(i + 1).getTimestamp(),
                        list.get(i).getTimestamp()).getSeconds() < 0)
                    return false;
            return true;
        }
    }

    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.newBuilder());
    }
}

