package com.pmann.treemap;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

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
        onView(withId(R.id.btn_add)).perform(click());
    }
}