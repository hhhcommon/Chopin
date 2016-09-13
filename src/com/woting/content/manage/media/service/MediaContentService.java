package com.woting.content.manage.media.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.WtContentMngConstants;
import com.woting.cm.core.channel.mem._CacheChannel;
import com.woting.cm.core.channel.model.Channel;
import com.woting.cm.core.channel.model.ChannelAsset;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.model.MaSource;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.model.SeqMediaAsset;
import com.woting.cm.core.media.persis.po.SeqMaRefPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.content.manage.dict.service.DictContentService;
import com.woting.content.manage.seq.service.SeqContentService;

@Service
public class MediaContentService {
	@Resource
	private MediaService mediaService;
	@Resource
	private DictContentService dictContentService;
	@Resource
	private ChannelService channelService;
	@Resource
	private SeqContentService seqContentService;
	private _CacheChannel _cc=null;
	
	@PostConstruct
    public void initParam() {
        _cc=(SystemCache.getCache(WtContentMngConstants.CACHE_CHANNEL)==null?null:((CacheEle<_CacheChannel>)SystemCache.getCache(WtContentMngConstants.CACHE_CHANNEL)).getContent());
    }
	
	/**
	 * 查询主播的资源列表
	 * 
	 * @param userid
	 * @param mediatype
	 * @return
	 */
	public Map<String, Object> getHostMediaContents(String userid) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list = mediaService.getMaInfoByMaPubId(userid);
		if (list != null && list.size() > 0) {
			map.put("List", list);
			map.put("AllCount", list.size());
			map.put("ReturnType", "1001");
		}
		return map;
	}
	
	/**
	 * 上传单体节目
	 * 
	 * @param upfiles
	 * @param uploadmap
	 * @return
	 */
	public Map<String, Object> addMediaInfo(String userid, String username, String maname, String maimg, String maurl, String mastatus,
			String keywords, String madesc, String seqid) {
		Map<String, Object> map = new HashMap<String, Object>();
		String maid = SequenceUUID.getPureUUID();
		Timestamp ctime = new Timestamp(System.currentTimeMillis()); // 节目创建时间

		MediaAsset ma = new MediaAsset();
		ma.setId(maid);
		ma.setMaTitle(maname);
		ma.setMaImg(maimg);
		ma.setMaURL(maurl);
		ma.setKeyWords("上传文件测试用待删除");
		ma.setMaPubType(3);
		ma.setMaPubId(userid);
		ma.setMaPublisher(username);
		ma.setDescn(madesc.toLowerCase().equals("null") ? "这家伙真懒，什么都没留下" : madesc);
		ma.setPubCount(0);
		ma.setMaStatus(Integer.valueOf(mastatus));
		ma.setCTime(ctime);

		// 保存单体资源
		mediaService.saveMa(ma);

		// 保存专辑与单体媒体对应表
		if (!seqid.toLowerCase().equals("null")) {
			SeqMediaAsset sma = mediaService.getSmaInfoById(seqid);
			mediaService.bindMa2Sma(ma, sma);
			List<Map<String, Object>> catalist = mediaService.getResDictRefByResId("'"+seqid+"'", "wt_SeqMediaAsset");
			if(catalist!=null&&catalist.size()>0)
				for (Map<String, Object> map2 : catalist) {
					dictContentService.addCataLogs("3", map2.get("dictDid")+"", "wt_MediaAsset", maid);
				}
		}

		// 保存资源来源表里
		MaSource maSource = new MaSource();
		maSource.setMa(ma);
		maSource.setId(SequenceUUID.getPureUUID());
		maSource.setMaSrcType(3);
		maSource.setMaSrcId(userid);
		maSource.setMaSource(username);
		maSource.setSmType(1);
		maSource.setPlayURI(maurl);
		maSource.setIsMain(1);
		maSource.setDescn("上传文件测试用待删除");
		maSource.setCTime(ctime);
		mediaService.saveMas(maSource);
		
		// 获取专辑分类
		ChannelAsset chasma = mediaService.getCHAInfoByAssetId(seqid);
		if(chasma!=null) 
			modifyMediaStatus(userid, maid, seqid, chasma.getFlowFlag());
		
		if (mediaService.getMaInfoById(maid) != null) {
			map.put("ReturnType", "1001");
			map.put("Message", "上传文件成功");
		} else {
			map.put("ReturnType", "1011");
			map.put("Message", "上传失败");
		}
		return map;
	}
	
	/**
	 * 修改单体信息
	 * 
	 * @param ma
	 * @param sma
	 * @return
	 */
	public Map<String, Object> updateMediaInfo(MediaAsset ma, SeqMediaAsset sma) {
		Map<String, Object> map = new HashMap<String,Object>();
		if(mediaService.getMaInfoById(ma.getId())!=null){
			mediaService.updateMa(ma);
		    if (sma!=null) {
		    	if (mediaService.getSeqMaRefByMId(ma.getId())!=null) {
					SeqMaRefPo seqmapo = new SeqMaRefPo();
			        seqmapo.setMId(ma.getId());
			        seqmapo.setSId(sma.getId());
			        seqmapo.setCTime(new Timestamp(System.currentTimeMillis()));
			        mediaService.updateSeqMaRef(seqmapo); // 待修改wt_Masource,wt_ResDict_Ref,wt_ChannelAsset
				}else{
					sma = mediaService.getSmaInfoById(sma.getId());
			        mediaService.bindMa2Sma(ma, sma);
				}
		    }
		    map.put("ReturnType", "1001");
		    map.put("Message", "修改成功");
		}else {
			map.put("ReturnType", "1011");
		    map.put("Message", "修改失败");
		}
		return map;
	}
	
	public Map<String, Object> modifyMediaStatus(String userid, String maid, String smaid, int flowflag) {
		Map<String, Object> map = new HashMap<String, Object>();
		MediaAsset ma = mediaService.getMaInfoById(maid);
		if (ma == null) {
			map.put("ReturnType", "1011");
			map.put("Message", "节目不存在");
			return map;
		}
		ChannelAsset chasma = mediaService.getCHAInfoByAssetId(smaid);
		if(chasma==null) {
			map.put("ReturnType", "1011");
			map.put("Message", "未查询到专辑发布信息");
			return map;
		}
		Channel ch = chasma.getCh();
		if (ch == null) {
			map.put("ReturnType", "1011");
			map.put("Message", "栏目不存在");
			return map;
		}
		ChannelAsset cha = mediaService.getCHAInfoByAssetId(maid);
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
		    cha.setPubObj(ma);
		    cha.setPublisherId(userid);
		    cha.setCheckerId("1");
		    cha.setFlowFlag(flowflag);
		    cha.setSort(0);
		    cha.setPubImg(ma.getMaImg());
		    cha.setCheckRuleIds("0");
		    cha.setCTime(new Timestamp(System.currentTimeMillis()));
		    if(flowflag==2) {
		    	cha.setPubTime(new Timestamp(System.currentTimeMillis()));
		    }
		    cha.setIsValidate(1);
		    cha.setInRuleIds("elt");
		    cha.setCheckRuleIds("elt");
		    mediaService.saveCha(cha);
		}
		if (flowflag==2&&chasma.getFlowFlag()==0) {
			chasma.setFlowFlag(flowflag);
			mediaService.updateCha(chasma);
		}
		if (mediaService.getCHAInfoById(cha.getId()) != null) {
			map.put("ReturnType", "1001");
			map.put("Message", "节目发布成功");
			MediaAsset ma2 = new MediaAsset();
			ma2.setId(maid);
			ma2.setPubCount(ma.getPubCount() + 1);
			mediaService.updateMa(ma2);
		} else {
			map.put("ReturnType", "1011");
			map.put("Message", "节目发布失败");
		}
		return map;
	}
	
	public Map<String, Object> removeMediaAsset(String contentid) {
		Map<String, Object> map = new HashMap<String,Object>();
		mediaService.removeMedia(contentid);
		if(mediaService.getMaInfoById(contentid)!=null){
			map.put("ReturnType", "1011");
			map.put("Message", "单体删除失败");
		}else{
			map.put("ReturnType", "1001");
			map.put("Message", "单体删除成功");
		}
		return map;
	}
	
	public Map<String, Object> getContents(String channelId, int perSize, int page, int pageSize) {
		ChannelPo chPo = channelService.getChannelById(channelId);
		if(chPo!=null) {
			List<ChannelPo> chs = channelService.getChannelsByPcId(chPo.getId());
			if(chs==null || chs.size()==0) {
				
			}
		}
		return null;
	}
}
