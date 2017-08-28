package cs380;

import java.io.*;
import java.net.*;

import javax.net.SocketFactory;

import java.util.*;

public class UdpClient {
	
	private static void generateData(byte[] data){
		Random rand = new Random();
		rand.nextBytes(data);
		
	}
	
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
	
	private static long calculateChecksum(short[] data, short length, byte[] sourceAddr, byte [] destAddr) {
		long udpChecksum = 0;
		short[] udpHeader = new short[6 + data.length];
		
		udpHeader[0] = sourceAddr[0];
		udpHeader[0] <<= 8;
		udpHeader[0] |= sourceAddr[1];
		udpHeader[1] = sourceAddr[2];
		udpHeader[1] <<= 8;
		udpHeader[1] |= sourceAddr[3];
		
		udpHeader[2] = destAddr[0];
		udpHeader[2] <<= 8;
		udpHeader[2] |= destAddr[1];
		
		udpHeader[3] = destAddr[2];
		udpHeader[3] <<= 8;
		udpHeader[3] |= destAddr[3];
		
		udpHeader[4] = 0;
		udpHeader[4] <<= 8;
		udpHeader[4] |= 0x11;
		
		udpHeader[5] = length;
		
		for (int i = 6, j = 0; i < udpHeader.length; i++, j++){
			udpHeader[i] = data[j];
		}
		
		for (int i = 0; i < udpHeader.length; i++){
			udpChecksum += (udpHeader[i] & 0xFFFF);
			if ((udpChecksum & 0xFFFF0000) > 0){
				udpChecksum &= 0xFFF;
				udpChecksum++;
			}
		}

		return ~(udpChecksum & 0xFFFF);
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
		byte protocol = 0b00010001;  // update for UDP
		short totalLength = 0b00010100;
		byte id[] = new byte[2]; 
		id[0] = 0b0;
		id[1] = 0b0;
		byte flags = 0b010;
		byte fragOffset[] = new byte[2];
		fragOffset[0] = 0b0;
		fragOffset[1] = 0b0;

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
		
		Socket socket = new Socket("cs380.codebank.xyz", 38005);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		Scanner kb = new Scanner(System.in);
		
		byte handshakeData[] = new byte[4];
		handshakeData[0] = (byte) 0xDE;
		handshakeData[1] = (byte) 0xAD;
		handshakeData[2] = (byte) 0xBE;
		handshakeData[3] = (byte) 0xEF;
		
		byte handshakeBuffer [] = new byte[24];
		handshakeBuffer[0] = version;
		handshakeBuffer[0] <<= 4;
		handshakeBuffer[0] |= IHL;
		handshakeBuffer[1] = TOS;
		handshakeBuffer[2] = (byte)(((totalLength + 4) >> 8) & 0xFF);
		handshakeBuffer[3] = (byte)((totalLength + 4) & 0xFF);
		handshakeBuffer[4] = id[1];
		handshakeBuffer[5] = id[0];
		handshakeBuffer[6] = flags;
		handshakeBuffer[6] <<= 5;
		handshakeBuffer[6] |= fragOffset[0];
		handshakeBuffer[7] = fragOffset[1];
		handshakeBuffer[8] = TTL;
		handshakeBuffer[9] = protocol;
		
		for (int i = 12, j = 0; i < 16; i++, j++)
			handshakeBuffer[i] = sourceAddr[j];
		for (int i = 16, j = 0; i < 20; i++, j++)
			handshakeBuffer[i] = destAddr[j];
		for (int i = 20, j = 0; j < handshakeData.length; i++, j++)
			handshakeBuffer[i] = handshakeData[j];
		
		short[] hs_shortArray = convertToShort(handshakeBuffer);
        short[] hs_checksumArray = new short[1];
        hs_checksumArray[0] = (short) calculateChecksum(hs_shortArray);
        byte hs_byteArray[] = convertToBytes(hs_checksumArray);
        
        handshakeBuffer[10] = hs_byteArray[0]; 
        handshakeBuffer[11] = hs_byteArray[1];
		
		System.out.println("Handshaking step..");
		out.write(handshakeBuffer);
		System.out.print("0x");
        for (int i =0; i < 4; i++)
        	System.out.printf("%x", br.read());
        System.out.println("\nConnection established.\n");
        
        byte rawData[] = new byte[2];
        in.read(rawData);
        short port[] = convertToShort(rawData);
        short finalPort = port[0];

		short dataSize = 2;
		int counter = 0;
		totalLength = 28; // add 8 to totalLength field in ipv4 header for udp header
		while (counter < 12) {
			System.out.println("Press enter to send packet " + (counter+1));
			kb.nextLine();
			
			byte[] buffer = new byte[totalLength + dataSize];
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

			// ==========================================================
			
			byte[] udpPacket = new byte[8 + dataSize]; 
			// src port
	        udpPacket[0] = 0x0; 
	        udpPacket[1] = 0x0;
	        
	        // dest port
	        short temp = (short) finalPort;
	        udpPacket[2] = (byte) ((temp >>> 8) & 0xFF);
	        udpPacket[3] = (byte) (temp & 0xFF);

	        // length
	        short udpLength = (short) (8 + dataSize);
	        udpPacket[4] = (byte) ((temp >>> 8) & 0xFF); 
	        udpPacket[5] = (byte) (temp & 0xFF);
	        
	        // start checksum with 0
	        udpPacket[6] = 0; 
	        udpPacket[7] = 0;
	        
	        // udp packet data
	        for(int i=8, j=0; j < dataSize; i++, j++)
	            udpPacket[i] = data[j];

	        short[] shortArrayUDP = convertToShort(udpPacket);
	        short checksum = (short)calculateChecksum(shortArrayUDP, udpLength, sourceAddr, destAddr);
	        //short[] checkSumArrayUDP = new short[1];
	        //checkSumArrayUDP[0] = (short) calculateChecksum(shortArrayUDP, udpLength, sourceAddr, destAddr);

	        udpPacket[6] = (byte) ((checksum >>> 8) & 0xFF);
	        udpPacket[7] = (byte) (checksum & 0xFF);
	        
	        //udpPacket[6] = byteArrayUDP[0];
	        //udpPacket[7] = byteArrayUDP[1];
	        
	        for (int i = 20, j = 0; j < udpPacket.length; i++, j++){
	        	buffer[i] = udpPacket[j];
	        }

	        // ==========================================================
	        
			short[] shortArray = convertToShort(buffer);
            short[] checksumArray = new short[1];
            checksumArray[0] = (short) calculateChecksum(shortArray);
            byte [] byteArray = convertToBytes(checksumArray);
			
            buffer[10] = byteArray[0]; 
            buffer[11] = byteArray[1];
				     
            out.write(buffer);
            
            for (int i = 0; i < 4; i++){
            	System.out.printf("%x", br.read());
            }
            System.out.println();
            
            dataSize *= 2;
            counter++;
		}
	}
}
