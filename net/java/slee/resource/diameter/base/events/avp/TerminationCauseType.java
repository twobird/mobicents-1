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

import java.io.Serializable;
import java.io.StreamCorruptedException;

/**
 * Java class to represent the TerminationCause enumerated type.
 * <p/>
 * Documentation from the relevant specification:
 * <p/>
 * The Termination-Cause AVP (AVP Code 295) is of type Enumerated, and is used to indicate the reason why a session was terminated on the access device. 
 *
 * @author Open Cloud
 */
public class TerminationCauseType implements Serializable, Enumerated {

  private static final long serialVersionUID = 1L;

  public static final int _DIAMETER_LOGOUT = 1;
  public static final int _DIAMETER_SERVICE_NOT_PROVIDED = 2;
  public static final int _DIAMETER_BAD_ANSWER = 3;
  public static final int _DIAMETER_ADMINISTRATIVE = 4;
  public static final int _DIAMETER_LINK_BROKEN = 5;
  public static final int _DIAMETER_AUTH_EXPIRED = 6;
  public static final int _DIAMETER_USER_MOVED = 7;
  public static final int _DIAMETER_SESSION_TIMEOUT = 8;

  /**
   * The user initiated a disconnect 
   */
  public static final TerminationCauseType DIAMETER_LOGOUT = new TerminationCauseType(_DIAMETER_LOGOUT);

  /**
   * This value is used when the user disconnected prior to the receipt of the authorization answer message. 
   */
  public static final TerminationCauseType DIAMETER_SERVICE_NOT_PROVIDED = new TerminationCauseType(_DIAMETER_SERVICE_NOT_PROVIDED);

  /**
   * This value indicates that the authorization answer received by the access device was not processed successfully. 
   */
  public static final TerminationCauseType DIAMETER_BAD_ANSWER = new TerminationCauseType(_DIAMETER_BAD_ANSWER);

  /**
   * The user was not granted access, or was disconnected, due to administrative reasons, such as the receipt of a Abort-Session- Request message. 
   */
  public static final TerminationCauseType DIAMETER_ADMINISTRATIVE = new TerminationCauseType(_DIAMETER_ADMINISTRATIVE);

  /**
   * The communication to the user was abruptly disconnected. 
   */
  public static final TerminationCauseType DIAMETER_LINK_BROKEN = new TerminationCauseType(_DIAMETER_LINK_BROKEN);

  /**
   * The user's access was terminated since its authorized session time has expired. 
   */
  public static final TerminationCauseType DIAMETER_AUTH_EXPIRED = new TerminationCauseType(_DIAMETER_AUTH_EXPIRED);

  /**
   * The user is receiving services from another access device. 
   */
  public static final TerminationCauseType DIAMETER_USER_MOVED = new TerminationCauseType(_DIAMETER_USER_MOVED);

  /**
   * The user's session has timed out, and service has been terminated. 
   */
  public static final TerminationCauseType DIAMETER_SESSION_TIMEOUT = new TerminationCauseType(_DIAMETER_SESSION_TIMEOUT);

  private TerminationCauseType(int value) {
    this.value = value;
  }

  public static TerminationCauseType fromInt(int type) {
    switch(type) {
    case _DIAMETER_LOGOUT:
      return DIAMETER_LOGOUT;
    case _DIAMETER_SERVICE_NOT_PROVIDED:
      return DIAMETER_SERVICE_NOT_PROVIDED;
    case _DIAMETER_BAD_ANSWER:
      return DIAMETER_BAD_ANSWER;
    case _DIAMETER_ADMINISTRATIVE:
      return DIAMETER_ADMINISTRATIVE;
    case _DIAMETER_LINK_BROKEN:
      return DIAMETER_LINK_BROKEN;
    case _DIAMETER_AUTH_EXPIRED:
      return DIAMETER_AUTH_EXPIRED;
    case _DIAMETER_USER_MOVED:
      return DIAMETER_USER_MOVED;
    case _DIAMETER_SESSION_TIMEOUT:
      return DIAMETER_SESSION_TIMEOUT;
    default:
      throw new IllegalArgumentException("Invalid TerminationCause value: " + type);
    }
  }

  public int getValue() {
    return value;
  }

  public String toString() {
    switch(value) {
    case _DIAMETER_LOGOUT:
      return "DIAMETER_LOGOUT";
    case _DIAMETER_SERVICE_NOT_PROVIDED:
      return "DIAMETER_SERVICE_NOT_PROVIDED";
    case _DIAMETER_BAD_ANSWER:
      return "DIAMETER_BAD_ANSWER";
    case _DIAMETER_ADMINISTRATIVE:
      return "DIAMETER_ADMINISTRATIVE";
    case _DIAMETER_LINK_BROKEN:
      return "DIAMETER_LINK_BROKEN";
    case _DIAMETER_AUTH_EXPIRED:
      return "DIAMETER_AUTH_EXPIRED";
    case _DIAMETER_USER_MOVED:
      return "DIAMETER_USER_MOVED";
    case _DIAMETER_SESSION_TIMEOUT:
      return "DIAMETER_SESSION_TIMEOUT";
    default: return "<Invalid Value>";
    }
  }

  private Object readResolve() throws StreamCorruptedException {
    try {
      return fromInt(value);
    }
    catch (IllegalArgumentException iae) {
      throw new StreamCorruptedException("Invalid internal state found: " + value);
    }
  }

  private int value;
}
