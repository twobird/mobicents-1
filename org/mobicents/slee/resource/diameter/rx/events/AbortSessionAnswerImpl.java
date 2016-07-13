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

package org.mobicents.slee.resource.diameter.rx.events;

import net.java.slee.resource.diameter.base.events.avp.DiameterAvpCodes;
import net.java.slee.resource.diameter.rx.events.AbortSessionAnswer;

import org.jdiameter.api.Message;
import org.mobicents.slee.resource.diameter.base.events.DiameterMessageImpl;

/**
 * Implementation of {@link AbortSessionAnswer}
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:richard.good@smilecoms.com"> Richard Good </a>
 * @see DiameterMessageImpl
 */
public class AbortSessionAnswerImpl extends DiameterMessageImpl implements AbortSessionAnswer {

  public AbortSessionAnswerImpl(Message message) {
    super(message);
  }

  @Override
  public String getLongName() {
    return "Abort-Session-Answer";
  }

  @Override
  public String getShortName() {
    return "ASA";
  }

  @Override
  public boolean hasProxyInfo() {

    return super.hasAvp(DiameterAvpCodes.PROXY_INFO);
  }

  @Override
  public boolean hasFailedAvp() {
    return super.hasAvp(DiameterAvpCodes.FAILED_AVP);
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.rx.events.AbortSessionAnswer#hasRediredectHosts()
   */
  @Override
  public boolean hasRediredectHosts() {
    return super.hasAvp(DiameterAvpCodes.REDIRECT_HOST);
  }
}
