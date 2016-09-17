package com.woting.content.common.web;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Controller;
import com.spiritdata.framework.core.web.AbstractFileUploadController;
import com.spiritdata.framework.util.FileNameUtils;
import com.spiritdata.framework.util.FileUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.content.common.utils.FileUploadUtils;
import net.coobird.thumbnailator.Thumbnails;

@Controller
public class FileUploadController extends AbstractFileUploadController{
	private static final String[] MediaPath = {
			"http://182.92.175.134:1108/Chopin/media/group01/",  //上传的音频文件路径
			"http://182.92.175.134:1108/Chopin/media/group02/",  //上传的视频文件路径
			"http://182.92.175.134:1108/Chopin/media/group03/",  //上传的原始图片文件路径
			"http://182.92.175.134:1108/Chopin/media/group04/"}; //上传的略缩图文件路径
    @Override
    public Map<String, Object> afterUploadOneFileOnSuccess(Map<String, Object> m, Map<String, Object> a, Map<String, Object> p) {
        String filepath = m.get("storeFilename")+"";
        String filename = FileNameUtils.getFileName(filepath);
        int typenum = FileUploadUtils.getFileType(filename);
        if(typenum>0) {
            String newname = SequenceUUID.getPureUUID()+filename.substring(filename.lastIndexOf("."), filename.length());
            String newpath = MediaPath[typenum-1]+newname;
            if(typenum==3) {
            	String smallImgName = SequenceUUID.getPureUUID()+filename.substring(filename.lastIndexOf("."), filename.length());
                String smallImgPath = MediaPath[typenum]+smallImgName;
                try {
			        Thumbnails.of(new File(filepath)).size(100, 100).toFile(smallImgPath);
			        m.put("smallFileName", smallImgName);
			        m.put("smallFilePath", smallImgPath);
		        } catch (IOException e) {e.printStackTrace();}
            }
            FileUtils.copyFile(filepath, newpath);
            FileUtils.deleteFile(new File(filepath));
            m.remove("warn");
            m.put("orglFileName", newname);
            m.put("storeFilePath",newpath);
        }
        return m;
    }

    @Override
    public void afterUploadAllFiles(Map<String, Object> retMap, Map<String, Object> a, Map<String, Object> p) {
    	
    }
}