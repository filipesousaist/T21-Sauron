package pt.tecnico.sauron.silo.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;


import static org.junit.jupiter.api.Assertions.*;

public class CamJoinIT extends BaseIT{
    // clean-up for each test
    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.newBuilder().getDefaultInstanceForType());
    }

    // tests

    @Test
    public void testOk() {
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
    public void invalidName() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder()
                .setCamName("Cam1()[/")
                .setCoordinates(coordinates)
                .build();

        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());
    }
}
