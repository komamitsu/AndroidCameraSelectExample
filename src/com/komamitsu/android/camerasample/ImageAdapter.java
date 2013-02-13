package com.komamitsu.android.camerasample;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

class ImageAdapter extends PagerAdapter {
  private final Context context;
  private final List<File> imageFiles;

  public ImageAdapter(Context context, List<File> imageFiles) {
    super();
    this.context = context;
    this.imageFiles = imageFiles;
  }

  @Override
  public int getCount() {
    return imageFiles.size();
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    File file = imageFiles.get(position);
    Bitmap bitmap = getBitmap(file);
    
    ImageView imageView = new ImageView(context);
    imageView.setImageBitmap(bitmap);

    container.addView(imageView);
    return imageView;
  }
  
  private Bitmap getBitmap(File file) {
    String path = file.getAbsolutePath();
    Bitmap origBitmap = null;
    int rot = 0;
    try {
      origBitmap = BitmapFactory.decodeFile(path);
      ExifInterface exifInterface = new ExifInterface(path);
      rot = Integer.valueOf(exifInterface.getAttribute("Orientation"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    Float degree = null;
    switch (rot) {
    case ExifInterface.ORIENTATION_ROTATE_90:
      degree = 90f;
      break;
    case ExifInterface.ORIENTATION_ROTATE_180:
      degree = 180f;
      break;
    case ExifInterface.ORIENTATION_ROTATE_270:
      degree = 270f;
      break;
    }

    Bitmap bitmap = origBitmap;
    if (degree != null) {
      Matrix matrix = new Matrix();
      matrix.postRotate(degree);
      bitmap = Bitmap.createBitmap(origBitmap, 0, 0, origBitmap.getWidth(), origBitmap.getHeight(), matrix, true);
    }
    
    return bitmap;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  @Override
  public boolean isViewFromObject(View view, Object obj) {
    return view == (ImageView) obj;
  }
}