import java.io.*;
import java.net.*;
import javax.net.SocketFactory;
import java.util.*;

public class Ipv4Client {
	/*
	private static void initiateChat(final byte[] buffer) throws IOException {
		String message;
		Scanner input = new Scanner(System.in);
		try (Socket socket = SocketFactory.getDefault().createSocket(
				"cs380.codebank.xyz", 38003)) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			OutputStream out = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(out);

			try {
				out.write(buffer);
				System.out.println("\nServer> " + br.readLine());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/

	private static void generateData(byte[] data){
		for (int i = 0; i < data.length; i++){
			data[i] = (byte) 666;
		}
	}
	/*
	private static short [] createPacket(int IHL, short[] data, int version,
			int TOS, int id, int flags, int TTL, int protocol, long destAddr) throws IOException {
		
		int totalLength = data.length + (IHL*2); // 20 bytes in protocol
		short [] packagedData = new short[totalLength];
		//totalLength *= 2;
		
		System.out.println("Data length: " + data.length);
		System.out.println("Total length: " + totalLength);
		System.out.println("packagedData: " + packagedData.length + "\n");

		packagedData[0] = (short)(packagedData[0] ^ (4 << 12)^(5 << 8));
		packagedData[1] = (short) (2);
		packagedData[2] = (short) id;
		packagedData[3] = (short) flags;
		packagedData[4] = (short) ((short)(TTL << 8)^(protocol & 0xFF));

		packagedData[5] = 0;
		packagedData[6] = 0;
		packagedData[7] = 0;

		packagedData[9] = 0;
		packagedData[8] = 0;

		
		int dataIndexCounter = 0;
		for (int i = 10; i < data.length + 10; ++i) {
		    packagedData[i] = data[dataIndexCounter]5;
		    dataIndexCounter++;
		}

		long chk = calculateChecksum(packagedData);
		packagedData[5] = (short) calculateChecksum(packagedData);

		return packagedData;
	
		
	}*/
	
	private static long calculateChecksum(short[] data) {
		long checksum = 0;
		int bitMask = 0x0000FFFF;
		for (int i = 0; i < 10; i++) {
			checksum += ((data[i]) & bitMask);
			if ((checksum & 0xFFFF0000) > 0) {
				checksum &= bitMask;
				checksum++;
			}
		}
		return ~(checksum & bitMask);
	}

	private static short[] convertToShort(byte[] array) {
		short[] shortArray = new short[(array.length + 1) / 2];
		for (int i = 0, j = 0; j < array.length - 1; i++, j += 2) {
			shortArray[i] |= (array[j] & 0xFF);
			shortArray[i] <<= 8;
			shortArray[i] |= (array[j + 1] & 0xFF);
		}
		return shortArray;
	}

	private static byte[] convertToBytes(short [] data)
	{
		int j = 0;
		byte[] result = new byte[(data.length << 1)];

		for (int i = 0; i < data.length; ++i) {
		    result[j + 1] |= (data[i] & 0xFF);
		    data[i] >>>= 8;
		    result[j] |= (data[i] & 0xFF);
		    j += 2;
		}
		return result;
	
	}
	
	public static void main(String[] args) throws IOException {

		byte version = 0b0100;
		byte IHL = 0b0101;
		byte TOS = 0b0;
		byte TTL = 0b00110010;
		byte protocol = 0b0110;
		short totalLength = 0b10100;
		byte id[] = new byte[2]; 
		id[0] = 0b0;
		id[1] = 0b0;
		byte flags = 0b010;
		byte fragOffset[] = new byte[2];
		fragOffset[0] = 0b0;
		fragOffset[1] = 0b0;

		int bitMask = 0x0000FFFF;
		byte sourceAddr[] = new byte[4];
		sourceAddr[0] = 0b0;
		sourceAddr[1] = 0b0;
		sourceAddr[2] = 0b0;
		sourceAddr[3] = 0b0;

		byte destAddr[] = new byte[4];
		destAddr[0] = 0b00110100; 
		destAddr[1] = 0b00100001; 
		destAddr[2] = (byte)(0b10000011 &0xFF); 
		destAddr[3] = 0b00010000; 

		
		Socket socket = new Socket("cs380.codebank.xyz", 38003);
		OutputStream out = socket.getOutputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
		Scanner kb = new Scanner(System.in);
		
		
		short dataSize = 2;
		int counter = 0;
		while (counter < 12) {
			System.out.println("Press enter to send packet " + (counter+1));
			kb.nextLine();
			byte[] buffer = new byte[20 + dataSize];
			byte data[] = new byte[dataSize];
			generateData(data);

			buffer[0] = version;
			buffer[0] <<= 4;
			buffer[0] |= IHL;
			buffer[1] = TOS;
			buffer[2] = (byte)(((totalLength + dataSize) >> 8) & 0xFF);
			buffer[3] = (byte)((totalLength + dataSize) & 0xFF);
			buffer[4] = id[1];
			buffer[5] = id[0];
			buffer[6] = flags;
			buffer[6] <<= 5;
			buffer[6] |= fragOffset[0];
			buffer[7] = fragOffset[1];
			buffer[8] = TTL;
			buffer[9] = protocol;

			for (int i = 12, j = 0; i < 16; i++, j++)
				buffer[i] = sourceAddr[j];

			for (int i = 16, j = 0; i < 20; i++, j++)
				buffer[i] = destAddr[j];

			for (int i = 20, j = 0; j < data.length; i++, j++)
				buffer[i] = data[j];
			
			short[] shortArray = convertToShort(buffer);
            short[] checksumArray = new short[1];
            checksumArray[0] = (short) calculateChecksum(shortArray);
            byte byteArray[] = convertToBytes(checksumArray);
			
            buffer[10] = byteArray[0]; 
            buffer[11] = byteArray[1];
				
            dataSize *= 2;
            counter++;
            //initiateChat(buffer);
            
            out.write(buffer);
            System.out.println(br.readLine());

		}
		
		//short[] dataToSend = createPacket(IHL, data, version, TOS, id, flags, TTL, protocol, destAddr);
	
		
		//byte [] bytesToSend= convertToBytes(dataToSend);
		
		/*for (int i = 0; i < dataToSend.length; i++){
			if (i == 0)
				System.out.printf("Version & Header:%8s\n", Integer.toBinaryString(dataToSend[i]).replace(' ', '0'));
			else if (i == 1)
				System.out.println("TOS:              " + Integer.toBinaryString(dataToSend[i]));
			else if (i == 2)
				System.out.println("Total length 1st: " + Integer.toBinaryString(dataToSend[i]));
			else if (i == 3)
				System.out.println("Total length 2nd: " + Integer.toBinaryString(dataToSend[i]));
			else if (i == 4)
				System.out.println("Identification:   " + Integer.toBinaryString(dataToSend[i]));
			else if (i == 5)
				System.out.println("Identification:   " + Integer.toBinaryString(dataToSend[i]));
			else if (i == 6)
				System.out.println("Flags & Offset:   " + Integer.toBinaryString(dataToSend[i]));
			else if (i == 7)
				System.out.println("TTL:              " + Integer.toBinaryString(dataToSend[i]));
			else if (i == 8)
				System.out.println("Protocol:         " + Integer.toBinaryString(dataToSend[i]));
			else if (i == 10)
				System.out.println("Checksum 1st:     " + Integer.toBinaryString(dataToSend[i]));
			else if (i == 11)
				System.out.println("Checksum 2nd:     " + Integer.toBinaryString(dataToSend[i]));
			else if (i >= 12 && i <= 19)
				System.out.println("Src/Dest addr:    " + Integer.toBinaryString(dataToSend[i]));
			else if (i >= 20 && i <= 21){
				System.out.println("Data:             " + Integer.toBinaryString(dataToSend[i]));
			}
		}
		*/
		/*for (int i = 0; i < dataToSend.length; i++){
			//System.out.println(Integer.toBinaryString(dataToSend[i]));
			System.out.println(dataToSend[i]);
		}*/

		//initiateChat(bytesToSend); 
	
	}



}
