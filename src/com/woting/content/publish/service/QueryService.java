package com.woting.content.publish.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.FConstants;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.util.FileUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.channel.model.ChannelAsset;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.content.common.utils.FileUploadUtils;
import com.woting.content.publish.utils.CacheUtils;
import com.woting.discuss.service.DiscussService;
import com.woting.favorite.service.FavoriteService;
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
	private FavoriteService favoriteService;
	@Resource
	private DiscussService discussService;
	@Resource
	private UserService userService;
	private String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
			+ "<meta name=\"viewport\" content=\"width=device-width,height=device-height,inital-scale=1.0,maximum-scale=1.0,user-scalable=no;\">"
			+ "<link href=\"/Chopin/resources/chopin/css/contentapp.css\" rel=\"stylesheet\">"
			+ "<script type=\"text/javascript\" src=\"/Chopin/resources/plugins/hplus/js/jquery-2.1.1.min.js\"></script>"
			+ "<script type=\"text/javascript\" src=\"/Chopin/resources/chopin/js/contentapp.js\"></script>"
			+ "</head><body>#####CONTENT#####</body></html>";
	private String word = "<div class=\"conpetitorContent\">" // 内容页文本内容
			+ "<div class=\"word\">#####WORD#####</div>" + "</div>";
	private String pic = "<div class=\"conpetitorContent\"><div class=\"pic\">" // 内容页图片信息
			+ "<img src=\"#####PICTURE#####\"/>" + "</div>" + "</div>";
	private String video = "<div class=\"conpetitorContent\">" // 内容视频信息
			+ "<div class=\"video\">" + "<video  id=\"myVideo\" controls preload >"
			+ "<source src=\"#####VIDEO#####\" >" + "</video>" + "</div>" + "</div>";
	private String audio = "<div class=\"conpetitorContent\">" + "<div class=\"audio\">"
			+ "<audio id=\"myAudio\" controls>" + "<source src=\"#####AUDIO#####\" type=\"audio/mpeg\">" + "</audio>"
			+ "</div>" + "</div>";

	public Map<String, Object> addContentByApp(String userId, String title, String filePath, String descn, String info, String channelId) {
		Map<String, Object> map = new HashMap<>();
		if (mediaService.getMaInfoByTitle(title) != null) {
			map.put("ReturnType", "1016");
			map.put("Message", "内容重名");
			return map;
		}
		
		UserPo userPo = userService.getUserById(userId);
		MediaAssetPo mapo = new MediaAssetPo();
		mapo.setId(SequenceUUID.getPureUUID());
		mapo.setMaTitle(title);
		mapo.setMaPubType(3);
		mapo.setMaPubId(userId);
		mapo.setMaPublisher(userPo.getLoginName());
		int num = FileUploadUtils.getFileType(filePath);
		if (num == 1 || num == 2) {
			mapo.setSubjectWords(filePath);
		}
		if (num == 3) {
			mapo.setMaImg(filePath);
		}
		mapo.setDescn(descn);
		mapo.setMaStatus(0);
		mapo.setCTime(new Timestamp(System.currentTimeMillis()));
		mapo.setMaPublishTime(new Timestamp(System.currentTimeMillis()));
		mapo.setPubCount(1);
		mapo.setLangDid("true");
		String alltext = "##" + title + "####" + descn + "####"+ info +"##";
		mapo.setAllText(alltext);
		String htmlstr = "";
		if (info != null) {
			info = "<p>" + info + "</p>";
			info = word.replace("#####WORD#####", info);
			htmlstr = html.replace("#####CONTENT#####", info);
		}
		String path = SystemCache.getCache(FConstants.APPOSPATH).getContent() + "dataCenter/mweb/" + mapo.getId() + "/" + mapo.getId() + ".html";
		FileUploadUtils.writeFile(htmlstr, path);
		path = "http://www.wotingfm.com/Chopin/dataCenter/mweb/" + mapo.getId() + "/" + mapo.getId() + ".html";
		mapo.setMaURL(path);
		MediaAsset mat = new MediaAsset();
		mat.buildFromPo(mapo);
		if (!mediaService.saveMa(mat)) {
			map.put("ReturnType", "1017");
			map.put("Message", "内容添加失败");
			return map;
		}
		ChannelAssetPo chas = new ChannelAssetPo();
		chas.setId(SequenceUUID.getPureUUID());
		chas.setChannelId(channelId);
		chas.setAssetId(mapo.getId());
		chas.setAssetType("wt_MediaAsset");
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
			map.put("ReturnType", "1017");
			map.put("Message", "内容添加失败");
			return map;
		} else {
			map.put("ReturnType", "1001");
			map.put("Message", "添加成功");
			return map;
		}
	}

	public List<Map<String, Object>> getContentListByApp(String userId) {
		return mediaService.getMaInfoByMaPubId(userId);
	}

	public Map<String, Object> removeContentByApp(String userId, String contentId) {
		MediaAsset ma = mediaService.getMaInfoById(contentId);
		String img = ma.getMaImg();
		if (img != null && !img.equals("null")) {
			FileUploadUtils.deleteFile(img.replace("http://www.wotingfm.com/", "/opt/tomcat_Chopin/webapps/"));
			String smallimg = img.replace("group03/", "group04/small");
			FileUploadUtils.deleteFile(smallimg.replace("http://www.wotingfm.com/", "/opt/tomcat_Chopin/webapps/"));
		}
		String vodie = ma.getSubjectWords();
		if (vodie != null && !vodie.equals("null"))
			FileUploadUtils.deleteFile(vodie.replace("http://www.wotingfm.com/", "/opt/tomcat_Chopin/webapps/"));
		String htmlpath = ma.getMaURL();
		if (htmlpath != null && !htmlpath.equals("null"))
			FileUploadUtils.deleteFile(htmlpath.replace("http://www.wotingfm.com/", "/opt/tomcat_Chopin/webapps/"));
		String shareurl = ma.getKeyWords();
		if (shareurl != null && !shareurl.equals("null"))
			FileUploadUtils.deleteFile(htmlpath.replace("http://www.wotingfm.com/", "/opt/tomcat_Chopin/webapps/"));
		mediaService.removeMa(contentId);
		channelService.removeChannelAsset(contentId);
		int removefavnum = favoriteService.delArticleFavorite(contentId, "6");
		int removedisnum = discussService.delArticleFavorite(contentId, "6");
		Map<String, Object> map = new HashMap<>();
		map.put("RemoveFavNum", removefavnum);
		map.put("RemoveDisNum", removedisnum);
		return map;
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

	@SuppressWarnings("unchecked")
	public Map<String, Object> makeContentHtml(String channelIds, String pubimg, String themeImg, String isShow, String mediaSrc, String thirdpath,
			String livePcUrl, String source, String sourcepath, String mastatus, String username, List<Map<String, Object>> list) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> statustype = new HashMap<>();
		statustype.put("一般文章", 0);
		statustype.put("选手介绍", 1);
		statustype.put("选手图片", 2);
		statustype.put("选手视频", 3);
		statustype.put("选手音频", 4);
		statustype.put("选手文章", 5);
		MediaAssetPo ma = new MediaAssetPo();
		ma.setId(SequenceUUID.getPureUUID());
		if (pubimg!=null) {
			String oldpath = pubimg.replace("http://www.wotingfm.com/Chopin/", SystemCache.getCache(FConstants.APPOSPATH).getContent()+"");
			String newpath = SystemCache.getCache(FConstants.APPOSPATH).getContent()+"dataCenter/media/group03/"+"lunbotu_"+ma.getId()+".png";
			FileUtils.copyFile(oldpath, newpath);
			FileUploadUtils.deleteFile(oldpath);
			System.out.println(oldpath+"     "+newpath);
		}
		if (username == null) {
			ma.setMaPubId("0");
			ma.setMaPubType(0);
			ma.setMaPublisher("admin");
			ma.setMaStatus(0);
		} else {
			UserPo u = userService.getUserByUserName(username);
			if (u == null) {
				map.put("ReturnType", "1014");
				map.put("Message", "用户不存在");
				return map;
			}
			ma.setMaPubId(u.getUserId());
			ma.setMaPubType(3);
			ma.setMaPublisher(u.getUserName());
			ma.setMaStatus(Integer.valueOf(statustype.get(mastatus) + ""));
		}
		if (source != null && sourcepath != null) {
			String sourceurl = "<a href='" + sourcepath + "'>" + source + "</a>";
			ma.setLanguage(sourceurl);
		}
		ma.setMaImg(themeImg);
		if (mediaSrc != null)
			ma.setSubjectWords(mediaSrc);
		if (thirdpath != null)
			ma.setSubjectWords(thirdpath);
		if(livePcUrl != null)
			ma.setLivePcUrl(livePcUrl);
		ma.setCTime(new Timestamp(System.currentTimeMillis()));
		ma.setMaPublishTime(ma.getCTime());
		ma.setLangDid(isShow);
		String allText = "";
		String htmlstr = "";
		if (list != null && list.size() > 0) {
			for (Map<String, Object> m : list) {
				switch (m.get("PartType") + "") {
				case "TITLE": // 标题
					String matitle = m.get("PartInfo") + "";
					if (matitle.equals("null")) {
						map.put("ReturnType", "1016");
						map.put("Message", "标题名为空");
						return map;
					}
					if (mediaService.getMaInfoByTitle(m.get("PartInfo") + "") != null) {
						map.put("ReturnType", "1015");
						map.put("Message", "内容重名");
						return map;
					}
					ma.setMaTitle(m.get("PartInfo") + "");
					allText += "##" + ma.getMaTitle() + "##";
					break;
				case "DESCN": // 摘要
					ma.setDescn(m.get("PartInfo") + "");
					allText += "##" + ma.getDescn() + "##";
					break;
				case "WORD": // 文本内容
					allText += "##" + m.get("PartInfo") + "##";
					// 合成html
					htmlstr += word.replace("#####WORD#####", m.get("PartInfo") + "");
					break;
				case "PICTURE": // 图片信息
					String partNameimg = m.get("PartName") + "";
					if (partNameimg.equals("null") || partNameimg.equals("")) {
						// 删除文件
						List<Map<String, Object>> imgs = (List<Map<String, Object>>) m.get("ResouceList");
						if (imgs != null && imgs.size() > 0) {
							for (Map<String, Object> imgm : imgs) {
								FileUtils.deleteFile(new File(imgm.get("FileOrgPath") + ""));
								FileUtils.deleteFile(new File(imgm.get("FileSmallPath") + ""));
							}
						}
					} else {
						List<Map<String, Object>> imgs = (List<Map<String, Object>>) m.get("ResouceList");
						if (imgs != null && imgs.size() > 0) {
							for (Map<String, Object> imgm : imgs) {
								String fileOrgPath = imgm.get("FileOrgPath") + "";
								if (!fileOrgPath.contains(partNameimg)) {
									// 删除文件
									FileUtils.deleteFile(new File(imgm.get("FileOrgPath") + ""));
									FileUtils.deleteFile(new File(imgm.get("FileSmallPath") + ""));
								} else {
									// 合成html
									htmlstr += pic.replace("#####PICTURE#####", fileOrgPath);
								}
							}
						}
					};
					break;
				case "VIDEO":
					String partNamevideo = m.get("PartName") + "";
					if (partNamevideo.equals("null") || partNamevideo.equals("")) {
						// 删除文件
						List<Map<String, Object>> videos = (List<Map<String, Object>>) m.get("ResouceList");
						if (videos != null && videos.size() > 0) {
							for (Map<String, Object> videom : videos) {
								String fileOrgPath = videom.get("FileOrgPath") + "";
								FileUtils.deleteFile(new File(fileOrgPath));
							}
						}
					} else {
						List<Map<String, Object>> videos = (List<Map<String, Object>>) m.get("ResouceList");
						if (videos != null && videos.size() > 0) {
							for (Map<String, Object> videom : videos) {
								String fileOrgPath = videom.get("FileOrgPath") + "";
								if (!fileOrgPath.contains(partNamevideo)) {
									// 删除文件
									FileUtils.deleteFile(new File(fileOrgPath));
								} else {
									// 合成html
									htmlstr += video.replace("#####VIDEO#####", fileOrgPath);
								}
							}
						}
					};
					break;
				case "MEDIA":
					String partNameaudio = m.get("PartName") + "";
					if (partNameaudio.equals("null") || partNameaudio.equals("")) {
						// 删除文件
						List<Map<String, Object>> audios = (List<Map<String, Object>>) m.get("ResouceList");
						if (audios != null && audios.size() > 0) {
							for (Map<String, Object> audiom : audios) {
								String fileOrgPath = audiom.get("FileOrgPath") + "";
								FileUtils.deleteFile(new File(fileOrgPath));
							}
						}
					} else {
						List<Map<String, Object>> audios = (List<Map<String, Object>>) m.get("ResouceList");
						if (audios != null && audios.size() > 0) {
							for (Map<String, Object> audiom : audios) {
								String fileOrgPath = audiom.get("FileOrgPath") + "";
								if (!fileOrgPath.contains(partNameaudio)) {
									// 删除文件
									FileUtils.deleteFile(new File(fileOrgPath));
								} else {
									// 合成html
									htmlstr += audio.replace("#####AUDIO#####", fileOrgPath);
								}
							}
						}
					};
					break;
				default:
					break;
				}
			}
		}
		ma.setAllText(CacheUtils.cleanTag(allText));
		String[] channelid = channelIds.split(",");
		if (channelid != null && channelid.length > 0) {
			for (String cid : channelid) {
				ChannelAssetPo cha = new ChannelAssetPo();
				cha.setId(SequenceUUID.getPureUUID());
				cha.setAssetId(ma.getId());
				cha.setAssetType("wt_MediaAsset");
				cha.setChannelId(cid);
				cha.setPublisherId(ma.getMaPubId());
				cha.setCheckerId("0");
				cha.setFlowFlag(2);
				cha.setSort(0);
				cha.setIsValidate(1);
				cha.setPubName(ma.getMaTitle());
				cha.setPubImg(ma.getMaImg());
				cha.setCTime(ma.getCTime());
				cha.setPubTime(ma.getCTime());
				channelService.insertChannelAsset(cha);
			}
		}
		htmlstr = html.replace("#####CONTENT#####", htmlstr);
		String path = SystemCache.getCache(FConstants.APPOSPATH).getContent() + "dataCenter/mweb/" + ma.getId()
				+ ".html";
		FileUploadUtils.writeFile(htmlstr, path);
		ma.setMaURL("http://www.wotingfm.com/Chopin/dataCenter/mweb/" + ma.getId() + ".html");
		ma.setKeyWords("http://www.wotingfm.com/Chopin/dataCenter/mweb/" + ma.getId() + ".html");
		MediaAsset mas = new MediaAsset();
		mas.buildFromPo(ma);
		mediaService.saveMa(mas);
		map.put("ReturnType", "1001");
		map.put("Message", "添加成功");
		return map;
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

	public Map<String, Object> getContentInfo2Updata(String contentid) {
		Map<String, Object> map = new HashMap<>();
		MediaAsset mediaAsset = mediaService.getMaInfoById(contentid);
		String channelIds = "";
		if (mediaAsset != null) {
			List<Map<String, Object>> chas = mediaService.getCHAByAssetId("'" + mediaAsset.getId() + "'",
					"wt_MediaAsset");
			if (chas != null && chas.size() > 0) {
				for (Map<String, Object> m2 : chas) {
					channelIds += "," + m2.get("channelId");
				}
				channelIds = channelIds.substring(1);
			}
		}
		Map<String, Object> statustype = new HashMap<>();
		statustype.put("0", "一般文章");
		statustype.put("1", "选手介绍");
		statustype.put("2", "选手图片");
		statustype.put("3", "选手视频");
		statustype.put("4", "选手音频");
		statustype.put("5", "选手文章");
		MediaAssetPo mapo = mediaAsset.convert2Po();
		String title = mapo.getMaTitle(); // 文章标题
		int pubtype = mapo.getMaStatus();
		String pubname = mapo.getMaPublisher();
		String maimg = mapo.getMaImg(); // 文章题图
		String mahtml = mapo.getMaURL(); // 文章页面路径
		String masrc = mapo.getSubjectWords(); // 文章媒体
		String isshow = mapo.getLangDid(); // 是否显示题图,媒体
		String language = mapo.getLanguage(); // 文章来源和网站<a href='http://www.sin80.com/series/chopin-polonaises'>新芭网</a>
		String livePcUrl = mapo.getLivePcUrl();
		String sourcepath = "";
		String source = "";
		String pubimgpath = SystemCache.getCache(FConstants.APPOSPATH).getContent()+"dataCenter/media/group03/"+"lunbotu_"+mapo.getId()+".png";
		File pubimgfile = new File(pubimgpath);
		if (pubimgfile.exists()) {
			map.put("ContentPubImg", "http://www.wotingfm.com/Chopin/dataCenter/media/group03/"+"lunbotu_"+mapo.getId()+".png");
		} else {
			map.put("ContentPubImg", "");
		}
		if (language!=null && !language.equals("null")) {
			Document docm = Jsoup.parse(language);
		    Element ee = docm.body().select("a").get(0);
		    sourcepath = ee.attr("href");
		    source = ee.html();
		}
		String descn = mapo.getDescn(); // 文章摘要
		Document doc = null;
		map.put("ChannelIds", channelIds);
		map.put("ThemeImg", maimg);
		map.put("Source", source);
		map.put("SourcePath", sourcepath);
		map.put("IsShow", isshow);
		map.put("LivePcUrl", livePcUrl);
		if (masrc != null) {
			if (masrc.contains("wotingfm.com")) {
				map.put("MediaSrc", masrc);
				map.put("ThirdPath", "");
			} else {
				if (masrc.contains("http")) {
					map.put("MediaSrc", "");
					map.put("ThirdPath", masrc);
				} else {
					map.put("MediaSrc", "");
					map.put("ThirdPath", "");
				}
			}
		} else {
			map.put("MediaSrc", "");
			map.put("ThirdPath", "");
		}
		map.put("ContentStatus", statustype.get(pubtype+""));
		map.put("UserName", pubname);
		List<Map<String, Object>> ls = new ArrayList<>();
		Map<String, Object> m1 = new HashMap<>();
		m1.put("PartId", 1);
		m1.put("PartType", "TITLE");
		m1.put("PartInfo", title);
		ls.add(m1);
		Map<String, Object> m2 = new HashMap<>();
		m2.put("PartId", 2);
		m2.put("PartType", "DESCN");
		m2.put("PartInfo", descn);
		ls.add(m2);
		try {
			doc = Jsoup.connect(mahtml).ignoreContentType(true).timeout(10000).get();
			if (doc != null) {
				Element body = doc.body();
				Elements eles = body.select("div[class=conpetitorContent]");
				if (eles != null && eles.size() > 0) {
					int num = 3;
					for (Element ele : eles) {
						Elements els = ele.children().select("div[class]");
						ele = els.get(0);
						String type = ele.attr("class");
						String html = ele.html();
						Map<String, Object> m = new HashMap<>();
						m.put("PartId", num);
						m.put("PartType", type);
						if (type.equals("word")) {
							m.put("PartInfo", html);
						} else {
							if (type.equals("pic")) {
								Document d1 = Jsoup.parse(html);
								Element el = d1.body().select("img").get(0);
								String path = el.attr("src");
								m.put("PartName", path.subSequence(path.lastIndexOf("/") + 1, path.length()));
								List<Map<String, Object>> l = new ArrayList<>();
								Map<String, Object> ms = new HashMap<>();
								ms.put("FileOrgPath", path);
								ms.put("FileSmallPath", path.replace("group03/", "group04/small"));
								l.add(ms);
								m.put("Resouce", l);
							} else if (type.equals("video")) {
								Document d1 = Jsoup.parse(html);
								Element el = d1.body().select("source").get(0);
								String path = el.attr("src");
								m.put("PartName", path.subSequence(path.lastIndexOf("/") + 1, path.length()));
								List<Map<String, Object>> l = new ArrayList<>();
								Map<String, Object> ms = new HashMap<>();
								ms.put("FileOrgPath", path);
								l.add(ms);
								m.put("Resouce", l);
							} else if (type.equals("audio")) {
								Document d1 = Jsoup.parse(html);
								Element el = d1.body().select("source").get(0);
								String path = el.attr("src");
								m.put("PartName", path.subSequence(path.lastIndexOf("/") + 1, path.length()));
								List<Map<String, Object>> l = new ArrayList<>();
								Map<String, Object> ms = new HashMap<>();
								ms.put("FileOrgPath", path);
								l.add(ms);
								m.put("Resouce", l);
							}
						}
						ls.add(m);
						num++;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("List", ls);
		return map;
	}

	public Map<String, Object> updateContentHtml(String contentid, String channelids, String pubimg, String themeImg, String isShow, String mediaSrc, String thirdpath, String livePcUrl, String source, String sourcepath, String mastatus, String username,List<Map<String, Object>> list) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> statustype = new HashMap<>();
		statustype.put("一般文章", 0);
		statustype.put("选手介绍", 1);
		statustype.put("选手图片", 2);
		statustype.put("选手视频", 3);
		statustype.put("选手音频", 4);
		statustype.put("选手文章", 5);
		MediaAsset me = mediaService.getMaInfoById(contentid);
		MediaAssetPo ma = me.convert2Po();
		if (pubimg!=null) {
			String oldpath = pubimg.replace("http://www.wotingfm.com/Chopin/", SystemCache.getCache(FConstants.APPOSPATH).getContent()+"");
			String newpath = SystemCache.getCache(FConstants.APPOSPATH).getContent()+"dataCenter/media/group03/"+"lunbotu_"+ma.getId()+".png";
			if (!oldpath.contains(ma.getId())) {
				FileUtils.copyFile(oldpath, newpath);
			    FileUploadUtils.deleteFile(oldpath);
			}
		}
		if (username == null) {
			ma.setMaPubId("0");
			ma.setMaPubType(0);
			ma.setMaPublisher("admin");
			ma.setMaStatus(0);
		} else {
			UserPo u = userService.getUserByUserName(username);
			if (u == null) {
				map.put("ReturnType", "1014");
				map.put("Message", "用户不存在");
				return map;
			}
			ma.setMaPubId(u.getUserId());
			ma.setMaPubType(3);
			ma.setMaPublisher(u.getUserName());
			ma.setMaStatus(Integer.valueOf(statustype.get(mastatus) + ""));
		}
		if (source != null && sourcepath != null) {
			String sourceurl = "<a href='" + sourcepath + "'>" + source + "</a>";
			ma.setLanguage(sourceurl);
		}
		ma.setMaImg(themeImg);
		if (mediaSrc != null)
			ma.setSubjectWords(mediaSrc);
		if (thirdpath != null)
			ma.setSubjectWords(thirdpath);
		if (livePcUrl!=null) {
			ma.setLivePcUrl(livePcUrl);
		}
		ma.setCTime(new Timestamp(System.currentTimeMillis()));
		ma.setMaPublishTime(ma.getCTime());
		ma.setLangDid(isShow);
		String allText = "";
		String htmlstr = "";
		if (list != null && list.size() > 0) {
			for (Map<String, Object> m : list) {
				switch (m.get("PartType") + "") {
				case "TITLE": // 标题
					String matitle = m.get("PartInfo") + "";
					if (matitle.equals("null")) {
						map.put("ReturnType", "1016");
						map.put("Message", "标题名为空");
						return map;
					}
					ma.setMaTitle(m.get("PartInfo") + "");
					allText += "##" + ma.getMaTitle() + "##";
					break;
				case "DESCN": // 摘要
					ma.setDescn(m.get("PartInfo") + "");
					allText += "##" + ma.getDescn() + "##";
					break;
				case "WORD": // 文本内容
					allText += "##" + m.get("PartInfo") + "##";
					// 合成html
					htmlstr += word.replace("#####WORD#####", m.get("PartInfo") + "");
					break;
				case "PICTURE": // 图片信息
					String partNameimg = m.get("PartName") + "";
					if (partNameimg.equals("null") || partNameimg.equals("")) {
						// 删除文件
						List<Map<String, Object>> imgs = (List<Map<String, Object>>) m.get("ResouceList");
						if (imgs != null && imgs.size() > 0) {
							for (Map<String, Object> imgm : imgs) {
								FileUtils.deleteFile(new File(imgm.get("FileOrgPath") + ""));
								FileUtils.deleteFile(new File(imgm.get("FileSmallPath") + ""));
							}
						}
					} else {
						List<Map<String, Object>> imgs = (List<Map<String, Object>>) m.get("ResouceList");
						if (imgs != null && imgs.size() > 0) {
							for (Map<String, Object> imgm : imgs) {
								String fileOrgPath = imgm.get("FileOrgPath") + "";
								if (!fileOrgPath.contains(partNameimg)) {
									// 删除文件
									FileUtils.deleteFile(new File(imgm.get("FileOrgPath") + ""));
									FileUtils.deleteFile(new File(imgm.get("FileSmallPath") + ""));
								} else {
									// 合成html
									htmlstr += pic.replace("#####PICTURE#####", fileOrgPath);
								}
							}
						}
					};
					break;
				case "VIDEO":
					String partNamevideo = m.get("PartName") + "";
					if (partNamevideo.equals("null") || partNamevideo.equals("")) {
						// 删除文件
						List<Map<String, Object>> videos = (List<Map<String, Object>>) m.get("ResouceList");
						if (videos != null && videos.size() > 0) {
							for (Map<String, Object> videom : videos) {
								String fileOrgPath = videom.get("FileOrgPath") + "";
								FileUtils.deleteFile(new File(fileOrgPath));
							}
						}
					} else {
						List<Map<String, Object>> videos = (List<Map<String, Object>>) m.get("ResouceList");
						if (videos != null && videos.size() > 0) {
							for (Map<String, Object> videom : videos) {
								String fileOrgPath = videom.get("FileOrgPath") + "";
								if (!fileOrgPath.contains(partNamevideo)) {
									// 删除文件
									FileUtils.deleteFile(new File(fileOrgPath));
								} else {
									// 合成html
									htmlstr += video.replace("#####VIDEO#####", fileOrgPath);
								}
							}
						}
					};
					break;
				case "MEDIA":
					String partNameaudio = m.get("PartName") + "";
					if (partNameaudio.equals("null") || partNameaudio.equals("")) {
						// 删除文件
						List<Map<String, Object>> audios = (List<Map<String, Object>>) m.get("ResouceList");
						if (audios != null && audios.size() > 0) {
							for (Map<String, Object> audiom : audios) {
								String fileOrgPath = audiom.get("FileOrgPath") + "";
								FileUtils.deleteFile(new File(fileOrgPath));
							}
						}
					} else {
						List<Map<String, Object>> audios = (List<Map<String, Object>>) m.get("ResouceList");
						if (audios != null && audios.size() > 0) {
							for (Map<String, Object> audiom : audios) {
								String fileOrgPath = audiom.get("FileOrgPath") + "";
								if (!fileOrgPath.contains(partNameaudio)) {
									// 删除文件
									FileUtils.deleteFile(new File(fileOrgPath));
								} else {
									// 合成html
									htmlstr += audio.replace("#####AUDIO#####", fileOrgPath);
								}
							}
						}
					};
					break;
				default:
					break;
				}
			}
		}
		ma.setAllText(CacheUtils.cleanTag(allText));
		htmlstr = html.replace("#####CONTENT#####", htmlstr);
		String path = SystemCache.getCache(FConstants.APPOSPATH).getContent() + "dataCenter/mweb/" + ma.getId()
				+ ".html";
		FileUploadUtils.writeFile(htmlstr, path);
		ma.setMaURL("http://www.wotingfm.com/Chopin/dataCenter/mweb/" + ma.getId() + ".html");
		ma.setKeyWords("http://www.wotingfm.com/Chopin/dataCenter/mweb/" + ma.getId() + ".html");
		mediaService.updateMa(ma);
		String[] chass = channelids.split(",");
		String chas = "";
		for (String str : chass) {
			chas += ",'"+str+"'";
			ChannelAsset cha = mediaService.getChannelAssetByChannelIdAndAssetId(str, contentid);
			if(cha!=null) {
				cha.setPubName(ma.getMaTitle());
			    cha.setPubImg(ma.getMaImg());
			    cha.setCTime(ma.getCTime());
			    cha.setPubTime(ma.getCTime());
			    mediaService.updateCha(cha);
			} else {
				ChannelAssetPo chapo = new ChannelAssetPo();
			    chapo.setId(SequenceUUID.getPureUUID());
			    chapo.setAssetId(ma.getId());
			    chapo.setAssetType("wt_MediaAsset");
			    chapo.setChannelId(str);
			    chapo.setPublisherId(ma.getMaPubId());
			    chapo.setCheckerId("0");
			    chapo.setFlowFlag(2);
			    chapo.setSort(0);
			    chapo.setIsValidate(1);
			    chapo.setPubName(ma.getMaTitle());
		        chapo.setPubImg(ma.getMaImg());
		        chapo.setCTime(ma.getCTime());
		        chapo.setPubTime(ma.getCTime());
		        channelService.insertChannelAsset(chapo);
			}
		}
		chas = chas.substring(1);
		Map<String, Object> m = new HashMap<>();
		m.put("value", "channelId not in ("+chas+") and assetId = '"+contentid+"'");
		mediaService.removeChaByMap(m);
		map.put("ReturnType", "1001");
		map.put("Message", "修改成功");
		return map;
	}
}
