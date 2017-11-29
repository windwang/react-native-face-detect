package com.smn.face;

import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

/**
 * Created by Jeff.Xu on 2017/11/29.
 */
public class FaceTrackerModule extends ReactContextBaseJavaModule {
  public FaceTrackerModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  /**
   * @return the name of this module. This will be the name used to {@code require()} this module
   * from javascript.
   */
  @Override
  public String getName() {
    return "FaceTracker";
  }


  @ReactMethod
  public void close() {
    Log.d("FACE", "ReactMethod-close");
    if (FaceTrackerViewManager.view != null) {
      FaceTrackerViewManager.view.releaseCamera();
    }
  }
}
