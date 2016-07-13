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

/**
 * 
 * Start time:15:00:42 2009-05-23<br>
 * Project: diameter-parent<br>
 * Defines rturn codes used by CCA messages.
 *      
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class CreditControlResultCode {

  /**
   * The credit-control server determines that the service can be granted to
   * the end user but no further credit-control is needed for the service (eg,
   * service is free of charge).
   */
  public static final int DIAMETER_CREDIT_CONTROL_NOT_APPLICABLE = 4011;
  /**
   * The credit-control server denies the service request since the end-user's
   * account could not cover the requested service. If the CCR contained
   * used-service-units they are deducted, if possible.
   * 
   */
  public static final int DIAMETER_CREDIT_LIMIT_REACHED = 4012;
  /**
   * The credit-control server denies the service request due to service
   * restrictions. If the CCR contained used-service-units they are deducted,
   * if possible.
   */
  public static final int DIAMETER_END_USER_SERVICE_DENIED = 4010;
  /**
   * This error code is used to inform the credit-control client that the
   * credit-control server cannot rate the service request due to insufficient
   * rating input, incorrect AVP combination or due to an AVP or an AVP value
   * that is not recognized or supported in the rating. The Failed-AVP AVP
   * MUST be included and contain a copy of the entire AVP(s) that could not
   * be processed successfully or an example of the missing AVP complete with
   * the Vendor-Id if applicable. The value field of the missing AVP should be
   * of correct minimum length and contain zeroes.
   */
  public static final int DIAMETER_RATING_FAILED = 5031;
  /**
   * The specified end user is unknown in the credit-control server.
   */
  public static final int DIAMETER_USER_UNKNOWN = 5030;

}
