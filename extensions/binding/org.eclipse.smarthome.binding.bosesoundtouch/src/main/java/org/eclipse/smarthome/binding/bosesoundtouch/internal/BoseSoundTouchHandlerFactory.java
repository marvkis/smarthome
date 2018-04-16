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
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BoseSoundTouchHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Niessner - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.bosesoundtouch")
public class BoseSoundTouchHandlerFactory extends BaseThingHandlerFactory {

    private Map<String, BoseSoundTouchHandler> mapOfBoseSoundTouchHandler = new HashMap<>();
    private StorageService storageService;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        Storage<ContentItem> storage = storageService.getStorage(thing.getUID().toString(),
                ContentItem.class.getClassLoader());
        BoseSoundTouchHandler handler = new BoseSoundTouchHandler(thing, this, new PresetContainer(storage));
        registerSoundTouchDevice(handler);
        return handler;
    }

    @Reference
    protected void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    protected void unsetStorageService(StorageService storageService) {
        this.storageService = null;
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
