/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth.bluez.internal.dbus;

import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.Variant;

@DBusInterfaceName("org.bluez.ProfileManager1")
public interface ProfileManager1 extends DBusInterface {

    public void RegisterProfile(DBusInterface profile, String UUID, Map<String, Variant> options);

    public void UnregisterProfile(DBusInterface profile);
}
