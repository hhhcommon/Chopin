package com.woting.content.publish.service;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.content.manage.channel.service.ChannelContentService;


@Service
public class QueryService {
	@Resource(name="dataSource")
	private DataSource DataSource;
	@Resource
	private MediaService mediaService;
	@Resource
	private ChannelContentService chaService;

	public Map<String, Object> addContentByApp(String userId, String title, String filePath,String descn , String channelId) {
		Map<String, Object> map = new HashMap<>();
		if(mediaService.getMaInfoByTitle(title)==null) {
			map.put("ReturnType", "1006");
			map.put("Message", "内容重名");
			return map;
		}
		MediaAssetPo mapo = new MediaAssetPo();
		mapo.setId(SequenceUUID.getPureUUID());
		mapo.setMaPubId(userId);
		
		
		return null;
	}
}
