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

package net.java.slee.resource.diameter.sh;

/**
 * Diameter Sh Result Codes
 */
public class DiameterShResultCode {

  /*
   * Permanent Failures
   *  
   * Errors that fall within the Permanent Failures category are used to inform the peer that
   * the request failed, and should not be attempted again.
   */

  /**
   * The data required, in the XML schema, does not match that which is specified within the
   * HSS.
   */
  public static final int DIAMETER_ERROR_USER_DATA_NOT_RECOGNIZED = 5100;

  /**
   * The requested operation is not allowed for the user
   */
  public static final int DIAMETER_ERROR_OPERATION_NOT_ALLOWED = 5101;

  /**
   * The requested user data is not allowed to be read.
   */
  public static final int DIAMETER_ERROR_USER_DATA_CANNOT_BE_READ = 5102;

  /**
   * The requested user data is not allowed to be modified.
   */
  public static final int DIAMETER_ERROR_USER_DATA_CANNOT_BE_MODIFIED = 5103;

  /**
   * The requested user data is not allowed to be notified on changes.
   */
  public static final int DIAMETER_ERROR_USER_DATA_CANNOT_BE_NOTIFIED = 5104;


  /**
   * The size of the data pushed to the receiving entity exceeds its capacity. This error code
   * is defined in 3GPP TS 29.229 [6].
   */
  public static final int DIAMETER_ERROR_TOO_MUCH_DATA = 5008;

  /**
   * The request to update the repository data at the HSS could not be completed because the
   * requested update is based on an out-of-date version of the repository data.  That is, the
   * sequence number in the Sh-Update Request message, does not match with the immediate
   * successor of the associated sequence number stored for that repository data at the HSS.
   * It is also used where an AS tries to create a new set of repository data when the
   * identified repository data already exists in the HSS.
   */
  public static final int DIAMETER_ERROR_TRANSPARENT_DATA_OUT_OF_SYNC = 5105;

  /**
   * See 3GPP TS 29.229 [6] clause 6.2.2.11.
   */
  public static final int DIAMETER_ERROR_FEATURE_UNSUPPORTED = 5011;

  /**
   * The Application Server requested to subscribe to changes to Repository Data that is not 
   * present in the HSS. 
   *  
   */
  public static final int DIAMETER_ERROR_SUBS_DATA_ABSENT = 5106;

  /*
   * Transient Failures
   * 
   * Errors that fall within the transient failures category are those used to inform a peer
   * that the request could not be satisfied at the time that it was received. The request may
   * be able to be satisfied in the future.
   */

  /**
   * The requested user data is not available at this time to satisfy the requested operation.
   */
  public static final int DIAMETER_USER_DATA_NOT_AVAILABLE = 4100;

  /**
   * The request to update the repository data at the HSS could not be completed because the
   * related repository data is currently being updated by another entity.
   */
  public static final int DIAMETER_PRIOR_UPDATE_IN_PROGRESS = 4101;

  /*
   * Inherited by Cx/Dx
   */
  
  /**
   * The HSS informs the I-CSCF that:
   *   - The user is authorized to register this public identity;
   *   - A S-CSCF shall be assigned to the user.
   */
  public static final int  DIAMETER_FIRST_REGISTRATION = 2001;

  /**
   * The HSS informs the I-CSCF that:
   *   - The user is authorized to register this public identity;
   *   - A S-CSCF is already assigned and there is no need to select a new one.
   */
  public static final int  DIAMETER_SUBSEQUENT_REGISTRATION = 2002;

  /**
   * The HSS informs the I-CSCF that:
   *   - The public identity is not registered but has services related to unregistered state;
   *   - A S-CSCF shall be assigned to the user.
   */
  public static final int  DIAMETER_UNREGISTERED_SERVICE = 2003;

  /**
   * The HSS informs to the S-CSCF that:
   *   - The de-registration is completed;
   *   - The S-CSCF name is not stored in the HSS.
   */
  public static final int  DIAMETER_SUCCESS_SERVER_NAME_NOT_STORED = 2004;

  /**
   * A message was received for a user that is unknown.
   */
  public static final int  DIAMETER_ERROR_USER_UNKNOWN = 5001;

  /**
   * A message was received with a public identity and a private identity for a user, and the server determines that the public identity does not correspond to the private identity.
   */
  public static final int  DIAMETER_ERROR_IDENTITIES_DONT_MATCH = 5002; 

  /**
   * A query for location information is received for a public identity that has not been registered before.
   */
  public static final int  DIAMETER_ERROR_IDENTITY_NOT_REGISTERED = 5003;

  /**
   * The user is not allowed to roam in the visited network.
   */
  public static final int  DIAMETER_ERROR_ROAMING_NOT_ALLOWED = 5004;

  /**
   * The identity being registered has already a server assigned and the registration status does not allow that it is overwritten.
   */
  public static final int  DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED = 5005;

  /**
   * The authentication scheme indicated in an authentication request is not supported.
   */
  public static final int  DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED = 5006;

  /**
   * The identity being registered has already the same server assigned and the registration status does not allow the server assignment type.
   */
  public static final int  DIAMETER_ERROR_IN_ASSIGNMENT_TYPE = 5007;

  /**
   * The S-CSCF informs HSS that the received subscription data contained information, which was not recognised or supported.
   */
  public static final int  DIAMETER_ERROR_NOT_SUPPORTED_USER_DATA = 5009;

}
