import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Exercise2Client {
	
	public static byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		try (Socket socket = new Socket("cs380.codebank.xyz", 38101)) {
			
			System.out.println("Connected to server.");
			
			int first;
			int second;
			byte [] response = new byte[100];
			
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			
			for (int i = 0; i < 100; i++) {
				first = is.read();
				second = is.read();
				//System.out.printf("First byte: %x\n", first); 
				//System.out.printf("Second byte: %x\n", second); 
				first = first << 4;

				//System.out.println("First byte after Lshift: " + first);
				
				response[i] = (byte)(first^second);
				//System.out.println("After XOR no cast " + (first^second));
				//System.out.printf("XOR without cast: %d \n", response[i]);
				//System.out.printf("XOR with cast: %x \n\n", response[i]);

			}

			System.out.println("Received bytes:");
			for (int i = 1; i < response.length+1; i++){
				System.out.printf("%x", response[i-1]);
				if (i % 10 == 0)
					System.out.println(" ");
			}
			
			Checksum check = new CRC32();
			check.update(response, 0, response.length);
			long checksum = check.getValue();
			System.out.printf("\nGenerated CRC32: %x\n", checksum);
			
			byte[] crc = longToBytes(checksum);
				
			os.write(crc[4]);
			os.write(crc[5]);
			os.write(crc[6]);
			os.write(crc[7]); 
			
			int confirmation = is.read();
			System.out.println("Server> " + confirmation);
			if(confirmation == 1)
				System.out.println("Response good.");
			else
				System.out.println("Response bad.");
			
			is.close();
			os.close();
		}

	}

}
