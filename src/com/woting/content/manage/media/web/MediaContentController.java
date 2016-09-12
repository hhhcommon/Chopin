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
	private static String ip_address = "182.92.175.134";

	/**
	 * 得到主播单体节目列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/media/getHostMediaList.do")
	@ResponseBody
	public Map<String, Object> getMediaList(HttpServletRequest request){
		Map<String, Object> map = new HashMap<String,Object>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String userid = m.get("UserId")+"";
		if(StringUtils.isNullOrEmptyOrSpace(userid)||userid.toLowerCase().equals("null")){
			map.put("ReturnType", "1011");
			map.put("Message", "无用户信息");
			return map;
		}
		Map<String, Object> c = mediaContentService.getHostMediaContents(userid);
		if(c!=null&&c.size()>0){
			map.put("ReturnType", c.get("ReturnType"));
			c.remove("ReturnType");
			map.put("ResultList", c);
		}else{
			map.put("ReturnType", "1011");
            map.put("Message", "没有查到任何内容");
		}
		return map;
	}
	
	/**
	 * 创建单体节目
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/media/addMediaInfo.do")
	@ResponseBody
	public Map<String, Object> addMediaInfo(HttpServletRequest request){
		Map<String, Object> map = new HashMap<String,Object>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String userid = m.get("UserId")+"";
		String username = m.get("UserName")+"";
		if(StringUtils.isNullOrEmptyOrSpace(userid)||userid.toLowerCase().equals("null")){
			map.put("ReturnType", "1011");
			map.put("Message", "无用户信息");
			return map;
		}
		String maname = m.get("ContentName")+"";
		if(StringUtils.isNullOrEmptyOrSpace(maname)||maname.toLowerCase().equals("null")){
			map.put("ReturnType", "1011");
			map.put("Message", "无节目名称");
			return map;
		}
		String mastatus = m.get("ContentStatus")+"";
		if(mastatus.toLowerCase().equals("null")){
			map.put("ReturnType", "1011");
			map.put("Message", "无资源状态");
			return map;
		}
		String rootpath = SystemCache.getCache(FConstants.APPOSPATH).getContent()+"";
		String maimg = m.get("ContentImg")+"";
		if(maimg.toLowerCase().equals("null"))
			maimg="htpp://www.wotingfm.com:908/CM/mweb/templet/zj_templet/imgs/default.png";
		maimg=maimg.replace(rootpath, "http://"+ip_address+":908/CM/");
		String mauri = m.get("ContentURI")+"";
		mauri=mauri.replace(rootpath, "http://"+ip_address+":908/CM/");
		String madescn = m.get("ContentDesc")+"";
		String contentkeywords = m.get("KeyWords")+"";
		String seqid = m.get("ContentSeqId")+"";
		map = mediaContentService.addMediaInfo(userid, username, maname, maimg, mauri, mastatus, contentkeywords, madescn, seqid);
		return map;
	}
	
	/**
	 * 修改单体节目信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/media/updateMediaInfo.do")
	@ResponseBody
	public Map<String, Object> updateMediaInfo(HttpServletRequest request){
		MediaAsset ma = new MediaAsset();
		SeqMediaAsset sma = new SeqMediaAsset();
		Map<String, Object> map = new HashMap<String,Object>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String maid = m.get("ContentId")+"";
		ma.setId(maid);
		String maname = m.get("ContentName")+"";
		if(!maname.toLowerCase().equals("null")) ma.setMaTitle(maname);
		String rootpath = SystemCache.getCache(FConstants.APPOSPATH).getContent()+"";
		String maimg = m.get("ContentImg")+"";
		if(maimg.equals("null"))
			maimg = "htpp://www.wotingfm.com:908/CM/mweb/templet/zj_templet/imgs/default.png";
		maimg=maimg.replace(rootpath, "http://"+ip_address+":908/CM/");
		if(!maimg.toLowerCase().equals("null")) ma.setMaImg(maimg);
		String mauri = m.get("ContentURI")+"";
		if(!mauri.toLowerCase().equals("null")) {
			mauri=mauri.replace(rootpath, "http://"+ip_address+":908/CM/");
			ma.setMaURL(mauri);
		} 
		String seqid = m.get("ContentSeqId")+"";
		if(!seqid.toLowerCase().equals("null")) sma.setId(seqid);
		String madesc = m.get("ContentDesc")+"";
		if(!madesc.toLowerCase().equals("null")) ma.setDescn(madesc);
		String mastatus = m.get("ContentStatus")+"";
		if(!mastatus.toLowerCase().equals("null")) ma.setMaStatus(Integer.valueOf(mastatus));
		if(seqid.toLowerCase().equals("null")) map = mediaContentService.updateMediaInfo(ma,null);
		else map = mediaContentService.updateMediaInfo(ma, sma);
		return map;
	}
	
	/**
	 * 发布单体节目(发布单体节目时，必须要绑定在专辑的下面)
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/content/media/updateMediaStatus.do")
	@ResponseBody
	public Map<String, Object> updateMediaStatus(HttpServletRequest request){
		Map<String, Object> map = new HashMap<String,Object>();
		Map<String, Object> m = RequestUtils.getDataFromRequest(request);
		String userid = m.get("UserId")+"";
		if(userid.toLowerCase().equals("null")){
			map.put("ReturnType", "1011");
			map.put("Message", "无用户信息");
			return map;
		}
		String maid = m.get("ContentId")+"";
		if(maid.toLowerCase().equals("null")){
			map.put("ReturnType", "1011");
			map.put("Message", "无节目id信息");
			return map;
		}
		String smaid = m.get("ContentSeqId")+"";
		if (smaid.toLowerCase().equals("null")) {
			map.put("ReturnType", "1011");
			map.put("Message", "无专辑id信息");
			return map;
		}
		map = mediaContentService.modifyMediaStatus(userid, maid, smaid, 2);
		return map;
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
