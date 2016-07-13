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

package net.java.slee.resource.diameter.s6a.events.avp;

import net.java.slee.resource.diameter.base.events.avp.GroupedAvp;

/**
 * Defines an interface representing the E-UTRAN-Vector grouped AVP type.
 * From the Diameter S6a Reference Point Protocol Details (3GPP TS 29.272 V9.6.0) specification:
 * 
 * <pre>
 * 7.3.18 E-UTRAN-Vector
 * 
 * The E-UTRAN-Vector AVP is of type Grouped. This AVP shall contain an E-UTRAN Vector.
 * 
 * AVP format:
 * E-UTRAN-Vector ::= < AVP header: 1414 10415 >
 *                    [ Item-Number ]
 *                    { RAND }
 *                    { XRES }
 *                    { AUTN }
 *                    { KASME }
 *                   *[ AVP ]
 * </pre>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:richard.good@smilecoms.com"> Richard Good </a>
 * @author <a href="mailto:paul.carter-brown@smilecoms.com"> Paul Carter-Brown </a>
 */
public interface EUTRANVectorAvp extends GroupedAvp {

  public long getItemNumber();
  public void setItemNumber(long itemNumber);
  public boolean hasItemNumber();

  public boolean hasRAND();
  public byte[] getRAND();
  public void setRAND(byte[] RAND);

  public boolean hasXRES();
  public byte[] getXRES();
  public void setXRES(byte[] XRES);

  public boolean hasAUTN();
  public byte[] getAUTN();
  public void setAUTN(byte[] AUTN);

  public boolean hasKASME();
  public byte[] getKASME();
  public void setKASME(byte[] KASME);

}
