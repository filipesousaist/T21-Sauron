package pt.tecnico.sauron.spotter;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.client.exception.NoServersException;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.spotter.domain.exception.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.text.SimpleDateFormat;
import java.util.*;

public class SpotterApp {
	private static final int NUM_FIXED_ARGS = 2;
	private static final int NUM_VAR_ARGS = 2;
	private static final int MAX_SIZE = 10;

	public static void main(String[] args) {
		try {
			//check arguments
			Object[] parsedArgs = parseArgs(args);

			SiloFrontend frontend = new SiloFrontend(
					(String) parsedArgs[0],
					(String) parsedArgs[1],
					(int) parsedArgs[2],
					(int) parsedArgs[3]
			);

			handleInput(frontend);
		} catch (ArgCountException | NumberFormatException | ZKNamingException | NoServersException e) {
			System.out.println(e.getMessage());
		}
	}

	private static Object[] parseArgs(String[] args) throws ArgCountException {
		if (args.length < NUM_FIXED_ARGS || args.length > NUM_FIXED_ARGS + NUM_VAR_ARGS)
			throw new ArgCountException(args.length, NUM_FIXED_ARGS, NUM_VAR_ARGS + NUM_FIXED_ARGS);
		Object[] parsedArgs = new Object[NUM_VAR_ARGS + NUM_FIXED_ARGS];

		// check if port is an integer
		int port = Integer.parseInt(args[1]);
		if (port < 0 || port >= (2 << 16))
			throw new NumberFormatException("Port must be between 0 and 65535");

		parsedArgs[0] = args[0];
		parsedArgs[1] = args[1];
		parsedArgs[2] = args.length > NUM_FIXED_ARGS ? Integer.parseInt(args[2]) : -1;
		parsedArgs[3] = args.length == NUM_FIXED_ARGS + NUM_VAR_ARGS ? Integer.parseInt(args[3]) : MAX_SIZE;

		return parsedArgs;
	}

	private static void handleInput(SiloFrontend frontend) {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			try {
				String line = scanner.nextLine();
				if (!parseLine(line, frontend))
					break;
			} catch (InvalidLineException e) {
				System.out.println(e.getMessage());
			}
		}
		scanner.close();
	}

	private static boolean parseLine(String line, SiloFrontend frontend)
			throws InvalidLineException {
		String[] lineArgs = line.split(" ");
		if (lineArgs.length > 3 || lineArgs.length < 1)
			throw new InvalidLineException(
					"Wrong number of arguments. " +
							"Expected at least 1 and at most 3, but " + lineArgs.length + " were given.");
		switch (lineArgs[0]) {
			case "spot":
				parseSpot(lineArgs, frontend);
				break;
			case "trail":
				parseTrail(lineArgs, frontend);
				break;
			case "ping":
				ping(lineArgs, frontend);
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
		if (lineArgs.length != 3) {
			throw new InvalidLineException(
					"Wrong number of arguments. Expected 3, but " + lineArgs.length + " were given.");
		}
		ObjectType type = getObjectType(lineArgs);

		ObjectData objectData = ObjectData.newBuilder().setType(type).setId(lineArgs[2]).build();

		try {
			if (lineArgs[2].indexOf('*') != -1) {
				TrackMatchReply reply = frontend.trackMatch(TrackMatchRequest.newBuilder().setData(objectData));
				printResult(reply.getDataList(), frontend);
			} else {
				List<ObservationData> reply = new ArrayList<>();
				reply.add(frontend.track(TrackRequest.newBuilder().setData(objectData)).getData());
				printResult(reply, frontend);
			}
		} catch (StatusRuntimeException e) {
			Status.Code code = e.getStatus().getCode();
			if (!(Status.INVALID_ARGUMENT.getCode().equals(code) ||
					Status.NOT_FOUND.getCode().equals(code)))
				System.out.println(e.getMessage());
		} catch (NoServersException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void parseTrail(String[] lineArgs, SiloFrontend frontend)
			throws InvalidLineException {
		if (lineArgs.length != 3) {
			throw new InvalidLineException(
					"Wrong number of arguments. Expected 3, but " + lineArgs.length + " were given.");
		}
		ObjectType type = getObjectType(lineArgs);

		ObjectData objectData = ObjectData.newBuilder().setType(type).setId(lineArgs[2]).build();

		try {
			TraceReply reply = frontend.trace(TraceRequest.newBuilder().setData(objectData));

			printResult(reply.getDataList(), frontend);

		} catch (StatusRuntimeException e) {
			Status.Code code = e.getStatus().getCode();
			if (!(Status.INVALID_ARGUMENT.getCode().equals(code) ||
					Status.NOT_FOUND.getCode().equals(code)))
				System.out.println(e.getMessage());
		} catch (NoServersException e) {
			System.out.println(e.getMessage());
		}
	}

	private static ObjectType getObjectType(String[] lineArgs) throws InvalidLineException {
		ObjectType type;
		switch (lineArgs[1]) {
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

		try {
			for (ObservationData responseData : replyCopy) {
				Coordinates camCoords = camInfo(responseData.getCamName(), frontend).getCoordinates();

				SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				String timeString = timeFormat.format(responseData.getTimestamp().getSeconds() * 1000);

				System.out.println(responseData.getId() + ","
						+ responseData.getType() + ","
						+ timeString + ","
						+ responseData.getCamName() + ","
						+ camCoords.getLatitude() + ","
						+ camCoords.getLongitude());
			}
		} catch (StatusRuntimeException | NoServersException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void ping(String[] lineArgs, SiloFrontend frontend)
			throws InvalidLineException {
		if (lineArgs.length != 2)
			throw new InvalidLineException(
					"Wrong number of arguments. Expected 2, but " + lineArgs.length + " were given.");

		CtrlPingRequest.Builder requestBuilder = CtrlPingRequest.newBuilder().setText(lineArgs[1]);
		CtrlPingReply response = frontend.ctrlPing(requestBuilder);
		System.out.println(response.getText());
	}

	private static void clear(SiloFrontend frontend) {
		CtrlClearRequest.Builder requestBuilder = CtrlClearRequest.newBuilder();
		CtrlClearReply response = frontend.ctrlClear(requestBuilder);
		System.out.println(response.getText());
	}

	private static void init(SiloFrontend frontend) {
		CtrlInitRequest.Builder requestBuilder = CtrlInitRequest.newBuilder();
		CtrlInitReply response = frontend.ctrlInit(requestBuilder);
		System.out.println(response.getText());
	}

	private static void printHelp() {
		System.out.println("Supported commands:\n" +
				"spot <type> <id> (returns observation(s) of <type> with <id> or partial <id>)\n" +
				"trail <type> <id> (returns path taken by <type> with <id>)\n" +
				"ping (sends control message to server, and server sends feedback)\n" +
				"clear (clears server state)\n" +
				"init (allows definition of initial configuration parameters of server)\n" +
				"exit (exits Spotter)");
	}

	private static CamInfoReply camInfo(String camName, SiloFrontend frontend) throws NoServersException {
		CamInfoRequest.Builder requestBuilder = CamInfoRequest.newBuilder().setCamName(camName);
		return frontend.camInfo(requestBuilder);
	}
}
