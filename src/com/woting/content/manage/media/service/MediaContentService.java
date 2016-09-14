package com.woting.content.manage.media.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.woting.WtContentMngConstants;
import com.woting.cm.core.channel.mem._CacheChannel;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.cm.core.utils.ContentUtils;
import com.woting.content.manage.channel.service.ChannelContentService;

@Service
public class MediaContentService {
	@Resource
	private MediaService mediaService;
	@Resource
	private ChannelContentService channelContentService;
	@Resource
	private ChannelService channelService;
	private _CacheChannel _cc = null;

	@PostConstruct
	public void initParam() {
		_cc = (SystemCache.getCache(WtContentMngConstants.CACHE_CHANNEL) == null ? null : ((CacheEle<_CacheChannel>) SystemCache.getCache(WtContentMngConstants.CACHE_CHANNEL)).getContent());
	}

	public List<Map<String, Object>> getContents(String channelId, int perSize, int page, int pageSize,
			String beginCatalogId) {
		List<Map<String, Object>> l = new ArrayList<>();
		ChannelPo chPo = channelService.getChannelById(channelId);
		if (chPo != null) {
			List<ChannelPo> chs = channelService.getChannelsByPcId(chPo.getId());
			if (chs == null || chs.size() == 0) {
				List<ChannelAssetPo> chas = channelService.getChannelAssetsByChannelId(chPo.getId(), page, pageSize);
				if (chas != null && chas.size() > 0) {
					List<Map<String, Object>> chsm = channelContentService.getChannelAssetList(chas);
					String resids = "";
					for (ChannelAssetPo chapo : chas) {
						resids += ",'" + chapo.getAssetId() + "'";
					}
					resids = resids.substring(1);
					List<MediaAssetPo> mas = mediaService.getMaListByIds(resids);
					for (MediaAssetPo maPo : mas) {
						MediaAsset mediaAsset = new MediaAsset();
						mediaAsset.buildFromPo(maPo);
						Map<String, Object> mam = ContentUtils.convert2Ma(mediaAsset.toHashMap(), null, null, chsm, null);
						
						l.add(mam);
					}
				}
			} else {
				List<ChannelPo> cs = new ArrayList<>();
				for (ChannelPo cho : chs) {
					cs.add(cho);
					if (beginCatalogId.equals(cho.getId())) {
						chs.removeAll(cs);
						break;
					}
				}
				if (chs != null && chs.size() > 0) {
					for (ChannelPo cho : chs) {
						if (pageSize < 1)
							return l;
						List<ChannelAssetPo> chas = channelService.getChannelAssetsByChannelId(cho.getId(), page, perSize);
						pageSize = pageSize - perSize;
						if (chas != null && chas.size() > 0) {
							List<Map<String, Object>> ll = new ArrayList<>();
							List<Map<String, Object>> chasm = channelContentService.getChannelAssetList(chas);
							String resids = "";
							for (ChannelAssetPo chapo : chas) {
								resids += ",'" + chapo.getAssetId() + "'";
							}
							resids = resids.substring(1);
							List<MediaAssetPo> mas = mediaService.getMaListByIds(resids);
							for (MediaAssetPo maPo : mas) {
								MediaAsset mediaAsset = new MediaAsset();
								mediaAsset.buildFromPo(maPo);
								Map<String, Object> mam = ContentUtils.convert2Ma(mediaAsset.toHashMap(), null, null, chasm, null);
								
								ll.add(mam);
							}
							if (ll.size() > 0) {
								Map<String, Object> m = new HashMap<>();
								m.put("List", ll);
								m.put("AllCount", channelService.getChannelAssetsNum(cho.getId()));
								m.put("CatalogType", "1");
								m.put("CatalogName", cho.getChannelName());
								l.add(m);
							}
						}
					}
				}
			}
		}
		return l;
	}

	//获得内容信息
	public Map<String, Object> getContentInfo(String userId, String contentId) {
		Map<String, Object> mam = null;
		MediaAsset ma = mediaService.getMaInfoById(contentId);
		List<ChannelAssetPo> chas = channelService.getChannelAssetsByAssetId(contentId);
		if (chas != null && chas.size() > 0) {
			List<Map<String, Object>> chasm = channelContentService.getChannelAssetList(chas);
			mam = ContentUtils.convert2Ma(ma.toHashMap(), null, null, chasm, null);
		}
		return mam;
	}
}
