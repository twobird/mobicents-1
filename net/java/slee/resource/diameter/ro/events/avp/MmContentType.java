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
 * Defines an interface representing the MM-Content-Type grouped AVP type.<br>
 * <br>
 * From the Diameter Ro Reference Point Protocol Details (3GPP TS 32.299 V7.1.0) specification: 
 * <pre>
 * 7.2.63 MM-Content-Type AVP 
 * The MM-Content-Type AVP (AVP code 1203) is of type Grouped and indicates the overall content type of the MM content
 * and includes information about all the contents of an MM. 
 * 
 * It has the following ABNF grammar: 
 *  MM-Content-Type ::= AVP Header: 1203 
 *      [ Type-Number ] 
 *      [ Additional-Type-Information ] 
 *      [ Content-Size ] 
 *    * [ Additional-Content-Information ]
 * </pre>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public interface MmContentType extends GroupedAvp {

  /**
   * Returns the set of Additional-Content-Information AVPs. The returned array contains the AVPs in the order they appear in the message. A return value of null implies that no Additional-Content-Information AVPs have been set. The elements in the given array are AdditionalContentInformation objects.
   */
  abstract AdditionalContentInformation[] getAdditionalContentInformations();

  /**
   * Returns the value of the Additional-Type-Information AVP, of type UTF8String. A return value of null implies that the AVP has not been set.
   */
  abstract String getAdditionalTypeInformation();

  /**
   * Returns the value of the Content-Size AVP, of type Unsigned32. A return value of null implies that the AVP has not been set.
   */
  abstract long getContentSize();

  /**
   * Returns the value of the Type-Number AVP, of type Integer32. A return value of null implies that the AVP has not been set.
   */
  abstract int getTypeNumber();

  /**
   * Returns true if the Additional-Type-Information AVP is present in the message.
   */
  abstract boolean hasAdditionalTypeInformation();

  /**
   * Returns true if the Content-Size AVP is present in the message.
   */
  abstract boolean hasContentSize();

  /**
   * Returns true if the Type-Number AVP is present in the message.
   */
  abstract boolean hasTypeNumber();

  /**
   * Sets a single Additional-Content-Information AVP in the message, of type Grouped.
   */
  abstract void setAdditionalContentInformation(AdditionalContentInformation additionalContentInformation);

  /**
   * Sets the set of Additional-Content-Information AVPs, with all the values in the given array. The AVPs will be added to message in the order in which they appear in the array. Note: the array must not be altered by the caller following this call, and getAdditionalContentInformations() is not guaranteed to return the same array instance, e.g. an "==" check would fail.
   */
  abstract void setAdditionalContentInformations(AdditionalContentInformation[] additionalContentInformations);

  /**
   * Sets the value of the Additional-Type-Information AVP, of type UTF8String.
   */
  abstract void setAdditionalTypeInformation(String additionalTypeInformation);

  /**
   * Sets the value of the Content-Size AVP, of type Unsigned32.
   */
  abstract void setContentSize(long contentSize);

  /**
   * Sets the value of the Type-Number AVP, of type Integer32.
   */
  abstract void setTypeNumber(int typeNumber);

}
