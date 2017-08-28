// Justin Buth
// CS 380
// 2/10/2016

import java.io.*;
import java.net.*;
import java.util.*;

public class Ipv6Client {


	public static void main(String[] args) throws IOException {

		byte version = 0b0110;   // 4 bits
		//byte traffic = 0b00000000; // 8 
		//byte flowlabel = 0b00000000; // 20 
		//byte payLoadLength =     ;// 16 bits
		byte nextHeader = 0b00010001; //
		byte hopLimit = 0b00010100;

		byte sourceAddr[] = new byte[4];
		sourceAddr[0] = (byte)(0b11000000 & 0xFF); // 192
		sourceAddr[1] = (byte)(0b10101000 & 0xFF); // 168
		sourceAddr[2] = 0b1; // 1
		sourceAddr[3] = 0b01000100; // 68

		byte destAddr[] = new byte[4];
		destAddr[0] = 0b00110100; 
		destAddr[1] = 0b00100001; 
		destAddr[2] = (byte)(0b10000011 & 0xFF); 
		destAddr[3] = 0b00010000; 

		Socket socket = new Socket("cs380.codebank.xyz", 38004);
		OutputStream out = socket.getOutputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		Scanner kb = new Scanner(System.in);
		//System.out.println(socket.getRemoteSocketAddress().toString());


		short dataSize = 2;
		int counter = 0;
		while (counter < 12) {
			System.out.print("Press enter to send packet " + (counter+1) + ":");
			kb.nextLine();
			byte[] buffer = new byte[40 + dataSize];
			byte data[] = new byte[dataSize];
			
			Random rand = new Random();
			rand.nextBytes(data);

			buffer[0] = (byte) (version << 4);
			buffer[0] = (byte)(buffer[0]^0b0);
			buffer[1] = 0b0;
			buffer[2] = 0b0;
			buffer[3] = 0b0;
			buffer[4] = (byte)(((dataSize) >> 8) & 0xFF);
			buffer[5] = (byte)((dataSize) & 0xFF);
			buffer[6] = nextHeader;
			buffer[7] = hopLimit;
			
			// source addr
			for (int i = 8; i < 18; i++)
				buffer[i] = 0x00;			// zero extension
			for (int i = 18; i < 20; i++) 	// prefix 2 bytes with 1's
				buffer[i] = (byte) 0xFF;
			for (int i = 20, j = 0; i < 24; i++, j++)
				buffer[i] = sourceAddr[j]; 	
			
			// dest addr
			for (int i = 24; i < 34; i++)
				buffer[i] = 0x00;
			for (int i = 34; i < 36; i++)
				buffer[i] = (byte) 0xFF;
			for (int i = 36, j = 0; i < 40; i++, j++)
				buffer[i] = destAddr[j];
			for (int i = 40, j = 0; j < data.length; i++, j++)
				buffer[i] = data[j];
				
            out.write(buffer);
            System.out.print("0x");
            for (int i =0; i < 4; i++)
            	System.out.printf("%x", br.read());
            System.out.println("\n");
            
            dataSize *= 2;
            counter++;

		}
	}
}
