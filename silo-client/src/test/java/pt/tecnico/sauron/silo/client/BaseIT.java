package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import pt.tecnico.sauron.silo.client.exception.NoServersException;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.IOException;
import java.util.Properties;


public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	static SiloFrontend frontend;
	
	@BeforeAll
	public static void oneTimeSetup () throws IOException {
		testProps = new Properties();
		
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(testProps);
		}
		catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		final String host = testProps.getProperty("zoo.host");
		final String port = testProps.getProperty("zoo.port");
		final int instance = Integer.parseInt((testProps.getProperty("server.instance")));
		final int cacheSize = Integer.parseInt((testProps.getProperty("cache.size")));
		try {
			frontend = new SiloFrontend(host, port, instance, cacheSize);
			frontend.ctrlClear(CtrlClearRequest.newBuilder());
		} catch (NoServersException | ZKNamingException e) {
			System.out.println(e.getMessage());
		}
	}
	
	@AfterAll
	public static void cleanup() {

	}


}
