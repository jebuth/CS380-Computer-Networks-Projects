byte[] makeUDP(byte[] data) 
    { 
        byte[] udp = new byte[8 + data.length];
        // source port, set to something random 
        udp[0] = 0x06; 
        udp[1] = 0x60;
        // destination port
        short temp = (short) udpPort;
        udp[2] = (byte) ((temp >>> 8) & 0xFF);
        udp[3] = (byte) (temp & 0xFF);
        
        temp = (short) (8 + data.length);
        udp[4] = (byte) ((temp >>> 8) & 0xFF); 
        udp[5] = (byte) (temp & 0xFF);
        
        // start checksum with 0
        udp[6] = 0; 
        udp[7] = 0;
        // udp packet data
        for(int i=8, j=0; j < data.length; ++i, ++j)
            udp[i] = data[j];
        short shortarray[] = toShortArray(udp);
        short checksum = (short)udpChecksum(shortarray, temp);
        udp[6] = (byte) ((checksum >>> 8) & 0xFF);
        udp[7] = (byte) (checksum & 0xFF);
        return udp;
    }
