import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class PhysLayerClient {

	public static String getNRZI(int[] buffer, float baseline){
		String fiveBits = "";
		String previousBit = "";
		
		int iterator = 0;
		
		for (int i = 0; i < buffer.length; i++){
			//if (i == 0){
			if (iterator == 0){
				if (buffer[0] < baseline){
					fiveBits += "0";
					previousBit = "0";
				}
				else {
					fiveBits += "1";
					previousBit = "1";
				}
			} else {
				// If bit is 0, signal stays the same.
				if (buffer[i] < baseline){
					fiveBits += previousBit;
					previousBit = previousBit;
				// If bit is 1, signal switches.
				} else {
					if (previousBit == "1"){
						fiveBits += "0";
						previousBit = "0";
					} else {
						fiveBits += "1";
						previousBit = "1";
					}
				}
				
			}
			//previousBit2 = previousBit;
			iterator++;
		}
		
		return fiveBits;
	}
	
	static String fourB5B(String fiveBits){
		String fourBits = "";
		
		switch (fiveBits) {
		case "11110":
			fourBits = "0000";
			break;
		case "01001":
			fourBits = "0001";
			break;
		case "10100":
			fourBits = "0010";
			break;
		case "10101":
			fourBits = "0011";
			break;
		case "01010":
			fourBits = "0100";
			break;
		case "01011":
			fourBits = "0101";
			break;
		case "01110":
			fourBits = "0110";
			break;
		case "01111":
			fourBits = "0111";
			break;
		case "10010":
			fourBits = "1000";
			break;
		case "10011":
			fourBits = "1001";
			break;
		case "10110":
			fourBits = "1010";
			break;
		case "10111":
			fourBits = "1011";
			break;
		case "11010":
			fourBits = "1100";
			break;
		case "11011":
			fourBits = "1101";
			break;
		case "11100":
			fourBits = "1110";
			break;
		case "11101":
			fourBits = "1111";
			break;
		default: fourBits = "1111"; 
			//System.out.println(fiveBits + " ***Not in table.***");
			break;
		}
		
		return fourBits;
	}
	
	public static byte constructByte(String first, String second){
		//System.out.println("here " + first);
		int firstFour = Integer.parseInt(first);
		int lastFour = Integer.parseInt(second);
		System.out.printf("First: %x ", firstFour);
		System.out.printf("Second: %x", lastFour);
		System.out.println();
		
		firstFour = firstFour << 4;
		return (byte)(firstFour^lastFour);
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		int byteCounter = 0;
		int[] message;
		byte[] binaryMessage;
		int[] buffer;
		String NRZI5bit = "";
		String fourBfiveB1;
		String fourBfiveB2;
		char scratch;
		
		try (Socket socket = new Socket("cs380.codebank.xyz", 38201)) {
			
			System.out.println("Connected to server.");
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			
			float total = 0;
			float baseline;
			
			for (int i = 0; i < 64; i++) {
				total += is.read();
			}
			
			baseline = total / 64;
			System.out.println("Baseline established from preamble: " + baseline);
			
			message = new int[32];
			buffer = new int[5];
			binaryMessage = new byte[32];
			
			String previousBit = "";
			String signal = "";
			
			//System.out.print("Read 64 bytes: ");
			for (int i = 0; i < message.length; i++) {
				
				// Read 5 times 
				System.out.print("\n");
				for (int j = 0; j < buffer.length; j++){
					buffer[j] = is.read();
					System.out.print(buffer[j] + " ");
				}

				//NRZI5bit = getNRZI(buffer, baseline);
				//String previousBit = "";
				if (i == 0)
					previousBit = "0";
				for (int j = 0; j < buffer.length; j++){
					if (j == 0){
						if (buffer[0] < baseline){
							//low
							NRZI5bit += "0";
							previousBit = "0";
							signal = "low";
								
						}
						else {
							// higher than baseline
							NRZI5bit += "1";
							previousBit = "1";
							signal = "high";
						} // HERE
					} else {
						if (buffer[j] < baseline){
							
							if (signal == "low"){
								NRZI5bit += "0";
								previousBit = "0";
							} else if (signal == "high"){
								NRZI5bit += "1";
								previousBit = "1";
								signal = "low";
							}
							
						} else {
							// higher than baseline
							if (signal == "high"){
								NRZI5bit += "0";
								previousBit = "0";
							} else if (signal == "low"){
								NRZI5bit += "1";
								previousBit ="1";
								signal = "high";
							}

						}			
					}
				}
				
				//scratch;
				
				System.out.println("\nNRZI: " + NRZI5bit);
				fourBfiveB1 = fourB5B(NRZI5bit);
				NRZI5bit = "";
				
				// Read another 5 times on same iteration
				// ===================================
				for (int j = 0; j < buffer.length; j++){
					buffer[j] = is.read();
					System.out.print(buffer[j] + " ");
				}
				for (int j = 0; j < buffer.length; j++){
						if (buffer[j] < baseline){
							if (signal == "low"){
								NRZI5bit += "0";
								previousBit = "0";
							} else if (signal == "high"){
								NRZI5bit += "1";
								previousBit = "1";
								signal = "low";
							}
							
						} else {
							// higher than baseline
							if (signal == "high"){
								NRZI5bit += "0";
								previousBit = "0";
							} else if (signal == "low"){
								NRZI5bit += "1";
								previousBit ="1";
								signal = "high";
							}

						}			
					//}
				}

				//NRZI5bit = getNRZI(buffer, baseline);
				//System.out.println("previous bit: " + previousBit);
				System.out.println("\nNRZI: " + NRZI5bit);
				fourBfiveB2 = fourB5B(NRZI5bit);
				NRZI5bit = "";
				// After reading 10 times -> 5 bits each -> converted to 8bits
				binaryMessage[byteCounter] = constructByte(fourBfiveB1, fourBfiveB2);
				byteCounter++;
				
			}
			os.write(binaryMessage);
			System.out.println("\nServer> " + is.read());
			
		}

	}

}
