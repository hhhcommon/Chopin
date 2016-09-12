package com.woting.content.manage.media.web;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.spiritdata.framework.FConstants;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.model.SeqMediaAsset;
import com.spiritdata.framework.util.RequestUtils;
import com.woting.content.manage.media.service.MediaContentService;

@Controller
public class MediaContentController {
	@Resource
	private MediaContentService mediaContentService;

	/**
     * 获得内容（某栏目或分类下的内容）
     * @param request
     * @return 所获得的内容，包括分页
     */
    @RequestMapping(value="getContents.do")
    @ResponseBody
    public Map<String,Object> getContents(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            }
            //1-得到模式Id
            String channelId=m.get("channelId")+"";
            if (StringUtils.isNullOrEmptyOrSpace(channelId)) {
            	map.put("ReturnType", "1002");
                map.put("Message", "缺少栏目Id");
                return map;
            }
            //2-得到每分类条目数
            int perSize=3;
            String perSizestr = m.get("PerSize")+"";
            if (!StringUtils.isNullOrEmptyOrSpace(perSizestr)) 
            	perSize = Integer.parseInt(perSizestr);
            //3-得到每页条数
            int pageSize=10;
            String pageSizestr = m.get("pageSize")+"";
            if(!StringUtils.isNullOrEmptyOrSpace(pageSizestr))
            	pageSize = Integer.parseInt(pageSizestr);
            //7-得到页数
            int page=1;
            try {page=Integer.parseInt(m.get("Page")+"");} catch(Exception e) {};
            //8-得到开始分类Id
            String beginCatalogId=(m.get("BeginCatalogId")==null?null:m.get("BeginCatalogId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(beginCatalogId)) beginCatalogId=null;
            
//            Map<String, Object> contents=mediaContentService.getContents(catalogType, catalogId, resultType, mediaType, perSize, pageSize, page, beginCatalogId, pageType, mk, filter);
//            if (contents!=null&&contents.size()>0) {
//                map.put("ResultList", contents);
//                map.put("ReturnType", "1001");
//            } else {
//                map.put("ReturnType", "1011");
//                map.put("Message", "没有查到任何内容");
//            }
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }
	
	
	/**
	 * 删除单体节目信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/media/removeMediaInfo.do")
	@ResponseBody
	public Map<String, Object> removeMediaInfo(HttpServletRequest request){
		Map<String, Object> map = new HashMap<String,Object>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String userid = m.get("UserId")+"";
		if(StringUtils.isNullOrEmptyOrSpace(userid)||userid.toLowerCase().equals("null")){
			map.put("ReturnType", "1011");
			map.put("Message", "无用户信息");
			return map;
		}
		String contentid = m.get("ContentId")+"";
		if(contentid.toLowerCase().equals("null")){
			map.put("ReturnType", "1011");
			map.put("Message", "无专辑信息");
			return map;
		}
		map = mediaContentService.removeMediaAsset(contentid);
		return map;
	}
}
