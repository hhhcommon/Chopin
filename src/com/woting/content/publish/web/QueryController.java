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
			map.put("ReturnType", "1012");
			map.put("Message", "无法获得用户Id");
			return map;
		}
		String title = m.get("ContentTitle")+"";
		if(title.equals("null")) {
			map.put("ReturnType", "1013");
			map.put("Message", "无法获得内容标题");
			return map;
		}
		String filePath = m.get("FilePath")+"";
		if(filePath.equals("null")) {
			map.put("ReturnType", "1014");
			map.put("Message", "无内容路径");
			return map;
		}
		String channelId = m.get("ChannelId")+"";
		if(channelId.equals("null")) {
			map.put("ReturnType", "1015");
			map.put("Message", "无栏目Id");
			return map;
		}
		String descn = m.get("ContentDecsn")+"";
		if (descn.equals("null") || descn.equals("")) 
			return null;
		map = queryService.addContentByApp(userId,title,filePath,descn,channelId);
		if (map==null) {
			
		}
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
		}else{
			map.put("ReturnType", "1003");
		    map.put("Message", "查询无内容");
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
			map.put("ReturnType", "1012");
			map.put("Message", "无法获得用户Id");
			return map;
		}
		String contentId = m.get("ContentId")+"";
		if(contentId.equals("null")) {
			map.put("ReturnType", "1013");
			map.put("Message", "无法获得内容Id");
			return map;
		}
		map = queryService.removeContentByApp(userId, contentId);
		if (map==null) {
			map = new HashMap<>();
			map.put("ReturnType", "1014");
			map.put("Message", "删除失败");
			return map;
		} else {
			map.put("ReturnType", "1001");
		    map.put("Message", "成功删除");
		    return map;
		}
	}
	
	/**
	 * 移动端添加内容
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "addByWeb.do")
	@ResponseBody
	public Map<String, Object> addByWeb(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String channelids = m.get("ChannelId")+"";
		if(channelids.equals("null") || channelids.equals("")) {
			map.put("ReturnType", "1012");
			map.put("Message", "无法获得栏目Id");
			return map;
		}
		String mastatus = m.get("ContentStatus")+"";
		if (mastatus.equals("null") || mastatus.equals("")) {
			map.put("ReturnType", "1013");
			map.put("Message", "无法获得类型信息");
			return map;
		}
		String themeImg = m.get("ThemeImg")+"";
		if (themeImg.equals("null") || themeImg.equals("")) 
			themeImg = null;
		String mediaSrc = m.get("MediaSrc")+"";
		if(mediaSrc.equals("null") || mediaSrc.equals(""))
			mediaSrc = null;
		String thirdpath = m.get("ThirdPath")+"";
		if(thirdpath.equals("null") || thirdpath.equals(""))
			thirdpath = null;
		String username = m.get("UserName")+"";
		if (!mastatus.equals("一般文章")) {
			if (username.equals("null") || username.equals("")) {
				map.put("ReturnType", "1014");
				map.put("Message", "无法获得选手姓名");
				return map;
			}
		} else {
			username = null;
		}
		String source = m.get("Source")+"";
		if (source.equals("null") || source.equals(""))
			source = null;
		String sourcepath = m.get("SourcePath")+"";
		if(sourcepath.equals("null") || sourcepath.equals(""))
			sourcepath = null;
		List<Map<String, Object>> list = (List<Map<String, Object>>) m.get("List");
		if(list==null || list.size()==0) {
			map.put("ReturnType", "1013");
			map.put("Message", "参数不全");
			return map;
		}
		map = queryService.makeContentHtml(channelids, themeImg, mediaSrc, thirdpath, source, sourcepath, mastatus, username, list);
		return map;
	}
	
	/**
	 * 获取需要修改的已发布内容信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getContentInfo2Updata.do")
	@ResponseBody
	public Map<String, Object> getupdateContentInfo(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		if (m == null || m.size() == 0) {
			map.put("ReturnType", "0000");
			map.put("Message", "无法获取相关的参数");
			return map;
		}
		String contentid = m.get("ContentId")+"";
		if(contentid.equals("null")) {
			map.put("ReturnType", "1011");
			map.put("Message", "缺少内容Id");
			return map;
		}
		map = queryService.getContentInfo2Updata(contentid);
		if(map!=null) {
			map.put("ReturnType", "1001");
		    map.put("Message", "获取成功");
		    return map;
		} else {
			map = new HashMap<>();
			map.put("ReturnType", "1012");
			map.put("Message", "获取失败");
			return map;
		}
	}
	
	/**
	 * 修改的已发布内容信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "updataContentInfo.do")
	@ResponseBody
	public Map<String, Object> updateContentInfo(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String contentid = m.get("ContentId")+"";
		if(contentid.equals("null") || contentid.equals("")) {
			map.put("ReturnType", "1011");
			map.put("Message", "无法获得内容Id");
			return map;
		}
		String channelids = m.get("ChannelId")+"";
		if(channelids.equals("null") || channelids.equals("")) {
			map.put("ReturnType", "1012");
			map.put("Message", "无法获得栏目Id");
			return map;
		}
		String mastatus = m.get("ContentStatus")+"";
		if (mastatus.equals("null") || mastatus.equals("")) {
			map.put("ReturnType", "1013");
			map.put("Message", "无法获得类型信息");
			return map;
		}
		String themeImg = m.get("ThemeImg")+"";
		if (themeImg.equals("null") || themeImg.equals("")) 
			themeImg = null;
		String mediaSrc = m.get("MediaSrc")+"";
		if(mediaSrc.equals("null") || mediaSrc.equals(""))
			mediaSrc = null;
		String thirdpath = m.get("ThirdPath")+"";
		if(thirdpath.equals("null") || thirdpath.equals(""))
			thirdpath = null;
		String username = m.get("UserName")+"";
		if (!mastatus.equals("一般文章")) {
			if (username.equals("null") || username.equals("")) {
				map.put("ReturnType", "1014");
				map.put("Message", "无法获得选手姓名");
				return map;
			}
		} else {
			username = null;
		}
		String source = m.get("Source")+"";
		if (source.equals("null") || source.equals(""))
			source = null;
		String sourcepath = m.get("SourcePath")+"";
		if(sourcepath.equals("null") || sourcepath.equals(""))
			sourcepath = null;
		List<Map<String, Object>> list = (List<Map<String, Object>>) m.get("List");
		if(list==null || list.size()==0) {
			map.put("ReturnType", "1013");
			map.put("Message", "参数不全");
			return map;
		}
		map = queryService.updateContentHtml(contentid, channelids, themeImg, mediaSrc, thirdpath, source, sourcepath, mastatus, username, list);
		return map;
	}
	
	/**
	 * 发布已撤销内容
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "addPubContent.do")
	@ResponseBody
	public Map<String, Object> addPubContent(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String channelid = m.get("ChannelId")+"";
		if(channelid.equals("null")) {
			map.put("ReturnType", "1011");
			map.put("Message", "缺少栏目Id");
			return map;
		}
		String contentid = m.get("ContentId")+"";
		if(contentid.equals("null")) {
			map.put("ReturnType", "1012");
			map.put("Message", "缺少内容Id");
			return map;
		}
		boolean isok = queryService.addPubContentInfo(channelid, contentid);
		if(isok) {
			map.put("ReturnType", "1001");
		    map.put("Message", "发布成功");
		    return map;
		} else {
			map.put("ReturnType", "1013");
			map.put("Message", "发布失败");
			return map;
		}
	}
	
	/**
	 * 修改已发布内容排序号
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "modifyPubSort.do")
	@ResponseBody
	public Map<String, Object> modifyPubSort(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String channelid = m.get("ChannelId")+"";
		if(channelid.equals("null")) {
			map.put("ReturnType", "1011");
			map.put("Message", "缺少栏目Id");
			return map;
		}
		String contentsort = m.get("ContentSort")+"";
		if(contentsort.equals("null")) {
			map.put("ReturnType", "1012");
			map.put("Message", "缺少内容排序号");
			return map;
		}
		int sort = Integer.valueOf(contentsort);
		String contentid = m.get("ContentId")+"";
		if(contentid.equals("null")) {
			map.put("ReturnType", "1013");
			map.put("Message", "缺少内容Id");
			return map;
		}
		boolean isok = queryService.modifyPubSortInfo(channelid, sort, contentid);
		if(isok) {
			map.put("ReturnType", "1001");
		    map.put("Message", "修改成功");
		    return map;
		} else {
			map.put("ReturnType", "1014");
			map.put("Message", "修改失败");
			return map;
		}
	}
	
	/**
	 * 撤销已发布内容
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "removePubContent.do")
	@ResponseBody
	public Map<String, Object> removePubContent(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String channelid = m.get("ChannelId")+"";
		if(channelid.equals("null")) {
			map.put("ReturnType", "1011");
			map.put("Message", "缺少栏目Id");
			return map;
		}
		String contentid = m.get("ContentId")+"";
		if(contentid.equals("null")) {
			map.put("ReturnType", "1012");
			map.put("Message", "缺少内容Id");
			return map;
		}
		boolean isok = queryService.removePubInfo(channelid, contentid);
		if(isok) {
			map.put("ReturnType", "1001");
		    map.put("Message", "撤销成功");
		    return map;
		} else {
			map.put("ReturnType", "1013");
			map.put("Message", "撤销失败");
			return map;
		}
	}
	
	/**
	 * 修改轮播图状态
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "modifyDirectContent.do")
	@ResponseBody
	public Map<String, Object> addDirectContent(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String channelid = m.get("ChannelId")+"";
		if(channelid.equals("null")) {
			map.put("ReturnType", "1011");
			map.put("Message", "缺少栏目Id");
			return map;
		}
		String contentid = m.get("ContentId")+"";
		if(contentid.equals("null")) {
			map.put("ReturnType", "1012");
			map.put("Message", "缺少内容Id");
			return map;
		}
		String str = m.get("ContentStatus")+"";
		if(str.equals("null")) {
			map.put("ReturnType", "1013");
			map.put("Message", "缺少状态信息");
			return map;
		}
		int status = Integer.valueOf(str);
		boolean isok = queryService.modifyDirectContentInfo(channelid, contentid, status);
		if(isok) {
			map.put("ReturnType", "1001");
		    map.put("Message", "撤销成功");
		    return map;
		} else {
			map.put("ReturnType", "1014");
			map.put("Message", "撤销失败");
			return map;
		}
	}
}
