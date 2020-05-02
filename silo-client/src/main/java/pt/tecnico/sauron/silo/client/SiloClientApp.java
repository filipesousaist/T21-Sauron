package pt.tecnico.sauron.silo.client;

import pt.tecnico.sauron.silo.client.exception.ArgCountException;
import pt.tecnico.sauron.silo.client.exception.NoServersException;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class SiloClientApp {
	private static final int NUM_FIXED_ARGS = 2;
	private static final int NUM_VAR_ARGS = 1;
	private static final int MAX_SIZE = 10;

	public static void main(String[] args)
		throws ZKNamingException {
		try {
			//check arguments
			Object[] parsedArgs = parseArgs(args);

			SiloFrontend frontend = new SiloFrontend(
				(String) parsedArgs[0],
				(String) parsedArgs[1],
				(int) parsedArgs[2],
				MAX_SIZE
			);

		}
		catch(ArgCountException | NumberFormatException | NoServersException e) {
			System.out.println(e.getMessage());
		}
/*
		EmptyMessage request = EmptyMessage.getDefaultInstance();

		StringMessage response = frontend.ctrlClear(request);
		System.out.println(response.getText());

		frontend.ctrlInit(request);
		Silo.ObjectData objectData = Silo.ObjectData.newBuilder()
				.setId("1234411111156")
				.setType(Silo.ObjectType.PERSON)
				.build();

		Silo.ObservationResponse observationResponse = frontend.trackMatch(objectData);

		List<ObservationData> objectDataList = observationResponse.getDataList();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");

		for (ObservationData od : objectDataList) {
			System.out.println("Id: " + od.getId());
			String s = format.format(od.getTimestamp().getSeconds()*1000);
			System.out.println(s);
		}*/
	}

	private static Object[] parseArgs(String[] args) throws ArgCountException {
		if (args.length < NUM_FIXED_ARGS || args.length > NUM_FIXED_ARGS + NUM_VAR_ARGS)
			throw new ArgCountException(args.length, NUM_FIXED_ARGS, NUM_VAR_ARGS + NUM_FIXED_ARGS);
		Object[] parsedArgs = new Object[NUM_VAR_ARGS + NUM_FIXED_ARGS];

		parsedArgs[0] = args[0];

		// check if port is an integer
		int port = Integer.parseInt(args[1]);
		if (port < 0 || port >= (2 << 16))
			throw new NumberFormatException("Port must be between 0 and 65535");

		parsedArgs[1] = args[1];

		parsedArgs[2] = args.length > NUM_FIXED_ARGS ? Integer.parseInt(args[2]) : -1;
		return parsedArgs;
	}
}
