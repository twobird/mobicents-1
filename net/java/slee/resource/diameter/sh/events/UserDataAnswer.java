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

package net.java.slee.resource.diameter.sh.events;

import java.io.IOException;

import net.java.slee.resource.diameter.base.events.avp.ExperimentalResultAvp;
import net.java.slee.resource.diameter.base.events.avp.FailedAvp;
import net.java.slee.resource.diameter.sh.events.avp.userdata.ShData;

/**
 * Defines an interface representing the User-Data-Answer command.
 * 
 * From the Diameter Sh Reference Point Protocol Details (3GPP TS 29.329 V7.1.0)
 * specification:
 * 
 * <pre>
 * 6.1.2        User-Data-Answer (UDA) Command
 * 
 * The User-Data-Answer (UDA) command, indicated by the Command-Code field set 
 * to 306 and the 'R' bit cleared in the Command Flags field, is sent by a server in
 * response to the User-Data-Request command. The Experimental-Result AVP may
 * contain one of the values defined in section 6.2 or in 3GPP TS 29.229 [6].
 * 
 * Message Format
 * &lt; User-Data-Answer &gt; ::=        &lt; Diameter Header: 306, PXY, 16777217 &gt;
 *                                 &lt; Session-Id &gt;
 *                                 { Vendor-Specific-Application-Id }
 *                                 [ Result-Code ]
 *                                 [ Experimental-Result ]
 *                                 { Auth-Session-State }
 *                                 { Origin-Host }
 *                                 { Origin-Realm }
 *                                 *[ Supported-Features ]
 *                                 [ User-Data ]
 *                                 *[ AVP ]
 *                                 *[ Failed-AVP ]
 *                                 *[ Proxy-Info ]
 *                                 *[ Route-Record ]
 * </pre>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public interface UserDataAnswer extends DiameterShMessage {

  static final int commandCode = 306;

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
   * Returns true if the Experimental-Result AVP is present in the message.
   */
  boolean hasExperimentalResult();

  /**
   * Returns the value of the Experimental-Result AVP, of type Grouped.
   * 
   * @return the value of the Experimental-Result AVP or null if it has not
   *         been set on this message
   */
  ExperimentalResultAvp getExperimentalResult();

  /**
   * Sets the value of the Experimental-Result AVP, of type Grouped.
   * 
   * @throws IllegalStateException
   *             if setExperimentalResult has already been called
   */
  void setExperimentalResult(ExperimentalResultAvp experimentalResult);

  /**
   * Returns true if the Auth-Session-State AVP is present in the message.
   */

  /**
   * Returns true if the User-Data AVP is present in the message.
   */
  boolean hasUserData();

  /**
   * Returns the value of the User-Data AVP, of type UserData.
   * 
   * @return the value of the User-Data AVP or null if it has not been set on
   *         this message
   */
  byte[] getUserData();

  /**
   * Returns the value of the User-Data AVP, of type UserData.
   * 
   * @return the value of the User-Data AVP or null if it has not been set on
   *         this message
   */
  ShData getUserDataObject() throws IOException;

  /**
   * Sets the value of the User-Data AVP, of type UserData.
   * 
   * @throws IllegalStateException
   *             if setUserData has already been called
   */
  void setUserData(byte[] userData);

  /**
   * Sets the value of the User-Data AVP, of type UserData.
   * 
   * @throws IllegalStateException
   *             if setUserData has already been called
   */
  void setUserDataObject(ShData userData) throws IOException;

  /**
   * Returns the set of Failed-AVP AVPs. The returned array contains the AVPs
   * in the order they appear in the message. A return value of null implies
   * that no Failed-AVP AVPs have been set. The elements in the given array
   * are FailedAvp objects.
   */
  FailedAvp[] getFailedAvps();

  /**
   * Sets a single Failed-AVP AVP in the message, of type Grouped.
   * 
   * @throws IllegalStateException
   *             if setFailedAvp or setFailedAvps has already been called
   */
  void setFailedAvp(FailedAvp failedAvp);

  /**
   * Sets the set of Failed-AVP AVPs, with all the values in the given array.
   * The AVPs will be added to message in the order in which they appear in
   * the array.
   * 
   * Note: the array must not be altered by the caller following this call,
   * and getFailedAvps() is not guaranteed to return the same array instance,
   * e.g. an "==" check would fail.
   * 
   * @throws IllegalStateException
   *             if setFailedAvp or setFailedAvps has already been called
   */
  void setFailedAvps(FailedAvp[] failedAvps);

}
