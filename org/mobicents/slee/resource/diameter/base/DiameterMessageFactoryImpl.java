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

package org.mobicents.slee.resource.diameter.base;

import net.java.slee.resource.diameter.base.DiameterMessageFactory;
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
import net.java.slee.resource.diameter.base.events.avp.AvpUtilities;
import net.java.slee.resource.diameter.base.events.avp.DiameterAvp;
import net.java.slee.resource.diameter.base.events.avp.DiameterAvpCodes;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;
import net.java.slee.resource.diameter.base.events.avp.GroupedAvp;

import org.apache.log4j.Logger;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Message;
import org.jdiameter.api.Session;
import org.jdiameter.api.Stack;
import org.jdiameter.client.impl.parser.MessageImpl;
import org.mobicents.slee.resource.diameter.base.events.AbortSessionAnswerImpl;
import org.mobicents.slee.resource.diameter.base.events.AbortSessionRequestImpl;
import org.mobicents.slee.resource.diameter.base.events.AccountingAnswerImpl;
import org.mobicents.slee.resource.diameter.base.events.AccountingRequestImpl;
import org.mobicents.slee.resource.diameter.base.events.CapabilitiesExchangeAnswerImpl;
import org.mobicents.slee.resource.diameter.base.events.CapabilitiesExchangeRequestImpl;
import org.mobicents.slee.resource.diameter.base.events.DeviceWatchdogAnswerImpl;
import org.mobicents.slee.resource.diameter.base.events.DeviceWatchdogRequestImpl;
import org.mobicents.slee.resource.diameter.base.events.DisconnectPeerAnswerImpl;
import org.mobicents.slee.resource.diameter.base.events.DisconnectPeerRequestImpl;
import org.mobicents.slee.resource.diameter.base.events.ExtensionDiameterMessageImpl;
import org.mobicents.slee.resource.diameter.base.events.ReAuthAnswerImpl;
import org.mobicents.slee.resource.diameter.base.events.ReAuthRequestImpl;
import org.mobicents.slee.resource.diameter.base.events.SessionTerminationAnswerImpl;
import org.mobicents.slee.resource.diameter.base.events.SessionTerminationRequestImpl;

/**
 * Diameter Base Message Factory
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author Erick Svenson
 */
public class DiameterMessageFactoryImpl implements DiameterMessageFactory {

  private static Logger logger = Logger.getLogger(DiameterMessageFactoryImpl.class);

  protected Session session;
  protected Stack stack;

  private static final ApplicationId BASE_AUTH_APP_ID = ApplicationId.createByAuthAppId(0, 0);
  private static final ApplicationId BASE_ACCT_APP_ID = ApplicationId.createByAccAppId(0, 3);

  public DiameterMessageFactoryImpl(Session session, Stack stack, DiameterIdentity... avps) {
    this.session = session;
    this.stack = stack;
  }

  public DiameterMessageFactoryImpl(Stack stack) {
    this.stack = stack;
  }

  public AbortSessionAnswer createAbortSessionAnswer(AbortSessionRequest request, DiameterAvp[] avps) throws AvpNotAllowedException {
    AbortSessionAnswer msg = (AbortSessionAnswer) this.createDiameterMessage(request.getHeader(), avps, Message.ABORT_SESSION_ANSWER, getApplicationId(request));

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);
    
    return msg;
  }

  public AbortSessionAnswer createAbortSessionAnswer(AbortSessionRequest request) {
    try {
      return createAbortSessionAnswer(request, new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create ASA message.", e);
    }

    return null;
  }

  public AbortSessionRequest createAbortSessionRequest(DiameterAvp[] avps) throws AvpNotAllowedException {
    ApplicationId appId = getApplicationId(avps);
    AbortSessionRequest msg = (AbortSessionRequest) this.createDiameterMessage(null, avps, Message.ABORT_SESSION_REQUEST, appId != null ? appId : BASE_AUTH_APP_ID);

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public AbortSessionRequest createAbortSessionRequest() {
    try {
      return createAbortSessionRequest(new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create ASR message.", e);
    }

    return null;
  }

  public AccountingAnswer createAccountingAnswer(AccountingRequest request, DiameterAvp[] avps) throws AvpNotAllowedException {
    AccountingAnswer msg = (AccountingAnswer) this.createDiameterMessage(request.getHeader(), avps, Message.ACCOUNTING_ANSWER, getApplicationId(request));

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;

  }

  public AccountingAnswer createAccountingAnswer(AccountingRequest request) {
    try {
      return createAccountingAnswer(request, new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create ACA message.", e);
    }

    return null;
  }

  public AccountingRequest createAccountingRequest(DiameterAvp[] avps) throws AvpNotAllowedException {
    ApplicationId appId = getApplicationId(avps);
    AccountingRequest msg = (AccountingRequest) this.createDiameterMessage(null, avps, Message.ACCOUNTING_REQUEST, appId != null ? appId : BASE_ACCT_APP_ID);

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public AccountingRequest createAccountingRequest() {
    try {
      return createAccountingRequest(new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create ACR message.", e);
    }

    return null;
  }

  public CapabilitiesExchangeAnswer createCapabilitiesExchangeAnswer(CapabilitiesExchangeRequest request, DiameterAvp[] avps) throws AvpNotAllowedException {
    CapabilitiesExchangeAnswer msg = (CapabilitiesExchangeAnswer) this.createDiameterMessage(request.getHeader(), avps, Message.CAPABILITIES_EXCHANGE_ANSWER, getApplicationId(request));

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public CapabilitiesExchangeAnswer createCapabilitiesExchangeAnswer(CapabilitiesExchangeRequest request) {
    try {
      return createCapabilitiesExchangeAnswer(request, new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create CEA message.", e);
    }

    return null;
  }

  public CapabilitiesExchangeRequest createCapabilitiesExchangeRequest(DiameterAvp[] avps) throws AvpNotAllowedException {
    CapabilitiesExchangeRequest msg = (CapabilitiesExchangeRequest) this.createDiameterMessage(null, avps, Message.CAPABILITIES_EXCHANGE_REQUEST, BASE_AUTH_APP_ID);

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public CapabilitiesExchangeRequest createCapabilitiesExchangeRequest() {
    try {
      return createCapabilitiesExchangeRequest(new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create CER message.", e);
    }

    return null;
  }

  public DeviceWatchdogAnswer createDeviceWatchdogAnswer(DeviceWatchdogRequest request, DiameterAvp[] avps) throws AvpNotAllowedException {
    DeviceWatchdogAnswer msg = (DeviceWatchdogAnswer) this.createDiameterMessage(request.getHeader(), avps, Message.DEVICE_WATCHDOG_ANSWER, getApplicationId(request));

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public DeviceWatchdogAnswer createDeviceWatchdogAnswer(DeviceWatchdogRequest request) {
    try {
      return createDeviceWatchdogAnswer(request, new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create DWA message.", e);
    }
    return null;
  }

  public DeviceWatchdogRequest createDeviceWatchdogRequest(DiameterAvp[] avps) throws AvpNotAllowedException {
    DeviceWatchdogRequest msg = (DeviceWatchdogRequest) this.createDiameterMessage(null, avps, Message.DEVICE_WATCHDOG_REQUEST, BASE_AUTH_APP_ID);

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public DeviceWatchdogRequest createDeviceWatchdogRequest() {
    try {
      return createDeviceWatchdogRequest(new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create DWR message.", e);
    }

    return null;
  }

  public DisconnectPeerAnswer createDisconnectPeerAnswer(DisconnectPeerRequest request, DiameterAvp[] avps) throws AvpNotAllowedException {
    DisconnectPeerAnswer msg = (DisconnectPeerAnswer) this.createDiameterMessage(request.getHeader(), avps, Message.DISCONNECT_PEER_ANSWER, getApplicationId(request));

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public DisconnectPeerAnswer createDisconnectPeerAnswer(DisconnectPeerRequest request) {
    try {
      return createDisconnectPeerAnswer(request, new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create DPA message.", e);
    }
    return null;
  }

  public DisconnectPeerRequest createDisconnectPeerRequest(DiameterAvp[] avps) throws AvpNotAllowedException {
    DisconnectPeerRequest msg = (DisconnectPeerRequest) this.createDiameterMessage(null, avps, Message.DISCONNECT_PEER_REQUEST, BASE_AUTH_APP_ID);

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public DisconnectPeerRequest createDisconnectPeerRequest() {
    try {
      return createDisconnectPeerRequest(new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create DPR message.", e);
    }
    return null;
  }

  public ExtensionDiameterMessage createMessage(DiameterCommand command, DiameterAvp[] avps) throws AvpNotAllowedException {
    ApplicationId aid = command.getCode() == AccountingRequest.commandCode ? 
        ApplicationId.createByAccAppId(0, command.getApplicationId()) : ApplicationId.createByAuthAppId(0, command.getApplicationId());

        ExtensionDiameterMessageImpl msg = (ExtensionDiameterMessageImpl) this.createDiameterMessage(null, avps, command.getCode(), aid);
        msg.getGenericData().setRequest(command.isRequest());
        ((MessageImpl)msg.getGenericData()).setProxiable(command.isProxiable());
        if(!command.isRequest()) {
          // just in case. answers never have T flag set
          ((MessageImpl)msg.getGenericData()).setReTransmitted(false);
        }

        return msg;
  }

  public ReAuthAnswer createReAuthAnswer(ReAuthRequest request, DiameterAvp[] avps) throws AvpNotAllowedException {
    ReAuthAnswer msg = (ReAuthAnswer) this.createDiameterMessage(request.getHeader(), avps, Message.RE_AUTH_ANSWER, getApplicationId(request));

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public ReAuthAnswer createReAuthAnswer(ReAuthRequest request) {
    try {
      return createReAuthAnswer(request, new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create RAA message.", e);
    }

    return null;
  }

  public ReAuthRequest createReAuthRequest(DiameterAvp[] avps) throws AvpNotAllowedException {
    ReAuthRequest msg = (ReAuthRequest) this.createDiameterMessage(null, avps, Message.RE_AUTH_REQUEST, BASE_AUTH_APP_ID);

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public ReAuthRequest createReAuthRequest() {
    try {
      return createReAuthRequest(new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create RAR message.", e);
    }

    return null;
  }

  public SessionTerminationAnswer createSessionTerminationAnswer(SessionTerminationRequest request, DiameterAvp[] avps) throws AvpNotAllowedException {
    SessionTerminationAnswer msg = (SessionTerminationAnswer) this.createDiameterMessage(request.getHeader(), avps, Message.SESSION_TERMINATION_REQUEST, getApplicationId(request));

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public SessionTerminationAnswer createSessionTerminationAnswer(SessionTerminationRequest request) {
    try {
      return createSessionTerminationAnswer(request, new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create STA message.", e);
    }

    return null;
  }

  public SessionTerminationRequest createSessionTerminationRequest(DiameterAvp[] avps) throws AvpNotAllowedException {
    SessionTerminationRequest msg = (SessionTerminationRequest) this.createDiameterMessage(null, avps, Message.SESSION_TERMINATION_REQUEST, BASE_AUTH_APP_ID);

    // Add Session-Id AVP if not present
    addSessionIdAvp(msg);

    return msg;
  }

  public SessionTerminationRequest createSessionTerminationRequest() {
    try {
      return createSessionTerminationRequest(new DiameterAvp[0]);
    }
    catch (AvpNotAllowedException e) {
      logger.error("Unexpected failure while trying to create STR message.", e);
      return null;
    }
  }

  protected DiameterMessage createDiameterMessage(DiameterHeader diameterHeader, DiameterAvp[] avps, int _commandCode, ApplicationId appId) throws IllegalArgumentException {

    boolean creatingRequest = diameterHeader == null;
    Message msg = null;

    if (!creatingRequest) {
      Message raw = createMessage(diameterHeader, avps, 0, appId);
      //raw.setProxiable(true);
      raw.setRequest(false);
      raw.setReTransmitted(false); // just in case. answers never have T flag set
      msg = raw;
    }
    else {
      Message raw = createMessage(diameterHeader, avps, _commandCode, appId);
      boolean isPeerManagementMessage = _commandCode == CapabilitiesExchangeRequest.commandCode || _commandCode == DeviceWatchdogRequest.commandCode || _commandCode == DisconnectPeerRequest.commandCode; 
      raw.setProxiable(!isPeerManagementMessage);
      raw.setRequest(true);
      msg = raw;
    }

    int commandCode = creatingRequest ? _commandCode : diameterHeader.getCommandCode();
    DiameterMessage diamMessage = null;

    switch (commandCode) {
    case Message.ABORT_SESSION_REQUEST:
      diamMessage = creatingRequest ? new AbortSessionRequestImpl(msg) : new AbortSessionAnswerImpl(msg);
      break;
    case Message.ACCOUNTING_REQUEST:
      diamMessage = creatingRequest ? new AccountingRequestImpl(msg) : new AccountingAnswerImpl(msg);
      break;
    case Message.CAPABILITIES_EXCHANGE_REQUEST:
      diamMessage = creatingRequest ? new CapabilitiesExchangeRequestImpl(msg) : new CapabilitiesExchangeAnswerImpl(msg);
      break;
    case Message.DEVICE_WATCHDOG_REQUEST:
      diamMessage = creatingRequest ? new DeviceWatchdogRequestImpl(msg) : new DeviceWatchdogAnswerImpl(msg);
      break;
    case Message.DISCONNECT_PEER_REQUEST:
      diamMessage = creatingRequest ? new DisconnectPeerRequestImpl(msg) : new DisconnectPeerAnswerImpl(msg);
      break;
    case Message.RE_AUTH_REQUEST:
      diamMessage = creatingRequest ? new ReAuthRequestImpl(msg) : new ReAuthAnswerImpl(msg);
      break;
    case Message.SESSION_TERMINATION_REQUEST:
      diamMessage = creatingRequest ? new SessionTerminationRequestImpl(msg) : new SessionTerminationAnswerImpl(msg);
      break;
    default:
      diamMessage = new ExtensionDiameterMessageImpl(msg);
    }

    // Finally, add Origin-Host and Origin-Realm, if not present.
    addOriginHostAndRealm(diamMessage);

    return diamMessage;
  }

  protected Message createMessage(DiameterHeader header, DiameterAvp[] avps,int _commandCode, ApplicationId appId) throws AvpNotAllowedException
  {
    try {
      Message msg = createRawMessage(header, _commandCode, appId);

      if(avps != null && avps.length > 0) {
        AvpSet set = msg.getAvps();
        for (DiameterAvp avp : avps) {
          if(appId != null && (avp.getCode() == DiameterAvpCodes.VENDOR_SPECIFIC_APPLICATION_ID || avp.getCode() == DiameterAvpCodes.ACCT_APPLICATION_ID || avp.getCode() == DiameterAvpCodes.AUTH_APPLICATION_ID)) {
            continue;
          }
          addAvp(avp, set);
        }
      }

      return msg;
    }
    catch (Exception e) {
      logger.error("Failure trying to create Diameter message.", e);
    }

    return null;
  }

  protected Message createRawMessage(DiameterHeader header, int _commandCode, ApplicationId appId) {

    int commandCode = 0;
    long endToEndId = 0;
    long hopByHopId = 0;
    
    boolean isRequest = true;
    boolean isProxiable = true;
    boolean isError = false;
    boolean isPotentiallyRetransmitted = false;

    if (header != null) {
      commandCode = header.getCommandCode();
      endToEndId = header.getEndToEndId();
      hopByHopId = header.getHopByHopId();

      isRequest = header.isRequest();
      isProxiable = header.isProxiable();
      isError = header.isError();
      isPotentiallyRetransmitted = header.isPotentiallyRetransmitted();
    }
    else {
      commandCode = _commandCode;
    }
    try {
      Message msg = stack.getSessionFactory().getNewRawSession().createMessage(commandCode, appId, hopByHopId, endToEndId);

      // Set the message flags from header (or default)
      msg.setRequest(isRequest);
      msg.setProxiable(isProxiable);
      msg.setError(isError);
      msg.setReTransmitted(isRequest && isPotentiallyRetransmitted);
      
      return msg;
    }
    catch (Exception e) {
      logger.error( "Failure while trying to create raw message.", e );
    }

    return null;
  }

  protected void addAvp(DiameterAvp avp, AvpSet set) {
    AvpUtilities.addAvp(avp, set);
  }

  /**
   * 
   */
  public void clean() {
    this.session = null;
  }

  /* (non-Javadoc)
   * @see net.java.slee.resource.diameter.base.DiameterMessageFactory#createMessage(net.java.slee.resource.diameter.base.events.DiameterHeader, net.java.slee.resource.diameter.base.events.avp.DiameterAvp[])
   */
  public DiameterMessage createMessage(DiameterHeader header, DiameterAvp[] avps) throws AvpNotAllowedException {
    return this.createDiameterMessage(header, avps, header.getCommandCode(), getApplicationId(avps));
  }

  private void addOriginHostAndRealm(DiameterMessage msg) {
    if(!msg.hasOriginHost()) {
      msg.setOriginHost(new DiameterIdentity(stack.getMetaData().getLocalPeer().getUri().getFQDN().toString()));
    }
    if(!msg.hasOriginRealm()) {
      msg.setOriginRealm(new DiameterIdentity(stack.getMetaData().getLocalPeer().getRealmName()));
    }
  }

  private ApplicationId getApplicationId(DiameterMessage msg) {
    ApplicationId applicationId = getApplicationId(msg.getAvps());

    if (applicationId == null) {
      applicationId = msg.getCommand().getCode() == AccountingRequest.commandCode ? 
          ApplicationId.createByAccAppId(ApplicationId.Standard.DIAMETER_COMMON_MESSAGE) : ApplicationId.createByAuthAppId(ApplicationId.Standard.DIAMETER_COMMON_MESSAGE);
    }

    return applicationId;
  }

  private ApplicationId getApplicationId(DiameterAvp[] avps) {
    ApplicationId applicationId = null;

    long vendorId = 0L;
    
    // Try to get Application-Id from Message AVPs
    if (avps != null) {
      for (DiameterAvp avp : avps) {
        if(avp.getCode() == DiameterAvpCodes.VENDOR_ID) {
          vendorId = avp.intValue();
        }
        if(avp.getCode() == DiameterAvpCodes.VENDOR_SPECIFIC_APPLICATION_ID) {
          applicationId = getApplicationId(((GroupedAvp)avp).getExtensionAvps());
          break;
        }
        if (avp.getCode() == DiameterAvpCodes.ACCT_APPLICATION_ID) {
          applicationId = ApplicationId.createByAccAppId(vendorId, avp.intValue());
          break;
        }
        else if (avp.getCode() == DiameterAvpCodes.AUTH_APPLICATION_ID) {
          applicationId = ApplicationId.createByAuthAppId(vendorId, avp.intValue());
          break;
        }
      }
    }

    return applicationId;
  }

  // private ApplicationId getApplicationId(DiameterHeader header) {
  //   return header.getCommandCode() == AccountingRequest.commandCode ? 
  //       ApplicationId.createByAccAppId(header.getApplicationId()) : ApplicationId.createByAuthAppId(header.getApplicationId());
  // }

  private void addSessionIdAvp(DiameterMessage msg) {
    if(!msg.hasSessionId() && session != null) {
      msg.setSessionId(session.getSessionId());
    }    
  }

}
