package com.example.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.custom.launcher.R;
import com.example.setting.PlayMusicActivity;
import com.example.setting.adapter.MyGridAdapter;
import com.example.setting.adapter.MyMedia;
import com.example.setting.listener.KeyBackListener;
import com.example.setting.util.MediaHelper;
import com.example.setting.util.UpdateUITask;
import com.example.setting.util.Util;
import com.example.setting.view.GridTitleView;
import com.example.setting.view.UsbTitleView;

@SuppressLint("ValidFragment")
public class MyGridFragment extends ComplexGridFragment implements
		KeyBackListener {
	private View layoutMain;
	private View layoutEmpty;
	private GridView mGridView;
	private GridTitleView gridTitleView;
	private MyGridAdapter adapter;
	private Stack<List> mStack;
	private List<MyMedia> mList;
	private String path;
	private int mediaType;
	private MediaHelper mediaHelper;

	private boolean isPlayMusic = false;
	private boolean isDataUpdate = false;
	private boolean isAllPage = false;
	private UsbTitleView usbTitleView;

	private String filePath;
	private ExecutorService threadPool;

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
		mGridView = (GridView) rootView.findViewById(R.id.grid);

		gridTitleView = (GridTitleView) rootView.findViewById(R.id.grid_title);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getActivity() != null) {
			Bundle arguments = getArguments();
			mediaType = arguments.getInt("mediaType");
			isPlayMusic = arguments.getBoolean("isPlayMusic", false);
			filePath = arguments.getString("path");
			mediaHelper = new MediaHelper(getActivity(), 130, 130);
			mStack = new Stack<List>();
			path = Util.getRootFilePath();
			threadPool = Executors.newCachedThreadPool();
			UpdateUITask task = new UpdateUITask(getActivity(),
					new UpdateUITask.TaskListener() {

						public void updatePageData() {
							initData();
						}

						public void updatePageUI() {
							initView();
						}
					});
			task.setTag("onActivityCreated");
			task.executeOnExecutor(threadPool);
		}
		
	}

	private void initData() {
		// 音乐播放和视频，图片都是 打开loadAll
		if (isPlayMusic || filePath != null) {
			mList = mediaHelper.loadAllMedia(path, mediaType);
			mStack.push(new ArrayList<MyMedia>());
			// 放到后面来，前面直接用根目录取，本来不用这个，只是为了配合back的时候去substring回来的
			path = path + "/All " + getMediaTypeName();
			isAllPage = true;
		} else
			mList = mediaHelper.loadMedia(path + "/", mediaType, true);
	}

	private void initView() {
		gridTitleView.setListener(new GridTitleView.GridTitleListener() {

			public void onDeteleStart() {
				adapter.setDelete(true);
				adapter.notifyDataSetChanged();
				final Handler myHandler = new Handler();
				if (mList.size() > 0) {
					mGridView.requestFocus();
					mGridView.setSelection(0);
				}
			}

			public void onDeteleExecu() {
				LayoutInflater flater = LayoutInflater.from(getActivity());
				View view = flater.inflate(R.layout.dialog_msg, null);

				final TextView textMsg = (TextView) view
						.findViewById(R.id.text_dialog_msg);
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
				deteleFileCanle();
			}

			public void onChanged() {
				updateUI();
				// TODO move focus to last item
			}
		});

		usbTitleView.setListener(new UsbTitleView.OnBackListener() {
			public void onBack() {
				onKeyBackDown();
				usbTitleView.requestFocus();
			}
		});

		updateUI();
	}

	@Override
	public GridView getRawGridView() {
		return mGridView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (filePath != null) {
			// 把焦点移动到选择的文件上
			// TODO do it failed
			// if file.equls "All"
			// int position = mediaHelper.getPositionByPath(mediaType,
			// path.substring(0,path.lastIndexOf("/")), filePath);
			// mGridView.requestFocus();
			// mGridView.setSelection(0);
		}
	}

	public void open(int position) {
		if (getActivity() != null && !adapter.isDelete()) {
			final MyMedia myMedia = mList.get(position);
			Integer type = myMedia.getMediaType();
			if (type == null) {
				Toast.makeText(getActivity(), "File or Folder can't open!",
						Toast.LENGTH_LONG).show();
				return;
			}
			if (type.intValue() == MyMedia.TYPE_DIR) {
				openDir(myMedia);
			} else if (type.intValue() == MyMedia.TYPE_ALL) {
				openAll(myMedia);
			} else {
				openFile(myMedia);
			}
		} else if (getActivity() != null && adapter.isDelete()) {
			// 点击更改选中状态
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

	private void openFile(final MyMedia myMedia) {
		String myMediaPath = myMedia.getPath();
		String mimeType = myMedia.getMimeType();
		if (myMediaPath != null && mimeType != null) {
			if (isPlayMusic) {
				Activity activity = getActivity();
				if (activity instanceof PlayMusicActivity)
					((PlayMusicActivity) activity).MusicPlay(myMedia.getId());
			} else {
				mediaHelper.play(myMediaPath, mimeType, mediaType,
						myMedia.getId());
			}
		} else {
			// TODO dialog
			Toast.makeText(getActivity(), "File can't open!", Toast.LENGTH_LONG)
					.show();
		}
	}

	private void openDir(final MyMedia myMedia) {
		UpdateUITask task = new UpdateUITask(getActivity(),
				new UpdateUITask.TaskListener() {

					public void updatePageData() {
						isAllPage = false;// 其实这里本来就一定是false
						mStack.push(mList);
						path = path + "/" + myMedia.getName();
						mList = mediaHelper.loadMedia(path + "/", mediaType,
								false);
					}

					public void updatePageUI() {
						updateUI();
					}
				});
		task.setTag("openDir");
		task.executeOnExecutor(threadPool);
	}

	private void openAll(final MyMedia myMedia) {
		UpdateUITask task = new UpdateUITask(getActivity(),
				new UpdateUITask.TaskListener() {

					public void updatePageData() {
						mStack.push(mList);
						mList = mediaHelper.loadAllMedia(path + "/", mediaType);
						isAllPage = true;
						// 放到后面来，前面直接用根目录取，本来不用这个，只是为了配合back的时候去substring回来的
						path = path + "/" + myMedia.getName();
					}

					public void updatePageUI() {
						updateUI();
					}
				});
		task.setTag("openAll");
		task.executeOnExecutor(threadPool);
	}

	private void deleteFile() {
		UpdateUITask task = new UpdateUITask(getActivity(),
				new UpdateUITask.TaskListener() {

					public void updatePageData() {
						for (MyMedia myMedia : mList) {
							if (myMedia.isCheck()) {
								if (myMedia.getMediaType() == MyMedia.TYPE_ALL) {
									List<MyMedia> allMedia = mediaHelper
											.loadAllMedia(path + "/", mediaType);
									for (MyMedia m : allMedia) {
										if (mediaHelper.deleteMedia(mediaType,
												m.getId(), m.getPath()))
											isDataUpdate = true;
										else {
											showDeleteFail(m.getName());
										}
									}
									break;
								} else if (myMedia.getMediaType() == MyMedia.TYPE_DIR) {
									String dir = path + "/" + myMedia.getName();
									List<MyMedia> allMedia = mediaHelper
											.loadAllMedia(dir + "/", mediaType);
									for (MyMedia m : allMedia) {
										if (mediaHelper.deleteMedia(mediaType,
												m.getId(), m.getPath()))
											isDataUpdate = true;
										else
											showDeleteFail(m.getName());
									}
								} else {
									if (mediaHelper.deleteMedia(mediaType,
											myMedia.getId(), myMedia.getPath()))
										isDataUpdate = true;
									else
										showDeleteFail(myMedia.getName());
								}
							}
						}
					}

					public void updatePageUI() {
						reLoadData();
						updateUI();
						if (mList.size() > 0) {
							// TODO it's failed
							// mGridView.requestFocus();
							// mGridView.setSelection(0);
						}
					}
				});
		task.setTag("deleteFile");
		task.executeOnExecutor(threadPool);
	}

	public void deteleFileCanle() {
		for (MyMedia myMedia : mList) {
			myMedia.setCheck(false);
		}
		adapter.setDelete(false);
		adapter.notifyDataSetChanged();
		if (mList.size() > 0) {
			mGridView.requestFocus();
			mGridView.setSelection(0);
		}
	}

	private void showDeleteFail(String name) {
		Toast.makeText(getActivity(), "Delete File " + name + " failed.",
				Toast.LENGTH_SHORT).show();
	}

	public boolean onKeyBackDown() {
		if (isPlayMusic) {
			getActivity().finish();
			return false;
		}

		if (!mStack.isEmpty()) {
			// 返回的上层一定不是所有文件的界面
			UpdateUITask task = new UpdateUITask(getActivity(),
					new UpdateUITask.TaskListener() {

						public void updatePageData() {
							isAllPage = false;
							path = path.substring(0, path.lastIndexOf("/"));
							mList = mStack.pop();
							if (isDataUpdate || mList.size() == 0) {
								reLoadData();
							}
						}

						public void updatePageUI() {
							adapter.setDelete(false);
							gridTitleView.reset();
							updateUI();
						}
					});
			task.setTag("onKeyBackDown");
			task.executeOnExecutor(threadPool);
			return true;
		}
		return false;
	}

	public void reLoadData() {
		if (isAllPage) {
			mList = mediaHelper.loadAllMedia(
					path.substring(0, path.lastIndexOf("/")), mediaType);
		} else {
			mList = mediaHelper
					.loadMedia(path + "/", mediaType, mStack.empty());
		}
	}

	public void updateUI() {
		if (mList == null || mList.size() == 0) {
			layoutMain.setVisibility(View.GONE);
			layoutEmpty.setVisibility(View.VISIBLE);

		} else {
			layoutMain.setVisibility(View.VISIBLE);
			layoutEmpty.setVisibility(View.GONE);

			adapter = new MyGridAdapter(getActivity(), mList);
			if (isPlayMusic) {
				boolean isList = gridTitleView.isList();
				gridTitleView.setPlayMusic(true);
				mGridView.setNumColumns(isList ? 1 : 7);
				adapter.setPlayMusic(true);
				adapter.setList(gridTitleView.isList());
				// TODO move focus to curr play
			}
			mGridView.setAdapter(adapter);
			gridTitleView.setTotal(mList == null ? 0 : mList.size());
		}
		if (isPlayMusic) {
			usbTitleView.setShowBack(true);
			usbTitleView.setLeftGone();
		} else
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

	public boolean isDataUpdate() {
		return isDataUpdate;
	}

}
