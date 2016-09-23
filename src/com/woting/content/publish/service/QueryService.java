package com.woting.content.publish.service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.woting.content.publish.utils.CacheUtils;
import com.woting.passport.UGA.persistence.pojo.UserPo;
import com.woting.passport.UGA.service.UserService;

@Service
public class QueryService {
	@Resource(name = "dataSource")
	private DataSource DataSource;
	@Resource
	private ChannelService channelService;
	@Resource
	private MediaService mediaService;
	@Resource
	private UserService userService;
	private String title = "<div class=\"titleContent\">" //内容页标题
			+ "<h2>#####TITLE#####</h2></div>";
	private String src = " <div class=\"webSource\">" //内容页来源信息
			+ "<span class=\"time\">#####PUBTIME#####</span>"
			+ "<a href=\"#####SOURCEPATH#####\" class=\"source\">来源:#####SOURCE#####</a>"
			+ "</div>";
	private String word = "<div class=\"conpetitorContent\">" //内容页文本内容
			+ "<div class=\"word\">#####WORD#####</div>"
			+ "</div>";
	private String pic = "<div class=\"conpetitorContent\"><div class=\"pic\">" //内容页图片信息
			+ "<img src=\"#####PICTURE#####\"/>"
					+ "</div>"
					+ "</div>";

	public Map<String, Object> addContentByApp(String userId, String title, String filePath, String descn,
			String channelId) {
		Map<String, Object> map = new HashMap<>();
		if (mediaService.getMaInfoByTitle(title) == null) {
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
		if (mapo.getMaImg() != null)
			chas.setPubImg(mapo.getMaImg());
		chas.setIsValidate(1);
		chas.setCheckerId("1");
		chas.setSort(0);
		chas.setFlowFlag(2);
		chas.setInRuleIds("0");
		chas.setCheckRuleIds("0");
		chas.setCTime(new Timestamp(System.currentTimeMillis()));
		chas.setPubTime(new Timestamp(System.currentTimeMillis()));
		if (!channelService.insertChannelAsset(chas)) {
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

	// 只用于发布已撤销的内容
	public boolean addPubContentInfo(String channelId, String contentId) {
		ChannelAsset cha = mediaService.getChannelAssetByChannelIdAndAssetId(channelId, contentId);
		if (cha != null) {
			cha.setIsValidate(1);
			mediaService.updateCha(cha);
			return true;
		}
		return false;
	}

	public boolean modifyPubSortInfo(String channelId, int contentSort, String contentId) {
		ChannelAsset cha = mediaService.getChannelAssetByChannelIdAndAssetId(channelId, contentId);
		if (cha != null) {
			cha.setSort(contentSort);
			mediaService.updateCha(cha);
			return true;
		}
		return false;
	}

	public boolean removePubInfo(String channelId, String contentId) {
		ChannelAsset cha = mediaService.getChannelAssetByChannelIdAndAssetId(channelId, contentId);
		if (cha != null) {
			cha.setIsValidate(2);
			mediaService.updateCha(cha);
			return true;
		}
		return false;
	}

	public boolean makeContentHtml(String channelId, String source, String sourcepath, String mastatus, String username, List<Map<String, Object>> list) {
		Map<String, Object> statustype = new HashMap<>();
		statustype.put("一般文章", 0);
		statustype.put("选手介绍", 1);
		statustype.put("选手图片", 2);
		statustype.put("选手视频", 3);
		statustype.put("选手音频", 4);
		statustype.put("选手文章", 5);
		MediaAssetPo ma = new MediaAssetPo();
		ma.setId(SequenceUUID.getPureUUID());
		if (username == null) {
			ma.setMaPubId("0");
			ma.setMaPubType(0);
			ma.setMaPublisher("admin");
			ma.setMaStatus(0);
		} else {
			UserPo u = userService.getUserByUserName(username);
			ma.setMaPubId(u.getUserId());
			ma.setMaPubType(3);
			ma.setMaPublisher(u.getUserName());
			ma.setMaStatus(Integer.valueOf(statustype.get(mastatus) + ""));
		}
		if (source!=null && sourcepath !=null) {
			String sourceurl = "<a href='"+sourcepath+"'>"+source+"</a>";
			ma.setLanguage(sourceurl);
		}
		ma.setCTime(new Timestamp(System.currentTimeMillis()));
		ma.setMaPublishTime(ma.getCTime());
		String allText = "";
		String htmlstr = "";
		DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		if (list != null && list.size() > 0) {
			for (Map<String, Object> m : list) {
				switch (m.get("PartType") + "") {
				case "TITLE": //标题
					ma.setMaTitle(m.get("PartInfo")+"");
					allText +="##"+ma.getMaTitle()+"##";
					//合成html
					htmlstr += title.replace("#####TITLE#####", ma.getMaTitle());
					htmlstr += src.replace("#####PUBTIME#####", sdf.format(ma.getCTime())).replace("#####SOURCE#####", source).replace("#####SOURCEPATH#####", sourcepath);
					break;
				case "DESCN": //摘要
					ma.setDescn(m.get("PartInfo")+"");
					allText +="##"+ma.getDescn()+"##";
					break;
				case "WORD" : //文本内容
					allText += "##"+m.get("PartInfo")+"##";
					//合成html
					htmlstr += word.replace("#####WORD#####", m.get("PartInfo")+"");
					break;
				case "PICTURE" : //图片信息
					String partNameimg = m.get("PartName")+"";
					if (partNameimg.equals("null") || partNameimg.equals("")) {
						//删除文件
					} else {
						List<Map<String, Object>> imgs = (List<Map<String, Object>>) m.get("ResouceList");
						if(imgs!=null && imgs.size()>0) {
							for (Map<String, Object> imgm : imgs) {
								String fileOrgPath = imgm.get("FileOrgPath")+"";
								if(!fileOrgPath.contains(partNameimg)) {
									//删除文件
								} else {
									ma.setMaImg(fileOrgPath);
									//合成html
									htmlstr += pic.replace("#####PICTURE#####", fileOrgPath);
								}
							}
						}
					};
					break;
				case "VIDEO" :
					String partNamevideo = m.get("PartName")+"";
					if (partNamevideo.equals("null") || partNamevideo.equals("")) {
						//删除文件
					} else {
						List<Map<String, Object>> videos = (List<Map<String, Object>>) m.get("ResouceList");
						if (videos!=null && videos.size()>0) {
							for (Map<String, Object> videom : videos) {
								String fileOrgPath = videom.get("FileOrgPath")+"";
								if(!fileOrgPath.contains(partNamevideo)) {
									//删除文件
								} else {
									ma.setSubjectWords(fileOrgPath);
									//合成html
								}
							}
						}
					};
					break;
				case "AUDIO" :
					String partNameaudio = m.get("PartName")+"";
					if (partNameaudio.equals("null") || partNameaudio.equals("")) {
						//删除文件
					} else {
						List<Map<String, Object>> videos = (List<Map<String, Object>>) m.get("ResouceList");
						if (videos!=null && videos.size()>0) {
							for (Map<String, Object> videom : videos) {
								String fileOrgPath = videom.get("FileOrgPath")+"";
								if(!fileOrgPath.contains(partNameaudio)) {
									//删除文件
								} else {
									ma.setSubjectWords(fileOrgPath);
									//合成html
								}
							}
						}
					};
					break;
				default: break;
				}
			}
		}
		ma.setAllText(CacheUtils.cleanTag(allText));
		ChannelAssetPo cha = new ChannelAssetPo();
		cha.setId(SequenceUUID.getPureUUID());
		cha.setAssetId(ma.getId());
		cha.setAssetType("wt_MediaAsset");
		cha.setChannelId(channelId);
		cha.setPublisherId(ma.getMaPubId());
		cha.setCheckerId("0");
		cha.setFlowFlag(2);
		cha.setSort(0);
		cha.setIsValidate(1);
		cha.setPubName(ma.getMaTitle());
		cha.setPubImg(ma.getMaImg());
		cha.setCTime(ma.getCTime());
		cha.setPubTime(ma.getCTime());
		System.out.println(htmlstr);
		System.out.println(JsonUtils.objToJson(ma));
		System.out.println(JsonUtils.objToJson(cha));
		return true;
	}

	public boolean modifyDirectContentInfo(String channelId, String contentId, int status) {
		ChannelAsset cha = mediaService.getChannelAssetByChannelIdAndAssetId(channelId, contentId);
		if (cha != null) {
			cha.setFlowFlag(status);
			mediaService.updateCha(cha);
			return true;
		}
		return false;
	}
}
