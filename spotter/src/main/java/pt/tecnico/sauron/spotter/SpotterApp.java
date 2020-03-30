package pt.tecnico.sauron.spotter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
		ObjectType type;
		SpotterResponse response;



		try (Scanner scanner = new Scanner(System.in).useDelimiter("\\s* \\s*")){

			while (true){
				operation = scanner.next();
				id = scanner.next();
				inputType = scanner.next();
				type = getType(inputType);

				if (type == ObjectType.UNKNOWN) {
					System.out.println("Wrong type " + inputType + ", try again...");
				}

				switch (operation){
					case "spot":
						response = spot(stub, type, id);
						break;
					case "trail":
						response = trail(stub, type, id);
						break;
					case "ctrl_ping":
						System.out.println(ping(stub));
						continue;
					case "ctrl_clear":
						clear(stub);
						continue;
					case "ctrl_init":

					case "help":
						printHelp();
						continue;
					default:
						System.out.println("Wrong input " + operation + ", try again...");
						continue;
				}

				for (int i = 0; i < response.getIdCount(); i++){
					System.out.println("" + response.getId(i) + ","
							+ response.getType(i) + ","
							+ response.getDate(i) + ","
							+ response.getCameraName(i) + ","
							+ response.getCameraLat(i) + ","
							+ response.getCameraLong(i));
				}
			}
		}
	}

	private static ObjectType getType(String inputType){
		switch(inputType){
			case "person":
				return ObjectType.PERSON;
			case "car":
				return ObjectType.CAR;
			default:
				return ObjectType.UNKNOWN;
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

	private static String ping(SiloServiceGrpc.SiloServiceBlockingStub stub){
		EmptyMessage request = EmptyMessage.newBuilder().build();
		StringMessage response = stub.ctrlPing(request);
		return response.getString();
	}

	private static void clear(SiloServiceGrpc.SiloServiceBlockingStub stub){
		EmptyMessage request = EmptyMessage.newBuilder().build();
		stub.ctrlClear(request);
	}

	private static SpotterResponse spot(SiloServiceGrpc.SiloServiceBlockingStub stub, ObjectType type, String id){
		ObjectRequest request = ObjectRequest.newBuilder().
				setType(type).setId(id).build();
		return stub.spot(request);
	}

	private static SpotterResponse trail(SiloServiceGrpc.SiloServiceBlockingStub stub, ObjectType type, String id){
		ObjectRequest request = ObjectRequest.newBuilder().
				setType(type).setId(id).build();
		return stub.trail(request);
	}
}
