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

package net.java.slee.resource.diameter.ro;

import net.java.slee.resource.diameter.base.DiameterActivity;

/**
 * RoSessionActivity.java
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public interface RoSessionActivity extends DiameterActivity {

  /**
   * Provides session state information. CC session must conform to CC FSM as
   * described in <a href="link http://rfc.net/rfc4006.html#s7">section 7 of
   * rfc4006</a>
   * 
   * @return instance of {@link CreditControlSessionState}
   */
  public RoSessionState getState();

  /**
   * Return a message factory to be used to create concrete implementations of
   * credit control messages.
   * 
   * @return
   */
  public RoMessageFactory getRoMessageFactory();

  /**
   * Returns the session ID of the credit control session, which uniquely
   * identifies the session.
   * 
   * @return the session ID
   */
  public String getSessionId();
}
