import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
	public static void main(String[] args) throws Exception {
		Scanner input = new Scanner(System.in);
		String msgIn = "";

		try (Socket socket = new Socket("cs380.codebank.xyz", 38002)) {

			Runnable inputThread = () -> {
				PrintStream out = null;
				String msgOut = "";
				
				System.out.println("Enter a username: ");
				msgOut = input.nextLine();
				System.out.println("Type .Disconnect to leave chat.");
				while (!msgOut.equals(".Disconnect")) {
					try { 
						out = new PrintStream(socket.getOutputStream());
					} catch (Exception e) {
						e.printStackTrace();
					}
					out.println(msgOut);       
					msgOut = input.nextLine(); 
				}	
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			
			Thread thread1 = new Thread(inputThread);
			thread1.start();
			
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				while ((msgIn = br.readLine()) != null) {
					System.out.println("Server> " + msgIn);
				}
			} catch (Exception e) {
				System.out.println("You have disconnected.");

			}
		}
	}
}
		
	

