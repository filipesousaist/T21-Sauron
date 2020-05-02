package pt.tecnico.sauron.silo;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.CamLog;
import pt.tecnico.sauron.silo.domain.ObsLog;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {
    private static final String BASE_PATH = "/grpc/sauron/silo";

    private SiloServer siloServer = new SiloServer();
    private Map<Integer, SiloServiceBlockingStub> connections = new ConcurrentHashMap<>();
    private VectorTS replicaTS; // Vector timestamp
    private int instance;

    private Map<VectorTS, ObsLog> obsLogs = new HashMap<>();
    private Map<VectorTS, CamLog> camLogs = new HashMap<>();

    ZKNaming zkNaming;

    private Thread gossipThread;
    private boolean willShutdown = false;

    public SiloServerImpl(int instance, String zkHost, String zkPort, int gossipDelay) {
        super();
        zkNaming = new ZKNaming(zkHost, zkPort);
        this.instance = instance;

        replicaTS = new VectorTS();

        gossipThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(gossipDelay);
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

        /*synchronized (this) {
            System.out.println(camLogs);
            System.out.println(obsLogs);
        }*/

        try {
            List<ZKRecord> conns = new ArrayList<>(zkNaming.listRecords(BASE_PATH));
            List<Integer> connectedInstances = conns.stream()
                .map(ZKRecord::getPath)
                .map(this::extractInstance)
                .collect(Collectors.toList());

            //check for potential new instances
            for (ZKRecord r : conns) {
                currInstance = extractInstance(r.getPath());
                if (!existsConnection(currInstance) && currInstance != instance) {
                    String target = zkNaming.lookup(BASE_PATH + "/" + currInstance).getURI();
                    channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                    connections.put(currInstance, SiloServiceGrpc.newBlockingStub(channel));
                }
            }

            GossipRequest gossipRequest;
            for (Integer i : connections.keySet())
                if (!connectedInstances.contains(i))
                    connections.remove(i);

            synchronized (this) {
                 gossipRequest = GossipRequest.newBuilder()
                        .putAllVecTS(this.replicaTS.getMap())
                        .build();
            }

            System.out.println("Requesting Updates from other replicas...");
            connections.values().forEach(c -> {
                try {
                    GossipReply gossipReply = c.gossip(gossipRequest);
                    replicaTS.update(new VectorTS(gossipReply.getReplicaTSMap()));

                    System.out.println("Receiving new cam updates...");
                    gossipReply.getCamJoinRequestList().forEach(cjr -> {
                        siloServer.addEye(cjr.getCamName(), cjr.getCoordinates());
                        VectorTS prevTS = new VectorTS(cjr.getPrevTSMap());
                        CamLog incCamLog = new CamLog(cjr.getCoordinates(), cjr.getCamName(), prevTS);
                        System.out.println("\t"+incCamLog);
                        synchronized (this) {
                            camLogs.put(prevTS, incCamLog);
                        }
                    });

                    System.out.println("Receiving new observation updates...");
                    gossipReply.getObservationLogMessageList().forEach(olm -> {
                        if (!olm.getDataList().isEmpty()) {
                            String camName = olm.getData(0).getCamName();
                            synchronized (this) {
                                // checks if cam is registered before adding an observation
                                if (camLogs.values().stream().map(CamLog::getCamName).collect(Collectors.toList()).contains(camName)) {
                                    ObsLog incObsLog = new ObsLog(olm);
                                    System.out.println("\t"+incObsLog);
                                    obsLogs.put(new VectorTS(olm.getPrevTSMap()), incObsLog);
                                    siloServer.addObservations(olm.getDataList());
                                }
                            }
                        }
                    });
                    System.out.println("Updates Received!\n");
                } catch (StatusRuntimeException e) {
                    if (!Status.UNAVAILABLE.getCode().equals(e.getStatus().getCode()))
                        System.out.println(e.getMessage());
                }
            });

        } catch (ZKNamingException e) {
            System.out.println(e.getMessage());
        }
    }

    private ObservationLogMessage buildObservationLogMessage(ObsLog obsLog) {
        ObservationLogMessage observationLogMessage;
        List<ObservationData> observationDataList = new LinkedList<>();
        buildObservationData(obsLog.getObss(), observationDataList);
        observationLogMessage = ObservationLogMessage.newBuilder()
            .addAllData(observationDataList)
            .putAllPrevTS(obsLog.getVectorTS().getMap())
            .build();

        return observationLogMessage;
    }

    private CamJoinRequest buildCamJoinRequest(CamLog camLog) {
        Coordinates c = Coordinates.newBuilder()
            .setLatitude(camLog.getLatitude())
            .setLongitude(camLog.getLongitude())
            .build();

        return CamJoinRequest.newBuilder()
            .putAllPrevTS(camLog.getVectorTS().getMap())
            .setCamName(camLog.getCamName())
            .setCoordinates(c)
            .build();
    }


    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinReply> responseObserver) {
        VectorTS prevTS = new VectorTS(request.getPrevTSMap());
        try {
            siloServer.cam_join(request.getCamName(), request.getCoordinates());
            CamJoinReply camJoinReply;
            synchronized (this) {
                replicaTS.incr(instance);
                prevTS.set(instance, replicaTS.get(instance));
                camLogs.put(prevTS, new CamLog(request.getCoordinates(), request.getCamName(), prevTS));
                camJoinReply = CamJoinReply.newBuilder().putAllValueTS(replicaTS.getMap()).build();
            }
            responseObserver.onNext(camJoinReply);
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
            CamInfoReply response;
            synchronized (this) {
                 response = CamInfoReply.newBuilder()
                    .setCoordinates(coordinates)
                    .putAllValueTS(replicaTS.getMap())
                    .build();
            }
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
            Date date = new Date();
            VectorTS prevTS;
            siloServer.report(request.getDataList(), request.getCamName(), date);
            prevTS = new VectorTS(request.getPrevTSMap());
            synchronized (this) {
                replicaTS.incr(instance);
                prevTS.set(instance, replicaTS.get(instance));

                obsLogs.putIfAbsent(prevTS, new ObsLog(request.getDataList(), request.getCamName(), date, prevTS));
            }
            responseObserver.onNext(ReportReply.newBuilder().putAllValueTS(prevTS.getMap()).build());
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

            Map<Integer, Integer> replicaTSMap;
            synchronized (this) {
                replicaTSMap = replicaTS.getMap();
            }
            TrackReply trackReply = TrackReply.newBuilder()
                .setData(observationData)
                .putAllValueTS(replicaTSMap)
                .build();

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

            Map<Integer, Integer> replicaTSMap;
            synchronized (this) {
                replicaTSMap = replicaTS.getMap();
            }
            TrackMatchReply trackMatchReply = TrackMatchReply.newBuilder()
                .addAllData(observationDataList)
                .putAllValueTS(replicaTSMap)
                .build();

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

            Map<Integer, Integer> replicaTSMap;
            synchronized (this) {
                replicaTSMap = replicaTS.getMap();
            }
            TraceReply traceReply = TraceReply.newBuilder()
                .addAllData(observationDataList)
                .putAllValueTS(replicaTSMap)
                .build();

            responseObserver.onNext(traceReply);
            responseObserver.onCompleted();

        } catch (InvalidIdException e) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (NoObservationFoundException e) {
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
    public void gossip(GossipRequest request, StreamObserver<GossipReply> responseObserver) {
        VectorTS incVectorTS = new VectorTS(request.getVecTSMap());
        GossipReply.Builder gossipReplyBuilder = GossipReply.newBuilder();
        GossipReply gossipReply;
        try {
            System.out.println("Sending updates...\n");
            // build observation logs
            gossipReplyBuilder.addAllObservationLogMessage(obsLogs.values().stream()
                    .filter(o -> incVectorTS.happensBefore(o.getVectorTS()))
                    .map(this::buildObservationLogMessage)
                    .collect(Collectors.toList()));

            // Build cam logs
            synchronized (this) {
                gossipReplyBuilder.addAllCamJoinRequest(camLogs.values().stream()
                        .filter(c -> incVectorTS.happensBefore(c.getVectorTS()))
                        .map(this::buildCamJoinRequest)
                        .collect(Collectors.toList()));

                gossipReplyBuilder.putAllReplicaTS(replicaTS.getMap());
            }
            gossipReply = gossipReplyBuilder.build();
            responseObserver.onNext(gossipReply);
            responseObserver.onCompleted();
            System.out.println("Updates sent!\n");

        } catch (StatusRuntimeException e) {
            if (!e.getStatus().getCode().equals(Status.UNAVAILABLE.getCode()))
                throw e;
        }
    }

    @Override
    public void ctrlPing(CtrlPingRequest request, StreamObserver<CtrlPingReply> responseObserver) {
        String msg = siloServer.ping(request.getText()) + instance;

        Map<Integer, Integer> replicaTSMap;
        synchronized (this) {
            replicaTSMap = replicaTS.getMap();
        }
        CtrlPingReply response = CtrlPingReply.newBuilder()
            .setText(msg)
            .putAllValueTS(replicaTSMap)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(CtrlClearRequest request, StreamObserver<CtrlClearReply> responseObserver) {
        String msg  = siloServer.clear();
        connections.clear();

        Map<Integer, Integer> replicaTSMap;
        synchronized (this) {
            obsLogs.clear();
            camLogs.clear();
            replicaTS = new VectorTS();
            replicaTSMap = replicaTS.getMap();
        }
        CtrlClearReply clearResponse = CtrlClearReply.newBuilder()
            .setText(msg)
            .putAllValueTS(replicaTSMap)
            .build();

        responseObserver.onNext(clearResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlInit(CtrlInitRequest request, StreamObserver<CtrlInitReply> responseObserver) {
        VectorTS prevTS = new VectorTS(request.getPrevTSMap());
        synchronized (this) {
            replicaTS.incr(instance);
            prevTS.set(instance, replicaTS.get(instance));
        }
        String msg  = siloServer.init();
        CtrlInitReply initResponse = CtrlInitReply.newBuilder()
            .setText(msg)
            .putAllValueTS(prevTS.getMap())
            .build();

        responseObserver.onNext(initResponse);
        responseObserver.onCompleted();
    }
}

