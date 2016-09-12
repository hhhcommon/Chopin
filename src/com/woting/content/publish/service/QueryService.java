package com.woting.content.publish.service;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.spiritdata.framework.FConstants;
import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.StringUtils;
import com.woting.WtContentMngConstants;
import com.woting.cm.core.channel.model.ChannelAsset;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.dict.mem._CacheDictionary;
import com.woting.cm.core.dict.model.DictDetail;
import com.woting.cm.core.dict.persis.po.DictRefResPo;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.model.SeqMediaAsset;
import com.woting.cm.core.media.persis.po.SeqMaRefPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.cm.core.utils.ContentUtils;
import com.woting.content.broadcast.persis.pojo.BroadcastPo;
import com.woting.content.broadcast.service.BroadcastService;
import com.woting.content.manage.channel.service.ChannelContentService;
import com.woting.content.publish.utils.CacheUtils;
@Service
public class QueryService {
	@Resource(name="dataSource")
	private DataSource DataSource;
	@Resource
	private MediaService mediaService;
	@Resource
	private ChannelContentService chaService;
	@Resource
	private BroadcastService bcService;

	/**
	 * 查询列表
	 * 
	 * @param flowFlag
	 * @param page
	 * @param pagesize
	 * @param catalogsid
	 * @param source
	 * @param beginpubtime
	 * @param endpubtime
	 * @param beginctime
	 * @param endctime
	 * @return
	 */
	public Map<String, Object> getContent(int flowFlag, int page, int pagesize, String channelId, String publisherId,
			Timestamp beginpubtime, Timestamp endpubtime, Timestamp beginctime, Timestamp endctime) {
		Map<String, Object> mapall = new HashMap<String, Object>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Map<String, Object>> list2seq = new ArrayList<Map<String, Object>>();
		int numall = 0;
		String sql = "";
		
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("channelId",channelId);
		m.put("publisherId", publisherId);
		m.put("beginPubtime", beginpubtime);
		m.put("endPubtime", endpubtime);
		m.put("begincTime", beginctime);
		m.put("endcTime", endctime);
		m.put("flowFlag", flowFlag);
		numall = mediaService.getCountInCha(m);
		
		m.clear();
		m.put("channelId",channelId);
		m.put("publisherId", publisherId);
		m.put("beginPubtime", beginpubtime);
		m.put("endPubtime", endpubtime);
		m.put("begincTime", beginctime);
		m.put("endcTime", endctime);
		m.put("flowFlag", flowFlag);
		m.put("beginNum", (page - 1) * pagesize);
		m.put("size", pagesize);
		List<ChannelAssetPo> listchapo = mediaService.getContentsByFlowFlag(m);
		for (ChannelAssetPo channelAssetPo : listchapo) {
			Map<String, Object> oneData = new HashMap<String, Object>();
			oneData.put("Id", channelAssetPo.getId());// 栏目ID修改排序功能时使用
			oneData.put("MediaType", channelAssetPo.getAssetType());
			oneData.put("ContentId", channelAssetPo.getAssetId());// 内容ID获得详细信息时使用
			oneData.put("ContentImg", channelAssetPo.getPubImg());
			oneData.put("ContentCTime", channelAssetPo.getCTime());
			oneData.put("ContentPubTime", channelAssetPo.getPubTime());
			oneData.put("ContentSort", channelAssetPo.getSort());
			oneData.put("ContentFlowFlag", channelAssetPo.getFlowFlag());
			list2seq.add(oneData);
		}
		
		// 查询显示的节目名称，发布组织和描述信息
		for (Map<String, Object> map : list2seq) {
			if (map.get("MediaType").equals("wt_SeqMediaAsset")) {
				SeqMediaAsset sma = mediaService.getSmaInfoById(map.get("ContentId")+"");
				map.put("ContentName",sma.getSmaTitle());
				map.put("ContentSource", sma.getPublisher());
				map.put("ContentDesc", sma.getDescn());
			} else {
				if (map.get("MediaType").equals("wt_MediaAsset")) {
					MediaAsset ma = mediaService.getMaInfoById(map.get("ContentId")+"");
					map.put("ContentName", ma.getMaTitle());
					map.put("ContentSource", ma.getMaPublisher());
					map.put("ContentDesc", ma.getDescn());
				} else {
					if (map.get("MediaType").equals("wt_Broadcast")) {
						//待更改
						sql = "select bcTitle,bcPublisher,descn from wt_Broadcast where id = ? limit 1";
						try {
							conn = DataSource.getConnection();
							ps = conn.prepareStatement(sql);
							ps.setString(1, (String) map.get("ContentId"));
							rs = ps.executeQuery();
							while (rs != null && rs.next()) {
								map.put("ContentName", rs.getString("bcTitle"));
								map.put("ContentSource", rs.getString("bcPublisher"));
								map.put("ContentDesc", rs.getString("descn"));
							}
						} catch (SQLException e) {
							e.printStackTrace();
						} finally {
							closeConnection(conn, ps, rs);
						}
					}
				}
			}
		}
		mapall.put("List", list2seq);
		mapall.put("Count", numall);
		return mapall;
	}

	/**
	 * 查询已显示的节目信息
	 * @param pagesize
	 * @param page
	 * @param id
	 * @param acttype
	 * @return
	 */
	public Map<String, Object> getContentInfo(int pagesize, int page, String id, String acttype) {
		Map<String, Object> map = new HashMap<String, Object>();
		switch (acttype) {
		case "wt_SeqMediaAsset":
			map = getSeqInfo(pagesize, page, id, acttype);
			break;
		case "wt_MediaAsset":
			map = getAudioInfo(id, acttype);
			break;
		case "wt_Broadcast":
			map = getBroadcastInfo(id, acttype);
			break;
		default:
			break;
		}
		return map;
	}

	/**
	 * 查看专辑及级下节目的信息
	 * 
	 * @param pagesize
	 * @param page
	 * @param id
	 * @param acttype
	 * @return
	 */
	public Map<String, Object> getSeqInfo(int pagesize, int page, String id, String acttype) {
		List<Map<String, Object>> listaudio = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> seqData = new HashMap<String, Object>();// 存放专辑信息
		SeqMediaAsset sma = mediaService.getSmaInfoById(id);
		List<Map<String, Object>> catalist = mediaService.getResDictRefByResId("'"+sma.getId()+"'", "wt_SeqMediaAsset");
		List<Map<String, Object>> chlist = mediaService.getCHAByAssetId("'"+sma.getId()+"'", "wt_SeqMediaAsset");
		List<SeqMaRefPo> listseqmaref = mediaService.getSeqMaRefBySid(sma.getId());
		Map<String, Object> smap = sma.toHashMap();
		smap.put("count", listseqmaref.size());
		smap.put("smaPublishTime",chlist.get(0).get("pubTime"));
		seqData = ContentUtils.convert2Sma(smap, null, catalist, chlist, null);

		// 查询专辑和单体的联系
		List<String> listaudioid = new ArrayList<String>();
		
		for (SeqMaRefPo seqMaRefPo : listseqmaref) {
			listaudioid.add(seqMaRefPo.getMId());
		}

		// 查询单体信息
		for (String audid : listaudioid) {
			listaudio.add(getAudioInfo(audid, "wt_MediaAsset"));
		}

		if (listaudio.size() == 0) {
			map.put("audio", null);
		} else {
			map.put("audio", listaudio);
		}
		map.put("sequ", seqData);
		return map;
	}

	/**
	 * 查询单体信息
	 * 
	 * @param contentid
	 * @param acttype
	 * @return
	 */
	public Map<String, Object> getAudioInfo(String contentid, String acttype) {
        CacheEle<_CacheDictionary> cache=((CacheEle<_CacheDictionary>)SystemCache.getCache(WtContentMngConstants.CACHE_DICT));
        _CacheDictionary cd=cache.getContent();

        Map<String, Object> audioData = new HashMap<String,Object>();
		MediaAsset ma = mediaService.getMaInfoById(contentid);
		audioData.put("ContentId",ma.getId());
		audioData.put("ContentName",ma.getMaTitle());
		audioData.put("MediaType", acttype);
		audioData.put("ContentImg", ma.getMaImg());
		audioData.put("ContentCTime",ma.getCTime());
		audioData.put("ContentPubTime", ma.getMaPublishTime());
		audioData.put("ContentDesc", ma.getDescn());
		audioData.put("ContentTimes", ma.getCTime());
		audioData.put("ContentSource", ma.getMaPublisher());
		audioData.put("ContentURI", ma.getMaURL());
		audioData.put("ContentPersons", null);
		
		List<DictRefResPo> listdicref = mediaService.getResDictRefByResId(audioData.get("ContentId")+"");
		String catalogs = "";
		for (DictRefResPo dictRefResPo : listdicref) {
		    DictDetail dd=cd.getDictDetail(dictRefResPo.getDictMid(), dictRefResPo.getDictDid());
		    if (dd!=null) catalogs+= ","+dd.getNodeName();
		}
		audioData.put("ContentCatalogs", (StringUtils.isNullOrEmptyOrSpace(catalogs)||catalogs.toLowerCase().equals("null"))?null:catalogs.substring(1));
		return audioData;
	}

	/**
	 * 查询电台信息
	 * 
	 * @param contentid
	 * @param acttype
	 * @return
	 */
	public Map<String, Object> getBroadcastInfo(String contentid, String acttype) {
		Map<String, Object> broadcastData = new HashMap<String, Object>();// 单体信息
		BroadcastPo bc = bcService.getBroadcastList(contentid);
		if (bc==null) return null;
		broadcastData.put("ContentId", bc.getId());
		broadcastData.put("ContentName", bc.getBcTitle());
		broadcastData.put("MediaType", bc.getBcTitle());
		broadcastData.put("ContentImg", bc.getBcImg());
		broadcastData.put("ContentCTime", bc.getCTime());
		broadcastData.put("ContentDesc", bc.getDesc());
		broadcastData.put("ContentSource", bc.getBcPublisher());
		broadcastData.put("ContentPersons", null);
		return broadcastData;
	}

	/**
	 * 修改显示节目排序号和审核状态
	 * 
	 * @param id
	 * @param number
	 * @param flowFlag
	 * @param OpeType
	 * @return
	 */
	public Map<String, Object> modifyInfo(String id, String number, int flowFlag, String OpeType) {
		Map<String, Object> map = new HashMap<String, Object>();
		switch (OpeType) {
		case "sort":
			map = modifySort(id, number); // 修改排序号
			break;
		case "pass":
			number = "2";
			map = modifyStatus(id, number); // 修改审核状态为通过
			break;
		case "nopass":
			number = "3";
			map = modifyStatus(id, number); // 修改审核状态为未通过
			break;
		case "revoke":
			number = "4";
			map = modifyStatus(id, number); // 修改审核状态为撤回
			break;
		default:
			break;
		}
		return map;
	}

	/**
	 * 修改审核状态
	 * 
	 * @param id 栏目发布表id
	 * @param number
	 * @return
	 */
	public Map<String, Object> modifyStatus(String id, String number) {
		Map<String, Object> map = new HashMap<String, Object>();
		id = id.replaceAll("%2C", ",");
		id = id.substring(0, id.length() - 1);
		String[] ids = id.split(",");
		int num = 0;
		for (int i = 0; i < ids.length; i++) {
			ChannelAsset cha = new ChannelAsset();
			cha.setId(ids[i]);
		    cha.setPubTime(new Timestamp(System.currentTimeMillis()));
		    cha.setFlowFlag(Integer.valueOf(number));
		    num = mediaService.updateCha(cha);
		}
		if (num == 1) {
			map.put("ReturnType", "1001");
		} else {
			map.put("ReturnType", "1011");
			map.put("Message", "修改失败");
		}
		return map;
	}

	/**
	 * 修改排序号
	 * @param id
	 * @param sort
	 * @return
	 */
	public Map<String, Object> modifySort(String id, String sort) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String, Object> map = new HashMap<String, Object>();
		int num = 0;
		ChannelAsset oldcha = mediaService.getCHAInfoById(id);
		ChannelAsset newcha = new ChannelAsset();
		newcha.setCh(oldcha.getCh());
		newcha.setId(id);
		newcha.setSort(Integer.valueOf(sort));
		newcha.setPubTime(new Timestamp(System.currentTimeMillis()));
		
		String sql = "update wt_ChannelAsset set sort = ?,pubTime= ? where id = ?";
		try {
			conn = DataSource.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, Integer.valueOf(sort));
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			ps.setTimestamp(2, timestamp);
			ps.setString(3, id);
			num = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn, ps, rs);
		}
		if (num == 1) {
			map.put("ReturnType", "1001");
		} else {
			map.put("ReturnType", "1011");
			map.put("Message", "修改失败");
		}
		return map;
	}

	/**
	 * 获得分类和发布组织信息
	 * @return
	 */
	public Map<String, Object> getConditionsInfo() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> listcatalogs = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> listorganize = new ArrayList<Map<String, Object>>();
		
		String sql = "select id,channelName from wt_Channel"; // 获得栏目分类信息
		try {
			conn = DataSource.getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				Map<String, Object> oneDate = new HashMap<String, Object>();
				oneDate.put("CatalogsId", rs.getString("id"));
				oneDate.put("CatalogsName", rs.getString("channelName"));
				listcatalogs.add(oneDate);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn, ps, rs);
		}

		sql = "select id,oName from wt_Organize"; // 获得发布组织信息
		try {
			conn = DataSource.getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				Map<String, Object> oneDate = new HashMap<>();
				oneDate.put("SourceId", rs.getString("id"));
				oneDate.put("SourceName", rs.getString("oName"));
				listorganize.add(oneDate);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn, ps, rs);
		}
		map.put("Catalogs", listcatalogs);
		map.put("Source", listorganize);
		return map;
	}

	/**
	 * 加载专辑id为zjId的专辑的下属的节目列表，注意是某一页page的列表
	 * @param zjId 专辑Id
	 * @param page 第几页
	 * @return 返回该页的数据，以json串的方式，若该页无数据返回null
	 */
	public Map<String, Object> getZJSubPage(String zjId, String page) {
		Map<String, Object> map = new HashMap<String,Object>();
		//1-根据zjId，计算出文件存放目录
		String path=SystemCache.getCache(FConstants.APPOSPATH).getContent()+""+ "mweb/zj/"+zjId+"/";
		//2-判断是否有page所对应的数据
		File thisPage, nextPage;
		thisPage = new File(path+"P"+page+".json");//func()
		int nextpage = Integer.valueOf(page)+1;
		nextPage=new File(path+"P"+nextpage+".json");
		if(!thisPage.exists()){
			map.put("ReturnType", "1011");
			map.put("Message", "没有相关内容 ");
		}else {//组织本页数据
			String jsonstr = CacheUtils.readFile(path+"P"+page+".json");
			List<Map<String, Object>> listaudios = (List<Map<String, Object>>) JsonUtils.jsonToObj(jsonstr, List.class);
			if(listaudios!=null){
				map.put("ResultList", listaudios);
				map.put("ReturnType", "1001");
				map.put("NextPage", String.valueOf(nextPage.exists()));//判断是否有下一页，并组织到返回数据中
			}else{
				map.put("ReturnType", "1011");
				map.put("Message", "没有相关内容 ");
			}
		}
		return map;
	}

	/**
	 * 关闭数据库连接
	 * @param conn
	 * @param ps
	 * @param rs
	 */
	private void closeConnection(Connection conn, PreparedStatement ps, ResultSet rs) {
		if (rs!=null) try {rs.close();rs=null;} catch(Exception e) {rs=null;} finally {rs=null;};
        if (ps!=null) try {ps.close();ps=null;} catch(Exception e) {ps=null;} finally {ps=null;};
        if (conn!=null) try {conn.close();conn=null;} catch(Exception e) {conn=null;} finally {conn=null;};
	}
}
