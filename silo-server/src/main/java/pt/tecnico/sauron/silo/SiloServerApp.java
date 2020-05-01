package pt.tecnico.sauron.silo;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.sauron.silo.domain.exception.ArgCountException;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;
import java.util.Scanner;

public class SiloServerApp {
	private static final int NUM_FIXED_ARGS = 5;
	private static final int NUM_VAR_ARGS = 1;

	private static final int GOSSIP_DELAY = 30000; // milliseconds

	public static void main(String[] args)
			throws IOException, InterruptedException {
		try {
			Object[] parsedArgs = parseArgs(args);
			String zkHost = (String) parsedArgs[0];
			String zkPort = (String) parsedArgs[1];
			int instance = (int) parsedArgs[2];
			String serverHost = (String) parsedArgs[3];
			String serverPort = (String) parsedArgs[4];
			int gossipDelay = (int) parsedArgs[5];

			String path = "/grpc/sauron/silo/" + instance;

			ZKNaming zkNaming = null;
			try {
				zkNaming = new ZKNaming(zkHost, zkPort);
				// publish
				zkNaming.rebind(path, serverHost, serverPort);

				SiloServerImpl service = new SiloServerImpl(instance, zkHost, zkPort, gossipDelay);
				Server server = ServerBuilder.forPort(Integer.parseInt(serverPort))
						.addService((BindableService) service).build();

				// start gRPC server
				server.start();

				System.out.println("Server started");

				// Create new thread where we wait for the user input.
				new Thread(() -> {
					System.out.println("<Press enter to shutdown>");
					new Scanner(System.in).nextLine();

					service.shutdown();
					server.shutdown();
				}).start();

				// await termination
				server.awaitTermination();

				System.out.println("Server terminated");
			} catch (ZKNamingException e) {
				System.out.println(e.getMessage());
			} finally {
				unbindZK(zkNaming, serverHost, serverPort, path);
			}
		} catch (ArgCountException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void unbindZK(ZKNaming zkNaming, String serverHost, String serverPort, String path) {
		if (zkNaming != null) {
			try {
				zkNaming.unbind(path, serverHost, serverPort);
			} catch (ZKNamingException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private static Object[] parseArgs(String[] args) throws ArgCountException {
		if (args.length < NUM_FIXED_ARGS || args.length > NUM_FIXED_ARGS + NUM_VAR_ARGS)
			throw new ArgCountException(args.length, NUM_FIXED_ARGS, NUM_VAR_ARGS + NUM_FIXED_ARGS);
		Object[] parsedArgs = new Object[NUM_VAR_ARGS + NUM_FIXED_ARGS];


		// check if zkPort is an integer
		int zkPort = Integer.parseInt(args[1]);
		if (zkPort < 0 || zkPort >= (2 << 16))
			throw new NumberFormatException("Zookeeper port must be between 0 and 65535");

		// check if serverPort is an integer
		int serverPort = Integer.parseInt(args[4]);
		if (serverPort < 0 || serverPort >= (2 << 16))
			throw new NumberFormatException("Server port must be between 0 and 65535");

		parsedArgs[0] = args[0];
		parsedArgs[1] = args[1];
		parsedArgs[2] = Integer.parseInt(args[2]);
		parsedArgs[3] = args[3];
		parsedArgs[4] = args[4];

		parsedArgs[5] = args.length > NUM_FIXED_ARGS ? Integer.parseInt(args[5]) : GOSSIP_DELAY;
		return parsedArgs;
	}
}
