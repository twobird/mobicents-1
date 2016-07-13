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

import net.java.slee.resource.diameter.base.events.avp.GroupedAvp;

/**
 * <pre>
 *  &lt;b&gt;8.30. G-S-U-Pool-Reference AVP&lt;/b&gt;
 * 
 * 
 *   The G-S-U-Pool-Reference AVP (AVP Code 457) is of type Grouped.  It
 *   is used in the Credit-Control-Answer message, and associates the
 *   Granted-Service-Unit AVP within which it appears with a credit pool
 *   within the session.
 * 
 *   The G-S-U-Pool-Identifier AVP specifies the credit pool from which
 *   credit is drawn for this unit type.
 * 
 *   The CC-Unit-Type AVP specifies the type of units for which credit is
 *   pooled.
 * 
 *   The Unit-Value AVP specifies the multiplier, which converts between
 *   service units of type CC-Unit-Type and abstract service units within
 *   the credit pool (and thus to service units of any other service or
 *   rating group associated with the same pool).
 * 
 *   The G-S-U-Pool-Reference AVP is defined as follows (per the grouped-
 *   avp-def of RFC 3588 [DIAMBASE]):
 * 
 *      G-S-U-Pool-Reference    ::= &lt; AVP Header: 457 &gt;
 *                                  { G-S-U-Pool-Identifier }
 *                                  { CC-Unit-Type }
 *                                  { Unit-Value }
 * </pre>
 *      
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public interface GSUPoolReferenceAvp extends GroupedAvp {
  /**
   * Returns the value of the CC-Unit-Type AVP, of type Enumerated. <br>
   * See: {@link CcUnitType}.
   * 
   * @return
   */
  CcUnitType getCreditControlUnitType();

  /**
   * Returns the value of the G-S-U-Pool-Identifier AVP, of type Unsigned32.
   * 
   * @return
   */
  long getGSUPoolIdentifier();

  /**
   * Returns the value of the Unit-Value AVP, of type Grouped.
   * 
   * @return
   */
  UnitValueAvp getUnitValue();

  /**
   * Returns true if the CC-Unit-Type AVP is present in the message.
   * 
   * @return
   */
  boolean hasCreditControlUnitType();

  /**
   * Returns true if the G-S-U-Pool-Identifier AVP is present in the message.
   * 
   * @return
   */
  boolean hasGSUPoolIdentifier();

  /**
   * Returns true if the Unit-Value AVP is present in the message.
   * 
   * @return
   */
  boolean hasUnitValue();

  /**
   * Sets the value of the CC-Unit-Type AVP, of type Enumerated. <br>
   * See: {@link CcUnitType}.
   * 
   * @param ccUnitType
   */
  void setCreditControlUnitType(CcUnitType ccUnitType);

  /**
   * Sets the value of the G-S-U-Pool-Identifier AVP, of type Unsigned32.
   * 
   * @param gsuPoolIdentifier
   */
  void setGSUPoolIdentifier(long gsuPoolIdentifier);

  /**
   * Sets the value of the Unit-Value AVP, of type Grouped. <br>
   * See
   * 
   * @param unitValue
   */
  void setUnitValue(UnitValueAvp unitValue);

}
