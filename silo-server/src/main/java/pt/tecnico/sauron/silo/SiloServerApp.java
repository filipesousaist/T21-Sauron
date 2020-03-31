package pt.tecnico.sauron.silo;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.sauron.silo.domain.Observation;
import pt.tecnico.sauron.silo.domain.PersonObservation;
import pt.tecnico.sauron.silo.grpc.Silo;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

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

		Date date = new Date();
		Timestamp  ts = new Timestamp(date.getTime());
		String s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(ts);
		System.out.println(s);

		server.awaitTermination();
	}


	
}
