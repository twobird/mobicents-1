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

package gov.nist.javax.sip.parser.chars;

import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.stack.SIPStackTimerTask;
import gov.nist.javax.sip.stack.timers.SipTimer;

import java.io.*;
import java.util.*;

/**
 * Input class for the pipelined parser. Buffer all bytes read from the socket
 * and make them available to the message parser.
 *
 * @author M. Ranganathan (Contains a bug fix contributed by Rob Daugherty (
 *         Lucent Technologies) )
 *
 */

public class Pipeline extends InputStream {
    private LinkedList buffList;

    private Buffer currentBuffer;

    private boolean isClosed;

    private SipTimer timer;

    private InputStream pipe;

    private int readTimeout;

    private SIPStackTimerTask myTimerTask;

    class MyTimer extends SIPStackTimerTask {
        Pipeline pipeline;

        private boolean isCancelled;

        protected MyTimer(Pipeline pipeline) {
            this.pipeline = pipeline;
        }

        public void runTask() {
            if (this.isCancelled)
                return;

            try {
                pipeline.close();
            } catch (IOException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }

        @Override
        public void cleanUpBeforeCancel() {
        	this.isCancelled = true;
        	super.cleanUpBeforeCancel();
        }             

    }

    class Buffer {
        byte[] bytes;

        int length;

        int ptr;

        public Buffer(byte[] bytes, int length) {
            ptr = 0;
            this.length = length;
            this.bytes = bytes;
        }

        public int getNextByte() {
            return (int) bytes[ptr++] & 0xFF;
        }

    }

    public void startTimer() {
        if (this.readTimeout == -1)
            return;
        // TODO make this a tunable number. For now 4 seconds
        // between reads seems reasonable upper limit.
        this.myTimerTask = new MyTimer(this);
        this.timer.schedule(this.myTimerTask, this.readTimeout);
    }

    public void stopTimer() {
        if (this.readTimeout == -1)
            return;
        if (this.myTimerTask != null)
        	this.timer.cancel(myTimerTask);
    }

    public Pipeline(InputStream pipe, int readTimeout, SipTimer timer) {
        // pipe is the Socket stream
        // this is recorded here to implement a timeout.
        this.timer = timer;
        this.pipe = pipe;
        buffList = new LinkedList();
        this.readTimeout = readTimeout;
    }

    public void write(byte[] bytes, int start, int length) throws IOException {
        if (this.isClosed)
            throw new IOException("Closed!!");
        Buffer buff = new Buffer(bytes, length);
        buff.ptr = start;
        synchronized (this.buffList) {
            buffList.add(buff);
            buffList.notifyAll();
        }
    }

    public void write(byte[] bytes) throws IOException {
        if (this.isClosed)
            throw new IOException("Closed!!");
        Buffer buff = new Buffer(bytes, bytes.length);
        synchronized (this.buffList) {
            buffList.add(buff);
            buffList.notifyAll();
        }
    }

    public void close() throws IOException {
        this.isClosed = true;
        synchronized (this.buffList) {
            this.buffList.notifyAll();
        }

        // JvB: added
        this.pipe.close();
    }

    public int read() throws IOException {
        // if (this.isClosed) return -1;
        synchronized (this.buffList) {
            if (currentBuffer != null
                    && currentBuffer.ptr < currentBuffer.length) {
                int retval = currentBuffer.getNextByte();
                if (currentBuffer.ptr == currentBuffer.length)
                    this.currentBuffer = null;
                return retval;
            }
            // Bug fix contributed by Rob Daugherty.
            if (this.isClosed && this.buffList.isEmpty())
                return -1;
            try {
                // wait till something is posted.
                while (this.buffList.isEmpty()) {
                    this.buffList.wait();
                    if (this.isClosed)
                        return -1;
                }
                currentBuffer = (Buffer) this.buffList.removeFirst();
                int retval = currentBuffer.getNextByte();
                if (currentBuffer.ptr == currentBuffer.length)
                    this.currentBuffer = null;
                return retval;
            } catch (InterruptedException ex) {
                throw new IOException(ex.getMessage());
            } catch (NoSuchElementException ex) {
                ex.printStackTrace();
                throw new IOException(ex.getMessage());
            }
        }
    }

}
