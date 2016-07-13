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

package org.mobicents.slee.resource.diameter.sh.server;

import static org.jdiameter.client.impl.helpers.Parameters.MessageTimeOut;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.management.ObjectName;
import javax.naming.OperationNotSupportedException;
import javax.slee.Address;
import javax.slee.facilities.EventLookupFacility;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityFlags;
import javax.slee.resource.ActivityHandle;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.EventFlags;
import javax.slee.resource.FailureReason;
import javax.slee.resource.FireableEventType;
import javax.slee.resource.InvalidConfigurationException;
import javax.slee.resource.Marshaler;
import javax.slee.resource.ReceivableService;
import javax.slee.resource.ResourceAdaptor;
import javax.slee.resource.ResourceAdaptorContext;
import javax.slee.resource.SleeEndpoint;

import net.java.slee.resource.diameter.Validator;
import net.java.slee.resource.diameter.base.CreateActivityException;
import net.java.slee.resource.diameter.base.DiameterActivity;
import net.java.slee.resource.diameter.base.DiameterAvpFactory;
import net.java.slee.resource.diameter.base.DiameterException;
import net.java.slee.resource.diameter.base.events.DiameterMessage;
import net.java.slee.resource.diameter.base.events.avp.AvpNotAllowedException;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;
import net.java.slee.resource.diameter.sh.DiameterShAvpFactory;
import net.java.slee.resource.diameter.sh.events.ProfileUpdateRequest;
import net.java.slee.resource.diameter.sh.events.PushNotificationAnswer;
import net.java.slee.resource.diameter.sh.events.PushNotificationRequest;
import net.java.slee.resource.diameter.sh.events.SubscribeNotificationsRequest;
import net.java.slee.resource.diameter.sh.events.UserDataRequest;
import net.java.slee.resource.diameter.sh.server.ShServerActivity;
import net.java.slee.resource.diameter.sh.server.ShServerMessageFactory;
import net.java.slee.resource.diameter.sh.server.ShServerProvider;
import net.java.slee.resource.diameter.sh.server.ShServerSubscriptionActivity;

import org.jboss.mx.util.MBeanServerLocator;
import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.Message;
import org.jdiameter.api.Peer;
import org.jdiameter.api.PeerTable;
import org.jdiameter.api.Request;
import org.jdiameter.api.Session;
import org.jdiameter.api.SessionFactory;
import org.jdiameter.api.Stack;
import org.jdiameter.api.sh.ServerShSession;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.server.impl.app.sh.ShServerSessionImpl;
import org.mobicents.diameter.stack.DiameterListener;
import org.mobicents.diameter.stack.DiameterStackMultiplexerMBean;
import org.mobicents.slee.resource.cluster.FaultTolerantResourceAdaptorContext;
import org.mobicents.slee.resource.diameter.AbstractClusteredDiameterActivityManagement;
import org.mobicents.slee.resource.diameter.DiameterActivityManagement;
import org.mobicents.slee.resource.diameter.LocalDiameterActivityManagement;
import org.mobicents.slee.resource.diameter.ValidatorImpl;
import org.mobicents.slee.resource.diameter.base.DiameterActivityHandle;
import org.mobicents.slee.resource.diameter.base.DiameterActivityImpl;
import org.mobicents.slee.resource.diameter.base.DiameterAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.base.DiameterBaseMarshaler;
import org.mobicents.slee.resource.diameter.base.DiameterMessageFactoryImpl;
import org.mobicents.slee.resource.diameter.base.EventIDFilter;
import org.mobicents.slee.resource.diameter.base.events.DiameterMessageImpl;
import org.mobicents.slee.resource.diameter.base.events.ErrorAnswerImpl;
import org.mobicents.slee.resource.diameter.base.events.ExtensionDiameterMessageImpl;
import org.mobicents.slee.resource.diameter.base.handlers.DiameterRAInterface;
import org.mobicents.slee.resource.diameter.sh.DiameterShAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.sh.EventIDCache;
import org.mobicents.slee.resource.diameter.sh.events.ProfileUpdateAnswerImpl;
import org.mobicents.slee.resource.diameter.sh.events.ProfileUpdateRequestImpl;
import org.mobicents.slee.resource.diameter.sh.events.PushNotificationAnswerImpl;
import org.mobicents.slee.resource.diameter.sh.events.PushNotificationRequestImpl;
import org.mobicents.slee.resource.diameter.sh.events.SubscribeNotificationsAnswerImpl;
import org.mobicents.slee.resource.diameter.sh.events.SubscribeNotificationsRequestImpl;
import org.mobicents.slee.resource.diameter.sh.events.UserDataAnswerImpl;
import org.mobicents.slee.resource.diameter.sh.events.UserDataRequestImpl;
import org.mobicents.slee.resource.diameter.sh.server.handlers.ShServerSessionFactory;

/**
 * Mobicents Diameter Sh (Server-side) Resource Adaptor
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class DiameterShServerResourceAdaptor  implements ResourceAdaptor, DiameterListener, DiameterRAInterface ,org.mobicents.slee.resource.cluster.FaultTolerantResourceAdaptor<String, DiameterActivity> {

  private static final long serialVersionUID = 1L;

  // Config Properties Names ---------------------------------------------

  private static final String AUTH_APPLICATION_IDS = "authApplicationIds";

  // Config Properties Values --------------------------------------------

  private List<ApplicationId> authApplicationIds;

  /**
   * caches the eventIDs, avoiding lookup in container
   */
  public final EventIDCache eventIdCache = new EventIDCache();

  /**
   * tells the RA if an event with a specified ID should be filtered or not
   */
  private final EventIDFilter eventIDFilter = new EventIDFilter();

  /**
   * The ResourceAdaptorContext interface is implemented by the SLEE. It provides the Resource
   * Adaptor with the required capabilities in the SLEE to execute its work. The ResourceAdaptorCon-
   * text object holds references to a number of objects that are of interest to many Resource Adaptors. A
   * resource adaptor object is provided with a ResourceAdaptorContext object when the setResour-
   * ceAdaptorContext method of the ResourceAdaptor interface is invoked on the resource adaptor
   * object. 
   */
  private ResourceAdaptorContext raContext;

  /**
   * FT/HA version of RA context.
   */
  private FaultTolerantResourceAdaptorContext<String, DiameterActivity> ftRAContext;

  /**
   * The SLEE endpoint defines the contract between the SLEE and the resource
   * adaptor that enables the resource adaptor to deliver events
   * asynchronously to SLEE endpoints residing in the SLEE. This contract
   * serves as a generic contract that allows a wide range of resources to be
   * plugged into a SLEE environment via the resource adaptor architecture.
   * For further information see JSLEE v1.1 Specification Page 307 The
   * sleeEndpoint will be initialized in entityCreated() method.
   */
  private transient SleeEndpoint sleeEndpoint = null;

  /**
   * A tracer is represented in the SLEE by the Tracer interface. Notification sources access the Tracer Facil-
   * ity through a Tracer object that implements the Tracer interface. A Tracer object can be obtained by
   * SBBs via the SbbContext interface, by resource adaptor entities via the ResourceAdaptorContext
   * interface, and by profiles via the ProfileContext interface. 
   */
  private Tracer tracer;
  private DiameterBaseMarshaler marshaler = new DiameterBaseMarshaler();

  // Diameter Specific Properties ----------------------------------------

  private Stack stack;
  private SessionFactory sessionFactory = null;
  private long messageTimeout = 5000;

  private long activityRemoveDelay = 30000;

  private ObjectName diameterMultiplexerObjectName = null;
  private DiameterStackMultiplexerMBean diameterMux = null;

  private DiameterAvpFactory baseAvpFactory = null;
  private DiameterShAvpFactory shAvpFactory = null;

  /**
   * the EventLookupFacility is used to look up the event id of incoming
   * events
   */
  private transient EventLookupFacility eventLookup = null;

  /**
   * The list of activities stored in this resource adaptor. If this resource
   * adaptor were a distributed and highly available solution, this storage
   * were one of the candidates for distribution.
   */
  //private transient ConcurrentHashMap<ActivityHandle, DiameterActivity> activities = null;
  private transient DiameterActivityManagement activities = null;

  /**
   * A link to the DiameterProvider which then will be exposed to Sbbs
   */
  private transient ShServerProviderImpl raProvider = null;

  /**
   * for all events we are interested in knowing when the event failed to be processed
   */
  private static final int EVENT_FLAGS = getEventFlags();

  private static int getEventFlags() {
    int eventFlags = EventFlags.REQUEST_EVENT_UNREFERENCED_CALLBACK;
    eventFlags = EventFlags.setRequestProcessingFailedCallback(eventFlags);
    eventFlags = EventFlags.setRequestProcessingSuccessfulCallback(eventFlags);
    return eventFlags;
  }

  private static final int DEFAULT_ACTIVITY_FLAGS = ActivityFlags.setRequestSleeActivityGCCallback(ActivityFlags.REQUEST_ENDED_CALLBACK);
  private static final int MARSHALABLE_ACTIVITY_FLAGS = ActivityFlags.setSleeMayMarshal(DEFAULT_ACTIVITY_FLAGS);

  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

  private static final String[] EMPTY_STRING_ARRAY = new String[]{};

  public DiameterShServerResourceAdaptor() {
    // TODO: Initialize any default values.
  }

  // Lifecycle methods ---------------------------------------------------

  public void setResourceAdaptorContext(ResourceAdaptorContext context) {
    this.raContext = context;
    this.tracer = context.getTracer("DiameterShServerResourceAdaptor");
    this.sleeEndpoint = context.getSleeEndpoint();
    this.eventLookup = context.getEventLookupFacility();
    this.raProvider = new ShServerProviderImpl(this);
  }

  public void unsetResourceAdaptorContext() {
    this.raContext = null;
    this.tracer = null;
    this.sleeEndpoint = null;
    this.eventLookup = null;
    this.raProvider = null;
  }

  // FT Lifecycle methods ------------------------------------------------

  /*
   * (non-Javadoc)
   * @see org.mobicents.slee.resource.cluster.FaultTolerantResourceAdaptor#setFaultTolerantResourceAdaptorContext
   * (org.mobicents.slee.resource.cluster.FaultTolerantResourceAdaptorContext)
   */
  public void setFaultTolerantResourceAdaptorContext(FaultTolerantResourceAdaptorContext<String, DiameterActivity> ctx) {
    this.ftRAContext = ctx;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.mobicents.slee.resource.cluster.FaultTolerantResourceAdaptor#unsetFaultTolerantResourceAdaptorContext()
   */
  public void unsetFaultTolerantResourceAdaptorContext() {
    this.ftRAContext = null;
    //clear this.activities ??
  }

  // FT methods ----------------------------------------------------------

  /*
   * (non-Javadoc)
   * @see org.mobicents.slee.resource.cluster.FaultTolerantResourceAdaptor#dataRemoved(java.io.Serializable)
   */
  public void dataRemoved(String arg0) {
    this.activities.remove(getActivityHandle(arg0));
  }

  /* (non-Javadoc)
   * @see org.mobicents.slee.resource.cluster.FaultTolerantResourceAdaptor#failOver(java.io.Serializable)
   */
  public void failOver(String arg0) {
    throw new UnsupportedOperationException();
  }

  // More life cycle methods ----------------------------------------------

  public void raActive() {
    if(tracer.isFineEnabled()) {
      tracer.fine("Diameter ShServer RA :: raActive.");
    }

    try {
      if(tracer.isInfoEnabled()) {
        tracer.info("Activating Diameter ShServer RA Entity");
      }

      this.diameterMultiplexerObjectName = new ObjectName("diameter.mobicents:service=DiameterStackMultiplexer");

      Object object = MBeanServerLocator.locateJBoss().invoke(this.diameterMultiplexerObjectName, "getMultiplexerMBean", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);

      if(object instanceof DiameterStackMultiplexerMBean) {
        this.diameterMux = (DiameterStackMultiplexerMBean) object;
      }

      //this.activities = new ConcurrentHashMap<ActivityHandle, DiameterActivity>();

      // Initialize stack
      initStack();

      // Initialize activities mgmt
      initActivitiesMgmt();

      // Initialize factories
      this.baseAvpFactory = new DiameterAvpFactoryImpl();
      this.shAvpFactory = new DiameterShAvpFactoryImpl(baseAvpFactory);

      // Setup session factories
      this.sessionFactory = this.stack.getSessionFactory();

      ((ISessionFactory) sessionFactory).registerAppFacory(ServerShSession.class, new ShServerSessionFactory(sessionFactory, this));
    }
    catch (Exception e) {
      tracer.severe("Error Activating Diameter ShServer RA Entity", e);
    }
  }

  public void raStopping() {
    if(tracer.isFineEnabled()) {
      tracer.fine("Diameter ShServer RA :: raStopping.");
    }

    try {
      diameterMux.unregisterListener(this);
    }
    catch (Exception e) {
      tracer.severe("Failed to unregister ShServer RA from Diameter Mux.", e);
    }

    //synchronized (this.activities) {
    //  for (ActivityHandle activityHandle : activities.keySet()) {
    //    try {
    //      if(tracer.isInfoEnabled()) {
    //        tracer.info("Ending activity [" + activityHandle + "]");
    //      }
    //
    //      activities.get(activityHandle).endActivity();
    //    }
    //    catch (Exception e) {
    //      tracer.severe("Error Deactivating Activity", e);
    //    }
    //  }
    //}

    if(tracer.isInfoEnabled()) {
      tracer.info("Diameter ShServer RA :: entityDeactivating completed.");
    }
  }

  public void raInactive() {
    if(tracer.isFineEnabled()) {
      tracer.fine("Diameter ShServer RA :: entityDeactivated.");
    }

    //synchronized (this.activities) {
    //  activities.clear();
    //}
    activities = null;

    if(tracer.isInfoEnabled()) {
      tracer.info("Diameter ShServer RA :: INACTIVE completed.");
    }
  }

  public void raConfigure(ConfigProperties properties) {
    parseApplicationIds((String) properties.getProperty(AUTH_APPLICATION_IDS).getValue());
  }

  private void parseApplicationIds(String appIdsStr) {
    if(appIdsStr != null) {
      appIdsStr = appIdsStr.replaceAll(" ", "");

      String[] appIdsStrings  = appIdsStr.split(",");

      List<ApplicationId> appIds = new ArrayList<ApplicationId>();

      for(String appId : appIdsStrings) {
        String[] vendorAndAppId = appId.split(":");
        appIds.add(ApplicationId.createByAuthAppId(Long.valueOf(vendorAndAppId[0]), Long.valueOf(vendorAndAppId[1]))); 
      }

      authApplicationIds = appIds;
    }
  }

  public void raUnconfigure() {
    // Clean up!
    this.activities = null;
    this.raContext = null;
    this.eventLookup = null;
    this.raProvider = null;
    this.sleeEndpoint = null;
    this.stack = null;
  }

  // Configuration management methods ------------------------------------

  public void raVerifyConfiguration(ConfigProperties properties) throws InvalidConfigurationException {
    // TODO Auto-generated method stub
  }

  public void raConfigurationUpdate(ConfigProperties properties) {
    // this ra does not support config update while entity is active    
  }

  // Interface access methods -------------------------------------------- 

  public Object getResourceAdaptorInterface(String className) {
    // this ra implements a single ra type
    return raProvider;
  }

  /*
   * (non-Javadoc)
   * @see javax.slee.resource.ResourceAdaptor#getMarshaler()
   */
  public Marshaler getMarshaler() {
    return this.marshaler;
  }

  // Event filtering methods ---------------------------------------------

  public void serviceActive(ReceivableService serviceInfo) {
    eventIDFilter.serviceActive(serviceInfo);   
  }

  public void serviceStopping(ReceivableService serviceInfo) {
    eventIDFilter.serviceStopping(serviceInfo);
  }

  public void serviceInactive(ReceivableService serviceInfo) {
    eventIDFilter.serviceInactive(serviceInfo); 
  }

  // Mandatory callback methods ------------------------------------------

  public void queryLiveness(ActivityHandle handle) {
    tracer.info("Diameter ShServer RA :: queryLiveness :: handle[" + handle + "].");
    if(!(handle instanceof DiameterActivityHandle)) {
      return;
    }

    DiameterActivityImpl activity = (DiameterActivityImpl) activities.get((DiameterActivityHandle) handle);

    if (activity != null && !activity.isValid()) {
      try {
        sleeEndpoint.endActivity(handle);
      }
      catch (Exception e) {
        tracer.severe("Failure ending non-live activity.", e);
      }
    }
  }

  public Object getActivity(ActivityHandle handle) {
    if(tracer.isFineEnabled()) {
      tracer.fine("Diameter ShServer RA :: getActivity :: handle[" + handle + "].");
    }
    if(!(handle instanceof DiameterActivityHandle)) {
      return null;
    }
    return this.activities.get((DiameterActivityHandle) handle);
  }

  public ActivityHandle getActivityHandle(Object activity) {
    if(tracer.isFineEnabled()) {
      tracer.fine("Diameter ShServer RA :: getActivityHandle :: activity[" + activity + "].");
    }

    if (!(activity instanceof DiameterActivity)) {
      return null;
    }

    DiameterActivityImpl inActivity = (DiameterActivityImpl) activity;
    return inActivity.getActivityHandle();
  }

  public void administrativeRemove(ActivityHandle handle) {
    // TODO what to do here?
  }

  // Optional callback methods -------------------------------------------

  public void eventProcessingFailed(ActivityHandle handle, FireableEventType eventType, Object event, Address address, ReceivableService service, int flags, FailureReason reason) {
    if(tracer.isInfoEnabled()) {
      tracer.info("Diameter ShServer RA :: eventProcessingFailed :: handle[" + handle + "], eventType[" + eventType + "], event[" + event + "], address[" + address + "], flags[" + flags + "], reason[" + reason + "].");
    }
    if(!(handle instanceof DiameterActivityHandle)) {
      return;
    }

    processAfterEventDelivery(handle, eventType, event, address, service, flags);
  }

  public void eventProcessingSuccessful(ActivityHandle handle, FireableEventType eventType, Object event, Address address, ReceivableService service, int flags) {
    if(tracer.isInfoEnabled()) {
      tracer.info("Diameter ShServer RA :: eventProcessingSuccessful :: handle[" + handle + "], eventType[" + eventType + "], event[" + event + "], address[" + address + "], flags[" + flags + "].");
    }
    if(!(handle instanceof DiameterActivityHandle)) {
      return;
    }

    processAfterEventDelivery(handle, eventType, event, address, service, flags);
  }

  public void eventUnreferenced(ActivityHandle handle, FireableEventType eventType, Object event, Address address, ReceivableService service, int flags) {
    if(tracer.isFineEnabled()) {
      tracer.fine("Diameter ShServer RA :: eventUnreferenced :: handle[" + handle + "], eventType[" + eventType + "], event[" + event + "], address[" + address + "], service[" + service + "], flags[" + flags + "].");
    }
    if(!(handle instanceof DiameterActivityHandle)) {
      return;
    }

    processAfterEventDelivery(handle, eventType, event, address, service, flags);
  }

  private void processAfterEventDelivery(ActivityHandle handle, FireableEventType eventType, Object event, Address address, ReceivableService service,
      int flags) {
    DiameterActivityImpl activity = (DiameterActivityImpl) getActivity(handle);
    if (activity != null) {
      synchronized (activity) {
        if (activity.isTerminateAfterProcessing()) {
          activity.endActivity();
        }
      }
    }
  }

  public void activityEnded(ActivityHandle handle) {
    tracer.info("Diameter ShServer RA :: activityEnded :: handle[" + handle + ".");
    if(!(handle instanceof DiameterActivityHandle)) {
      return;
    }
    if(this.activities != null) {
      synchronized (this.activities) {
        this.activities.remove((DiameterActivityHandle) handle);
      }
    }
  }

  public void activityUnreferenced(ActivityHandle handle) {
    if(tracer.isFineEnabled()) {
      tracer.fine("Diameter ShServer RA :: activityUnreferenced :: handle[" + handle + "].");
    }
    //this.activityEnded(handle);
    if(handle instanceof DiameterActivityHandle) {
      this.endActivity((DiameterActivityHandle) handle);
    }
  }

  // Event and Activities management -------------------------------------

  public boolean fireEvent(Object event, ActivityHandle handle, FireableEventType eventID, Address address, boolean useFiltering, boolean transacted) {

    if (useFiltering && eventIDFilter.filterEvent(eventID)) {
      if (tracer.isFineEnabled()) {
        tracer.fine("Event " + eventID + " filtered");
      }
    }
    else if (eventID == null) {
      tracer.severe("Event ID for " + eventID + " is unknown, unable to fire.");
    }
    else {
      if (tracer.isFineEnabled()) {
        tracer.fine("Firing event " + event + " on handle " + handle);
      }
      try {
        /* TODO: Support transacted fire of events when in cluster
        if (transacted){
          this.raContext.getSleeEndpoint().fireEventTransacted(handle, eventID, event, address, null, EVENT_FLAGS);
        }
        else */{
          DiameterActivity activity = (DiameterActivity) getActivity(handle);
          if(activity instanceof ShServerActivityImpl)
          {
            ((ShServerActivityImpl)activity).fetchSessionData((DiameterMessage)event, true);
          }else if(activity instanceof ShServerSubscriptionActivityImpl)
          {
            ((ShServerSubscriptionActivityImpl)activity).fetchSessionData((DiameterMessage)event, true);
          }
          this.raContext.getSleeEndpoint().fireEvent(handle, eventID, event, address, null, EVENT_FLAGS);
        }       
        return true;
      }
      catch (Exception e) {
        tracer.severe("Error firing event.", e);
      }
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * @see org.mobicents.slee.resource.diameter.base.handlers.BaseSessionCreationListener#fireEvent(java.lang.String, org.jdiameter.api.Request, org.jdiameter.api.Answer)
   */
  public void fireEvent(String sessionId, Message message) {
    DiameterMessage event = (DiameterMessage) createEvent(message);

    FireableEventType eventId = eventIdCache.getEventId(eventLookup, message);

    this.fireEvent(event, getActivityHandle(sessionId), eventId, null, true, message.isRequest());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endActivity(DiameterActivityHandle handle) {
    sleeEndpoint.endActivity(handle);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startActivityRemoveTimer(DiameterActivityHandle handle) {
    this.activities.startActivityRemoveTimer(handle);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stopActivityRemoveTimer(DiameterActivityHandle handle) {
    this.activities.stopActivityRemoveTimer(handle);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(DiameterActivityHandle handle, DiameterActivity activity) {
    activities.update(handle, activity);
  }

  @Override
  public ApplicationId[] getSupportedApplications() {
    return null;
  }

  /**
   * Create Event object from a JDiameter message (request or answer)
   * 
   * @return a DiameterMessage object wrapping the request/answer
   * @throws OperationNotSupportedException
   */
  private DiameterMessage createEvent(Message message) {
    if (message == null) {
      throw new NullPointerException("Message argument cannot be null while creating event.");
    }

    int commandCode = message.getCommandCode();

    if (message.isError()) {
      return new ErrorAnswerImpl(message);
    }

    boolean isRequest = message.isRequest();

    switch (commandCode) {
    case PushNotificationRequest.commandCode: // PNR/PNA
      return isRequest ? new PushNotificationRequestImpl(message) : new PushNotificationAnswerImpl(message);
    case ProfileUpdateRequest.commandCode: // PUR/PUA
      return isRequest ? new ProfileUpdateRequestImpl(message) : new ProfileUpdateAnswerImpl(message);
    case SubscribeNotificationsRequest.commandCode: // SNR/SNA
      return isRequest ? new SubscribeNotificationsRequestImpl(message) : new SubscribeNotificationsAnswerImpl(message);
    case UserDataRequest.commandCode: // UDR/UDA
      return isRequest ? new UserDataRequestImpl(message) : new UserDataAnswerImpl(message);

    default:
      return new ExtensionDiameterMessageImpl(message);
    }
  }

  // Session Management --------------------------------------------------

  public void sessionCreated(ServerShSession session, boolean isSubscription) {
    // Make sure it's a new session and there's no activity created yet.
    if(this.getActivity(getActivityHandle(session.getSessions().get(0).getSessionId())) != null) {
      tracer.warning("Activity found for created Credit-Control Server Session. Shouldn't exist. Aborting.");
      return;
    }

    // Get Message Factories (for Base and ShServer)
    DiameterMessageFactoryImpl baseMsgFactory = new DiameterMessageFactoryImpl(session.getSessions().get(0), this.stack);
    ShServerMessageFactoryImpl shServerMsgFactory = new ShServerMessageFactoryImpl(baseMsgFactory, session.getSessions().get(0), this.stack, this.shAvpFactory);

    // Create Server Activity
    DiameterActivity activity = null;
    if(isSubscription) {
      ShServerSubscriptionActivityImpl _activity = new ShServerSubscriptionActivityImpl(shServerMsgFactory, this.shAvpFactory, session, null, null);
      _activity.setSessionListener(this);
      activity = _activity;
      activityCreated(activity, false);
    }
    else {
      ShServerActivityImpl _activity = new ShServerActivityImpl(shServerMsgFactory, this.shAvpFactory, session, null, null);
      _activity.setSessionListener(this);
      activity = _activity;
      activityCreated(activity, false);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.mobicents.slee.resource.diameter.sh.server.handlers.ShServerSessionListener#sessionDestroyed(java.lang.String, org.jdiameter.api.sh.ServerShSession)
   */
  public void sessionDestroyed(String sessionId, ServerShSession session) {
    try {
      this.sleeEndpoint.endActivity(getActivityHandle(sessionId));
    }
    catch (Exception e) {
      tracer.severe( "Failed to end activity with handle[" + getActivityHandle(sessionId) );
    }
  }

  /**
   * Method for performing tasks when activity is created, such as informing SLEE about it and storing into internal map.
   * 
   * @param ac the activity that has been created
   */
  private void activityCreated(DiameterActivity ac, boolean suspended) {
    try {
      // Inform SLEE that Activity Started
      DiameterActivityImpl activity = (DiameterActivityImpl) ac;

      if (suspended) {
        sleeEndpoint.startActivitySuspended(activity.getActivityHandle(), activity, MARSHALABLE_ACTIVITY_FLAGS);
      }
      else {
        sleeEndpoint.startActivity(activity.getActivityHandle(), activity, MARSHALABLE_ACTIVITY_FLAGS);
      }

      // Put it into our activities map
      activities.put(activity.getActivityHandle(), activity);

      if(tracer.isInfoEnabled()) {
        tracer.info("Activity started [" + activity.getActivityHandle() + "]");
      }
    }
    catch (Exception e) {
      tracer.severe("Error creating/starting activity.", e);
    }
  }

  // Others --------------------------------------------------------------

  /**
   * Retrieves the RA context
   */
  public ResourceAdaptorContext getRaContext() {
    return raContext;
  }

  // Private Methods -----------------------------------------------------

  /**
   * Initializes the RA Diameter Stack.
   * 
   * @throws Exception
   */
  private synchronized void initStack() throws Exception {
    // Register in the Mux as app listener.
    this.diameterMux.registerListener(this, (ApplicationId[]) authApplicationIds.toArray(new ApplicationId[authApplicationIds.size()]));

    // Get the stack (should not mess with)
    this.stack = this.diameterMux.getStack();
    this.messageTimeout = stack.getMetaData().getConfiguration().getLongValue(MessageTimeOut.ordinal(), (Long) MessageTimeOut.defValue());

    this.raProvider = new ShServerProviderImpl(this);
    this.shAvpFactory = new DiameterShAvpFactoryImpl(this.baseAvpFactory);

    if(tracer.isInfoEnabled()) {
      tracer.info("Diameter ShServer RA :: Successfully initialized stack.");
    }
  }
  /**
   * Based on env create inits {@link #activities} reference.
   */
  private void initActivitiesMgmt() {
    final DiameterRAInterface lst = this;
    if (this.ftRAContext.isLocal()) {
      // local mgmt;
      if(tracer.isInfoEnabled()) {
        tracer.info(raContext.getEntityName() + " -- running in LOCAL mode.");
      }
      this.activities = new LocalDiameterActivityManagement(this.raContext, activityRemoveDelay);
    }
    else {
      if(tracer.isInfoEnabled()) {
        tracer.info(raContext.getEntityName()+" -- running in CLUSTER mode.");
      }
      final org.mobicents.slee.resource.cluster.ReplicatedData<String, DiameterActivity> clusteredData = this.ftRAContext.getReplicateData(true);
      // get special one
      this.activities = new AbstractClusteredDiameterActivityManagement(this.ftRAContext, activityRemoveDelay,this.raContext.getTracer(""), stack, this.raContext.getSleeTransactionManager(), clusteredData) {

        @Override
        protected void performBeforeReturn(DiameterActivityImpl activity) {
          // do all the dirty work;
          try {
            Session session = null;
            if (activity.getClass().equals(DiameterActivityImpl.class)) {
              // check as first. since it requires session recreation.
              // JIC: is this required?
              session = this.diameterStack.getSessionFactory().getNewSession(activity.getSessionId());
              performBeforeReturnOnBase(activity, session);
              return;
            }
            else if(activity instanceof ShServerActivity) {
              ShServerActivityImpl sh = (ShServerActivityImpl) activity;
              ServerShSession appSession = this.diameterStack.getSession(activity.getSessionId(), ServerShSession.class);
              session = appSession.getSessions().get(0);
              performBeforeReturnOnBase(activity, session);
              performBeforeReturnSh(sh,session);
              sh.setSession(appSession);
            }
            else if(activity instanceof ShServerSubscriptionActivity) {
              ShServerSubscriptionActivityImpl sh = (ShServerSubscriptionActivityImpl) activity;
              ServerShSession appSession = this.diameterStack.getSession(activity.getSessionId(), ServerShSession.class);
              session = appSession.getSessions().get(0);
              performBeforeReturnOnBase(activity, session);
              performBeforeReturnSh(sh,session);
              sh.setSession(appSession);
            }
            else {
              throw new IllegalArgumentException("Unknown activity type: " + activity);
            }
          }
          catch (Exception e) {
            throw new DiameterException(e);
          }
        }

        private void performBeforeReturnSh(ShServerSubscriptionActivityImpl sh, Session session) {
          ShServerMessageFactoryImpl messageFactory = new ShServerMessageFactoryImpl(session, stack);
          sh.setServerMessageFactory(messageFactory);
          sh.setServerAvpFactory(shAvpFactory);
        }

        private void performBeforeReturnSh(ShServerActivityImpl sh, Session session) {
          ShServerMessageFactoryImpl messageFactory = new ShServerMessageFactoryImpl(session, stack);
          sh.setServerMessageFactory(messageFactory);
          sh.setServerAvpFactory(shAvpFactory);
        }

        private void performBeforeReturnOnBase(DiameterActivityImpl ac,Session session) {
          DiameterMessageFactoryImpl msgFactory = new DiameterMessageFactoryImpl(session, stack, new DiameterIdentity[] {});
          ac.setAvpFactory(baseAvpFactory);
          ac.setMessageFactory(msgFactory);
          ac.setCurrentWorkingSession(session);
          ac.setSessionListener(lst);
        }

        @Override
        public DiameterActivity get(DiameterActivityHandle handle) {
          return super.get(handle);
        }

        @Override
        public void put(DiameterActivityHandle handle, DiameterActivity activity) {
          super.put(handle, activity);
        }

        @Override
        public DiameterActivity remove(DiameterActivityHandle handle) {
          return super.remove(handle);
        }
      };
    }
  }

  protected DiameterActivityHandle getActivityHandle(String sessionId) {
    return new DiameterActivityHandle(sessionId);
  }

  // NetworkReqListener Implementation -----------------------------------

  public Answer processRequest(Request request) {
    try {
      raProvider.createActivity(request);
    }
    catch (Throwable e) {
      tracer.severe(e.getMessage(), e);
    }

    // returning null so we can answer later
    return null;
  }

  public void receivedSuccessMessage(Request req, Answer ans) {
    if(tracer.isFineEnabled()) {
      tracer.fine("Diameter ShServer RA :: receivedSuccessMessage :: " + "Request[" + req + "], Answer[" + ans + "].");
    }

    try {
      if(tracer.isInfoEnabled()) {
        tracer.info("Received Message Result-Code: " + ans.getResultCode().getUnsigned32());
      }
    }
    catch (AvpDataException ignore) {
      // ignore, this was just for informational purposes...
    }
  }

  public void timeoutExpired(Request req) {
    if(tracer.isInfoEnabled()) {
      tracer.info("Diameter ShServer RA :: timeoutExpired :: " + "Request[" + req + "].");
    }

    try {
      // Message delivery timed out - we have to remove activity
      ((DiameterActivity) getActivity(getActivityHandle(req.getSessionId()))).endActivity();
    }
    catch (Exception e) {
      tracer.severe("Failure processing timeout message.", e);
    }
  }

  // Provider Implementation ---------------------------------------------

  class ShServerProviderImpl implements ShServerProvider {
    private DiameterShServerResourceAdaptor ra = null;
    private Validator validator = new ValidatorImpl();

    private ArrayList<Integer> requestCodes = new ArrayList<Integer>();

    public ShServerProviderImpl(DiameterShServerResourceAdaptor diameterShServerResourceAdaptor) {
      this.ra = diameterShServerResourceAdaptor;

      requestCodes.add(SubscribeNotificationsRequest.commandCode);
      requestCodes.add(UserDataRequest.commandCode);
      requestCodes.add(ProfileUpdateRequest.commandCode);
    }

    // Here we only act on requests, answer don't concern us ?
    public DiameterActivity createActivity(Request request) throws CreateActivityException {
      if(!requestCodes.contains(request.getCommandCode())) {
        throw new CreateActivityException("Cant create activity for unknown command code: " + request.getCommandCode());
      }

      String sessionId = request.getSessionId();
      // FIXME: Get from request?
      ApplicationId appId = ApplicationId.createByAuthAppId(10415, 16777217);

      try {
        // In this case we have to pass so session factory know what to tell to session listener
        ShServerSessionImpl session = ((ISessionFactory) sessionFactory).getNewAppSession(sessionId, appId, ServerShSession.class, new Object[]{request});
        sessionCreated(session, request.getCommandCode() == SubscribeNotificationsRequest.commandCode);
        session.processRequest(request);
      }
      catch(Exception e) {
        throw new CreateActivityException(e);
      }

      return (DiameterActivity) getActivity(getActivityHandle(sessionId));
    }

    public ShServerMessageFactory getServerMessageFactory() {
      return new ShServerMessageFactoryImpl(stack);
    }

    public DiameterShAvpFactory getServerAvpFactory() {
      return shAvpFactory;
    }

    public PushNotificationAnswer pushNotificationRequest(PushNotificationRequest message) throws IOException {
      if (message == null) {
        throw new NullPointerException("Message argument cannot be null.");
      }

      try {
        String sessionID = message.getSessionId();

        if (sessionID == null) {
          throw new NullPointerException("Session-Id for message is null, cannot proceed.");
        }

        Session session = stack.getSessionFactory().getNewSession(sessionID);
        Future<Message> f = session.send(((DiameterMessageImpl) message).getGenericData());
        session.release();
        return new PushNotificationAnswerImpl(f.get());
      }
      catch (AvpNotAllowedException e) {
        throw e;
      }
      catch (Exception e) {
        throw new IOException("Failed to send due to: " + e);
      }
    }

    public DiameterIdentity[] getConnectedPeers() {
      return ra.getConnectedPeers();
    }

    public int getPeerCount() {
      return ra.getConnectedPeers().length;
    }

    /* (non-Javadoc)
     * @see net.java.slee.resource.diameter.sh.server.ShServerProvider#getValidator()
     */
    @Override
    public Validator getValidator() {
      return this.validator;
    }

  }

  public DiameterIdentity[] getConnectedPeers() {
    if (stack != null) {
      try {
        // Get the list of peers from the stack
        List<Peer> peers = stack.unwrap(PeerTable.class).getPeerTable();

        DiameterIdentity[] result = new DiameterIdentity[peers.size()];

        int i = 0;

        // Get each peer from the list and make a DiameterIdentity
        for (Peer peer : peers) {
          DiameterIdentity identity = new DiameterIdentity(peer.getUri().toString());
          result[i++] = identity;
        }

        return result;
      }
      catch (Exception e) {
        tracer.severe("Failure getting peer list.", e);
      }
    }

    return null;
  }

  public int getPeerCount() {
    return getConnectedPeers().length;
  }

  public long getMessageTimeout() {
    return this.messageTimeout; 
  }
}
