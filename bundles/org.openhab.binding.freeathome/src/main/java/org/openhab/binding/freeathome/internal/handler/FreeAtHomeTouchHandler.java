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
import org.openhab.binding.freeathome.internal.config.FreeAtHomeTouchConfig;
import org.openhab.binding.freeathome.internal.model.DefaultDecimalTypeConverter;
import org.openhab.binding.freeathome.internal.model.DefaultOnOffTypeConverter;
import org.openhab.binding.freeathome.internal.model.DefaultPercentTypeConverter;
import org.openhab.binding.freeathome.internal.model.StateConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeTouchHandler} represents touch with thermostat
 *
 * @author Stian Kjoglum - Initial contribution
 */
public class FreeAtHomeTouchHandler extends FreeAtHomeBaseHandler {

    public FreeAtHomeTouchHandler(Thing thing) {
        super(thing);
    }

    private Logger logger = LoggerFactory.getLogger(FreeAtHomeTouchHandler.class);

    private FreeAtHomeTouchConfig mConfiguration = new FreeAtHomeTouchConfig();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();

        if (bridge == null) {
            logger.error("No bridge connected");
            return;
        }

        // Values
        if (command instanceof DecimalType) {

            // Switch on/off
            if (channelUID.getId().equals(FreeAtHomeBindingConstants.TOUCH_TARGET_TEMP_THING_CHANNEL)) {
                DecimalType udCommand = (DecimalType) command;
                String channel = mConfiguration.deviceId + "/" + mConfiguration.channelId + "/"
                        + mConfiguration.dataPointIdTarget;
                String targetTemperature = "Set target temperature: " + channel + " value(" + udCommand.toString()
                        + ")";
                logger.debug("Set target temperature {}", targetTemperature);
                bridge.setDataPoint(channel, udCommand.toString());
            }
        }
        /*
         * OnOff
         */
        if (command instanceof OnOffType) {

            // Switch on/off
            if (channelUID.getId().equals(FreeAtHomeBindingConstants.TOUCH_SWITCH_THING_CHANNEL)) {
                OnOffType udCommand = (OnOffType) command;
                String channel = mConfiguration.deviceId + "/" + mConfiguration.channelId + "/"
                        + mConfiguration.dataPointIdSwitch;

                if (udCommand.equals(OnOffType.ON)) {

                    logger.debug("Thermostat Switch ON {}", channel);
                    bridge.setDataPoint(channel, "1");

                }
                if (udCommand.equals(OnOffType.OFF)) {

                    logger.debug("Thermostat Switch OFF {}", channel);
                    bridge.setDataPoint(channel, "0");

                }
            }
            // Switch eco on/off
            if (channelUID.getId().equals(FreeAtHomeBindingConstants.TOUCH_ECO_THING_CHANNEL)) {
                OnOffType udCommand = (OnOffType) command;
                String channel = mConfiguration.deviceId + "/" + mConfiguration.channelId + "/"
                        + mConfiguration.dataPointIdEco;

                if (udCommand.equals(OnOffType.ON)) {

                    logger.debug("Thermostat ECO switch ON {}", channel);
                    bridge.setDataPoint(channel, "1");

                }
                if (udCommand.equals(OnOffType.OFF)) {

                    logger.debug("Thermostat ECO switch OFF {}", channel);
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
        mConfiguration = getConfigAs(FreeAtHomeTouchConfig.class);

        // Fetch bridge on initialization to get proper state
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();
        if (bridge != null) {
            // Target temperature update
            class TargetTempDecimalTypeConverter implements StateConverter {
                @Override
                public State convert(String value) {
                    if (value.equals("35")) // reported if thermostat is switched off
                    {
                        return new DecimalType(Double.NaN);
                    } else {
                        return new DecimalType(value);
                    }
                }
            }

            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this,
                    FreeAtHomeBindingConstants.TOUCH_TARGET_TEMP_THING_CHANNEL, new TargetTempDecimalTypeConverter(),
                    mConfiguration.deviceId, mConfiguration.channelId, mConfiguration.dataPointIdTargetUpdate));

            // Room temperature update
            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this,
                    FreeAtHomeBindingConstants.TOUCH_ROOM_TEMP_THING_CHANNEL, new DefaultDecimalTypeConverter(),
                    mConfiguration.deviceId, mConfiguration.channelId, mConfiguration.dataPointIdRoomUpdate));

            // Switch on/off update
            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this, FreeAtHomeBindingConstants.TOUCH_SWITCH_THING_CHANNEL,
                    new DefaultOnOffTypeConverter(), mConfiguration.deviceId, mConfiguration.channelId,
                    mConfiguration.dataPointIdSwitchUpdate));

            // Heat state on/off update
            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this,
                    FreeAtHomeBindingConstants.TOUCH_HEAT_ACTIVE_THING_CHANNEL, new DefaultOnOffTypeConverter(),
                    mConfiguration.deviceId, mConfiguration.channelId, mConfiguration.dataPointIdHeatStateUpdate));

            // Heat actuator percentage state
            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this,
                    FreeAtHomeBindingConstants.TOUCH_HEATACTUATOR_DIMMER_THING_CHANNEL,
                    new DefaultPercentTypeConverter(), mConfiguration.deviceId, mConfiguration.channelId,
                    mConfiguration.dataPointIdHeatActuatorStateUpdate));

            // Switch eco on/off
            // Nested class for eco switch
            class EcoModeOnOffTypeConverter implements StateConverter {
                @Override
                public State convert(String value) {
                    if (value.equals("68")) {
                        return OnOffType.ON;
                    } else { // expected "65"
                        return OnOffType.OFF;
                    }
                }
            }
            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this, FreeAtHomeBindingConstants.TOUCH_ECO_THING_CHANNEL,
                    new EcoModeOnOffTypeConverter(), mConfiguration.deviceId, mConfiguration.channelId,
                    mConfiguration.dataPointIdEcoUpdate));

        } // dummy call to avoid optimization
    }
}
