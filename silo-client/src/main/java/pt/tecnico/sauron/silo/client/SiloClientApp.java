package pt.tecnico.sauron.silo.client;


import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.List;

public class SiloClientApp {
	
	public static void main(String[] args) {
		String host;
		int port;

		System.out.println(SiloClientApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
		host = args[0];
		port = Integer.parseInt(args[1]);

		SiloFrontend frontend = new SiloFrontend(host, port);

		EmptyMessage request = EmptyMessage.getDefaultInstance();

		StringMessage response = frontend.ctrlClear(request);
		System.out.println(response.getText());

		Silo.ObjectData objectData = Silo.ObjectData.newBuilder()
				.setId("123456")
				.setType(Silo.ObjectType.PERSON)
				.build();

		Silo.ObservationResponse observationResponse = frontend.track(objectData);

		List<ObservationData> objectDataList = observationResponse.getDataList();

		objectDataList.stream().forEach(System.out::println);
	}
	
}
