package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.exception.NoServersException;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc.*;
import pt.tecnico.sauron.util.VectorTS;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.*;
import java.util.concurrent.Callable;

public class SiloFrontend implements AutoCloseable {
    private static final String BASE_PATH = "/grpc/sauron/silo";
    private static final int MAX_SIZE = 10;
    private static final int MAX_CONNECT_TRIES = 5;
    private static final int MAX_REQUEST_TRIES = 3;
    private static final int SLEEP_TIME = 40;//milliseconds
    private ManagedChannel channel;
    private SiloServiceBlockingStub stub;
    private ZKNaming zkNaming;

    private int numServers;
    private VectorTS ts; // Vector timestamp
    private int instance;


    private SiloCache<SiloCacheKey, Message> cache;

    public SiloFrontend(String zkHost, String zkPort, int instance)
            throws ZKNamingException, NoServersException {
        zkNaming = new ZKNaming(zkHost, zkPort);

        numServers = zkNaming.listRecords(BASE_PATH).size();
        if (numServers == 0)
            throw new NoServersException();
        ts = new VectorTS();
        this.instance = instance;

        cache = new SiloCache<>(MAX_SIZE);

        connect();

    }

    private String getRandomTarget() throws ZKNamingException, NoServersException {
        ZKRecord[] records = zkNaming.listRecords(BASE_PATH).toArray(ZKRecord[]::new);
        if (records.length == 0)
            throw new NoServersException();

        return records[(new Random()).nextInt(numServers)].getURI();
    }

    private boolean connect() {
        // lookup
        for (int i = 0; i < MAX_CONNECT_TRIES; i++) {
            try {
                if (i > 0)
                    Thread.sleep(100);
                String target = instance >= 0 ?
                        zkNaming.lookup(BASE_PATH + "/" + instance).getURI()
                        : getRandomTarget();
                channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                stub = SiloServiceGrpc.newBlockingStub(channel);
                return true;
            } catch (ZKNamingException | NoServersException ignored) {
                //ignored
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    private class GenericRPC<Reply extends Message> {
        private Callable<Object[]> rpc;
        public GenericRPC(Callable<Object[]> rpc) {
            this.rpc = rpc;
        }

        public Reply call(SiloCacheKey cacheKey) throws NoServersException {
            for (int i = 0; i < MAX_REQUEST_TRIES; i ++) {
                try {
                    System.out.println("Before call ");
                    Object[] object = rpc.call();

                    return getFromCache(cacheKey, object);

                } catch (StatusRuntimeException e) {
                    onRuntimeStatusException(e,cacheKey);
                } catch (Exception ignored) {}
            }
            System.out.println("Failed to reconnect ");
            throw new StatusRuntimeException(Status.UNAVAILABLE);
        }

        private Reply getFromCache(SiloCacheKey cacheKey, Object[] object) {
            Reply reply = (Reply) object[0];
            SiloCacheKey.OperationType operationType = cacheKey.getOperationType();
            System.out.println("Use cache? ");
            if (operationType != SiloCacheKey.OperationType.CAM_JOIN &&
                operationType != SiloCacheKey.OperationType.REPORT) {
                System.out.println("Yes ");
                VectorTS valueTS = (VectorTS) object[1];

                if (ts.happensBeforeOrEquals(valueTS)) {
                    System.out.println("Happens before or equals ");
                    if (valueTS.isZero())
                        throw new StatusRuntimeException(Status.NOT_FOUND);
                    cache.remove(cacheKey);
                    cache.put(cacheKey, reply);
                    ts.update(valueTS);
                } else if (cache.containsKey(cacheKey)) {
                    System.out.println("Happens after or concurrent");
                    reply = (Reply) cache.remove(cacheKey);
                    cache.put(cacheKey, reply);
                } else {
                    System.out.println("Not Found");
                    throw new StatusRuntimeException(Status.NOT_FOUND);
                }
            }
            return reply;
        }

        private void onRuntimeStatusException(StatusRuntimeException e, SiloCacheKey cacheKey) throws NoServersException {
            System.out.println("Frontend error: " + e.getMessage());
            Status.Code code = e.getStatus().getCode();
            if (!Status.UNAVAILABLE.getCode().equals(code)) {
                throw e;
            } else if (!connect()) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
                throw new NoServersException();
            } else { //connected
                if (cacheKey.getOperationType() == SiloCacheKey.OperationType.REPORT)
                    throw new StatusRuntimeException(Status.UNAUTHENTICATED);
            }
        }
    }

    //camJoin
    public CamJoinReply camJoin(CamJoinRequest.Builder requestBuilder) throws NoServersException {
        CamJoinRequest request = requestBuilder
                .putAllPrevTS(ts.getMap())
                .build();
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.CAM_JOIN,
                SiloCacheKey.ObjectType.NONE,
                request.getCamName());
        return (new GenericRPC<CamJoinReply>(() -> camJoinRPC(request))).call(key);
    }

    private Object[] camJoinRPC(CamJoinRequest request) {
        CamJoinReply reply = stub.camJoin(request);
        ts.update(new VectorTS(reply.getValueTSMap()));
        return new Object[]{reply};
    }

    //camInfo
    public CamInfoReply camInfo(CamInfoRequest.Builder requestBuilder) throws NoServersException {
        CamInfoRequest request = requestBuilder.putAllPrevTS(ts.getMap()).build();
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.CAM_INFO,
                SiloCacheKey.ObjectType.NONE,
                request.getCamName());
        return (new GenericRPC<CamInfoReply>(() -> camInfoRPC(request))).call(key);
    }

    private Object[] camInfoRPC(CamInfoRequest request) {
        CamInfoReply reply = stub.camInfo(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSMap());
        return new Object[]{reply, valueTS};
    }

    //report
    public ReportReply report(ReportRequest.Builder requestBuilder) throws NoServersException {
        ReportRequest request = requestBuilder
                .putAllPrevTS(ts.getMap())
                .build();
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.REPORT,
                SiloCacheKey.ObjectType.NONE,
                request.getCamName());
        return (new GenericRPC<ReportReply>(() -> reportRPC(request))).call(key);
    }

    private Object[] reportRPC(ReportRequest request) {
        ReportReply reply = stub.report(request);
        ts.update(new VectorTS(reply.getValueTSMap()));
        System.out.println(reply.getValueTSMap().toString()); //debug
        return new Object[]{reply};
    }

    //track
    public TrackReply track(TrackRequest.Builder requestBuilder) throws NoServersException {
        TrackRequest request = requestBuilder.putAllPrevTS(ts.getMap()).build();
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.SPOT,
                SiloCacheKey.toObjectType(request.getData().getType()),
                request.getData().getId());
        return (new GenericRPC<TrackReply>(() -> trackRPC(request))).call(key);
    }

    private Object[] trackRPC(TrackRequest request) {
        TrackReply reply;
        try {
            reply = stub.track(request);
        } catch (StatusRuntimeException e) {
            if (Status.NOT_FOUND.getCode().equals(e.getStatus().getCode())){
                reply = TrackReply.getDefaultInstance();
            } else
                throw e;
        }
        VectorTS valueTS = new VectorTS(reply.getValueTSMap());
        return new Object[]{reply, valueTS};
    }

    //trackMatch
    public TrackMatchReply trackMatch(TrackMatchRequest.Builder requestBuilder) throws NoServersException {
        TrackMatchRequest request = requestBuilder.putAllPrevTS(ts.getMap()).build();
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.SPOT,
                SiloCacheKey.toObjectType(request.getData().getType()),
                request.getData().getId());
        return (new GenericRPC<TrackMatchReply>(() -> trackMatchRPC(request))).call(key);
    }

    private Object[] trackMatchRPC (TrackMatchRequest request) {
        TrackMatchReply reply = stub.trackMatch(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSMap());
        return new Object[]{reply, valueTS};
    }

    //trace
    public TraceReply trace(TraceRequest.Builder requestBuilder) throws NoServersException {
        TraceRequest request = requestBuilder.putAllPrevTS(ts.getMap()).build();
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.TRAIL,
                SiloCacheKey.toObjectType(request.getData().getType()),
                request.getData().getId());
        return (new GenericRPC<TraceReply>(() -> traceRPC(request))).call(key);
    }

    private Object[] traceRPC (TraceRequest request) {
        TraceReply reply = stub.trace(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSMap());
        return new Object[]{reply, valueTS};
    }


    public CtrlPingReply ctrlPing(CtrlPingRequest.Builder requestBuilder) {
        CtrlPingRequest request = requestBuilder.putAllPrevTS(ts.getMap()).build();
        CtrlPingReply reply = stub.ctrlPing(request);
        ts.update(new VectorTS(reply.getValueTSMap()));
        return reply;
    }

    public CtrlClearReply ctrlClear(CtrlClearRequest.Builder requestBuilder) {
        CtrlClearRequest request = requestBuilder.putAllPrevTS(ts.getMap()).build();
        CtrlClearReply reply = stub.ctrlClear(request);
        ts.update(new VectorTS(reply.getValueTSMap()));
        return reply;
    }

    public CtrlInitReply ctrlInit(CtrlInitRequest.Builder requestBuilder) {
        CtrlInitRequest request = requestBuilder.putAllPrevTS(ts.getMap()).build();
        CtrlInitReply reply = stub.ctrlInit(request);
        ts.update(new VectorTS(reply.getValueTSMap()));
        return reply;
    }

    public void clear(){
        ts = new VectorTS();
        cache.clear();
    }
    
    @Override
    public void close(){
        channel.shutdown();
    }
}
