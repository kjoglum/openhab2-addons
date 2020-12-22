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
package org.openhab.binding.freeathome.internal.model;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * Convert values of update events to openhab states
 *
 * Default conversion for on/off
 *
 * @author Stian Kjoglum - Initial contribution
 *
 */
public class DefaultOnOffTypeConverter implements StateConverter {

    @Override
    public State convert(String value) {

        if (value.equals("1")) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }
}
