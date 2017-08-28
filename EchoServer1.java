// Justin Buth
// CS 380
// 1/11/2016

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class EchoServer {

	public static void main(String[] args) throws Exception {
		try (ServerSocket serverSocket = new ServerSocket(22222)) {
			try (Socket socket = serverSocket.accept()) {
				Thread thread1 = new Thread(new Runnable() {
					public void run() {
						String address = socket.getInetAddress().getHostAddress();
						System.out.printf("Client connected: %s%n", address);
						OutputStream os = socket.getOutputStream();
						PrintStream out = new PrintStream(os);
						out.printf("Hi %s, thanks for connecting!%n", address);

						InputStream is = socket.getInputStream();
						InputStreamReader isr = new InputStreamReader(is,"UTF-8");
						BufferedReader br = new BufferedReader(isr);
					}
				});
				
				OutputStream os2 = socket.getOutputStream();
				PrintStream out2 = new PrintStream(os2);
				
				InputStream is2 = socket.getInputStream();
				InputStreamReader isr2 = new InputStreamReader(is2,"UTF-8");
				BufferedReader br2 = new BufferedReader(isr2);
				
				String msg;
				while ((msg = br2.readLine()) != null) {
					System.out.println("Client> " + msg);

					out2.printf(msg);

				}
			}
		}
	}
}
