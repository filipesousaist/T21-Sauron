package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import com.google.protobuf.util.Timestamps;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TraceIT extends BaseIT {
    private static final int NUMINIT = 4;
    private Timestamp ts;
    private static int maxDelay;

    @BeforeAll
    public static void beforeAll() {
        maxDelay = Integer.parseInt(testProps.getProperty("server.maxdelay"));
    }

    @BeforeEach
    public void setup() {
        ts = Timestamp.newBuilder().setSeconds((new Date()).getTime() / 1000).setNanos(0).build();
        for (int i = 0; i < NUMINIT; i ++)
            frontend.ctrlInit(CtrlInitRequest.getDefaultInstance());
    }

    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.getDefaultInstance());
    }

    @Test
    public void validTest() {
        TraceRequest traceRequest = TraceRequest.newBuilder().setData(
            ObjectData.newBuilder().setType(ObjectType.PERSON).setId("2568628").build()).build();
        TraceReply traceReply = frontend.trace(traceRequest);

        List<ObservationData> dataList = traceReply.getDataList();

        assertEquals(dataList.size(), NUMINIT);
        assertTrue(isSortedByDate(dataList));

        for (ObservationData data: dataList) {
            assertEquals(data.getType(), ObjectType.PERSON);
            assertTrue(Timestamps.between(ts, data.getTimestamp()).getSeconds() <= maxDelay);
            assertEquals(data.getCamName(), "Tagus");
            assertEquals(data.getId(), "2568628");
        }
    }

    @Test
    public void nonExistingIdTest() {
        TraceRequest traceRequest = TraceRequest.newBuilder().setData(
                ObjectData.newBuilder().setType(ObjectType.CAR).setId("XF4570").build()).build();

        assertEquals(Status.Code.NOT_FOUND,
                assertThrows(StatusRuntimeException.class, () -> frontend.trace(traceRequest))
                        .getStatus().getCode());
    }

    @Test
    public void invalidPersonIdTest() {
        final BigInteger LARGEID = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        final BigInteger LARGEID2 = LARGEID.add(BigInteger.valueOf(902084218));
        String[] personIds = {"-1", "-6753825", LARGEID.toString(), LARGEID2.toString(), "0.3",
                "hfF2", ")", "%$&#(HF7Fg8FG8", ""/*, null*/};

        for (String id: personIds) {
            TraceRequest traceRequest = TraceRequest.newBuilder().setData(
                ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id).build()).build();

            assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trace(traceRequest))
                            .getStatus().getCode());
        }
    }

    @Test
    public void invalidCarIdTest() {
        String[] carIds = {"X", "23jn45", "-3232BG", "123456", "36CF2", "55GG66V", "12G3BK", "L*", ""/*, null*/};

        for (String id: carIds) {
            TraceRequest traceRequest = TraceRequest.newBuilder().setData(
                    ObjectData.newBuilder().setType(ObjectType.CAR).setId(id).build()).build();
            assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trace(traceRequest))
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
