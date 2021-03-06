package com.utilities;

import com.domain.MessageType;
import com.domain.Packet;
import com.google.common.primitives.Bytes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Adaptor class which converts object from one form to another here
 * we are converting packet into byte array vice versa. This class also
 * returns new Packet objects as well.
 *
 * @author umar.tahir@afiniti.com
 *
 */

@Slf4j
public class Adaptor {

    public static ReentrantLock lock = new ReentrantLock();

    /**
     * In this method we will make a packet with generated
     * client id information. And for rest of information
     * in the packet includes constants which doesnt
     * have significant service value except it provides
     * meta data about packet.
     *
     * @param clientID id
     *
     * @return packet
     *
     */

     public Packet makingPacketWithGeneratedId(int clientID) {
        return Packet.builder().magicBytes(Constants.NO_MAGIC_BYTES_DEFINED).messageType(MessageType.GENERATED_ID).
                messageSourceId(Constants.SERVER_SOURCE_ID).messageDestinationId(clientID).
                messageLength(Constants.MESSAGE_FROM_SERVER.length()).
                message(Constants.MESSAGE_FROM_SERVER).build(); //verify
    }

    /**
     * This helper function is used to generate bytes array
     * from the packet. The byte is supposed to follow all the
     * standards set up by use.
     *
     *  first,  4 bytes for magic number
     *  second, 2 bytes for message types
     *  third,  4 bytes for source id
     *  fourth, 4 bytes for destination id
     *  fifth,  4 bytes for message length
     *  six,    x number of bytes for message
     *
     * @param packet any packet
     *
     * @return byte array
     *
     */

    public byte[] getBytesArrayFromPacket(Packet packet) {
        Adaptor.lock.lock();
        try{
            log.info("Execution of convertPacketIntoByteArray method started");
            List<Byte> byteArrayList = new ArrayList<>();

            byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMagicBytes()).array()));
            byteArrayList.addAll(Bytes.asList(packet.getMessageType().getMessageCode().getBytes()));
            byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMessageSourceId()).array()));
            byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMessageDestinationId()).array()));
            byteArrayList.addAll(Bytes.asList(ByteBuffer.allocate(4).putInt(packet.getMessageLength()).array()));
            byteArrayList.addAll(Bytes.asList(packet.getMessage().getBytes()));

            log.info("returning bytes array");
            log.info("Execution of convertMessagePacketIntoTheByteArray method ended");

            return Bytes.toArray(byteArrayList);
        }finally {
            Adaptor.lock.unlock();
        }

    }

    
    /**
     *
     * This method is used to build Packet with login details
     * which we will send to client
     *
     * @param packet received from client (request)
     *
     * @param magicNumber generated magic number for logged in client
     *
     * @return new packet to send to clients
     *
     */

    public Packet getLoggedInPacket(Packet packet,int magicNumber) {
        return Packet.builder().magicBytes(magicNumber).messageType(MessageType.LOGIN).messageSourceId(Constants.SERVER_SOURCE_ID).
                messageDestinationId(packet.getMessageSourceId()).messageLength(packet.getMessageLength()).
                message(packet.getMessage()).build();
    }

    /**
     * This packet is built in case we cant find destination socket
     *
     * @param packet old
     *
     * @return new packet
     *
     */

    public Packet getPacketWhenNoSocketPresent(Packet packet) {
        return Packet.builder().magicBytes(packet.getMagicBytes()).messageSourceId(Constants.SERVER_SOURCE_ID).
                messageDestinationId(packet.getMessageSourceId()).messageType(MessageType.DATA).
                message(Constants.MESSAGE_WHEN_DESTINATION_ID_NOT_PRESENT).
                messageLength(Constants.MESSAGE_WHEN_DESTINATION_ID_NOT_PRESENT.length()).build();
    }

    /**
     *
     *  This method returns new packet with logged out information
     *
     * @param packet old packet
     *
     * @return new logged out packet
     *
     */

    public Packet getLoggedOutPacket(Packet packet) {
        return Packet.builder().magicBytes(packet.getMagicBytes()).messageType(MessageType.LOGOUT).
                messageSourceId(Constants.SERVER_SOURCE_ID).messageDestinationId(packet.getMessageSourceId()).
                messageLength(packet.getMessage().length()).message(packet.getMessage()).build();
    }

}
