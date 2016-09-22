package com.woting.cm.core.media.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.util.JsonUtils;
import com.woting.cm.core.channel.model.Channel;
import com.woting.cm.core.channel.model.ChannelAsset;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.dict.persis.po.DictRefResPo;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.persis.po.MaSourcePo;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.persis.po.SeqMaRefPo;
import com.woting.cm.core.media.persis.po.SeqMediaAssetPo;
import com.woting.cm.core.utils.ContentUtils;
import com.woting.content.manage.channel.service.ChannelContentService;
import com.woting.favorite.service.FavoriteService;

public class MediaService {
    @Resource(name="defaultDAO")
    private MybatisDAO<MediaAssetPo> mediaAssetDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<MaSourcePo> maSourceDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<SeqMediaAssetPo> seqMediaAssetDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<SeqMaRefPo> seqMaRefDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<ChannelAssetPo> channelAssetDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<ChannelPo> channelDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<DictRefResPo> dictRefDao;
    @Resource
    private FavoriteService favoriteService;
    @Resource
    private ChannelContentService channelContentService;

    @PostConstruct
    public void initParam() {
        mediaAssetDao.setNamespace("A_MEDIA");
        maSourceDao.setNamespace("A_MEDIA");
        seqMaRefDao.setNamespace("A_MEDIA");
        seqMediaAssetDao.setNamespace("A_MEDIA");
        channelAssetDao.setNamespace("A_CHANNELASSET");
        channelDao.setNamespace("A_CHANNEL");
        dictRefDao.setNamespace("A_DREFRES");
    }
    
    public MediaAsset getMaInfoByTitle(String title) {
    	MediaAssetPo mapo = mediaAssetDao.getInfoObject("getMaInfoByTitle",title);
    	if (mapo==null) return null;
    	MediaAsset ma = new MediaAsset();
    	ma.buildFromPo(mapo);
    	return ma;
    }
    
    public int getCountInCha(Map<String, Object> m){
		return channelAssetDao.getCount("countnum", m);
    }
    
    public List<ChannelAssetPo> getContentsByFlowFlag(Map<String, Object> m){
    	return channelAssetDao.queryForList("getListByFlowFlag", m);
    }
    
    public List<ChannelPo> getChannleByPcId(String pcId) {
    	Map<String, Object> m = new HashMap<>();
    	m.put("pcId", pcId);
		return channelDao.queryForList("getList", m);
    }
    
    //根据主播id查询其所有单体资源
    public List<Map<String, Object>> getMaInfoByMaPubId(String userId) {
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        List<MediaAssetPo> listpo = new ArrayList<MediaAssetPo>();
        listpo = mediaAssetDao.queryForList("getMaListByMaPubId", userId);
        if(listpo!=null&&listpo.size()>0){
        	String ids = "";
        	String[] articlaIds = new String[listpo.size()];
        	for (int i = 0; i < listpo.size(); i++) {
				ids = ",'"+listpo.get(i).getId()+"'";
				articlaIds[i] = listpo.get(i).getId();
			}
        	ids = ids.substring(1);
        	List<Map<String, Object>> fm = favoriteService.getContentFavoriteInfo(articlaIds, userId);
        	List<ChannelAssetPo> chas = getChaListByIds(ids);
        	List<Map<String, Object>> chsm = channelContentService.getChannelAssetList(chas);
        	for (MediaAssetPo mediaAssetPo : listpo) {
        	    MediaAsset ma=new MediaAsset();
			    ma.buildFromPo(mediaAssetPo);
			    Map<String, Object> mam = ContentUtils.convert2Ma(ma.toHashMap(), null, null, chsm, fm);
			    list.add(mam);
		    }
        }
        return list;
    }
    
    public List<ChannelAssetPo> getChaListByIds(String ids) {
    	Map<String, Object> m = new HashMap<>();
    	m.put("assetIds", ids);
    	m.put("assetType", "wt_MediaAsset");
    	m.put("sortByClause", "cTime");
		return channelAssetDao.queryForList("getListByAssetIds", m);
    }
    
    public ChannelAsset getChannelAssetByChannelIdAndAssetId(String channelId, String assetId) {
    	Map<String, Object> m = new HashMap<>();
    	m.put("channelId", channelId);
    	m.put("assetId", assetId);
    	List<ChannelAssetPo> cha = channelAssetDao.queryForList("getList", m);
    	if (cha!=null && cha.size()==1) {
    		ChannelAsset chass = new ChannelAsset();
    		chass.buildFromPo(cha.get(0));
			return chass;
		}
		return null;
    }
    
    //根据栏目id得到栏目
    public Channel getChInfoById(String id){
    	Channel ch = new Channel();
    	ChannelPo chpo = channelDao.getInfoObject("getInfoById", id);
    	if(chpo==null) return null;
    	ch.buildFromPo(chpo);
    	return ch;
    }
    
    //根据栏目发布表id得到栏目发布信息
    public ChannelAsset getCHAInfoById(String id){
    	ChannelAsset cha = new ChannelAsset();
    	ChannelAssetPo chapo = channelAssetDao.getInfoObject("getInfoById",id);
    	if (chapo==null) return null;
    	cha.buildFromPo(chapo);
		return cha;
    }
    
  //根据栏目发布表资源id得到栏目发布信息
    public ChannelAsset getCHAInfoByAssetId(String id){
    	ChannelAsset cha = new ChannelAsset();
    	ChannelAssetPo chapo = channelAssetDao.getInfoObject("getInfoByAssetId",id);
    	if (chapo==null) return null;
    	cha.buildFromPo(chapo);
		return cha;
    }
    
    public List<ChannelAssetPo> getCHAListByAssetId(String assetIds, String assetType){
    	Map<String, String> param=new HashMap<String, String>();
        param.put("assetType", assetType);
        param.put("assetIds", assetIds);
    	List<ChannelAssetPo> chapolist = channelAssetDao.queryForList("getListByAssetIds", param);
		return chapolist;
    }
    
    public List<Map<String, Object>> getCHAByAssetId(String assetIds, String assetType){
    	Map<String, Object> param=new HashMap<String,Object>();
    	param.put("assetType", assetType);
    	param.put("assetIds", assetIds);
    	List<ChannelAssetPo> chpolist = channelAssetDao.queryForList("getListByAssetIds", param);
    	List<Map<String, Object>> chlist = new ArrayList<Map<String,Object>>();
    	for (ChannelAssetPo chpo : chpolist) {
			chlist.add(chpo.toHashMap());
		}
		return chlist;
    }
    
    public List<MediaAssetPo> getMaListByIds(String ids) {
    	Map<String, Object> m = new HashMap<>();
    	m.put("ids", ids);
    	m.put("sortByClause", "cTime desc");
		return mediaAssetDao.queryForList("getMaListByIds", m);
    }
    
    public MediaAsset getMaInfoById(String id) {
        MediaAsset ma=new MediaAsset();
        MediaAssetPo mapo = mediaAssetDao.getInfoObject("getMaInfoById", id);
        if(mapo==null) return null;
        else ma.buildFromPo(mapo);
        return ma;
    }
    
    public boolean saveMa(MediaAsset ma) {
    	if(mediaAssetDao.insert("insertMa", ma.convert2Po())>0)
    		return true;
    	return false;
    }
    
    public void saveCha(ChannelAsset cha){
    	System.out.println(JsonUtils.objToJson(cha.convert2Po()));
    	channelAssetDao.insert("insert", cha.convert2Po());
    }
    
    public void updateMa(MediaAsset ma) {
        mediaAssetDao.update("updateMa", ma.convert2Po());
    }
    
    public int  updateCha(ChannelAsset cha) {
    	return channelAssetDao.update("update", cha.convert2Po().toHashMap());
    }

    public boolean removeMa(String id){
    	if (mediaAssetDao.delete("multiMaById", id)>0) 
    		return true;
    	return false;
    }
    
    public void removeCha(String assetId){
    	channelAssetDao.delete("deleteByAssetId", assetId);
    }
}