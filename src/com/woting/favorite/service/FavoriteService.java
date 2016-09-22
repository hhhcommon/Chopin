package com.woting.favorite.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.core.model.Page;
import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.utils.ContentUtils;
import com.woting.content.manage.channel.service.ChannelContentService;
import com.woting.cm.core.channel.persis.po.ChannelAssetPo;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.model.MediaAsset;
import com.woting.cm.core.media.persis.po.MediaAssetPo;
import com.woting.cm.core.media.service.MediaService;
import com.woting.favorite.persis.po.UserFavoritePo;
import com.woting.passport.mobile.MobileUDKey;

@Lazy(true)
@Service
public class FavoriteService {
    @Resource
    private ChannelService channelService;
    @Resource(name="defaultDAO")
    private MybatisDAO<UserFavoritePo> userFavoriteDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<MediaAssetPo> mediaAssetDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<ChannelAssetPo> channelAssetDao;
    @Resource
    private MediaService mediaService;
    @Resource
    private ChannelContentService channelContentService;

    @PostConstruct
    public void initParam() {
        userFavoriteDao.setNamespace("DA_USERFAVORITE");
        mediaAssetDao.setNamespace("A_MEDIA");
        channelAssetDao.setNamespace("A_CHANNELASSET");
    }

    /**
     * 喜欢或取消喜欢某个内容
     * @param mediaType 内容类型
     * @param contentId 内容Id
     * @param flag 操作类型:=1喜欢；=0取消喜欢，默认=1
     * @param mk 用户标识，可以是登录用户，也可以是手机设备
     * @return 若成功返回1；若是喜欢：0=所指定的节目不存在，2=已经喜欢了此内容；若是取消喜欢：-1=还未喜欢此内容；
     *          -100——内容类型不符合要求
     */
    public int favorite(String mediaType, String contentId, int flag, MobileUDKey mUdk) {
        String CType=mediaType.toUpperCase();
        if (!CType.equals("RADIO")&&!CType.equals("AUDIO")&&!CType.equals("SEQU")&&!CType.equals("TEXT")) return -100;

        String assetType=ContentUtils.getResTableName(mediaType);
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("resTableName", assetType);
        param.put("resId", contentId);

        if (flag==1) {
            if (!channelService.isPub(assetType, contentId)) return 0;
            if (mUdk.isUser()) {
                param.put("ownerType", "201");
                param.put("ownerId", mUdk.getUserId());
            } else {
                param.put("ownerType", "202");
                param.put("ownerId", mUdk.getDeviceId());
            }
            if (userFavoriteDao.getCount(param)>0) return 2;
            param.put("id", SequenceUUID.getUUIDSubSegment(4));
            userFavoriteDao.insert(param);//加入喜欢队列
        } else {
            param.put("mobileId", mUdk.getDeviceId());
            if (mUdk.isUser()) param.put("userId", mUdk.getUserId());
            if (userFavoriteDao.getCount("getCount4Favorite", param)==0) return -1;
            //设备删除
            param.put("ownerType", "202");
            param.put("ownerId", mUdk.getDeviceId());
            userFavoriteDao.delete("deleteByEntity",param);
            //用户删除
            if (mUdk.isUser()) {
                param.put("ownerType", "201");
                param.put("ownerId", mUdk.getUserId());
                userFavoriteDao.delete("deleteByEntity",param);
            }
        }
        return 1;
    }

    /**
     * 点赞或举报某个内容
     * @param articleId 内容Id
     * @param flag 是点赞还是举报
     * @param dealType 操作类型:=1增加；=0删除，默认=1
     * @param mUdk 用户标识，可以是登录用户，也可以是手机设备
     * @return 若成功返回1；
     *    -2——举报不能删除
     *    -1——类型错误
     *     0——无对应内容
     *     2——参赛选手，只能投票(点赞)
     *     3——参赛选手，不能取消投票(点赞)
     *     4——参赛选手投票(点赞)，必须先登录
     *     5——还未点赞，不能删除
     *    60——已对参赛选手投票
     *    61——已点赞
     */
    public int favorite(String articleId, int flag, int dealType, MobileUDKey mUdk) {
        //获得文章信息
        MediaAsset ma=mediaService.getMaInfoById(articleId);
        if (ma==null) return 0;//无内容
        //参数整理
        boolean isPlayer=(ma.getMaStatus()==1);
        boolean isTraveler=mUdk.getUserId().equals("0");
        if (dealType!=0&&dealType!=1) dealType=1;

        if (isPlayer&&flag==2) return 2; //参赛选手，只能投票(点赞)
        if (isPlayer&&dealType==0) return 3; //参赛选手，不能取消投票(点赞)
        if (isPlayer&&isTraveler) return 4; //参赛选手投票(点赞)，必须先登录

        //得到现有的点赞或举报数据
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("ownerId", mUdk.getUserId());
        param.put("ownerType", "201");
        param.put("resTableName", flag+"");
        param.put("resId", articleId);

        UserFavoritePo ufPo=userFavoriteDao.getInfoObject(param);
        if (flag==1) { //点赞(投票)
            if (dealType==1) {//增加
                if (!isTraveler&&ufPo!=null) {
                    return isPlayer?60:61;
                }
                if (ufPo==null) {//新增投票/点赞
                    param.put("id", SequenceUUID.getUUIDSubSegment(4));
                    userFavoriteDao.insert(param);
                } else { //投票/点赞数加1
                    userFavoriteDao.update("increment", ufPo.getId());
                }
            } else { //删除
                if (ufPo==null) return 5;//还未点赞，不能删除
                if (ufPo.getSumNum()>0) userFavoriteDao.update("decrement", ufPo.getId());
            }
        }
        if (flag==2) { //举报，只有增加
            if (dealType==0) return -2;
            if (ufPo==null) {//新增举报
                param.put("id", SequenceUUID.getUUIDSubSegment(4));
                userFavoriteDao.insert(param);
            } else { //举报数加1
                userFavoriteDao.update("increment", ufPo.getId());
            }
        }
        return 1;
    }

    /**
     * 得到给定文章列表的点赞举报情况
     * @param articlaIds 内容Id的数组
     * @param userId 用户Id，若过客，则可以为空
     * @return 返回的是一个list，list中的元素是Map，表明每文章的情况，具体每条返回数据如下：<br/>
     * <pre>
     *    ContentId:文章Id
     *    IsFavorate:该用户是否喜欢，如果喜欢是1否则是0
     *    FavoSum:该文章被喜欢的次数
     *    IsReport:该用户是否举报，如果举报是1否则是0
     *    RepoSum:该文章被举报的次数
     * </pre>
     * 注意：若是过客：IsFavorate、IsReport都是0；若该文章未找到，FavoSum、RepoSum都是-1
     */
    public List<Map<String, Object>> getContentFavoriteInfo(String[] articlaIds, String userId) {
        if (articlaIds==null||articlaIds.length==0) return null;
        List<Map<String, Object>> ret= new ArrayList<Map<String, Object>>();
        String whereSql="";
        for (String cid: articlaIds) {
            Map<String, Object> one=new HashMap<String, Object>();
            one.put("ContentId", cid);
            one.put("IsFavorate", 0);
            one.put("FavoSum", -1);
            one.put("IsReport", 0);
            one.put("RepoSum", -1);
            ret.add(one);
            whereSql+=" or resId='"+cid+"'";
        }
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("whereByClause", whereSql.substring(4));
        List<UserFavoritePo> ufList=userFavoriteDao.queryForList("getListByWhere", param);//不排序了，这样会慢一些
        if (ufList!=null&&!ufList.isEmpty()) {
            for (UserFavoritePo ufPo: ufList) {
                //找到对应的Content
                Map<String, Object> findOne=null;
                for (Map<String, Object> o: ret) {
                    if (ufPo.getResId().equals(o.get("ContentId"))) {
                        findOne=o;
                        break;
                    }
                }
                //喜欢
                if (ufPo.getResTableName().equals("1")) {
                    if (ufPo.getOwnerId().equals(userId)) findOne.put("IsFavorate", 1);
                    findOne.put("FavoSum", (Integer)findOne.get("FavoSum")+ufPo.getSumNum());
                }
                //举报
                if (ufPo.getResTableName().equals("2")) {
                    if (ufPo.getOwnerId().equals(userId)) findOne.put("IsReport", 1);
                    findOne.put("RepoSum", (Integer)findOne.get("RepoSum")+ufPo.getSumNum());
                }
            }
        }
        //最后的调整
        for (Map<String, Object> o: ret) {
            if ((Integer)o.get("FavoSum")!=-1) o.put("FavoSum", (Integer)o.get("FavoSum")+1);
            if ((Integer)o.get("RepoSum")!=-1) o.put("RepoSum", (Integer)o.get("RepoSum")+1);
        }
        return ret;
    }

    /**
     * 获得某用户：喜欢、举报、投票的文章列表
     * @param userId 用户ID
     * @param flag 喜欢、举报、投票的标识；1=喜欢；2=举报；3=投票，默认是喜欢
     * @param page 页数
     * @param pageSize 每页条数
     * @return
     */
    public List<Map<String, Object>> getFavoriteList(String userId, int flag, int page, int pageSize) {
        if (StringUtils.isNullOrEmptyOrSpace(userId)) return null;
        if (flag!=1&&flag!=2&&flag!=3) flag=1;

        try {
            //获得列表
            Map<String, String> param=new HashMap<String, String>();
            param.put("sortByClause", "d.cTime desc");

            List<MediaAssetPo> mas=null;
            String sqlStatement="getFavoriteContents";
            param.put("whereByClause", "e.resTableName='"+flag+"' and e.ownerId='"+userId+"'");
            if (flag==3) {
                sqlStatement="getVoteContents";
                param.put("whereByClause", "a.maStatus='1' and e.ownerId='"+userId+"'");
            }

            if (page==-1) {
                mas=mediaAssetDao.queryForList(sqlStatement, param);
            } else {
                if (page==0) page=1;
                if (pageSize<0) pageSize=10;
                Page<MediaAssetPo> p=mediaAssetDao.pageQuery(sqlStatement, param, page, pageSize);//查询内容列表
                if (!p.getResult().isEmpty()) {
                    mas=new ArrayList<MediaAssetPo>();
                    mas.addAll(p.getResult());
                }
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
                List<Map<String, Object>> fsm=getContentFavoriteInfo(articlaIds, userId);

                //组织返回值
                List<Map<String, Object>> rl=new ArrayList<>();
                for (MediaAssetPo maPo : mas) {
                    MediaAsset mediaAsset = new MediaAsset();
                    mediaAsset.buildFromPo(maPo);
                    Map<String, Object> mam=ContentUtils.convert2Ma(mediaAsset.toHashMap(), null, null, chasm, fsm);
                    rl.add(mam);
                }
                return rl;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}