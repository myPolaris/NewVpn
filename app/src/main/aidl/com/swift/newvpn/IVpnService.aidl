// IVpnService.aidl
package com.swift.newvpn;

import com.swift.newvpn.IVpnCallback;

// Declare any non-default types here with import statements

interface IVpnService {
    int getState();
    String getProfileName();
    void registerCallback(in IVpnCallback cb, int id);
    oneway void unregisterCallback(in IVpnCallback cb);
}