/*
package pt.tecnico.sauron.silo.client;


import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@RunWith(Parameterized.class)
public class CamJoinIT extends BaseIT{
    // clean-up for each test
    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(EmptyMessage.newBuilder().getDefaultInstanceForType());
    }

    // tests

    @Test
    public void testOk() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(eyeJoinRequest);
        assertTrue(true);

    }

    @Test
    public void invalidCoordindates1(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(-91).setLongitude(0).build();
        EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();


        assertEquals(Status.OUT_OF_RANGE.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(eyeJoinRequest)).getStatus().getCode());
    }

    @Test
    public void invalidCoordindates2(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(-181).build();
        EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();


        assertEquals(Status.OUT_OF_RANGE.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(eyeJoinRequest)).getStatus().getCode());
    }


    @Test
    public void invalidCoordindates3(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(91).setLongitude(0).build();
        EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();


        assertEquals(Status.OUT_OF_RANGE.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(eyeJoinRequest)).getStatus().getCode());
    }

    @Test
    public void invalidCoordindates4(){
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(181).build();
        EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();


        assertEquals(Status.OUT_OF_RANGE.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(eyeJoinRequest)).getStatus().getCode());
    }

    @Test
    public void sameNameDifferentCoords() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(5).build();
        EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(eyeJoinRequest);


        coordinates = Coordinates.newBuilder().setLatitude(45).setLongitude(20).build();
        EyeJoinRequest eyeJoinRequest2 = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        assertEquals(Status.ALREADY_EXISTS.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(eyeJoinRequest2)).getStatus().getCode());
    }

    @Test
    public void sameCoordsDifferentName() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(eyeJoinRequest);


        EyeJoinRequest eyeJoinRequest2 = EyeJoinRequest.newBuilder()
                .setCamName("Cam2")
                .setCoordinates(coordinates)
                .build();

        frontend.camJoin(eyeJoinRequest2);
        assertTrue(true);
    }

    @Test
    public void sameCoordsSameName() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(0).setLongitude(0).build();
        EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();
        frontend.camJoin(eyeJoinRequest);


        EyeJoinRequest eyeJoinRequest2 = EyeJoinRequest.newBuilder()
                .setCamName("Cam1")
                .setCoordinates(coordinates)
                .build();

        assertEquals(Status.OK.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(eyeJoinRequest2)).getStatus().getCode());
    }

    @Test
    public void invalidName() {
        Coordinates coordinates = Coordinates.newBuilder().setLatitude(89.2315).setLongitude(55.669).build();
        EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
                .setCamName("Cam1/*")
                .setCoordinates(coordinates)
                .build();

        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camJoin(eyeJoinRequest)).getStatus().getCode());
    }
}

 */
