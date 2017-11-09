/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BluetoothBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - refactoring and extension
 */
public class BluetoothBindingConstants {

    public static final String BINDING_ID = "bluetooth";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "generic");

    // List of all Channel Type IDs
    public static final String CHANNEL_TYPE_RSSI = "rssi";

    public static final String PROPERTY_TXPOWER = "txpower";
    public static final String PROPERTY_MAXCONNECTIONS = "maxconnections";

    public static final String CONFIGURATION_ADDRESS = "address";

    public static final long BLUETOOTH_BASE_UUID = 0x800000805f9b34fbL;

}
