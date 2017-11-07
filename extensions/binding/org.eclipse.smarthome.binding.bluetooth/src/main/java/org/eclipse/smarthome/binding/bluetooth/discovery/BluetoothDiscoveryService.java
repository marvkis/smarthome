/**
 * Copyright (c) 1997, 2015 by Huawei Technologies Co., Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.binding.bluetooth.BluetoothBridge;
import org.eclipse.smarthome.binding.bluetooth.BluetoothDevice;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothDiscoveryService} handles searching for BLE devices.
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class BluetoothDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(BluetoothDiscoveryService.class);

    private final static int SEARCH_TIME = 15;

    private BluetoothBridge bridge;
    private ThingUID bridgeUID;

    private boolean scanning = false;

    public BluetoothDiscoveryService(ThingUID bridgeUID, BluetoothBridge bridge) {
        super(SEARCH_TIME);

        this.bridge = bridge;
        this.bridgeUID = bridgeUID;

        logger.debug("Creating BLE discovery service");
    }

    public void activate() {
        logger.debug("Activating BLE discovery service for {}", bridgeUID);
    }

    @Override
    public void deactivate() {
        logger.debug("Deactivating BLE discovery service for {}", bridgeUID);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return BluetoothThingTypeFilter.getSupportedThingTypes();
    }

    @Override
    public void startScan() {
        scanning = bridge.scanStart();
    }

    @Override
    public void stopScan() {
        bridge.scanStop();
        scanning = false;
    }

    /**
     * Add a new device to the inbox. This is called by the bridge when a new device has been discovered, and this
     * method adds the device to the inbox.
     *
     * @param device the newly discovered {@link BluetoothDevice}
     */
    public void deviceDiscovered(BluetoothDevice device) {
        ThingTypeUID thingTypeUID = BluetoothThingTypeFilter.findThingType(device);

        // Did we find a thing type for this device?
        if (thingTypeUID == null) {
            // Ignore it (?)
            return;
        }

        Map<String, Object> properties = new HashMap<>();

        String label = device.getName();
        if (label == null || label.length() == 0) {
            label = "BLE Device " + device.getAddress().toString();
        }

        properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
        if (device.getManufacturerName() != null) {
            properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getManufacturerName());
        }

        properties.put(BluetoothBindingConstants.PROPERTY_TXPOWER, Integer.toString(device.getTxPower()));

        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID,
                device.getAddress().toString().toLowerCase().replace(":", ""));

        // Create the discovery result and add to the inbox
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(bridgeUID).withLabel(label).build();
        thingDiscovered(discoveryResult);
    }

    // class DiscoveryScanCallback {

    // @Override
    // public void onScanResult(int callbackType, ScanResult result) {
    // BluetoothDevice device = result.getDevice();
    // BluetoothClass clazz = device.getBluetoothClass();

    // String manufacturerName = null;
    // BluetoothManufacturer manufacturer = BluetoothManufacturer.getManufacturer(device.getManufacturer());
    // if (manufacturer != null) {
    // label += " by " + manufacturer.getLabel();
    // manufacturerName = manufacturer.getLabel();
    // }

    // if (device.getName() != null) {
    // label += " (" + device.getName() + ")";
    // }
    // addDiscoveryResultThing(thingTypeUID, device.getAddress(), label, manufacturerName);
    // }
    // }
}
