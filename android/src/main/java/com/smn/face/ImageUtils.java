package com.smn.face;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import com.facebook.imageutils.BitmapUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by wwm on 2017-07-18.
 */

public class ImageUtils {
  public static int calculateInSampleSize(
    BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) > reqHeight
        && (halfWidth / inSampleSize) > reqWidth) {
        inSampleSize *= 2;
      }
    }
    return inSampleSize;
  }

  //Get Path Image file
  public final static String getRealPathFromURI(Context context, Uri contentUri) {
    Cursor cursor = null;
    try {
      String[] proj = {MediaStore.Images.Media.DATA};
      cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }


  //Rotate Bitmap
  public final static Bitmap rotate(Bitmap b, float degrees) {
    if (degrees != 0 && b != null) {
      Matrix m = new Matrix();
      m.setRotate(degrees, (float) b.getWidth() / 2,
        (float) b.getHeight() / 2);

      Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
        b.getHeight(), m, true);
      if (b != b2) {
        b.recycle();
        b = b2;
      }

    }
    return b;
  }


  public static Bitmap getBitmap(String filePath, int width, int height) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(filePath, options);
    options.inSampleSize = ImageUtils.calculateInSampleSize(options, width, height);
    options.inJustDecodeBounds = false;
    options.inPreferredConfig = Bitmap.Config.RGB_565;

    Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

    if (bitmap != null) {
      try {
        ExifInterface ei = new ExifInterface(filePath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
          case ExifInterface.ORIENTATION_ROTATE_90:
            bitmap = ImageUtils.rotate(bitmap, 90);
            break;
          case ExifInterface.ORIENTATION_ROTATE_180:
            bitmap = ImageUtils.rotate(bitmap, 180);
            break;
          case ExifInterface.ORIENTATION_ROTATE_270:
            bitmap = ImageUtils.rotate(bitmap, 270);
            break;
          // etc.
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return bitmap;
  }


  public static Bitmap cropBitmap(Bitmap bitmap, Rect rect) {
    int w = rect.right - rect.left;
    int h = rect.bottom - rect.top;
    Bitmap ret = Bitmap.createBitmap(w, h, bitmap.getConfig());
    Canvas canvas = new Canvas(ret);
    canvas.drawBitmap(bitmap, -rect.left, -rect.top, null);
    bitmap.recycle();
    return ret;
  }

  public static String getBase64FromBitmap(Bitmap bitmap) {
    String base64Str = null;
    ByteArrayOutputStream baos = null;
    try {
      if (bitmap != null) {
        baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] bitmapBytes = baos.toByteArray();
        base64Str = Base64.encodeToString(bitmapBytes, 2);
      }
    } finally {
      try {
        if (baos != null) {
          baos.flush();
          baos.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return base64Str;
  }

  public static Bitmap cropFace(FaceResult face, Bitmap bitmap, int rotate) {
    Bitmap bmp;
    float eyesDis = face.eyesDistance();
    PointF mid = new PointF();
    face.getMidPoint(mid);

    Rect rect = new Rect(
      (int) (mid.x - eyesDis * 1.90f),
      (int) (mid.y - eyesDis * 3.05f),
      (int) (mid.x + eyesDis * 1.90f),
      (int) (mid.y + eyesDis * 3.05f));

    Bitmap.Config config = Bitmap.Config.RGB_565;
    if (bitmap.getConfig() != null) config = bitmap.getConfig();
    bmp = bitmap.copy(config, true);

    switch (rotate) {
      case 90:
        bmp = ImageUtils.rotate(bmp, 90);
        break;
      case 180:
        bmp = ImageUtils.rotate(bmp, 180);
        break;
      case 270:
        bmp = ImageUtils.rotate(bmp, 270);
        break;
    }
    if(rect.left<0){
      rect.right-=rect.left;
      rect.left=0;
    }
    if(rect.top<0){
      rect.bottom-=rect.top;
      rect.top=0;
    }
    if(rect.right>bmp.getWidth()){
      rect.left-=rect.right-bmp.getWidth();
      rect.right=bmp.getWidth();
    }
    if(rect.bottom>bmp.getHeight()){
      rect.top-=rect.bottom-bmp.getHeight();
      rect.bottom=bmp.getHeight();
    }
    if(rect.left<0){
      rect.left=0;
    }
    if(rect.top<0){
      rect.top=0;
    }

  //  bmp = cropBitmap(bmp, rect);
    return Bitmap.createBitmap(bmp,rect.left,rect.top,rect.width(),rect.height());
  }


}
