package pt.tecnico.sauron.silo;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.domain.exception.*;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

import javax.sound.midi.Track;
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
            responseObserver.onNext(EmptyMessage.getDefaultInstance());
            responseObserver.onCompleted();
        }
        catch (InvalidEyeNameException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
        }
        catch (InvalidCoordinatesException e) {
            responseObserver.onError(Status.OUT_OF_RANGE.asRuntimeException());
        }
        catch (DuplicateJoinException e) {
            responseObserver.onError(Status.ALREADY_EXISTS.asRuntimeException()); // Allow duplicate joins
        }
        catch (RepeatedNameException e) {
            responseObserver.onError(Status.PERMISSION_DENIED.asRuntimeException());
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

            responseObserver.onNext(EmptyMessage.getDefaultInstance());

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
    public void track(TrackRequest request, StreamObserver<TrackReply> responseObserver) {
        Observation observation = siloServer.track(request.getData().getId(), request.getData().getType());
        Timestamp ts = Timestamp.newBuilder().setSeconds(observation.getDate().getTime()/1000).setNanos(0).build();

        ObservationData observationData = ObservationData.newBuilder()
                .setType(observation.getType())
                .setTimestamp(ts)
                .setId(observation.getStrId())
                .setCamName(observation.getCamName())
                .build();

        TrackReply trackReply = TrackReply.newBuilder().setData(observationData).build();

        responseObserver.onNext(trackReply);

        responseObserver.onCompleted();
    }

    @Override
    public void trackMatch(TrackMatchRequest request, StreamObserver<TrackMatchReply> responseObserver) {
        List<Observation> observations = siloServer.trackMatch(request.getData().getId(), request.getData().getType());
        List<ObservationData> observationDataList = new LinkedList<>();
        buildObservationData(observations, observationDataList);

        TrackMatchReply trackMatchReply = TrackMatchReply.newBuilder().addAllData(observationDataList).build();

        responseObserver.onNext(trackMatchReply);
        responseObserver.onCompleted();
    }


    @Override
    public void trace(TraceRequest request, StreamObserver<TraceReply> responseObserver) {
        List<Observation> observations = siloServer.trace(request.getData().getId(), request.getData().getType());
        List<ObservationData> observationDataList = new LinkedList<>();
        buildObservationData(observations, observationDataList);

        TraceReply traceReply = TraceReply.newBuilder().addAllData(observationDataList).build();

        responseObserver.onNext(traceReply);
        responseObserver.onCompleted();
    }


    private void buildObservationData(List<Observation> observations, List<ObservationData> observationDataList) {
        Timestamp ts;
        ObservationData observationData;

        for (Observation obs : observations) {
            ts = Timestamp.newBuilder().setSeconds(obs.getDate().getTime() / 1000).setNanos(0).build();

            observationData = ObservationData.newBuilder()
                    .setType(obs.getType())
                    .setTimestamp(ts)
                    .setId(obs.getStrId())
                    .setCamName(obs.getCamName())
                    .build();

            observationDataList.add(observationData);
        }
    }

}
