package com.example.fragment;

import java.util.List;
import java.util.Stack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import cn.ireliance.android.common.ui.ComplexGridFragment;

import com.android.custom.launcher.R;
import com.example.setting.adapter.MyGridAdapter;
import com.example.setting.adapter.MyMedia;
import com.example.setting.listener.KeyBackListener;
import com.example.setting.util.MediaHelper;
import com.example.setting.view.GridTitleView;
import com.example.setting.view.UsbTitleView;

/**
 * @author hxy
 * TODO 游标adapter和下拉刷新解决内存溢出问题
 */
public class MyGridFragment extends ComplexGridFragment implements
		KeyBackListener {
	private View layoutMain;
	private View layoutEmpty;
	private GridView mGridView;
	private GridTitleView gridTitleView;
	private MyGridAdapter adapter;
	private TextView textLoading;
	private Stack<List> mStack;
	private List<MyMedia> mList;
	private String path; 
	private int mediaType;
	private MediaHelper mediaHelper;
	
	private boolean isPlayMusic = false;

	private boolean isDataUpdate = false;
	private UsbTitleView usbTitleView;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MyGridFragment() {
	}

	public MyGridFragment(UsbTitleView usbTitleView) {
		this.usbTitleView = usbTitleView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.grid_fragment, container,
				false);
		layoutMain = rootView.findViewById(R.id.layout_grid_fragment_main);
		layoutEmpty = rootView.findViewById(R.id.layout_grid_fragment_empty);
		mGridView = (GridView)rootView.findViewById(R.id.grid);
		
		textLoading = (TextView) rootView.findViewById(R.id.tv_loading);
		gridTitleView = (GridTitleView) rootView.findViewById(R.id.grid_title);
		textLoading.setVisibility(View.VISIBLE);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// load Data

		if (getActivity() != null) {
			Bundle arguments = getArguments();
			mediaType = arguments.getInt("mediaType");
			isPlayMusic = arguments.getBoolean("isPlayMusic",false);
			
			textLoading.setVisibility(View.VISIBLE);
			mGridView.setVisibility(View.GONE);

			mediaHelper = new MediaHelper(getActivity(), 130, 130);

			mStack = new Stack<List>();
			path = Environment.getExternalStorageDirectory().getAbsolutePath();
			try {
				if(isPlayMusic)
					mList = mediaHelper.loadAllMedia(path, mediaType);
				else
					mList = mediaHelper.loadMedia(path + "/", mediaType, true);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("Catch", e.getMessage() + "");
			}

			gridTitleView.setListener(new GridTitleView.GridTitleListener() {

				public void onDeteleStart() {
					adapter.setDelete(true);
					adapter.notifyDataSetChanged();
					final Handler myHandler = new Handler();
					mGridView.requestFocus();
					mGridView.setSelection(0);
				}

				public void onDeteleExecu() {
					// TODO async
					LayoutInflater flater = LayoutInflater.from(getActivity());
					View view = flater.inflate(R.layout.dialog_msg, null);
					
					final TextView textMsg = (TextView)view.findViewById(R.id.text_dialog_msg);
					textMsg.setText("Are you sure you want to delete?");
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setView(view);
					builder.setPositiveButton("Delete",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									deleteFile();
								}
							});
					builder.setNegativeButton("Cancle",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									deteleFileCanle();
								}
							});

					Dialog alertDialog = builder.create();
					alertDialog.show();

				}

				public void onDeteleCancel() {
					// TODO async
					deteleFileCanle();
				}

				public void onChanged() {
					updateUI();
					//TODO move focus to last item
				}
			});

			usbTitleView.setListener(new UsbTitleView.OnBackListener() {
				public void onBack() {
					onKeyBackDown();
					usbTitleView.requestFocus();
				}
			});

			updateUI();
			
			textLoading.setVisibility(View.GONE);
			mGridView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public GridView getRawGridView() {
		return mGridView;
	}

	public void openFile(int position) {
		if (getActivity() != null && !adapter.isDelete()) {
			MyMedia myMedia = mList.get(position);
			Integer type = myMedia.getMediaType();
			if (type == null) {
				Toast.makeText(getActivity(), "File or Folder can't open!",
						Toast.LENGTH_LONG).show();
				return;
			}
			if (type.intValue() == MyMedia.TYPE_DIR) {
				mStack.push(mList);
				path = path + "/" + myMedia.getName();
				try {
					mList = mediaHelper.loadMedia(path + "/", mediaType, false);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Catch", e.getMessage() + "");
				}
				updateUI();
			} else if (type.intValue() == MyMedia.TYPE_ALL) {
				mStack.push(mList);
				try {
					mList = mediaHelper.loadAllMedia(path + "/", mediaType);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Catch", e.getMessage() + "");
				}
				// 放到后面来，前面直接用根目录取，本来不用这个，只是为了配合back的时候去substring回来的
				path = path + "/" + myMedia.getName();
				updateUI();
			} else {
				String myMediaPath = myMedia.getPath();
				String mimeType = myMedia.getMimeType();
				if (myMediaPath != null && mimeType != null){
					if(isPlayMusic)
						;//TODO play music
					else
						mediaHelper.play(myMediaPath, mimeType, mediaType);
				}
				else {
					//TODO dialog
					Toast.makeText(getActivity(), "File can't open!",
							Toast.LENGTH_LONG).show();
				}

			}
		} else if (getActivity() != null && adapter.isDelete()) {
			MyMedia myMedia = mList.get(position);
			myMedia.setCheck(!myMedia.isCheck());
			adapter.notifyDataSetChanged();
			if (myMedia.isCheck()) {
				gridTitleView.plusCount();
			} else {
				gridTitleView.minusCount();
			}
		}
	}

	private void deleteFile() {
		for (MyMedia myMedia : mList) {
			if (myMedia.isCheck()) {
				if (myMedia.getMediaType() == MyMedia.TYPE_ALL) {
					List<MyMedia> allMedia = mediaHelper.loadAllMedia(path
							+ "/", mediaType);
					for (MyMedia m : allMedia) {
						if (mediaHelper.deleteMedia(mediaType, m.getId(),
								m.getPath()))
							isDataUpdate = true;
						else{
							showDeleteFail(m.getName());
						}
					}
					break;
				} else if (myMedia.getMediaType() == MyMedia.TYPE_DIR) {
					String dir = path + "/" + myMedia.getName();
					List<MyMedia> allMedia = mediaHelper.loadAllMedia(
							dir + "/", mediaType);
					for (MyMedia m : allMedia) {
						if (mediaHelper.deleteMedia(mediaType, m.getId(),
								m.getPath()))
							isDataUpdate = true;
						else
							showDeleteFail(m.getName());
					}
				} else {
					if (mediaHelper.deleteMedia(mediaType, myMedia.getId(),
							myMedia.getPath()))
						isDataUpdate = true;
					else
						showDeleteFail(myMedia.getName());
				}

				// if (myMedia.getMediaType() == MyMedia.TYPE_DIR) {
				// String dir = path+"/"+myMedia.getName();
				// if(mediaType == MyMedia.TYPE_OTHER){
				// List<MyMedia> loadOther =
				// mediaHelper.loadAllOther(dir + "/");
				// for (MyMedia m : loadOther) {
				// if (mediaHelper.deleteMedia(mediaType,
				// m.getId())) {
				// isDataUpdate = true;
				// }
				// }
				// } else {
				// if (mediaHelper.deleteDirFile(mediaType,dir)) {
				// isDataUpdate = true;
				// }
				// }
				//
				// } else if(myMedia.getMediaType() !=
				// MyMedia.TYPE_ALL){
				// if (mediaHelper.deleteAduio(mediaType,
				// myMedia.getId(),myMedia.getPath())) {
				// mList.remove(myMedia);
				// isDataUpdate = true;
				// }
				// }
			}
		}

		mList = mediaHelper.loadMedia(path + "/", mediaType, mStack.empty());
		updateUI();
		if(mList.size() > 0){
			mGridView.requestFocus();
			mGridView.setSelection(0);
		}
	}

	public void deteleFileCanle(){
		for (MyMedia myMedia : mList) {
			myMedia.setCheck(false);
		}
		adapter.setDelete(false);
		adapter.notifyDataSetChanged();
		mGridView.requestFocus();
		mGridView.setSelection(0);
	}
	
	private void showDeleteFail(String name){
		Toast.makeText(getActivity(), "Delete File " + name +" failed.", Toast.LENGTH_SHORT).show();
	}
	
	public boolean onKeyBackDown() {
		if(isPlayMusic){
			getActivity().finish();
			return false;
		}
		
		if (!mStack.isEmpty()) {
			path = path.substring(0, path.lastIndexOf("/"));
			if (isDataUpdate) {
				mStack.pop();
				try {
					mList = mediaHelper.loadMedia(path + "/", mediaType,
							mStack.empty());
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Catch", e.getMessage() + "");
				}
			} else
				mList = mStack.pop();
			adapter.setDelete(false);
			gridTitleView.reset();
			updateUI();
			return true;
		}
		return false;
	}

	public void updateUI() {
		if (mList == null || mList.size() == 0) {
			layoutMain.setVisibility(View.GONE);
			layoutEmpty.setVisibility(View.VISIBLE);
			
		} else {
			layoutMain.setVisibility(View.VISIBLE);
			layoutEmpty.setVisibility(View.GONE);
			
			adapter = new MyGridAdapter(getActivity(), mList);
			if(isPlayMusic){
				boolean isList = gridTitleView.isList();
				gridTitleView.setPlayMusic(true);
				mGridView.setNumColumns(isList ? 1 : 7);
				adapter.setPlayMusic(true);
				adapter.setList(gridTitleView.isList());
				//TODO move focus to curr play
			}
			mGridView.setAdapter(adapter);
			gridTitleView.setTotal(mList == null ? 0 : mList.size());
		}
		if(isPlayMusic){
			usbTitleView.setShowBack(true);
			usbTitleView.setLeftGone();
		}else
			usbTitleView.setShowBack(!mStack.empty());
		usbTitleView.setRightText(mStack.empty() ? getMediaTypeName() : path
				.substring(path.lastIndexOf("/") + 1));
		
	}

	private String getMediaTypeName() {
		switch (mediaType) {
		case MyMedia.TYPE_VIDEO:
			return "Video";
		case MyMedia.TYPE_MUSIC:
			return "Music";
		case MyMedia.TYPE_GALLERY:
			return "Gallery";
		case MyMedia.TYPE_OTHER:
			return "Other Files";
		}
		return "";
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

}
