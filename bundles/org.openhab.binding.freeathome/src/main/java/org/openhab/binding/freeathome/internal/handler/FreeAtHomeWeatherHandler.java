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
import org.openhab.binding.freeathome.internal.config.FreeAtHomeWeatherConfig;
import org.openhab.binding.freeathome.internal.model.DefaultDecimalTypeConverter;
import org.openhab.binding.freeathome.internal.model.DefaultOnOffTypeConverter;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeWeatherHandler} represents weather station
 *
 * @author Stian Kjoglum - Initial contribution
 */

public class FreeAtHomeWeatherHandler extends FreeAtHomeBaseHandler {

    public FreeAtHomeWeatherHandler(Thing thing) {
        super(thing);
    }

    private Logger logger = LoggerFactory.getLogger(FreeAtHomeWeatherHandler.class);

    private FreeAtHomeWeatherConfig mConfiguration = new FreeAtHomeWeatherConfig();

    /*
     * No commands need to be handled
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();

        if (bridge == null) {
            logger.error("No bridge connected");
            return;
        }
    }

    @Override
    public void tearDown() {
    }

    @Override
    public void setUp() {
        mConfiguration = getConfigAs(FreeAtHomeWeatherConfig.class);

        // Fetch bridge on initialization to get proper state
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();
        if (bridge != null) {
            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this, FreeAtHomeBindingConstants.WEATHER_TEMP_THING_CHANNEL,
                    new DefaultDecimalTypeConverter(), mConfiguration.deviceId, mConfiguration.channelIdTemp,
                    mConfiguration.dataPointIdTemp));

            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this, FreeAtHomeBindingConstants.WEATHER_WIND_THING_CHANNEL,
                    new DefaultDecimalTypeConverter(), mConfiguration.deviceId, mConfiguration.channelIdWind,
                    mConfiguration.dataPointIdWind));

            mUpdateChannels.add(
                    new FreeAtHomeUpdateChannel(this, FreeAtHomeBindingConstants.WEATHER_ILLUMINATION_THING_CHANNEL,
                            new DefaultDecimalTypeConverter(), mConfiguration.deviceId,
                            mConfiguration.channelIdIllumination, mConfiguration.dataPointIdIllumination));

            mUpdateChannels.add(new FreeAtHomeUpdateChannel(this, FreeAtHomeBindingConstants.WEATHER_RAIN_THING_CHANNEL,
                    new DefaultOnOffTypeConverter(), mConfiguration.deviceId, mConfiguration.channelIdRain,
                    mConfiguration.dataPointIdRain));

        } // dummy call to avoid optimization
    }
}
