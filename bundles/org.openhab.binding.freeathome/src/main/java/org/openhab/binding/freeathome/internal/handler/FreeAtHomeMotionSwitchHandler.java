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

import org.openhab.binding.freeathome.internal.FreeAtHomeBindingConstants;
import org.openhab.binding.freeathome.internal.FreeAtHomeUpdateChannel;
import org.openhab.binding.freeathome.internal.config.FreeAtHomeMotionConfig;
import org.openhab.binding.freeathome.internal.model.DefaultDecimalTypeConverter;
import org.openhab.binding.freeathome.internal.model.DefaultOnOffTypeConverter;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeMotionSwitchHandler} represents virtual switch
 *
 * @author Stian Kjoglum - Initial contribution
 */

public class FreeAtHomeMotionSwitchHandler extends FreeAtHomeBaseHandler {

    public FreeAtHomeMotionSwitchHandler(Thing thing) {
        super(thing);
    }

    private Logger logger = LoggerFactory.getLogger(FreeAtHomeMotionSwitchHandler.class);

    private FreeAtHomeMotionConfig mConfiguration = new FreeAtHomeMotionConfig();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();

        if (bridge == null) {
            logger.error("No bridge connected");
            return;
        }

        /*
         * UpDownCommand
         */
        if (command instanceof OnOffType) {

            if (channelUID.getId().equals(FreeAtHomeBindingConstants.MOTIONSWITCH_THING_CHANNEL)) {
                OnOffType udCommand = (OnOffType) command;
                String channel = mConfiguration.deviceId + "/" + mConfiguration.channelId + "/"
                        + mConfiguration.dataPointIdSwitch;

                if (udCommand.equals(OnOffType.ON)) {

                    logger.debug("Motion Switch ON {}", channel);
                    bridge.setDataPoint(channel, "1");

                }
                if (udCommand.equals(OnOffType.OFF)) {

                    logger.debug("Motion Switch OFF {}", channel);
                    bridge.setDataPoint(channel, "0");

                }
            }
        }
    }

    @Override
    public void tearDown() {
    }

    @Override
    public void setUp() {
        mConfiguration = getConfigAs(FreeAtHomeMotionConfig.class);

        // Fetch bridge on initialization to get proper state
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();
        if (bridge != null) {
            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this, FreeAtHomeBindingConstants.MOTIONSWITCH_THING_CHANNEL,
                    new DefaultOnOffTypeConverter(), mConfiguration.deviceId, mConfiguration.channelId,
                    mConfiguration.dataPointIdSwitchUpdate));

            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this, FreeAtHomeBindingConstants.MOTIONLUX_THING_CHANNEL,
                    new DefaultDecimalTypeConverter(), mConfiguration.deviceId, mConfiguration.channelId,
                    mConfiguration.dataPointIdLuxUpdate));
        }
    }
}
