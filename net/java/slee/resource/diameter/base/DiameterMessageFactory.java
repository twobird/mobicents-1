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

package net.java.slee.resource.diameter.base;


import net.java.slee.resource.diameter.base.events.AbortSessionAnswer;
import net.java.slee.resource.diameter.base.events.AbortSessionRequest;
import net.java.slee.resource.diameter.base.events.AccountingAnswer;
import net.java.slee.resource.diameter.base.events.AccountingRequest;
import net.java.slee.resource.diameter.base.events.CapabilitiesExchangeAnswer;
import net.java.slee.resource.diameter.base.events.CapabilitiesExchangeRequest;
import net.java.slee.resource.diameter.base.events.DeviceWatchdogAnswer;
import net.java.slee.resource.diameter.base.events.DeviceWatchdogRequest;
import net.java.slee.resource.diameter.base.events.DiameterCommand;
import net.java.slee.resource.diameter.base.events.DiameterHeader;
import net.java.slee.resource.diameter.base.events.DiameterMessage;
import net.java.slee.resource.diameter.base.events.DisconnectPeerAnswer;
import net.java.slee.resource.diameter.base.events.DisconnectPeerRequest;
import net.java.slee.resource.diameter.base.events.ExtensionDiameterMessage;
import net.java.slee.resource.diameter.base.events.ReAuthAnswer;
import net.java.slee.resource.diameter.base.events.ReAuthRequest;
import net.java.slee.resource.diameter.base.events.SessionTerminationAnswer;
import net.java.slee.resource.diameter.base.events.SessionTerminationRequest;
import net.java.slee.resource.diameter.base.events.avp.AvpNotAllowedException;
import net.java.slee.resource.diameter.base.events.avp.DiameterAvp;

/**
 * Factory to create instances of {@link DiameterMessage}.
 * <p/>
 * An implementation of this class should be returned by the
 * {@link DiameterProvider#getDiameterMessageFactory()} method.
 *
 * @author Open Cloud
 */
public interface DiameterMessageFactory {

  /**
   * Create an instance of a DiameterMessage concrete implementation using
   * the given arguments and default AVPs required for the command (must be known
   * to the factory).
   *
   * @param command the command for this message
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *              message in the order they are in the array.
   * @return a complete and correct DiameterMessage object to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  ExtensionDiameterMessage createMessage(DiameterCommand command, DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an instance of a DiameterMessage concrete implementation using
   * the given arguments and default AVPs required for the command (must be known
   * to the factory).
   * 
   * @param header the header to be included in this message
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *              message in the order they are in the array.
   * @return a complete and correct DiameterMessage object to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  DiameterMessage createMessage(DiameterHeader header, DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create a AbortSessionRequest DiameterMessage for a ASR command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  AbortSessionRequest createAbortSessionRequest(DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty AbortSessionRequest DiameterMessage for a ASR command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  AbortSessionRequest createAbortSessionRequest();

  /**
   * Create a AbortSessionAnswer DiameterMessage for a ASA command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  AbortSessionAnswer createAbortSessionAnswer(AbortSessionRequest request,DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty AbortSessionAnswer DiameterMessage for a ASA command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  AbortSessionAnswer createAbortSessionAnswer(AbortSessionRequest request);

  /**
   * Create a AccountingRequest DiameterMessage for a ACR command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  AccountingRequest createAccountingRequest(DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty AccountingRequest DiameterMessage for a ACR command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  AccountingRequest createAccountingRequest();

  /**
   * Create a AccountingAnswer DiameterMessage for a ACA command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  AccountingAnswer createAccountingAnswer(AccountingRequest request,DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty AccountingAnswer DiameterMessage for a ACA command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  AccountingAnswer createAccountingAnswer(AccountingRequest request);

  /**
   * Create a CapabilitiesExchangeRequest DiameterMessage for a CER command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  CapabilitiesExchangeRequest createCapabilitiesExchangeRequest(DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty CapabilitiesExchangeRequest DiameterMessage for a CER command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  CapabilitiesExchangeRequest createCapabilitiesExchangeRequest();

  /**
   * Create a CapabilitiesExchangeAnswer DiameterMessage for a CEA command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  CapabilitiesExchangeAnswer createCapabilitiesExchangeAnswer(CapabilitiesExchangeRequest request,DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty CapabilitiesExchangeAnswer DiameterMessage for a CEA command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  CapabilitiesExchangeAnswer createCapabilitiesExchangeAnswer(CapabilitiesExchangeRequest request);

  /**
   * Create a DeviceWatchdogRequest DiameterMessage for a DWR command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  DeviceWatchdogRequest createDeviceWatchdogRequest(DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty DeviceWatchdogRequest DiameterMessage for a DWR command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  DeviceWatchdogRequest createDeviceWatchdogRequest();

  /**
   * Create a DeviceWatchdogAnswer DiameterMessage for a DWA command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  DeviceWatchdogAnswer createDeviceWatchdogAnswer(DeviceWatchdogRequest request,DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty DeviceWatchdogAnswer DiameterMessage for a DWA command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  DeviceWatchdogAnswer createDeviceWatchdogAnswer(DeviceWatchdogRequest request);

  /**
   * Create a DisconnectPeerRequest DiameterMessage for a DPR command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  DisconnectPeerRequest createDisconnectPeerRequest(DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty DisconnectPeerRequest DiameterMessage for a DPR command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  DisconnectPeerRequest createDisconnectPeerRequest();

  /**
   * Create a DisconnectPeerAnswer DiameterMessage for a DPA command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  DisconnectPeerAnswer createDisconnectPeerAnswer(DisconnectPeerRequest request,DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty DisconnectPeerAnswer DiameterMessage for a DPA command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  DisconnectPeerAnswer createDisconnectPeerAnswer(DisconnectPeerRequest request);

  /**
   * Create a ReAuthRequest DiameterMessage for a RAR command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  ReAuthRequest createReAuthRequest(DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty ReAuthRequest DiameterMessage for a RAR command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  ReAuthRequest createReAuthRequest();

  /**
   * Create a ReAuthAnswer DiameterMessage for a RAA command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  ReAuthAnswer createReAuthAnswer(ReAuthRequest request,DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty ReAuthAnswer DiameterMessage for a RAA command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  ReAuthAnswer createReAuthAnswer(ReAuthRequest request);

  /**
   * Create a SessionTerminationRequest DiameterMessage for a STR command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  SessionTerminationRequest createSessionTerminationRequest(DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty SessionTerminationRequest DiameterMessage for a STR command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  SessionTerminationRequest createSessionTerminationRequest();

  /**
   * Create a SessionTerminationAnswer DiameterMessage for a STA command containing the given AVPs.
   *
   * @param avps an array of DiameterAvp objects. AVPs will be added to the
   *             message in the order they are in the array.  May be null or empty.
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  SessionTerminationAnswer createSessionTerminationAnswer(SessionTerminationRequest request, DiameterAvp[] avps) throws AvpNotAllowedException;

  /**
   * Create an empty SessionTerminationAnswer DiameterMessage for a STA command.
   *
   * @return an implementation of DiameterMessage to be passed to
   *         {@link DiameterActivity#sendMessage(DiameterMessage)}.
   */
  SessionTerminationAnswer createSessionTerminationAnswer(SessionTerminationRequest request);

}
