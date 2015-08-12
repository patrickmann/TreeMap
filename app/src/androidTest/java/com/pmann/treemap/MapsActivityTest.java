package com.pmann.treemap;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import org.junit.Before;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

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

    public void test() throws Exception {

        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation()
                .addMonitor(AddDialogFragment.class.getName(), null, false);

        onView(withId(R.id.btn_add)).perform(click());

        Activity activity = activityMonitor.getLastActivity();

        onView(withId(R.id.txt_type)).perform(typeText("Apple"), closeSoftKeyboard());
    }
}