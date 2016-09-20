package com.woting.content.common.utils;

public class FileUploadUtils {
	private static final String[] mediatype = {".mp3",".MP3",".mP3",".Mp3"};
	private static final String[] videotype = {".mp4",".MP4",".mP4",".Mp4"};
	private static final String[] picturetype = {".jpg",".png",".JPG",".PNG",".Jpg",".jPG",".jPg",".jpG",".JpG"
			                                     ,"JPg",".Png",".PNg",".pNG",".pnG",".pNg",".PnG"};
	/**
	 * 判断文件上传的类型
	 * @return
	 * 
	 * 返回0:上传文件类型未知
	 * 返回1:上传文件类型为音频文件
	 * 返回2:上传文件类型为视频文件
	 * 返回3:上传文件类型为图片文件
	 */
	public static int getFileType(String fileName) {
		for (String str : mediatype) {
			if(fileName.contains(str))
				return 1;
		}
		for (String str : videotype) {
			if(fileName.contains(str))
				return 2;
		}
		for (String str : picturetype) {
			if(fileName.contains(str))
				return 3;
		}
		return 0;
	}
}
