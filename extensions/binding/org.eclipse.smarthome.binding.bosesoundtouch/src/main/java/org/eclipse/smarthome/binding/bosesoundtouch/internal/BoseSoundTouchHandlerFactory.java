/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bosesoundtouch.internal;

import static org.eclipse.smarthome.binding.bosesoundtouch.BoseSoundTouchBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link BoseSoundTouchHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Niessner - Initial contribution
 */
public class BoseSoundTouchHandlerFactory extends BaseThingHandlerFactory {

    private Map<String, BoseSoundTouchHandler> mapOfBoseSoundTouchHandler = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        BoseSoundTouchHandler handler = new BoseSoundTouchHandler(thing, this);
        registerSoundTouchDevice(handler);
        return handler;
    }

    /**
     * Removes a registered handler from the factory
     *
     * Note that a created Handler automatically gets registered! But it is necessary to remove it, if it is not needed
     * anymore
     */
    public void removeSoundTouchDevice(BoseSoundTouchHandler handler) {
        mapOfBoseSoundTouchHandler.remove(handler.getMacAddress());
    }

    /**
     * Returns a collection of all registered BoseSoundTouchHandlers
     *
     * @return a collection of all registered BoseSoundTouchHandlers
     */
    public Collection<BoseSoundTouchHandler> getAllBoseSoundTouchHandler() {
        return mapOfBoseSoundTouchHandler.values();
    }

    /**
     * Returns a BoseSoundTouchHandler if a Handler with mac is registered. Otherwise null
     *
     * @param mac the MAC Address of the registered device
     *
     * @return a BoseSoundTouchHandler if a Handler with mac is registered. Otherwise null
     */
    public BoseSoundTouchHandler getBoseSoundTouchDevice(String mac) {
        return mapOfBoseSoundTouchHandler.get(mac);
    }

    /**
     * Registers a handler to the factory. So every Handler of a thing knows all other Handlers.
     * This is necessary for (un)grouping the devices
     *
     * Note that a created Handler automatically gets registered! But it is necessary to remove it, if it is not needed
     * anymore
     */
    private void registerSoundTouchDevice(BoseSoundTouchHandler handler) {
        mapOfBoseSoundTouchHandler.put(handler.getMacAddress(), handler);
    }
}
