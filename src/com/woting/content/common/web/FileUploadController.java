package com.woting.content.common.web;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;

import com.spiritdata.framework.FConstants;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.core.web.AbstractFileUploadController;
import com.spiritdata.framework.util.FileNameUtils;
import com.spiritdata.framework.util.FileUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.content.common.utils.FileUploadUtils;

import net.coobird.thumbnailator.Thumbnails;

@Controller
public class FileUploadController extends AbstractFileUploadController{
//	private static final String rootpath = "/opt/tomcat_Chopin/webapps/Chopin/";
//	private static final String rootpath = "D:/workIDE/Chopin/WebContent/";
	private static final String webpath = "http://www.wotingfm.com/Chopin/";
	private static final String[] MediaPath = {
			"media/group01/",  //上传的音频文件路径
			"media/group02/",  //上传的视频文件路径
			"media/group03/",  //上传的原始图片文件路径
			"media/group04/"}; //上传的略缩图文件路径
    @Override
    public Map<String, Object> afterUploadOneFileOnSuccess(Map<String, Object> m, Map<String, Object> a, Map<String, Object> p, HttpSession session) {
    	String rootpath = SystemCache.getCache(FConstants.APPOSPATH).getContent()+"";
        String filepath = m.get("storeFilename")+"".trim();
        String filename = FileNameUtils.getFileName(filepath);
        int typenum = FileUploadUtils.getFileType(filename);
        if(typenum>0) {
            String newname = SequenceUUID.getPureUUID()+filename.substring(filename.lastIndexOf("."), filename.length());
            String newpath = rootpath+MediaPath[typenum-1]+newname;
            File f=new File(rootpath+MediaPath[typenum-1]);
            f.mkdirs();
            FileUtils.copyFile(filepath, newpath);
            if(typenum==3) {
            	String smallImgName = "small"+newname;
                String smallImgPath = rootpath+MediaPath[typenum]+smallImgName.trim();
                try {
			        Thumbnails.of(new File(filepath)).size(288, 216).toFile(smallImgPath);
			        m.put("smallFilename", smallImgName);
			        m.put("smallFilepath", webpath+MediaPath[typenum]+smallImgName);
		        } catch (IOException e) {e.printStackTrace();}
            }
            FileUtils.deleteFile(new File(filepath));
            m.remove("warn");
            m.put("storeFilename", newname);
            m.put("storeFilepath", webpath+MediaPath[typenum-1]+newname);
            m.put("success", "TRUE");
        }
        return m;
    }
}