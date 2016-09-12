package com.woting.content.manage.seq.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.channel.model.Channel;
import com.woting.cm.core.channel.model.ChannelAsset;
import com.woting.cm.core.dict.model.DictDetail;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.model.SeqMediaAsset;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMaRefPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.content.manage.dict.service.DictContentService;
import com.woting.content.manage.media.service.MediaContentService;

@Service
public class SeqContentService {
	@Resource
	private MediaService mediaService;
	@Resource
	private DictContentService dictContentService;
	@Resource
	private MediaContentService mediaContentService;

	/**
	 * 查询主播的资源列表
	 * 
	 * @param userid
	 * @param mediatype
	 * @return
	 */
	public Map<String, Object> getHostSeqMediaContents(String userid) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list = mediaService.getSmaInfoBySmaPubId(userid);
		if (list != null && list.size() > 0) {
			map.put("List", list);
			map.put("AllCount", list.size());
			map.put("ReturnType", "1001");
		}
		return map;
	}
	
	/**
	 * 创建专辑
	 * 
	 * @param upfile
	 * @param m
	 * @return
	 */
	public Map<String, Object> addSeqInfo(String userid, String username, String smaname, String smaimg, String smastatus,
			String did, String chid, String smadesc, List<String> tagslist) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 保存专辑信息到资源库
		String smaid = SequenceUUID.getPureUUID();
		SeqMediaAsset sma = new SeqMediaAsset();
		sma.setId(smaid);
		String smatitle = smaname;
		if (StringUtils.isNullOrEmptyOrSpace(smatitle) || smatitle.toLowerCase().equals("null")) {
			map.put("ReturnType", "1011");
			map.put("Message", "无专辑名称");
			return map;
		}
		sma.setSmaTitle(smatitle);
		if (smaimg.toLowerCase().equals("null"))
			smaimg = "http://www.wotingfm.com:908/CM/mweb/templet/zj_templet/imgs/default.png";
		sma.setSmaImg(smaimg);
		sma.setDescn(smadesc.toLowerCase().equals("null") ? "这家伙真懒，什么都没留下" : smadesc);
		sma.setSmaStatus(Integer.valueOf(smastatus));
		sma.setCTime(new Timestamp(System.currentTimeMillis()));
		sma.setSmaPubType(3);
		sma.setSmaPubId(userid);
		sma.setSmaPublisher(username);
		DictDetail detail = new DictDetail();
		detail.setId("zho");
		detail.setNodeName("中文");
		sma.setLang(detail);
		sma.setPubCount(0);
		mediaService.saveSma(sma);
		if(!did.toLowerCase().equals("null"))
			dictContentService.addCataLogs("3", did, "wt_SeqMediaAsset", smaid);
		if(!chid.equals("null"))
			map = modifySeqStatus(userid, smaid, chid, 0);
		if (mediaService.getSmaInfoById(smaid) != null) {
			if(chid.equals("null")||map.get("ReturnType").equals("1001"))
				map.clear();
			map.put("ReturnType", "1001");
			map.put("Message", "添加专辑成功");
		} else {
			map.clear();
			map.put("ReturnType", "1011");
			map.put("Message", "添加专辑失败");
		}
		return map;
	}
	
	/**
	 * 修改专辑信息
	 * 
	 * @param ma
	 * @param sma
	 * @return
	 */
	public Map<String, Object> updateSeqInfo(String userid, SeqMediaAsset sma, String did, String chid) {
		Map<String, Object> map = new HashMap<String,Object>();
		List<MediaAssetPo> malist = mediaService.getMaListBySmaId(sma.getId());
		if(mediaService.getSmaInfoById(sma.getId())!=null) {
			mediaService.updateSma(sma);
			if(malist!=null&&malist.size()>0){
				for (MediaAssetPo mapo : malist) {
					MediaAsset ma = new MediaAsset();
					ma.buildFromPo(mapo);
					mediaService.bindMa2Sma(ma, sma);
				}
			}
			List<SeqMaRefPo> l = mediaService.getSmaListBySid(sma.getId());
			if(!did.toLowerCase().equals("null")){
				mediaService.removeResDictRef(sma.getId());
				dictContentService.addCataLogs("3", did, "wt_SeqMediaAsset", sma.getId());
				if(l!=null&&l.size()>0)
				    for (SeqMaRefPo seqMaRefPo : l) { // 查询专辑下级信息，删除下级单体的内容分类数据信息，重新写入wt_ResDict_Ref表内
					    mediaService.removeResDictRef(seqMaRefPo.getMId());
					    dictContentService.addCataLogs("3", did, "wt_MediaAsset", seqMaRefPo.getMId());
				    }
			}
			if(!chid.toLowerCase().equals("null")){
				ChannelAsset cha = mediaService.getCHAInfoByAssetId(sma.getId());
				if(cha!=null){
					int flowflag = cha.getFlowFlag();
					mediaService.removeCha(sma.getId());
					modifySeqStatus(userid, sma.getId(), chid, flowflag);
				}
			}
			map.put("ReturnType", "1001");
		    map.put("Message", "修改成功");
		}else{
			map.put("ReturnType", "1011");
		    map.put("Message", "修改失败");
		}
		return map;
	}
	
	public Map<String, Object> modifySeqStatus(String userid, String smaid, String chid, int flowflag) {
		Map<String, Object> map = new HashMap<String, Object>();
		SeqMediaAsset sma = mediaService.getSmaInfoById(smaid);
		if (sma == null) {
			map.put("ReturnType", "1011");
			map.put("Message", "专辑不存在");
			return map;
		}
		Channel ch = mediaService.getChInfoById(chid);
		if (ch == null) {
			map.put("ReturnType", "1011");
			map.put("Message", "栏目不存在");
			return map;
		}
		List<MediaAssetPo> malist = mediaService.getMaListBySmaId(smaid);
		if(flowflag!=0 && (malist==null||malist.size()==0)) {
			map.put("ReturnType", "1011");
			map.put("Message", "专辑无下级单体");
			return map;
		}
		ChannelAsset cha = mediaService.getCHAInfoByAssetId(smaid);
		if(cha!=null){
			cha.setFlowFlag(flowflag);
			cha.setCh(ch);
			if(flowflag==2)
				cha.setPubTime(new Timestamp(System.currentTimeMillis()));
			mediaService.updateCha(cha);
		}else{
			cha = new ChannelAsset();
		    String chaid = SequenceUUID.getPureUUID();
		    cha.setId(chaid);
		    cha.setCh(ch);
		    cha.setPubObj(sma);
		    cha.setPublisherId(userid);
		    cha.setCheckerId("1");
		    cha.setFlowFlag(flowflag);
		    cha.setSort(0);
		    cha.setCheckRuleIds("0");
		    cha.setCTime(new Timestamp(System.currentTimeMillis()));
		    if(flowflag==2) {
		    	cha.setPubTime(new Timestamp(System.currentTimeMillis()));
		    }
		    cha.setIsValidate(1);
		    cha.setInRuleIds("elt");
		    cha.setCheckRuleIds("elt");
		    //发布专辑
		    mediaService.saveCha(cha);
		}
		
		//发布专辑下级节目
		for (MediaAssetPo mediaAssetPo : malist) {
			mediaContentService.modifyMediaStatus(userid, mediaAssetPo.getId(), smaid, flowflag);
		}
		
		if (mediaService.getCHAInfoById(cha.getId()) != null) {
			map.put("ReturnType", "1001");
			map.put("Message", "专辑发布成功");
			SeqMediaAsset sma2 = new SeqMediaAsset();
			sma2.setId(smaid);
			sma2.setPubCount(sma.getPubCount() + 1);
			mediaService.updateSma(sma2);
		} else {
			map.put("ReturnType", "1011");
			map.put("Message", "专辑发布失败");
		}
		return map;
	}
	
	
	public Map<String, Object> removeSeqMediaAsset(String contentid) {
		Map<String, Object> map = new HashMap<String,Object>();
		mediaService.removeSeqMedia(contentid);
		if(mediaService.getSmaInfoById(contentid)!=null){
			map.put("ReturnType", "1011");
			map.put("Message", "专辑删除失败");
		}else{
			map.put("ReturnType", "1001");
			map.put("Message", "专辑删除成功");
		}
		return map;
	}
}
