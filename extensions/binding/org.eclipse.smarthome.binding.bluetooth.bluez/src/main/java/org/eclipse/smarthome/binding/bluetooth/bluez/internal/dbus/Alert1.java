/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth.bluez.internal.dbus;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.UInt16;

@DBusInterfaceName("org.bluez.Alert1")
public interface Alert1 extends DBusInterface {

    public void RegisterAlert(String category, DBusInterface agent);

    public void NewAlert(String category, UInt16 count, String description);

    public void UnreadAlert(String category, UInt16 count);
}
