package com.woting.content.publish.web;

import java.sql.Timestamp;
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
import com.woting.content.publish.utils.CacheUtils;
import com.woting.passport.login.utils.RequestDataUtils;

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
	@RequestMapping(value = "addByApp.do.do")
	@ResponseBody
	public Map<String, Object> addContentByApp(HttpServletRequest request) {
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
}
