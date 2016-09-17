package com.woting.content.common.utils;

public class FileUploadUtils {
	
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
		if(fileName.contains(".jpg"))
			return 3;
		return 0;
	}
}
