/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link IMQTTActions} defines the interface for all thing actions supported by the binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface IMQTTActions {

    public void publishMQTT(@Nullable String topic, @Nullable String value, @Nullable Boolean retain);
}
