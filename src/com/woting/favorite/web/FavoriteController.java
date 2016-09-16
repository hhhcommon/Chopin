package com.woting.favorite.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spiritdata.framework.util.RequestUtils;
import com.spiritdata.framework.util.StringUtils;
import com.woting.favorite.service.FavoriteService;
import com.woting.passport.mobile.MobileParam;
import com.woting.passport.mobile.MobileUDKey;
import com.woting.passport.session.SessionService;

@Controller
public class FavoriteController {
    @Resource(name="redisSessionService")
    private SessionService sessionService;
    @Resource
    private FavoriteService favoriteService;

    /**
     * 点赞或举报
     * @param request
     * @return
     */
    @RequestMapping(value="clickFavorite.do")
    @ResponseBody
    public Map<String,Object> clickFavorite(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            String userId="";
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "clickFavorite");
                if ((retM.get("ReturnType")+"").equals("2001")) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                } else {
                    //处理过客
                    if ((retM.get("ReturnType")+"").equals("2003")) {
                        mUdk.setUserId("0");
                    }
                    map.putAll(mUdk.toHashMapAsBean());
                    userId=mUdk.getUserId();
                    //注意这里可以写日志了
                }
                if (map.get("ReturnType")==null&&StringUtils.isNullOrEmptyOrSpace(userId)) {
                    map.put("ReturnType", "1002");
                    map.put("Message", "无法获取用户Id");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //3-获取文章Id
            String articleId=(m.get("ContentId")==null?null:m.get("ContentId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(articleId)) {
                map.put("ReturnType", "1003");
                map.put("Message", "无法获取文章Id");
                return map;
            }
            //4-获取类型
            int flag=-1;
            try {
                flag=Integer.parseInt(m.get("Flag")==null?null:m.get("Flag")+"");
            } catch(Exception e) {
            }
            if (flag!=1&&flag!=2) {
                map.put("ReturnType", "1005");
                map.put("Message", "无法获得分类");
                return map;
            }
            //4-获取类型
            int dealType=1;
            try {
                dealType=Integer.parseInt(m.get("DealType")==null?null:m.get("DealType")+"");
            } catch(Exception e) {
            }
            if (dealType!=1&&dealType!=0) dealType=1;
            if (dealType==0&&flag==2) {
                map.put("ReturnType", "1006");
                map.put("Message", "举报无法删除");
                return map;
            }
            flag=favoriteService.favorite(articleId, flag, dealType, mUdk);
            if (flag==-2) {
                map.put("ReturnType", "1006");
                map.put("Message", "举报无法删除");
                return map;
            }
            if (flag==-1) {
                map.put("ReturnType", "1005");
                map.put("Message", "无法获得分类");
                return map;
            }
            if (flag==0) {
                map.put("ReturnType", "1004");
                map.put("Message", "所指定的内容不存在");
                return map;
            }
            if (flag==2) {
                map.put("ReturnType", "2001");
                map.put("Message", "参赛选手，只能投票");
                return map;
            }
            if (flag==3) {
                map.put("ReturnType", "2002");
                map.put("Message", "参赛选手，不能取消投票");
                return map;
            }
            if (flag==4) {
                map.put("ReturnType", "2003");
                map.put("Message", "参赛选手投票，必须先登录");
                return map;
            }
            if (flag==5) {
                map.put("ReturnType", "1007");
                map.put("Message", "还未点赞，不能删除");
                return map;
            }
            if (flag==60) {
                map.put("ReturnType", "2004");
                map.put("Message", "您已投票");
                return map;
            }
            if (flag==61) {
                map.put("ReturnType", "1008");
                map.put("Message", "您已点赞");
                return map;
            }
            map.put("ReturnType", "1001");
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    /**
     * 得到某人的点赞或举报过的文章列表
     * @param request
     * @return
     */
    @RequestMapping(value="/user/getFavoriteList.do")
    @ResponseBody
    public Map<String,Object> getFavoriteList(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //获取参数
            String userId="";
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                if (StringUtils.isNullOrEmptyOrSpace(mUdk.getDeviceId())) { //是PC端来的请求
                    mUdk.setDeviceId(request.getSession().getId());
                }
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "user/getFavoriteList.do");
                if ((retM.get("ReturnType")+"").equals("2001")) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                } else if ((retM.get("ReturnType")+"").equals("2003")) {
                    map.put("ReturnType", "200");
                    map.put("Message", "需要登录");
                } else {
                    if (mUdk.isUser()) userId=mUdk.getUserId();
                }
                if (map.get("ReturnType")==null&&StringUtils.isNullOrEmptyOrSpace(userId)) {
                    map.put("ReturnType", "1002");
                    map.put("Message", "无法获取用户Id");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //获取分页信息
            int page=-1;
            try {
                page=Integer.parseInt(m.get("Page")==null?null:m.get("Page")+"");
            } catch(Exception e) {
            }
            int pageSize=10;
            try {
                page=Integer.parseInt(m.get("PageSize")==null?null:m.get("PageSize")+"");
            } catch(Exception e) {
            }

            //获取分类信息
            int flag=1;
            try {
                flag=Integer.parseInt(m.get("Flag")==null?null:m.get("Flag")+"");
            } catch(Exception e) {
            }

            List<Map<String, Object>> fl=favoriteService.getFavoriteList(mUdk.getUserId(), flag, page, pageSize);

            if (fl!=null&&fl.size()>0) {
                map.put("ReturnType", "1001");
                map.put("ContentList", fl);
            } else {
                map.put("ReturnType", "1011");
                map.put("Message", "无用户喜欢列表");
            }
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }
}