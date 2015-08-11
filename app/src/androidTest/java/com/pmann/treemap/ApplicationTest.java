package com.pmann.treemap;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.widget.TextView;

public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    private MapsActivity mMapsActivity;
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }
}