package com.gridViewAdd;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class MainActivity extends Activity {
	GridView gridView;
	ArrayList<Bitmap> aList;
	MyAdapter adapter;
	Context context;
	private String picturePath;
	private Bitmap bmp;

	@SuppressLint("SdCardPath")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullScreen();
		setContentView(R.layout.activity_main);
		initView();

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == aList.size() - 1) {
					new AlertDialog.Builder(context)
							.setItems(new String[] { "从相册添加", "拍照添加" },
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											switch (which) {
											case 0:
												Intent intent = new Intent(
														Intent.ACTION_PICK,
														android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
												startActivityForResult(intent,
														1);
												break;
											case 1:
												Intent intent1 = new Intent();
												intent1.setAction("android.media.action.IMAGE_CAPTURE");
												intent1.addCategory("android.intent.category.DEFAULT");
												// TODO 创建照片存储路径，方便调用
												picturePath = "/mnt/sdcard/DCIM/photo"
														+ DateFormat.format(
																"kkmmss",
																new Date())
																.toString()
														+ ".jpg";
												File file = new File(
														picturePath);
												Uri uri = Uri.fromFile(file);
												intent1.putExtra(
														MediaStore.EXTRA_OUTPUT,
														uri);
												startActivity(intent1);
												break;
											}
										}
									}).create().show();

				} else {
					ImageView iView = (ImageView) view.findViewById(R.id.iv);
					// 获取当前控件绑定图片的原始尺寸
					int[] parm = (int[]) iView.getTag();
					// 当前绑定的图片
					Bitmap bmp = aList.get(position);
					final Dialog dialog = new Dialog(context);
					View contentview = View.inflate(context, R.layout.diaglog,
							null);
					ImageView iv = (ImageView) contentview
							.findViewById(R.id.iv_dialog);
					Button bt = (Button) contentview.findViewById(R.id.btn);
					Bitmap bmBitmap = Bitmap.createScaledBitmap(bmp, parm[0],
							parm[1], true);
					iv.setImageBitmap(bmBitmap);
					dialog.setContentView(contentview);
					bt.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					// 设置对话框尺寸
					Window diaWindow = dialog.getWindow();
					WindowManager.LayoutParams lp = diaWindow.getAttributes();
					lp.width = getResources().getDisplayMetrics().widthPixels;
					lp.height = getResources().getDisplayMetrics().heightPixels - 100;
					diaWindow.setAttributes(lp);
					dialog.show();
				}
			}
		});
	}

	private void initView() {
		gridView = (GridView) findViewById(R.id.grid);
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.qq);
		aList = new ArrayList<Bitmap>();
		aList.add(bmp);
		adapter = new MyAdapter(this, aList);
		gridView.setAdapter(adapter);
		context = MainActivity.this;
	}

	// 点击画囊后执行
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Log.i("info", filePathColumn[0] + "");
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			picturePath = cursor.getString(columnIndex);
			cursor.close();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(picturePath)) {
			// 对添加的图片按比例缩小
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap addbmp = BitmapFactory.decodeFile(picturePath, options);
			options.inJustDecodeBounds = false;
			int be = (int) (options.outHeight / (float) 200);
			if (be <= 0)

				be = 1;
			options.inSampleSize = be;
			addbmp = BitmapFactory.decodeFile(picturePath, options);

			aList.add(addbmp);
			// 先移除用来添加的图标，再添加以保证添加的图片始终在最后
			aList.remove(bmp);
			aList.add(bmp);
			adapter.setDate(aList);
			adapter.notifyDataSetChanged();
			// 刷新后释放，防止手机休眠后自动添加
			picturePath = null;
		}
	}

	private void setFullScreen() {
		// 设置无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 设置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 锁定竖屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}
