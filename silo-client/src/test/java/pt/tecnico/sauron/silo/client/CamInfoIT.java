package pt.tecnico.sauron.silo.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CamInfoIT extends BaseIT{

    @BeforeEach
    public void setup(){
        frontend.ctrlInit(CtrlInitRequest.newBuilder().getDefaultInstanceForType());
    }


    // clean-up for each test
    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.newBuilder().getDefaultInstanceForType());
    }

    // tests

    @Test
    public void testOk() {
        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Tagus").build();
        CamInfoReply camInfoReply = frontend.camInfo(camInfoRequest);

        assertEquals(38.737613,
                camInfoReply.getCoordinates().getLatitude());

        assertEquals(-9.303164,
                camInfoReply.getCoordinates().getLongitude());
    }

    @Test
    public void nameTooShort(){
        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Ta").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());
    }

    @Test
    public void nameTooLong(){
        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Tacsdcsdsdcscsscscsdcsdcsdcsd").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());
    }

    @Test
    public void invalidCharsInName(){
        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Tagus55())(").build();
        assertEquals(Status.INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());
    }

    @Test
    public void camDoesNotExist(){
        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setCamName("Taguscssa").build();

        assertEquals(Status.NOT_FOUND.getCode(),
                assertThrows(StatusRuntimeException.class,
                        () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());

    }
}
