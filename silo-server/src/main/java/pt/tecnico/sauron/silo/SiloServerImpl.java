package pt.tecnico.sauron.silo;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.domain.exception.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {
    private SiloServer siloServer = new SiloServer();

    @Override
    public void ctrlPing(StringMessage request, StreamObserver<StringMessage> responseObserver) {
        String msg = siloServer.ping(request.getText());
        StringMessage response = StringMessage.newBuilder().setText(msg).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(EmptyMessage request, StreamObserver<StringMessage> responseObserver) {
        String msg  = siloServer.clear();
        StringMessage clearResponse = StringMessage.newBuilder().setText(msg).build();

        responseObserver.onNext(clearResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlInit(EmptyMessage request, StreamObserver<StringMessage> responseObserver) {
        String msg  = siloServer.init();
        StringMessage clearResponse = StringMessage.newBuilder().setText(msg).build();

        responseObserver.onNext(clearResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void camJoin(EyeJoinRequest request, StreamObserver<EmptyMessage> responseObserver) {
        try {
            siloServer.cam_join(request.getCamName(), request.getCoordinates());
            responseObserver.onNext(EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
        catch (InvalidEyeNameException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
        }
        catch (InvalidCoordinatesException e) {
            responseObserver.onError(Status.OUT_OF_RANGE.asRuntimeException());
        }
        catch (DuplicateJoinException e) {
            responseObserver.onError(Status.OK.asRuntimeException()); // Allow duplicate joins
        }
        catch (RepeatedNameException e) {
            responseObserver.onError(Status.ALREADY_EXISTS.asRuntimeException());
        }
    }

    @Override
    public void camInfo(StringMessage request, StreamObserver<Coordinates> responseObserver) {
        try {
            Coordinates response = siloServer.cam_info(request.getText());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (UnregisteredEyeException e) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
    }

    @Override
    public void report(EyeObservation request, StreamObserver<EmptyMessage> responseObserver) {
        try {
            siloServer.report(request.getDataList(), request.getCamName());

            responseObserver.onNext(EmptyMessage.newBuilder().build());

            responseObserver.onCompleted();
        }
        catch (InvalidIdException e) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (UnregisteredEyeException e) {
            responseObserver.onError(
                Status.UNAUTHENTICATED.asRuntimeException());
        }
    }

    @Override
    public void track(ObjectData request, StreamObserver<ObservationResponse> responseObserver) {
        List<Observation> observations = new LinkedList<>();
        Optional<Observation> result = siloServer.track(request.getId(), request.getType());
        result.ifPresent(observations::add);

        ObservationResponse response = buildObservationResponse(observations);

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void trackMatch(ObjectData request, StreamObserver<ObservationResponse> responseObserver) {
        List<Observation> observations = siloServer.trackMatch(request.getId(), request.getType());
        ObservationResponse response = buildObservationResponse(observations);

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
    @Override
    public void trace(ObjectData request, StreamObserver<ObservationResponse> responseObserver) {
        List<Observation> observations = siloServer.trace(request.getId(), request.getType());
        ObservationResponse response = buildObservationResponse(observations);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    private ObservationResponse buildObservationResponse(List<Observation> observations){
        ObservationResponse.Builder builder = ObservationResponse.newBuilder();
        List<ObservationData> observationDataList = new LinkedList<>();

        for(Observation obs : observations) {

            Timestamp ts = Timestamp.newBuilder().setSeconds(obs.getDate().getTime()/1000).setNanos(0).build();

            ObservationData observationData = ObservationData.newBuilder()
                    .setType(obs.getType())
                    .setTimestamp(ts)
                    .setId(obs.getStrId())
                    .setCamName(obs.getCamName())
                    .build();

            observationDataList.add(observationData);
        }
        builder.addAllData(observationDataList);
        return builder.build();
    }
}
