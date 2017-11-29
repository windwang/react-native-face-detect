package com.smn.face;

import android.util.Log;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

/**
 * Created by wwm on 2017-07-15.
 */

public class FaceTrackerViewManager extends SimpleViewManager<FaceTrackerView> {


  public static  FaceTrackerView view=null;

  @Override
  public String getName() {
    return "RCTFaceTrackerView";
  }

  @Override
  protected FaceTrackerView createViewInstance(ThemedReactContext reactContext) {
    view= new FaceTrackerView(reactContext, null);
    return view;
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
  public void setImageHeight(FaceTrackerView view, int height) {
      view.setImageHeight(height);
  }

  /**
   * 最少连续监测到用户的次数，打到次数才算是有效用户
   * @param view
   * @param minDetectedTimes
   */
  @ReactProp(name = "minDetectedTimes", defaultInt = 5)
  public void setMinDetectedTimes(FaceTrackerView view, int minDetectedTimes) {
      view.setMinDetectedTimes(minDetectedTimes);
  }

  /**
   * 最小置信度，大于该值的才被识别为人脸
   * @param view
   * @param confidence
   */
  @ReactProp(name="confidence",defaultFloat = 0.3f)
  public void setConfidence(FaceTrackerView view, float confidence) {
      view.setMinConfidence(confidence);

  }

  /**
   *  超时时间，超过此时间后用户再出现默认为新用户
   * @param view
   * @param minKeepTime
   */
  @ReactProp(name = "minKeepTime", defaultInt = 10)
  public void setMinKeepTime(FaceTrackerView view, int minKeepTime) {
         view.setMinKeepTime(minKeepTime);

  }


  /**
   * 关闭view
   * @param view
   * @param close
   */
  @ReactProp(name = "close", defaultBoolean = false)
  public  void  setClose(FaceTrackerView view,boolean close){
    if(close){
      Log.d("FACE","setClose");
      view.releaseCamera();
    }
  }



}
