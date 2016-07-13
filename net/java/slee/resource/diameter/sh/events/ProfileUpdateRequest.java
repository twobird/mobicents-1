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

import net.java.slee.resource.diameter.sh.events.avp.DataReferenceType;
import net.java.slee.resource.diameter.sh.events.avp.UserIdentityAvp;
import net.java.slee.resource.diameter.sh.events.avp.userdata.ShData;

/**
 * Defines an interface representing the Profile-Update-Request command.
 * 
 * From the Diameter Sh Reference Point Protocol Details (3GPP TS 29.329 V7.1.0)
 * specification:
 * 
 * <pre>
 * 6.1.3        Profile-Update-Request (PUR) Command
 * 
 * The Profile-Update-Request (PUR) command, indicated by the Command-Code field
 * set to 307 and the 'R' bit set in the Command Flags field, is sent by a
 * Diameter client to a Diameter server in order to update user data in the
 * server. 
 * 
 * Message Format
 * &lt; Profile-Update-Request &gt; ::=      &lt; Diameter Header: 307, REQ, PXY, 16777217 &gt;
 *                                     &lt; Session-Id &gt;
 *                                     { Vendor-Specific-Application-Id }
 *                                     { Auth-Session-State }
 *                                     { Origin-Host }
 *                                     { Origin-Realm }
 *                                     [ Destination-Host ]
 *                                     { Destination-Realm }
 *                                    *[ Supported-Features ]
 *                                     { User-Identity }
 *                                     { Data-Reference }
 *                                     { User-Data }
 *                                    *[ AVP ]
 *                                    *[ Proxy-Info ]
 *                                    *[ Route-Record ]
 * </pre>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public interface ProfileUpdateRequest extends DiameterShMessage {

  static final int commandCode = 307;

  /**
   * Returns true if the User-Identity AVP is present in the message.
   */
  boolean hasUserIdentity();

  /**
   * Returns the value of the User-Identity AVP, of type Grouped.
   * 
   * @return the value of the User-Identity AVP or null if it has not been set
   *         on this message
   */
  UserIdentityAvp getUserIdentity();

  /**
   * Sets the value of the User-Identity AVP, of type Grouped.
   * 
   * @throws IllegalStateException
   *             if setUserIdentity has already been called
   */
  void setUserIdentity(UserIdentityAvp userIdentity);

  /**
   * Returns true if the Data-Reference AVP is present in the message.
   */
  boolean hasDataReference();

  /**
   * Returns the value of the Data-Reference AVP, of type Enumerated.
   * 
   * @return the value of the Data-Reference AVP or null if it has not been
   *         set on this message
   */
  DataReferenceType getDataReference();

  /**
   * Sets the value of the Data-Reference AVP, of type Enumerated.
   * 
   * @throws IllegalStateException
   *             if setDataReference has already been called
   */
  void setDataReference(DataReferenceType dataReference);

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

}
