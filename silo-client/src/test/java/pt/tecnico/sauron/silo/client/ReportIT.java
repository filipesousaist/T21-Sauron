package pt.tecnico.sauron.silo.client;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ReportIT extends BaseIT {
    @BeforeEach
    public void setup() {
        frontend.ctrlInit(CtrlInitRequest.getDefaultInstance());
    }

    @AfterEach
    public void tearDown() {
        frontend.ctrlClear(CtrlClearRequest.getDefaultInstance());
    }

    @Test
    public void validTest() {
        String[] personIds = {"0", "1", "36517", Long.toString(Long.MAX_VALUE)};
        String[] carIds = {"ABCD12", "EF34GH", "56IJKL", "0000ZZ", "67LC95", "XY3774"};

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName("Tagus");

        for (String id: personIds)
            reportRequestBuilder.addData(
                ReportData.newBuilder().setType(ObjectType.PERSON).setId(id));
        for (String id: carIds)
            reportRequestBuilder.addData(
                ReportData.newBuilder().setType(ObjectType.CAR).setId(id));

        frontend.report(reportRequestBuilder.build());
    }

    @Test
    public void unregisteredCamNameTest() {
        ReportRequest.Builder reportRequestBuilder =
            ReportRequest.newBuilder().setCamName("Trudy");

        reportRequestBuilder.addData(
            ReportData.newBuilder().setType(ObjectType.PERSON).setId("0").build());

        assertEquals(Code.UNAUTHENTICATED,
            assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequestBuilder.build()))
                .getStatus().getCode());
    }

    @Test
    public void invalidPersonIdsTest() {
        final BigInteger LARGEID = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        final BigInteger LARGEID2 = LARGEID.add(BigInteger.valueOf(42153241));
        String[] personIds = {"-1", "-7828426", LARGEID.toString(), LARGEID2.toString(), "1.2",
            "abc", "/", "(Y#(F!H))", ""/*, null*/};

        for (String id: personIds) {
            ReportRequest reportRequest = ReportRequest.newBuilder().setCamName("Alameda")
                .addData(ReportData.newBuilder().setType(ObjectType.PERSON).setId(id).build()).build();
            assertEquals(Code.INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequest))
                    .getStatus().getCode());
        }
    }

    @Test
    public void invalidCarIdsTest() {
        String[] carIds = {"A", "aa11aa", "-12AABB", "CCCCFF", "36CF2", "SS44SS4", "K4BB00", ""/*, null*/};

        for (String id: carIds) {
            ReportRequest reportRequest = ReportRequest.newBuilder().setCamName("Alameda")
                .addData(ReportData.newBuilder().setType(ObjectType.CAR).setId(id).build()).build();
            assertEquals(Code.INVALID_ARGUMENT,
                assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequest))
                    .getStatus().getCode());
        }
    }

    @Test
    public void multipleInvalidIdsTest() {
        ReportRequest reportRequest = ReportRequest.newBuilder().setCamName("Tagus")
            .addData(ReportData.newBuilder().setType(ObjectType.CAR).setId("ABC123").build())
            .addData(ReportData.newBuilder().setType(ObjectType.PERSON).setId("-2").build())
            .addData(ReportData.newBuilder().setType(ObjectType.CAR).setId("xh2245").build())
            .addData(ReportData.newBuilder().setType(ObjectType.PERSON).setId("0.1").build())
            .addData(ReportData.newBuilder().setType(ObjectType.PERSON).setId("").build())
            .build();
        assertEquals(Code.INVALID_ARGUMENT,
            assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequest))
                .getStatus().getCode());
    }

    @Test
    public void validAndInvalidIds() {
        ReportRequest reportRequest = ReportRequest.newBuilder().setCamName("Tagus")
            .addData(ReportData.newBuilder().setType(ObjectType.PERSON).setId("3").build())
            .addData(ReportData.newBuilder().setType(ObjectType.CAR).setId("ABC123").build())
            .build();
        assertEquals(Code.INVALID_ARGUMENT,
            assertThrows(StatusRuntimeException.class, () -> frontend.report(reportRequest))
                .getStatus().getCode());
    }
}
