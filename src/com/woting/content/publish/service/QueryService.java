package com.woting.content.publish.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.channel.model.ChannelAsset;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.passport.UGA.persistence.pojo.UserPo;
import com.woting.passport.UGA.service.UserService;


@Service
public class QueryService {
	@Resource(name="dataSource")
	private DataSource DataSource;
	@Resource
	private ChannelService channelService;
	@Resource
	private MediaService mediaService;
	@Resource
	private UserService userService;

	public Map<String, Object> addContentByApp(String userId, String title, String filePath,String descn , String channelId) {
		Map<String, Object> map = new HashMap<>();
		if(mediaService.getMaInfoByTitle(title)==null) {
			map.put("ReturnType", "1006");
			map.put("Message", "内容重名");
			return map;
		}
		UserPo userPo = userService.getUserById(userId);
		MediaAssetPo mapo = new MediaAssetPo();
		mapo.setId(SequenceUUID.getPureUUID());
		mapo.setMaPubType(3);
		mapo.setMaPubId(userId);
		mapo.setMaPublisher(userPo.getUserName());
		
		mapo.setDescn(descn);
		mapo.setMaStatus(2);
		mapo.setCTime(new Timestamp(System.currentTimeMillis()));
		mapo.setMaPublishTime(new Timestamp(System.currentTimeMillis()));
		mapo.setPubCount(1);
		MediaAsset mat = new MediaAsset();
		mat.buildFromPo(mapo);
		if (!mediaService.saveMa(mat)) {
			map.put("ReturnType", "1007");
			map.put("Message", "内容添加失败");
			return map;
		}
		
		ChannelAssetPo chas = new ChannelAssetPo();
		chas.setId(SequenceUUID.getPureUUID());
		chas.setChannelId(channelId);
		chas.setAssetId(mapo.getId());
		chas.setAssetType("wt_Mediasset");
		chas.setPublisherId(userId);
		chas.setPubName(title);
		if (mapo.getMaImg()!=null) 
			chas.setPubImg(mapo.getMaImg());
		chas.setIsValidate(1);
		chas.setCheckerId("1");
		chas.setSort(0);
		chas.setFlowFlag(2);
		chas.setInRuleIds("0");
		chas.setCheckRuleIds("0");
		chas.setCTime(new Timestamp(System.currentTimeMillis()));
		chas.setPubTime(new Timestamp(System.currentTimeMillis()));
		if(!channelService.insertChannelAsset(chas)) {
			mediaService.removeMa(mapo.getId());
			map.put("ReturnType", "1007");
			map.put("Message", "内容添加失败");
			return map;
		}
		return null;
	}
	
	public List<Map<String, Object>> getContentListByApp(String userId) {
		return mediaService.getMaInfoByMaPubId(userId);
	}
	
	public void removeContentByApp(String userId, String contentId) {
		mediaService.removeMa(contentId);
		channelService.removeChannelAsset(contentId);
	}
	
	//只用于发布已撤销的内容
	public boolean addPubContentInfo(String channelId, String contentId) {
		ChannelAsset cha = mediaService.getChannelAssetByChannelIdAndAssetId(channelId, contentId);
		if(cha!=null) {
			cha.setIsValidate(1);
			mediaService.updateCha(cha);
			return true;
		}
		return false;
	}
	
	public boolean modifyPubSortInfo(String channelId, int contentSort, String contentId) {
		ChannelAsset cha = mediaService.getChannelAssetByChannelIdAndAssetId(channelId, contentId);
		if(cha!=null) {
			cha.setSort(contentSort);
			mediaService.updateCha(cha);
			return true;
		}
		return false;
	}
	
	public boolean removePubInfo(String channelId, String contentId) {
		ChannelAsset cha = mediaService.getChannelAssetByChannelIdAndAssetId(channelId, contentId);
		if(cha!=null) {
			cha.setIsValidate(2);
			mediaService.updateCha(cha);
			return true;
		}
		return false;
	}
	
	public boolean makeContentHtml(String channelId, List<Map<String, Object>> list){
		MediaAssetPo ma = new MediaAssetPo();
		if (list!=null && list.size()>0) {
			for (Map<String, Object> m : list) {
				
			}
			System.out.println(JsonUtils.objToJson(list));
			return true;
		}
		return false;
	}
	
	public boolean modifyDirectContentInfo(String channelId, String contentId, int status){
		ChannelAsset cha = mediaService.getChannelAssetByChannelIdAndAssetId(channelId, contentId);
		if(cha!=null) {
			cha.setFlowFlag(status);
			mediaService.updateCha(cha);
			return true;
		}
		return false;
	}
	
	
}
