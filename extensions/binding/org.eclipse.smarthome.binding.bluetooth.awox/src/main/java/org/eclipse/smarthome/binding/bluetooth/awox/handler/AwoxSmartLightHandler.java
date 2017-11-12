/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth.awox.handler;

import java.util.UUID;

import org.eclipse.smarthome.binding.bluetooth.BluetoothCharacteristic;
import org.eclipse.smarthome.binding.bluetooth.BluetoothCompletionStatus;
import org.eclipse.smarthome.binding.bluetooth.BluetoothDeviceListener;
import org.eclipse.smarthome.binding.bluetooth.GenericBluetoothHandler;
import org.eclipse.smarthome.binding.bluetooth.awox.AwoxBindingConstants;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AwoxSmartLightHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class AwoxSmartLightHandler extends GenericBluetoothHandler implements BluetoothDeviceListener {

    private final Logger logger = LoggerFactory.getLogger(AwoxSmartLightHandler.class);

    private final static UUID AWOX_LIGHT_SET_STATE_SERVICE = UUID.fromString("3316-0fb9-5b27-4e70-b0f8-ff41-1e3a-e078");
    private final static UUID AWOX_LIGHT_SET_STATE_CHARAC = UUID.fromString("2178-87f8-0af2-4002-9c05-24c9-ecf7-1600");
    private final static UUID AWOX_LIGHT_STATE_READ_CHARAC = UUID.fromString("2178-87f8-0af2-4002-9c05-24c9-ecf7-1600");
    private final static byte[] AWOX_LIGHT_ON = new byte[] { 0x01 };
    private final static byte[] AWOX_LIGHT_OFF = new byte[] { 0x00 };

    // The characteristics we regularly use
    private BluetoothCharacteristic characteristicControl = null;
    private BluetoothCharacteristic characteristicRequest = null;

    public AwoxSmartLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String value = null;

        if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            double r = hsb.getRed().doubleValue() * 2.55;
            double g = hsb.getGreen().doubleValue() * 2.55;
            double b = hsb.getBlue().doubleValue() * 2.55;
            double a = hsb.getSaturation().doubleValue();
            value = String.format("%.0f,%.0f,%.0f,%.0f", r, g, b, a);
        }

        else if (command instanceof PercentType) {
            value = ",,," + ((PercentType) command).intValue() + "";
        }

        else if (command instanceof OnOffType) {
            value = ",,," + ((OnOffType) command == OnOffType.ON ? 100 : 0) + "";
        }

        if (value == null) {
            logger.debug("Unable to convert value!");
            return;
        }

        if (characteristicControl == null) {
            logger.debug("Unable to find control characteristic!");
            return;
        }

        // Terminate the value string with commas - up to 18 characters long
        for (int cnt = value.length(); cnt < 18; cnt++) {
            value += ",";
        }
        logger.debug("Conversion: {} to \"{}\"", command, value);

        characteristicControl.setValue(value);
        device.writeCharacteristic(characteristicControl);
    }

    @Override
    public void onServicesDiscovered() {
        // Everything is initialised now - get the characteristics we want to use
        characteristicControl = device.getCharacteristic(AWOX_LIGHT_SET_STATE_CHARAC);
        if (characteristicControl == null) {
            logger.debug("Control characteristic not known after service discovery!");
        }
        characteristicRequest = device.getCharacteristic(AWOX_LIGHT_STATE_READ_CHARAC);
        if (characteristicRequest == null) {
            logger.debug("Status characteristic not known after service discovery!");
        }

        // Read the current value so we can update the UI
        readStatus();
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        // If this was a write to the control, then read back the state
        if (characteristic.getUuid().equals(AWOX_LIGHT_SET_STATE_CHARAC)) {
            readStatus();
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        if (characteristic.getUuid().equals(AWOX_LIGHT_SET_STATE_CHARAC)) {
            String value = characteristic.getStringValue(0);
            logger.debug("Status update is \"{}\"", value);

            String[] elements = value.split(",");

            int red, green, blue;
            try {
                red = Integer.parseInt(elements[0]);
            } catch (NumberFormatException e) {
                red = 0;
            }
            try {
                green = Integer.parseInt(elements[1]);
            } catch (NumberFormatException e) {
                green = 0;
            }
            try {
                blue = Integer.parseInt(elements[2]);
            } catch (NumberFormatException e) {
                blue = 0;
            }

            HSBType hsbState = HSBType.fromRGB(red, green, blue);

            updateState(new ChannelUID(getThing().getUID(), AwoxBindingConstants.CHANNEL_COLOR), hsbState);
        }
    }

    private void readStatus() {
        if (characteristicRequest == null) {
            logger.debug("Status characteristic not known");
            return;
        }

        characteristicRequest.setValue("S");
        device.writeCharacteristic(characteristicRequest);
    }
}
