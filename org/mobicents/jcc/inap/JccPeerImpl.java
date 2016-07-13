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
 * File Name     : JccPeerImpl.java
 *
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

package org.mobicents.jcc.inap;

import java.util.Properties;
import java.util.StringTokenizer;

import javax.csapi.cc.jcc.*;

/**
 * Implements javax.csapi.cc.jcc.JccPeer Interface.
 *
 * @author Oleg Kulikov
 */
public class JccPeerImpl implements JccPeer {
    
    private final static String name = "Java Call Control API for INAP";
    /**
     * Creates a new instance of JccPeerImpl 
     */
    public JccPeerImpl() {
    }

    /**
     * (Non Java-doc).
     * @see javax.csapi.cc.jcc.JccPeer#getName().
     */
    public String getName() {
        return name;
    }

    /**
     * (Non Java-doc).
     * @see javax.csapi.cc.jcc.JccPeer#getProvider(String).
     */
    public JccProvider getProvider(String providerString) throws ProviderUnavailableException {
        //The providerString argument has the following format: 
        //< service name > ; arg1 = val1; arg2 = val2; ... 
        //Where < service name > is not optional, and each optional argument 
        //pair which follows is separated by a semi-colon.
        //The keys for these arguments is implementation specific, except for 
        //two standard-defined keys: 
        //login: provides the login user name to the Provider. 
        //passwd: provides a password to the Provider.
        String serviceName = null;
        Properties properties = new Properties();
        
        int pos = providerString.indexOf(';');
        if (pos < 0) {
            serviceName = providerString;
        } else {
            StringTokenizer tokenizer = new StringTokenizer(providerString, ";");
            serviceName = tokenizer.nextToken();
            while (tokenizer.hasMoreTokens()) {
                String pair = tokenizer.nextToken();
                pos = pair.indexOf("=");
                
                if ( pos < 0) {
                    throw new ProviderUnavailableException("Bad argument's format: " + pair);
                }
                
                String arg = pair.substring(0, pos).trim();
                String val = pair.substring(pos + 1, pair.length()).trim();
                
                properties.setProperty(arg, val);
            }
        }
        
        if (!serviceName.equals("<jcc-inap>")) {
            throw new ProviderUnavailableException("Not supported service: " + serviceName);
        }
        System.out.println("**** JCC: loading provider..."); 
        return new JccInapProviderImpl(properties);
    }

    /**
     * (Non Java-doc).
     * @see javax.csapi.cc.jcc.JccPeer#getServices().
     */
    public String[] getServices() {
        return new String[]{"jcc-inap"};
    }
    
}
