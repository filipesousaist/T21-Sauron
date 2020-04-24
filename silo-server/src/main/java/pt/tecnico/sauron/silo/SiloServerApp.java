package pt.tecnico.sauron.silo;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SiloServerApp {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String zkhost = args[0];
		int zkport = Integer.parseInt(args[1]);
		int instance = Integer.parseInt(args[2]);
		String serverHost = args[3];
		int serverPort = Integer.parseInt(args[4]);

		ZKNaming zkNaming = null;
		BindableService service = new SiloServerImpl();

		Server server = ServerBuilder.forPort(serverPort).addService(service).build();

		server.start();

		server.awaitTermination();
	}
}
