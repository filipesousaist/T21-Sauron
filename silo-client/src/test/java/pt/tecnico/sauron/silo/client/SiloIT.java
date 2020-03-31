package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;

public class SiloIT extends BaseIT {
	
	// static members
	// TODO	
	static final String SERVER_STATUS = "Server has been cleared.";

	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){
		
	}

	@AfterAll
	public static void oneTimeTearDown() {
		
	}
	
	// initialization and clean-up for each test
	
	@BeforeEach
	public void setUp() {

	}
	
	@AfterEach
	public void tearDown() {
		
	}
		
	// tests 

	/*
	@Test
	public void testOkctrlPing() {
		Assertions.assertEquals(SERVER_STATUS, frontend.ctrlClear(buildEmptyMessage()).getClearStatus());
	}
	 */

}
