/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth.awox.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.binding.bluetooth.awox.AwoxBindingConstants;
import org.eclipse.smarthome.binding.bluetooth.awox.handler.AwoxSmartLightHandler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * The {@link AwoxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.awox", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class AwoxHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(AwoxBindingConstants.THING_TYPE_SMLCOLOR);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(AwoxBindingConstants.THING_TYPE_SMLCOLOR)) {
            return new AwoxSmartLightHandler(thing);
        }

        return null;
    }
}
