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
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.core.model.Page;
import com.spiritdata.framework.util.StringUtils;
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
import com.woting.favorite.service.FavoriteService;
import com.woting.passport.mobile.MobileUDKey;

@Service
public class MediaContentService {
	@Resource
	private MediaService mediaService;
    @Resource
    private ChannelContentService channelContentService;
    @Resource
    private FavoriteService favoriteService;
	@Resource
	private ChannelService channelService;
    @Resource(name="defaultDAO")
    private MybatisDAO<ChannelAssetPo> channelAssetDao;

    private _CacheChannel _cc=null;
    @Resource(name="defaultDAO")
    private MybatisDAO<MediaAssetPo> mediaAssetDao;
	
	@PostConstruct
    public void initParam() {
        _cc=(SystemCache.getCache(WtContentMngConstants.CACHE_CHANNEL)==null?null:((CacheEle<_CacheChannel>)SystemCache.getCache(WtContentMngConstants.CACHE_CHANNEL)).getContent());
    }
	
	public List<Map<String, Object>> getContents(String channelId, int perSize, int page, int pageSize) {
		List<Map<String, Object>> l = new ArrayList<>();
		ChannelPo chPo = channelService.getChannelById(channelId);
		if(chPo!=null) {
			List<ChannelPo> chs = channelService.getChannelsByPcId(chPo.getId());
			if(chs==null || chs.size()==0) {
				List<ChannelAssetPo> chas = channelService.getChannelAssetsByChannelId(chPo.getId(), page, pageSize);
				if(chas!=null && chas.size()>0) {
					List<Map<String, Object>> chsm = channelContentService.getChannelAssetList(chas);
					String resids = "";
		    	    for (ChannelAssetPo chapo : chas) {
					    resids+=",'"+chapo.getAssetId()+"'";
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
				for (ChannelPo cho : chs) {
					if(pageSize<1) return l; 
					List<ChannelAssetPo> chas = channelService.getChannelAssetsByChannelId(cho.getId(), page, perSize);
					pageSize = pageSize - pageSize;
					if(chas!=null && chas.size()>0) {
						List<Map<String, Object>> ll = new ArrayList<>();
						List<Map<String, Object>> chasm = channelContentService.getChannelAssetList(chas);
						String resids = "";
			    	    for (ChannelAssetPo chapo : chas) {
						    resids+=",'"+chapo.getAssetId()+"'";
					    }
			    	    resids = resids.substring(1);
			    	    List<MediaAssetPo> mas = mediaService.getMaListByIds(resids);
			    	    for (MediaAssetPo maPo : mas) {
							MediaAsset mediaAsset = new MediaAsset();
							mediaAsset.buildFromPo(maPo);
							Map<String, Object> mam = ContentUtils.convert2Ma(mediaAsset.toHashMap(), null, null, chasm, null);
							ll.add(mam);
						}
			    	    if (ll.size()>0) {
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
		return l;
	}
	
    public Map<String, Object> searchByText(String searchStr, int page, int pageSize, MobileUDKey mUdk) {
        if (StringUtils.isNullOrEmptyOrSpace(searchStr)) {
            Map<String, Object> m=new HashMap<>();
            m.put("ReturnType", "1002");
            return m;
        }
        //拼Sql串
        String __s[]=searchStr.split(",");
        String _s[]=new String[__s.length];
        for (int i=0; i<__s.length; i++) _s[i]=__s[i].trim();
        String whereStr="";
        for (int k=0; k<_s.length; k++) {
            if (k==0) whereStr+="(a.fullText like '%"+_s[k]+"%'";
            else whereStr+=" or a.fullText like '%"+_s[k]+"%'";
        }
        whereStr+=")";

        Map<String, Object> param=new HashMap<String, Object>();
        param.put("whereByClause", whereStr);
        param.put("sortByClause", "d.cTime desc");

        List<MediaAssetPo> mas=null;
        if (page==-1) {
            mas=mediaAssetDao.queryForList("getListBySearchText", param);
        } else {
            if (page==0) page=1;
            if (pageSize<0) pageSize=10;
            Page<MediaAssetPo> p=mediaAssetDao.pageQuery("getListBySearchText", param, page, pageSize);//查询内容列表
            if (!p.getResult().isEmpty()) {
                mas=new ArrayList<MediaAssetPo>();
                mas.addAll(p.getResult());
            }
        }
        if (!mas.isEmpty()) {
            //获得栏目列表
            whereStr="";
            String[] articlaIds=new String[mas.size()];
            int i=0;
            for (MediaAssetPo maPo:mas) {
                whereStr+=" or assetId='"+maPo.getId()+"'";
                articlaIds[i++]=maPo.getId();
            }
            param.clear();
            param.put("whereByClause", whereStr.substring(4));
            List<ChannelAssetPo> chas=channelAssetDao.queryForList("getListByWhere", param);
            List<Map<String, Object>> chasm=channelContentService.getChannelAssetList(chas);
            //获得喜欢列表
            String userId=(mUdk==null?null:(StringUtils.isNullOrEmptyOrSpace(mUdk.getUserId())?null:(mUdk.getUserId().equals("0")?null:mUdk.getUserId())));
            List<Map<String, Object>> fsm=favoriteService.getContentFavoriteInfo(articlaIds, userId);

            //组织返回值
            List<Map<String, Object>> rl = new ArrayList<>();
            for (MediaAssetPo maPo : mas) {
                MediaAsset mediaAsset = new MediaAsset();
                mediaAsset.buildFromPo(maPo);
                Map<String, Object> mam=ContentUtils.convert2Ma(mediaAsset.toHashMap(), null, null, chasm, fsm);
                rl.add(mam);
            }
            Map<String, Object> m=new HashMap<>();
            m.put("AllCount", rl.size());
            m.put("List", rl);
            m.put("ReturnType", "1001");
            return m;
        }
        return null;
    }
}
