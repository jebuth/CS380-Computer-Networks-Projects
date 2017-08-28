// Justin Buth
// CS 380
// 1/11/2016

import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public final class EchoClient {

	public static void main(String[] args) throws Exception {
		//String message;
		Scanner input = new Scanner(System.in);

		try (Socket socket = new Socket("localhost", 22222)) {
			Thread thread1 = new Thread(new Runnable() {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));

				public void run() {
					try {
						System.out.println("Server> " + br.readLine());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});
			thread1.start();

			PrintStream out = new PrintStream(socket.getOutputStream());
			String echo;
			
			String message;
			
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader br2 = new BufferedReader(isr);
			
			while (true) {

				System.out.print("Client> ");
				message = input.nextLine();
				out.println(message);
				
				/*while ((echo = br2.readLine()) != null) {
					System.out.println("Client> " + echo);


				}*/

			}

		}
	}
}