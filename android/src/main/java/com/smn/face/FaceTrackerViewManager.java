package com.smn.face;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

/**
 * Created by wwm on 2017-07-15.
 */

public class FaceTrackerViewManager extends SimpleViewManager<FaceTrackerView> {
  @Override
  public String getName() {
    return "RCTFaceTrackerView";
  }

  @Override
  protected FaceTrackerView createViewInstance(ThemedReactContext reactContext) {
    return new FaceTrackerView(reactContext, null);
  }


  /**
   * 抓取图片的宽度
   * @param view
   * @param width
   */
  @ReactProp(name = "imageWidth", defaultInt = 320)
  public void setImageWidth(FaceTrackerView view, Integer width) {
    if (width != null) {
      view.setImageWidth(width);
    }
  }


  /**
   * 抓取图片的高度
   * @param view
   * @param height
   */
  @ReactProp(name = "imageHeight", defaultInt = 240)
  public void setImageHeight(FaceTrackerView view, Integer height) {
    if (height != null) {

      view.setImageHeight(height);
    }
  }

  /**
   * 最少连续监测到用户的次数，打到次数才算是有效用户
   * @param view
   * @param minDetectedTimes
   */
  @ReactProp(name = "minDetectedTimes", defaultInt = 5)
  public void setMinDetectedTimes(FaceTrackerView view, Integer minDetectedTimes) {
    if (minDetectedTimes != null) {
      view.setMinDetectedTimes(minDetectedTimes);
    }
  }

  /**
   * 最小置信度，大于该值的才被识别为人脸
   * @param view
   * @param confidence
   */
  @ReactProp(name="confidence",defaultFloat = 0.3f)
  public void setConfidence(FaceTrackerView view, Float confidence) {
    if (confidence != null) {
      view.setMinConfidence(confidence);
    }
  }

  /**
   *  超时时间，超过此时间后用户再出现默认为新用户
   * @param view
   * @param minKeepTime
   */
  @ReactProp(name = "minKeepTime", defaultInt = 1)
  public void setMinKeepTime(FaceTrackerView view, Integer minKeepTime) {
    if (minKeepTime != null) {
      view.setMinKeepTime(minKeepTime);
    }
  }


}
