// This class encapsulates properties of trees, e.g. the color coding on the map

package com.pmann.treemap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Tree {

    private static String[] mStandardTrees =
            {"apple", "cherry", "crabapple", "fig", "filbert", "grape", "loquat",
                    "peach", "pear", "persimmon", "plum", "quince"};
    private static int mHueMultiplier = 360 / mStandardTrees.length;

    public static float hueByType(String pType) {
        for  (int i=0; i < mStandardTrees.length; i++) {
            if (pType.equalsIgnoreCase(mStandardTrees[i])) {
                return (float) i * mHueMultiplier;
            }
        }
        return BitmapDescriptorFactory.HUE_YELLOW;
    }
}


