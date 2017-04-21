package com.scandemo;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bewant2be.doit.utilslib.DisplayUtil;
import com.bewant2be.doit.utilslib.ToastUtil;
import com.megvii.bean.FaceInfo;
import com.megvii.bean.MGGrayscaleImage;
import com.megvii.bean.MGYUVImage;
import com.megvii.bean.Track;
import com.megvii.facequality.DetectionManager;
import com.megvii.facequality.DetectionManager.DetectionListener;
import com.megvii.facequality.FaceDetector;
import com.megvii.facequality.FaceQualityConfig;
import com.megvii.koalacamonline.R;
import com.xier.ndk.FaceLiveDetect;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import megvii.megfaceandroid.MegfaceDetector;
import megvii.megfaceandroid.MegfaceFace;
import megvii.megfaceandroid.MegfaceFloatPoint;
import megvii.megfaceandroid.MegfaceLandmark;
import megvii.megfaceandroid.MegfaceModel;

import  com.bewant2be.doit.utilslib.CameraView;

public class ScanActivity extends Activity {

	public final static String TAG = "ScanActivity";
	public final static int THRESHHOLD = 0x1E;// suitable for 720p
	//public final static int THRESHHOLD = 0x3C;// suitable for 2688*1512

	private CameraView cameraView1;
	private Camera mCamera1 = null;


	private CameraView cameraView2;
	private Camera mCamera2 = null;

	private DetectionManager mDetectionManager1;
	private DetectionManager mDetectionManager2;

	TextView textView;
	private int display_degree;

	FaceDetector faceDetector = null;
	FaceLiveDetect faceLiveDetect = null;
	MegfaceDetector megfaceDetector =null;


	private float pitchAngle = 0.17f, yawAngle = 0.17f;
	private boolean isDetectorTimeOut;

	ImageView imageView = null;

	DetectionListener detectionListener1 =  new DetectionListener() {

		@Override
		public void detected(List<FaceInfo> faces, List<Track> disappearTracks,
							 List<Track> newTracks, List<Track> stillTrackingTracks) {
			Log.e(TAG, "detected1");

			int face_size = faces.size();
			Log.e(TAG, "face_size: " + face_size);

			Status.bFaceDetected1 = false;

			if (stillTrackingTracks != null && stillTrackingTracks.size() > 0) {
				final Track track = stillTrackingTracks.get(0);

				for (int i = 0; i < faces.size(); i++) {
					FaceInfo faceInfo = faces.get(i);
					if (faceInfo.faceID == track.id) {
						float pitch = faceInfo.pitch;// 上下角度 正数是低头，负数是抬头
						Log.w( TAG, "pitch====" + pitch);
						if (pitch > pitchAngle) {
							Log.e(TAG,"请稍微抬头");
						} else if (pitch < -pitchAngle) {
							Log.e(TAG, "请稍微低头");
						}
						float yaw = faceInfo.yaw;// 左右角度
						if (yaw > yawAngle) {
							Log.e(TAG, "请稍微向右转");
						} else if (yaw < -yawAngle) {
							Log.e(TAG, "请稍微向左转");
						}

						float b1 = faceInfo.gaussianBlur;
						if (b1 < 0.08) {
							// 可以过
						}
						float b2 = faceInfo.motionBlur;
						if (b2 < 0.1) {
							// 过
						}

						float integrity = faceInfo.integrity;// 人脸完整性
						if (integrity < 0.9) {
							Log.e(TAG, "请保持脸部整体出现在框内");
						}

						float brightness = faceInfo.brightness;
						float faceQuality = faceInfo.faceQuality;
						RectF position = faceInfo.position;
						Log.e(TAG, "faceQuality：" + faceQuality + "  高斯模糊：" + b1
								+ "  动作模糊：" + b2 + "  pitch===" + pitch
								+ "  yaw===" + yaw + "  integrity===" + integrity
								+ "  brightness===" + brightness
								+ "  faceQuality===" + faceQuality
								+ "  position：" + position);

						Camera.Parameters params = mCamera1.getParameters();
						Camera.Size previewSize = params.getPreviewSize();
						Log.e(TAG, "previewSize: " + previewSize.width + " " + previewSize.height);

						Log.e(TAG, "bFaceDetected = true");
						Status.bFaceDetected1 = true;
						Status.rectF1 = position;
					} else {
						Log.e(TAG, "No Track");
					}
				}
			}
		}

	};


	int FPS4FaceDetected = 0;

	DetectionListener detectionListener2 =  new DetectionListener() {

		@Override
		public void detected(List<FaceInfo> faces, List<Track> disappearTracks,
							 List<Track> newTracks, List<Track> stillTrackingTracks) {
			Log.e(TAG, "detected2");

			int face_size = faces.size();
			Log.e(TAG, "face_size2: " + face_size);

			Status.bFaceDetected2 = false;

			if(face_size >=1){
				// ..... statistic begin....
				long tick = System.currentTimeMillis();
				if( tick-lastStartTickInASecond > 1000 ){
					Log.e( TAG, "FPS4FaceDetected 222: " + FPS4FaceDetected );
					FPS4FaceDetected = 0;
					lastStartTickInASecond =tick;
				}else{
					FPS4FaceDetected ++;
				}
			}


			if (stillTrackingTracks != null && stillTrackingTracks.size() > 0) {
				final Track track = stillTrackingTracks.get(0);

				for (int i = 0; i < faces.size(); i++) {
					FaceInfo faceInfo = faces.get(i);
					if (faceInfo.faceID == track.id) {
						float pitch = faceInfo.pitch;// 上下角度 正数是低头，负数是抬头
						Log.w( TAG, "pitch====" + pitch);
						if (pitch > pitchAngle) {
							Log.e(TAG,"请稍微抬头");
						} else if (pitch < -pitchAngle) {
							Log.e(TAG, "请稍微低头");
						}
						float yaw = faceInfo.yaw;// 左右角度
						if (yaw > yawAngle) {
							Log.e(TAG, "请稍微向右转");
						} else if (yaw < -yawAngle) {
							Log.e(TAG, "请稍微向左转");
						}

						float b1 = faceInfo.gaussianBlur;
						if (b1 < 0.08) {
							// 可以过
						}
						float b2 = faceInfo.motionBlur;
						if (b2 < 0.1) {
							// 过
						}

						float integrity = faceInfo.integrity;// 人脸完整性
						if (integrity < 0.9) {
							Log.e(TAG, "请保持脸部整体出现在框内");
						}

						float brightness = faceInfo.brightness;
						float faceQuality = faceInfo.faceQuality;
						RectF position = faceInfo.position;
						/*
						Log.e(TAG, "faceQuality：" + faceQuality + "\n高斯模糊：" + b1
								+ "\n动作模糊：" + b2 + "\npitch===" + pitch
								+ "\nyaw===" + yaw + "\nintegrity===" + integrity
								+ "\nbrightness===" + brightness
								+ "\nfaceQuality===" + faceQuality
								+ "\nposition：" + position);*/

						Camera.Parameters params = mCamera2.getParameters();
						Camera.Size previewSize = params.getPreviewSize();
						Log.e(TAG, "previewSize: " + previewSize.width + " " + previewSize.height);

						Log.e(TAG, "bFaceDetected = true");
						Status.bFaceDetected2 = true;
						Status.rectF2 = position;
					} else {
						Log.e(TAG, "No Track");
					}
				}
			}
		}

	};



	boolean bInfradDetecting = false;
	Camera.PreviewCallback previewCallback1 = new Camera.PreviewCallback(){
		@Override
		public void onPreviewFrame(final byte[] data, Camera camera) {

			boolean b = true;
			if(b){
				camera.addCallbackBuffer(data);
				return;
			}

			final int width = Status.previewWidth;
			final int height = Status.previewHeight;

			if(bInfradDetecting){
				if( ConUtil.opt ) {
					mCamera1.addCallbackBuffer(data);
				}

				Log.i(TAG, "bInfradDetecting quit");
				return;
			}
			else{

				Log.i(TAG, "go ahead quit");
			}

			// ..... statistic begin....
			long tick = System.currentTimeMillis();
			if( tick-lastStartTickInASecond > 1000 ){
				Log.e( TAG, "FPS 111: " + FPS );
				FPS = 0;
				lastStartTickInASecond =tick;
			}else{
				FPS ++;
			}

			/*
			if(Status.bNeedFaceDetection){
				Log.e(TAG, "mCameraManager.cameraWidth, mCameraManager.cameraHeight mCameraManager.mDetectionAngle: "
						+ mCameraManager1.cameraWidth + "  "
						+ mCameraManager1.cameraHeight + "  "
						+ mCameraManager1.mDetectionAngle + "  "
						+ data.length + "  " );
				mDetectionManager1.postDetection(new MGYUVImage(data,
						mCameraManager1.cameraWidth, mCameraManager1.cameraHeight,
						mCameraManager1.mDetectionAngle));
			}
			*/

			boolean infrarDetect = true;
			if(infrarDetect ){
				bInfradDetecting = true;
				new Thread(){
					@Override
					public void run() {
						super.run();

						long tick_start = System.currentTimeMillis();

						/*
						int[] rgb = Utils.decodeYUV420SP(data, width, height);

						int[] gray = Utils.decodeYUV420SPGray(data, width, height);

						Bitmap bm8888 = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
						*/
						long tick_b4Megvii = System.currentTimeMillis();

						MegfaceFace face = MegfaceDetector.detect(megfaceDetector.detector, data, width, height);

						int face_1[] = new  int[9];

						if (face == null)
						{
							face_1[0] = face.rect.left;
							face_1[1] = face.rect.top;
							face_1[2] = face.rect.right;
							face_1[3] = face.rect.bottom;
							MegfaceFloatPoint lefteye = MegfaceLandmark.GetLandmark(face.landmark.handleAddr, MegfaceLandmark.LandmarkTag.LEFTEYE_CENTER.value());
							MegfaceFloatPoint righteye = MegfaceLandmark.GetLandmark(face.landmark.handleAddr, MegfaceLandmark.LandmarkTag.RIGHTEYE_CENTER.value());

							face_1[4] = (int) lefteye.x;
							face_1[5] = (int)lefteye.y;
							face_1[6] = (int) righteye.x;
							face_1[7] = (int) righteye.y;

							face_1[8] = 0;
						}
						else
						{

							face_1[8]=1;
						}



						//int face_1[] = runFD(faceDetector, data, width, height);
						long tick_postMegvii = System.currentTimeMillis();
						Log.e(TAG, "time Megvii cost: "+ (tick_postMegvii-tick_b4Megvii));


						//检测人脸检测是否成功，如果face_1[8] == 1,则成功，否则失败
						if (face_1[8] == 1) {
							//byte[] imgData1 = bitMap2RGB(bm8888);
							float huotiScore = -100;
							//Log.e("kkkkk",imgData1[0]+","+imgData1[1]+","+imgData1[2]);
							Log.e("kkkkk", "data.length:" + data.length + ", width*height: " + width + "*" + height + " , " + (float) (data.length / width) / (float) height);
							long tick_b4copy = System.currentTimeMillis();
							byte[] copy = new byte[width*height];
							System.arraycopy(data, 0, copy, 0, width*height);
							long tick_afterCopy = System.currentTimeMillis();	Log.e(TAG, "time copy cost: "+ (tick_afterCopy-tick_b4copy));
							huotiScore = faceLiveDetect.getLiveDetect(copy, width, height
									, face_1[4], face_1[5], face_1[6], face_1[7]);
							long tick_afterLiveDetect = System.currentTimeMillis();	Log.e(TAG, "time LiveDetect cost: "+ (tick_afterLiveDetect-tick_afterCopy));


							if(huotiScore>50.0){// traditional: 50   deep learning: 10.0
							//	textView.setTextColor(Color.GREEN);
								EventBus.getDefault().post(new DataSynEvent(1, 0.0f));
							}else{
								//	textView.setTextColor(Color.RED);
								EventBus.getDefault().post(new DataSynEvent(2, 0.0f));
							}


							//textView.setText( "     Srcore = " + huotiScore );
							Log.e(TAG, "     Srcore = " + huotiScore );
							EventBus.getDefault().post(new DataSynEvent(3, huotiScore));
						} else {
							Log.e(TAG, "bitmap_2 no face detect ,liveDetect fail!");
							EventBus.getDefault().post(new DataSynEvent(0, 0.0f));
						}


						long cons = System.currentTimeMillis() - tick_start;
						Log.i(TAG, "time consume = " + cons );
					}
				}.start();

			}

			// ..... statistic end....
			if( ConUtil.opt ) {
				mCamera1.addCallbackBuffer(data);
			}
		}
	};

	Camera.PreviewCallback previewCallback2 = new Camera.PreviewCallback(){
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			boolean b = true;
			if(b){
				camera.addCallbackBuffer(data);
				return;
			}

			int width = Status.previewWidth;
			int height = Status.previewHeight;

			// ..... statistic begin....
			long tick = System.currentTimeMillis();
			if( tick-lastStartTickInASecond > 1000 ){
				Log.e( TAG, "FPS 2222: " + FPS );
				FPS = 0;
				lastStartTickInASecond =tick;
			}else{
				FPS ++;
			}

			if(Status.bNeedFaceDetection){
				mDetectionManager2.postDetection(new MGYUVImage(data,
						width, height,
						0));
			}

			// ..... statistic end....
			if( ConUtil.opt ) {
				mCamera2.addCallbackBuffer(data);
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Screen.initialize(this);
		setContentView(R.layout.scan_layout);
		display_degree = DisplayUtil.getRotation(this);


		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.ll_surfaces);
		if(display_degree % 180==0){
			linearLayout.setOrientation(LinearLayout.VERTICAL);
		}else{
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		}



		ToastUtil.toastComptible(getApplicationContext(), "display_degree=" + display_degree);
		Log.i(TAG, "display_degree = " + display_degree);

		EventBus.getDefault().register(this);

		init();

		System.loadLibrary("megface-android");
		AssetManager mgr = getApplicationContext().getAssets();
		MegfaceModel detectModel = MegfaceModel.initModel(mgr, "cnntrk_160129.bin");
		// TODO: handle failed init
		long addr = detectModel.modelAddr;
		int size = detectModel.modelSize;
		long det = MegfaceDetector.initDetector(addr, size);
		MegfaceModel.releaseModel(addr);
		megfaceDetector = new MegfaceDetector(det);



		//人脸检测初始化
		faceDetector = new FaceDetector(new FaceQualityConfig.Builder().build());
		faceDetector.init(this, Util.readModel(this));

		faceLiveDetect = new FaceLiveDetect();

		//基于常规的算法
		faceLiveDetect.init(Util.readLiveDetectModel(this,"liveDetectModel_1"),1);

		//基于深度学习的算法
		//faceLiveDetect.init(Util.readLiveDetectModel(this,"liveDetectModel_1"),1);
	}

	private void init() {
		isDetectorTimeOut = ConUtil.isDetectorTimeOut();
		if (isDetectorTimeOut) {
			Log.e(TAG,"isDetectorTimeOut!!");
			return;
		}
		FaceQualityConfig config = new FaceQualityConfig.Builder()
				.setBrightness(0, 255).build();
		mDetectionManager1 = new DetectionManager(config);
		mDetectionManager1.init(this, ConUtil.readModel(this));
		mDetectionManager1.setDetectionListener( detectionListener1 );
		mDetectionManager2 = new DetectionManager(config);
		mDetectionManager2.init(this, ConUtil.readModel(this));
		mDetectionManager2.setDetectionListener( detectionListener2 );
		FaceQualityConfig fConfig = new FaceQualityConfig.Builder().build();
		pitchAngle = fConfig.pitchAngle;
		yawAngle = fConfig.yawAngle;
		float gaussianBlur = fConfig.gaussianBlur;
		float motionBlur = fConfig.motionBlur;
		float maxBrightness = fConfig.maxBrightness;
		float minBrightness = fConfig.minBrightness;

		cameraView1 = (CameraView) findViewById(R.id.cameraView1);
		cameraView1.init(CameraView.FRONT_CAMERA, display_degree, previewCallback1);

		cameraView2 = (CameraView) findViewById(R.id.cameraView2);
		cameraView2.init(CameraView.BACK_CAMERA, display_degree, previewCallback2);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	// ..... statistic begin....
	long lastStartTickInASecond = 0;
	long FPS = 0;
	// ..... statistic end....

	long lastSetMeteringTick = 0;

	@Override
	protected void onPause() {
		super.onPause();
	}

	private boolean isDetected = true;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDetectionManager1 != null) {
			mDetectionManager1.release();
		}


		if (mDetectionManager2 != null) {
			mDetectionManager2.release();
		}
	}


	private int[] runFD(final FaceDetector faceDetector, final byte[] gray, int width,int height) {
		//byte[] gray = Util.getGrayscale(bitmap);
		List<FaceInfo> faceInfos =
				faceDetector.detect(new MGGrayscaleImage(gray, width,height, 0));

		int[] facerect = new int[9];
		//如果没有检测到人脸
		if (faceInfos.size() <= 0) {
			facerect[8] = 0;
			return facerect;
		} else {
			float x = 0;
			float y = 0;
			float w = 0;
			float h = 0;
			int x_l = 0;
			int y_l = 0;
			int x_r = 0;
			int y_r = 0;
			double max = -1000000000000000000.0;
			//获取最大的人脸
			for (FaceInfo oneFace : faceInfos) {
				Log.e("aaaa", oneFace.toString());
				if (oneFace.position.width() * oneFace.position.height() > max) {
					x = oneFace.position.left;
					y = oneFace.position.top;
					w = oneFace.position.width();
					h = oneFace.position.height();

					PointF pointF_leftEye = oneFace.landmark[0];
					PointF pointF_rightEye = oneFace.landmark[1];

					x_l = (int) (pointF_leftEye.x * width + 0.5);
					y_l = (int) (pointF_leftEye.y * height + 0.5);

					x_r = (int) (pointF_rightEye.x * width + 0.5);
					y_r = (int) (pointF_rightEye.y * height + 0.5);


					Log.d("ddd", x_l + "," + y_l + "," + x_r + "," + y_r);

					max = w * h;
				}
			}

			int x_i = (int) (x * width + 0.5);
			int w_i = (int) (w * width - 0.5);
			int y_i = (int) (y * height + 0.5);
			int h_i = (int) (h * height - 0.5);
			w_i = Math.min(w_i, h_i);
			h_i = w_i;

			facerect[0] = x_i;
			facerect[1] = y_i;
			facerect[2] = w_i;
			facerect[3] = h_i;

			facerect[4] = x_l;
			facerect[5] = y_l;
			facerect[6] = x_r;
			facerect[7] = y_r;

			facerect[8] = 1;

			Log.d("w,h:", width + "," + height);

			Log.d("FaceRect : ", "[" + facerect[0] + "," + facerect[1] + "," + facerect[2] + "," + facerect[3] + "]");

			return facerect;
		}
	}


	private byte[] bitMap2RGB(Bitmap bitmap) {
		if (bitmap == null)
			return null;

		int w = bitmap.getWidth();
		byte[] ret = new byte[bitmap.getWidth() * bitmap.getHeight() * 3];
		for (int j = 0; j < bitmap.getHeight(); ++j)
			for (int i = 0; i < bitmap.getWidth(); ++i) {

				int pixel = bitmap.getPixel(i, j);
				int red = ((pixel & 0x00FF0000) >> 16);
				int green = ((pixel & 0x0000FF00) >> 8);
				int blue = pixel & 0x000000FF;

				ret[j * w * 3 + 3 * i] = (byte) red;
				ret[j * w * 3 + 3 * i + 1] = (byte) green;
				ret[j * w * 3 + 3 * i + 2] = (byte) blue;

			}
		return ret;
	}


	@Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
	public void onDataSynEvent(DataSynEvent event) {
		Log.e(TAG, "event---->" + event.getCount());

		int i = event.getCount();
		if( 0==i ){
			bInfradDetecting = false;
			textView.setTextColor(Color.WHITE);
			textView.setText("NO FACE");
		}else if( 1==i ){
			textView.setTextColor(Color.GREEN);
			bInfradDetecting = false;
		}else if( 2==i ){
			textView.setTextColor(Color.RED);
			bInfradDetecting = false;
		}else {
			bInfradDetecting = false;
			textView.setText( "     Srcore = " + event.getFloat() );
			//textView.setTextSize(50);
		}
	}
}