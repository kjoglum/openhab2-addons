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

import static org.openhab.binding.freeathome.internal.FreeAtHomeBindingConstants.*;

import org.openhab.binding.freeathome.internal.FreeAtHomeBindingConstants;
import org.openhab.binding.freeathome.internal.FreeAtHomeUpdateChannel;
import org.openhab.binding.freeathome.internal.config.FreeAtHomeRaffStoreConfig;
import org.openhab.binding.freeathome.internal.model.DefaultPercentTypeConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeRaffStoreHandler} represents raffstore switch or group
 *
 * @author Stian Kjoglum - Initial contribution
 */
public class FreeAtHomeRaffStoreHandler extends FreeAtHomeBaseHandler {

    private Logger logger = LoggerFactory.getLogger(FreeAtHomeRaffStoreHandler.class);

    private FreeAtHomeRaffStoreConfig mConfiguration = new FreeAtHomeRaffStoreConfig();

    public FreeAtHomeRaffStoreHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();

        if (bridge == null) {
            logger.error("No bridge connected");
            return;
        }

        /*
         * Value
         */
        if (command instanceof DecimalType) {

            if (channelUID.getId().equals(FreeAtHomeBindingConstants.RAFFSTORE_THING_CHANNEL_PERCENTAGE)) {
                DecimalType udCommand = (DecimalType) command;
                String channel = mConfiguration.DeviceId + "/" + mConfiguration.ChannelId + "/"
                        + mConfiguration.InputIdPercentage;
                String raffstorePercentage = channel + " value(" + udCommand.toString() + ")";
                logger.debug("Set percentage value {}: ", raffstorePercentage);
                bridge.setDataPoint(channel, udCommand.toString());
            }
            if (channelUID.getId().equals(FreeAtHomeBindingConstants.RAFFSTORE_THING_CHANNEL_SHUTTERPERCENTAGE)) {
                DecimalType udCommand = (DecimalType) command;
                String channel = mConfiguration.DeviceId + "/" + mConfiguration.ChannelId + "/"
                        + mConfiguration.InputShutterPercentage;
                String shutterPercentage = channel + " value(" + udCommand.toString() + ")";
                logger.debug("Set shutterpercentage value {}: ", shutterPercentage);
                bridge.setDataPoint(channel, udCommand.toString());
            }
        }

        if (command instanceof StopMoveType && channelUID.getId().equals(RAFFSTORE_THING_CHANNEL_COMPLETE)) {
            StopMoveType udCommand = (StopMoveType) command;
            if (udCommand.equals(StopMoveType.STOP)) {
                String channel = mConfiguration.DeviceId + "/" + mConfiguration.ChannelId + "/"
                        + mConfiguration.InputIdStepwise;

                logger.debug("Stop complete run {}", channel);
                bridge.setDataPoint(channel, "1");
            }
        }

        /*
         * UpDownCommand
         */
        if (command instanceof UpDownType) {
            UpDownType udCommand = (UpDownType) command;

            if (channelUID.getId().equals(RAFFSTORE_THING_CHANNEL_STEPWISE)) {
                String channel = mConfiguration.DeviceId + "/" + mConfiguration.ChannelId + "/"
                        + mConfiguration.InputIdStepwise;

                logger.info("Called channel STEPWISE {}", channel);

                if (udCommand.equals(UpDownType.UP)) {
                    logger.debug("STEPWISE UP");
                    bridge.setDataPoint(channel, "0");
                } else {
                    logger.debug("STEPWISE DOWN");
                    bridge.setDataPoint(channel, "1");
                }
            }

            if (channelUID.getId().equals(RAFFSTORE_THING_CHANNEL_COMPLETE)) {
                String channel = mConfiguration.DeviceId + "/" + mConfiguration.ChannelId + "/"
                        + mConfiguration.InputIdComplete;

                logger.debug("Called channel COMPLETE {}", channel);

                if (udCommand.equals(UpDownType.UP)) {
                    logger.debug("COMPLETE UP");
                    bridge.setDataPoint(channel, "0");
                } else {
                    logger.debug("COMPLETE DOWN");
                    bridge.setDataPoint(channel, "1");
                }
            }
        }
    }

    @Override
    public void setUp() {
        mConfiguration = getConfigAs(FreeAtHomeRaffStoreConfig.class);

        // Fetch bridge on initialization to get proper state
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();
        if (bridge != null) {

        }

        mUpdateChannels.add(
                new FreeAtHomeUpdateChannel(this, RAFFSTORE_THING_CHANNEL_PERCENTAGE, new DefaultPercentTypeConverter(),
                        mConfiguration.DeviceId, mConfiguration.ChannelId, mConfiguration.OutputIdPercentage));
        mUpdateChannels.add(new FreeAtHomeUpdateChannel(this, RAFFSTORE_THING_CHANNEL_SHUTTERPERCENTAGE,
                new DefaultPercentTypeConverter(), mConfiguration.DeviceId, mConfiguration.ChannelId,
                mConfiguration.OutputShutterPercentage));
    }

    @Override
    public void tearDown() {
    }
}
