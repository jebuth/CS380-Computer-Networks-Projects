import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public final class EchoClient {

public static void main(String[] args) throws Exception {
Scanner kb = new Scanner(System.in);
String message;
try (Socket socket = new Socket("localhost", 22222)) {
InputStream is = socket.getInputStream();
InputStreamReader isr = new InputStreamReader(is, "UTF-8");
BufferedReader br = new BufferedReader(isr);
System.out.println("Server> " + br.readLine());
//System.out.print("Client> ");
//message = kb.nextLine();
OutputStream os = socket.getOutputStream();
PrintStream out = new PrintStream(os);
//out.println(message);
InputStream is2 = socket.getInputStream();
InputStreamReader isr2 = new InputStreamReader(is2, "UTF-8");
BufferedReader br2 = new BufferedReader(isr2);

while (true) {
System.out.print("Client> ");
message = kb.nextLine();
out.println(message);
System.out.println("Server>" + br2.readLine());

}
}
}
}