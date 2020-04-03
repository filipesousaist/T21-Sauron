package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompleteIT extends BaseIT {
    static Timestamp ts;
    private static int maxDelay;

    @BeforeAll
    public static void beforeAll() {
        maxDelay = Integer.parseInt(testProps.getProperty("server.maxdelay"));
    }

    @BeforeEach
    public void setUp() {
        ts = Timestamp.newBuilder().setSeconds((new Date()).getTime() / 1000).setNanos(0).build();
    }

    @AfterEach
    public void tearDown() {
        CtrlClearRequest ctrlClearRequest = CtrlClearRequest.newBuilder().getDefaultInstanceForType();
        frontend.ctrlClear(ctrlClearRequest);
    }

    @Test
    public void trackValidTest() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(camJoinRequest);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Cam1");

        reportRequestBuilder.addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("123456"));

        frontend.report(reportRequestBuilder.build());

        ObjectData data = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("123456")
                .build();
        TrackRequest request = TrackRequest.newBuilder().setData(data).build();

        TrackReply reply = frontend.track(request);
        ObservationData replyData = reply.getData();

        CamInfoReply camInfoReply = frontend.camInfo(CamInfoRequest.newBuilder()
                .setCamName(replyData.getCamName()).build());

        assertEquals(ObjectType.PERSON, replyData.getType());
        assertTrue(Timestamps.between(ts, replyData.getTimestamp()).getSeconds() <= maxDelay);
        assertEquals("123456", replyData.getId());
        assertEquals("Cam1", replyData.getCamName());

        assertEquals(89.2315, camInfoReply.getCoordinates().getLatitude());
        assertEquals(55.669, camInfoReply.getCoordinates().getLongitude());

    }

    @Test
    public void trackMatchTestOk() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(camJoinRequest);

        String[] personIds = {"123456", "1234321", "12456"};

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Tagus");

        for (String id : personIds)
            reportRequestBuilder.addData(
                    ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id));

        frontend.report(reportRequestBuilder.build());

        ObjectData data = ObjectData.newBuilder()
                .setType(ObjectType.PERSON)
                .setId("12*")
                .build();

        TrackMatchRequest request = TrackMatchRequest.newBuilder().setData(data).build();
        TrackMatchReply reply = frontend.trackMatch(request);

        CamInfoReply camInfoReply = frontend.camInfo(CamInfoRequest.newBuilder()
                .setCamName(reply.getData(0).getCamName()).build());

        assertEquals(ObjectType.PERSON, reply.getData(0).getType());
        assertTrue(Timestamps.between(ts, reply.getData(0).getTimestamp()).getSeconds() <= maxDelay);
        assertEquals("123456", reply.getData(0).getId());
        assertEquals("Cam1", reply.getData(0).getCamName());

        assertEquals(ObjectType.PERSON, reply.getData(1).getType());
        assertTrue(Timestamps.between(ts, reply.getData(1).getTimestamp()).getSeconds() <= maxDelay);
        assertEquals("12344321", reply.getData(1).getId());
        assertEquals("Cam1", reply.getData(1).getCamName());

        assertEquals(ObjectType.PERSON, reply.getData(2).getType());
        assertTrue(Timestamps.between(ts, reply.getData(2).getTimestamp()).getSeconds() <= maxDelay);
        assertEquals("12456", reply.getData(2).getId());
        assertEquals("Cam1", reply.getData(2).getCamName());

        assertEquals(89.2315, camInfoReply.getCoordinates().getLatitude());
        assertEquals(55.669, camInfoReply.getCoordinates().getLongitude());

    }


    @Test
    public void traceTestOk() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(camJoinRequest);

        String[] personIds = {"123456", "123456", "12456"};

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Tagus");

        for (String id : personIds)
            reportRequestBuilder.addData(
                    ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id));

        frontend.report(reportRequestBuilder.build());

        TraceRequest traceRequest = TraceRequest.newBuilder().setData(
                ObjectData.newBuilder().setType(ObjectType.PERSON).setId("2568628").build()).build();
        TraceReply traceReply = frontend.trace(traceRequest);

        List<ObservationData> dataList = traceReply.getDataList();

        assertEquals(2, dataList.size());
        assertTrue(isSortedByDate(dataList));

        for (ObservationData data : dataList) {
            assertEquals(ObjectType.PERSON, data.getType());
            assertTrue(Timestamps.between(ts, data.getTimestamp()).getSeconds() <= maxDelay);
            assertEquals("Cam1", data.getCamName());
            assertEquals("123456", data.getId());

        }
    }

    private boolean isSortedByDate(List<ObservationData> list) {
        for (int i = 0; i < list.size() - 1; i++)
            if (Timestamps.between(list.get(i + 1).getTimestamp(),
                    list.get(i).getTimestamp()).getSeconds() < 0)
                return false;
        return true;
    }

    @Test
    public void invalidReportTest() {

    }
}