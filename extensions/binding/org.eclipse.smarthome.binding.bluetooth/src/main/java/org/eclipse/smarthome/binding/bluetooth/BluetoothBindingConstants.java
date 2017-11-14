/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth;

import java.util.UUID;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BluetoothBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
public class BluetoothBindingConstants {

    public static final String BINDING_ID = "ble";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    // List of all Channel ids
    public static final String BLE_CHANNEL_RSSI = "rssi";

    public static final String XMLPROPERTY_BLE_FILTER = "bleFilter";

    public static final String PROPERTY_TXPOWER = "ble_txpower";
    public static final String PROPERTY_MAXCONNECTIONS = "ble_maxconnections";

    public static final String CONFIGURATION_ADDRESS = "ble_address";

    public static final long bleUuid = 0x800000805f9b34fbL;

    // Bluetooth profile UUID definitions
    public final static UUID PROFILE_GATT = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_A2DP_SOURCE = UUID.fromString("0000110a-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_A2DP_SINK = UUID.fromString("0000110b-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_A2DP = UUID.fromString("0000110d-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_AVRCP_REMOTE = UUID.fromString("0000110c-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_CORDLESS_TELEPHONE = UUID.fromString("00001109-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_DID_PNPINFO = UUID.fromString("00001200-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_HEADSET = UUID.fromString("00001108-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_HFP = UUID.fromString("0000111e-0000-1000-8000-00805f9b34fb");
    public final static UUID PROFILE_HFP_AUDIOGATEWAY = UUID.fromString("0000111f-0000-1000-8000-00805f9b34fb");

}
