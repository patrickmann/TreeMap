// This class encapsulates properties of trees, e.g. the color coding on the map

package com.pmann.treemap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Tree {
    public static float hueByType(String pType) {
        switch (pType.toLowerCase()) {
            case "apple": return BitmapDescriptorFactory.HUE_RED;
            case "pear": return BitmapDescriptorFactory.HUE_GREEN;
            default: return BitmapDescriptorFactory.HUE_YELLOW;
        }
    }
}
