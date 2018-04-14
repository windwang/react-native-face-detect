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
package com.smn.face.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import com.smn.face.Converter;

import java.io.IOException;

public class CameraSourcePreview extends ViewGroup {
  private static final String TAG = "CameraSourcePreview";
  protected final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();

  private Context mContext;
  private SurfaceView mSurfaceView;
  private boolean mStartRequested;
  private boolean mSurfaceAvailable;
  protected Camera mCameraSource;

  /**
   * 是否隐藏预览界面
   */
  private boolean hidePreview = false;
  private int mCameraId;
  protected   int mDisplayOrientation;
  protected int mDisplayRotation;

  public CameraSourcePreview(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    mStartRequested = false;
    mSurfaceAvailable = false;

    mSurfaceView = new SurfaceView(context);
    mSurfaceView.getHolder().addCallback(new SurfaceCallback());
    // mSurfaceView.setLayoutParams(new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT));
    addView(mSurfaceView);
  }

  public void start(Camera cameraSource, int mCameraId) throws IOException {
    this.mCameraId = mCameraId;
    this.mCameraSource = cameraSource;

    if (mCameraSource != null) {
      mStartRequested = true;
      startIfReady();
    }
  }

//  public void start(Camera cameraSource) throws IOException {
//    start(cameraSource);
//  }

  public void stop() {
    releaseCamera();
  }

  public void releaseCamera() {
    Log.d("FACE", "releaseCamera");
    if (mCameraSource != null) {
      mCameraSource.setPreviewCallback(null);
      mCameraSource.stopPreview();
      mCameraSource.release();
      mCameraSource = null;
    }
  }

  private void startIfReady() throws IOException {
    if (mCameraSource != null && mStartRequested && mSurfaceAvailable) {
      mCameraSource.setPreviewDisplay(mSurfaceView.getHolder());
      mCameraSource.startPreview();
      setCameraDisplayOrientation();

      mStartRequested = false;
    }
  }

  /**
   * 是否隐藏预览界面
   */
  public boolean isHidePreview() {
    return hidePreview;
  }

  public void setHidePreview(boolean hidePreview) {
    this.hidePreview = hidePreview;
  }

  private class SurfaceCallback implements SurfaceHolder.Callback {
    @Override
    public void surfaceCreated(SurfaceHolder surface) {
      mSurfaceAvailable = true;

      try {
        startIfReady();
      } catch (IOException e) {
        Log.e(TAG, "Could not start camera source.", e);
      }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surface) {
      mSurfaceAvailable = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    int width = 320;
    int height = 240;
    if (mCameraSource != null) {
      Camera.Size size = mCameraSource.getParameters().getPreviewSize();

      if (size != null) {
        width = size.width;
        height = size.height;
      }
    }

    // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
    if (isPortraitMode()) {

      int tmp = width;
      width = height;
      height = tmp;
    }

    final int layoutWidth = right - left;
    final int layoutHeight = bottom - top;

    // Computes height and width for potentially doing fit width.
    int childWidth = layoutWidth;
    int childHeight = (int) (((float) layoutWidth / (float) width) * height);

    // If height is too tall using fit width, does fit height instead.
    if (childHeight > layoutHeight) {
      childHeight = layoutHeight;
      childWidth = (int) (((float) layoutHeight / (float) height) * width);
    }

    for (int i = 0; i < getChildCount(); ++i) {
      getChildAt(i).layout(0, 0, childWidth, childHeight);
    }
    if (hidePreview)
      mSurfaceView.layout(0, 0, 1, 1);

    try {
      startIfReady();
    } catch (IOException e) {
      Log.e(TAG, "Could not start camera source.", e);
    }
  }

  private boolean isPortraitMode() {
    int orientation = mContext.getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
      return false;
    }
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      return true;
    }

    Log.d(TAG, "isPortraitMode returning false by default");
    return false;
  }

  public void setCameraDisplayOrientation() {
    mDisplayRotation =Converter.getRotation(getContext());
    mDisplayOrientation =Converter.getDisplayOrientation(mDisplayRotation,mCameraInfo);
    mCameraSource.setDisplayOrientation(mDisplayOrientation);
  }

}
