package com.woting.content.common.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
	
	public static File createFile(String path) {
		File file = new File(path);
		try {
			if (!file.exists()) {
				if (!file.getParentFile().exists()){
					file.getParentFile().mkdirs();
				}
				else {
					file.createNewFile();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return file;
	}
	
	public static boolean writeFile(String jsonstr, String path) {
		File file = createFile(path);
		try {
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
			BufferedWriter writer = new BufferedWriter(write);
			writer.write(jsonstr);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (file.exists())
			return true;
		else
			return false;
	}
	
	/** 
	 * 删除单个文件 
	 * @param   sPath    被删除文件的文件名 
	 */  
	public static void deleteFile(String sPath) {  
	    File file = new File(sPath);
	    // 路径为文件且不为空则进行删除 
	    if (file.isFile() && file.exists()) {
	        file.delete();
	    }
	}
}
