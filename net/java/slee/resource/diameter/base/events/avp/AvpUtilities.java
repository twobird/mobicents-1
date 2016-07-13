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

package net.java.slee.resource.diameter.base.events.avp;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Message;
import org.jdiameter.api.validation.Dictionary;
import org.jdiameter.api.validation.MessageRepresentation;
import org.jdiameter.client.api.parser.ParseException;
import org.jdiameter.client.impl.parser.MessageParser;
import org.jdiameter.common.impl.validation.DictionaryImpl;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.diameter.dictionary.AvpRepresentation;
import org.mobicents.slee.resource.diameter.base.events.avp.DiameterAvpImpl;
import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * This class contains some handy methods for working with AVPs in messages and other AVPs.
 * It requires AVP dictionary to be loaded.
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class AvpUtilities {

  private static transient final Logger logger = Logger.getLogger(AvpUtilities.class);

  private static long _DEFAULT_VENDOR_ID = 0L;

  private static boolean _DEFAULT_MANDATORY = true;
  private static boolean _DEFAULT_PROTECTED = false;

  private static Dictionary dictionary;

  static {
    // Just so we have it
    parser = new MessageParser();
    dictionary = DictionaryImpl.INSTANCE;
  }

  public static void setParser(MessageParser singletonParser) {
    parser = singletonParser;
  }

  public static Dictionary getDictionary() {
    return dictionary;
  }

  public static void setDictionary(Dictionary dictionary) {
    AvpUtilities.dictionary = dictionary;
  }

  public static MessageParser getParser() {
    return parser;
  }

  public static boolean hasAvp(int avpCode, long vendorId, AvpSet set) {
    AvpSet inner = set.getAvps(avpCode, vendorId);

    return ((inner.getAvp(avpCode, vendorId) != null) || (set.getAvp(avpCode, vendorId) != null));
  }

  /**
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode
   * @param vendorId
   * @param set
   * @throws AvpNotAllowedException 
   */
  private static void performPreAddOperations(Object parent, int avpCode, long vendorId, AvpSet set) throws AvpNotAllowedException {
    if (!dictionary.isEnabled()) {
      // no need to proceed any further.. no validation
      return;
    }

    if(parent instanceof Message) {
      Message msg = (Message) parent;

      MessageRepresentation msgRep = dictionary.getMessage(msg.getCommandCode(), msg.getApplicationId(), msg.isRequest());

      // if we don't know anything about this message, let's just move on..
      if(msgRep == null) {
        if(logger.isDebugEnabled()) {
          logger.debug("Unable to find message in dictionary, skipping validation. (Command Code: " + msg.getCommandCode() + ", Application-Id: " + msg.getApplicationId() + ")");
        }
        return;
      }
      if (!msgRep.isAllowed(avpCode, vendorId)) {
        throw new AvpNotAllowedException("Avp defined by code: " + avpCode + ", vendorId: " + vendorId + " is not allowed in message - code: " + msg.getCommandCode() + ", appId: "
            + msg.getApplicationId() + ", isRequest: " + msg.isRequest(), avpCode, vendorId);
      }

      if (msgRep.isCountValidForMultiplicity(msg.getAvps(), avpCode, vendorId,1)) { // 1 --> +1
        // its ok.
        return;
      }
      else {
        throw new AvpNotAllowedException("Avp not allowed, count exceeded.", avpCode, vendorId);
      }
    }
    else if (parent instanceof GroupedAvp) {
      GroupedAvpImpl gAvp = (GroupedAvpImpl) parent;

      org.jdiameter.api.validation.AvpRepresentation parentAvpRep = dictionary.getAvp(gAvp.getCode(), gAvp.getVendorId());

      // if we don't know anything about this avp, let's just move on..
      if(parentAvpRep == null) {
        if(logger.isDebugEnabled()) {
          logger.debug("Unable to find parent AVP in dictionary, skipping validation. (AVP Code: " + gAvp.getCode() + ", Vendor-Id: " + gAvp.getVendorId() + ")");
        }
        return;
      }
      if (!parentAvpRep.isAllowed(avpCode, vendorId)) {
        throw new AvpNotAllowedException("AVP with Code '" + avpCode + "' and Vendor-Id '" + vendorId + "' is not allowed as a child of AVP with Code '" + parentAvpRep.getCode() + "' and Vendor-Id '"
            + parentAvpRep.getVendorId() + "'.", avpCode, vendorId);
      }

      // Now we get the actual AVP to add representation...
      org.jdiameter.api.validation.AvpRepresentation avpRep = dictionary.getAvp(avpCode, vendorId);

      // ..to check if it can be added (multiplicity) to the parent.
      if (avpRep.isCountValidForMultiplicity(gAvp.getGenericData(), 1)) { // 1 --> +1
        // its ok.
        return;
      }
      else {
        throw new AvpNotAllowedException("Avp not allowed, count exceeded.", avpCode, vendorId);
      }
    }
  }

  /**
   * Returns an {@link AvpRepresentation} of the AVP with the given code and given Vendor-Id, if found.
   * 
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @return
   */
  private static AvpRepresentation getAvpRepresentation(int avpCode, long vendorId) {
    return AvpDictionary.INSTANCE.getAvp(avpCode);
  }

  /**
   * Adds AVP to {@link AvpSet} as String (Octet or UTF-8) with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param isOctetString if true added as OctetString type, otherwise as UTF8String
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsString(Object parent, int avpCode, boolean isOctetString, AvpSet set, String value) {
    setAvpAsString(parent, avpCode, _DEFAULT_VENDOR_ID, isOctetString, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as String (Octet or UTF-8) with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param isOctetString if true added as OctetString type, otherwise as UTF8String
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsString(Object parent, int avpCode, long vendorId, boolean isOctetString, AvpSet set, String value) {
    AvpRepresentation rep = getAvpRepresentation(avpCode, vendorId);

    if (rep != null) {
      setAvpAsString(parent, avpCode, vendorId, isOctetString, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsString(parent, avpCode, vendorId, isOctetString, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as String (Octet or UTF-8) with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param isOctetString if true added as OctetString type, otherwise as UTF8String
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsString(Object parent, int avpCode, long vendorId, boolean isOctetString, AvpSet set, boolean isMandatory, boolean isProtected, String value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    switch(avpCode) {
      case Avp.SESSION_ID:
        // (...) the Session-Id SHOULD appear immediately following the Diameter Header
        set.insertAvp(0, avpCode, value, vendorId, isMandatory, isProtected, isOctetString);
        break;
      case Avp.ORIGIN_HOST:
      case Avp.ORIGIN_REALM:
      case Avp.DESTINATION_HOST:
      case Avp.DESTINATION_REALM:
        // This AVP SHOULD be placed as close to the Diameter header as possible.
        Avp firstAvp = set.size() > 0 ? set.getAvpByIndex(0) : null;
        int index = (firstAvp != null && firstAvp.getCode() == Avp.SESSION_ID) ? 1 : 0;

        set.insertAvp(index, avpCode, value, vendorId, isMandatory, isProtected, isOctetString);
        break;
      default:
        set.addAvp(avpCode, value, vendorId, isMandatory, isProtected, isOctetString);
        break;
    }
  }

  public static byte[] getAvpAsOctetString(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      return avp != null ? avp.getOctetString() : null;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type OctetString.", e);
      }
      return null;
    }
  }

  public static byte[][] getAvpsAsOctetString(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      byte[][] values = new byte[avpSet.size()][];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getOctetString();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type OctetString.", e);
      }
      return new byte[0][];
    }
  }

  public static byte[] getAvpAsOctetString(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      return avp != null ? avp.getOctetString() : null;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type OctetString.", e);
      }
      return null;
    }
  }

  public static byte[][] getAvpsAsOctetString(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      byte[][] values = new byte[avpSet.size()][];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getOctetString();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type OctetString.", e);
      }
      return new byte[0][];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as OctetString with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsOctetString(Object parent, int avpCode, AvpSet set, String value) {
    setAvpAsOctetString(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as OctetString with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsOctetString(Object parent, int avpCode, long vendorId, AvpSet set, String value) {
    AvpRepresentation rep = getAvpRepresentation(avpCode, vendorId);

    if (rep != null) {
      setAvpAsOctetString(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsOctetString(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as OctetString with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsOctetString(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, String value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    set.addAvp(avpCode, value, vendorId, isMandatory, isProtected, true);
  }

  public static String getAvpAsUTF8String(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      return avp != null ? avp.getUTF8String() : null;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type UTF8String.", e);
      }
      return null;
    }
  }

  public static String[] getAvpsAsUTF8String(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      String[] values = new String[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getUTF8String();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type UTF8String.", e);
      }
      return new String[0];
    }
  }

  public static String getAvpAsUTF8String(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      return avp != null ? avp.getUTF8String() : null;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type UTF8String.", e);
      }
      return null;
    }
  }

  public static String[] getAvpsAsUTF8String(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      String[] values = new String[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getUTF8String();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type UTF8String.", e);
      }
      return new String[0];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as UTF8String with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsUTF8String(Object parent, int avpCode, AvpSet set, String value) {
    setAvpAsUTF8String(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as UTF8String with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsUTF8String(Object parent, int avpCode, long vendorId, AvpSet set, String value) {
    AvpRepresentation rep = getAvpRepresentation(avpCode, vendorId);

    if (rep != null) {
      setAvpAsUTF8String(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsUTF8String(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as OctetString with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsUTF8String(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, String value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    switch(avpCode) {
      case Avp.SESSION_ID:
        // (...) the Session-Id SHOULD appear immediately following the Diameter Header
        set.insertAvp(0, avpCode, value, vendorId, isMandatory, isProtected, false);
        break;
      case Avp.ORIGIN_HOST:
      case Avp.ORIGIN_REALM:
      case Avp.DESTINATION_HOST:
      case Avp.DESTINATION_REALM:
        // This AVP SHOULD be placed as close to the Diameter header as possible.
        Avp firstAvp = set.size() > 0 ? set.getAvpByIndex(0) : null;
        int index = (firstAvp != null && firstAvp.getCode() == Avp.SESSION_ID) ? 1 : 0;

        set.insertAvp(index, avpCode, value, vendorId, isMandatory, isProtected, false);
        break;
      default:
        set.addAvp(avpCode, value, vendorId, isMandatory, isProtected, false);
        break;
    }
  }

  public static long getAvpAsUnsigned32(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      if(avp == null) {
        if(logger.isDebugEnabled()) { 
          logger.debug("Unable to retrieve AVP with code " + avpCode + ". Returning " + Long.MIN_VALUE);
        }
        return Long.MIN_VALUE;
      }
      return avp.getUnsigned32();
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Unsigned32.", e);
      }
      return Long.MIN_VALUE;
    }
  }

  public static long[] getAvpsAsUnsigned32(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      long[] values = new long[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getUnsigned32();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Unsigned32.", e);
      }
      return new long[0];
    }
  }

  public static long getAvpAsUnsigned32(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      if(avp == null) {
        if(logger.isDebugEnabled()) { 
          logger.debug("Unable to retrieve AVP with code " + avpCode + " and Vendor-Id " + vendorId + ". Returning " + Long.MIN_VALUE);
        }
        return Long.MIN_VALUE;
      }
      return avp.getUnsigned32();
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Unsigned32.", e);
      }
      return Long.MIN_VALUE;
    }
  }

  public static long[] getAvpsAsUnsigned32(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      long[] values = new long[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getUnsigned32();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Unsigned32.", e);
      }
      return new long[0];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Unsigned32 with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsUnsigned32(Object parent, int avpCode, AvpSet set, long value) {
    setAvpAsUnsigned32(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as Unsigned32 with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsUnsigned32(Object parent, int avpCode, long vendorId, AvpSet set, long value) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if (rep != null) {
      setAvpAsUnsigned32(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsUnsigned32(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Unsigned32 with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsUnsigned32(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, long value) { 
    performPreAddOperations(parent, avpCode, vendorId, set);

    set.addAvp(avpCode, value, vendorId, isMandatory, isProtected, true);
  }

  public static long getAvpAsUnsigned64(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      if(avp == null) {
        if(logger.isDebugEnabled()) { 
          logger.debug("Unable to retrieve AVP with code " + avpCode + ". Returning " + Long.MIN_VALUE);
        }
        return Long.MIN_VALUE;
      }
      return avp.getUnsigned64();
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Unsigned64.", e);
      }
      return Long.MIN_VALUE;
    }
  }

  public static long[] getAvpsAsUnsigned64(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      long[] values = new long[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getUnsigned64();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Unsigned64.", e);
      }
      return new long[0];
    }
  }

  public static long getAvpAsUnsigned64(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      if(avp == null) {
        if(logger.isDebugEnabled()) { 
          logger.debug("Unable to retrieve AVP with code " + avpCode + " and Vendor-Id " + vendorId + ". Returning " + Long.MIN_VALUE);
        }
        return Long.MIN_VALUE;
      }
      return avp.getUnsigned64();
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Unsigned64.", e);
      }
      return Long.MIN_VALUE;
    }
  }

  public static long[] getAvpsAsUnsigned64(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      long[] values = new long[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getUnsigned64();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Unsigned64.", e);
      }
      return new long[0];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Unsigned64 with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsUnsigned64(Object parent, int avpCode, AvpSet set, long value) {
    setAvpAsUnsigned64(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as Unsigned64 with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsUnsigned64(Object parent, int avpCode, long vendorId, AvpSet set, long value) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if (rep != null) {
      setAvpAsUnsigned64(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsUnsigned64(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Unsigned64 with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsUnsigned64(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, long value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    set.addAvp(avpCode, value, vendorId, isMandatory, isProtected, false);
  }

  public static int getAvpAsInteger32(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      if(avp == null) {
        if(logger.isDebugEnabled()) { 
          logger.debug("Unable to retrieve AVP with code " + avpCode + ". Returning " + Integer.MIN_VALUE);
        }
        return Integer.MIN_VALUE;
      }
      return avp.getInteger32();
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Integer32.", e);
      }
      return Integer.MIN_VALUE;
    }
  }

  public static int[] getAvpsAsInteger32(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      int[] values = new int[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getInteger32();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Integer32.", e);
      }
      return new int[0];
    }
  }

  public static int getAvpAsInteger32(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      if(avp == null) {
        if(logger.isDebugEnabled()) { 
          logger.debug("Unable to retrieve AVP with code " + avpCode + " and Vendor-Id " + vendorId + ". Returning " + Integer.MIN_VALUE);
        }
        return Integer.MIN_VALUE;
      }
      return avp.getInteger32();
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Integer32.", e);
      }
      return Integer.MIN_VALUE;
    }
  }

  public static int[] getAvpsAsInteger32(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      int[] values = new int[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getInteger32();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Integer32.", e);
      }
      return new int[0];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Integer32 with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsInteger32(Object parent, int avpCode, AvpSet set, int value) {
    setAvpAsInteger32(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as Integer32 with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsInteger32(Object parent, int avpCode, long vendorId, AvpSet set, int value) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if (rep != null) {
      setAvpAsInteger32(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsInteger32(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Integer32 with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsInteger32(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, int value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    set.addAvp(avpCode, value, vendorId, isMandatory, isProtected);
  }

  public static long getAvpAsInteger64(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      if(avp == null) {
        if(logger.isDebugEnabled()) { 
          logger.debug("Unable to retrieve AVP with code " + avpCode + ". Returning " + Long.MIN_VALUE);
        }
        return Long.MIN_VALUE;
      }
      return avp.getInteger64();
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Integer64.", e);
      }
      return Long.MIN_VALUE;
    }
  }

  public static long[] getAvpsAsInteger64(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      long[] values = new long[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getInteger64();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Integer64.", e);
      }
      return new long[0];
    }
  }

  public static long getAvpAsInteger64(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      if(avp == null) {
        if(logger.isDebugEnabled()) { 
          logger.debug("Unable to retrieve AVP with code " + avpCode + " and Vendor-Id " + vendorId + ". Returning " + Long.MIN_VALUE);
        }
        return Long.MIN_VALUE;
      }
      return avp.getInteger64();
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Integer64.", e);
      }
      return Long.MIN_VALUE;
    }
  }

  public static long[] getAvpsAsInteger64(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      long[] values = new long[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getInteger64();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Integer64.", e);
      }
      return new long[0];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Integer64 with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsInteger64(Object parent, int avpCode, AvpSet set, long value) {
    setAvpAsInteger64(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as Integer64 with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsInteger64(Object parent, int avpCode, long vendorId, AvpSet set, long value) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if (rep != null) {
      setAvpAsInteger64(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsInteger64(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Integer64 with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsInteger64(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, long value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    set.addAvp(avpCode, value, vendorId, isMandatory, isProtected, false);
  }

  public static float getAvpAsFloat32(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      return avp != null ? avp.getFloat32() : Float.MIN_VALUE;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Float32.", e);
      }
      return Float.MIN_VALUE;
    }
  }

  public static float[] getAvpsAsFloat32(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      float[] values = new float[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getFloat32();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Float32.", e);
      }
      return new float[0];
    }
  }

  public static float getAvpAsFloat32(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      return avp != null ? avp.getFloat32() : Float.MIN_VALUE;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Float32.", e);
      }
      return Float.MIN_VALUE;
    }
  }

  public static float[] getAvpsAsFloat32(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      float[] values = new float[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getFloat32();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Float32.", e);
      }
      return new float[0];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Float32 with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsFloat32(Object parent, int avpCode, AvpSet set, float value) {
    setAvpAsFloat32(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as Float32 with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsFloat32(Object parent, int avpCode, long vendorId, AvpSet set, float value) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if (rep != null) {
      setAvpAsFloat32(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsFloat32(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Float32 with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsFloat32(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, float value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    set.addAvp(avpCode, value, vendorId, isMandatory, isProtected);
  }

  public static double getAvpAsFloat64(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      return avp != null ? avp.getFloat64() : Double.MIN_VALUE;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Float64.", e);
      }
      return Double.MIN_VALUE;
    }
  }

  public static double[] getAvpsAsFloat64(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      double[] values = new double[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getFloat32();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Float64.", e);
      }
      return new double[0];
    }
  }

  public static double getAvpAsFloat64(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      return avp != null ? avp.getFloat64() : Double.MIN_VALUE;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Float64.", e);
      }
      return Double.MIN_VALUE;
    }
  }

  public static double[] getAvpsAsFloat64(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      double[] values = new double[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getFloat32();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Float64.", e);
      }
      return new double[0];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Float64 with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsFloat64(Object parent, int avpCode, AvpSet set, double value) {
    setAvpAsFloat64(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as Float64 with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsFloat64(Object parent, int avpCode, long vendorId, AvpSet set, double value) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if (rep != null) {
      setAvpAsFloat64(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsFloat64(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Float64 with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsFloat64(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, double value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    set.addAvp(avpCode, value, vendorId, isMandatory, isProtected);
  }

  public static Date getAvpAsTime(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      return avp != null ? avp.getTime() : null;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Time.", e);
      }
      return  null;
    }
  }

  public static Date[] getAvpsAsTime(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      Date[] values = new Date[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getTime();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Float64.", e);
      }
      return new Date[0];
    }
  }

  public static Date getAvpAsTime(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      return avp != null ? avp.getTime() : null;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Time.", e);
      }
      return null;
    }
  }

  public static Date[] getAvpsAsTime(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      Date[] values = new Date[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getTime();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Float64.", e);
      }
      return new Date[0];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Time with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsTime(Object parent, int avpCode, AvpSet set, Date value) {
    setAvpAsTime(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as Time with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsTime(Object parent, int avpCode, long vendorId, AvpSet set, Date value) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if (rep != null) {
      setAvpAsTime(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsTime(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Time with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsTime(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, Date value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    set.addAvp(avpCode, value, vendorId, isMandatory, isProtected);
  }

  public static byte[] getAvpAsGrouped(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      return avp != null ? avp.getRawData() : null;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Grouped.", e);
      }
      return  null;
    }
  }

  public static byte[][] getAvpsAsGrouped(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      byte[][] values = new byte[avpSet.size()][];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getRawData();
      }

      return values;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Grouped.", e);
      }
      return new byte[0][];
    }
  }

  public static byte[] getAvpAsGrouped(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      return avp != null ? avp.getRawData() : null;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Grouped.", e);
      }
      return null;
    }
  }

  public static byte[][] getAvpsAsGrouped(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      byte[][] values = new byte[avpSet.size()][];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getRawData();
      }

      return values;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Grouped.", e);
      }
      return new byte[0][];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Grouped with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static AvpSet setAvpAsGrouped(Object parent, int avpCode, AvpSet set, DiameterAvp[] childs) {
    return setAvpAsGrouped(parent, avpCode, _DEFAULT_VENDOR_ID, set, childs);
  }

  /**
   * Adds AVP to {@link AvpSet} as Grouped with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static AvpSet setAvpAsGrouped(Object parent, int avpCode, long vendorId, AvpSet set, DiameterAvp[] childs) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if (rep != null) {
      return setAvpAsGrouped(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), childs);
    }
    else {
      return setAvpAsGrouped(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, childs);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as Grouped with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static AvpSet setAvpAsGrouped(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, DiameterAvp[] childs) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    AvpSet grouped = set.addGroupedAvp(avpCode, vendorId, isMandatory, isProtected);

    for (DiameterAvp child : childs) {
      grouped.addAvp(child.getCode(), child.byteArrayValue(), child.getVendorId(), child.getMandatoryRule() != DiameterAvp.FLAG_RULE_MUSTNOT, child.getProtectedRule() == DiameterAvp.FLAG_RULE_MUST);
    }

    return grouped;
  }

  public static byte[] getAvpAsRaw(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      return avp != null ? avp.getRaw() : null;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Raw.", e);
      }
      return null;
    }
  }

  public static byte[][] getAvpsAsRaw(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      byte[][] values = new byte[avpSet.size()][];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getRaw();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Raw.", e);
      }
      return new byte[0][];
    }
  }

  public static byte[] getAvpAsRaw(int avpCode, long vendorId, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);
      return avp != null ? avp.getRaw() : null;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Raw.", e);
      }
      return null;
    }
  }

  public static byte[][] getAvpsAsRaw(int avpCode, long vendorId, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      byte[][] values = new byte[avpSet.size()][];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = avp.getRaw();
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Raw.", e);
      }
      return new byte[0][];
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as raw data with the given code and Base Vendor-Id (0).
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param set the Vendor-Id of the AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsRaw(Object parent, int avpCode, AvpSet set, byte[] value) {
    setAvpAsRaw(parent, avpCode, _DEFAULT_VENDOR_ID, set, value);
  }

  /**
   * Adds AVP to {@link AvpSet} as raw data with the given code and given Vendor-Id.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param value the value of the AVP to add
   */
  public static void setAvpAsRaw(Object parent, int avpCode, long vendorId, AvpSet set, byte[] value) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if (rep != null) {
      setAvpAsRaw(parent, avpCode, vendorId, set, rep.isMandatory(), rep.isProtected(), value);
    }
    else {
      setAvpAsRaw(parent, avpCode, vendorId, set, _DEFAULT_MANDATORY, _DEFAULT_PROTECTED, value);
    }
  }

  /**
   * Adds AVP to {@link AvpSet} as raw data with the given code and given Vendor-Id plus defined mandatory and protected flags.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP
   * @param vendorId the Vendor-Id of the AVP
   * @param set the AvpSet to add AVP
   * @param isMandatory the value for the mandatory bit
   * @param isProtected the value for the protected bit
   * @param value the value of the AVP to add
   */
  public static void setAvpAsRaw(Object parent, int avpCode, long vendorId, AvpSet set, boolean isMandatory, boolean isProtected, byte[] value) {
    performPreAddOperations(parent, avpCode, vendorId, set);

    switch(avpCode) {
      case Avp.SESSION_ID:
        // (...) the Session-Id SHOULD appear immediately following the Diameter Header
        set.insertAvp(0, avpCode, value, vendorId, isMandatory, isProtected);
        break;
      case Avp.ORIGIN_HOST:
      case Avp.ORIGIN_REALM:
      case Avp.DESTINATION_HOST:
      case Avp.DESTINATION_REALM:
        // This AVP SHOULD be placed as close to the Diameter header as possible.
        Avp firstAvp = set.size() > 0 ? set.getAvpByIndex(0) : null;
        int index = (firstAvp != null && firstAvp.getCode() == Avp.SESSION_ID) ? 1 : 0;

        set.insertAvp(index, avpCode, value, vendorId, isMandatory, isProtected);
        break;
      default:
        set.addAvp(avpCode, value, vendorId, isMandatory, isProtected);
        break;
    }
  }

  public static Object getAvpAsCustom(int avpCode, AvpSet set, Class clazz) {
    return getAvpAsCustom(avpCode, 0L, set, clazz);
  }

  public static Object[] getAvpsAsCustom(int avpCode, AvpSet set, Class clazz) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      Object array = Array.newInstance(clazz, avpSet.size());
      int i = 0;

      Constructor c = clazz.getConstructor(int.class, long.class, int.class, int.class, byte[].class);

      AvpRepresentation rep = null;

      for(Avp avp : avpSet) {
        rep = AvpDictionary.INSTANCE.getAvp(avpCode, avp.getVendorId());

        Array.set(array, i++, c.newInstance(rep.getCode(), rep.getVendorId(), rep.getRuleMandatoryAsInt(), rep.getRuleProtectedAsInt(), avp.getRawData()));
      }

      return (Object[]) array;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Custom (Class '" + clazz.getName() + "').", e);
      }
      return (Object[]) Array.newInstance(clazz, 0);
    }
  }

  public static Object getAvpAsCustom(int avpCode, long vendorId, AvpSet set, Class clazz) {
    try {
      Avp avp = set.getAvp(avpCode, vendorId);

      if (avp != null) {
        AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

        Constructor c = null;

        c = clazz.getConstructor(int.class, long.class, int.class, int.class, byte[].class);
        return c.newInstance(rep.getCode(), rep.getVendorId(), rep.getRuleMandatoryAsInt(), rep.getRuleProtectedAsInt(), avp.getRawData());
      }
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Custom (Class '" + clazz.getName() + "').", e);
      }
    }

    return null;
  }

  public static Object[] getAvpsAsCustom(int avpCode, long vendorId, AvpSet set, Class clazz) {
    try {
      AvpSet avpSet = set.getAvps(avpCode, vendorId);

      Object array = Array.newInstance(clazz, avpSet.size());
      int i = 0;

      AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

      Constructor c = clazz.getConstructor(int.class, long.class, int.class, int.class, byte[].class);

      for(Avp avp : avpSet) {
        Array.set(array, i++, c.newInstance(rep.getCode(), rep.getVendorId(), rep.getRuleMandatoryAsInt(), rep.getRuleProtectedAsInt(), avp.getRawData()));
      }

      return (Object[]) array;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Custom (Class '" + clazz.getName() + "').", e);
      }
      return (Object[]) Array.newInstance(clazz, 0);
    }
  }

  public static void addAvp(Object parent, String avpName, AvpSet set, Object avp) {
    AvpRepresentation rep = AvpDictionary.INSTANCE.getAvp(avpName);

    if(rep != null) {
      addAvp(parent, rep.getCode(), rep.getVendorId(), set, avp);
    }
    else {
      throw new IllegalArgumentException("Unknown AVP with name '" + avpName + "'. Unable to add it.");
    }
  }

  public static void addAvp(Object parent, int avpCode, AvpSet set, Object avp) {
    addAvp(parent, avpCode, 0L, set, avp);
  }

  /**
   * Method for adding AVP with given code and Vendor-Id to the given set.
   * 
   * @param parent the Message/Grouped AVP where AVP will be added to, for validation purposes. if null, no validation is performed.
   * @param avpCode the code of the AVP to look for
   * @param vendorId the Vendor-Id of the AVP to be added
   * @param avp the AVP object
   * @param set the AvpSet where to add the AVP
   */
  public static void addAvp(Object parent, int avpCode, long vendorId, AvpSet set, Object avp) {
    AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if(avpRep != null) {
      DiameterAvpType avpType = DiameterAvpType.fromString(avpRep.getType());

      boolean isMandatoryAvp = !(avpRep.getRuleMandatory().equals("mustnot") || avpRep.getRuleMandatory().equals("shouldnot"));
      boolean isProtectedAvp = avpRep.getRuleProtected().equals("must");

      if(avp instanceof byte[]) {
        setAvpAsRaw(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (byte[]) avp);
      }
      else {
        switch (avpType.getType()) {
          case DiameterAvpType._ADDRESS:
          case DiameterAvpType._DIAMETER_IDENTITY:
          case DiameterAvpType._DIAMETER_URI:
          case DiameterAvpType._IP_FILTER_RULE:
          case DiameterAvpType._OCTET_STRING:
          case DiameterAvpType._QOS_FILTER_RULE:
            if(avp instanceof Address) {
              // issue: http://code.google.com/p/mobicents/issues/detail?id=2758 
              setAvpAsRaw(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp,((Address)avp).encode());
            }
            else {
              setAvpAsOctetString(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, avp.toString());
            }
            break;
          case DiameterAvpType._ENUMERATED:
          case DiameterAvpType._INTEGER_32:
            setAvpAsInteger32(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (Integer) avp);        
            break;
          case DiameterAvpType._FLOAT_32:
            setAvpAsFloat32(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (Float) avp);        
            break;
          case DiameterAvpType._FLOAT_64:
            setAvpAsFloat64(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (Float) avp);        
            break;
          case DiameterAvpType._GROUPED:
            setAvpAsGrouped(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (DiameterAvp[]) avp);
            break;
          case DiameterAvpType._INTEGER_64:
            setAvpAsInteger64(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (Long) avp);
            break;
          case DiameterAvpType._TIME:
            setAvpAsTime(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (Date) avp);
            break;
          case DiameterAvpType._UNSIGNED_32:
            setAvpAsUnsigned32(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (Long) avp);
            break;
          case DiameterAvpType._UNSIGNED_64:
            setAvpAsUnsigned64(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (Long) avp);
            break;
          case DiameterAvpType._UTF8_STRING:
            setAvpAsUTF8String(parent, avpCode, vendorId, set, isMandatoryAvp, isProtectedAvp, (String) avp);
            break;
        }
      }
    }
  }

  // Some special types getter/setter

  public static DiameterIdentity getAvpAsDiameterIdentity(int avpCode, AvpSet set) {
    try {
      Avp avp = set.getAvp(avpCode);
      return avp != null ? new DiameterIdentity(avp.getDiameterIdentity()) : null;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type OctetString.", e);
      }
      return null;
    }
  }

  public static DiameterIdentity[] getAvpsAsDiameterIdentity(int avpCode, AvpSet set) {
    try {
      AvpSet avpSet = set.getAvps(avpCode);

      DiameterIdentity[] values = new DiameterIdentity[avpSet.size()];
      int i = 0;

      for(Avp avp : avpSet) {
        values[i++] = new DiameterIdentity(avp.getDiameterIdentity());
      }

      return values;
    }
    catch (AvpDataException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type DiameterIdentity.", e);
      }
      return new DiameterIdentity[0];
    }
  }

  public static DiameterIdentity getAvpAsDiameterIdentity(int avpCode, long vendorId, AvpSet set) {
    String value = getAvpAsUTF8String(avpCode, vendorId, set);

    return value != null ? new DiameterIdentity(value) : null;
  }

  public static DiameterIdentity[] getAvpsAsDiameterIdentity(int avpCode, long vendorId, AvpSet set) {
    List<DiameterIdentity> values = new ArrayList<DiameterIdentity>();

    for(String value : getAvpsAsUTF8String(avpCode, vendorId, set)) {
      if(value != null) {
        values.add(new DiameterIdentity(value));
      }
    }

    return values.toArray(new DiameterIdentity[0]);
  }

  public static DiameterURI getAvpAsDiameterURI(int avpCode, AvpSet set) {
    try {
      String value = getAvpAsUTF8String(avpCode, set);

      return value != null ? new DiameterURI(value) : null;
    }
    catch (URISyntaxException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type DiameterURI.", e);
      }
      return null;
    }
  }

  public static DiameterURI[] getAvpsAsDiameterURI(int avpCode, AvpSet set) {
    try {
      List<DiameterURI> values = new ArrayList<DiameterURI>();

      for(String value : getAvpsAsUTF8String(avpCode, set)) {
        if(value != null) {
          values.add(new DiameterURI(value));
        }
      }

      return values.toArray(new DiameterURI[0]);
    }
    catch (URISyntaxException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type DiameterURI.", e);
      }
      return null;
    }
  }

  public static DiameterURI getAvpAsDiameterURI(int avpCode, long vendorId, AvpSet set) {
    try {
      String value = getAvpAsUTF8String(avpCode, vendorId, set);

      return value != null ? new DiameterURI(value) : null;
    }
    catch (URISyntaxException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type DiameterURI.", e);
      }
      return null;
    }
  }

  public static DiameterURI[] getAvpsAsDiameterURI(int avpCode, long vendorId, AvpSet set) {
    try {
      List<DiameterURI> values = new ArrayList<DiameterURI>();

      for(String value : getAvpsAsUTF8String(avpCode, vendorId, set)) {
        if(value != null) {
          values.add(new DiameterURI(value));
        }
      }

      return values.toArray(new DiameterURI[0]);
    }
    catch (URISyntaxException e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type DiameterURI.", e);
      }
      return null;
    }
  }

  public static Address getAvpAsAddress(int avpCode, AvpSet set) {
    try {
      byte[] value = getAvpAsRaw(avpCode, set);

      return value != null ? Address.decode(value) : null;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Address.", e);
      }
      return null;
    }
  }

  public static Address[] getAvpsAsAddress(int avpCode, AvpSet set) {
    List<Address> values = new ArrayList<Address>();

    for(byte[] value : getAvpsAsRaw(avpCode, set)) {
      if(value != null) {
        values.add(Address.decode(value));
      }
    }

    return values.toArray(new Address[0]);
  }

  public static Address getAvpAsAddress(int avpCode, long vendorId, AvpSet set) {
    try {
      byte[] value = getAvpAsRaw(avpCode, vendorId, set);

      return value != null ? Address.decode(value) : null;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Address.", e);
      }
      return null;
    }
  }

  public static Address[] getAvpsAsAddress(int avpCode, long vendorId, AvpSet set) {
    List<Address> values = new ArrayList<Address>();

    for(byte[] value : getAvpsAsRaw(avpCode, vendorId, set)) {
      if(value != null) {
        values.add(Address.decode(value));
      }
    }

    return values.toArray(new Address[0]);
  }

  public static Object getAvpAsEnumerated(int avpCode, AvpSet set, Class clazz) {
    try {
      int value = getAvpAsInteger32(avpCode, set);

      return clazz.getMethod("fromInt", int.class).invoke(null, value);
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Enumerated.", e);
      }
      return null;
    }
  }

  public static Object[] getAvpsAsEnumerated(int avpCode, AvpSet set, Class clazz) {
    Object array = Array.newInstance(clazz, 0);

    try {
      int[] values = getAvpsAsInteger32(avpCode, set);

      array = Array.newInstance(clazz, values.length);
      int i = 0;

      for(int value : values) {
        Array.set(array, i++, clazz.getMethod("fromInt", int.class).invoke(null, value));
      }
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Enumerated.", e);
      }
    }

    return (Object[]) array;
  }

  public static Object getAvpAsEnumerated(int avpCode, long vendorId, AvpSet set, Class clazz) {
    try {
      int value = getAvpAsInteger32(avpCode, vendorId, set);

      return clazz.getMethod("fromInt", int.class).invoke(null, value);
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type Enumerated.", e);
      }
      return null;
    }
  }

  public static Object[] getAvpsAsEnumerated(int avpCode, long vendorId, AvpSet set, Class clazz) {
    Object array = Array.newInstance(clazz, 0);

    try {
      int[] values = getAvpsAsInteger32(avpCode, vendorId, set);

      array = Array.newInstance(clazz, values.length);
      int i = 0;

      for(int value : values) {
        Array.set(array, i++, clazz.getMethod("fromInt", int.class).invoke(null, value));
      }
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type Enumerated.", e);
      }
    }

    return (Object[]) array;
  }

  public static IPFilterRule getAvpAsIPFilterRule(int avpCode, AvpSet set) {
    try {
      String value = getAvpAsUTF8String(avpCode, set);

      return value != null ? new IPFilterRule(value) : null;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " as type IPFilterRule.", e);
      }
      return null;
    }
  }

  public static IPFilterRule[] getAvpsAsIPFilterRule(int avpCode, AvpSet set) {
    List<IPFilterRule> values = new ArrayList<IPFilterRule>();

    for(String value : getAvpsAsUTF8String(avpCode, set)) {
      if(value != null) {
        values.add(new IPFilterRule(value));
      }
    }

    return values.toArray(new IPFilterRule[0]);
  }

  public static IPFilterRule getAvpAsIPFilterRule(int avpCode, long vendorId, AvpSet set) {
    try {
      String value = getAvpAsUTF8String(avpCode, vendorId, set);

      return value != null ? new IPFilterRule(value) : null;
    }
    catch (Exception e) {
      if(logger.isDebugEnabled()) { 
        logger.debug("Failed to obtain AVP with code " + avpCode + " and Vendor-Id " + vendorId + " as type IPFilterRule.", e);
      }
      return null;
    }
  }

  public static IPFilterRule[] getAvpsAsIPFilterRule(int avpCode, long vendorId, AvpSet set) {
    List<IPFilterRule> values = new ArrayList<IPFilterRule>();

    for(String value : getAvpsAsUTF8String(avpCode, set)) {
      if(value != null) {
        values.add(new IPFilterRule(value));
      }
    }

    return values.toArray(new IPFilterRule[0]);
  }

  /**
   * Method for removing AVP with given code.
   * 
   * @param avpCode the code of the AVP to be removed
   * @param set the AvpSet to remove the AVP from
   */
  public static void removeAvp(int avpCode, AvpSet set) {
    set.removeAvp(avpCode);
  }

  /**
   * Method for removing AVP with given code.
   * 
   * @param avpCode the code of the AVP to be removed
   * @param vendorId the vendor id of the AVP to be removed
   * @param set the AvpSet to remove the AVP from
   */
  public static void removeAvp(int avpCode, long vendorId, AvpSet set) {
    set.removeAvp(avpCode,vendorId);
  }

  /**
   * Method for obtaining AVP with given code and any Vendor-Id.
   * 
   * @param avpCode the code of the AVP to look for
   * @param set the set of AVPs where to look
   * 
   * @return an AVP with the given code, or null if none is present.
   */
  public static Object getAvp(int avpCode, AvpSet set) {
    Avp avp = set.getAvp(avpCode);

    if(avp != null) {
      return getAvp(avp.getCode(), avp.getVendorId(), set);
    }

    return null;
  }

  public static Object getAvp(String avpName, AvpSet set) {
    AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(avpName);

    if(avpRep != null) {
      return getAvp(avpRep.getCode(), avpRep.getVendorId(), set);
    }

    return null;
  }

  /**
   * Method for obtaining AVP with given code and Vendor-Id.
   * 
   * @param avpCode the code of the AVP to look for
   * @param vendorId the Vendor-Id of the AVP to look for
   * @param set the set of AVPs where to look
   * 
   * @return an AVP with the given code and Vendor-Id, or null if none is present.
   */
  public static Object getAvp(int avpCode, long vendorId, AvpSet set) {
    AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    if(avpRep != null) {
      DiameterAvpType avpType = DiameterAvpType.fromString(avpRep.getType());

      switch (avpType.getType()) {
        case DiameterAvpType._ADDRESS:
          return Address.decode(getAvpAsRaw(avpCode, vendorId, set));
        case DiameterAvpType._DIAMETER_IDENTITY:
          return new DiameterIdentity(getAvpAsUTF8String(avpCode, vendorId, set));
        case DiameterAvpType._DIAMETER_URI:
          try {
            return new DiameterURI(getAvpAsUTF8String(avpCode, vendorId, set));
          }
          catch (URISyntaxException e) {
            logger.error("Failed to return AVP with code " + avpCode + " of type DiameterURI as it is malformed: " + getAvpAsOctetString(avpCode, vendorId, set), e);
            return null;
          }
        case DiameterAvpType._IP_FILTER_RULE:
          return new IPFilterRule(getAvpAsUTF8String(avpCode, vendorId, set));
        case DiameterAvpType._OCTET_STRING:
          return getAvpAsOctetString(avpCode, vendorId, set);
        case DiameterAvpType._QOS_FILTER_RULE:
          return getAvpAsOctetString(avpCode, vendorId, set);
        case DiameterAvpType._ENUMERATED:
        case DiameterAvpType._INTEGER_32:
          return getAvpAsInteger32(avpCode, vendorId, set);
        case DiameterAvpType._FLOAT_32:
          return getAvpAsFloat32(avpCode, vendorId, set);
        case DiameterAvpType._FLOAT_64:
          return getAvpAsFloat64(avpCode, vendorId, set);
        case DiameterAvpType._GROUPED:
          return getAvpAsGrouped(avpCode, vendorId, set);
        case DiameterAvpType._INTEGER_64:
          return getAvpAsInteger64(avpCode, vendorId, set);
        case DiameterAvpType._TIME:
          return getAvpAsTime(avpCode, vendorId, set);
        case DiameterAvpType._UNSIGNED_32:
          return getAvpAsUnsigned32(avpCode, vendorId, set);
        case DiameterAvpType._UNSIGNED_64:
          return getAvpAsUnsigned64(avpCode, vendorId, set);
        case DiameterAvpType._UTF8_STRING:
          return getAvpAsUTF8String(avpCode, vendorId, set);
        default:
          return getAvpAsRaw(avpCode, vendorId, set);
      }
    }

    return null;
  }

  public static void addAvp(DiameterAvp avp, AvpSet set) {
    addAvpInternal(avp, set);
  }

  private static void addAvpInternal(DiameterAvp avp, AvpSet set) {
    int avpCode = avp.getCode();
    if (avp.getType() == DiameterAvpType.GROUPED) {
      AvpSet groupedAvp = null;
      if (avpCode == Avp.VENDOR_SPECIFIC_APPLICATION_ID) {
        // This AVP SHOULD be placed as close to the Diameter header as possible.
        Avp firstAvp = set.size() > 0 ? set.getAvpByIndex(0) : null;
        int index = (firstAvp != null && firstAvp.getCode() == Avp.SESSION_ID) ? 1 : 0;

        groupedAvp = set.insertGroupedAvp(index, avpCode, avp.getVendorId(), avp.getMandatoryRule() != DiameterAvp.FLAG_RULE_MUSTNOT, avp.getProtectedRule() == DiameterAvp.FLAG_RULE_MUST);
      }
      else {
        groupedAvp = set.addGroupedAvp(avpCode, avp.getVendorId(), avp.getMandatoryRule() != DiameterAvp.FLAG_RULE_MUSTNOT, avp.getProtectedRule() == DiameterAvp.FLAG_RULE_MUST);
      }

      for (DiameterAvp subAvp : ((GroupedAvp) avp).getExtensionAvps()) {
        addAvpInternal(subAvp, groupedAvp);
      }
    }
    else {
      switch (avpCode) {
        case Avp.SESSION_ID:          
          // (...) the Session-Id SHOULD appear immediately following the Diameter Header
          set.insertAvp(0, avpCode, avp.byteArrayValue(), avp.getVendorId(), avp.getMandatoryRule() != DiameterAvp.FLAG_RULE_MUSTNOT, avp.getProtectedRule() == DiameterAvp.FLAG_RULE_MUST);
          break;
        case Avp.ORIGIN_HOST: 
        case Avp.ORIGIN_REALM: 
        case Avp.DESTINATION_HOST: 
        case Avp.DESTINATION_REALM: 
        case Avp.VENDOR_SPECIFIC_APPLICATION_ID:
          // This AVP SHOULD be placed as close to the Diameter header as possible.
          Avp firstAvp = set.size() > 0 ? set.getAvpByIndex(0) : null;
          int index = (firstAvp != null && firstAvp.getCode() == Avp.SESSION_ID) ? 1 : 0;
          set.insertAvp(index, avpCode, avp.byteArrayValue(), avp.getVendorId(), avp.getMandatoryRule() != DiameterAvp.FLAG_RULE_MUSTNOT, avp.getProtectedRule() == DiameterAvp.FLAG_RULE_MUST);        
          break;
        default:
          set.addAvp(avpCode, avp.byteArrayValue(), avp.getVendorId(), avp.getMandatoryRule() != DiameterAvp.FLAG_RULE_MUSTNOT, avp.getProtectedRule() == DiameterAvp.FLAG_RULE_MUST);
          break;
      }
    }
  }

  // AVP Factories Methods -----------------------------------------------

  private static MessageParser parser;

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, net.java.slee.resource.diameter.base.events.avp.DiameterAvp[])
   */
  public static DiameterAvp createAvp(int avpCode, DiameterAvp[] avps) throws AvpNotAllowedException {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, avps);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, net.java.slee.resource.diameter.base.events.avp.DiameterAvp[])
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, DiameterAvp[] avps) throws AvpNotAllowedException {
    GroupedAvpImpl avp = (GroupedAvpImpl) AvpUtilities.createAvp(avpCode, vendorId, GroupedAvpImpl.class);

    avp.setExtensionAvps(avps);

    return avp;
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, byte[])
   */
  public static DiameterAvp createAvp(int avpCode, byte[] value) {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, value);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, byte[])
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, byte[] value) {
    return createAvpInternal(vendorId, avpCode, value);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int)
   */
  public static DiameterAvp createAvp(int avpCode, int value) {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, value);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, int)
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, int value) {
    return createAvpInternal(vendorId, avpCode, parser.int32ToBytes(value));    
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, long)
   */
  public static DiameterAvp createAvp(int avpCode, long value) {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, value);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, long)
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, long value) {
    DiameterAvpType avpType = getAvpType(avpCode, vendorId);

    byte[] byteValue = null;

    if(avpType.getType() ==  DiameterAvpType._INTEGER_64  || avpType.getType() ==  DiameterAvpType._UNSIGNED_64) {
      byteValue = parser.int64ToBytes(value);
    }
    else if (avpType.getType() ==  DiameterAvpType._UNSIGNED_32) {
      byteValue = parser.intU32ToBytes(value);
    }
    else {
      return null;
    }

    return createAvpInternal(vendorId, avpCode, byteValue);    
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, float)
   */
  public static DiameterAvp createAvp(int avpCode, float value) {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, value);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, float)
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, float value) {
    return createAvpInternal(vendorId, avpCode, parser.float32ToBytes(value));
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, double)
   */
  public static DiameterAvp createAvp(int avpCode, double value) {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, value);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, double)
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, double value) {
    return createAvpInternal(vendorId, avpCode, parser.float64ToBytes(value));
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, java.net.InetAddress)
   */
  public static DiameterAvp createAvp(int avpCode, InetAddress value) {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, value);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, java.net.InetAddress)
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, InetAddress value) {
    return createAvpInternal(vendorId, avpCode, parser.addressToBytes(value));
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, java.util.Date)
   */
  public static DiameterAvp createAvp(int avpCode, Date value) {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, value);

  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, java.util.Date)
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, Date value) {
    return createAvpInternal(vendorId, avpCode, parser.dateToBytes(value));
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, java.lang.String)
   */
  public static DiameterAvp createAvp(int avpCode, String value) {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, value);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, java.lang.String)
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, String value) {
    DiameterAvpType avpType = getAvpType(avpCode, vendorId);

    byte[] byteValue = null;

    try {
      if(avpType.getType() ==  DiameterAvpType._OCTET_STRING) {
        byteValue = parser.octetStringToBytes(value);
      }
      else if (avpType.getType() ==  DiameterAvpType._UTF8_STRING) {
        byteValue = parser.utf8StringToBytes(value);
      }
      else {
        return null;
      }
    }
    catch (Exception e) {
      logger.error("Failed to create AVP.", e);
      return null;
    }

    return createAvpInternal(vendorId, avpCode, byteValue);    
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, net.java.slee.resource.diameter.base.events.avp.Enumerated)
   */
  public static DiameterAvp createAvp(int avpCode, Enumerated value) {
    return createAvp(avpCode, _DEFAULT_VENDOR_ID, value);
  }

  /*
   * (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterAvpFactory#createAvp(int, int, net.java.slee.resource.diameter.base.events.avp.Enumerated)
   */
  public static DiameterAvp createAvp(int avpCode, long vendorId, Enumerated value) {
    try {
      return createAvpInternal(vendorId, avpCode, parser.objectToBytes(value));
    }
    catch (ParseException e) {
      logger.error("Failed to create AVP.", e);
      return null;
    }
  }

  private static DiameterAvpType getAvpType(int avpCode, long vendorId) {
    AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

    return avpRep != null ? DiameterAvpType.fromString(avpRep.getType()) : null;
  }

  public static DiameterAvp createAvp(int avpCode, Class avpImplClass) {
    return createAvp(avpCode, 0L, null, avpImplClass);
  }

  public static DiameterAvp createAvp(int avpCode, DiameterAvp[] childAVPs, Class avpImplClass) {
    return createAvp(avpCode, 0L, childAVPs, avpImplClass);    
  }

  public static DiameterAvp createAvp(int avpCode, long vendorId, Class avpImplClass) {
    return createAvp(avpCode, vendorId, null, avpImplClass);
  }

  public static DiameterAvp createAvp(int avpCode, long vendorId, DiameterAvp[] childAVPs, Class avpImplClass) {
    return createAvp(avpCode, vendorId, null, childAVPs, avpImplClass);
  }

  public static DiameterAvp createAvp(int avpCode, long vendorId, byte[] value, DiameterAvp[] childAVPs, Class avpImplClass) {
    try {
      AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(avpCode, vendorId);

      int mandatoryAvp = avpRep.getRuleMandatoryAsInt();
      int protectedAvp = avpRep.getRuleProtectedAsInt();

      if(avpImplClass == DiameterAvpImpl.class) {
        Constructor avpConstructor = avpImplClass.getConstructor(int.class, long.class, int.class, int.class, byte[].class, DiameterAvpType.class);
        return (DiameterAvp) avpConstructor.newInstance(avpCode, vendorId, mandatoryAvp, protectedAvp, value != null ? value : new byte[]{}, DiameterAvpType.fromString(avpRep.getType()));
      }
      else {
        Constructor avpConstructor = avpImplClass.getConstructor(int.class, long.class, int.class, int.class, byte[].class);
        GroupedAvp returnAvp = (GroupedAvp) avpConstructor.newInstance(avpCode, vendorId, mandatoryAvp, protectedAvp, value != null ? value : new byte[]{});

        returnAvp.setExtensionAvps(childAVPs);

        return returnAvp;
      }
    }
    catch (NoSuchMethodException e) {
      logger.error("Failure while trying to create AVP with Code " + avpCode + " and Vendor-Id " + vendorId, e);
    }
    catch (IllegalArgumentException e) {
      logger.error("Failure while trying to create AVP with Code " + avpCode + " and Vendor-Id " + vendorId, e);
    }
    catch (InstantiationException e) {
      logger.error("Failure while trying to create AVP with Code " + avpCode + " and Vendor-Id " + vendorId, e);
    }
    catch (IllegalAccessException e) {
      logger.error("Failure while trying to create AVP with Code " + avpCode + " and Vendor-Id " + vendorId, e);
    }
    catch (InvocationTargetException e) {
      logger.error("Failure while trying to create AVP with Code " + avpCode + " and Vendor-Id " + vendorId, e);
    }

    return null;
  }

  private static DiameterAvp createAvpInternal(long vendorID, int avpCode, byte[] value) {
    return createAvp(avpCode, vendorID, value, null, DiameterAvpImpl.class);
  }

}
