package com.woting.content.common.web;

import java.io.File;
import java.util.Map;
import org.springframework.stereotype.Controller;
import com.spiritdata.framework.core.web.AbstractFileUploadController;
import com.spiritdata.framework.util.FileNameUtils;
import com.spiritdata.framework.util.FileUtils;
import com.spiritdata.framework.util.SequenceUUID;

@Controller
public class FileUploadController extends AbstractFileUploadController{
    @Override
    public Map<String, Object> afterUploadOneFileOnSuccess(Map<String, Object> m, Map<String, Object> a, Map<String, Object> p) {
        String filepath = m.get("storeFilename")+"";
        String filename = FileNameUtils.getFileName(filepath);
        String path = FileNameUtils.getFilePath(filepath);
        String newname = SequenceUUID.getPureUUID()+filename.substring(filename.lastIndexOf("."), filename.length());
        String newpath = path+"/"+newname;
        FileUtils.copyFile(filepath, newpath);
        FileUtils.deleteFile(new File(filepath));
        m.remove("warn");
        m.put("orglFilename", newname);
        m.put("storeFilename",newpath);
        return m;
    }

    @Override
    public void afterUploadAllFiles(Map<String, Object> retMap, Map<String, Object> a, Map<String, Object> p) {
    	
    }
}