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

import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;
import net.java.slee.resource.diameter.base.events.avp.ProxyInfoAvp;
import net.java.slee.resource.diameter.base.events.avp.ReAuthRequestType;

/**
 * Defines an interface representing the Re-Auth-Request command.
 * 
 * From the Diameter Base Protocol (rfc3588.txt) specification:
 * 
 * <pre>
 * 8.3.1.  Re-Auth-Request
 * 
 *    The Re-Auth-Request (RAR), indicated by the Command-Code set to 258
 *    and the message flags' 'R' bit set, may be sent by any server to the
 *    access device that is providing session service, to request that the
 *    user be re-authenticated and/or re-authorized.
 * 
 *    Message Format
 * 
 *       &lt;Re-Auth-Request&gt;  ::= &lt; Diameter Header: 258, REQ, PXY &gt;
 *                  &lt; Session-Id &gt;
 *                  { Origin-Host }
 *                  { Origin-Realm }
 *                  { Destination-Realm }
 *                  { Destination-Host }
 *                  { Auth-Application-Id }
 *                  { Re-Auth-Request-Type }
 *                  [ User-Name ]
 *                  [ Origin-State-Id ]
 *                * [ Proxy-Info ]
 *                * [ Route-Record ]
 *                * [ AVP ]
 * </pre>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public interface ReAuthRequest extends DiameterMessage {

  static final int commandCode = 258;

  /**
   * Returns true if the Auth-Application-Id AVP is present in the message.
   */
  boolean hasAuthApplicationId();

  /**
   * Returns the value of the Auth-Application-Id AVP, of type Unsigned32. Use
   * {@link #hasAuthApplicationId()} to check the existence of this AVP.
   * 
   * @return the value of the Auth-Application-Id AVP
   * @throws IllegalStateException
   *             if the Auth-Application-Id AVP has not been set on this
   *             message
   */
  long getAuthApplicationId();

  /**
   * Sets the value of the Auth-Application-Id AVP, of type Unsigned32.
   * 
   * @throws IllegalStateException
   *             if setAuthApplicationId has already been called
   */
  void setAuthApplicationId(long authApplicationId);

  /**
   * Returns true if the Re-Auth-Request-Type AVP is present in the message.
   */
  boolean hasReAuthRequestType();

  /**
   * Returns the value of the Re-Auth-Request-Type AVP, of type Enumerated.
   * 
   * @return the value of the Re-Auth-Request-Type AVP or null if it has not
   *         been set on this message
   */
  ReAuthRequestType getReAuthRequestType();

  /**
   * Sets the value of the Re-Auth-Request-Type AVP, of type Enumerated.
   * 
   * @throws IllegalStateException
   *             if setReAuthRequestType has already been called
   */
  void setReAuthRequestType(ReAuthRequestType reAuthRequestType);

  /**
   * Returns true if the User-Name AVP is present in the message.
   */
  boolean hasUserName();

  /**
   * Returns the value of the User-Name AVP, of type UTF8String.
   * 
   * @return the value of the User-Name AVP or null if it has not been set on
   *         this message
   */
  String getUserName();

  /**
   * Sets the value of the User-Name AVP, of type UTF8String.
   * 
   * @throws IllegalStateException
   *             if setUserName has already been called
   */
  void setUserName(String userName);

  /**
   * Returns true if the Origin-State-Id AVP is present in the message.
   */
  boolean hasOriginStateId();

  /**
   * Returns the value of the Origin-State-Id AVP, of type Unsigned32. Use
   * {@link #hasOriginStateId()} to check the existence of this AVP.
   * 
   * @return the value of the Origin-State-Id AVP
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
   * Returns the set of Proxy-Info AVPs. The returned array contains the AVPs
   * in the order they appear in the message. A return value of null implies
   * that no Proxy-Info AVPs have been set. The elements in the given array
   * are ProxyInfo objects.
   */
  ProxyInfoAvp[] getProxyInfos();

  /**
   * Sets a single Proxy-Info AVP in the message, of type Grouped.
   * 
   * @throws IllegalStateException
   *             if setProxyInfo or setProxyInfos has already been called
   */
  void setProxyInfo(ProxyInfoAvp proxyInfo);

  /**
   * Sets the set of Proxy-Info AVPs, with all the values in the given array.
   * The AVPs will be added to message in the order in which they appear in
   * the array.
   * 
   * Note: the array must not be altered by the caller following this call,
   * and getProxyInfos() is not guaranteed to return the same array instance,
   * e.g. an "==" check would fail.
   * 
   * @throws IllegalStateException
   *             if setProxyInfo or setProxyInfos has already been called
   */
  void setProxyInfos(ProxyInfoAvp[] proxyInfos);

  /**
   * Returns the set of Route-Record AVPs. The returned array contains the
   * AVPs in the order they appear in the message. A return value of null
   * implies that no Route-Record AVPs have been set. The elements in the
   * given array are DiameterIdentity objects.
   */
  DiameterIdentity[] getRouteRecords();

  /**
   * Sets a single Route-Record AVP in the message, of type DiameterIdentity.
   * 
   * @throws IllegalStateException
   *             if setRouteRecord or setRouteRecords has already been called
   */
  void setRouteRecord(DiameterIdentity routeRecord);

  /**
   * Sets the set of Route-Record AVPs, with all the values in the given
   * array. The AVPs will be added to message in the order in which they
   * appear in the array.
   * 
   * Note: the array must not be altered by the caller following this call,
   * and getRouteRecords() is not guaranteed to return the same array
   * instance, e.g. an "==" check would fail.
   * 
   * @throws IllegalStateException
   *             if setRouteRecord or setRouteRecords has already been called
   */
  void setRouteRecords(DiameterIdentity[] routeRecords);

}
