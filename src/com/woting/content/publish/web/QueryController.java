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
public class QueryController {
	@Resource
	private QueryService queryService;

	/**
	 * 查询列表信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/getContents.do")
	@ResponseBody
	public Map<String, Object> getContents(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> m = RequestDataUtils.getDataFromRequest(request);
		String catalogsid = null;
		int flowFlag = 0;
		String source = null;
		Timestamp begincontentpubtime = null;
		Timestamp endcontentpubtime = null;
		Timestamp begincontentctime = null;
		Timestamp endcontentctime = null;
//		String userId = m.get("UserId")+"";
		int page = m.get("Page") == null ? -1 : Integer.valueOf((String) m.get("Page"));
		int pagesize = m.get("PageSize") == null ? -1 : Integer.valueOf((String) m.get("PageSize"));
		if (m.containsKey("CatalogsId"))
			catalogsid = (String) m.get("CatalogsId");
		if (m.containsKey("ContentFlowFlag"))
			flowFlag = m.get("ContentFlowFlag") == null ? -1 : Integer.valueOf((String) m.get("ContentFlowFlag"));
		if (m.containsKey("SourceId"))
			source = (String) m.get("SourceId");
		if (m.containsKey("BeginContentPubTime"))
			begincontentpubtime = Timestamp.valueOf(m.get("BeginContentPubTime")+"");
		if (m.containsKey("EndContentPubTime"))
			endcontentpubtime = Timestamp.valueOf(m.get("EndContentPubTime")+"");
		if (m.containsKey("BeginContentCTime"))
			begincontentctime = Timestamp.valueOf(m.get("BeginContentCTime")+"");
		if (m.containsKey("EndContentCTime"))
			endcontentctime = Timestamp.valueOf(m.get("EndContentCTime")+"");
		if (flowFlag > 0 && page > 0 && pagesize > 0) {
			Map<String, Object> maplist = queryService.getContent(flowFlag, page, pagesize, catalogsid, source,
					begincontentpubtime, endcontentpubtime, begincontentctime, endcontentctime);
			map.put("ResultList", maplist.get("List"));
			map.put("ReturnType", "1001");
			map.put("ContentCount", maplist.get("Count"));
		} else {
			map.put("ReturnType", "1002");
			map.put("Message", "请求信息有误");
		}
		return map;
	}

	/**
	 * 查询节目详细信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/getContentInfo.do")
	@ResponseBody
	public Map<String, Object> getContentInfo(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
//		String userId = (String) m.get("UserId");
		int pagesize = m.get("PageSize") == null ? -1 : Integer.valueOf((String) m.get("PageSize"));
		int page = m.get("Page") == null ? -1 : Integer.valueOf((String) m.get("Page"));
		String id = (String) m.get("ContentId");
		String mediatype = m.get("MediaType")+"";
		Map<String, Object> mapdetail = queryService.getContentInfo(pagesize, page, id, mediatype);
		if (mediatype.equals("wt_SeqMediaAsset")) {
			if (mapdetail.get("audio") != null) {
				map.put("ContentDetail", mapdetail.get("sequ"));
				map.put("SubList", mapdetail.get("audio"));
				map.put("ReturnType", "1001");
			} else {
				map.put("ReturnType", "1011");
				map.put("Message", "没有相关内容 ");
			}
		} else {
			if (mediatype.equals("wt_MediaAsset")) {
				if (map.isEmpty()) {
					map.put("ContentDetail", mapdetail);
					map.put("ReturnType", "1001");
				} else {
					map.put("ReturnType", "1011");
					map.put("Message", "没有相关内容 ");
				}
			} else {
				if (mediatype.equals("wt_Broadcast")) {
					map.put("SubList", mapdetail);
					map.put("ReturnType", "1001");
				} else {
					map.put("ReturnType", "1011");
					map.put("Message", "没有相关内容 ");
				}
			}
		}
		return map;
	}

	/**
	 * 修改序号和审核状态
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/updateContentStatus.do")
	@ResponseBody
	public Map<String, Object> updateContentStatus(HttpServletRequest request) {
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		int flowFlag = m.get("ContentFlowFlag") == null ? -1 : Integer.valueOf((String) m.get("ContentFlowFlag"));
//		String userId = (String) m.get("UserId");
		String ids = (String) m.get("Id");
		String numbers = (String) m.get("ContentSort");
		String opeType = (String) m.get("OpeType");
		System.out.println(flowFlag + "#" + ids + "#" + numbers + "#" + opeType);
		Map<String, Object> map = queryService.modifyInfo(ids, numbers, flowFlag, opeType);
		return map;
	}

	/**
	 * 查询栏目分类和发布组织信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/getConditions.do")
	@ResponseBody
	public Map<String, Object> getCatalogs(HttpServletRequest request) {
//		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
//		String userId = (String) m.get("UserId");
		Map<String, Object> map = new HashMap<String, Object>();
		map = queryService.getConditionsInfo();
		map.put("ReturnType", "1001");
		return map;
	}

	/**
	 * 发布所有已审核的节目 只用于测试用
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/getAll.do")
	@ResponseBody
	public Map<String, Object> getAll(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		System.out.println(m);
		int flowFlag = 0;
		// String userId = (String) m.get("UserId");
		int page = 0;
		int pagesize = 0;
		if (m.containsKey("ContentFlowFlag"))
			flowFlag = m.get("ContentFlowFlag") == null ? -1 : Integer.valueOf((String) m.get("ContentFlowFlag"));
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < 675; i++) { //目前测试i循环参数固定
			page = i;
			pagesize = 10;
			Map<String, Object> maplist = queryService.getContent(flowFlag, page, pagesize, null, null, null, null, null, null);
			List<Map<String, Object>> listsequs = (List<Map<String, Object>>) maplist.get("List");
			for (Map<String, Object> map2 : listsequs) {
				String sequid = (String) map2.get("ContentId");
				if (sb.indexOf(sequid) < 0) {
					Map<String, Object> m2 = queryService.getContentInfo(page, pagesize, sequid, "wt_SeqMediaAsset");
					if (m2.get("audio") != null) {
						map.put("ContentDetail", m2.get("sequ"));
						map.put("SubList", m2.get("audio"));
					}
					sb.append(sequid);
					CacheUtils.publishZJ(map);
				}
			}
		}
		return null;
	}

	/**
	 * 分享页的分页加载请求
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/getZJSubPage.do")
	@ResponseBody
	public Map<String, Object> getZJSubPage(HttpServletRequest request) {
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		System.out.println(m);
		String zjid = (String) m.get("ContentId");
		String page = (String) m.get("Page");
		Map<String, Object> map = new HashMap<String, Object>();
		map = queryService.getZJSubPage(zjid, page);
		return map;
	}
}
