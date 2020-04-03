package pt.tecnico.sauron.silo.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EyeIT extends BaseIT {

    @AfterEach
    public void tearDown() {
        CtrlClearRequest ctrlClearRequest = CtrlClearRequest.newBuilder().getDefaultInstanceForType();
        frontend.ctrlClear(ctrlClearRequest);
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
    public void invalidCoordindates1(){
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
    public void invalidCoordindates2(){
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
    public void invalidCoordindates3(){
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
    public void invalidCoordindates4(){
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
    public void validCoordindates1(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(-89.999999).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

    }

    @Test
    public void validCoordindates2(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(-179.999999).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();
    }


    @Test
    public void validCoordindates3(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.999999).setLongitude(0).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();
    }

    @Test
    public void validCoordindates4(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(179.999999).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

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
}
