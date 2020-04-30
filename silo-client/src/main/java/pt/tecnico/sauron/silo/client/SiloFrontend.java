package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc.*;
import pt.tecnico.sauron.util.VectorTS;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.*;

public class SiloFrontend implements AutoCloseable {
    private static final String BASE_PATH = "/grpc/sauron/silo";
    private static final int MAX_SIZE = 10;
    private final ManagedChannel channel;
    private SiloServiceBlockingStub stub;
    private ZKNaming zkNaming;

    private int numServers;
    private VectorTS ts; // Vector timestamp
    private int currOpId;

    private SiloCache<SiloCacheKey, Message> cache;

    public SiloFrontend(String zkHost, String zkPort, int instance) throws ZKNamingException {
        zkNaming = new ZKNaming(zkHost, zkPort);

        numServers = zkNaming.listRecords(BASE_PATH).size();
        ts = new VectorTS(numServers);

        cache = new SiloCache<>(MAX_SIZE);
        // lookup
        String target = instance >= 0 ?
              zkNaming.lookup(BASE_PATH + "/" + instance).getURI()
            : getRandomTarget();

        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = SiloServiceGrpc.newBlockingStub(channel);

        currOpId = 0;
    }

    private String getRandomTarget() throws ZKNamingException {
        ZKRecord[] records = zkNaming.listRecords(BASE_PATH).toArray(ZKRecord[]::new);

        return records[(new Random()).nextInt(numServers)].getURI();
    }

    public CamJoinReply camJoin(CamJoinRequest.Builder requestBuilder) {
        CamJoinRequest request = requestBuilder
                .addAllPrevTS(ts)
                .setOpId(currOpId++)
                .build();
        CamJoinReply reply = stub.camJoin(request);
        ts.update(new VectorTS(reply.getValueTSList()));
        return reply;
    }

    public CamInfoReply camInfo(CamInfoRequest.Builder requestBuilder) {
        CamInfoRequest request = requestBuilder.addAllPrevTS(ts).build();
        CamInfoReply reply = stub.camInfo(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSList());

        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.CAM_INFO,
                SiloCacheKey.ObjectType.NONE,
                request.getCamName());

        if (ts.happensBeforeOrEquals(valueTS)) {
            cache.remove(key);
            cache.put(key, reply);
            ts.update(valueTS);
        } else if (cache.get(key) == null){
            throw new StatusRuntimeException(Status.NOT_FOUND);
        } else {
            reply = (CamInfoReply) cache.remove(key);
            cache.put(key, reply);
        }
        return reply;
    }

    public ReportReply report(ReportRequest.Builder requestBuilder) {
        ReportRequest request = requestBuilder
                .addAllPrevTS(ts)
                .setOpId(currOpId++)
                .build();

        ReportReply reply = stub.report(request);
        ts.update(new VectorTS(reply.getValueTSList()));
        System.out.println(reply.getValueTSList().toString());
        return reply;
    }

    public TrackReply track(TrackRequest.Builder requestBuilder) {
        TrackRequest request = requestBuilder.addAllPrevTS(ts).build();
        TrackReply reply = stub.track(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSList());

        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.SPOT,
                SiloCacheKey.toObjectType(request.getData().getType()),
                request.getData().getId());

        if (ts.happensBeforeOrEquals(valueTS)) {
            cache.remove(key);
            cache.put(key, reply);
            ts.update(valueTS);
        } else if (cache.get(key) == null){
            throw new StatusRuntimeException(Status.NOT_FOUND);
        } else {
            reply = (TrackReply) cache.remove(key);
            cache.put(key, reply);
        }

        return reply;
    }

    public TrackMatchReply trackMatch(TrackMatchRequest.Builder requestBuilder) {
        TrackMatchRequest request = requestBuilder.addAllPrevTS(ts).build();
        TrackMatchReply reply = stub.trackMatch(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSList());

        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.SPOT,
                SiloCacheKey.toObjectType(request.getData().getType()),
                request.getData().getId());

        if (ts.happensBeforeOrEquals(valueTS)) {
            cache.remove(key);
            cache.put(key, reply);
            ts.update(valueTS);
        } else if (cache.get(key) == null){
            throw new StatusRuntimeException(Status.NOT_FOUND);
        } else {
            reply = (TrackMatchReply) cache.remove(key);
            cache.put(key, reply);
        }
        return reply;
    }

    public TraceReply trace(TraceRequest.Builder requestBuilder) {
        TraceRequest request = requestBuilder.addAllPrevTS(ts).build();
        TraceReply reply = stub.trace(request);
        VectorTS valueTS = new VectorTS(reply.getValueTSList());

        SiloCacheKey key = new SiloCacheKey(SiloCacheKey.OperationType.TRAIL,
                SiloCacheKey.toObjectType(request.getData().getType()),
                request.getData().getId());

        if (ts.happensBeforeOrEquals(valueTS)) {
            cache.remove(key);
            cache.put(key, reply);
            ts.update(valueTS);
        } else if (cache.get(key) == null){
            throw new StatusRuntimeException(Status.NOT_FOUND);
        } else {
            reply = (TraceReply) cache.remove(key);
            cache.put(key, reply);
        }
        return reply;
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
    
    @Override
    public void close(){
        channel.shutdown();
    }
}
