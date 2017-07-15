package com.smn.face;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

/**
 * Created by wwm on 2017-07-15.
 */

public class FaceTrackerViewManager extends SimpleViewManager<FaceTrackerView> {
    @Override
    public String getName() {
        return null;
    }

    @Override
    protected FaceTrackerView createViewInstance(ThemedReactContext reactContext) {
        return new FaceTrackerView(reactContext);
    }


}
