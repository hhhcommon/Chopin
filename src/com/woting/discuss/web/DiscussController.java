package com.woting.discuss.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spiritdata.framework.util.StringUtils;
import com.woting.discuss.model.Discuss;
import com.woting.discuss.service.DiscussService;
import com.woting.passport.UGA.service.UserService;
import com.spiritdata.framework.util.RequestUtils;
import com.woting.passport.UGA.persistence.pojo.UserPo;
import com.woting.passport.mobile.MobileParam;
import com.woting.passport.mobile.MobileUDKey;
import com.woting.passport.session.SessionService;

@Controller
@RequestMapping(value="/discuss/")
public class DiscussController {
    @Resource
    private DiscussService discussService;
    @Resource
    private UserService userService;
    @Resource(name="redisSessionService")
    private SessionService sessionService;

    /**
     * 提交文章评论
     * @param request
     * @return
     */
    @RequestMapping(value="add.do")
    @ResponseBody
    public Map<String,Object> add(HttpServletRequest request) {
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
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "discuss/add");
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

            //2-获取意见
            String opinion=(m.get("Opinion")==null?null:m.get("Opinion")+"");
            if (StringUtils.isNullOrEmptyOrSpace(opinion)) {
                map.put("ReturnType", "1003");
                map.put("Message", "无法评论内容");
                return map;
            }
            //3-获取文章Id
            String articalId=(m.get("ContentId")==null?null:m.get("ContentId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(articalId)) {
                map.put("ReturnType", "1004");
                map.put("Message", "无法获取文章Id");
                return map;
            }
            //4-存储意见
            try {
                Discuss discuss=new Discuss();
                discuss.setImei(mUdk.getDeviceId());
                discuss.setUserId(userId);
                discuss.setArticalId(articalId);
                discuss.setOpinion(opinion);
                //是否重复提交意见
                List<Discuss> duplicates=discussService.getDuplicates(discuss);
                if (duplicates!=null&&duplicates.size()>0) {
                    map.put("ReturnType", "1005");
                    map.put("Message", "该评论已经提交");
                    return map;
                };
                discussService.insertDiscuss(discuss);
            } catch(Exception ei) {
                map.put("ReturnType", "1006");
                map.put("Message", ei.getMessage());
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
     * 删除文章评论
     * @param request
     * @return
     */
    @RequestMapping(value="del.do")
    @ResponseBody
    public Map<String,Object> del(HttpServletRequest request) {
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
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "discuss/del");
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

            //2-评论Id
            String discussId=(m.get("DiscussId")==null?null:m.get("DiscussId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(discussId)) {
                map.put("ReturnType", "1003");
                map.put("Message", "无法获取评论Id");
                return map;
            }
            //3-删除意见
            try {
                Discuss discuss=new Discuss();
                discuss.setImei(mUdk.getDeviceId());
                discuss.setUserId(userId);
                discuss.setId(discussId);
                int flag=discussService.delDiscuss(discuss);
                if (flag==-1) {
                    map.put("ReturnType", "1004");
                    map.put("Message", "无对应评论，无法删除");
                } else if (flag==-2) {
                    map.put("ReturnType", "1005");
                    map.put("Message", "无权删除");
                } else if (flag==0) {
                    map.put("ReturnType", "1006");
                    map.put("Message", "删除失败");
                } else {
                    map.put("ReturnType", "1001");
                }
           } catch(Exception ei) {
                map.put("ReturnType", "1006");
                map.put("Message", ei.getMessage());
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

    @RequestMapping(value="article/getList.do")
    @ResponseBody
    public Map<String,Object> getList(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        com.spiritdata.framework.core.web.InitSysConfigListener cc;
        try {
            //0-获取参数
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
                sessionService.dealUDkeyEntry(mUdk, "discuss/article/getList");
            }

            //1-获取文章Id
            String articalId=(m.get("ContentId")==null?null:m.get("ContentId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(articalId)) {
                map.put("ReturnType", "1004");
                map.put("Message", "无法获取文章Id");
                return map;
            }
            //2-获取分页信息
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

            List<Discuss> ol=discussService.getArticleDiscusses(articalId, page, pageSize);
            if (ol!=null&&ol.size()>0) {
                map.put("ReturnType", "1001");
                map.put("OpinionList", convertDiscissView(ol));
            } else {
                map.put("ReturnType", "1011");
                map.put("Message", "无意见及反馈信息");
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

    /**
     * 获得返回的列表，包括用户的信息
     * @param ol
     * @return
     */
    private List<Map<String, Object>> convertDiscissView(List<Discuss> disList) {
        //得到用户列表
        List<String> userIds=new ArrayList<String>();
        Map<String, String> userMap=new HashMap<String, String>();
        for (Discuss d: disList) {
            if (d.getUserId()!=null||!d.getUserId().equals("0")) {
                userMap.put(d.getUserId(), d.getUserId());
            }
        }
        if (userMap!=null&&!userMap.isEmpty()) {
            for (String key: userMap.keySet()) {
                userIds.add(key);
            }
        }

        List<UserPo> ul=userService.getUserByIds(userIds);
        List<Map<String, Object>> ret=new ArrayList<Map<String, Object>>();

        Map<String, Object> oneDiscuss=null;
        for (Discuss d: disList) {
            oneDiscuss=d.toHashMap4Mobile();
            if (oneDiscuss!=null) {
                if (d.getUserId()!=null||!d.getUserId().equals("0")) {
                    for (UserPo up: ul) {
                        if (up.getUserId().equals(d.getUserId())) {
                            oneDiscuss.put("UserInfo", up.toHashMap4Mobile());
                            break;
                        }
                    }
                }
                ret.add(oneDiscuss);
            }
        }
        return ret==null||ret.isEmpty()?null:ret;
    }
}