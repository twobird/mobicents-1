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

/*
 * The Java Call Control API for CAMEL 2
 *
 * The source code contained in this file is in in the public domain.
 * It can be used in any project or product without prior permission,
 * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
 * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * AND DATA ACCURACY.  We do not warrant or make any representations
 * regarding the use of the software or the  results thereof, including
 * but not limited to the correctness, accuracy, reliability or
 * usefulness of the software.
 */

package org.mobicents.jcc.inap.protocol;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.mobicents.jcc.inap.protocol.parms.CalledPartyBcdNumber;
import org.mobicents.jcc.inap.protocol.parms.CalledPartyNumber;
import org.mobicents.jcc.inap.protocol.parms.CallingPartyNumber;
import org.mobicents.jcc.inap.protocol.tcap.Util;

/**
 *
 * @author Oleg Kulikov
 */
public class InitialDP extends Operation {
    
    private int serviceKey;
    
    private CalledPartyNumber calledPartyNumber;
    private CallingPartyNumber callingPartyNumber;
    private CalledPartyBcdNumber calledPartyBcdNumber;
    
    /** Creates a new instance of InitialDP */
    public InitialDP() {
    }
    
    public InitialDP(byte[] bin) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bin);
        while (in.available() > 0) {
            int param = Util.readTag(in);
            int length = Util.readLen(in);
            
            byte[] buff = new byte[length];
            in.read(buff);
            
            switch (param) {
                case 0 :
                    serviceKey = buff[0];
                    break;
                case 2 :
                    calledPartyNumber = new CalledPartyNumber(buff);
                    break;
                case 3 :
                    callingPartyNumber = new CallingPartyNumber(buff);
                    break;
                case 56 :
                    calledPartyBcdNumber = new CalledPartyBcdNumber(buff);
                    break;
            }
        }
    }
    
    
    public int getServiceKey() {
        return serviceKey;
    }
    public CalledPartyBcdNumber getCalledPartyBcdNumber() {
        return calledPartyBcdNumber;
    }
    
    public CalledPartyNumber getCalledPartyNumber() {
        return calledPartyNumber;
    }
    
    public CallingPartyNumber getCallingPartyNumber() {
        return callingPartyNumber;
    }
    
    public byte[] toByteArray() {
        return null;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("InitialDP { CallingPartyNumber=" + callingPartyNumber);
        
        if (calledPartyNumber != null) {
            buffer.append(",CalledPartyNumber=" + calledPartyNumber);
        }
        
        if (calledPartyBcdNumber != null) {
            buffer.append(",CalledPartyBcdNumber=" + calledPartyBcdNumber);
        }
        
        buffer.append("}");
        return buffer.toString();
    }
}
