package com.woting.content.manage.media.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.spiritdata.framework.util.RequestUtils;
import com.spiritdata.framework.util.StringUtils;
import com.woting.content.manage.media.service.MediaContentService;
import com.woting.passport.mobile.MobileParam;
import com.woting.passport.mobile.MobileUDKey;
import com.woting.passport.session.SessionService;

@Controller
public class MediaContentController {
    @Resource(name="redisSessionService")
    private SessionService sessionService;
	@Resource
	private MediaContentService mediaContentService;

	@RequestMapping(value = "/content/getContents.do")
	@ResponseBody
	public Map<String, Object> getContents(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Map<String, Object> m = RequestUtils.getDataFromRequest(request);
			String userId, channelId, beginCatalogId, pagestr;
			int perSize = 3, pageSize = 10, page = 1;
			if (m == null || m.size() == 0) {
				userId = null;
				channelId = null;
				beginCatalogId = null;
				pagestr = null;
			} else {
				userId = m.get("UserId") + "";
				if (userId.equals("null"))
					userId = null;
				channelId = m.get("ChannelId") + "";
				if (channelId.equals("null")) {
					channelId = null;
				}
				perSize = 3;
				String perSizestr = m.get("PerSize") + "";
				if (!perSizestr.equals("null"))
					perSize = Integer.parseInt(perSizestr);
				pageSize = 10;
				String pageSizestr = m.get("PageSize") + "";
				if (!pageSizestr.equals("null"))
					pageSize = Integer.parseInt(pageSizestr);
				page = 1;
				pagestr = m.get("Page") + "";
				if (!pagestr.equals("null"))
					page = Integer.parseInt(pagestr);
				beginCatalogId = m.get("BeginCatalogId") + "";
				if (beginCatalogId.equals("null"))
					beginCatalogId = null;
			}

			List<Map<String, Object>> contents = mediaContentService.getContents(userId, channelId, perSize, page,
					pageSize, beginCatalogId);
			if (contents != null && contents.size() > 0) {
				map.put("ResultInfo", contents);
				map.put("AllCount", contents.size());
				map.put("ReturnType", "1001");
			} else {
				map.put("ReturnType", "1011");
				map.put("Message", "查询无内容");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("ReturnType", "T");
			map.put("TClass", e.getClass().getName());
			map.put("Message", e.getMessage());
			return map;
		}
	}

	@RequestMapping(value = "/content/getContentInfo.do")
	@ResponseBody
	public Map<String, Object> getContentInfo(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Map<String, Object> m = RequestUtils.getDataFromRequest(request);
			if (m == null || m.size() == 0) {
				map.put("ReturnType", "0000");
				map.put("Message", "无法获取相关的参数");
				return map;
			}
			String userId = m.get("UserId") + "";
			String contentId = m.get("ContentId") + "";
			if (contentId.equals("null")) {
				map.put("ReturnType", "1011");
				map.put("Message", "无法获取内容Id");
				return map;
			}
			Map<String, Object> contents = mediaContentService.getContentInfo(userId, contentId);
			if (contents != null) {
				map.put("ResultInfo", contents);
				map.put("ReturnType", "1001");
			} else {
				map.put("ReturnType", "1012");
				map.put("Message", "查询无内容");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("ReturnType", "T");
			map.put("TClass", e.getClass().getName());
			map.put("Message", e.getMessage());
			return map;
		}
	}

	@RequestMapping(value = "/content/getNoPubList.do")
	@ResponseBody
	public Map<String, Object> getNoPubList(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Map<String, Object> m = RequestUtils.getDataFromRequest(request);
			if (m == null || m.size() == 0) {
				map.put("ReturnType", "0000");
				map.put("Message", "无法获取相关的参数");
				return map;
			}
			String channelId = m.get("ChannelId") + "";
			if (channelId.equals("null")) {
				map.put("ReturnType", "1011");
				map.put("Message", "无法获取内容Id");
				return map;
			}
			List<Map<String, Object>> contents = mediaContentService.getNoPubContentList(channelId);
			if (contents != null) {
				map.put("ResultInfo", contents);
				map.put("ReturnType", "1001");
				map.put("AllCount", contents.size());
			} else {
				map.put("ReturnType", "1012");
				map.put("Message", "查询无内容");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("ReturnType", "T");
			map.put("TClass", e.getClass().getName());
			map.put("Message", e.getMessage());
			return map;
		}
	}

	@RequestMapping(value = "/playSumCount.do")
	@ResponseBody
	public Map<String, Object> getPlaySumCount(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
            String userId="";
		    MobileUDKey mUdk=null;
			Map<String, Object> m = RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "clickFavorite");
                if ((retM.get("ReturnType")+"").equals("2001")) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                } else {
                    //处理过客
                    if ((retM.get("ReturnType")+"").equals("2003")||(retM.get("ReturnType")+"").equals("2002")) {
                        mUdk.setUserId("0");
                    }
                    map.putAll(mUdk.toHashMapAsBean());
                    userId=mUdk.getUserId();
                    //注意这里可以写日志了
                }
                if (map.get("ReturnType")==null&&StringUtils.isNullOrEmptyOrSpace(userId)) {
                    map.put("ReturnType", "1002");
                    map.put("Message", "无法获取用户Id");
                }
            }
            if (map.get("ReturnType")!=null) return map;

			List<Map<String, Object>> l = mediaContentService.getPlaySumList(userId.equals("0")?null:userId);
			if (l != null) {
				map.put("ResultInfo", l);
				map.put("AllCount", l.size());
				map.put("ReturnType", "1001");
			} else {
				map.put("ReturnType", "1011");
				map.put("Message", "查询无内容");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("ReturnType", "T");
			map.put("TClass", e.getClass().getName());
			map.put("Message", e.getMessage());
			return map;
		}
	}

	@RequestMapping(value = "/directContent.do")
	@ResponseBody
	public Map<String, Object> getDirectContent(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Map<String, Object> m = RequestUtils.getDataFromRequest(request);
			String channelId, userId;
			if (m == null || m.size() == 0) {
				channelId = null;
				userId = null;
			} else {
				channelId = m.get("ChannelId") + "";
				if (channelId.equals("null"))
					channelId = null;
				userId = m.get("UserId") + "";
				if (userId.equals("null"))
					userId = null;
			}
			List<Map<String, Object>> contents = mediaContentService.getDirectContentList(userId, channelId, "1");
			if (contents != null) {
				map.put("ResultInfo", contents);
				map.put("AllCount", contents.size());
				map.put("ChannelId", channelId);
				map.put("ReturnType", "1001");
			} else {
				map.put("ReturnType", "1011");
				map.put("Message", "查询无内容");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("ReturnType", "T");
			map.put("TClass", e.getClass().getName());
			map.put("Message", e.getMessage());
			return map;
		}
	}
	
	@RequestMapping(value = "/content/removeContent.do")
	@ResponseBody
	public Map<String, Object> removeContent(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Map<String, Object> m = RequestUtils.getDataFromRequest(request);
			if (m == null || m.size() == 0) {
				map.put("ReturnType", "0000");
				map.put("Message", "无法获取相关的参数");
				return map;
			}
			String channelId = m.get("ChannelId")+"";
			if (channelId.equals("null")) {
				map.put("ReturnType", "1012");
				map.put("Message", "栏目Id为空");
				return map;
			}
			String contentId = m.get("ContentId")+"";
			if (contentId.equals("null")) {
				map.put("ReturnType", "1013");
				map.put("Message", "内容Id为空");
				return map;
			}
			map = mediaContentService.removeContent(channelId, contentId);
			if (map != null) {
				return map;
			} else {
				map = new HashMap<>();
				map.put("ReturnType", "1014");
				map.put("Message", "删除失败");
				return map;
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("ReturnType", "T");
			map.put("TClass", e.getClass().getName());
			map.put("Message", e.getMessage());
			return map;
		}
	}

	@RequestMapping(value = "/content/getPlayerContents.do")
	@ResponseBody
	public Map<String, Object> getPlayerContents(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Map<String, Object> m = RequestUtils.getDataFromRequest(request);
			if (m == null || m.size() == 0) {
				map.put("ReturnType", "0000");
				map.put("Message", "无法获取相关的参数");
				return map;
			}
			String contentId = m.get("ContentId") + "";
			if (contentId.equals("null")) {
				map.put("ReturnType", "1011");
				map.put("Message", "无法获取内容Id");
				return map;
			}
			Map<String, Object> contents = mediaContentService.getPlayerContents(contentId);
			if (contents != null) {
				map.put("AllCount", contents.get("AllCount"));
				contents.remove("AllCount");
				map.put("ResultList", contents);
				map.put("ReturnType", "1001");
			} else {
				map.put("ReturnType", "1011");
				map.put("Message", "查询无内容");
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			map.put("ReturnType", "T");
			map.put("TClass", e.getClass().getName());
			map.put("Message", e.getMessage());
			return map;
		}
	}
}
