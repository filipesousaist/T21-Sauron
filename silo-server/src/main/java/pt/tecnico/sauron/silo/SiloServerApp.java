package pt.tecnico.sauron.silo;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;
import java.util.Scanner;

public class SiloServerApp {
	public static void main(String[] args)
			throws IOException, InterruptedException {
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

			SiloServerImpl service = new SiloServerImpl(instance);
			Server server = ServerBuilder.forPort(Integer.parseInt(serverPort))
					 					 .addService((BindableService) service).build();

			// start gRPC server
			server.start();

			System.out.println("Server started");

			// Create new thread where we wait for the user input.
			new Thread(() -> {
				System.out.println("<Press enter to shutdown>");
				new Scanner(System.in).nextLine();

				service.shutdown();
				server.shutdown();
			}).start();

			// await termination
			server.awaitTermination();

			System.out.println("Server terminated");
		}
		catch (ZKNamingException e) {
			System.out.println(e.getMessage());
		}
		finally {
			if (zkNaming != null) {
				try {
					zkNaming.unbind(path, serverHost, serverPort);
				} catch (ZKNamingException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}
}
