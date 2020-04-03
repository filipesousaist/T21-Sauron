package pt.tecnico.sauron.spotter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SiloServiceGrpc;
import pt.tecnico.sauron.spotter.domain.exception.*;


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SpotterApp {
	private static final int NUM_ARGS = 2;

	public static void main(String[] args) {
		System.out.println(SpotterApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		try {
			//check arguments
			Object[] parsedArgs = parseArgs(args);

			SiloFrontend frontend = new SiloFrontend((String)parsedArgs[0], (int)parsedArgs[1]);

			handleInput(frontend);
		} catch(ArgCountException e) {
			System.out.println(e.getMessage());
		}
	}

	private static Object[] parseArgs(String[] args) throws ArgCountException{
		if (args.length != NUM_ARGS)
			throw new ArgCountException(args.length, NUM_ARGS);
		Object[] parsedArgs = new Object[NUM_ARGS];

		parsedArgs[0] = args[0];
		parsedArgs[1] = Integer.parseInt(args[1]);

		return parsedArgs;
	}

	private static void handleInput(SiloFrontend frontend) {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			try {
				String line = scanner.nextLine();
				if (!parseLine(line, frontend))
					break;
			}
			catch (InvalidLineException e){
				System.out.println(e.getMessage());
			}
		}
		scanner.close();
	}

	private static boolean parseLine(String line, SiloFrontend frontend)
			throws InvalidLineException{
		String[] lineArgs = line.split(" ");
		if (lineArgs.length > 3 || lineArgs.length < 1)
			throw new InvalidLineException(
					"Wrong number of arguments. " +
							"Expected at least 1 and at most 3, but " + lineArgs.length + " were given.");
		switch (lineArgs[0]){
			case "spot":
				parseSpot(lineArgs, frontend);
				break;
			case "trail":
				parseTrail(lineArgs, frontend);
				break;
			case "ping":
				ping(lineArgs[1], frontend);
				break;
			case "clear":
				clear(frontend);
				break;
			case "init":
				init(frontend);
				break;
			case "exit":
				return false;
			case "help":
				printHelp();
				break;
			default:
				throw new InvalidLineException("Unknown command: " + lineArgs[0]);
		}
		return true;
	}


	private static void parseSpot(String[] lineArgs, SiloFrontend frontend)
			throws InvalidLineException {
		if (lineArgs.length != 3){
			throw new InvalidLineException(
					"Wrong number of arguments. Expected 3, but " + lineArgs.length + "were given.");
		}
		ObjectType type = getObjectType(lineArgs);

		ObjectData objectData = ObjectData.newBuilder().setType(type).setId(lineArgs[2]).build();

		try {
			if (lineArgs[2].indexOf('*') != -1) {
				TrackMatchReply reply = frontend.trackMatch(TrackMatchRequest.newBuilder().setData(objectData).build());
				printResult(reply.getDataList(), frontend);
			} else {
				List<ObservationData> reply = new ArrayList<>();
				reply.add(frontend.track(TrackRequest.newBuilder().setData(objectData).build()).getData());
				printResult(reply, frontend);
			}
		} catch (StatusRuntimeException e) {
			Status.Code code = e.getStatus().getCode();
			if (Status.INVALID_ARGUMENT.getCode().equals(code) ||
				Status.NOT_FOUND.getCode().equals(code)) {
			}
		}
	}

	private static void parseTrail(String[] lineArgs, SiloFrontend frontend)
			throws InvalidLineException {
		if (lineArgs.length != 3){
			throw new InvalidLineException(
					"Wrong number of arguments. Expected 3, but " + lineArgs.length + "were given.");
		}
		ObjectType type = getObjectType(lineArgs);

		ObjectData objectData = ObjectData.newBuilder().setType(type).setId(lineArgs[2]).build();

		try {
			TraceReply reply = frontend.trace(TraceRequest.newBuilder().setData(objectData).build());

			printResult(reply.getDataList(), frontend);

		}
		catch (StatusRuntimeException e) {
			Status.Code code = e.getStatus().getCode();
			if (Status.INVALID_ARGUMENT.getCode().equals(code) ||
					Status.NOT_FOUND.getCode().equals(code)) {
			}
		}
	}

	private static ObjectType getObjectType(String[] lineArgs) throws InvalidLineException {
		ObjectType type;
		switch(lineArgs[1]){
			case "person":
				type = ObjectType.PERSON;
				break;
			case "car":
				type = ObjectType.CAR;
				break;
			default:
				throw new InvalidLineException("Unknown command: " + lineArgs[1]);
		}
		return type;
	}
	
	private static void printResult(List<ObservationData> reply, SiloFrontend frontend) {
		List<ObservationData> replyCopy = new ArrayList<>(reply);

		if (replyCopy.get(0).getType().equals(ObjectType.CAR))
			replyCopy.sort(Comparator.comparing(ObservationData::getId));

		else if (replyCopy.get(0).getType().equals(ObjectType.PERSON))
			replyCopy.sort(Comparator.comparing(id -> Long.parseLong(id.getId())));

		for (ObservationData responseData : replyCopy) {
			Coordinates camCoords = camInfo(responseData.getCamName(), frontend).getCoordinates();

			SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String timeString = timeFormat.format(responseData.getTimestamp().getSeconds() * 1000);

			System.out.println("" + responseData.getId() + ","
					+ responseData.getType() + ","
					+ timeString + ","
					+ responseData.getCamName() + ","
					+ camCoords.getLatitude() + ","
					+ camCoords.getLongitude());
		}

	}

	private static void ping(String message, SiloFrontend frontend){
		CtrlPingRequest request = CtrlPingRequest.newBuilder().setText(message).build();
		CtrlPingReply response = frontend.ctrlPing(request);
		System.out.println(response.getText());
	}

	private static void clear(SiloFrontend frontend){
		CtrlClearRequest request = CtrlClearRequest.newBuilder().build();
		CtrlClearReply response = frontend.ctrlClear(request);
		System.out.println(response.getText());
	}

	private static void init(SiloFrontend frontend){
		CtrlInitRequest request = CtrlInitRequest.newBuilder().build();
		CtrlInitReply response = frontend.ctrlInit(request);
		System.out.println(response.getText());
	}

	private static void printHelp(){
		System.out.println("Supported commands:\n" +
				"spot <type> <id> (returns observation(s) of <type> with <id> or partial <id>)\n" +
				"trail <type> <id> (returns path taken by <type> with <id>)\n" +
				"ping (returns message with server state)\n" +
				"clear (clears server state)\n" +
				"init (allows definition of initial configuration parameters of server)\n" +
				"exit (exits Spotter)");
	}

	private static CamInfoReply camInfo(String camName, SiloFrontend frontend){
		CamInfoRequest request = CamInfoRequest.newBuilder().setCamName(camName).build();
		return frontend.camInfo(request);
	}
}
