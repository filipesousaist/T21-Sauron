package pt.tecnico.sauron.silo;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class SiloServerApp {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(SiloServerApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		int port = Integer.parseInt(args[0]);

		BindableService service = new SiloServerImpl();

		Server server = ServerBuilder.forPort(port).addService(service).build();

		server.start();

		server.awaitTermination();
	}


	
}
