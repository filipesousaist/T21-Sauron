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
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SiloServerImpl extends SiloServiceGrpc.SiloServiceImplBase {
    private static final String BASE_PATH = "/grpc/sauron/silo";
    private static final int GOSSIP_DELAY = 500;
    private SiloServer siloServer = new SiloServer();
    private HashMap<Integer, SiloServiceBlockingStub> connections = new HashMap<>();
    private HashMap<Integer, VectorTS> otherReplicasTS = new HashMap<>();
    private VectorTS replicaTS; // Vector timestamp
    private int instance;

    private List<ObsLog> obsLogs = new ArrayList<>();
    private List<CamLog> camLogs = new ArrayList<>();

    ZKNaming zkNaming = null;

    private Thread gossipThread;
    private boolean willShutdown = false;

    public SiloServerImpl(int instance, String zkHost, String zkPort) {
        super();
        zkNaming = new ZKNaming(zkHost, zkPort);
        this.instance = instance;


        replicaTS = new VectorTS(instance);

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
        GossipMessage.Builder gossipMessageBuilder = GossipMessage.newBuilder();


        System.out.println("Gossiping...");
        System.out.println(camLogs);
        System.out.println(obsLogs);

        try {
            List<ZKRecord> conns = new ArrayList<>(zkNaming.listRecords(BASE_PATH));
            List<Integer> connectedInsntances = conns.stream()
                    .map(ZKRecord::getPath)
                    .map(this::extractInstance)
                    .collect(Collectors.toList());

            //check for potential new instances
            for (ZKRecord r : conns) {
                currInstance = extractInstance(r.getPath());
                if(!existsConnection(currInstance) && currInstance != this.instance){
                    String target = zkNaming.lookup(BASE_PATH + "/" + currInstance).getURI();
                    channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                    connections.put(currInstance, SiloServiceGrpc.newBlockingStub(channel));
                }
            }

            for(Integer i : connections.keySet()){
                if(!connectedInsntances.contains(i))
                    conns.remove(i);
            }

            for(Integer s : connections.keySet()) {
                // build observation logs
                gossipMessageBuilder.addAllObservationLogMessage(obsLogs.stream()
                        .filter(o -> otherReplicasTS.get(s).happensBeforeOrEquals(o.getVectorTS()))
                        .map(this::buildObservationLogMessage)
                        .collect(Collectors.toList()));

                // Build cam logs
                gossipMessageBuilder.addAllCamJoinRequest(camLogs.stream()
                        .map(this::buildCamJoinRequest)
                        .collect(Collectors.toList()));

                gossipMessageBuilder.setReplicaInstance(this.instance);
                gossipMessageBuilder.addAllVecTS(this.replicaTS);
                connections.get(s).gossip(gossipMessageBuilder.build());
            }

        } catch (ZKNamingException e) {
            e.printStackTrace();
        }
    }

    private ObservationLogMessage buildObservationLogMessage(ObsLog obsLog){
        ObservationLogMessage observationLogMessage;
        List<ObservationData> observationDataList = new LinkedList<>();
        buildObservationData(obsLog.getObss(), observationDataList);
        observationLogMessage = ObservationLogMessage.newBuilder()
                .addAllData(observationDataList)
                .addAllPrevTS(obsLog.getVectorTS())
                .setOpId(obsLog.getOpId())
                .build();

        return observationLogMessage;
    }

    private CamJoinRequest buildCamJoinRequest(CamLog camLog){

        Coordinates c = Coordinates.newBuilder()
                .setLatitude(camLog.getLatitude())
                .setLongitude(camLog.getLongitude())
                .build();

        return CamJoinRequest.newBuilder()
                .addAllPrevTS(this.replicaTS)
                .setCamName(camLog.getCamName())
                .setCoordinates(c)
                .build();
    }


    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinReply> responseObserver) {
        try {
            siloServer.cam_join(request.getCamName(), request.getCoordinates());
            camLogs.add(new CamLog(request.getCoordinates(), request.getCamName(), request.getOpId()));
            VectorTS vectorTS = new VectorTS(request.getPrevTSList());

            replicaTS.incr(instance);
            vectorTS.set(instance, replicaTS.get(instance));
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
            Date date = new Date();
            VectorTS vectorTS;
            siloServer.report(request.getDataList(), request.getCamName(), date);
            vectorTS = new VectorTS(request.getPrevTSList());
            System.out.println(request.getOpId()); //debug
            this.obsLogs.add(new ObsLog(request.getDataList(), request.getCamName(), date, vectorTS, request.getOpId()));

            replicaTS.incr(instance);
            vectorTS.set(instance, replicaTS.get(instance));
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
    public void gossip(GossipMessage request, StreamObserver<GossipReply> responseObserver) {
        VectorTS incVectorTS;
        for (CamJoinRequest cjr : request.getCamJoinRequestList()) {
            if (findByOpId2(cjr.getOpId(), cjr.getCamName()).isEmpty()) {
                siloServer.addEye(cjr.getCamName(), cjr.getCoordinates());
                camLogs.add(new CamLog(cjr.getCoordinates(), cjr.getCamName(), cjr.getOpId()));
            }
        }

        for (ObservationLogMessage olm : request.getObservationLogMessageList()) {
            String camName = olm.getData(0).getCamName();
            if (findByOpId(olm.getOpId(), camName).isEmpty()
                    && camLogs.stream().map(CamLog::getCamName).collect(Collectors.toList()).contains(camName)) {// checks if cam is registered before adding and observation
                obsLogs.add(new ObsLog(olm));
                siloServer.addObservations(olm.getDataList());
            }
        }
        incVectorTS = new VectorTS(request.getVecTSList());
        System.out.println(otherReplicasTS);
        this.otherReplicasTS.put(request.getReplicaInstance(), incVectorTS);
        this.replicaTS.update(incVectorTS);


        responseObserver.onNext(GossipReply.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private Optional<ObsLog> findByOpId(int opId, String camName){
        return obsLogs.stream()
                .filter(o -> o.getOpId() == opId && o.getCamName().equals(camName))
                .findFirst();
    }

    private Optional<CamLog> findByOpId2(int opId, String camName){
        return camLogs.stream()
                .filter(o -> o.getOpId() == opId && o.getCamName().equals(camName))
                .findFirst();
    }

    @Override
    public void ctrlPing(CtrlPingRequest request, StreamObserver<CtrlPingReply> responseObserver) {
        String msg = siloServer.ping(request.getText()) + instance;
        CtrlPingReply response = CtrlPingReply.newBuilder().setText(msg).addAllValueTS(replicaTS).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(CtrlClearRequest request, StreamObserver<CtrlClearReply> responseObserver) {
        String msg  = siloServer.clear();
        obsLogs.clear();
        camLogs.clear();
        otherReplicasTS.clear();
        connections.clear();
        replicaTS = new VectorTS(instance);

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

