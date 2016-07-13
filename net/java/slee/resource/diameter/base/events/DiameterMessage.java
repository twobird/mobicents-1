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

package net.java.slee.resource.diameter.base.events;

import net.java.slee.resource.diameter.base.events.avp.AvpNotAllowedException;
import net.java.slee.resource.diameter.base.events.avp.DiameterAvp;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;

/**
 * A Diameter message containing a command code and a collection of AVPs.
 *<P>
 * This is a representation of the contents of a message that a client may be
 * interested in.
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public interface DiameterMessage extends Cloneable {

  /**
   * Return the
   * {@link org.mobicents.slee.resource.diameter.base.DiameterHeader} in this
   * message, if it exists. The header will not exist for outgoing messages
   * created via the
   * {@link net.java.slee.resource.diameter.base.DiameterMessageFactory}.
   * 
   * @return DiameterHeader if one exists, otherwise null
   */
  DiameterHeader getHeader();

  /**
   * Return the {@link DiameterCommand} contained in the header of this
   * message.
   * 
   * @return a DiameterCommand instance
   */
  DiameterCommand getCommand();

  /**
   * Return the AVPs contained in this message, as an array of DiameterAvp
   * objects. AVPs are returned in the same order in which they appear in the
   * message.
   * 
   * @return a list of AVPs
   */
  DiameterAvp[] getAvps();

  /**
   * Returns true if the Session-Id AVP is present in the message.
   */
  boolean hasSessionId();

  /**
   * Returns the value of the Session-Id AVP, of type UTF8String. A return
   * value of null implies that the AVP has not been set.
   */
  String getSessionId();

  /**
   * Sets the value of the Session-Id AVP, of type UTF8String.
   * 
   * @throws IllegalStateException
   *             if setSessionId has already been called
   */
  void setSessionId(String sessionId);

  /**
   * Returns true if the Origin-Host AVP is present in the message.
   */
  boolean hasOriginHost();

  /**
   * Returns the value of the Origin-Host AVP, of type DiameterIdentity. A
   * return value of null implies that the AVP has not been set.
   */
  DiameterIdentity getOriginHost();

  /**
   * Sets the value of the Origin-Host AVP, of type DiameterIdentity.
   * 
   * @throws IllegalStateException
   *             if setOriginHost has already been called
   */
  void setOriginHost(DiameterIdentity originHost);

  /**
   * Returns true if the Origin-Realm AVP is present in the message.
   */
  boolean hasOriginRealm();

  /**
   * Returns the value of the Origin-Realm AVP, of type DiameterIdentity. A
   * return value of null implies that the AVP has not been set.
   */
  DiameterIdentity getOriginRealm();

  /**
   * Sets the value of the Origin-Realm AVP, of type DiameterIdentity.
   * 
   * @throws IllegalStateException
   *             if setOriginRealm has already been called
   */
  void setOriginRealm(DiameterIdentity originRealm);

  /**
   * Returns true if the Destination-Realm AVP is present in the message.
   */
  boolean hasDestinationRealm();

  /**
   * Returns the value of the Destination-Realm AVP, of type DiameterIdentity.
   * A return value of null implies that the AVP has not been set.
   */
  DiameterIdentity getDestinationRealm();

  /**
   * Sets the value of the Destination-Realm AVP, of type DiameterIdentity.
   * 
   * @throws IllegalStateException
   *             if setDestinationRealm has already been called
   */
  void setDestinationRealm(DiameterIdentity destinationRealm);

  /**
   * Returns true if the Destination-Host AVP is present in the message.
   */
  boolean hasDestinationHost();

  /**
   * Returns the value of the Destination-Host AVP, of type DiameterIdentity.
   * A return value of null implies that the AVP has not been set.
   */
  DiameterIdentity getDestinationHost();

  /**
   * Sets the value of the Destination-Host AVP, of type DiameterIdentity.
   * 
   * @throws IllegalStateException
   *             if setDestinationHost has already been called
   */
  void setDestinationHost(DiameterIdentity destinationHost);

  /**
   * Creates and returns a deep copy of this Diameter message.
   * 
   * @return a deep copy of this message.
   */
  Object clone();

  /**
   * Returns the set of extension AVPs. The returned array contains the
   * extension AVPs in the order they appear in the message. A return value of
   * null implies that no extensions AVPs have been set.
   */
  DiameterAvp[] getExtensionAvps();

  /**
   * Sets the set of extension AVPs with all the values in the given array.
   * The AVPs will be added to message in the order in which they appear in
   * the array.
   * 
   * Note: the array must not be altered by the caller following this call,
   * and getExtensionAvps() is not guaranteed to return the same array
   * instance, e.g. an "==" check would fail.
   * 
   * @throws AvpNotAllowedException
   *             if an AVP is encountered of a type already known to this
   *             class (i.e. an AVP for which get/set methods already appear
   *             in this class)
   * @throws IllegalStateException
   *             if setExtensionAvps has already been called
   */
  void setExtensionAvps(DiameterAvp... avps) throws AvpNotAllowedException;

}
