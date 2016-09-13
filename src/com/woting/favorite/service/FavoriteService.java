package com.woting.favorite.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.utils.ContentUtils;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.media.model.MediaAsset;
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
    @Resource
    private MediaService mediaService;

    @PostConstruct
    public void initParam() {
        userFavoriteDao.setNamespace("DA_USERFAVORITE");
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
     * @param articalId 内容Id
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
     */
    public int favorite(String articalId, int flag, int dealType, MobileUDKey mUdk) {
        //获得文章信息
        MediaAsset ma=mediaService.getMaInfoById(articalId);
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
        param.put("resId", articalId);

        UserFavoritePo ufPo=userFavoriteDao.getInfoObject(param);
        if (flag==1) { //点赞(投票)
            if (dealType==1) {//增加
                
            } else { //删除
                if (ufPo==null) return 5;//还未点赞，不能删除
                //判断是否允许删除
                
            }
        }
        if (flag==2) { //举报，只有增加
            if (dealType==0) return -2;
            if (ufPo==null) {//新增举报
                param.put("id", SequenceUUID.getUUIDSubSegment(4));
                userFavoriteDao.insert(param);
            } else { //举报数加1
                userFavoriteDao.update("increme", ufPo.getId());
            }
        }
        return -1;
    }
}