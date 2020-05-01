package pt.tecnico.sauron.silo.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.client.exception.NoServersException;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EyeIT extends BaseIT {

    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.newBuilder());
        frontend.clear();
    }

    @Test
    public void testOkJoin() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void invalidCoordinates(){
        List<Double[]> doubles = new LinkedList<>();
        doubles.add(new Double[]{-90.000001, 0.0});
        doubles.add(new Double[]{0.0, -180.000001});
        doubles.add(new Double[]{90.000001, 0.0});
        doubles.add(new Double[]{0.0, 180.000001});

        for(Double[] d : doubles) {
            Coordinates coordinates = Coordinates.newBuilder().setLatitude(d[0]).setLongitude(d[1]).build();
            CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                    .setCamName("Cam1")
                    .setCoordinates(coordinates);


            assertEquals(Status.OUT_OF_RANGE.getCode(),
                    assertThrows(StatusRuntimeException.class,
                            () -> frontend.camJoin(camJoinRequestBuilder)).getStatus().getCode());
        }
    }

    @Test
    public void validCoordinates(){
        List<Double[]> doubles = new LinkedList<>();
        doubles.add(new Double[]{-89.999999, 0.0});
        doubles.add(new Double[]{0.0, -179.999999});
        doubles.add(new Double[]{89.999999, 0.0});
        doubles.add(new Double[]{0.0, 179.999999});


            for (Double[] d : doubles) {
                try {
                    frontend.ctrlClear(CtrlClearRequest.newBuilder());
                    Coordinates coordinates = Coordinates.newBuilder().setLatitude(d[0]).setLongitude(d[1]).build();
                    CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                            .setCamName("Cam1")
                            .setCoordinates(coordinates);

                    frontend.camJoin(camJoinRequestBuilder);
                } catch (NoServersException e) {
                    System.out.println(e.getMessage());
                }
            }

    }


    @Test
    public void sameNameDifferentCoords() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(5).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);


            coordinates = Coordinates.newBuilder().setLatitude(45).setLongitude(20).build();
            CamJoinRequest.Builder camJoinRequestBuilder2 = CamJoinRequest.newBuilder()
                    .setCamName("Cam1")
                    .setCoordinates(coordinates);

            assertEquals(Status.PERMISSION_DENIED.getCode(),
                    assertThrows(StatusRuntimeException.class,
                            () -> frontend.camJoin(camJoinRequestBuilder2)).getStatus().getCode());
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void sameCoordsDifferentName() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);


            CamJoinRequest.Builder camJoinRequestBuilder2 = CamJoinRequest.newBuilder()
                    .setCamName("Cam2")
                    .setCoordinates(coordinates);

            frontend.camJoin(camJoinRequestBuilder2);
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void sameCoordsSameName() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);


            CamJoinRequest.Builder camJoinRequestBuilder2 = CamJoinRequest.newBuilder()
                    .setCamName("Cam1")
                    .setCoordinates(coordinates);

            assertEquals(Status.ALREADY_EXISTS.getCode(),
                    assertThrows(StatusRuntimeException.class,
                            () -> frontend.camJoin(camJoinRequestBuilder2)).getStatus().getCode());
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }


    @Test
    public void emptyNameJoin(){
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder().setCamName("");
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequestBuilder)).getStatus().getCode());
    }

    @Test
    public void nameTooShort(){
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder().setCamName("Ta");
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequestBuilder)).getStatus().getCode());
    }

    @Test
    public void nameTooLong(){
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder().setCamName("Tacsdcsdsdcscsscscsdcsdcsdcsd");
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequestBuilder)).getStatus().getCode());
    }

    @Test
    public void invalidCharsInName(){
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder().setCamName("Tagus[{");
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequestBuilder)).getStatus().getCode());
    }


    @Test
    public void testOkInfo() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);

            CamInfoRequest.Builder camInfoRequestBuilder = CamInfoRequest.newBuilder().setCamName("Tagus");
            CamInfoReply camInfoReply = frontend.camInfo(camInfoRequestBuilder);

            assertEquals(38.737613,
                    camInfoReply.getCoordinates().getLatitude());

            assertEquals(-9.303164,
                    camInfoReply.getCoordinates().getLongitude());
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void nameTooShortInfo(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);

            CamInfoRequest.Builder camInfoRequestBuilder = CamInfoRequest.newBuilder().setCamName("Ta");
            assertEquals(Status.INVALID_ARGUMENT.getCode(),
                    assertThrows(StatusRuntimeException.class,
                            () -> frontend.camInfo(camInfoRequestBuilder)).getStatus().getCode());
        } catch (NoServersException e) {
                System.out.println(e.getMessage());
        }
    }

    @Test
    public void nameTooLongInfo(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);

            CamInfoRequest.Builder camInfoRequestBuilder = CamInfoRequest.newBuilder().setCamName("Tacsdcsdsdcscsscscsdcsdcsdcsd");
            assertEquals(Status.INVALID_ARGUMENT.getCode(),
                    assertThrows(StatusRuntimeException.class,
                            () -> frontend.camInfo(camInfoRequestBuilder)).getStatus().getCode());
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void emptyNameInfo(){
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder().setCamName("");
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequestBuilder)).getStatus().getCode());
    }

    @Test
    public void invalidCharsInNameInfo(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);

            CamInfoRequest.Builder camInfoRequestBuilder = CamInfoRequest.newBuilder().setCamName("Tagus55())(");
            assertEquals(Status.INVALID_ARGUMENT.getCode(),
                    assertThrows(StatusRuntimeException.class,
                            () -> frontend.camInfo(camInfoRequestBuilder)).getStatus().getCode());
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void camDoesNotExistInfo(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);

            CamInfoRequest.Builder camInfoRequestBuilder = CamInfoRequest.newBuilder().setCamName("Taguscssa");
            assertEquals(Status.NOT_FOUND.getCode(),
                    assertThrows(StatusRuntimeException.class,
                            () -> frontend.camInfo(camInfoRequestBuilder)).getStatus().getCode());
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }

    }

    // Auxiliary method
    private void setupCam() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
                .setCamName("Tagus")
                .setCoordinates(coordinates);
        try {
            frontend.camJoin(camJoinRequestBuilder);
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
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
        try {
            frontend.report(reportRequestBuilder);
        } catch (NoServersException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void reportUnregisteredCamNameTest() {
        setupCam();

        ReportRequest.Builder reportRequestBuilder =
                ReportRequest.newBuilder().setCamName("Trudy");

        reportRequestBuilder.addData(
                ObjectData.newBuilder().setType(ObjectType.PERSON).setId("3").build());

        assertEquals(Status.Code.UNAUTHENTICATED,
                assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequestBuilder))
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
            ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Tagus")
                .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId(id).build());
            assertEquals(Status.Code.INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequestBuilder))
                    .getStatus().getCode());
        }
    }

    @Test
    public void reportInvalidCarIdsTest() {
        String[] carIds = {"A", "aa11aa", "-12AABB", "CCCCFF", "36CF2", "SS44SS4", "K4BB00", ""};

        setupCam();

        for (String id: carIds) {
            ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Tagus")
                .addData(ObjectData.newBuilder().setType(ObjectType.CAR).setId(id).build());
            assertEquals(Status.Code.INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequestBuilder))
                    .getStatus().getCode());
        }
    }

    @Test
    public void reportMultipleInvalidIdsTest() {
        setupCam();

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Tagus")
            .addData(ObjectData.newBuilder().setType(ObjectType.CAR).setId("ABC123").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("-2").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.CAR).setId("xh2245").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("0.1").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("").build());
        assertEquals(Status.Code.INVALID_ARGUMENT,
            assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequestBuilder))
                .getStatus().getCode());
    }

    @Test
    public void reportValidAndInvalidIdsTest() {
        setupCam();

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Tagus")
            .addData(ObjectData.newBuilder().setType(ObjectType.PERSON).setId("3").build())
            .addData(ObjectData.newBuilder().setType(ObjectType.CAR).setId("ABC123").build());
        assertEquals(Status.Code.INVALID_ARGUMENT,
            assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequestBuilder))
                .getStatus().getCode());
    }
}
