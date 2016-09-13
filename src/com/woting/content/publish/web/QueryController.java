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
