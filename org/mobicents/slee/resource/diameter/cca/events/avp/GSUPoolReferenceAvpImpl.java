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

package org.mobicents.slee.resource.diameter.cca.events.avp;

import net.java.slee.resource.diameter.cca.events.avp.CcUnitType;
import net.java.slee.resource.diameter.cca.events.avp.CreditControlAVPCodes;
import net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp;
import net.java.slee.resource.diameter.cca.events.avp.UnitValueAvp;

import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * Start time:16:03:57 2008-11-10<br>
 * Project: mobicents-diameter-parent<br>
 * Implementation of AVP: {@link GSUPoolReferenceAvp}
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class GSUPoolReferenceAvpImpl extends GroupedAvpImpl implements GSUPoolReferenceAvp {

  public GSUPoolReferenceAvpImpl() {
    super();
  }

  public GSUPoolReferenceAvpImpl(int code, long vendorId, int mnd, int prt, byte[] value) {
    super(code, vendorId, mnd, prt, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp#getCreditControlUnitType()
   */
  public CcUnitType getCreditControlUnitType() {
    return (CcUnitType) getAvpAsEnumerated(CreditControlAVPCodes.CC_Unit_Type, CcUnitType.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp#getGSUPoolIdentifier()
   */
  public long getGSUPoolIdentifier() {
    return getAvpAsUnsigned32(CreditControlAVPCodes.G_S_U_Pool_Identifier);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp#getUnitValue()
   */
  public UnitValueAvp getUnitValue() {
    return (UnitValueAvp) getAvpAsCustom(CreditControlAVPCodes.Unit_Value, UnitValueAvpImpl.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp#hasCreditControlUnitType()
   */
  public boolean hasCreditControlUnitType() {
    return hasAvp(CreditControlAVPCodes.CC_Unit_Type);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp#hasGSUPoolIdentifier()
   */
  public boolean hasGSUPoolIdentifier() {
    return hasAvp(CreditControlAVPCodes.G_S_U_Pool_Identifier);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp#hasUnitValue()
   */
  public boolean hasUnitValue() {
    return hasAvp(CreditControlAVPCodes.Unit_Value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp#setCreditControlUnitType
   * (net.java.slee.resource.diameter.cca.events.avp.CcUnitType)
   */
  public void setCreditControlUnitType(CcUnitType ccUnitType) {
    addAvp(CreditControlAVPCodes.CC_Unit_Type, ccUnitType.getValue());
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp#setGSUPoolIdentifier(long)
   */
  public void setGSUPoolIdentifier(long gsuPoolIdentifier) {
    addAvp(CreditControlAVPCodes.G_S_U_Pool_Identifier, gsuPoolIdentifier);
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp#setUnitValue
   * (net.java.slee.resource.diameter.cca.events.avp.UnitValueAvp)
   */
  public void setUnitValue(UnitValueAvp unitValue) {
    addAvp(CreditControlAVPCodes.Unit_Value, unitValue.byteArrayValue());
  }

}
