package com.komamitsu.android.camerasample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;

public class CameraSampleActivity extends Activity {
  private static final String TAG = CameraSampleActivity.class.getSimpleName();
  private boolean shouldInvokeCamera;
  private ComponentName componentName;
  private ViewPager gallery;
  private List<File> imageFiles;
  private Uri currentImageUri;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    shouldInvokeCamera = false;
    showCameraSelector();
    initView();
  }

  private void initView() {
    gallery = (ViewPager) findViewById(R.id.gallery);
  }

  private void showCameraSelector() {
    List<ResolveInfo> resolveInfoList = getResolveInfoList();
    List<String> cameraNames = new ArrayList<String>();
    final List<ComponentName> componentNames = new ArrayList<ComponentName>();
    for (ResolveInfo resolveInfo : resolveInfoList) {
      componentNames.add(
          new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
      cameraNames.add(resolveInfo.activityInfo.applicationInfo.loadLabel(getPackageManager()).toString());
    }

    new AlertDialog.Builder(this).setItems(cameraNames.toArray(new String[0]), new OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        imageFiles = new ArrayList<File>();
        componentName = componentNames.get(which);
        invokeCamera(componentName);
      }
    }).show();
  }

  private List<ResolveInfo> getResolveInfoList() {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
    return getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onActivityResult(int, int,
   * android.content.Intent)
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case 0: {
      switch (resultCode) {
      case Activity.RESULT_OK:
        shouldInvokeCamera = true;
        File specifiedOutputFile = getFile(currentImageUri);
        File receivedOutputFile = data.getData() == null ? null : getFile(data.getData());
        File file = specifiedOutputFile;
        if (receivedOutputFile != null) {
          file = receivedOutputFile;
        }
        imageFiles.add(file);
        break;
      case Activity.RESULT_CANCELED:
        shouldInvokeCamera = false;
        gallery.setAdapter(new ImageAdapter(this, imageFiles));
        break;
      }
      break;
    }
    }
  }
  
  private File getFile(Uri uri) {
    Cursor c = getContentResolver().query(uri, null, null, null, null);
    if (c == null)
      return null;
    
    try {
      c.moveToFirst();
      int iData = c.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
      String path = c.getString(iData);
      File f = new File(path);
      return f.length() > 0 ? f : null;
    } finally {
      if (c != null)
        c.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();
    if (shouldInvokeCamera && componentName != null) {
      invokeCamera(componentName);
      shouldInvokeCamera = false;
    }
  }

  private void invokeCamera(ComponentName componentName) {
    String filename = System.currentTimeMillis() + ".jpg";

    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.TITLE, filename);
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    currentImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    Intent intent = new Intent();
    intent.setComponent(componentName);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
    startActivityForResult(intent, 0);
  }
}