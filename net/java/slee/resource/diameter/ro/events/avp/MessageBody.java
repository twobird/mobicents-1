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

package net.java.slee.resource.diameter.ro.events.avp;

import net.java.slee.resource.diameter.base.events.avp.GroupedAvp;

/**
 * Defines an interface representing the Message-Body grouped AVP type.<br>
 * <br>
 * From the Diameter Ro Reference Point Protocol Details (3GPP TS 32.299 V7.1.0) specification: 
 * <pre>
 * 7.2.58 Message-Body AVP 
 * The Message-Body AVP (AVP Code 889) is of type Grouped AVP and holds information about the message bodies including
 * user-to-user data. 
 * 
 * It has the following ABNF grammar: 
 *  Message-Body ::= AVP Header: 889 
 *      [ Content-Type ] 
 *      [ Content-Length ] 
 *      [ Content-Disposition ] 
 *      [ Originator ]
 * </pre>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public interface MessageBody extends GroupedAvp {

  /**
   * Returns the value of the Content-Disposition AVP, of type UTF8String. A return value of null implies that the AVP has not been set.
   */
  abstract String getContentDisposition();

  /**
   * Returns the value of the Content-Length AVP, of type Unsigned32. A return value of null implies that the AVP has not been set.
   */
  abstract long getContentLength();

  /**
   * Returns the value of the Content-Type AVP, of type UTF8String. A return value of null implies that the AVP has not been set.
   */
  abstract String getContentType();

  /**
   * Returns the value of the Originator AVP, of type Enumerated. A return value of null implies that the AVP has not been set.
   */
  abstract Originator getOriginator();

  /**
   * Returns true if the Content-Disposition AVP is present in the message.
   */
  abstract boolean hasContentDisposition();

  /**
   * Returns true if the Content-Length AVP is present in the message.
   */
  abstract boolean hasContentLength();

  /**
   * Returns true if the Content-Type AVP is present in the message.
   */
  abstract boolean hasContentType();

  /**
   * Returns true if the Originator AVP is present in the message.
   */
  abstract boolean hasOriginator();

  /**
   * Sets the value of the Content-Disposition AVP, of type UTF8String.
   */
  abstract void setContentDisposition(String contentDisposition);

  /**
   * Sets the value of the Content-Length AVP, of type Unsigned32.
   */
  abstract void setContentLength(long contentLength);

  /**
   * Sets the value of the Content-Type AVP, of type UTF8String.
   */
  abstract void setContentType(String contentType);

  /**
   * Sets the value of the Originator AVP, of type Enumerated.
   */
  abstract void setOriginator(Originator originator);

}
