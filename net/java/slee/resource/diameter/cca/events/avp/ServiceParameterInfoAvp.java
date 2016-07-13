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
 *<pre> <b>8.43. Service-Parameter-Info AVP</b>
 *
 *
 *   The Service-Parameter-Info AVP (AVP Code 440) is of type Grouped and
 *   contains service-specific information used for price calculation or
 *   rating.  The Service-Parameter-Type AVP defines the service parameter
 *   type, and the Service-Parameter-Value AVP contains the parameter
 *   value.  The actual contents of these AVPs are not within the scope of
 *   this document and SHOULD be defined in another Diameter application,
 *   in standards written by other standardization bodies, or in service-
 *   specific documentation.
 *
 *   In the case of an unknown service request (e.g., unknown Service-
 *   Parameter-Type), the corresponding answer message MUST contain the
 *   error code DIAMETER_RATING_FAILED.  A Credit-Control-Answer message
 *   with this error MUST contain one or more Failed-AVP AVPs containing
 *   the Service-Parameter-Info AVPs that caused the failure.
 *
 *   It is defined as follows (per the grouped-avp-def of RFC 3588
 *   [DIAMBASE]):
 *
 *      Service-Parameter-Info ::= < AVP Header: 440 >
 *                                 { Service-Parameter-Type }
 *                                 { Service-Parameter-Value }
 *                                 </pre>
 *      
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public interface ServiceParameterInfoAvp extends GroupedAvp {

  /**
   * Returns the value of the Service-Parameter-Type AVP, of type Unsigned32.
   * 
   * @return
   */
  long getServiceParameterType();

  /**
   * Returns the value of the Service-Parameter-Value AVP, of type
   * OctetString.
   * 
   * @return
   */
  byte[] getServiceParameterValue();

  /**
   * Returns true if the Service-Parameter-Type AVP is present in the message.
   * 
   * @return
   */
  boolean hasServiceParameterType();

  /**
   * Returns true if the Service-Parameter-Value AVP is present in the
   * message.
   * 
   * @return
   */
  boolean hasServiceParameterValue();

  /**
   * Sets the value of the Service-Parameter-Type AVP, of type Unsigned32.
   * 
   * @param serviceParameterType
   */
  void setServiceParameterType(long serviceParameterType);

  /**
   * Sets the value of the Service-Parameter-Value AVP, of type OctetString.
   * 
   * @param serviceParameterValue
   */
  void setServiceParameterValue(byte[] serviceParameterValue);

}
