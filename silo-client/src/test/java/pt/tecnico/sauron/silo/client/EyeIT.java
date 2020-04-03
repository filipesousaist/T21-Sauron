package pt.tecnico.sauron.silo.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EyeIT extends BaseIT {

    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.getDefaultInstance());
    }

    @Test
    public void testOkJoin() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(camJoinRequest);
    }

    @Test
    public void invalidCoordinates1(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(-90.000001).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();


        assertEquals(Status.OUT_OF_RANGE.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }

    @Test
    public void invalidCoordinates2(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(-180.000001).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();


        assertEquals(Status.OUT_OF_RANGE.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }


    @Test
    public void invalidCoordinates3(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(90.000001).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();


        assertEquals(Status.OUT_OF_RANGE.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }

    @Test
    public void invalidCoordinates4(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(180.000001).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();


        assertEquals(Status.OUT_OF_RANGE.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }

    @Test
    public void validCoordinates1(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(-89.999999).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(camJoinRequest);
    }

    @Test
    public void validCoordinates2(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(-179.999999).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(camJoinRequest);

    }


    @Test
    public void validCoordinates3(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.999999).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(camJoinRequest);

    }

    @Test
    public void validCoordinates4(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(179.999999).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(camJoinRequest);
    }

    @Test
    public void sameNameDifferentCoords() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(5).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(camJoinRequest);


        coordinates = Coordinates.newBuilder().setLatitude(45).setLongitude(20).build();
        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        assertEquals(Status.PERMISSION_DENIED.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest2)).getStatus().getCode());
    }

    @Test
    public void sameCoordsDifferentName() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(camJoinRequest);


        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder()
                .setCamName("Cam2")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(camJoinRequest2);
    }

    @Test
    public void sameCoordsSameName() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(camJoinRequest);


        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        assertEquals(Status.ALREADY_EXISTS.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest2)).getStatus().getCode());
    }


    @Test
    public void emptyNameJoin(){
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setCamName("").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }

    @Test
    public void nameTooShort(){
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setCamName("Ta").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }

    @Test
    public void nameTooLong(){
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setCamName("Tacsdcsdsdcscsscscsdcsdcsdcsd").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }

    @Test
    public void invalidCharsInName(){
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setCamName("Tagus[{").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }


    @Test
    public void testOkInfo() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(camJoinRequest);

        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Tagus").build();
        CamInfoReply camInfoReply = frontend.camInfo(camInfoRequest);

        assertEquals(38.737613,
                camInfoReply.getCoordinates().getLatitude());

        assertEquals(-9.303164,
                camInfoReply.getCoordinates().getLongitude());
    }

    @Test
    public void nameTooShortInfo(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(camJoinRequest);

        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Ta").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());
    }

    @Test
    public void nameTooLongInfo(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(camJoinRequest);

        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Tacsdcsdsdcscsscscsdcsdcsdcsd").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());
    }

    @Test
    public void emptyNameInfo(){
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setCamName("").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }

    @Test
    public void invalidCharsInNameInfo(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(camJoinRequest);

        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Tagus55())(").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());
    }

    @Test
    public void camDoesNotExistInfo(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(camJoinRequest);


        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Taguscssa").build();
        assertEquals(Status.NOT_FOUND.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());

    }

    // Auxiliary method
    private void setupCam() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
            .setCamName("Tagus")
            .setCoordinates(coordinates)
            .build();
        frontend.camJoin(camJoinRequest);
    }

    @Test
    public void reportValidTest() {
        setupCam();

        String[] personIds = {"1", "36517", Long.toString(Long.MAX_VALUE)};
        String[] carIds = {"ABCD12", "EF34GH", "56IJKL", "0000ZZ", "67LC95", "XY3774"};

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Tagus");

        for (String id: personIds)
            reportRequestBuilder.addData(
                    ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id));
        for (String id: carIds)
            reportRequestBuilder.addData(
                    ObjectData.newBuilder().setType(ObjectType.CAR).setId(id));

        frontend.report(reportRequestBuilder.build());
    }

    @Test
    public void reportUnregisteredCamNameTest() {
        setupCam();

        ReportRequest.Builder reportRequestBuilder =
                ReportRequest.newBuilder().setCamName("Trudy");

        reportRequestBuilder.addData(
                ObjectData.newBuilder().setType(ObjectType.PERSON).setId("3").build());

        assertEquals(Status.Code.UNAUTHENTICATED,
                assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequestBuilder.build()))
                        .getStatus().getCode());
    }

    @Test
    public void reportInvalidPersonIdsTest() {
        final BigInteger LARGEID = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        final BigInteger LARGEID2 = LARGEID.add(BigInteger.valueOf(42153241));
        String[] personIds = {"0", "-1", "-7828426", LARGEID.toString(), LARGEID2.toString(), "1.2",
                "abc", "/", "(Y#(F!H))", ""};

        setupCam();

        for (String id: personIds) {
            ReportRequest reportRequest = ReportRequest.newBuilder().setCamName("Tagus")
                .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id).build()).build();
            assertEquals(Status.Code.INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequest))
                    .getStatus().getCode());
        }
    }

    @Test
    public void reportInvalidCarIdsTest() {
        String[] carIds = {"A", "aa11aa", "-12AABB", "CCCCFF", "36CF2", "SS44SS4", "K4BB00", ""};

        setupCam();

        for (String id: carIds) {
            ReportRequest reportRequest = ReportRequest.newBuilder().setCamName("Tagus")
                .addData(ObjectData.newBuilder().setType(ObjectType.CAR).setId(id).build()).build();
            assertEquals(Status.Code.INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequest))
                    .getStatus().getCode());
        }
    }

    @Test
    public void reportMultipleInvalidIdsTest() {
        setupCam();

        ReportRequest reportRequest = ReportRequest.newBuilder().setCamName("Tagus")
            .addData(ObjectData.newBuilder().setType(ObjectType.CAR).setId("ABC123").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("-2").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.CAR).setId("xh2245").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("0.1").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("").build())
            .build();
        assertEquals(Status.Code.INVALID_ARGUMENT,
            assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequest))
                .getStatus().getCode());
    }

    @Test
    public void reportValidAndInvalidIdsTest() {
        setupCam();

        ReportRequest reportRequest = ReportRequest.newBuilder().setCamName("Tagus")
            .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("3").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.CAR).setId("ABC123").build())
            .build();
        assertEquals(Status.Code.INVALID_ARGUMENT,
            assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequest))
                .getStatus().getCode());
    }
}
