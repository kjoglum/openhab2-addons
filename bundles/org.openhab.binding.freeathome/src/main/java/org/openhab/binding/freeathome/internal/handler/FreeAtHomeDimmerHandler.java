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
import org.openhab.binding.freeathome.internal.config.FreeAtHomeDimmerConfig;
import org.openhab.binding.freeathome.internal.model.DefaultOnOffTypeConverter;
import org.openhab.binding.freeathome.internal.model.DefaultPercentTypeConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeDimmerHandler} represents a dimmer
 *
 * @author Stian Kjoglum - Initial contribution
 */

public class FreeAtHomeDimmerHandler extends FreeAtHomeBaseHandler {

    public FreeAtHomeDimmerHandler(Thing thing) {
        super(thing);
    }

    private Logger logger = LoggerFactory.getLogger(FreeAtHomeDimmerHandler.class);

    private FreeAtHomeDimmerConfig mConfiguration = new FreeAtHomeDimmerConfig();

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

            if (channelUID.getId().equals(FreeAtHomeBindingConstants.DIMMER_SWITCH_THING_CHANNEL)) {
                OnOffType udCommand = (OnOffType) command;
                String channel = mConfiguration.deviceId + "/" + mConfiguration.channelId + "/"
                        + mConfiguration.dataPointIdSwitch;

                if (udCommand.equals(OnOffType.ON)) {

                    logger.debug("Switch ON {}", channel);
                    bridge.setDataPoint(channel, "1");

                }
                if (udCommand.equals(OnOffType.OFF)) {

                    logger.debug("Switch OFF {}", channel);
                    bridge.setDataPoint(channel, "0");

                }
            }
        }

        /*
         * Stop Move
         */
        if (command instanceof StopMoveType
                && channelUID.getId().equals(FreeAtHomeBindingConstants.DIMMER_FADING_THING_CHANNEL)) {
            StopMoveType udCommand = (StopMoveType) command;
            if (udCommand.equals(StopMoveType.STOP)) {
                String channel = mConfiguration.deviceId + "/" + mConfiguration.channelId + "/"
                        + mConfiguration.dataPointIdFade;
                String raffstoreSwitch = "Fading STOP " + channel + " 0";
                logger.debug("Event {}", raffstoreSwitch);
                bridge.setDataPoint(channel, "0");
            }
        }

        /*
         * UpDownCommand
         */
        if (command instanceof UpDownType) {
            UpDownType udCommand = (UpDownType) command;

            if (channelUID.getId().equals(FreeAtHomeBindingConstants.DIMMER_FADING_THING_CHANNEL)) {
                String channel = mConfiguration.deviceId + "/" + mConfiguration.channelId + "/"
                        + mConfiguration.dataPointIdFade;

                logger.info("Called channel fading {}", channel);

                if (udCommand.equals(UpDownType.UP)) {
                    logger.debug("Fading UP");
                    bridge.setDataPoint(channel, "9");
                } else {
                    logger.debug("Fading DOWN");
                    bridge.setDataPoint(channel, "1");
                }
            } // stepwise
        }

        /*
         * Value
         */
        if (command instanceof DecimalType) {

            // Switch on/off
            if (channelUID.getId().equals(FreeAtHomeBindingConstants.DIMMER_VALUE_THING_CHANNEL)) {
                DecimalType udCommand = (DecimalType) command;
                String channel = mConfiguration.deviceId + "/" + mConfiguration.channelId + "/"
                        + mConfiguration.dataPointIdValue;

                String dimmerSwitch = "Set target value: " + channel + " value(" + udCommand.toString() + ")";
                logger.debug("Target Value {}", dimmerSwitch);
                bridge.setDataPoint(channel, udCommand.toString());
            }
        }
    }

    @Override
    public void tearDown() {
    }

    @Override
    public void setUp() {
        mConfiguration = getConfigAs(FreeAtHomeDimmerConfig.class);

        // Fetch bridge on initialization to get proper state
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();
        if (bridge != null) {

            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this,
                    FreeAtHomeBindingConstants.DIMMER_SWITCH_THING_CHANNEL, new DefaultOnOffTypeConverter(),
                    mConfiguration.deviceId, mConfiguration.channelId, mConfiguration.dataPointIdSwitchUpdate));

            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this, FreeAtHomeBindingConstants.DIMMER_VALUE_THING_CHANNEL,
                    new DefaultPercentTypeConverter(), mConfiguration.deviceId, mConfiguration.channelId,
                    mConfiguration.dataPointIdValueUpdate));
        } // dummy call to avoid optimization
    }
}
