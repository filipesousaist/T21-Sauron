package pt.tecnico.sauron.silo;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;

public class SiloServerApp {
	
	public static void main(String[] args)
			throws IOException, InterruptedException, ZKNamingException {
		String zkHost = args[0];
		String zkPort = args[1];
		int instance = Integer.parseInt(args[2]);
		String serverHost = args[3];
		String serverPort = args[4];

		String path = "/grpc/sauron/silo/" + instance;

		ZKNaming zkNaming = null;
		try {
			zkNaming = new ZKNaming(zkHost, zkPort);
			// publish
			zkNaming.rebind(path, serverHost, serverPort);

			BindableService service = new SiloServerImpl();
			Server server = ServerBuilder.forPort(Integer.parseInt(serverPort))
					 					 .addService(service).build();

			// start gRPC server
			server.start();

			System.out.println("Started");

			// await termination
			server.awaitTermination();
		} finally {
			if (zkNaming != null)
				zkNaming.unbind(path, serverHost, serverPort);
		}
	}
}
