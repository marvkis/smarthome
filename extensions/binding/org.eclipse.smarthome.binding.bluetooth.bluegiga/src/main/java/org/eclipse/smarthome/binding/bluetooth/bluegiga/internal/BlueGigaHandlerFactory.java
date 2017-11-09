/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth.bluegiga.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.binding.bluetooth.BluetoothAdapter;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.BlueGigaBindingConstants;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.handler.BlueGigaBridgeHandler;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link BlueGigaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - added support for adapter service registration
 */
public class BlueGigaHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(BlueGigaBindingConstants.THING_TYPE_BLUEGIGA);

    private final Map<ThingUID, ServiceRegistration<?>> serviceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BlueGigaBindingConstants.THING_TYPE_BLUEGIGA)) {
            BlueGigaBridgeHandler handler = new BlueGigaBridgeHandler((Bridge) thing);
            registerBluetoothAdapter(handler);
        }

        return null;
    }

    private synchronized void registerBluetoothAdapter(BluetoothAdapter adapter) {
        this.serviceRegs.put(adapter.getUID(), bundleContext.registerService(BluetoothAdapter.class.getName(), adapter,
                new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof BluetoothAdapter) {
            UID uid = ((BluetoothAdapter) thingHandler).getUID();
            ServiceRegistration<?> serviceReg = this.serviceRegs.get(uid);
            if (serviceReg != null) {
                serviceReg.unregister();
                serviceRegs.remove(uid);
            }
        }
    }
}
