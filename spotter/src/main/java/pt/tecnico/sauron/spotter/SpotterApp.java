package pt.tecnico.sauron.spotter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;


import java.util.Scanner;

public class SpotterApp {

	public static void main(String[] args) {
		System.out.println(SpotterApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		//check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final String target = host + ":" + port;

		final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		SiloServiceGrpc.SiloServiceBlockingStub stub = SiloServiceGrpc.newBlockingStub(channel);

		spotter(stub);

	}


	private static void spotter(SiloServiceGrpc.SiloServiceBlockingStub stub){

		String operation;
		String id;
		String inputType;
		Coordinates camCoords;
		ObjectType type;
		ObservationData responseData;
		ObservationResponse response;



		try (Scanner scanner = new Scanner(System.in).useDelimiter("\\s* \\s*")){

			while (true){
				operation = scanner.next();
				id = scanner.next();
				inputType = scanner.next();

				switch(inputType){
					case "person":
						type = ObjectType.PERSON;
						break;
					case "car":
						type = ObjectType.CAR;
						break;
					default:
						System.out.println("Wrong type " + inputType + ", try again...");
						continue;
				}

				switch (operation){
					case "spot":
						response = spot(stub, type, id);
						break;
					case "trail":
						response = trail(stub, type, id);
						break;
					/*case "ctrl_ping":
						System.out.println(ping(stub));
						continue;
					case "ctrl_clear":
						clear(stub);
						continue;*/
					case "ctrl_init":

					case "help":
						printHelp();
						continue;
					default:
						System.out.println("Wrong input " + operation + ", try again...");
						continue;
				}
				if (response.getDataCount() != 0) {
					for (int i = 0; i < response.getDataCount(); i++) {
						responseData = response.getData(i);
						camCoords = camInfo(stub, responseData.getCamName());
						System.out.println("" + responseData.getId() + ","
								+ responseData.getType() + ","
								+ responseData.getDate() + ","
								+ responseData.getCamName() + ","
								+ camCoords.getLatitude() + ","
								+ camCoords.getLongitude());
					}
				}
			}
		}
	}

	private static void printHelp(){
		System.out.println("Supported commands:\n" +
				"spot <type> <id> (returns observation(s) of <type> with <id> or partial <id>)\n" +
				"trail <type> <id> (returns path taken by <type> with <id>)\n" +
				"ctrl_ping (returns message with server state)" +
				"ctrl_clear (clears server state)" +
				"ctrl-init (allows definition of initial configuration parameters of server)");
	}

	/*private static String ping(SiloServiceGrpc.SiloServiceBlockingStub stub){
		EmptyMessage request = EmptyMessage.newBuilder().build();
		StringMessage response = stub.ctrlPing(request);
		return response.getString();
	}

	private static void clear(SiloServiceGrpc.SiloServiceBlockingStub stub){
		EmptyMessage request = EmptyMessage.newBuilder().build();
		stub.ctrlClear(request);
	}*/

	private static ObservationResponse spot(SiloServiceGrpc.SiloServiceBlockingStub stub, ObjectType type, String id){
		ObjectData request = ObjectData.newBuilder().
				setType(type).setId(id).build();

		if (id.indexOf('*') != -1){
		 	return stub.trackMatch(request);
		} else {
			return stub.track(request);
		}
	}

	private static ObservationResponse trail(SiloServiceGrpc.SiloServiceBlockingStub stub, ObjectType type, String id){
		ObjectData request = ObjectData.newBuilder().
				setType(type).setId(id).build();
		return stub.trace(request);
	}

	private static Coordinates camInfo(SiloServiceGrpc.SiloServiceBlockingStub stub, String camName){
		EyeName request = EyeName.newBuilder().setCamName(camName).build();
		return stub.camInfo(request);
	}
}
