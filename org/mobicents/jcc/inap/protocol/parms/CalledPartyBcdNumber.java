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

package org.mobicents.jcc.inap.protocol.parms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author Oleg Kulikov
 */
public class CalledPartyBcdNumber implements Serializable {
    
    private String address;
    
    private int ext;
    private int ni;
    private int np;
    
    /** Creates a new instance of CalingPartyBcdNumber */
    public CalledPartyBcdNumber(int ext, int ni, int np, String address) {
        this.ext = ext;
        this.ni = ni;
        this.np = np;
        this.address = address;
    }
    
    public CalledPartyBcdNumber(String address) {
        this.address = address;
        this.ni = CalledPartyNumber.NATIONAL;
    }
    
    public CalledPartyBcdNumber(byte[] bin) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bin);
        int length = bin.length;
        
        int b = in.read() & 0xff;
        
        ext = (b & 0x7f) >> 7;
        ni = (b & 0x70) >> 4;
        np = (b & 0x0f);
                
        length -= 1;
        
        address = "";
        
        while (length - 1 > 0) {
            b = in.read() & 0xff;
            
            int d1 = b & 0x0f;
            int d2 = (b & 0xf0) >> 4;
            
            address += Integer.toHexString(d1) + Integer.toHexString(d2);            
            length--;
        }
        
        b = in.read() & 0xff;
        address += Integer.toHexString((b & 0x0f));
        
        int c = (b & 0xf0) >> 4;
        if (c != 0x0f) {
            address += Integer.toHexString(c);
        }   
    }    

    public String getAddress() {
        return address;
    }

    public void setAddress(String digits) {
        this.address = digits;
    }
    
    public int getNi() {
        return ni;
    }

    public int getNp() {
        return np;
    }

    public int getExt() {
        return ext;
    }
    
    public String toString() {
        return ni + ":" + address;
    }
}
