package pt.tecnico.sauron.eye;

import pt.tecnico.sauron.eye.domain.Eye;
import pt.tecnico.sauron.eye.domain.exceptions.*;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class EyeApp {
	private static final int NUM_ARGS = 5;

	public static void main(String[] args) {
		System.out.println(EyeApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
		try {
			Object[] parsedArgs = parseArgs(args);

			try (SiloFrontend frontend =
						 new SiloFrontend((String) parsedArgs[0], (int) parsedArgs[1])) {
				Eye eye = new Eye(
						(String) parsedArgs[2],
						(Double) parsedArgs[3],
						(Double) parsedArgs[4]);

				registerOnServer(frontend, eye);
				handleInput(frontend, eye);
			}
			catch (RegistrationException e) {
				System.out.println("Error registering in server: " + e.getMessage());
			}
		}
		catch (ArgCountException|NumberFormatException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("End");
	}

	private static Object[] parseArgs(String[] args) throws ArgCountException, NumberFormatException {
		if (args.length != NUM_ARGS)
			throw new ArgCountException(args.length, NUM_ARGS);
		Object[] parsedArgs = new Object[NUM_ARGS];

		parsedArgs[0] = args[0];
		parsedArgs[1] = Integer.parseInt(args[1]);
		parsedArgs[2] = args[2];
		parsedArgs[3] = Double.parseDouble(args[3]);
		parsedArgs[4] = Double.parseDouble(args[4]);

		return parsedArgs;
	}

	private static void registerOnServer(SiloFrontend frontend, Eye eye)
			throws RegistrationException {
		EyeJoinRequest eyeJoinRequest = EyeJoinRequest.newBuilder()
			.setCamName(eye.getName())
			.setCoordinates(eye.getCoordinates())
			.build();
		EyeJoinStatus eyeJoinStatus = frontend.camJoin(eyeJoinRequest).getStatus();
		switch (eyeJoinStatus) {
			case JOIN_OK:
				System.out.println("Registration successful. Proceeding..."); break;
			case DUPLICATE_JOIN:
				System.out.println("This Eye was already registered on server. Proceeding..."); break;
			case REPEATED_NAME:
				throw new RegistrationException(
					"An Eye already exists with same name, but different coordinates.");
			case INVALID_EYE_NAME:
				throw new RegistrationException(
					"Eye name does not match specified format.");
			case INVALID_COORDINATES:
				throw new RegistrationException(
					"Eye coordinates do not match specified format.");
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
			throws InvalidLineException, InvalidIdException {
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
		// Assemble observations to send to server
		EyeObservation.Builder eyeObservationBuilder = EyeObservation.newBuilder();
		while (eye.hasObservation())
			eyeObservationBuilder.addData(eye.getNextObservation().toObjectData());
		eye.clearObservations();

		// Report observations to server and check for status
		ReportStatus status = frontend.report(eyeObservationBuilder.build()).getStatus();
		switch (status) {
			case INVALID_ID:
				throw new InvalidIdException();
			case UNREGISTERED_EYE:
				throw new UnregisteredEyeException();
		}
	}

	private static void parseZZZ(String timeStr) throws InvalidLineException {
		try {
			long sleepTime = Long.parseLong(timeStr);
			if (sleepTime < 0)
				throw new InvalidLineException("Argument 2 must be non-negative.");
			TimeUnit.SECONDS.sleep(sleepTime);
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
