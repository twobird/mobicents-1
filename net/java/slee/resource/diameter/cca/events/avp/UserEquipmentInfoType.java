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

package net.java.slee.resource.diameter.cca.events.avp;

import java.io.StreamCorruptedException;

import net.java.slee.resource.diameter.base.events.avp.Enumerated;

/**
 *<pre> <b>8.50. User-Equipment-Info-Type AVP</b>
 *
 *
 *   The User-Equipment-Info-Type AVP is of type Enumerated  (AVP Code
 *   459) and defines the type of user equipment information contained in
 *   the User-Equipment-Info-Value AVP.
 *
 *   This specification defines the following user equipment types.
 *   However, new User-Equipment-Info-Type values can be assigned by an
 *   IANA designated expert, as defined in section 12.
 *
 *   <b>IMEISV                          0</b>
 *      The identifier contains the International Mobile Equipment
 *      Identifier and Software Version in the international IMEISV format
 *      according to 3GPP TS 23.003 [3GPPIMEI].
 *
 *   <b>MAC                             1</b>
 *      The 48-bit MAC address is formatted as described in [RAD802.1X].
 *
 *   <b>EUI64                           2</b>
 *      The 64-bit identifier used to identify hardware instance of the
 *      product, as defined in [EUI64].
 *
 *   <b>MODIFIED_EUI64                  3</b>
 *      There are a number of types of terminals that have identifiers
 *      other than IMEI, IEEE 802 MACs, or EUI-64.  These identifiers can
 *      be converted to modified EUI-64 format as described in [IPv6Addr]
 *      or by using some other methods referred to in the service-specific
 *      documentation.
 *      
 *      </pre>
 *  
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public enum UserEquipmentInfoType implements Enumerated {

  IMEISV(0), MAC(1), EUI64(2), MODIFIED_EUI64(3);

  private int value = -1;

  private UserEquipmentInfoType(int value) {
    this.value = value;
  }

  private Object readResolve() throws StreamCorruptedException {
    try {
      return fromInt(value);
    }
    catch (IllegalArgumentException iae) {
      throw new StreamCorruptedException("Invalid internal state found: " + value);
    }
  }

  public static UserEquipmentInfoType fromInt(int presumableValue) throws IllegalArgumentException
  {
    switch (presumableValue) {
    case 0:
      return IMEISV;
    case 1:
      return MAC;
    case 2:
      return EUI64;
    case 3:
      return MODIFIED_EUI64;

    default:
      throw new IllegalArgumentException();
    }
  }

  public int getValue() {
    return this.value;
  }
}
