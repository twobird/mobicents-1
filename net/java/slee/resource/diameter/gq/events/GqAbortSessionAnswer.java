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

package net.java.slee.resource.diameter.gq.events;

import net.java.slee.resource.diameter.base.events.DiameterMessage;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;
import net.java.slee.resource.diameter.base.events.avp.DiameterURI;
import net.java.slee.resource.diameter.base.events.avp.ExperimentalResultAvp;
import net.java.slee.resource.diameter.base.events.avp.FailedAvp;
import net.java.slee.resource.diameter.base.events.avp.ProxyInfoAvp;
import net.java.slee.resource.diameter.base.events.avp.RedirectHostUsageType;

/**
 * <pre>
 * <b>7.1.8 Abort-Session-Answer (ASA) command</b>
 * 
 * The ASA command, indicated by the Command-Code field set to 274 and the 'R' bit 
 * cleared in the Command Flags field, is sent by the AF to the SPDF in response to the
 * ASR command.
 * 
 * Message Format:
 * &lt;Abort-Session-Answer&gt; ::= < Diameter Header: 274, PXY > < Session-Id >
 *                            { Origin-Host } 
 *                            { Origin-Realm } 
 *                            [ Result-Code ]
 *                            [ Experimental-Result ] 
 *                            [ Origin-State-Id ] 
 *                            [ Error-Message ] 
 *                            [ Error-Reporting-Host ]
 *                           *[ Failed-AVP ] 
 *                           *[ Redirected-Host ]
 *                            [ Redirected-Host-Usage ]
 *                            [ Redirected-Max-Cache-Time ] 
 *                           *[ Proxy-Info ] 
 *                           *[ AVP ]
 * </pre>
 * 
 * @author <a href="mailto:webdev@web-ukraine.info"> Yulian Oifa </a>
 */
public interface GqAbortSessionAnswer extends DiameterMessage
{
	public static final int COMMAND_CODE = 274;
	
	/**
	* Returns true if the Result-Code AVP is present in the message.
	*/
	boolean hasResultCode();

	/**
	* Returns the value of the Result-Code AVP, of type Unsigned32. Use
	* {@link #hasResultCode()} to check the existence of this AVP.
	* 
	* @return the value of the Result-Code AVP
	* @throws IllegalStateException
	*             if the Result-Code AVP has not been set on this message
	*/
	long getResultCode();

	/**
	* Sets the value of the Result-Code AVP, of type Unsigned32.
	* 
	* @throws IllegalStateException
	*             if setResultCode has already been called
	*/
	void setResultCode(long resultCode);
		
	/**
	* Returns the set of Failed-AVP AVPs.
	* 
	* @return
	*/
	FailedAvp[] getFailedAvps();

	/**
	* Sets a single Failed-AVP AVP in the message, of type Grouped.
	* 
	* @param failedAvp
	* @throws IllegalStateException
	*/
	void setFailedAvp(FailedAvp failedAvp) throws IllegalStateException;

	/**
	* Sets the set of Failed-AVP AVPs, with all the values in the given array.
	* 
	* @param failedAvps
	* @throws IllegalStateException
	*/
	void setFailedAvps(FailedAvp[] failedAvps) throws IllegalStateException;
		
	/**
	* Returns the set of Proxy-Info AVPs.
	* 
	* @return
	*/
	ProxyInfoAvp[] getProxyInfos();

	/**
	* Sets a single Proxy-Info AVP in the message, of type Grouped.
	* 
	* @param proxyInfo
	* @throws IllegalStateException
	*/
	void setProxyInfo(ProxyInfoAvp proxyInfo) throws IllegalStateException;

	/**
	* Sets the set of Proxy-Info AVPs, with all the values in the given array.
	* 
	* @param proxyInfos
	* @throws IllegalStateException
	*/
	void setProxyInfos(ProxyInfoAvp[] proxyInfos) throws IllegalStateException;
	
	/**
	* Returns true if the Error-Message AVP is present in the message.
	*/
	boolean hasErrorMessage();

	/**
	* Returns the value of the Error-Message AVP, of type UTF8String.
	* @return the value of the Error-Message AVP or null if it has not been set on this message
	*/
	String getErrorMessage();

	/**
	* Sets the value of the Error-Message AVP, of type UTF8String.
	* @throws IllegalStateException if setErrorMessage has already been called
	*/
	void setErrorMessage(String errorMessage) throws IllegalStateException;
	
	/**
	* Returns true if the Error-Reporting-Host AVP is present in the message.
	*/
	boolean hasErrorReportingHost();

	/**
	* Returns the value of the Error-Reporting-Host AVP, of type DiameterIdentity.
	* @return the value of the Error-Reporting-Host AVP or null if it has not been set on this message
	*/
	DiameterIdentity getErrorReportingHost();

	/**
	* Sets the value of the Error-Reporting-Host AVP, of type DiameterIdentity.
	* @throws IllegalStateException if setErrorReportingHost has already been called
	*/
	void setErrorReportingHost(DiameterIdentity errorReportingHost) throws IllegalStateException;
	
	/**
	* Returns true if the Experimental-Result AVP is present in the message.
	*/
	boolean hasExperimentalResult();

	/**
	* Returns the value of the Experimental-Result AVP, of type ExperimentalResultAvp.
	* @return the value of the Experimental-Result AVP or null if it has not been set on this message
	*/
	ExperimentalResultAvp getExperimentalResult();

	/**
	* Sets the value of the Experimental-Result AVP, of type ExperimentalResultAvp.
	* @throws IllegalStateException if setErrorReportingHost has already been called
	*/
	void setExperimentalResult(ExperimentalResultAvp experimentalResult) throws IllegalStateException;
	
	/**
	* Returns true if the Origin-State-Id AVP is present in the message.
	*/
	boolean hasOriginStateId();
	    
	/**
	* Returns the value of the Origin-State-Id AVP, of type Unsigned32. Use
	* {@link #hasOriginStateId()} to check the existence of this AVP.
	* 
	* @return the value of the Origin-State-Idd AVP
	* @throws IllegalStateException
	*             if the Origin-State-Id AVP has not been set on this message
	*/
	long getOriginStateId();

	/**
	* Sets the value of the Origin-State-Id AVP, of type Unsigned32.
	* 
	* @throws IllegalStateException
	*             if setOriginStateId has already been called
	*/
	void setOriginStateId(long originStateId);
	
	/**
	* Sets the value of the Redirect-Host-Usage AVP, of type RedirectHostUsageType.
	* @throws IllegalStateException if setErrorReportingHost has already been called
	*/
	void setRedirectHostUsage(RedirectHostUsageType errorReportingHost) throws IllegalStateException;
	
	/**
	* Returns true if the Redirect-Host-Usage AVP is present in the message.
	*/
	boolean hasRedirectHostUsage();

	/**
	* Returns the value of the  Redirect-Host-Usage AVP, of type RedirectHostUsageType.
	* @return the value of the  Redirect-Host-Usage AVP or null if it has not been set on this message
	*/
	RedirectHostUsageType getRedirectHostUsage();
	
	/**
	* Returns true if the Redirect-Max-Cache-Time AVP is present in the message.
	*/
	boolean hasRedirectMaxCacheTime();
	    
	/**
	* Returns the value of the Redirect-Max-Cache-Time AVP, of type Unsigned32. Use
	* {@link #hasRedirectMaxCacheTime()} to check the existence of this AVP.
	* 
	* @return the value of the Redirect-Max-Cache-Time AVP
	* @throws IllegalStateException
	*             if the Redirect-Max-Cache-Time AVP has not been set on this message
	*/
	long getRedirectMaxCacheTime();

	/**
	* Sets the value of the Redirect-Max-Cache-Time AVP, of type Unsigned32.
	* 
	* @throws IllegalStateException
	*             if setRedirectMaxCacheTime has already been called
	*/
	void setRedirectMaxCacheTime(long redirectMaxCacheTime);
	
	/**
	* Returns the set of Redirect-Host AVPs.
	* 
	* @return
	*/
	DiameterURI[] getRedirectHosts();

	/**
	* Sets a single Redirect-Host AVP in the message, of type DiameterIdentity.
	* 
	* @param redirectHost
	* @throws IllegalStateException
	*/
	void setRedirectHost(DiameterURI redirectHost) throws IllegalStateException;

	/**
	* Sets the set of Redirect-Host AVPs, with all the values in the given
	* array.
	* 
	* @param redirectHosts
	* @throws IllegalStateException
	*/
	void setRedirectHosts(DiameterURI[] redirectHosts) throws IllegalStateException;
}
