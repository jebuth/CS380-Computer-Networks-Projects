import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import javax.net.SocketFactory;

import java.util.*;

public class TcpClient {

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
		
		udpHeader[0] = 0b0;
		udpHeader[0] = 0b0;
		udpHeader[0] = 0b0;
		udpHeader[1] = 0b0;
		udpHeader[1] = 0b0;
		udpHeader[1] = 0b0;
		
		udpHeader[2] = destAddr[0];
		udpHeader[2] <<= 8;
		udpHeader[2] |= destAddr[1];
		
		udpHeader[3] = destAddr[2];
		udpHeader[3] <<= 8;
		udpHeader[3] |= destAddr[3];
		
		udpHeader[4] = 0;
		udpHeader[4] <<= 8;
		udpHeader[4] |= 0x06;
		
		udpHeader[5] = length; 
		
		for (int i = 6, j = 0; i < data.length+6; i++, j++){
			udpHeader[i] = data[j];
		}
		
		for (int i = 0; i < udpHeader.length; i++){
			udpChecksum += (udpHeader[i] & 0xFFFF);
			if ((udpChecksum & 0xFFFF0000) > 0){
				udpChecksum &= 0xFFFF;
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
		byte protocol = 0b0110;
		short totalLength = 40; // 0b10100
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
		
		Socket socket = new Socket("cs380.codebank.xyz", 38006);
		OutputStream out = socket.getOutputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
		Scanner kb = new Scanner(System.in);
		
		int dataSize0 = 0;
		byte[] handshakeTCPHeader = new byte[20];
		byte[] handshakeIPV4 = new byte[20]; 
		
		// randomize sequence number
		Random rand = new Random();
		int seqNum = rand.nextInt();
		
		//System.out.printf("%x\n", (byte)seqNum);
		
		// IPv4 packet ===========================
		handshakeIPV4[0] = version;
		handshakeIPV4[0] <<= 4;
		handshakeIPV4[0] |= IHL;
		handshakeIPV4[1] = TOS;
		handshakeIPV4[2] = (byte)(((totalLength + dataSize0) >> 8) & 0xFF);
		handshakeIPV4[3] = (byte)((totalLength + dataSize0) & 0xFF);
		handshakeIPV4[4] = id[1];
		handshakeIPV4[5] = id[0];
		handshakeIPV4[6] = flags;
		handshakeIPV4[6] <<= 5;
		handshakeIPV4[6] |= fragOffset[0];
		handshakeIPV4[7] = fragOffset[1];
		handshakeIPV4[8] = TTL;
		handshakeIPV4[9] = protocol;
		
		handshakeIPV4[10] = 0b0;
		handshakeIPV4[11] = 0b0;

		for (int i = 12, j = 0; i < 16; i++, j++)
			handshakeIPV4[i] = sourceAddr[j];
		for (int i = 16, j = 0; i < 20; i++, j++)
			handshakeIPV4[i] = destAddr[j];
		// =========================================
		
		// TCP header ==============================
		handshakeTCPHeader[0] = 0b0; // src port
		handshakeTCPHeader[1] = 0b0; //
		handshakeTCPHeader[2] = 0b0; // dest port
		handshakeTCPHeader[3] = 0b0; // 
		handshakeTCPHeader[4] = (byte)(seqNum >> 24 & 0xFF);
		handshakeTCPHeader[5] = (byte)(seqNum >> 16 & 0xFF);
		handshakeTCPHeader[6] = (byte)(seqNum >> 8 & 0xFF);
		handshakeTCPHeader[7] = (byte)(seqNum & 0xFF);
		handshakeTCPHeader[8] = 0b0; // acknowledgement number
		handshakeTCPHeader[9] = 0b0; //
		handshakeTCPHeader[10] = 0b0; //
		handshakeTCPHeader[11] = 0b0; //
		handshakeTCPHeader[12] = (byte) (5 << 4); // header length(offset) // reserved
		handshakeTCPHeader[13] = 0b00000010; // flags / SYN set to 1
		handshakeTCPHeader[14] = 0b0; // window size
		handshakeTCPHeader[15] = 0b0; //
		handshakeTCPHeader[16] = 0b0; // checksum
		handshakeTCPHeader[17] = 0b0; //
		handshakeTCPHeader[18] = 0b0; // urgent pointer
		handshakeTCPHeader[19] = 0b0; //
		
		// =========================================
		
		short[] tcpHeader = convertToShort(handshakeTCPHeader);
        short tcpChecksum = (short) calculateChecksum(tcpHeader, (short) 20, sourceAddr, destAddr);
		
        handshakeTCPHeader[16] = (byte)((tcpChecksum >>> 8) & 0xFF); 
        handshakeTCPHeader[17] = (byte)(tcpChecksum & 0xFF);
		
		short[] IPV4header = convertToShort(handshakeIPV4);
        short[] IPV4checksum = new short[1];
        IPV4checksum[0] = (short) calculateChecksum(IPV4header);
        byte byteArrayOuter[] = convertToBytes(IPV4checksum);
        
        handshakeIPV4[10] = byteArrayOuter[0];
        handshakeIPV4[11] = byteArrayOuter[1];
        
        // IPv4 packet with TCP header ==============
        byte[] outputBuffer = new byte[40];
        for (int i = 0, j = 0; j < handshakeIPV4.length; i++, j++){
        	outputBuffer[i] = handshakeIPV4[j];
        }
        for (int i = 20, j = 0; j < handshakeTCPHeader.length; i++, j++){
        	outputBuffer[i] = handshakeTCPHeader[j];
        }
		// =========================================
        
		out.write(outputBuffer);
		
		System.out.print("Response to SYN: ");
		for (int i = 0; i < 4; i++){
        	System.out.printf("%x", br.read());
        }
		// Reading in the TCP header =========================
		byte[] incomingTCP = new byte[20];
        for (int i = 0; i < 20; i++){
        	incomingTCP[i] = (byte) br.read();
        }
        System.out.println();
     
		seqNum += 1;
		ByteBuffer bb = ByteBuffer.wrap(incomingTCP);
		int seqFromServer = bb.getInt(4);
		seqFromServer++;

		// Package the ack

		// TCP header ==============================
		handshakeTCPHeader[0] = 0b0; // src port
		handshakeTCPHeader[1] = 0b0; //
		handshakeTCPHeader[2] = 0b0; // dest port
		handshakeTCPHeader[3] = 0b0; //
		handshakeTCPHeader[4] = (byte) (seqNum >> 24 & 0xFF);
		handshakeTCPHeader[5] = (byte) (seqNum >> 16 & 0xFF);
		handshakeTCPHeader[6] = (byte) (seqNum >> 8 & 0xFF);
		handshakeTCPHeader[7] = (byte) (seqNum & 0xFF);
		handshakeTCPHeader[8] = (byte) (seqFromServer >> 24 & 0xFF); // acknowledgement
																		// number
		handshakeTCPHeader[9] = (byte) (seqFromServer >> 16 & 0xFF); //
		handshakeTCPHeader[10] = (byte) (seqFromServer >> 8 & 0xFF); //
		handshakeTCPHeader[11] = (byte) (seqFromServer & 0xFF); //
		handshakeTCPHeader[12] = (byte) (5 << 4); // header length(offset) //
													// reserved
		handshakeTCPHeader[13] = 0b00010100; // flags / ACK set to 1
		//handshakeTCPHeader[13] = 0b00010000; // flags / ACK set to 1
		handshakeTCPHeader[14] = 0b0; // window size
		handshakeTCPHeader[15] = 0b0; //
		handshakeTCPHeader[16] = 0b0; // checksum
		handshakeTCPHeader[17] = 0b0; //
		handshakeTCPHeader[18] = 0b0; // urgent pointer
		handshakeTCPHeader[19] = 0b0; //

		// =========================================

		short[] tcpHeader2 = convertToShort(handshakeTCPHeader);
		short tcpChecksum2 = (short) calculateChecksum(tcpHeader2, (short) 20,
				sourceAddr, destAddr);

		handshakeTCPHeader[16] = (byte) ((tcpChecksum2 >>> 8) & 0xFF);
		handshakeTCPHeader[17] = (byte) (tcpChecksum2 & 0xFF);

		short[] IPV4header2 = convertToShort(handshakeIPV4);
		//short[] IPV4checksum2 = new short[1];
		IPV4checksum[0] = (short) calculateChecksum(IPV4header2);
		//byte byteArrayOuter2[] = convertToBytes(IPV4checksum2);

		handshakeIPV4[10] = byteArrayOuter[0];
		handshakeIPV4[11] = byteArrayOuter[1];

		// IPv4 packet with TCP header ==============
		// byte[] outputBuffer = new byte[40];
		for (int i = 0, j = 0; j < handshakeIPV4.length; i++, j++) {
			outputBuffer[i] = handshakeIPV4[j];
		}
		for (int i = 20, j = 0; j < handshakeTCPHeader.length; i++, j++) {
			outputBuffer[i] = handshakeTCPHeader[j];
		}
		// =========================================

		out.write(outputBuffer);

		System.out.print("Reply to ack: ");
		for (int i = 0; i < 4; i++) {
			System.out.printf("%x", br.read());
		}
		System.out.println();


		// Start
		short dataSize = 2;
		int counter = 0;
		while (counter < 12) {
			System.out.print("Press enter to send packet " + (counter+1));
			kb.nextLine();
			byte[] ipv4Packet = new byte[20];
			byte data[] = new byte[dataSize];
			generateData(data);
			
			seqNum += (dataSize/2);
			
			// IPv4 packet ===========================
			ipv4Packet[0] = version;
			ipv4Packet[0] <<= 4;
			ipv4Packet[0] |= IHL;
			ipv4Packet[1] = TOS;
			ipv4Packet[2] = (byte)(((totalLength + dataSize) >> 8) & 0xFF);
			ipv4Packet[3] = (byte)((totalLength + dataSize) & 0xFF);
			ipv4Packet[4] = id[1];
			ipv4Packet[5] = id[0];
			ipv4Packet[6] = flags;
			ipv4Packet[6] <<= 5;
			ipv4Packet[6] |= fragOffset[0];
			ipv4Packet[7] = fragOffset[1];
			ipv4Packet[8] = TTL;
			ipv4Packet[9] = protocol;
			ipv4Packet[10] = 0b0;
			ipv4Packet[11] = 0b0;

			for (int i = 12, j = 0; i < 16; i++, j++)
				ipv4Packet[i] = sourceAddr[j];
			for (int i = 16, j = 0; i < 20; i++, j++)
				ipv4Packet[i] = destAddr[j];
			
			
			// =========================================
			byte[] tcpBuffer = new byte[20 + dataSize];
			// TCP header ==============================
			tcpBuffer[0] = 0b0; // src port
			tcpBuffer[1] = 0b0; //
			tcpBuffer[2] = 0b0; // dest port
			tcpBuffer[3] = 0b0; //
			tcpBuffer[4] = (byte) (seqNum >> 24 & 0xFF);
			tcpBuffer[5] = (byte) (seqNum >> 16 & 0xFF);
			tcpBuffer[6] = (byte) (seqNum >> 8 & 0xFF);
			tcpBuffer[7] = (byte) (seqNum & 0xFF);
			tcpBuffer[8] = (byte) (seqFromServer >> 24 & 0xFF); // acknowledgement
																			// number
			tcpBuffer[9] = (byte) (seqFromServer >> 16 & 0xFF); //
			tcpBuffer[10] = (byte) (seqFromServer >> 8 & 0xFF); //
			tcpBuffer[11] = (byte) (seqFromServer & 0xFF); //
			tcpBuffer[12] = (byte) (5 << 4); // header length(offset) //
														// reserved
			tcpBuffer[13] = 0b00010000; // flags / ACK set to 1
			tcpBuffer[14] = 0b0; // window size
			tcpBuffer[15] = 0b0; //
			tcpBuffer[16] = 0b0; // checksum
			tcpBuffer[17] = 0b0; //
			tcpBuffer[18] = 0b0; // urgent pointer
			tcpBuffer[19] = 0b0; //
			
	        // append payload
	        for (int i = 20, j = 0; j < data.length; i++, j++){
	        	tcpBuffer[i] = data[j];
	        }
			
			short[] tcp = convertToShort(tcpBuffer);
	        short tcpChecksums = (short) calculateChecksum(tcp, (short) (20+dataSize), sourceAddr, destAddr);
			
	        tcpBuffer[16] = (byte)((tcpChecksums >>> 8) & 0xFF); 
	        tcpBuffer[17] = (byte)(tcpChecksums & 0xFF);
			
			short[] ipHeader = convertToShort(ipv4Packet);
	        short[] ipChecksum = new short[1];
	        ipChecksum[0] = (short) calculateChecksum(ipHeader); // wtf
	        byte byteChecksum[] = convertToBytes(ipChecksum);
	        
	        ipv4Packet[10] = byteChecksum[0];
	        ipv4Packet[11] = byteChecksum[1];
	        
	        // IPv4 packet with TCP header ==============
	        byte[] outputBuffers = new byte[40+dataSize];
	        for (int i = 0, j = 0; j < ipv4Packet.length; i++, j++){
	        	outputBuffers[i] = ipv4Packet[j];
	        }
	        for (int i = 20, j = 0; j < tcpBuffer.length; i++, j++){
	        	outputBuffers[i] = tcpBuffer[j];
	        }
	        
				
            dataSize *= 2;
			counter++;

			out.write(outputBuffers);

			System.out.print("Reply to packet " + counter + " : ");
			for (int i = 0; i < 4; i++) {
				System.out.printf("%x", br.read());
			}
			System.out.println("\n");

		}

		// IPv4 packet ===========================
		handshakeIPV4[0] = version;
		handshakeIPV4[0] <<= 4;
		handshakeIPV4[0] |= IHL;
		handshakeIPV4[1] = TOS;
		handshakeIPV4[2] = (byte) (((totalLength + dataSize0) >> 8) & 0xFF);
		handshakeIPV4[3] = (byte) ((totalLength + dataSize0) & 0xFF);
		handshakeIPV4[4] = id[1];
		handshakeIPV4[5] = id[0];
		handshakeIPV4[6] = flags;
		handshakeIPV4[6] <<= 5;
		handshakeIPV4[6] |= fragOffset[0];
		handshakeIPV4[7] = fragOffset[1];
		handshakeIPV4[8] = TTL;
		handshakeIPV4[9] = protocol;

		handshakeIPV4[10] = 0b0;
		handshakeIPV4[11] = 0b0;

		for (int i = 12, j = 0; i < 16; i++, j++)
			handshakeIPV4[i] = sourceAddr[j];
		for (int i = 16, j = 0; i < 20; i++, j++)
			handshakeIPV4[i] = destAddr[j];
		// =========================================

		// TCP header ==============================
		handshakeTCPHeader[0] = 0b0; // src port
		handshakeTCPHeader[1] = 0b0; //
		handshakeTCPHeader[2] = 0b0; // dest port
		handshakeTCPHeader[3] = 0b0; //
		handshakeTCPHeader[4] = (byte) (seqNum >> 24 & 0xFF);
		handshakeTCPHeader[5] = (byte) (seqNum >> 16 & 0xFF);
		handshakeTCPHeader[6] = (byte) (seqNum >> 8 & 0xFF);
		handshakeTCPHeader[7] = (byte) (seqNum & 0xFF);
		handshakeTCPHeader[8] = 0b0; // acknowledgement number
		handshakeTCPHeader[9] = 0b0; //
		handshakeTCPHeader[10] = 0b0; //
		handshakeTCPHeader[11] = 0b0; //
		handshakeTCPHeader[12] = (byte) (5 << 4); // header length(offset) //
													// reserved
		handshakeTCPHeader[13] = (byte) 0b00000001; // flags / FIN set to 1
		handshakeTCPHeader[14] = (byte) 0b0; // window size
		handshakeTCPHeader[15] = 0b0; //
		handshakeTCPHeader[16] = 0b0; // checksum
		handshakeTCPHeader[17] = 0b0; //
		handshakeTCPHeader[18] = 0b0; // urgent pointer
		handshakeTCPHeader[19] = 0b0; //

		// =========================================

		short[] tcpTearDown = convertToShort(handshakeTCPHeader);
		short tcpChecksumTearDown = (short) calculateChecksum(tcpTearDown, (short) 20,
				sourceAddr, destAddr);

		handshakeTCPHeader[16] = (byte) ((tcpChecksumTearDown >>> 8) & 0xFF);
		handshakeTCPHeader[17] = (byte) (tcpChecksumTearDown & 0xFF);

		short[] IPV4header1 = convertToShort(handshakeIPV4);
		short[] IPV4checksum1 = new short[1];
		IPV4checksum[0] = (short) calculateChecksum(IPV4header1);
		byte byteArrayOuter1[] = convertToBytes(IPV4checksum1);

		handshakeIPV4[10] = byteArrayOuter[0];
		handshakeIPV4[11] = byteArrayOuter[1];

		// IPv4 packet with TCP header ==============
		byte[] outputBuffer1 = new byte[40];
		for (int i = 0, j = 0; j < handshakeIPV4.length; i++, j++) {
			outputBuffer1[i] = handshakeIPV4[j];
		}
		for (int i = 20, j = 0; j < handshakeTCPHeader.length; i++, j++) {
			outputBuffer1[i] = handshakeTCPHeader[j];
		}
		// =========================================

		out.write(outputBuffer1);
		
		
		System.out.print("Reply to FIN: ");
		for (int i = 0; i < 4; i++) {
			System.out.printf("%x", br.read());
		}
		System.out.println();
		
		// read in TCP header with ACK flag set
		byte [] tcpwithACK = new byte[20];
		for (int i = 0; i < 20; i++) {
			tcpwithACK[i] = (byte) br.read();
		}
		
		// read in TCP header with FIN flag set
		byte [] tcpwithFIN = new byte[20];
		for (int i = 0; i < 20; i++) {
			tcpwithFIN[i] = (byte) br.read();
		}
		
		handshakeIPV4[0] = version;
		handshakeIPV4[0] <<= 4;
		handshakeIPV4[0] |= IHL;
		handshakeIPV4[1] = TOS;
		handshakeIPV4[2] = (byte) (((totalLength + dataSize0) >> 8) & 0xFF);
		handshakeIPV4[3] = (byte) ((totalLength + dataSize0) & 0xFF);
		handshakeIPV4[4] = id[1];
		handshakeIPV4[5] = id[0];
		handshakeIPV4[6] = flags;
		handshakeIPV4[6] <<= 5;
		handshakeIPV4[6] |= fragOffset[0];
		handshakeIPV4[7] = fragOffset[1];
		handshakeIPV4[8] = TTL;
		handshakeIPV4[9] = protocol;

		handshakeIPV4[10] = 0b0;
		handshakeIPV4[11] = 0b0;

		for (int i = 12, j = 0; i < 16; i++, j++)
			handshakeIPV4[i] = sourceAddr[j];
		for (int i = 16, j = 0; i < 20; i++, j++)
			handshakeIPV4[i] = destAddr[j];
		// =========================================

		// TCP header ==============================
		handshakeTCPHeader[0] = 0b0; // src port
		handshakeTCPHeader[1] = 0b0; //
		handshakeTCPHeader[2] = 0b0; // dest port
		handshakeTCPHeader[3] = 0b0; //
		handshakeTCPHeader[4] = (byte) (seqNum >> 24 & 0xFF);
		handshakeTCPHeader[5] = (byte) (seqNum >> 16 & 0xFF);
		handshakeTCPHeader[6] = (byte) (seqNum >> 8 & 0xFF);
		handshakeTCPHeader[7] = (byte) (seqNum & 0xFF);
		handshakeTCPHeader[8] = 0b0; // acknowledgement number
		handshakeTCPHeader[9] = 0b0; //
		handshakeTCPHeader[10] = 0b0; //
		handshakeTCPHeader[11] = 0b0; //
		handshakeTCPHeader[12] = (byte) (5 << 4); // header length(offset) //
													// reserved
		handshakeTCPHeader[13] = (byte) 0b0010000; // flags / ACK set to 1
		handshakeTCPHeader[14] = (byte) 0b0; // window size
		handshakeTCPHeader[15] = 0b0; //
		handshakeTCPHeader[16] = 0b0; // checksum
		handshakeTCPHeader[17] = 0b0; //
		handshakeTCPHeader[18] = 0b0; // urgent pointer
		handshakeTCPHeader[19] = 0b0; //

		// =========================================

		short[] tcpTearDownFinal = convertToShort(handshakeTCPHeader);
		short tcpChecksumTearDownFinal = (short) calculateChecksum(tcpTearDownFinal, (short) 20,
				sourceAddr, destAddr);

		handshakeTCPHeader[16] = (byte) ((tcpChecksumTearDownFinal >>> 8) & 0xFF);
		handshakeTCPHeader[17] = (byte) (tcpChecksumTearDownFinal & 0xFF);

		short[] IPV4header1Final = convertToShort(handshakeIPV4);
		short[] IPV4checksum1Final = new short[1];
		IPV4checksum[0] = (short) calculateChecksum(IPV4header1Final);
		byte byteArrayOuter1Final[] = convertToBytes(IPV4checksum1Final);

		handshakeIPV4[10] = byteArrayOuter[0];
		handshakeIPV4[11] = byteArrayOuter[1];

		// IPv4 packet with TCP header ==============
		byte[] outputBuffer1Final = new byte[40];
		for (int i = 0, j = 0; j < handshakeIPV4.length; i++, j++) {
			outputBuffer1Final[i] = handshakeIPV4[j];
		}
		for (int i = 20, j = 0; j < handshakeTCPHeader.length; i++, j++) {
			outputBuffer1Final[i] = handshakeTCPHeader[j];
		}
		// =========================================


		out.write(outputBuffer1Final);
		
		System.out.print("Reply to final ACK: ");
		for (int i = 0; i < 4; i++) {
			System.out.printf("%x", br.read());
		}
		System.out.println();
		


	}

}
