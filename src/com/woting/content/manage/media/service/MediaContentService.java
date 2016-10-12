package com.woting.content.manage.media.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.core.model.Page;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.persis.po.ChannelPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.cm.core.utils.ContentUtils;
import com.woting.content.common.utils.FileUploadUtils;
import com.woting.content.manage.channel.service.ChannelContentService;
import com.woting.discuss.service.DiscussService;
import com.woting.favorite.service.FavoriteService;
import com.woting.passport.UGA.persistence.pojo.UserPo;
import com.woting.passport.UGA.service.UserService;
import com.woting.passport.mobile.MobileUDKey;

@Service
public class MediaContentService {
	@Resource
	private MediaService mediaService;
	@Resource
	private FavoriteService favoriteService;
	@Resource
	private DiscussService discussService;
	@Resource
	private ChannelContentService channelContentService;
	@Resource
	private ChannelService channelService;
	@Resource
	private UserService userService;
	@Resource(name = "defaultDAO")
	private MybatisDAO<MediaAssetPo> mediaAssetDao;

	@PostConstruct
	public void initParam() {
		mediaAssetDao.setNamespace("A_MEDIA");
	}

	public List<Map<String, Object>> getContents(String userId, String channelId, int perSize, int page, int pageSize, String beginCatalogId) {
		if (channelId == null) {
			return getContentsByNoChannelId(userId, perSize, page, pageSize, beginCatalogId);
		}
		List<Map<String, Object>> l = new ArrayList<>();
		ChannelPo chPo = channelService.getChannelById(channelId);
		if (chPo != null) {
			List<ChannelPo> chs = channelService.getChannelsByPcId(chPo.getId());
			if (chs == null || chs.size() == 0) {
				List<Map<String, Object>> ll = new ArrayList<>();
				List<ChannelAssetPo> chas = channelService.getChannelAssetsByChannelId(chPo.getId(), page, pageSize, 2);
				if (chas != null && chas.size() > 0) {
					List<Map<String, Object>> chsm = channelContentService.getChannelAssetList(chas);
					String[] ids = new String[chas.size()];
					List<MediaAsset> mas = new ArrayList<>();
					for (int i = 0; i < chas.size(); i++) {
						ids[i] = chas.get(i).getAssetId();
						MediaAsset ma = mediaService.getMaInfoById(chas.get(i).getAssetId());
						if (ma != null) {
							mas.add(ma);
						}
					}
					List<Map<String, Object>> fm = favoriteService.getContentFavoriteInfo(ids, userId);
					for (MediaAsset ma : mas) {
						Map<String, Object> mam = ContentUtils.convert2Ma(ma.convert2Po().toHashMap(), null, null, chsm,
								fm);
						for (ChannelAssetPo cs : chas) {
							if (cs.getAssetId().equals(mam.get("ContentId"))) {
								mam.put("ContentSort", cs.getSort());
								if (cs.getFlowFlag() == 1) {
									mam.put("IsDirect", true);
								} else {
									mam.put("IsDirect", false);
								}
							}
						}
						ll.add(mam);
					}
				}
				if (ll != null && ll.size() > 0) {
					Map<String, Object> m = new HashMap<>();
					m.put("List", ll);
					m.put("AllCount", ll.size());
					m.put("CatalogId", chPo.getId());
					m.put("CatalogName", chPo.getChannelName());
					m.put("CatalogEName", chPo.getChannelEName());
					l.add(m);
				}
			} else {
				List<ChannelPo> cs = new ArrayList<>();
				for (ChannelPo cho : chs) {
					cs.add(cho);
					if (beginCatalogId != null && beginCatalogId.equals(cho.getId())) {
						chs.removeAll(cs);
						break;
					}
				}
				if (chs != null && chs.size() > 0) {
					for (ChannelPo cho : chs) {
						if (pageSize < 1)
							return l;
						List<ChannelAssetPo> chas = channelService.getChannelAssetsByChannelId(cho.getId(), page,
								perSize, 2);
						if (chas != null && chas.size() > 0) {
							pageSize = pageSize - chas.size();
							List<Map<String, Object>> ll = new ArrayList<>();
							List<Map<String, Object>> chasm = channelContentService.getChannelAssetList(chas);
							String[] ids = new String[chas.size()];
							List<MediaAsset> mas = new ArrayList<>();
							for (int i = 0; i < chas.size(); i++) {
								ids[i] = chas.get(i).getAssetId();
								MediaAsset ma = mediaService.getMaInfoById(chas.get(i).getAssetId());
								if (ma != null) {
									mas.add(ma);
								}
							}
							List<Map<String, Object>> fm = favoriteService.getContentFavoriteInfo(ids, userId);
							for (MediaAsset ma : mas) {
								Map<String, Object> mam = ContentUtils.convert2Ma(ma.convert2Po().toHashMap(), null,
										null, chasm, fm);
								for (ChannelAssetPo css : chas) {
									if (css.getAssetId().equals(mam.get("ContentId"))) {
										mam.put("ContentSort", css.getSort());
										if (css.getFlowFlag() == 1) {
											mam.put("IsDirect", true);
										} else {
											mam.put("IsDirect", false);
										}
									}
								}
								ll.add(mam);
							}
							if (ll.size() > 0) {
								Map<String, Object> m = new HashMap<>();
								m.put("List", ll);
								m.put("AllCount", ll.size());
								m.put("CatalogId", cho.getId());
								m.put("CatalogName", cho.getChannelName());
								m.put("CatalogEName", cho.getChannelEName());
								l.add(m);
							}
						}
					}
				}
			}
		}
		return l;
	}

	private List<Map<String, Object>> getContentsByNoChannelId(String userId, int perSize, int page, int pageSize, String beginCatalogId) {
		List<Map<String, Object>> l = new ArrayList<>();
		List<ChannelPo> chs = mediaService.getChannleByPcId("0");
		List<ChannelPo> cs = new ArrayList<>();
		for (ChannelPo cho : chs) {
			cs.add(cho);
			if (beginCatalogId != null && beginCatalogId.equals(cho.getId())) {
				chs.removeAll(cs);
				break;
			}
		}
		if (chs != null && chs.size() > 0) {
			for (ChannelPo ch : chs) {
				if (pageSize < 1)
					return l;
				List<ChannelAssetPo> chas = channelService.getChannelAssetsByPcId(ch.getId(), 2, page, perSize);
				if (chas != null && chas.size() > 0) {
					pageSize = pageSize - chas.size();
					List<Map<String, Object>> ll = new ArrayList<>();
					List<Map<String, Object>> chasm = channelContentService.getChannelAssetList(chas);
					String[] ids = new String[chas.size()];
					List<MediaAsset> mas = new ArrayList<>();
					for (int i = 0; i < chas.size(); i++) {
						ids[i] = chas.get(i).getAssetId();
						MediaAsset ma = mediaService.getMaInfoById(chas.get(i).getAssetId());
						if (ma != null) {
							mas.add(ma);
						}
					}
					List<Map<String, Object>> fm = favoriteService.getContentFavoriteInfo(ids, userId);
					for (MediaAsset ma : mas) {
						Map<String, Object> mam = ContentUtils.convert2Ma(ma.convert2Po().toHashMap(), null, null,
								chasm, fm);
						for (ChannelAssetPo css : chas) {
							if (css.getAssetId().equals(mam.get("ContentId"))) {
								mam.put("ContentSort", css.getSort());
								if (css.getFlowFlag() == 1) {
									mam.put("IsDirect", true);
								} else {
									mam.put("IsDirect", false);
								}
							}
						}
						ll.add(mam);
					}
					if (ll.size() > 0) {
						Map<String, Object> m = new HashMap<>();
						m.put("List", ll);
						m.put("AllCount", ll.size());
						m.put("CatalogId", ch.getId());
						m.put("CatalogName", ch.getChannelName());
						m.put("CatalogEName", ch.getChannelEName());
						l.add(m);
					}
				}
			}
		}
		return l;
	}

	public Map<String, Object> searchByText(String searchStr, int page, int pageSize, MobileUDKey mUdk) {
		if (StringUtils.isNullOrEmptyOrSpace(searchStr)) {
			Map<String, Object> m = new HashMap<>();
			m.put("ReturnType", "1002");
			return m;
		}
		// 拼Sql串
		String __s[] = searchStr.split(",");
		String _s[] = new String[__s.length];
		for (int i = 0; i < __s.length; i++)
			_s[i] = __s[i].trim();
		String whereStr = "";
		for (int k = 0; k < _s.length; k++) {
			if (k == 0)
				whereStr += "(a.allText like '%" + _s[k] + "%'";
			else
				whereStr += " or a.allText like '%" + _s[k] + "%'";
		}
		whereStr += ")";

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("whereByClause", whereStr);
		param.put("sortByClause", "d.cTime desc");

		List<MediaAssetPo> mas = null;
		if (page == -1) {
			mas = mediaAssetDao.queryForList("getListBySearchText", param);
		} else {
			if (page == 0)
				page = 1;
			if (pageSize < 0)
				pageSize = 10;
			Page<MediaAssetPo> p = mediaAssetDao.pageQuery("getListBySearchText", param, page, pageSize);// 查询内容列表
			if (!p.getResult().isEmpty()) {
				mas = new ArrayList<MediaAssetPo>();
				mas.addAll(p.getResult());
			}
		}
		if (mas != null && !mas.isEmpty()) {
			// 获得栏目列表
			whereStr = "";
			String[] articlaIds = new String[mas.size()];
			int i = 0;
			for (MediaAssetPo maPo : mas) {
				whereStr += " or assetId='" + maPo.getId() + "'";
				articlaIds[i++] = maPo.getId();
			}
			param.clear();
			param.put("whereByClause", whereStr.substring(4));
			List<ChannelAssetPo> chas = channelService.getListByWhere(param);
			List<Map<String, Object>> chasm = channelContentService.getChannelAssetList(chas);
			// 获得喜欢列表
			String userId = (mUdk == null ? null
					: (StringUtils.isNullOrEmptyOrSpace(mUdk.getUserId()) ? null
							: (mUdk.getUserId().equals("0") ? null : mUdk.getUserId())));
			List<Map<String, Object>> fsm = favoriteService.getContentFavoriteInfo(articlaIds, userId);
			// 组织返回值
			List<Map<String, Object>> rl = new ArrayList<>();
			for (MediaAssetPo maPo : mas) {
				MediaAsset mediaAsset = new MediaAsset();
				mediaAsset.buildFromPo(maPo);
				Map<String, Object> mam = ContentUtils.convert2Ma(mediaAsset.toHashMap(), null, null, chasm, fsm);
				rl.add(mam);
			}
			Map<String, Object> m = new HashMap<>();
			m.put("AllCount", rl.size());
			m.put("List", rl);
			m.put("ReturnType", "1001");
			return m;
		}
		return null;
	}

	// 获得内容信息
	public Map<String, Object> getContentInfo(String userId, String contentId) {
		Map<String, Object> mam = null;
		MediaAsset ma = mediaService.getMaInfoById(contentId);
		if (ma == null)
			return null;
		String[] ids = new String[1];
		ids[0] = ma.getId();
		List<ChannelAssetPo> chas = channelService.getChannelAssetsByAssetId(contentId);
		List<Map<String, Object>> fm = favoriteService.getContentFavoriteInfo(ids, userId);
		if (chas != null && chas.size() > 0) {
			List<Map<String, Object>> chasm = channelContentService.getChannelAssetList(chas);
			mam = ContentUtils.convert2Ma(ma.convert2Po().toHashMap(), null, null, chasm, fm);
			String alltext = ma.getAllText();
			int num1 = alltext.indexOf("####");
			if (num1 > 0) {
				int num2 = alltext.indexOf("####", num1 + 4);
				if (num2 > 0) {
					alltext = alltext.substring(num2 + 4, alltext.length());
					alltext = alltext.replace("#", "");
				}
			}
			if (!alltext.contains("#") && !alltext.equals("") && alltext.length() > 0) {
				mam.put("ContentTTS", alltext);
			} else {
				mam.put("ContentTTS", null);
			}
		}
		return mam;
	}

	public List<Map<String, Object>> getNoPubContentList(String channelId) {
		ChannelPo ch = channelService.getChannelById(channelId);
		List<ChannelPo> chs = new ArrayList<>();
		String ids = "";
		if (ch != null) {
			chs = channelService.getChannelsByPcId(channelId);
			if (chs != null && chs.size() > 0) {
				for (ChannelPo c : chs) {
					ids += ",'" + c.getId() + "'";
				}
			}
			ids = "'" + channelId + "'" + ids;
		} else
			return null;
		List<ChannelAssetPo> chas = channelService.getChannelAssetList(ids);
		if (chas == null || chas.size() == 0)
			return null;
		List<Map<String, Object>> chasm = channelContentService.getChannelAssetList(chas);
		List<Map<String, Object>> l = new ArrayList<>();
		for (ChannelAssetPo cha : chas) {
			MediaAsset ma = mediaService.getMaInfoById(cha.getAssetId());
			if (ma != null) {
				Map<String, Object> mm = ContentUtils.convert2Ma(ma.convert2Po().toHashMap(), null, null, chasm, null);
				mm.put("ContentSort", cha.getSort());
				if (cha.getFlowFlag() == 1) {
					mm.put("IsDirect", true);
				} else {
					mm.put("IsDirect", false);
				}
				l.add(mm);
			}
		}
		return l;
	}

	public List<Map<String, Object>> getDirectContentList(String userId, String channelId, String flowFlag) {
		List<Map<String, Object>> ls = new ArrayList<>();
		if (userId == null && channelId == null) {
			List<ChannelPo> chs = channelService.getChannelsByPcId("0");
			if (chs != null && chs.size() > 0) {
				for (ChannelPo ch : chs) {
					List<Map<String, Object>> l = getDirectContent(null, ch.getId(), flowFlag, true);
					if (l != null && l.size() > 0) {
						ls.addAll(l);
					}
				}
			}
		} else {
			ls = getDirectContent(userId, channelId, flowFlag, false);
		}
		return ls;
	}

	public Map<String, Object> removeContent(String channelId, String contentId) {
		Map<String, Object> m = new HashMap<>();
		m.put("channelId", channelId);
		m.put("assetId", contentId);
		boolean n = channelService.removeChannelAssetByEntity(m);
		m.clear();
		if (n && channelService.getChannelAssetsByAssetId(contentId).size() == 0) { // 如果其他栏目下无此内容，则删除此内容
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
			boolean isok = mediaService.removeMa(contentId);
			int removefavnum = favoriteService.delArticleFavorite(contentId, channelId);
			int removedisnum = discussService.delArticleFavorite(contentId, channelId);
			if (isok) {
				m.put("ReturnType", "1001");
				m.put("Message", "内容彻底删除");
				m.put("RemoveFavNum", removefavnum);
				m.put("RemoveDisNum", removedisnum);
				return m;
			}
			return null;
		} else {
			if (n) {
				m.put("ReturnType", "1001");
				m.put("Message", "栏目下此内容删除成功");
				// m.put("RemoveFavNum", value);
				// m.put("RemoveDisNum", value);
				return m;
			}
			return null;
		}
	}

	private List<Map<String, Object>> getDirectContent(String userId, String channelId, String flowFlag, boolean getone) {
		Map<String, Object> m = new HashMap<>();
		m.put("flowFlag", flowFlag);
		m.put("isValidate", 1);
		m.put("sortByClause", "sort desc");
		String channelIds = "";
		List<ChannelPo> chs = channelService.getChannelsByPcId(channelId);
		if (chs != null && chs.size() > 0) {
			for (ChannelPo chPo : chs) {
				channelIds += ",'" + chPo.getId() + "'";
			}
		}
		channelIds += ",'" + channelId + "'";
		channelIds = channelIds.substring(1);
		m.put("whereByClause", "channelId in (" + channelIds + ")");
		List<ChannelAssetPo> chas = channelService.getChannelAssets(m);
		List<Map<String, Object>> l = new ArrayList<>();
		if (chas != null && chas.size() > 0) {
			String[] ids = new String[chas.size()];
			for (int i = 0; i < chas.size(); i++) {
				ids[i] = chas.get(i).getAssetId();
			}
			List<Map<String, Object>> fm = favoriteService.getContentFavoriteInfo(ids, userId);
			for (ChannelAssetPo cha : chas) {
				MediaAsset ma = mediaService.getMaInfoById(cha.getAssetId());
				Map<String, Object> mam = ContentUtils.convert2Ma(ma.convert2Po().toHashMap(), null, null, null, fm);
				mam.put("ContentSort", cha.getSort());
				mam.put("ChannelId", cha.getChannelId());
				l.add(mam);
				if (getone)
					break;
			}
			return l;
		}
		return null;
	}

	public List<Map<String, Object>> getPlaySumList(String userId) {
		List<Map<String, Object>> l = new ArrayList<>();
		Map<String, Object> m = new HashMap<>();
		m.put("maStatus", 1);
		m.put("sortByClause", "pubCount desc");
		List<MediaAssetPo> mas = mediaAssetDao.queryForList("getPlayMaListByIds", m);
		if(mas!=null && mas.size()>0) {
			String[] ids=new String[mas.size()];
			List<String> userIds=new ArrayList<String>();
			for (int i = 0; i < ids.length; i++) {
                ids[i] = mas.get(i).getId();
                userIds.add(mas.get(i).getMaPubId());
			}
			List<Map<String, Object>> favs=favoriteService.getContentFavoriteInfo(ids, userId);
			List<UserPo> users=userService.getUserByIds(userIds);
			for (MediaAssetPo ma : mas) {
				for (Map<String, Object> mp : favs) {
					if (mp.get("ContentId").equals(ma.getId())) {
                        Map<String, Object> map = new HashMap<>();
					    for (UserPo upo: users) {
					        if (upo.getUserId().equals(ma.getMaPubId())) {
		                        map.put("UserId", upo.getUserId());
		                        map.put("UserName", upo.getUserName());
		                        map.put("UserBigImg", upo.getPortraitBig());
		                        map.put("UserSmallImg", upo.getPortraitMini());
					        }
					    }
						map.put("FavoSum", mp.get("FavoSum"));
						map.put("IsFavorate", mp.get("IsFavorate"));
						map.put("IsPlaying", ma.getPubCount());
						map.put("ContentId", ma.getId());
						l.add(map);
					}
				}
			}
		}
		if (l != null && l.size() > 0)
			return l;
		return null;
	}

	public Map<String, Object> getPlayerContents(String id) {
		Map<String, Object> m = new HashMap<>();// MaStatus0 MaStatus1
		List<MediaAssetPo> mas = mediaAssetDao.queryForList("getMaListByContentId", id);
		if (mas != null && mas.size() > 0) {
			for (MediaAssetPo ma : mas) {
				if (m.containsKey("MaStatus" + ma.getMaStatus())) {
					List<Map<String, Object>> ms = (List<Map<String, Object>>) m.get("MaStatus" + ma.getMaStatus());
					ms.add(ContentUtils.convert2Ma(ma.toHashMap(), null, null, null, null));
				} else {
					List<Map<String, Object>> ms = new ArrayList<>();
					ms.add(ContentUtils.convert2Ma(ma.toHashMap(), null, null, null, null));
					m.put("MaStatus" + ma.getMaStatus(), ms);
				}
			}
		}
		m.put("AllCount", mas.size());
		return m;
	}
}