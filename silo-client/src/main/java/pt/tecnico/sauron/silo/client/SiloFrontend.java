package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class SiloFrontend implements AutoCloseable {
    private final ManagedChannel channel;
    private SiloServiceBlockingStub stub;

    public SiloFrontend(String zkHost, String zkPort, int instance) throws ZKNamingException {
        ZKNaming zkNaming = new ZKNaming(zkHost, zkPort);
        // lookup
        String target = zkNaming.lookup("/grpc/sauron/silo/" + instance).getURI();

        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = SiloServiceGrpc.newBlockingStub(channel);
    }

    public CamJoinReply camJoin(CamJoinRequest request) {
        return stub.camJoin(request);
    }

    public CamInfoReply camInfo(CamInfoRequest request) {
        return stub.camInfo(request);
    }

    public ReportReply report(ReportRequest request) {
        return stub.report(request);
    }

    public TrackReply track(TrackRequest request){
        return stub.track(request);
    }

    public TrackMatchReply trackMatch(TrackMatchRequest request){
        return stub.trackMatch(request);
    }

    public TraceReply trace(TraceRequest request){
        return stub.trace(request);
    }

    public CtrlPingReply ctrlPing(CtrlPingRequest request){ return stub.ctrlPing(request); }

    public CtrlClearReply ctrlClear(CtrlClearRequest request){ return stub.ctrlClear(request); }

    public CtrlInitReply ctrlInit(CtrlInitRequest request){ return stub.ctrlInit(request); }
    
    @Override
    public void close(){
        channel.shutdown();
    }
}
