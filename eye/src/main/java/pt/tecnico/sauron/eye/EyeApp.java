package pt.tecnico.sauron.eye;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class EyeApp {

	public static void main(String[] args) {
		System.out.println(EyeApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		handleInput();

		System.out.println("End");
	}

	public static void handleInput() {
		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				String line = scanner.nextLine();

				System.out.println(line);
			}
		}
		catch (NoSuchElementException e) {
			System.out.println(e.getMessage());
		}
	}
	
}
