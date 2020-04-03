package pt.tecnico.sauron.silo;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SiloServerApp {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		int port = Integer.parseInt(args[0]);

		BindableService service = new SiloServerImpl();

		Server server = ServerBuilder.forPort(port).addService(service).build();

		server.start();

		server.awaitTermination();
	}
}
