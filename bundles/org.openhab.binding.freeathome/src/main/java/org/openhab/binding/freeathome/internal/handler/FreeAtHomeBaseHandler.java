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

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.freeathome.internal.FreeAtHomeUpdateChannel;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeBaseHandler} is responsible for handling common commands, which are
 * relevant for all things
 *
 * @author Stian Kjoglum - Initial contribution
 */
public abstract class FreeAtHomeBaseHandler extends BaseThingHandler {

    protected List<FreeAtHomeUpdateChannel> mUpdateChannels;
    private Logger logger = LoggerFactory.getLogger(FreeAtHomeBaseHandler.class);

    protected FreeAtHomeBaseHandler(Thing thing) {
        super(thing);
        mUpdateChannels = new ArrayList<FreeAtHomeUpdateChannel>();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(bridgeStatusInfo.getStatus());
        } else {
            updateStatus(bridgeStatusInfo.getStatus(), ThingStatusDetail.BRIDGE_OFFLINE,
                    bridgeStatusInfo.getDescription());
        }
    }

    protected FreeAtHomeBridgeHandler getFreeAtHomeBridge() {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge available");
            return null;
        }

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof FreeAtHomeBridgeHandler) {
            FreeAtHomeBridgeHandler h = (FreeAtHomeBridgeHandler) handler;
            updateStatus(h.getThing().getStatus(), h.getThing().getStatusInfo().getStatusDetail());
            return h;
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge of incorrect type");
        return null;
    }

    public void notifyUpdate(ChannelUID cUID, State state) {
        updateState(cUID, state);
    }

    public void registerUpdate() {

        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();

        if (bridge == null) {
            logger.error("No bridge found -> nothing to register");
            return;
        }

        for (int i = 0; i < mUpdateChannels.size(); i++) {
            logger.debug("Register {}", mUpdateChannels.get(i).toString());
            bridge.mUpdateHandler.Register(mUpdateChannels.get(i));
        }
    }

    public void unregisterUpdate() {
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();

        if (bridge == null) {
            logger.error("No bridge found -> nothing to register");
            return;
        }

        for (int i = 0; i < mUpdateChannels.size(); i++) {
            logger.debug("Register {}", mUpdateChannels.get(i).toString());
            bridge.mUpdateHandler.Unregister(mUpdateChannels.get(i));
        }
    }

    /*
     * Called during initialize
     */
    public abstract void setUp();

    /*
     * Called during dispose
     */
    public abstract void tearDown();

    @Override
    public void initialize() {
        this.setUp();
        this.registerUpdate();
    }

    @Override
    public void dispose() {
        this.tearDown();
        this.unregisterUpdate();
    }
}
