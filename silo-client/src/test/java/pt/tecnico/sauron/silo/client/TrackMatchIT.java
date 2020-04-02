package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.math.BigInteger;
import java.util.Date;

public class TrackMatchIT extends BaseIT {

    static Timestamp ts;
    private static int maxDelay;


    @BeforeAll
    public static void beforeAll() {
        maxDelay = Integer.parseInt(testProps.getProperty("server.maxdelay"));
    }

    @BeforeEach
    public void setUp() {
        ts = Timestamp.newBuilder().setSeconds((new Date()).getTime() / 1000).setNanos(0).build();
        frontend.ctrlInit(CtrlInitRequest.getDefaultInstance());
    }

    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.getDefaultInstance());
    }



}
