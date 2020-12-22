/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.freeathome.internal.handler;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.openhab.binding.freeathome.internal.FreeAtHomeBindingDiscoveryService;
import org.openhab.binding.freeathome.internal.FreeAtHomeUpdateHandler;
import org.openhab.binding.freeathome.internal.config.FreeAtHomeBridgeConfig;
import org.openhab.binding.freeathome.internal.xmpp.rocks.extension.abb.com.protocol.update.Update;
import org.openhab.binding.freeathome.internal.xmpp.rocks.extension.abb.com.protocol.update.UpdateEvent;
import org.openhab.binding.freeathome.internal.xmpp.rocks.extension.abb.com.protocol.update.UpdateManager;
import org.openhab.binding.freeathome.internal.xmpp.rocks.extensions.abb.com.protocol.data.Channel;
import org.openhab.binding.freeathome.internal.xmpp.rocks.extensions.abb.com.protocol.data.DataPoint;
import org.openhab.binding.freeathome.internal.xmpp.rocks.extensions.abb.com.protocol.data.Device;
import org.openhab.binding.freeathome.internal.xmpp.rocks.extensions.abb.com.protocol.data.Project;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.bind.model.Bind;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.sasl.model.Abort;
import rocks.xmpp.core.sasl.model.Auth;
import rocks.xmpp.core.sasl.model.Challenge;
import rocks.xmpp.core.sasl.model.Failure;
import rocks.xmpp.core.sasl.model.Mechanisms;
import rocks.xmpp.core.sasl.model.Response;
import rocks.xmpp.core.sasl.model.Success;
import rocks.xmpp.core.session.Extension;
import rocks.xmpp.core.session.SessionStatusEvent;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.core.stanza.IQEvent;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.core.stanza.model.client.ClientPresence;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.extensions.caps.model.EntityCapabilities1;
import rocks.xmpp.extensions.caps2.model.EntityCapabilities2;
import rocks.xmpp.extensions.component.accept.model.Handshake;
import rocks.xmpp.extensions.disco.model.info.InfoDiscovery;
import rocks.xmpp.extensions.featureneg.model.FeatureNegotiation;
import rocks.xmpp.extensions.forward.StanzaForwardingManager;
import rocks.xmpp.extensions.forward.model.Forwarded;
import rocks.xmpp.extensions.hashes.model.Hash;
import rocks.xmpp.extensions.idle.model.Idle;
import rocks.xmpp.extensions.ping.PingManager;
import rocks.xmpp.extensions.ping.handler.PingHandler;
import rocks.xmpp.extensions.ping.model.Ping;
import rocks.xmpp.extensions.pubsub.PubSubManager;
import rocks.xmpp.extensions.pubsub.model.Item;
import rocks.xmpp.extensions.pubsub.model.PubSub;
import rocks.xmpp.extensions.pubsub.model.event.Event;
import rocks.xmpp.extensions.rpc.client.ClientRpcManager;
import rocks.xmpp.extensions.rpc.model.Rpc;
import rocks.xmpp.extensions.rpc.model.Value;
import rocks.xmpp.extensions.shim.client.ClientHeaderManager;
import rocks.xmpp.extensions.si.StreamInitiationManager;
import rocks.xmpp.extensions.si.model.StreamInitiation;
import rocks.xmpp.extensions.sm.client.ClientStreamManager;
import rocks.xmpp.extensions.sm.model.StreamManagement;
import rocks.xmpp.im.roster.RosterEvent;
import rocks.xmpp.im.roster.RosterManager;
import rocks.xmpp.im.roster.model.Contact;
import rocks.xmpp.im.roster.model.DefinedState;
import rocks.xmpp.im.roster.model.Roster;
import rocks.xmpp.im.subscription.PresenceManager;
import rocks.xmpp.websocket.net.client.WebSocketConnectionConfiguration;

/**
 * Handler that connects to FreeAtHome gateway.
 *
 * @author Stian Kjoglum - Initial contribution
 *
 */
public class FreeAtHomeBridgeHandler extends BaseBridgeHandler {

    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FreeAtHomeBridgeHandler.class);

    private XmppClient mXmppClient;
    private XmppSessionConfiguration mXmppConfiguration;
    private final Properties httpHeader = new Properties();
    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(20);
    private String resp;
    private boolean isConnected = false;

    /*
     * Store bridge configuration
     */
    protected FreeAtHomeBridgeConfig mConfiguration;

    protected FreeAtHomeUpdateHandler mUpdateHandler;

    public FreeAtHomeBridgeHandler(Bridge bridge) {
        super(bridge);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initialize() {

        FreeAtHomeBridgeConfig configuration = getConfigAs(FreeAtHomeBridgeConfig.class);

        mConfiguration = configuration;

        logger.debug("Gateway IP            {}.", mConfiguration.host);
        logger.debug("Port                  {}.", mConfiguration.port);
        logger.debug("Login                 {}.", mConfiguration.login);
        logger.debug("Password              {}.", mConfiguration.password);
        logger.debug("dummy_things_enabled  {}.", mConfiguration.dummy_things_enabled);
        logger.debug("console_debugger_enabled  {}.", mConfiguration.console_debugger_enabled);

        mUpdateHandler = new FreeAtHomeUpdateHandler();

        connectGateway();
    }

    public boolean dummyThingsEnabled() {
        return mConfiguration.dummy_things_enabled;
    }

    public String getResponse() {
        return this.resp;
    }

    public boolean isOnline() {
        return this.isConnected;
    }

    @Override
    public void dispose() {

        onConnectionLost(ThingStatusDetail.CONFIGURATION_ERROR, "Bridge removed");

        mUpdateHandler = null;

        try {
            mXmppClient.close();
        } catch (XmppException e) {
            logger.error("Problems closing XMPP Client: {}", e.toString());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            handleCommand(channelUID, command);
        } catch (Exception e) {
            logger.warn("No bridge commands defined. Cannot process: {}", command);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(FreeAtHomeBindingDiscoveryService.class);
    }

    /*
     * Call set data point via XMPP service
     */
    public void setDataPoint(String adress, String value) {

        try {
            mXmppClient.getManager(ClientRpcManager.class).call(Jid.of("mrha@busch-jaeger.de/rpc"),
                    "RemoteInterface.setDatapoint", Value.of(adress), Value.of(value)).getResult();

        } catch (XmppException e) {
            logger.debug("XMPP Exception: {}", e.toString());
        }
    }

    public void getAll() {
        try {
            resp = mXmppClient
                    .getManager(ClientRpcManager.class).call(Jid.of("mrha@busch-jaeger.de/rpc"),
                            "RemoteInterface.getAll", Value.of("de"), Value.of(4), Value.of(0), Value.of(0))
                    .getResult().getAsString();
            logger.debug("Message GetAll: {} ", resp);

        } catch (XmppException e) {
            logger.debug("XMPP Exception: {}", e.getMessage());
        }
    }

    private void connectGateway() {
        // If old session is still connected -> close xmpp session
        if (mXmppClient != null) {
            try {
                mXmppClient.close();
            } catch (XmppException e) {
                logger.error("Error closing existing Client Connection: {}", e.toString());
            }
        }
        mXmppConfiguration = XmppSessionConfiguration.builder()
                .extensions(Extension.of("http://abb.com/protocol/update", null, true, true, Update.class),
                        Extension.of("http://abb.com/protocol/update", null, true, Update.class),
                        Extension.of(Handshake.class), Extension.of(Abort.class), Extension.of(Auth.class),
                        Extension.of(Challenge.class), Extension.of(Failure.class), Extension.of(Mechanisms.class),
                        Extension.of(Response.class), Extension.of(Success.class), Extension.of(StreamFeatures.class),
                        Extension.of(Bind.class), Extension.of(ClientMessage.class), Extension.of(ClientPresence.class),
                        Extension.of(ClientIQ.class), Extension.of(Roster.class), Extension.of(PubSub.class),
                        Extension.of(Ping.class), Extension.of(Contact.class), Extension.of(DefinedState.class),
                        Extension.of(InfoDiscovery.class), Extension.of(EntityCapabilities1.class),
                        Extension.of(EntityCapabilities2.class), Extension.of(Hash.class), Extension.of(Event.class),
                        Extension.of(Value.class), Extension.of(ClientRpcManager.class, false), Extension.of(Rpc.class),
                        Extension.of(FeatureNegotiation.NAMESPACE, true),
                        Extension.of(StreamInitiation.NAMESPACE, StreamInitiationManager.class, true),
                        Extension.of(ClientHeaderManager.class, false),
                        Extension.of(StreamManagement.NAMESPACE, ClientStreamManager.class, false),
                        Extension.of(new PingHandler(), PingManager.class, true),
                        Extension.of(Forwarded.NAMESPACE, StanzaForwardingManager.class, false),
                        Extension.of(Idle.NAMESPACE, true))
                .debugger(mConfiguration.console_debugger_enabled ? ConsoleDebugger.class : null)
                .authenticationMechanisms("SCRAM-SHA-1").defaultResponseTimeout(Duration.ofSeconds(30)).build();

        WebSocketConnectionConfiguration mWebSocketConfiguration = WebSocketConnectionConfiguration.builder()
                .hostname(mConfiguration.host).port(mConfiguration.port).path("/xmpp-websocket/")
                .channelEncryption(ChannelEncryption.DISABLED).connectTimeout(30000).build();

        mXmppClient = XmppClient.create("busch-jaeger.de", mXmppConfiguration, mWebSocketConfiguration);

        // Listen for inbound messages.
        mXmppClient.addInboundMessageListener(e -> onMessageEvent(e));
        mXmppClient.addOutboundMessageListener(e -> onMessageEvent(e));
        mXmppClient.addInboundIQListener(e -> onIQEvent(e));
        mXmppClient.getManager(RosterManager.class).addRosterListener(e -> onRosterEvent(e));

        // Listen for inbound presence.
        mXmppClient.addInboundPresenceListener(e -> onPresenceEvent(e));

        mXmppClient.addSessionStatusListener(e -> onUpdateXMPPStatus(e));

        PubSubManager pubSubManager = mXmppClient.getManager(PubSubManager.class);
        pubSubManager.setEnabled(true);

        UpdateManager updateManager = mXmppClient.getManager(UpdateManager.class);
        updateManager.setEnabled(true);

        updateManager.addUpdateListener(e -> onUpdateEvent(e));

        // Connect XMPP client over websocket layer and login to SysAp
        try {
            String jid = getJid(mConfiguration.login);
            logger.info("Found JID: {}", Jid.of(jid));

            if (jid != null) {
                Jid From = Jid.of(jid);

                try {
                    logger.info("Connecting to XMPP Client");
                    mXmppClient.connect(From);

                    onConnectionEstablished();
                } catch (Exception e) {
                    logger.warn("Problems connecting to SysAp: {}", e.getMessage());
                    onConnectionLost(ThingStatusDetail.COMMUNICATION_ERROR,
                            "Can not connect to SysAP with address: " + mConfiguration.host);
                    return;
                }
                try {
                    String id = From.toString().split("@")[0];
                    logger.info("Logging in to SysAp: {}", id);
                    mXmppClient.login(id, mConfiguration.password);

                    Presence presence = new Presence(Jid.of("mrha@busch-jaeger.de/rpc"), Presence.Type.SUBSCRIBE, null,
                            null, (byte) 0, null, Jid.of(id), null, null, null);
                    logger.debug("Presence update: {}", presence.toString());

                    mXmppClient.send(presence);

                } catch (Exception e) {
                    logger.warn("Problems logging in to SysAp: {}", e.getMessage());
                    onConnectionLost(ThingStatusDetail.CONFIGURATION_ERROR,
                            "Login on SysAP with login name: " + mConfiguration.login);

                    try {
                        mXmppClient.close();
                    } catch (XmppException e2) {
                        logger.error("Ops! {}", e2.toString());
                    }
                    return;
                }
            } else {
                logger.warn("No SysAp account (JID) found for provided username: Verify username");
            }

        } catch (Exception e) {
            logger.warn("Problems getting JID: {}", e.getMessage());
        }
    }

    private String getJid(String userName) throws Exception {

        String foundJid = null;
        /*
         * Read settings.json from SysAP
         */
        String url = "http://" + mConfiguration.host + "/settings.json"; // settings stores mapping to jid
        String USER_AGENT = "Mozilla/5.0";

        httpHeader.put("User-Agent", USER_AGENT);
        String jsonResponse = HttpUtil.executeUrl("GET", url, httpHeader, null, null, REQUEST_TIMEOUT);
        logger.debug("JSON Response (JID): {}", jsonResponse);

        /*
         * Parse json to find mapping to jid
         */
        JsonObject myObject = (JsonObject) new JsonParser().parse(jsonResponse);
        JsonArray myArray = myObject.getAsJsonArray("users");

        for (int i = 0; i < myArray.size(); i++) {
            String login = myArray.get(i).getAsJsonObject().get("name").toString().replaceAll("\"", "");
            String jid = myArray.get(i).getAsJsonObject().get("jid").toString().replaceAll("\"", "");
            if (login.equals(userName)) {
                foundJid = jid;
            }
        }
        return foundJid;
    }

    private void onConnectionEstablished() {
        logger.debug("Bridge connected. Updating thing status to ONLINE.");
        updateStatus(ThingStatus.ONLINE);
        isConnected = true;
    }

    private void onConnectionLost(ThingStatusDetail detail, String msg) {
        logger.debug("Bridge connection lost. Updating thing status to OFFLINE.");
        updateStatus(ThingStatus.OFFLINE, detail, msg);
        isConnected = false;
    }

    private void onUpdateXMPPStatus(SessionStatusEvent e) {
        if (e.getStatus() == XmppClient.Status.DISCONNECTED) {
            onConnectionLost(ThingStatusDetail.BRIDGE_OFFLINE, "XMPP connection lost");
            logger.debug("Connection lost {}", e);
        }

        if (e.getStatus() == XmppClient.Status.AUTHENTICATED) {
            onConnectionEstablished();
            logger.debug("Connection authenticated: {}", e);
        }
    }

    /*
     * XMPP Event handlers
     */
    private void onPresenceEvent(PresenceEvent e) {
        logger.debug("PresenceEvent Handler called: {}", e.getPresence().toString());
        Presence presence = e.getPresence();
        if (presence.getType() == Presence.Type.SUBSCRIBE) {
            logger.debug("Presence subscribed: {}", presence);
        }
        mXmppClient.getManager(PresenceManager.class).approveSubscription(presence.getFrom());
    }

    /**
     * When an XMPP message is received from the bridge. It will be parsed to an XML string.
     *
     * TODO can be used to react on pressed switches and status update of raffstores.
     *
     * @param e
     */
    private void onMessageEvent(MessageEvent e) {
        logger.debug("MessageEvent Handler called: {}", e.getMessage());
        Message message = e.getMessage();
        Event event = message.getExtension(Event.class);

        logger.debug("Namespace {}", event.getNode());
        if (Update.NAMESPACE.equals(event.getNode())) {
            for (Item item : event.getItems()) {

                logger.debug("Payload of Item {}", item.getPayload());

                if (item.getPayload() instanceof org.openhab.binding.freeathome.internal.xmpp.rocks.extension.abb.com.protocol.update.Update) {
                    org.openhab.binding.freeathome.internal.xmpp.rocks.extension.abb.com.protocol.update.Update updateData = (org.openhab.binding.freeathome.internal.xmpp.rocks.extension.abb.com.protocol.update.Update) item
                            .getPayload();
                    String data = updateData.getData().replace("&amp;", "&").replace("&apos;", "'").replace("&lt;", "<")
                            .replace("&gt;", ">").replace("&quot;", "\"");
                    logger.debug("UpdateEvent {}", data);

                    try {
                        Project p = org.openhab.binding.freeathome.internal.xmpp.rocks.extensions.abb.com.protocol.data.Project
                                .builder().build(data);

                        // create JAXB context and instantiate marshaller
                        JAXBContext context = JAXBContext.newInstance(
                                org.openhab.binding.freeathome.internal.xmpp.rocks.extensions.abb.com.protocol.data.Project.class);
                        Marshaller m = context.createMarshaller();
                        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

                        try {
                            List<Device> devices = p.getDevices();
                            for (int i = 0; i < devices.size(); i++) {
                                Device currentDevice = devices.get(i);

                                logger.debug("Update From {}", currentDevice.getSerialNumber());
                                List<Channel> channels = currentDevice.getChannels();

                                for (int j = 0; j < channels.size(); j++) {
                                    Channel channel = channels.get(j);

                                    /*
                                     * Outputs
                                     */
                                    List<DataPoint> dataPointsOut = channel.getOutputs();
                                    for (int d = 0; d < dataPointsOut.size(); d++) {
                                        DataPoint datapoint = dataPointsOut.get(d);
                                        String dataPoint = "Serial: " + currentDevice.getSerialNumber() + " Channel: "
                                                + channel.getI() + " DataPoint: " + datapoint.getI() + " Value: "
                                                + datapoint.getValue();
                                        logger.debug("Datapoint {}", dataPoint);

                                        mUpdateHandler.NotifyThing(currentDevice.getSerialNumber(), channel.getI(),
                                                datapoint.getI(), datapoint.getValue());
                                    }
                                }
                            }

                        } catch (Exception e2) {
                            logger.error("Ops!", e2);
                            logger.error("Exception {}", e2.getMessage());
                        }

                    } catch (JAXBException e1) {
                        // TODO Auto-generated catch block
                        logger.error("Ops!", e1);
                        logger.error("JaxbException {}", e1.getMessage());
                    } catch (Exception ex) {
                        logger.error("General Exception {}", ex.getMessage());
                    }

                    // ...
                } else {
                    logger.debug("Payload is not instance of extension.abb.com.protocol.update.Update");
                }
            }
        } else {
            logger.debug("Message does not have namespace" + Update.NAMESPACE);
        }
    }

    private void onIQEvent(IQEvent e) {
        logger.debug("IQEvent Handler called: {}", e.getIQ());
        IQ iq = e.getIQ();
        if (iq.getType() == IQ.Type.SET) {
            IQ sendIq = new IQ(iq.getFrom(), IQ.Type.RESULT, null, iq.getId(), iq.getTo(), iq.getLanguage(), null);
            mXmppClient.send(sendIq);
        }
    }

    private void onRosterEvent(RosterEvent e) {
        logger.debug("RosterEvent Handler called: {}", e);
    }

    private void onUpdateEvent(UpdateEvent e) {
        logger.debug("UpdateEvent: {}", e);
    }
}
