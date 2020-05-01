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
    private static final int MAX_CONNECT_TRIES = 3;
    private static final int MAX_REQUEST_TRIES = 3;
    private ManagedChannel channel;
    private SiloServiceBlockingStub stub;
    private ZKNaming zkNaming;

    private int numServers;
    private VectorTS ts; // Vector timestamp
    private int instance;
    private int currOpId;

    private SiloCache<SiloCacheKey, Message> cache;

    public SiloFrontend(String zkHost, String zkPort, int instance)
            throws ZKNamingException, NoServersException {
        zkNaming = new ZKNaming(zkHost, zkPort);

        numServers = zkNaming.listRecords(BASE_PATH).size();
        if (numServers == 0)
            throw new NoServersException();
        ts = new VectorTS(numServers);
        this.instance = instance;

        cache = new SiloCache<>(MAX_SIZE);

        connect();

        currOpId = 0;
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

        public Reply call(boolean useCache) throws NoServersException {
            for (int i = 0; i < MAX_REQUEST_TRIES; i ++) {
                try {
                    Object[] object = rpc.call();
                    Reply reply = (Reply) object[0];
                    if (useCache) {
                        VectorTS valueTS = (VectorTS) object[1];
                        SiloCacheKey key = (SiloCacheKey) object[2];

                        if (ts.happensBeforeOrEquals(valueTS)) {
                            cache.remove(key);
                            cache.put(key, reply);
                            ts.update(valueTS);
                        } else if (cache.get(key) == null) {
                            throw new StatusRuntimeException(Status.NOT_FOUND);
                        } else {
                            reply = (Reply) cache.remove(key);
                            cache.put(key, reply);
                        }
                    }
                    return reply;

                } catch (StatusRuntimeException e) {
                    System.out.println("Frontend error: " + e.getMessage());
                    Status.Code code = e.getStatus().getCode();
                    if (!Status.UNAVAILABLE.getCode().equals(code)) {
                        throw e;
                    } else if (!connect()) {
                        throw new NoServersException();
                    }
                } catch (Exception ignored) {}
            }
            throw new StatusRuntimeException(Status.UNAVAILABLE);
        }
    }

    //camJoin
    public CamJoinReply camJoin(CamJoinRequest.Builder requestBuilder) throws NoServersException {
        CamJoinRequest request = requestBuilder
                .addAllPrevTS(ts)
                .setOpId(currOpId++)
                .build();
        return (new GenericRPC<CamJoinReply>(() -> camJoinRPC(request))).call(false);
    }

    private Object[] camJoinRPC(CamJoinRequest request) {
        CamJoinReply reply = stub.camJoin(request);
        ts.update(new VectorTS(reply.getValueTSList()));
        return new Object[]{reply};
    }

    //camInfo
    public CamInfoReply camInfo(CamInfoRequest.Builder requestBuilder) throws NoServersException {
        CamInfoRequest request = requestBuilder.addAllPrevTS(ts).build();
        return (new GenericRPC<CamInfoReply>(() -> camInfoRPC(request))).call(true);
    }

    private Object[] camInfoRPC(CamInfoRequest request) {
        CamInfoReply reply = stub.camInfo(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSList());
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.SPOT,
                SiloCacheKey.ObjectType.NONE,
                request.getCamName());
        return new Object[]{reply, valueTS, key};
    }

    //report
    public ReportReply report(ReportRequest.Builder requestBuilder) throws NoServersException {
        ReportRequest request = requestBuilder
                .addAllPrevTS(ts)
                .setOpId(currOpId++)
                .build();

        return (new GenericRPC<ReportReply>(() -> reportRPC(request))).call(false);
    }

    private Object[] reportRPC(ReportRequest request) {
        ReportReply reply = stub.report(request);
        ts.update(new VectorTS(reply.getValueTSList()));
        System.out.println(reply.getValueTSList().toString()); //debug
        return new Object[]{reply};
    }

    //track
    public TrackReply track(TrackRequest.Builder requestBuilder) throws NoServersException {
        TrackRequest request = requestBuilder.addAllPrevTS(ts).build();
        return (new GenericRPC<TrackReply>(() -> trackRPC(request))).call(true);
    }

    private Object[] trackRPC(TrackRequest request) {
        TrackReply reply = stub.track(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSList());
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.SPOT,
                SiloCacheKey.toObjectType(request.getData().getType()),
                request.getData().getId());
        return new Object[]{reply, valueTS, key};
    }

    //trackMatch
    public TrackMatchReply trackMatch(TrackMatchRequest.Builder requestBuilder) throws NoServersException {
        TrackMatchRequest request = requestBuilder.addAllPrevTS(ts).build();
        return (new GenericRPC<TrackMatchReply>(() -> trackMatchRPC(request))).call(true);
    }

    private Object[] trackMatchRPC (TrackMatchRequest request) {
        TrackMatchReply reply = stub.trackMatch(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSList());
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.SPOT,
                SiloCacheKey.toObjectType(request.getData().getType()),
                request.getData().getId());
        return new Object[]{reply, valueTS, key};
    }

    //trace
    public TraceReply trace(TraceRequest.Builder requestBuilder) throws NoServersException {
        TraceRequest request = requestBuilder.addAllPrevTS(ts).build();
        return (new GenericRPC<TraceReply>(() -> traceRPC(request))).call(true);
    }

    private Object[] traceRPC (TraceRequest request) {
        TraceReply reply = stub.trace(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSList());
        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.SPOT,
                SiloCacheKey.toObjectType(request.getData().getType()),
                request.getData().getId());
        return new Object[]{reply, valueTS, key};
    }


    public CtrlPingReply ctrlPing(CtrlPingRequest.Builder requestBuilder) {
        CtrlPingRequest request = requestBuilder.addAllPrevTS(ts).build();
        CtrlPingReply reply = stub.ctrlPing(request);
        ts.update(new VectorTS(reply.getValueTSList()));
        return reply;
    }

    public CtrlClearReply ctrlClear(CtrlClearRequest.Builder requestBuilder) {
        CtrlClearRequest request = requestBuilder.addAllPrevTS(ts).build();
        CtrlClearReply reply = stub.ctrlClear(request);
        ts.update(new VectorTS(reply.getValueTSList()));
        return reply;
    }

    public CtrlInitReply ctrlInit(CtrlInitRequest.Builder requestBuilder) {
        CtrlInitRequest request = requestBuilder.addAllPrevTS(ts).build();
        CtrlInitReply reply = stub.ctrlInit(request);
        ts.update(new VectorTS(reply.getValueTSList()));
        return reply;
    }

    public void clear(){
        ts = new VectorTS(0);
        cache.clear();
    }
    
    @Override
    public void close(){
        channel.shutdown();
    }
}
