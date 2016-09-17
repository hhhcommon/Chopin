package com.woting.content.common.web;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Controller;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.core.web.AbstractFileUploadController;
import com.spiritdata.framework.util.FileNameUtils;
import com.spiritdata.framework.util.FileUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.ChopinConstants;
import com.woting.content.common.utils.FileUploadUtils;
import net.coobird.thumbnailator.Thumbnails;

@Controller
public class FileUploadController extends AbstractFileUploadController{
	private static final String rootpath = "/opt/tomcat_Chopin/webapps/Chopin/";
//	private static final String rootpath = "D:/workIDE/Chopin/WebContent/";
	private static final String webpath = "http://182.92.175.134:1108/Chopin/";
	private static final String[] MediaPath = {
			"media/group01/",  //上传的音频文件路径
			"media/group02/",  //上传的视频文件路径
			"media/group03/",  //上传的原始图片文件路径
			"media/group04/"}; //上传的略缩图文件路径
    @Override
    public Map<String, Object> afterUploadOneFileOnSuccess(Map<String, Object> m, Map<String, Object> a, Map<String, Object> p) {
        String filepath = m.get("storeFilename")+"";
        String filename = FileNameUtils.getFileName(filepath);
        int typenum = FileUploadUtils.getFileType(filename);
        if(typenum>0) {
            String newname = SequenceUUID.getPureUUID()+filename.substring(filename.lastIndexOf("."), filename.length());
            String newpath = rootpath+MediaPath[typenum-1]+newname;
            if(typenum==3) {
            	String smallImgName = SequenceUUID.getPureUUID()+filename.substring(filename.lastIndexOf("."), filename.length());
                String smallImgPath = rootpath+MediaPath[typenum]+smallImgName;
                try {
			        Thumbnails.of(new File(filepath)).size(100, 100).toFile(smallImgPath);
			        m.put("smallFilename", smallImgName);
			        m.put("smallFilepath", webpath+MediaPath[typenum]+smallImgName);
		        } catch (IOException e) {e.printStackTrace();}
            }
            FileUtils.copyFile(filepath, newpath);
            FileUtils.deleteFile(new File(filepath));
            m.remove("warn");
            m.put("orglFilename", newname);
            m.put("storeFilePath", webpath+MediaPath[typenum-1]+newname);
            m.put("storeFilename", webpath+MediaPath[typenum-1]+newname);
        }
        return m;
    }

    @Override
    public void afterUploadAllFiles(Map<String, Object> retMap, Map<String, Object> a, Map<String, Object> p) {
    	
    }
}