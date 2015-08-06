// This class encapsulates properties of trees, e.g. the color coding on the map

package com.pmann.treemap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Tree {
    public static float hueByType(String pType) {
        switch (pType.toLowerCase()) {
            case "apple": return BitmapDescriptorFactory.HUE_MAGENTA;
            case "pear": return BitmapDescriptorFactory.HUE_GREEN;
            case "plum": return BitmapDescriptorFactory.HUE_VIOLET;
            case "cherry": return BitmapDescriptorFactory.HUE_RED;
            case "fig":  return BitmapDescriptorFactory.HUE_CYAN;
            default: return BitmapDescriptorFactory.HUE_YELLOW;
        }
    }
}
