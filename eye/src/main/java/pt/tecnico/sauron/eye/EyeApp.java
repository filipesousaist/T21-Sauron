package pt.tecnico.sauron.eye;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.eye.domain.Eye;
import pt.tecnico.sauron.eye.domain.exceptions.*;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.client.exception.NoServersException;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class EyeApp {
	private static final int NUM_FIXED_ARGS = 5;
	private static final int NUM_VAR_ARGS = 1;
	private static final int MAX_SIZE = 10;

	public static void main(String[] args)
		throws ZKNamingException {

		try {
			Object[] parsedArgs = parseArgs(args);

			try (SiloFrontend frontend =
				new SiloFrontend(
					(String) parsedArgs[0],
					(String) parsedArgs[1],
					(int) parsedArgs[5],
					MAX_SIZE
			)) {
				Eye eye = new Eye(
					(String) parsedArgs[2],
					(Double) parsedArgs[3],
					(Double) parsedArgs[4]);

				registerOnServer(frontend, eye);
				handleInput(frontend, eye);
			}
			catch (RegistrationException | NoServersException e) {
				System.out.println("Error registering in server: " + e.getMessage());
			}
		}
		catch (ArgCountException | NumberFormatException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("End");
	}

	private static Object[] parseArgs(String[] args) throws ArgCountException {
		if (args.length < NUM_FIXED_ARGS || args.length > NUM_FIXED_ARGS + NUM_VAR_ARGS)
			throw new ArgCountException(args.length, NUM_FIXED_ARGS, NUM_VAR_ARGS + NUM_FIXED_ARGS);
		Object[] parsedArgs = new Object[NUM_VAR_ARGS + NUM_FIXED_ARGS];

		// check if port is an integer
		int port = Integer.parseInt(args[1]);
		if (port < 0 || port >= 1 << 16)
			throw new NumberFormatException("Port must be between 0 and 65535");

		parsedArgs[0] = args[0];
		parsedArgs[1] = args[1];
		parsedArgs[2] = args[2];
		parsedArgs[3] = Double.parseDouble(args[3]);
		parsedArgs[4] = Double.parseDouble(args[4]);
		parsedArgs[5] = args.length > NUM_FIXED_ARGS ? Integer.parseInt(args[5]) : -1;

		return parsedArgs;
	}

	private static void registerOnServer(SiloFrontend frontend, Eye eye)
			throws RegistrationException, NoServersException {
		CamJoinRequest.Builder camJoinRequestBuilder = CamJoinRequest.newBuilder()
			.setCamName(eye.getName())
			.setCoordinates(eye.getCoordinates());
		try {
			frontend.camJoin(camJoinRequestBuilder);
			System.out.println("Registration successful. Proceeding...");
		}
		catch (StatusRuntimeException e) {
			Status.Code code = e.getStatus().getCode();
			if (Status.PERMISSION_DENIED.getCode().equals(code)) {
				throw new RegistrationException(
					"An Eye already exists with same name, but different coordinates.");
			}
			else if (Status.INVALID_ARGUMENT.getCode().equals(code)) {
				throw new RegistrationException(
					"Eye name does not match specified format.");
			}
			else if (Status.OUT_OF_RANGE.getCode().equals(code)) {
				throw new RegistrationException(
					"Eye coordinates do not match specified format.");
			}
		}
	}

	private static void handleInput(SiloFrontend frontend, Eye eye) {
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				scanLine(scanner, eye, frontend);
			}
			sendObservations(frontend, eye); // Send observations once System.in is closed
		}
		catch (UnregisteredEyeException|InvalidIdException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	private static void scanLine(Scanner scanner, Eye eye, SiloFrontend frontend)
			throws UnregisteredEyeException {
		try {
			String line = scanner.nextLine();
			if (line.length() == 0)
				sendObservations(frontend, eye);
			else if (!line.startsWith("#"))
				parseLine(eye, line);
		}
		catch (InvalidLineException e) {
			System.out.println("Invalid line: " + e.getMessage());
		}
		catch (InvalidIdException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	private static void parseLine(Eye eye, String line)
			throws InvalidLineException {
		String[] lineArgs = line.split(",");
		if (lineArgs.length != 2)
			throw new InvalidLineException(
				"Wrong number of arguments. Expected 2, but " + lineArgs.length + " were given.");
		switch (lineArgs[0]) {
			case "zzz":
				parseZZZ(lineArgs[1]);
				break;
			case "car":
				parseCar(eye, lineArgs[1]);
				break;
			case "person":
				parsePerson(eye, lineArgs[1]);
				break;
			default:
				throw new InvalidLineException("Unknown command: " + lineArgs[0]);
		}
	}

	private static void sendObservations(SiloFrontend frontend, Eye eye)
			throws InvalidIdException, UnregisteredEyeException {
		// Assemble report to send to server
		ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder().setCamName(eye.getName());
		while (eye.hasObservation())
			reportRequestBuilder.addData(eye.getNextObservation().toObjectData());
		eye.clearObservations();

		// Report observations to server and check for status
		try {
			frontend.report(reportRequestBuilder);
		}
		catch (StatusRuntimeException e) {
			Status.Code code = e.getStatus().getCode();
			if (Status.INVALID_ARGUMENT.getCode().equals(code)) {
				throw new InvalidIdException(e.getMessage());
			}
			else if (Status.UNAUTHENTICATED.getCode().equals(code)) {
				try {
					System.out.println("Lost connection. Attempting to reconnect...");
					registerOnServer(frontend, eye);
					frontend.report(reportRequestBuilder);
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
				}
			}
		} catch (NoServersException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void parseZZZ(String timeStr) throws InvalidLineException {
		try {
			long sleepTime = Long.parseLong(timeStr);
			if (sleepTime < 0)
				throw new InvalidLineException("Argument 2 must be non-negative.");
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		}
		catch (NumberFormatException e) {
			throw new InvalidLineException("Argument 2 is not a valid integer.");
		}
		catch (InterruptedException e) { // Execution was interrupted during sleep
			System.out.println(e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

	private static void parseCar(Eye eye, String carId) {
		eye.addObservedCar(carId);
	}

	private static void parsePerson(Eye eye, String personIdStr) throws InvalidLineException {
		try {
			eye.addObservedPerson(Long.parseLong(personIdStr));
		}
		catch (NumberFormatException e) {
			throw new InvalidLineException("Argument 2 is not a valid person identifier.");
		}
	}
}
