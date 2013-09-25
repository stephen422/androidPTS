package kr.hs.sshs.AndroidPTS.ui;

import kr.hs.sshs.AndroidPTS.logic.CPU;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;

import java.io.File;

import com.example.androidpts.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {
	CPU ARMv7;
	FrameGrabber grabber;

	TextView tvCPU;
	ImageView iv;
	Button btnProcess;
	Button btnBypass;
	EditText etJumpFrame;
	
	IplImage result;
	
	MyHandler mh;
	
	static double progress;

	public TextView gettvCPU() {
		return this.tvCPU;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d("PASS", "Program started");

		mh = new MyHandler();
		
		iv = (ImageView) findViewById(R.id.imageView1);
		tvCPU = (TextView) findViewById(R.id.textView_CPUState);
		btnBypass = (Button) findViewById(R.id.button_Bypass);
		btnProcess = (Button) findViewById(R.id.button_Process);
		btnBypass.setOnClickListener(this);
		btnProcess.setOnClickListener(this);
		etJumpFrame = (EditText) findViewById(R.id.editText_JumpFrame);

		try {
			ARMv7 = new CPU(mh);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("STOP", e.getMessage());
		}
	}

	public void onClick(View v) {
		// IplImage read = cvCreateImage(new CvSize(640, 480), IPL_DEPTH_8U, 4);
		
		try {
			switch (v.getId()) {
			case R.id.button_Process:
				tvCPU.setText("Busy");
				Log.d("PASS", "Processing...");
				
				new Thread() {
					@Override
					public void run() {
						try {
							while (CPU.framecount <= CPU.framelength) {
								result = ARMv7.process();
								mh.sendMessage(mh.obtainMessage(101));	// When not done
								
								if(CPU.foundBall)
									break;
							}
							
							//ARMv7.release();
							
							mh.sendMessage(mh.obtainMessage(100));	// When done
						} catch (java.lang.Exception e) {
							e.printStackTrace();
							Log.e("STOP", e.getMessage());
						}
					}
				}.start();
				break;

			case R.id.button_Bypass:		
				int jump = Integer.valueOf(etJumpFrame.getText().toString());
				ARMv7.jumpFrames(jump);
				cvSaveImage("/mnt/sdcard/result.jpg", ARMv7.process(true));
				File pic2 = new File("/mnt/sdcard", "result.jpg");		
				iv.setImageBitmap(BitmapFactory.decodeFile(pic2.getAbsolutePath()));
				
				Log.d("PASS", "Bypassed " + jump + " frames");
				tvCPU.setText("吏�굹移�(" + CPU.framecount + "踰덉㎏ �꾨젅��");
				break;
			}
		} catch (java.lang.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("STOP", e.getMessage());
		}

		// IplImage read = cvLoadImage("/mnt/sdcard/437.jpg");
		// IplImage gray = cvCreateImage(cvSize(read.width(), read.height()),
		// IPL_DEPTH_8U, 1);
		// cvCvtColor(read, gray, CV_RGB2GRAY);
		/*
		IplImage readB = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 1);
		IplImage readG = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 1);
		IplImage readR = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 1);
		cvSplit(read, readR, readG, readB, null);
		IplImage readA = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 1); // alpha
		cvSet(readA, new CvScalar(256), null);

		IplImage readBGRA = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 4);
		cvMerge(readB, readG, readR, readA, readBGRA);

		Bitmap temp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
		temp.copyPixelsFromBuffer(readBGRA.getByteBuffer());
		iv.setImageBitmap(temp);

		Log.w("FFmpeg", "Reached end FROM BUTTON");
		*/
	}

	public void showState(String state) {
		tvCPU.setText("吏꾪뻾 以�" + CPU.framecount + " frame, " + Math.round((float)CPU.framecount*100.0/CPU.framelength) + "%) - " + state);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public class MyHandler extends Handler {
		public static final int TEST = 0;
		
		@Override
		public void handleMessage(Message msg) {
			File pic2 = null;
			switch(msg.what) {
			
			case CPU.STATE_DETECT_VALUE_CHANGE:
				showState("DVC");
				Log.d("PASS", "DVC.....");
				break;
				
			case CPU.STATE_BLOB_LABELING:
				showState("BL");
				Log.d("PASS", "BL.....");
				break;
				
			case CPU.STATE_BLOB_FILTERING:
				showState("BF");
				Log.d("PASS", "BF.....");
				break;
				
			case CPU.STATE_CANDIDATE_PROCESSING:
				showState("CP");
				Log.d("PASS", "CP.....");
				break;
				
			case CPU.STATE_BLOB_STAMPING:
				showState("BS");
				Log.d("PASS", "BS.....");
				break;
				
			case CPU.STATE_FOUND_BALL:
				showState("BALLFOUND");
				Log.d("PASS", "BALL FOUND");
				break;
			
			case 101:				
				cvSaveImage("/mnt/sdcard/result.jpg", result);
				pic2 = new File("/mnt/sdcard", "result.jpg");		
				iv.setImageBitmap(BitmapFactory.decodeFile(pic2.getAbsolutePath()));
				break;
				
			case 100:				
				cvSaveImage("/mnt/sdcard/result.jpg", result);
				pic2 = new File("/mnt/sdcard", "result.jpg");		
				iv.setImageBitmap(BitmapFactory.decodeFile(pic2.getAbsolutePath()));
				
				tvCPU.setText("�꾨즺! �꾨줈洹몃옩��醫낅즺�⑸땲��");
				Log.d("PASS", "Done! saved result image");
				break;
			}
		}
	}
}