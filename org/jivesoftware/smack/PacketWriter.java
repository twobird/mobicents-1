/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jivesoftware.smack;

import java.util.*;
import java.io.*;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * Writes packets to a XMPP server. Packets are sent using a dedicated thread. Packet
 * interceptors can be registered to dynamically modify packets before they're actually
 * sent. Packet listeners can be registered to listen for all outgoing packets.
 *
 * @author Matt Tucker
 */
class PacketWriter {

    private Thread writerThread;
    private Writer writer;
    private XMPPConnection connection;
    private LinkedList queue;
    private boolean done = false;
    
    private List listeners = new ArrayList();
    private boolean listenersDeleted = false;

    /**
     * List of PacketInterceptor that will be notified when a new packet is about to be
     * sent to the server. These interceptors may modify the packet before it is being
     * actually sent to the server.
     */
    private List interceptors = new ArrayList();
    /**
     * Flag that indicates if an interceptor was deleted. This is an optimization flag.
     */
    private boolean interceptorDeleted = false;

    /**
     * Creates a new packet writer with the specified connection.
     *
     * @param connection the connection.
     */
    protected PacketWriter(XMPPConnection connection) {
        this.connection = connection;
        this.writer = connection.writer;
        this.queue = new LinkedList();

        writerThread = new Thread() {
            public void run() {
                writePackets();
            }
        };
        writerThread.setName("Smack Packet Writer");
        writerThread.setDaemon(true);

        // Schedule a keep-alive task to run if the feature is enabled. will write
        // out a space character each time it runs to keep the TCP/IP connection open.
        int keepAliveInterval = SmackConfiguration.getKeepAliveInterval();
        if (keepAliveInterval > 0) {
            Thread keepAliveThread = new Thread(new KeepAliveTask(keepAliveInterval));
            keepAliveThread.setDaemon(true);
            keepAliveThread.start();
        }
    }

    /**
     * Sends the specified packet to the server.
     *
     * @param packet the packet to send.
     */
    public void sendPacket(Packet packet) {
    	
    	//EduardoMartins: set from if is a component connection
    	if(connection instanceof ComponentXMPPConnection && packet.getFrom() == null) {
    		packet.setFrom(((ComponentXMPPConnection)connection).componentName);
    	}
    		
        if (!done) {
            // Invoke interceptors for the new packet that is about to be sent. Interceptors
            // may modify the content of the packet.
            processInterceptors(packet);

            synchronized(queue) {
                queue.addFirst(packet);
                queue.notifyAll();
            }

            // Process packet writer listeners. Note that we're using the sending
            // thread so it's expected that listeners are fast.
            processListeners(packet);
        }
    }

    /**
     * Registers a packet listener with this writer. The listener will be
     * notified immediately after every packet this writer sends. A packet filter
     * determines which packets will be delivered to the listener. Note that the thread
     * that writes packets will be used to invoke the listeners. Therefore, each
     * packet listener should complete all operations quickly or use a different
     * thread for processing.
     *
     * @param packetListener the packet listener to notify of sent packets.
     * @param packetFilter the packet filter to use.
     */
    public void addPacketListener(PacketListener packetListener, PacketFilter packetFilter) {
        synchronized (listeners) {
            listeners.add(new ListenerWrapper(packetListener, packetFilter));
        }
    }

    /**
     * Removes a packet listener.
     *
     * @param packetListener the packet listener to remove.
     */
    public void removePacketListener(PacketListener packetListener) {
        synchronized (listeners) {
            for (int i=0; i<listeners.size(); i++) {
                ListenerWrapper wrapper = (ListenerWrapper)listeners.get(i);
                if (wrapper != null && wrapper.packetListener.equals(packetListener)) {
                    listeners.set(i, null);
                    // Set the flag to indicate that the listener list needs
                    // to be cleaned up.
                    listenersDeleted = true;
                }
            }
        }
    }

    /**
     * Returns the number of registered packet listeners.
     *
     * @return the count of packet listeners.
     */
    public int getPacketListenerCount() {
        synchronized (listeners) {
            return listeners.size();
        }
    }

    /**
     * Registers a packet interceptor with this writer. The interceptor will be
     * notified of every packet that this writer is about to send. Interceptors
     * may modify the packet to be sent. A packet filter determines which packets
     * will be delivered to the interceptor.
     *
     * @param packetInterceptor the packet interceptor to notify of packets about to be sent.
     * @param packetFilter the packet filter to use.
     */
    public void addPacketInterceptor(PacketInterceptor packetInterceptor, PacketFilter packetFilter) {
        synchronized (interceptors) {
            interceptors.add(new InterceptorWrapper(packetInterceptor, packetFilter));
        }
    }

    /**
     * Removes a packet interceptor.
     *
     * @param packetInterceptor the packet interceptor to remove.
     */
    public void removePacketInterceptor(PacketInterceptor packetInterceptor) {
        synchronized (interceptors) {
            for (int i=0; i<interceptors.size(); i++) {
                InterceptorWrapper wrapper = (InterceptorWrapper)interceptors.get(i);
                if (wrapper != null && wrapper.packetInterceptor.equals(packetInterceptor)) {
                    interceptors.set(i, null);
                    // Set the flag to indicate that the interceptor list needs
                    // to be cleaned up.
                    interceptorDeleted = true;
                }
            }
        }
    }

    /**
     * Starts the packet writer thread and opens a connection to the server. The
     * packet writer will continue writing packets until {@link #shutdown} or an
     * error occurs.
     */
    public void startup() {
        writerThread.start();
    }

    void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Shuts down the packet writer. Once this method has been called, no further
     * packets will be written to the server.
     */
    public void shutdown() {
        done = true;
    }

    /**
     * Returns the next available packet from the queue for writing.
     *
     * @return the next packet for writing.
     */
    private Packet nextPacket() {
        synchronized(queue) {
            while (!done && queue.size() == 0) {
                try {
                    queue.wait(2000);
                }
                catch (InterruptedException ie) { }
            }
            if (queue.size() > 0) {
                return (Packet)queue.removeLast();
            }
            else {
                return null;
            }
        }
    }

    private void writePackets() {
        try {
            // Open the stream.
            openStream();
            // Write out packets from the queue.
            while (!done) {
                Packet packet = nextPacket();
                if (packet != null) {
                    synchronized (writer) {
                        writer.write(packet.toXML());
                        writer.flush();
                    }
                }
            }
            // Close the stream.
            try {
                writer.write("</stream:stream>");
                writer.flush();
            }
            catch (Exception e) { }
            finally {
                try {
                    writer.close();
                }
                catch (Exception e) { }
            }
        }
        catch (IOException ioe){
            if (!done) {
                done = true;
                connection.packetReader.notifyConnectionError(ioe);
            }
        }
    }

    /**
     * Process listeners.
     */
    private void processListeners(Packet packet) {
        // Clean up null entries in the listeners list if the flag is set. List
        // removes are done seperately so that the main notification process doesn't
        // need to synchronize on the list.
        synchronized (listeners) {
            if (listenersDeleted) {
                for (int i=listeners.size()-1; i>=0; i--) {
                    if (listeners.get(i) == null) {
                        listeners.remove(i);
                    }
                }
                listenersDeleted = false;
            }
        }
        // Notify the listeners of the new sent packet
        int size = listeners.size();
        for (int i=0; i<size; i++) {
            ListenerWrapper listenerWrapper = (ListenerWrapper)listeners.get(i);
            if (listenerWrapper != null) {
                listenerWrapper.notifyListener(packet);
            }
        }
    }

    /**
     * Process interceptors. Interceptors may modify the packet that is about to be sent.
     * Since the thread that requested to send the packet will invoke all interceptors, it
     * is important that interceptors perform their work as soon as possible so that the
     * thread does not remain blocked for a long period.
     *
     * @param packet the packet that is going to be sent to the server
     */
    private void processInterceptors(Packet packet) {
        if (packet != null) {
            // Clean up null entries in the interceptors list if the flag is set. List
            // removes are done seperately so that the main notification process doesn't
            // need to synchronize on the list.
            synchronized (interceptors) {
                if (interceptorDeleted) {
                    for (int i=interceptors.size()-1; i>=0; i--) {
                        if (interceptors.get(i) == null) {
                            interceptors.remove(i);
                        }
                    }
                    interceptorDeleted = false;
                }
            }
            // Notify the interceptors of the new packet to be sent
            int size = interceptors.size();
            for (int i=0; i<size; i++) {
                InterceptorWrapper interceptorWrapper = (InterceptorWrapper)interceptors.get(i);
                if (interceptorWrapper != null) {
                    interceptorWrapper.notifyListener(packet);
                }
            }
        }
    }

    /**
     * Sends to the server a new stream element. This operation may be requested several times
     * so we need to encapsulate the logic in one place. This message will be sent while doing
     * TLS, SASL and resource binding.
     *
     * @throws IOException If an error occurs while sending the stanza to the server.
     */
    void openStream() throws IOException {    	
        StringBuffer stream = new StringBuffer();
        stream.append("<stream:stream");
        stream.append(" to=\"").append(connection.serviceName).append("\"");
        // EduardoMartins: append namespace regarding connection type
        if(connection instanceof ComponentXMPPConnection) {
        	stream.append(" xmlns=\"jabber:component:accept\"");
        }
        else {
        	stream.append(" xmlns=\"jabber:client\"");
        }
        stream.append(" xmlns:stream=\"http://etherx.jabber.org/streams\"");
        if (connection instanceof SSLXMPPConnection) {
            // Old SSL connections should not include indicate XMPP 1.0 compliance
            stream.append(">");
        }
        else {
            stream.append(" version=\"1.0\">");
        }        
        writer.write(stream.toString());
        writer.flush();       
    }

    /**
     * A wrapper class to associate a packet filter with a listener.
     */
    private static class ListenerWrapper {

        private PacketListener packetListener;
        private PacketFilter packetFilter;

        public ListenerWrapper(PacketListener packetListener,
                               PacketFilter packetFilter)
        {
            this.packetListener = packetListener;
            this.packetFilter = packetFilter;
        }

        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            if (object instanceof ListenerWrapper) {
                return ((ListenerWrapper)object).packetListener.equals(this.packetListener);
            }
            else if (object instanceof PacketListener) {
                return object.equals(this.packetListener);
            }
            return false;
        }

        public void notifyListener(Packet packet) {
            if (packetFilter == null || packetFilter.accept(packet)) {
                packetListener.processPacket(packet);
            }
        }
    }

    /**
     * A wrapper class to associate a packet filter with an interceptor.
     */
    private static class InterceptorWrapper {

        private PacketInterceptor packetInterceptor;
        private PacketFilter packetFilter;

        public InterceptorWrapper(PacketInterceptor packetInterceptor, PacketFilter packetFilter)
        {
            this.packetInterceptor = packetInterceptor;
            this.packetFilter = packetFilter;
        }

        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            if (object instanceof InterceptorWrapper) {
                return ((InterceptorWrapper) object).packetInterceptor
                        .equals(this.packetInterceptor);
            }
            else if (object instanceof PacketInterceptor) {
                return object.equals(this.packetInterceptor);
            }
            return false;
        }

        public void notifyListener(Packet packet) {
            if (packetFilter == null || packetFilter.accept(packet)) {
                packetInterceptor.interceptPacket(packet);
            }
        }
    }

    /**
     * A TimerTask that keeps connections to the server alive by sending a space
     * character on an interval.
     */
    private class KeepAliveTask implements Runnable {

        private int delay;

        public KeepAliveTask(int delay) {
            this.delay = delay;
        }

        public void run() {
            while (!done) {
                synchronized (writer) {
                    try {
                        writer.write(" ");
                        writer.flush();
                    }
                    catch (Exception e) { }
                }
                try {
                    // Sleep until we should write the next keep-alive.
                    Thread.sleep(delay);
                }
                catch (InterruptedException ie) { }
            }
        }
    }
}