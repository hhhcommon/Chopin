package com.woting.content.publish.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.spiritdata.framework.util.RequestUtils;
import com.woting.content.publish.service.QueryService;

/**
 * 列表查询接口
 *
 * @author wbq
 *
 */
@Controller
@RequestMapping(value="/content/")
public class QueryController {
	@Resource
	private QueryService queryService;

	/**
	 * 移动端添加内容
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "addByApp.do")
	@ResponseBody
	public Map<String, Object> addByApp(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String userId = m.get("UserId")+"";
		if(userId.equals("null")) {
			map.put("ReturnType", "1002");
			map.put("Message", "无法获得用户Id");
			return map;
		}
		String title = m.get("ContentTitle")+"";
		if(title.equals("null")) {
			map.put("ReturnType", "1003");
			map.put("Message", "无法获得内容标题");
			return map;
		}
		String filePath = m.get("FilePath")+"";
		if(filePath.equals("null")) {
			map.put("ReturnType", "1004");
			map.put("Message", "无内容路径");
			return map;
		}
		String channelId = m.get("ChannelId")+"";
		if(channelId.equals("null")) {
			map.put("ReturnType", "1005");
			map.put("Message", "无栏目Id");
			return map;
		}
		String descn = m.get("ContentDecsn")+"";
		map = queryService.addContentByApp(userId,title,filePath,descn,channelId);
		return map;
	}
	
	/**
	 * 获得移动端用户内容列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getListByApp.do")
	@ResponseBody
	public Map<String, Object> getListByApp(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String userId = m.get("UserId")+"";
		if(userId.equals("null")) {
			map.put("ReturnType", "1002");
			map.put("Message", "无法获得用户Id");
			return map;
		}
		List<Map<String, Object>> mm = queryService.getContentListByApp(userId);
		if(mm!=null&&mm.size()>0) {
			map.put("ReturnType", "1001");
			map.put("ResultInfo", mm);
		    map.put("Message", "查询成功");
		}
		
		return map;
	}
	
	/**
	 * 移动端添加内容
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "removeByApp.do")
	@ResponseBody
	public Map<String, Object> removeByApp(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String userId = m.get("UserId")+"";
		if(userId.equals("null")) {
			map.put("ReturnType", "1002");
			map.put("Message", "无法获得用户Id");
			return map;
		}
		String contentId = m.get("ContentId")+"";
		if(contentId.equals("null")) {
			map.put("ReturnType", "1003");
			map.put("Message", "无法获得内容Id");
			return map;
		}
		queryService.removeContentByApp(userId, contentId);
		map.put("ReturnType", "1001");
		map.put("Message", "成功删除");
		return map;
	}
}
