package com.pmann.treemap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * This class encapsulates properties of trees, e.g. the color coding on the map. Any methods
 * that reference specific tree names should be in this class, to facilitate maintenance.
 * <p>
 * Note: For future proofing and localizing, this information should really live in the DB.
 * </p>
 */
class Tree {

    @SuppressWarnings("SpellCheckingInspection")
    final private static String[] mStandardTrees =
            {"apple", "cherry", "crabapple", "fig", "filbert", "grape", "loquat",
                    "peach", "pear", "persimmon", "plum", "quince"};
    final private static int mHueMultiplier = 360 / mStandardTrees.length;

    /**
     * Return a color value or hue (float between 0 and 360) corresponding to a given tree.
     * <p>
     * Note that colors will change, if the list of known tree types is expanded. We are just
     * ensuring that colors are as distinct as possible, as measured by the hue value.
     * </p>
     * @param pType string identifying a tree type
     * @return the corresponding hue or yellow, if the type is unknown
     */
    public static float hueByType(String pType) {
        for  (int i=0; i < mStandardTrees.length; i++) {
            if (pType.equalsIgnoreCase(mStandardTrees[i])) {
                return (float) i * mHueMultiplier;
            }
        }
        return BitmapDescriptorFactory.HUE_YELLOW;
    }
}


