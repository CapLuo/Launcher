package com.example.setting.adapter;

public class MyMedia {

	public static final int TYPE_VIDEO = 0;
	public static final int TYPE_VIDEO_N = 1;
	public static final int TYPE_MUSIC = 2;
	public static final int TYPE_MUSIC_N = 3;
	public static final int TYPE_GALLERY = 4;
	public static final int TYPE_GALLERY_N = 5;
	public static final int TYPE_OTHER = 6;
	public static final int TYPE_DIR = 7;
	public static final int TYPE_ALL = 8;

	// 图标(缩略图)
	private Object image;

	// 媒体类型：视频、音乐、图片、其它
	private Integer mediaType;

	// 如果是文件夹,文件夹下的文件总数
	private Integer total;

	// 文件名
	private String name;

	private String mimeType;

	//绝对路径
	private String path;

	private boolean isCheck = false;

	private String id;

	//音乐时长
	private Integer duration;

	public Object getImage() {
		return image;
	}

	public void setImage(Object image) {
		this.image = image;
	}

	public Integer getMediaType() {
		return mediaType;
	}

	public void setMediaType(Integer mediaType) {
		this.mediaType = mediaType;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

}
