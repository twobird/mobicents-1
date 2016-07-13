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
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
*
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*
* .
*
*/
/************************************************************************************************
 * PRODUCT OF PT INOVACAO - EST DEPARTMENT and Telecommunications Institute (Aveiro, Portugal)  *
 ************************************************************************************************/


package gov.nist.javax.sip.parser.chars.ims;

/**
 * Security-Server header parser.
 *
 * @author Miguel Freitas (IT) PT-Inovacao
 */


import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

import gov.nist.javax.sip.header.ims.SecurityServerList;
import gov.nist.javax.sip.header.ims.SecurityServer;


public class SecurityServerParser extends SecurityAgreeParser
{

    public SecurityServerParser(String security)
    {
        super(security);
    }

    protected SecurityServerParser(Lexer lexer)
    {
        super(lexer);
    }


    public SIPHeader parse() throws ParseException
    {
        dbg_enter("SecuriryServer parse");
        try {

            headerName(TokenTypes.SECURITY_SERVER);
            SecurityServer secServer = new SecurityServer();
            SecurityServerList secServerList =
                (SecurityServerList) super.parse(secServer);
            return secServerList;

        } finally {
            dbg_leave("SecuriryServer parse");
        }
    }



    /** Test program

    public static void main(String args[]) throws ParseException {
        String r[] = {
                "Security-Server: ipsec-3gpp; ealg=aes-cbc; " +
                                "alg=hmac-md5-96; port-c=5062; port-s=5063; " +
                                "q=0.1\n"
                };

        String r2[] = {
                "Security-Server: ipsec-3gpp; ealg=aes-cbc; " +
                                "alg=hmac-md5-96; port-c=5062; port-s=5063; " +
                                "q=0.1, " +
                                "digest; d-alg=md5; " +
                                "d-qop=auth-int; d-ver=AEF1D222; " +
                                "q=0.01\n"
                };


        for (int i = 0; i < r.length; i++ )
        {

            SecurityServerParser parser =
              new SecurityServerParser(r[i]);

            SecurityServer secServer= (SecurityServer) parser.parse();
            System.out.println("encoded = " + secServer.encode());
        }


        for (int i = 0; i < r2.length; i++ ) {
            SecurityServerParser parser =
              new SecurityServerParser(r2[i]);

            java.util.ListIterator list;
            SecurityServerList secList = (SecurityServerList) parser.parse();
            System.out.println("encoded = " + secList.encode());
        }


    }
    */



}


