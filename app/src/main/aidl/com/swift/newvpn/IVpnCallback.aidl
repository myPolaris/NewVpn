// IVpnCallback.aidl
package com.swift.newvpn;

import com.swift.newvpn.model.SpeedData;
import com.swift.newvpn.model.TrafficData;

// Declare any non-default types here with import statements

interface IVpnCallback {
    void stateChanged(int state, String profileName, String msg);
    void cbSpeedUpdate(in SpeedData stats);
    void cbTrafficUpdate(in TrafficData stats);
    void cbSelectorUpdate(String id);
}