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
package org.openhab.binding.freeathome.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeathome.internal.handler.FreeAtHomeBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements manually triggered discovery
 *
 * @author Stian Kjoglum - Initial contribution
 *
 */
public class FreeAtHomeBindingDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int SEARCH_TIME = 90;
    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeBindingDiscoveryService.class);
    private @Nullable FreeAtHomeBridgeHandler handler;

    public FreeAtHomeBindingDiscoveryService() {
        super(FreeAtHomeBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof FreeAtHomeBridgeHandler) {
            this.handler = (FreeAtHomeBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void startScan() {
        // TODO Auto-generated method stub
        FreeAtHomeBridgeHandler fh = this.handler;
        this.removeOlderResults(getTimestampOfLastScan());

        if ((fh != null) && (fh.isOnline())) {
            fh.getAll();

            String response = fh.getResponse();
            ThingUID bridgeUID = fh.getThing().getUID();

            FreeAtHomeBindingInventoryIterator iter = new FreeAtHomeBindingInventoryIterator(response);

            while (iter.iter_hasnext()) {
                FreeAtHomeBindingInventoryIterator.DeviceDescription device = iter.iter_get();

                String deviceTypeId = device.DeviceTypeId;
                logger.debug("Device identified: {}", deviceTypeId);

                switch (deviceTypeId) {
                    // Jalousieaktor 4-fach, REG
                    case "B001": {
                        for (int i = 0; i < 4; i++) // B001 provides 4 different channels
                        {
                            String channelId = "ch000" + i;
                            ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.RAFFSTORE_THING_TYPEUID, bridgeUID,
                                    device.Serial + "_" + channelId);
                            Map<String, Object> properties = new HashMap<>(1);
                            properties.put("DeviceId", device.Serial);
                            properties.put("ChannelId", channelId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                    .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                            + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                    .withBridge(bridgeUID).withProperties(properties).build();
                            thingDiscovered(discoveryResult);
                        }
                        break;
                    }
                    // Jalousieaktor 1-fach, REG
                    // 1013 provides 1 channel, but it runs on channel ch0003
                    case "9013":
                    case "9014":
                    case "1013": {
                        String channelId = "ch0003";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.RAFFSTORE_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("DeviceId", device.Serial);
                        properties.put("ChannelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Sensor/Schaltaktor 2/1-fach
                    // 9015 provides 1 channel, but it runs on channel ch0006
                    case "1002":
                    case "100E":
                    case "1015":
                    case "9015":
                    case "9003": {
                        String channelId = "ch0006";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.RAFFSTORE_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("DeviceId", device.Serial);
                        properties.put("ChannelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Hue Schaltaktor
                    case "10C4": {
                        String channelId = "ch0000";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Schaltaktor 4-fach, 16A, REG
                    case "B002": {
                        for (int i = 0; i < 4; i++) // B002 provides 4 different channels
                        {
                            String channelId = "ch000" + i;
                            ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                    device.Serial + "_" + channelId);
                            Map<String, Object> properties = new HashMap<>(1);
                            properties.put("deviceId", device.Serial);
                            properties.put("channelId", channelId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                    .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                            + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                    .withBridge(bridgeUID).withProperties(properties).build();
                            thingDiscovered(discoveryResult);
                        }
                        break;
                    }

                    // Schaltaktor 8 fach
                    case "B008": {
                        for (int i = 0; i < 8; i++) // B008 provides 8 binary channels
                        {
                            String channelId = "ch000" + i;
                            ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.BINARYSWITCH_THING_TYPEUID,
                                    bridgeUID, device.Serial + "_" + channelId);
                            Map<String, Object> properties = new HashMap<>(1);
                            properties.put("DeviceId", device.Serial);
                            properties.put("ChannelId", channelId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                    .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                            + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                    .withBridge(bridgeUID).withProperties(properties).build();
                            thingDiscovered(discoveryResult);
                        }

                        for (int i = 12; i < 12 + 8; i++) // B008 provides 8 actuator channels
                        {
                            String n = Integer.toHexString(i).toUpperCase();

                            String result = ("0000" + n).substring(n.length());
                            String channelId = "ch" + result;
                            ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                    device.Serial + "_" + channelId);
                            Map<String, Object> properties = new HashMap<>(1);
                            properties.put("deviceId", device.Serial);
                            properties.put("channelId", channelId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                    .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                            + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                    .withBridge(bridgeUID).withProperties(properties).build();
                            thingDiscovered(discoveryResult);
                        }
                        break;
                    }

                    // Schaltaktor 1/1
                    case "100C":
                    case "900C": {
                        String channelId = "ch0003";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Schaltaktor 2/2
                    case "1010":
                    case "9010":
                    case "9011": {
                        for (int i = 6; i < 8; i++) {
                            String channelId = "ch000" + i;
                            ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                    device.Serial + "_" + channelId);
                            Map<String, Object> properties = new HashMap<>(1);
                            properties.put("deviceId", device.Serial);
                            properties.put("channelId", channelId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                    .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                            + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                    .withBridge(bridgeUID).withProperties(properties).build();
                            thingDiscovered(discoveryResult);
                        }
                        break;
                    }

                    // Switch sensor 1/1
                    case "1000":
                    case "9000":
                    case "9001": {
                        String channelId = "ch0000";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Schaltactor 1/1
                    case "900B":
                    case "900D": {
                        String channelId = "ch0001";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Switch group
                    case "4000": {
                        String channelId = "ch0000";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);
                        properties.put("dataPointId", "odp0002");

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Motion Detector (with actuator)
                    case "900A": {
                        String channelId1 = "ch0000";
                        ThingUID uid1 = new ThingUID(FreeAtHomeBindingConstants.MOTIONSWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId1);
                        Map<String, Object> properties1 = new HashMap<>(1);
                        properties1.put("deviceId", device.Serial);
                        properties1.put("channelId", channelId1);

                        DiscoveryResult discoveryResult1 = DiscoveryResultBuilder.create(uid1)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId1)
                                .withBridge(bridgeUID).withProperties(properties1).build();
                        thingDiscovered(discoveryResult1);

                        String channelId2 = "ch0001";
                        ThingUID uid2 = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId2);
                        Map<String, Object> properties2 = new HashMap<>(1);
                        properties2.put("deviceId", device.Serial);
                        properties2.put("channelId", channelId2);

                        DiscoveryResult discoveryResult2 = DiscoveryResultBuilder.create(uid2)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId2)
                                .withBridge(bridgeUID).withProperties(properties2).build();
                        thingDiscovered(discoveryResult2);
                        break;
                    }

                    // Motion Detector (without actuator)
                    case "9008":
                    case "1008":
                    case "9009": {
                        String channelId = "ch0000";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.MOTIONSWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Virtual Switch
                    case "0001": {
                        String channelId = "ch0000";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.VIRTUALSWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Jalousiegruppe
                    case "4001": {
                        String channelId = "ch0000";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.RAFFSTORE_THING_TYPEUID, bridgeUID,
                                device.Serial);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("DeviceId", device.Serial);
                        properties.put("ChannelId", channelId);
                        properties.put("InputIdComplete", "odp0003");
                        properties.put("InputIdStepwise", "odp0004");

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                                .create(uid).withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                        + deviceTypeId + "_" + device.Serial)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // thermostat
                    case "1004":
                    case "9004":
                    case "9005":
                    case "2041": {
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.THERMOSTAT_THING_TYPEUID, bridgeUID,
                                device.Serial);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                                .create(uid).withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                        + deviceTypeId + "_" + device.Serial)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // touch
                    case "1020": {
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.TOUCH_THING_TYPEUID, bridgeUID,
                                device.Serial);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                                .create(uid).withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                        + deviceTypeId + "_" + device.Serial)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // 7" Panel with door opener functionality
                    case "1038": {
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.DOORPANEL_THING_TYPEUID, bridgeUID,
                                device.Serial);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                                .create(uid).withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                        + deviceTypeId + "_" + device.Serial)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // New scene
                    case "FFFF":
                    case "4800": {
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SCENE_THING_TYPEUID, bridgeUID,
                                device.Serial);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("SceneId", device.Serial);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                                .create(uid).withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                        + deviceTypeId + "_" + device.Serial)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Dimmaktor 4-fach
                    case "101C":
                    case "1021":
                    case "901C": {
                        for (int i = 0; i < 4; i++) {
                            String channelId = "ch000" + i;
                            ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.DIMMER_THING_TYPEUID, bridgeUID,
                                    device.Serial + "_" + channelId);
                            Map<String, Object> properties = new HashMap<>(1);
                            properties.put("deviceId", device.Serial);
                            properties.put("channelId", channelId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                    .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                            + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                    .withBridge(bridgeUID).withProperties(properties).build();
                            thingDiscovered(discoveryResult);
                        }
                        break;
                    }

                    // Dimmaktor 1/1-fach
                    case "9017": {
                        String channelId = "ch0003";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.DIMMER_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Dimmaktor 2/1-fach
                    case "9019": {
                        String channelId = "ch0006";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.DIMMER_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Hue dimmer
                    case "10C0": {
                        String channelId = "ch0000";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.DIMMER_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Window sensor
                    case "2042": {
                        String channelId = "ch0001";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.WINDOWSENSOR_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Binary sensor
                    case "B007":
                    case "B005": {
                        String channelId = "ch0000";
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.BINARYSWITCH_THING_TYPEUID, bridgeUID,
                                device.Serial + "_" + channelId);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);
                        properties.put("channelId", channelId);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_" + deviceTypeId
                                        + "_" + device.Serial + "_" + channelId)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // weather station
                    case "101D": {
                        ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.WEATHER_THING_TYPEUID, bridgeUID,
                                device.Serial);
                        Map<String, Object> properties = new HashMap<>(1);
                        properties.put("deviceId", device.Serial);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                                .create(uid).withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                        + deviceTypeId + "_" + device.Serial)
                                .withBridge(bridgeUID).withProperties(properties).build();
                        thingDiscovered(discoveryResult);
                        break;
                    }

                    // Wireless devices
                    case "A039": {
                        if (device.DeviceTypeName.contains("Schaltaktor 1/1-fach")) {
                            String channelId = "ch0006";
                            ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                    device.Serial + "_" + channelId);
                            Map<String, Object> properties = new HashMap<>(1);
                            properties.put("deviceId", device.Serial);
                            properties.put("channelId", channelId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                    .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                            + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                    .withBridge(bridgeUID).withProperties(properties).build();
                            thingDiscovered(discoveryResult);

                        } else if (device.DeviceTypeName.contains("Schaltaktor 2/2-fach")) {
                            for (int i = 6; i < 8; i++) {
                                String channelId = "ch000" + i;
                                ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.SWITCH_THING_TYPEUID, bridgeUID,
                                        device.Serial + "_" + channelId);
                                Map<String, Object> properties = new HashMap<>(1);
                                properties.put("deviceId", device.Serial);
                                properties.put("channelId", channelId);

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                        .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                                + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                        .withBridge(bridgeUID).withProperties(properties).build();
                                thingDiscovered(discoveryResult);

                            }
                        } else if (device.DeviceTypeName.contains("Dimmaktor 1/1-fach")) {
                            String channelId = "ch0008";
                            ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.DIMMER_THING_TYPEUID, bridgeUID,
                                    device.Serial + "_" + channelId);
                            Map<String, Object> properties = new HashMap<>(1);
                            properties.put("deviceId", device.Serial);
                            properties.put("channelId", channelId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                    .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                            + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                    .withBridge(bridgeUID).withProperties(properties).build();
                            thingDiscovered(discoveryResult);

                        } else if (device.DeviceTypeName.contains("Jalousieaktor")) {
                            String channelId = "ch0009";
                            ThingUID uid = new ThingUID(FreeAtHomeBindingConstants.RAFFSTORE_THING_TYPEUID, bridgeUID,
                                    device.Serial + "_" + channelId);
                            Map<String, Object> properties = new HashMap<>(1);
                            properties.put("DeviceId", device.Serial);
                            properties.put("ChannelId", channelId);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                                    .withLabel(device.DeviceDisplayName + "_" + device.DeviceTypeName + "_"
                                            + deviceTypeId + "_" + device.Serial + "_" + channelId)
                                    .withBridge(bridgeUID).withProperties(properties).build();
                            thingDiscovered(discoveryResult);
                        }
                        break;
                    }

                    default: {
                        if (fh.dummyThingsEnabled()) {

                            ThingUID uid1 = new ThingUID(FreeAtHomeBindingConstants.DUMMY_THING_TYPEUID, bridgeUID,
                                    device.Serial);

                            DiscoveryResult discoveryResult1 = DiscoveryResultBuilder
                                    .create(uid1).withLabel("Dummy_" + device.DeviceDisplayName + "_"
                                            + device.DeviceTypeName + "_" + deviceTypeId + "_" + device.Serial)
                                    .withBridge(bridgeUID).build();
                            thingDiscovered(discoveryResult1);
                            break;

                        }
                    }
                }

                iter.iter_next();
            }

        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
    }

    @Override
    public boolean isBackgroundDiscoveryEnabled() {
        return false;
    }
}
