package com.smn.face;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.Surface;
import android.view.WindowManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;


/**
 * Created by wwm on 2017-07-17.
 */

public final class Converter {

  public static int getRotation(Context context) {
    int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
      .getRotation();
    switch (rotation) {
      case Surface.ROTATION_0:
        return 0;
      case Surface.ROTATION_90:
        return 90;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_270:
        return 270;
    }
    return 0;
  }

  public static int getDisplayOrientation(int degrees, Camera.CameraInfo info) {

    int result;
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      result = (info.orientation + degrees) % 360;
      result = (360 - result) % 360;  // compensate the mirror
    } else {  // back-facing
      result = (info.orientation - degrees + 360) % 360;
    }
    return result;
  }

  public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
                                   int viewWidth, int viewHeight) {
    // Need mirror for front camera.
    matrix.setScale(mirror ? -1 : 1, 1);
    // This is the value for android.hardware.Camera.setDisplayOrientation.
    matrix.postRotate(displayOrientation);
    // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
    // UI coordinates range from (0, 0) to (width, height).
    matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
    matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
  }


  public static WritableMap toMap(FaceResult face, boolean mirror) {
    WritableMap map = Arguments.createMap();


    map.putDouble("confidence", face.getConfidence());
//    map.putInt("height", face.rect.height());
//    map.putInt("width", face.rect.width());
//    map.putInt("x", face.rect.left);
//    map.putInt("y", face.get);
    map.putString("image", face.getImage());
    map.putInt("id", face.getId());

    return map;

  }

  public static WritableArray toMap(FaceResult[] faces, boolean mirror) {
    WritableArray array = Arguments.createArray();
    for (FaceResult face : faces) {
      array.pushMap(toMap(face, mirror));
    }
    return array;
  }

}
