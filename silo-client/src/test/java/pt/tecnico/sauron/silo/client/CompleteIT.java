package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.client.exception.NoServersException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        CtrlClearRequest.Builder ctrlClearRequestBuilder = CtrlClearRequest.newBuilder();
        frontend.ctrlClear(ctrlClearRequestBuilder);
    }

    @Test
    public void trackValidTest() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);

            ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Cam1");

            reportRequestBuilder.addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("123456"));

            frontend.report(reportRequestBuilder);

            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId("123456")
                    .build();
            TrackRequest.Builder requestBuilder = TrackRequest.newBuilder().setData(data);

            TrackReply reply = frontend.track(requestBuilder);
            ObservationData replyData = reply.getData();

            CamInfoReply camInfoReply = frontend.camInfo(CamInfoRequest.newBuilder()
                    .setCamName(replyData.getCamName()));

            assertEquals(ObjectType.PERSON, replyData.getType());
            assertTrue(Timestamps.between(ts, replyData.getTimestamp()).getSeconds() <= maxDelay);
            assertEquals("123456", replyData.getId());
            assertEquals("Cam1", replyData.getCamName());

            assertEquals(89.2315, camInfoReply.getCoordinates().getLatitude());
            assertEquals(55.669, camInfoReply.getCoordinates().getLongitude());

        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void trackMatchTestOk() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);

            String[] personIds = {"123456", "1234321", "12456"};

            ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Cam1");

            for (String id : personIds)
                reportRequestBuilder.addData(
                        ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id));

            frontend.report(reportRequestBuilder);

            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId("12*")
                    .build();

            TrackMatchRequest.Builder requestBuilder = TrackMatchRequest.newBuilder().setData(data);
            TrackMatchReply reply = frontend.trackMatch(requestBuilder);

            CamInfoReply camInfoReply = frontend.camInfo(CamInfoRequest.newBuilder()
                    .setCamName(reply.getData(0).getCamName()));

            assertEquals(ObjectType.PERSON, reply.getData(0).getType());
            assertTrue(Timestamps.between(ts, reply.getData(0).getTimestamp()).getSeconds() <= maxDelay);
            assertEquals("123456", reply.getData(0).getId());
            assertEquals("Cam1", reply.getData(0).getCamName());

            assertEquals(ObjectType.PERSON, reply.getData(1).getType());
            assertTrue(Timestamps.between(ts, reply.getData(1).getTimestamp()).getSeconds() <= maxDelay);
            assertEquals("1234321", reply.getData(1).getId());
            assertEquals("Cam1", reply.getData(1).getCamName());

            assertEquals(ObjectType.PERSON, reply.getData(2).getType());
            assertTrue(Timestamps.between(ts, reply.getData(2).getTimestamp()).getSeconds() <= maxDelay);
            assertEquals("12456", reply.getData(2).getId());
            assertEquals("Cam1", reply.getData(2).getCamName());

            assertEquals(89.2315, camInfoReply.getCoordinates().getLatitude());
            assertEquals(55.669, camInfoReply.getCoordinates().getLongitude());

        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }


    @Test
    public void traceTestOk() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);

        try {
            frontend.camJoin(camJoinRequestBuilder);

            String[] personIds = {"123456", "123456", "12456"};

            ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Cam1");

            for (String id : personIds)
                reportRequestBuilder.addData(
                        ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id));

            frontend.report(reportRequestBuilder);

            TraceRequest.Builder traceRequestBuilder = TraceRequest.newBuilder().setData(
                    ObjectData.newBuilder().setType(ObjectType.PERSON).setId("123456").build());
            TraceReply traceReply = frontend.trace(traceRequestBuilder);

            CamInfoReply camInfoReply = frontend.camInfo(CamInfoRequest.newBuilder()
                    .setCamName(traceReply.getData(0).getCamName()));

            List<ObservationData> dataList = traceReply.getDataList();

            assertEquals(2, dataList.size());
            assertTrue(isSortedByDate(dataList));

            for (ObservationData data : dataList) {
                assertEquals(ObjectType.PERSON, data.getType());
                assertTrue(Timestamps.between(ts, data.getTimestamp()).getSeconds() <= maxDelay);
                assertEquals("Cam1", data.getCamName());
                assertEquals("123456", data.getId());

            }

            assertEquals(89.2315, camInfoReply.getCoordinates().getLatitude());
            assertEquals(55.669, camInfoReply.getCoordinates().getLongitude());
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
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
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);

        try {
            frontend.camJoin(camJoinRequestBuilder);

            ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Cam1")
                    .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("A").build());
            assertEquals(Status.Code.INVALID_ARGUMENT,
                    assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequestBuilder))
                            .getStatus().getCode());

            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.CAR)
                    .setId("A*")
                    .build();

            TrackMatchRequest.Builder requestBuilder = TrackMatchRequest.newBuilder().setData(data);
            assertEquals(Status.Code.NOT_FOUND,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(requestBuilder))
                            .getStatus().getCode());

        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void invalidTrackTest() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);

        try {
            frontend.camJoin(camJoinRequestBuilder);

            ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Cam1");

            reportRequestBuilder.addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("123456"));

            frontend.report(reportRequestBuilder);

            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId("123556")
                    .build();
            TrackRequest.Builder requestBuilder = TrackRequest.newBuilder().setData(data);

            assertEquals(Status.Code.NOT_FOUND,
                    assertThrows(StatusRuntimeException.class, () -> frontend.track(requestBuilder))
                            .getStatus().getCode());

        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void invalidTrackMatchTest() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);

        try {
            frontend.camJoin(camJoinRequestBuilder);

            String[] personIds = {"123456", "1234321", "12456"};

            ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Cam1");

            for (String id : personIds)
                reportRequestBuilder.addData(
                        ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id));

            frontend.report(reportRequestBuilder);

            ObjectData data = ObjectData.newBuilder()
                    .setType(ObjectType.PERSON)
                    .setId("12*4")
                    .build();

            TrackMatchRequest.Builder requestBuilder = TrackMatchRequest.newBuilder().setData(data);

            assertEquals(Status.Code.NOT_FOUND,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trackMatch(requestBuilder))
                            .getStatus().getCode());
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void invalidTraceTest() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);

        try {
            frontend.camJoin(camJoinRequestBuilder);

            String[] personIds = {"123456", "123456", "12456"};

            ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Cam1");

            for (String id : personIds)
                reportRequestBuilder.addData(
                        ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id));

            frontend.report(reportRequestBuilder);

            TraceRequest.Builder traceRequestBuilder = TraceRequest.newBuilder().setData(
                    ObjectData.newBuilder().setType(ObjectType.PERSON).setId("123556").build());

            assertEquals(Status.Code.NOT_FOUND,
                    assertThrows(StatusRuntimeException.class, () -> frontend.trace(traceRequestBuilder))
                            .getStatus().getCode());

        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }
}