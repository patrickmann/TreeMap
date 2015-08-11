package com.pmann.treemap;

import android.test.ActivityInstrumentationTestCase2;
import android.support.test.InstrumentationRegistry;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class MapsActivityTest extends ActivityInstrumentationTestCase2<MapsActivity> {

    private MapsActivity mActivity;

    public MapsActivityTest() {
        super(MapsActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

    @Test
    public void test() throws Exception {

    }
}