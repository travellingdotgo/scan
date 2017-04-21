package com.scandemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

public class SufaceCameraManager implements SurfaceHolder.Callback {
	public final static String TAG  = "SufaceCameraManager";

	private Activity mActivity;
	private Camera.PreviewCallback mCallback;
	private SurfaceView mSurfaceView;
	private boolean mIsFrontalCamera = false;// 相机第一次启动是否是前置摄像头
	public Camera mCamera;
	public int cameraWidth;
	public int cameraHeight;
	private int cameraId = 0;// 前置摄像头
	public int mDetectionAngle = 90;

	public SufaceCameraManager(Activity mActivity, Camera.PreviewCallback mCallback, SurfaceView mSurfaceView, boolean IsFrontalCamera) {
		this.mActivity = mActivity;
		this.mCallback = mCallback;
		this.mSurfaceView = mSurfaceView;
		mSurfaceView.getHolder().addCallback(this);

		mIsFrontalCamera = IsFrontalCamera;
	}

	public Camera openCamera() {
		try {
			cameraId = mIsFrontalCamera ? FindFrontCamera() : FindBackCamera();
			mDetectionAngle = mIsFrontalCamera ? 360 - getCameraAngle() : getCameraAngle();
			Log.i(TAG, "cameraId= " + cameraId);
			if (cameraId != -1) {
				mCamera = Camera.open(cameraId);
				mSurfaceView.setLayoutParams(getParams());
			}
			getBestPreviewSize();

			return mCamera;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 切换前后摄像头
	 */
	public Camera changeCameraOnline() {
		closeCamera();
		mSurfaceView.setVisibility(View.INVISIBLE);
		mIsFrontalCamera = !mIsFrontalCamera;
		cameraId = mIsFrontalCamera ? FindFrontCamera() : FindBackCamera();
		mDetectionAngle = mIsFrontalCamera ? 360 - getCameraAngle() : getCameraAngle();
		if (cameraId != -1) {
			mCamera = Camera.open(cameraId);
			mSurfaceView.setLayoutParams(getParams());
		}
		mSurfaceView.setVisibility(View.VISIBLE);
		return mCamera;
	}

	public void closeCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * 聚焦
	 */
	public void autoFocus() {
		mCamera.autoFocus(null);
	}

	public int getCameraAngle() {
		int rotateAngle = 90;
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = mActivity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			rotateAngle = (info.orientation + degrees) % 360;
			rotateAngle = (360 - rotateAngle) % 360; // compensate the mirror
		} else { // back-facing
			rotateAngle = (info.orientation - degrees + 360) % 360;
		}

		Log.e(TAG, "rotateAngle:  " + rotateAngle);
		return 0;//rotateAngle;
	}

	private Size getBestPreviewSize() {
		Camera.Parameters camPara = mCamera.getParameters();
		List<Camera.Size> allSupportedSize = camPara.getSupportedPreviewSizes();
		ArrayList<Camera.Size> widthLargerSize = new ArrayList<Camera.Size>();
		for (Camera.Size tmpSize : allSupportedSize) {
			Log.i(TAG, "tmpSize:  " + tmpSize.width + "  " + tmpSize.height);
			if (tmpSize.width > tmpSize.height) {
				widthLargerSize.add(tmpSize);
			}
		}

		Collections.sort(widthLargerSize, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size lhs, Camera.Size rhs) {
				int off_one = Math.abs(lhs.width * lhs.height - Screen.mWidth
						* Screen.mHeight);
				int off_two = Math.abs(rhs.width * rhs.height - Screen.mWidth
						* Screen.mHeight);
				return off_one - off_two;
			}
		});

		return widthLargerSize.get(0);
	}

	/**
	 * 获取展示屏幕的宽高
	 */
	public RelativeLayout.LayoutParams getParams() {
		Camera.Parameters camPara = mCamera.getParameters();

		boolean bFullScreen = false;
		if( bFullScreen ){
			Camera.Size bestPreviewSize = getBestPreviewSize();
			cameraWidth = bestPreviewSize.width;
			cameraHeight = bestPreviewSize.height;

			camPara.setPreviewSize(cameraWidth, cameraHeight);
			mCamera.setParameters(camPara);

			// 因为旋转90度所以bestPreviewSize.height对应屏幕的宽
			float scaleW = (float) ((Screen.mWidth * 1.00) / (bestPreviewSize.height * 1.00));
			float scaleH = (float) ((Screen.mHeight * 1.00) / (bestPreviewSize.width * 1.00));
			float scale = scaleW > scaleH ? scaleH : scaleW;

			RelativeLayout.LayoutParams layoutPara = new RelativeLayout.LayoutParams(
					(int) (bestPreviewSize.height * scale),
					(int) (bestPreviewSize.width * scale));

			layoutPara.addRule(RelativeLayout.CENTER_HORIZONTAL);// 设置照相机水平居中
			return layoutPara;
		}
		else{
			cameraWidth = Status.previewWidth;
			cameraHeight = Status.previewHeight;
			camPara.setPreviewSize(cameraWidth, cameraHeight);
			mCamera.setParameters(camPara);

			float scaleW = (float) ((Screen.mWidth * 1.00) / (cameraWidth * 1.00));
			float scaleH = (float) ((Screen.mHeight * 1.00) / (cameraHeight * 1.00));
			float scale = scaleW > scaleH ? scaleH : scaleW;

			RelativeLayout.LayoutParams layoutPara = new RelativeLayout.LayoutParams(
					(int) (cameraWidth * scale),
					(int) (cameraHeight * scale));

			layoutPara.addRule(RelativeLayout.CENTER_HORIZONTAL);// 设置照相机水平居中
			return layoutPara;
		}
	}

	public int FindFrontCamera() {
		return 1;
		/*
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				return camIdx;
			}
		}
		return -1;
		*/
	}

	public int FindBackCamera() {
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				return camIdx;
			}
		}
		return -1;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.w(TAG, "surfaceChanged++++");

		try {
			//mSurfaceView.setLayoutParams(getParams());
			mCamera.stopPreview();
			mCamera.setPreviewDisplay(mSurfaceView.getHolder());
			mCamera.setDisplayOrientation(getCameraAngle());
			mCamera.startPreview();

			if( ConUtil.opt ){
				int bufferSize = cameraWidth*cameraHeight * ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()) / 8;
				mCamera.addCallbackBuffer(new byte[bufferSize]);
				mCamera.setPreviewCallbackWithBuffer(mCallback);
			}else{
				mCamera.setPreviewCallback(mCallback);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	public void setMetering( Rect areaRect ){/* new Rect(-100, -100, 100, 100) */
		Camera.Parameters params = mCamera.getParameters();

		if (params.getMaxNumMeteringAreas() > 0){ // check that metering areas are supported
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
			meteringAreas.add(new Camera.Area(areaRect, 1000)); // set weight to 60%
			params.setMeteringAreas(meteringAreas);

			mCamera.setParameters(params);
		}
		else{
			Log.e(TAG, "MeteringAreas not supported ");
		}
	}
}
