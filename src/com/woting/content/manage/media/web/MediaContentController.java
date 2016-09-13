package com.woting.content.manage.media.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.spiritdata.framework.util.StringUtils;
import com.spiritdata.framework.util.RequestUtils;
import com.woting.content.manage.media.service.MediaContentService;

@Controller
@RequestMapping(value="/content/")
public class MediaContentController {
	@Resource
	private MediaContentService mediaContentService;

	
    @RequestMapping(value="getContents.do")
    @ResponseBody
    public Map<String,Object> getContents(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取相关的参数");
                return map;
            }
            String channelId=m.get("ChannelId")+"";
            if (channelId.equals("null")) {
            	map.put("ReturnType", "1002");
                map.put("Message", "无法获得Id");
                return map;
            }
            int perSize=3;
            String perSizestr = m.get("PerSize")+"";
            if (!perSizestr.equals("null")) 
            	perSize = Integer.parseInt(perSizestr);
            int pageSize=10;
            String pageSizestr = m.get("PageSize")+"";
            if(!pageSizestr.equals("null"))
            	pageSize = Integer.parseInt(pageSizestr);
            int page=1;
            String pagestr = m.get("Page")+"";
            if(!pagestr.equals("null"))
            	page = Integer.parseInt(pagestr);
            String beginCatalogId=(m.get("BeginCatalogId")==null?null:m.get("BeginCatalogId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(beginCatalogId)) beginCatalogId=null;
            
            List<Map<String, Object>> contents=mediaContentService.getContents(channelId, perSize, page, pageSize);
            if (contents!=null&&contents.size()>0) {
                map.put("ResultList", contents);
                map.put("AllCount", contents.size());
                map.put("ReturnType", "1001");
            } else {
                map.put("ReturnType", "1011");
                map.put("Message", "查询无内容");
            }
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }
}
