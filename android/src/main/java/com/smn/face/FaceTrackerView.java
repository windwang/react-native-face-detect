/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smn.face;


import android.content.Context;


import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.support.annotation.Nullable;


import android.util.AttributeSet;
import android.util.Log;


import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.smn.face.camera.CameraSourcePreview;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerView extends CameraSourcePreview implements LifecycleEventListener, Camera.PreviewCallback {
  private static final String TAG = "FaceTracker";

  private Camera mCameraSource = null;

  private CameraSourcePreview mPreview = this;

  private static final int RC_HANDLE_GMS = 9001;
  // permission request codes need to be < 256
  private static final int RC_HANDLE_CAMERA_PERM = 2;
  ThemedReactContext context;
  private int mCameraId;

  Camera.Size previewSize;

  private byte[] grayBuff;
  private int bufflen;
  private int[] rgbs;
  private android.media.FaceDetector fdet;
  private boolean isThreadWorking = false;
  private FaceDetectThread detectThread = null;
  private Integer imageWidth = 320;
  private Integer imageHeight = 240;
  private int maxFace;

  private FaceResult faces[];
  private FaceResult faces_previous[];
  private int Id = 0;
  private HashMap<Integer, Integer> facesCount = new HashMap<>();
  private Integer minDetectedTimes = 3;
  private Integer minKeepTime = 10;

  public FaceTrackerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    onCreate(context, attrs);
  }


  //==============================================================================================
  // Activity Methods
  //==============================================================================================

  /**
   * Initializes the UI and initiates the creation of a face detector.
   *
   * @param context
   * @param attrs
   */

  public void onCreate(Context context, AttributeSet attrs) {
    this.context = (ThemedReactContext) context;
    this.context.addLifecycleEventListener(this);
    createCameraSource();
  }


  /**
   * Creates and starts the camera.  Note that this uses a higher resolution in comparison
   * to other detection examples to enable the barcode detector to detect small barcodes
   * at long distances.
   */
  private void createCameraSource() {
    mCameraSource = open();
    maxFace = mCameraSource.getParameters().getMaxNumDetectedFaces();
    if (maxFace == 0)
      maxFace = 10;
    //mCameraSource.setFaceDetectionListener(this);
    previewSize = mCameraSource.getParameters().getPreviewSize();

    bufflen = previewSize.width * previewSize.height;
    grayBuff = new byte[bufflen];
    rgbs = new int[bufflen];
    faces = new FaceResult[maxFace];
    faces_previous = new FaceResult[maxFace];
    for (int i = 0; i < maxFace; i++) {
      faces[i] = new FaceResult();
      faces_previous[i] = new FaceResult();
    }
    fdet = new android.media.FaceDetector(previewSize.width, previewSize.height, maxFace);
    mCameraSource.setPreviewCallback(this);
  }


  public Camera open() {
    int numberOfCameras = Camera.getNumberOfCameras();
    for (int i = 0; i < numberOfCameras; i++) {
      Camera.getCameraInfo(i, mCameraInfo);
      if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        this.mCameraId = i;
        return Camera.open(i);
      }
    }
    return null;
  }


  //==============================================================================================
  // Camera Source Preview
  //==============================================================================================

  /**
   * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    if (mCameraSource != null) {
      try {
        mPreview.start(mCameraSource, mCameraId);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        mCameraSource.release();
        mCameraSource = null;
      }
    }
  }


  @Override
  public void onHostResume() {
    startCameraSource();
  }

  @Override
  public void onHostPause() {
    mPreview.stop();
  }

  @Override
  public void onHostDestroy() {
    if (mCameraSource != null) {
      mCameraSource.release();
    }
  }


  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    if (!isThreadWorking) {
      isThreadWorking = true;
      waitForFdetThreadComplete();
      detectThread = new FaceDetectThread(this.context);
      detectThread.setData(data);
      detectThread.start();
    }
  }

  private void waitForFdetThreadComplete() {
    if (detectThread == null) {
      return;
    }

    if (detectThread.isAlive()) {
      try {
        detectThread.join();
        detectThread = null;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

  public void setImageWidth(Integer imageWidth) {
    this.imageWidth = imageWidth;
  }

  public Integer getImageWidth() {
    return imageWidth;
  }

  public void setImageHeight(Integer imageHeight) {
    this.imageHeight = imageHeight;
  }

  public Integer getImageHeight() {
    return imageHeight;
  }

  public void setMinDetectedTimes(Integer minDetectedTimes) {
    this.minDetectedTimes = minDetectedTimes;
  }

  public Integer getMinDetectedTimes() {
    return minDetectedTimes;
  }

  public void setMinKeepTime(Integer minKeepTime) {
    this.minKeepTime = minKeepTime;
  }

  public Integer getMinKeepTime() {
    return minKeepTime;
  }


  /**
   * Do face detect in thread
   */
  private class FaceDetectThread extends Thread {
    private byte[] data = null;
    private Context ctx;
    private Bitmap faceCroped;

    public FaceDetectThread(Context ctx) {
      this.ctx = ctx;

    }


    public void setData(byte[] data) {
      this.data = data;
    }

    public void run() {
      Bitmap bitmap = getBitmap();
      float aspect = (float) previewSize.height / (float) previewSize.width;
      int w = imageWidth;
      int h = (int) (imageWidth * aspect);

      float xScale = (float) previewSize.width / (float) w;
      float yScale = (float) previewSize.height / (float) h;

      Bitmap bmp = Bitmap.createScaledBitmap(bitmap, w, h, false);


      int rotate = mDisplayOrientation;
      if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mDisplayRotation % 180 == 0) {
        if (rotate + 180 > 360) {
          rotate = rotate - 180;
        } else
          rotate = rotate + 180;
      }
      switch (rotate) {
        case 90:
          bmp = ImageUtils.rotate(bmp, 90);
          xScale = (float) previewSize.height / bmp.getWidth();
          yScale = (float) previewSize.width / bmp.getHeight();
          break;
        case 180:
          bmp = ImageUtils.rotate(bmp, 180);
          break;
        case 270:
          bmp = ImageUtils.rotate(bmp, 270);
          xScale = (float) previewSize.height / (float) h;
          yScale = (float) previewSize.width / (float) imageWidth;
          break;
      }
      fdet = new android.media.FaceDetector(bmp.getWidth(), bmp.getHeight(), maxFace);

      android.media.FaceDetector.Face[] fullResults = new android.media.FaceDetector.Face[maxFace];
      int findCount = fdet.findFaces(bmp, fullResults);

      for (int i = 0; i < maxFace; i++) {
        processFace(bitmap, xScale, yScale, rotate, fullResults[i], i);
      }

      isThreadWorking = false;
    }

    private void processFace(Bitmap bitmap, float xScale, float yScale, int rotate, FaceDetector.Face detectedFace, int i) {
      if (detectedFace == null || detectedFace.confidence() < 0.3) {
        faces[i].clear();
        return;
      }
      PointF mid = new PointF();
      detectedFace.getMidPoint(mid);

      mid.x *= xScale;
      mid.y *= yScale;

      float eyesDis = detectedFace.eyesDistance() * xScale;
      float confidence = detectedFace.confidence();
      float pose = detectedFace.pose(FaceDetector.Face.EULER_Y);
      int idFace = Id;


      Rect rect = new Rect(
        (int) (mid.x - eyesDis * 1.20f),
        (int) (mid.y - eyesDis * 0.55f),
        (int) (mid.x + eyesDis * 1.20f),
        (int) (mid.y + eyesDis * 1.85f));

      /**
       * Only detect face size > 100x100
       */
      if (rect.height() * rect.width() <= 100 * 100) return;

      // Check this face and previous face have same ID?
      for (int j = 0; j < maxFace; j++) {
        float eyesDisPre = faces_previous[j].eyesDistance();
        PointF midPre = new PointF();
        faces_previous[j].getMidPoint(midPre);

        RectF rectCheck = new RectF(
          (midPre.x - eyesDisPre * 1.5f),
          (midPre.y - eyesDisPre * 1.15f),
          (midPre.x + eyesDisPre * 1.5f),
          (midPre.y + eyesDisPre * 1.85f));

        if (rectCheck.contains(mid.x, mid.y) && (System.currentTimeMillis() - faces_previous[j].getTime()) < minKeepTime * 1000) {
          idFace = faces_previous[j].getId();
          break;
        }
      }

      if (idFace == Id) Id++;

      faces[i].setFace(idFace, mid, eyesDis, confidence, pose, System.currentTimeMillis());
      faces_previous[i].set(faces[i].getId(), faces[i].getMidEye(), faces[i].eyesDistance(), faces[i].getConfidence(), faces[i].getPose(), faces[i].getTime());

      //
      // if focus in a face 5 frame -> take picture face display in RecyclerView
      // because of some first frame have low quality
      //
      if (facesCount.get(idFace) == null) {
        facesCount.put(idFace, 0);
        return;
      }

      int count = facesCount.get(idFace) + 1;
      if (count <= minDetectedTimes)
        facesCount.put(idFace, count);
      //
      // Crop Face to display in RecylerView
      //
      if (count == minDetectedTimes) {
        faceCroped = ImageUtils.cropFace(faces[i], bitmap, rotate);
        if (faceCroped != null) {
          faces[i].setImage(ImageUtils.getBase64FromBitmap(faceCroped));
          WritableMap event = Arguments.createMap();
          event.putMap("face", Converter.toMap(faces[i], mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT));
          context
            .getJSModule(RCTEventEmitter.class)
            .receiveEvent(
              FaceTrackerView.this.getId(),
              "topChange",
              event
            );
        }
      }

    }


    private void saveImage(Bitmap bitmap, String fileName) throws IOException {
      File f = new File(context.getExternalCacheDir(), fileName);
      f.createNewFile();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
      byte[] bitmapdata = bos.toByteArray();
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(bitmapdata);
      fos.flush();
      fos.close();
    }

    private Bitmap getBitmap() {
      ByteBuffer bbuffer = ByteBuffer.wrap(data);
      bbuffer.get(grayBuff, 0, bufflen);
      gray8toRGB32(grayBuff, previewSize.width, previewSize.height, rgbs);
      return Bitmap.createBitmap(rgbs, previewSize.width, previewSize.height, Bitmap.Config.RGB_565);
    }

    private void gray8toRGB32(byte[] gray8, int width, int height, int[] rgb_32s) {
      final int endPtr = width * height;
      int ptr = 0;
      while (true) {
        if (ptr == endPtr)
          break;

        final int Y = gray8[ptr] & 0xff;
        rgb_32s[ptr] = 0xff000000 + (Y << 16) + (Y << 8) + Y;
        ptr++;
      }
    }
  }

}
