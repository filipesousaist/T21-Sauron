package pt.tecnico.sauron.silo;

import com.google.protobuf.Value;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.domain.SiloServer;
import pt.tecnico.sauron.silo.domain.exception.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;

import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc.*;
import pt.tecnico.sauron.util.VectorTS;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {
    private static final String BASE_PATH = "/grpc/sauron/silo";
    private static final int GOSSIP_DELAY = 5000;
    private SiloServer siloServer = new SiloServer();
    private HashMap<Integer, SiloServiceBlockingStub> connections = new HashMap<>();
    private VectorTS replicaTS; // Vector timestamp
    private VectorTS valueTS;
    private int instance;


    ZKNaming zkNaming = null;

    private Thread gossipThread;
    private boolean willShutdown = false;

    public SiloServerImpl(int instance, String zkHost, String zkPort) {
        super();
        zkNaming = new ZKNaming(zkHost, zkPort);
        this.instance = instance;


        replicaTS = new VectorTS(instance);
        valueTS = new VectorTS(instance);

        gossipThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(GOSSIP_DELAY);
                    synchronized(this) {
                        if (willShutdown)
                            break;
                    }
                    gossip();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        gossipThread.start();
    }

    private boolean existsConnection(int ins){
        return connections.containsKey(ins);
    }

    private int extractInstance(String path){
        return Integer.parseInt(String.valueOf(path.charAt(path.length()-1)));
    }

    public void shutdown() {
        synchronized (this) {
            // Mark gossip thread for termination
            willShutdown = true;
        }
        try {
            // Await gossip thread termination
            gossipThread.join();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void gossip() {
        int currInstance;
        ManagedChannel channel;

        System.out.println("Gossiping...");
        try {
            ArrayList<ZKRecord> conns = new ArrayList<>(zkNaming.listRecords(BASE_PATH));

            //check for potential new instances
            for (ZKRecord r : conns) {
                currInstance = extractInstance(r.getPath());
                if (!existsConnection(currInstance) && currInstance != this.instance) {
                    String target = zkNaming.lookup(BASE_PATH + "/" + currInstance).getURI();
                    System.out.println("New instance showed up!");
                    System.out.println(target);
                    channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                    connections.put(currInstance, SiloServiceGrpc.newBlockingStub(channel));
                }

            }

        } catch (ZKNamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinReply> responseObserver) {
        try {
            siloServer.cam_join(request.getCamName(), request.getCoordinates());
            replicaTS.incr(instance);
            responseObserver.onNext(CamJoinReply.newBuilder().addAllValueTS(replicaTS).build());
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
    public void camInfo(CamInfoRequest request, StreamObserver<CamInfoReply> responseObserver) {
        try {
            Coordinates coordinates = siloServer.cam_info(request.getCamName());
            CamInfoReply response = CamInfoReply.newBuilder().setCoordinates(coordinates).addAllValueTS(replicaTS).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (InvalidEyeNameException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
        }
        catch (UnregisteredEyeException e) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
    }

    @Override
    public void report(ReportRequest request, StreamObserver<ReportReply> responseObserver) {
        try {
            siloServer.report(request.getDataList(), request.getCamName());
            replicaTS.incr(instance);
            responseObserver.onNext(ReportReply.newBuilder().addAllValueTS(replicaTS).build());

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
        try {
            Observation observation = siloServer.track(request.getData().getId(), request.getData().getType());

            Timestamp timestamp = Timestamp.newBuilder().setSeconds(observation.getDate().getTime() / 1000).setNanos(0).build();

            ObservationData observationData = ObservationData.newBuilder()
                .setType(observation.getType())
                .setTimestamp(timestamp)
                .setId(observation.getStrId())
                .setCamName(observation.getCamName())
                .build();

            TrackReply trackReply = TrackReply.newBuilder().setData(observationData).addAllValueTS(replicaTS).build();

            responseObserver.onNext(trackReply);
            responseObserver.onCompleted();

        } catch (InvalidIdException e) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (NoObservationFoundException e) {
            responseObserver.onError(
                Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void trackMatch(TrackMatchRequest request, StreamObserver<TrackMatchReply> responseObserver) {
        try {
            List<Observation> observations = siloServer.trackMatch(request.getData().getId(), request.getData().getType());

            List<ObservationData> observationDataList = new LinkedList<>();
            buildObservationData(observations, observationDataList);

            TrackMatchReply trackMatchReply = TrackMatchReply.newBuilder().addAllData(observationDataList).addAllValueTS(replicaTS).build();

            responseObserver.onNext(trackMatchReply);
            responseObserver.onCompleted();

        } catch (InvalidIdException e) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (NoObservationFoundException e) {
            responseObserver.onError(
                Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void trace(TraceRequest request, StreamObserver<TraceReply> responseObserver) {
        try {
            List<Observation> observations = siloServer.trace(request.getData().getId(), request.getData().getType());
            List<ObservationData> observationDataList = new LinkedList<>();
            buildObservationData(observations, observationDataList);

            TraceReply traceReply = TraceReply.newBuilder().addAllData(observationDataList).addAllValueTS(replicaTS).build();

            responseObserver.onNext(traceReply);
            responseObserver.onCompleted();

        }catch (InvalidIdException e){
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }catch (NoObservationFoundException e){
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
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

    @Override
    public void ctrlPing(CtrlPingRequest request, StreamObserver<CtrlPingReply> responseObserver) {
        String msg = siloServer.ping(request.getText());
        CtrlPingReply response = CtrlPingReply.newBuilder().setText(msg).addAllValueTS(replicaTS).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(CtrlClearRequest request, StreamObserver<CtrlClearReply> responseObserver) {
        String msg  = siloServer.clear();
        replicaTS.incr(instance);
        CtrlClearReply clearResponse = CtrlClearReply.newBuilder().setText(msg).addAllValueTS(replicaTS).build();

        responseObserver.onNext(clearResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlInit(CtrlInitRequest request, StreamObserver<CtrlInitReply> responseObserver) {
        String msg  = siloServer.init();
        replicaTS.incr(instance);
        CtrlInitReply initResponse = CtrlInitReply.newBuilder().setText(msg).addAllValueTS(replicaTS).build();

        responseObserver.onNext(initResponse);
        responseObserver.onCompleted();
    }
}
