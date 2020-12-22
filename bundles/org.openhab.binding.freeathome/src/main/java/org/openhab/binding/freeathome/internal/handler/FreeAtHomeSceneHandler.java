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

import static org.openhab.binding.freeathome.internal.FreeAtHomeBindingConstants.SCENE_THING_CHANNEL_ACTIVATE;

import java.util.Timer;
import java.util.TimerTask;

import org.openhab.binding.freeathome.internal.config.FreeAtHomeSceneConfig;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeSceneHandler} represents scene
 *
 * @author Stian Kjoglum - Initial contribution
 */
public class FreeAtHomeSceneHandler extends FreeAtHomeBaseHandler {

    private Logger logger = LoggerFactory.getLogger(FreeAtHomeSceneHandler.class);

    private FreeAtHomeSceneConfig mConfiguration = new FreeAtHomeSceneConfig();

    private Timer mTimer = new Timer(true);

    public FreeAtHomeSceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void tearDown() {
    }

    @Override
    public void setUp() {
        mConfiguration = getConfigAs(FreeAtHomeSceneConfig.class);

        logger.debug("Reset Timeout            {}.", mConfiguration.resetTimeout);
        // Fetch bridge on initialization to get proper state
        FreeAtHomeBridgeHandler bridge = getFreeAtHomeBridge();
        if (bridge != null) {
            bridge.dummyThingsEnabled();
        } // dummy call to avoid optimization
    }

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

            if (channelUID.getId().equals(SCENE_THING_CHANNEL_ACTIVATE)) {
                OnOffType udCommand = (OnOffType) command;

                if (udCommand.equals(OnOffType.ON)) {
                    String channel = mConfiguration.SceneId + "/" + mConfiguration.ChannelId + "/"
                            + mConfiguration.OutputId;

                    logger.debug("Called channel SCENE {}", channel);
                    bridge.setDataPoint(channel, mConfiguration.DataPoint);

                    if (mConfiguration.resetTimeout > 0) {
                        mTimer.schedule(new ResetTask(this), (long) (mConfiguration.resetTimeout * 1000));
                        logger.debug("Reset task scheduled");
                    }
                }
            }
        }
    }

    public void ResetSwitch() {
        logger.debug("Reset scene switch to OFF");
        updateState(SCENE_THING_CHANNEL_ACTIVATE, OnOffType.OFF);
    }

    /*
     * Class to reset Scene switch asynchronously
     */
    class ResetTask extends TimerTask {
        FreeAtHomeSceneHandler mHandler = null;

        ResetTask(FreeAtHomeSceneHandler handler) {
            mHandler = handler;
        }

        @Override
        public void run() {
            mHandler.ResetSwitch();
        }
    }
}
