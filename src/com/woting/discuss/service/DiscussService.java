package com.woting.discuss.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.core.model.Page;
import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.utils.ContentUtils;
import com.woting.content.manage.channel.service.ChannelContentService;
import com.woting.discuss.model.Discuss;
import com.woting.discuss.persis.po.DiscussPo;
import com.woting.favorite.service.FavoriteService;

public class DiscussService {
    @Resource
    private FavoriteService favoriteService;
    @Resource(name="defaultDAO")
    private MybatisDAO<DiscussPo> discussDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<MediaAssetPo> mediaAssetDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<ChannelAssetPo> channelAssetDao;
    @Resource
    private ChannelContentService channelContentService;

    @PostConstruct
    public void initParam() {
        discussDao.setNamespace("WT_DISCUSS");
        mediaAssetDao.setNamespace("A_MEDIA");
        channelAssetDao.setNamespace("A_CHANNELASSET");
    }

    /**
     * 得到重复意见
     * @param opinion 意见信息
     * @return 创建用户成功返回1，否则返回0
     */
    public List<Discuss> getDuplicates(Discuss discuss) {
        try {
            List<Discuss> ret=new ArrayList<Discuss>();
            List<DiscussPo> _ret=discussDao.queryForList("getDuplicates", discuss.toHashMapAsBean());
            if (_ret!=null&&!_ret.isEmpty()) {
                for (DiscussPo dpo: _ret) {
                    Discuss ele=new Discuss();
                    ele.buildFromPo(dpo);
                    ret.add(ele);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存用户所提意见
     * @param opinion 意信息
     * @return 创建用户成功返回1，否则返回0
     */
    public int insertDiscuss(Discuss discuss) {
        int i=0;
        try {
            discuss.setId(SequenceUUID.getUUIDSubSegment(4));
            discussDao.insert(discuss.convert2Po());
            i=1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * 删除评论
     * @param discuss 要删除的评论信息
     * @return 0-删除失败;1删除成功;-1无对应的评论无法删除;-2无权删除
     */
    public int delDiscuss(Discuss discuss) {
        try {
            Map<String, Object> param=((DiscussPo)discuss.convert2Po()).toHashMapAsBean();
            DiscussPo dpo=discussDao.getInfoObject(param);
            if (dpo==null) return -1;
            if (discuss.getImei().equals(dpo.getImei())&&discuss.getUserId().equals(dpo.getUserId())) {
                discussDao.delete(param);
                return 1;
            } else return -2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 根据文章Id获得文章的评论列表
     * @param articleId 文章Id
     * @param page 页数
     * @param pageSize 每页条数
     * @return 文章评论列表
     */
    public Map<String, Object> getArticleDiscusses(String articleId, int page, int pageSize) {
        try {
            Map<String, Object> param=new HashMap<String, Object>();
            param.put("articleId", articleId);
            param.put("sortByClause", " cTime desc");
            List<DiscussPo> ol=null;
            long allCount=0;
            if (page>=0) { //分页
                if (page==0) page=1;
                if (pageSize<0) pageSize=10;
                Page<DiscussPo> p=this.discussDao.pageQuery(param, page, pageSize);
                if (!p.getResult().isEmpty()) {
                    ol=new ArrayList<DiscussPo>();
                    ol.addAll(p.getResult());
                }
                allCount=p.getDataCount();
            } else { //获得所有
                ol=this.discussDao.queryForList(param);
            }
            if (ol!=null&&ol.size()>0) {
                List<Discuss> ret=new ArrayList<Discuss>();
                if (ol!=null&&!ol.isEmpty()) {
                    for (DiscussPo dpo: ol) {
                        Discuss ele=new Discuss();
                        ele.buildFromPo(dpo);
                        ret.add(ele);
                    }
                }
                param.clear();
                param.put("AllCount", allCount);
                param.put("List", ret);
                return param;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据用户Id,获得用户评论过的文章列表
     * @param userId 用户Id
     * @param page 页数
     * @param pageSize 每页条数
     * @return 文章列表
     */
    public Map<String, Object> getUserDiscusses(String userId, int page, int pageSize) {
        if (StringUtils.isNullOrEmptyOrSpace(userId)) return null;
        try {
            //获得列表
            Map<String, Object> param=new HashMap<String, Object>();
            param.put("whereByClause", "e.userId='"+userId+"'");
            param.put("sortByClause", "d.cTime desc");
            long allCount=0;
            List<MediaAssetPo> mas=null;
            if (page==-1) {
                mas=mediaAssetDao.queryForList("getUserDiscussContents", param);
            } else {
                if (page==0) page=1;
                if (pageSize<0) pageSize=10;
                Page<MediaAssetPo> p=mediaAssetDao.pageQuery("getUserDiscussContents", param, page, pageSize);//查询内容列表
                if (!p.getResult().isEmpty()) {
                    mas=new ArrayList<MediaAssetPo>();
                    mas.addAll(p.getResult());
                }
                allCount=p.getDataCount();
            }

            if (!mas.isEmpty()) {
                //获得相关栏目信息
                String whereStr="";
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
                List<Map<String, Object>> fsm=favoriteService.getContentFavoriteInfo(articlaIds, userId);

                //组织返回值
                List<Map<String, Object>> rl=new ArrayList<>();
                for (MediaAssetPo maPo : mas) {
                    MediaAsset mediaAsset = new MediaAsset();
                    mediaAsset.buildFromPo(maPo);
                    Map<String, Object> mam=ContentUtils.convert2Ma(mediaAsset.toHashMap(), null, null, chasm, fsm);
                    rl.add(mam);
                }
                param.clear();
                param.put("AllCount", allCount);
                param.put("List", rl);
                return param;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除某文章的所有评论
     * @param articleId 文章Id
     * @param channelId 文章的Id
     * @return 返回删除的数目
     */
    public int delArticleFavorite(String articleId, String channelId) {
        //获得列表
        Map<String, String> param=new HashMap<String, String>();
        param.put("resId", articleId);

        return discussDao.delete("delArticle", param);
    }
}