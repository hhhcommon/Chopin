package com.woting.content.common.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spiritdata.framework.util.StringUtils;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.common.model.Owner;
import com.woting.content.manage.media.service.MediaContentService;
import com.woting.gather.GatherUtils;
import com.woting.passport.UGA.service.UserService;
import com.woting.passport.mobile.MobileParam;
import com.woting.passport.mobile.MobileUDKey;
import com.woting.passport.session.SessionService;
import com.woting.searchword.service.WordService;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.RequestUtils;
import org.jsoup.nodes.Document;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

@Controller
@RequestMapping(value="")
public class CommonController {
    @Resource
    private ChannelService channelService;
    @Resource
    private UserService userService;
    @Resource(name="redisSessionService")
    private SessionService sessionService;
    @Resource
    private WordService wordService;
    @Resource
    private MediaContentService mediaContentService;

    /**
     * 进入App
     * @param request 请求对象。数据包含在Data流中，以json格式存储，其中必须包括手机串号。如：{"imei":"123456789023456789"}
     * @return 分为如下情况<br/>
     *   若有异常：{ReturnType:T, TClass:exception.class, Message: e.getMessage()}
     *   已经登录：{ReturnType:1001, userInfo:{userName:un, mphone:138XXXX2345, email:a@b.c, realName:实名, headImg:hiUrl}}
     *     其中用户信息若没有相关内容，则相关的key:value对就不存在
     *   还未登录：{ReturnType:1002}
     */
    @RequestMapping(value="/common/entryApp.do")
    @ResponseBody
    public Map<String,Object> entryApp(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            MobileUDKey mUdk=null;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数");
                return map;
            } else {
                mUdk=MobileParam.build(m).getUserDeviceKey();
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "common/entryApp");
                if ((retM.get("ReturnType")+"").equals("2001")) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                } else {
                    if ((retM.get("ReturnType")+"").equals("1002")) {
                        map.put("ReturnType", "1002");
                    } if ((retM.get("ReturnType")+"").equals("2002")) {
                        map.put("ReturnType", "2002");
                        map.put("Message", "无法找到相应的用户");
                    }else {
                        map.put("ReturnType", "1001");
                        if (retM.get("UserInfo")!=null) map.put("UserInfo", retM.get("UserInfo"));
                    }
                }
                map.put("ServerStatus", "1"); //服务器状态
                map.put("IsExtension", "0"); //是否推广
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

    @RequestMapping(value="/searchByText.do")
    @ResponseBody
    public Map<String,Object> searchByText(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
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
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "searchByText");
                if ((retM.get("ReturnType")+"").equals("2001")) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                } else {
                    //处理过客
                    if ((retM.get("ReturnType")+"").equals("2003")||(retM.get("ReturnType")+"").equals("2002")) {
                        mUdk.setUserId("0");
                    }
                }
            }
            if (map.get("ReturnType")!=null) return map;
            
            //获得查询串
            String searchStr=(m.get("SearchStr")==null?null:m.get("SearchStr")+"");
            if (StringUtils.isNullOrEmptyOrSpace(searchStr)) {
                map.put("ReturnType", "1002");
                map.put("Message", "无法得到查询串");
                return map;
            }

            //敏感词处理
            Owner o=new Owner(201, mUdk.getUserId());
            String _s[]=searchStr.split(",");
            for (int i=0; i<_s.length; i++) wordService.addWord2Online(_s[i].trim(), o);

            //获取分页信息
            int page=-1;
            try {
                page=Integer.parseInt(m.get("Page")==null?null:m.get("Page")+"");
            } catch(Exception e) {
            }
            int pageSize=10;
            try {
                pageSize=Integer.parseInt(m.get("PageSize")==null?null:m.get("PageSize")+"");
            } catch(Exception e) {
            }

            Map<String, Object> cl=mediaContentService.searchByText(searchStr, page, pageSize, mUdk);

            if (cl!=null&&cl.size()>0) {
                map.put("ResultType", cl.get("ResultType"));
                cl.remove("ResultType");
                map.put("ResultList", cl);
                map.put("ReturnType", "1001");
            } else {
                map.put("ReturnType", "1011");
                map.put("Message", "没有查到任何内容");
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
     * 得到当前的活跃的热词
     * @param request
     * @return
     */
    @RequestMapping(value="/getHotKeys.do")
    @ResponseBody
    public Map<String,Object> getHotKeys(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
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
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "getHotKeys");
                if ((retM.get("ReturnType")+"").equals("2001")) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                }
            }
            m.put("ObjType", 10);//敏感词
            //m.put("ObjId", request.getRequestURL().toString());//id
            m.put("V_Url", request.getRequestURL().toString());//URL
            m.put("AllParam", JsonUtils.objToJson(m));
            GatherUtils.SaveLogFromAPI(mUdk, m);
            if (map.get("ReturnType")!=null) return map;

            //1-获取功能类型，目前只有1内容搜索
            int funType=1;
            try {funType=Integer.parseInt(m.get("FunType")+"");} catch(Exception e) {}
            //2-检索词数量
            int wordSize=10;
            try {wordSize=Integer.parseInt(m.get("WordSize")+"");} catch(Exception e) {}
            //3-返回类型
            int returnType=1;
            try {returnType=Integer.parseInt(m.get("ReturnType")+"");} catch(Exception e) {}

            Owner o=new Owner(201, mUdk.getUserId());
            List<String>[] retls=wordService.getHotWords(o, returnType, wordSize);
            if (retls==null||retls.length==0) map.put("ReturnType", "1011");
            else {
                if (retls.length==1&&(retls[0]==null||retls[0].size()==0)) map.put("ReturnType", "1011");
                else 
                if (retls.length==2&&(retls[0]==null||retls[0].size()==0)&&(retls[1]==null||retls[1].size()==0)) map.put("ReturnType", "1011");
                else 
                if (retls.length>2)  map.put("ReturnType", "1011");
            }
            if (map.get("ReturnType")!=null) return map;

            String tempStr="";
            List<String> tempWords=null;
            if (retls.length==1) {
                tempWords=retls[0];
                for (String word: tempWords) tempStr+=","+word;
                map.put("KeyList", tempStr.substring(1));
            } else if (retls.length==2) {
                tempStr="";
                if ((retls[0]!=null&&retls[0].size()>0)) {
                    tempWords=retls[0];
                    for (String word: tempWords) tempStr+=","+word;
                    map.put("SysKeyList", tempStr.substring(1));
                }
                tempStr="";
                if ((retls[1]!=null&&retls[1].size()>0)) {
                    tempWords=retls[1];
                    for (String word: tempWords) tempStr+=","+word;
                    map.put("MyKeyList", tempStr.substring(1));
                }
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
     * 目前不处理群组查找热词
     * 在搜索框输入字符串时所使用的接口
     */
    @RequestMapping(value="/searchHotKeys.do")
    @ResponseBody
    public Map<String,Object> searchHotKeys(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
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
                Map<String, Object> retM=sessionService.dealUDkeyEntry(mUdk, "searchHotKeys");
                if ((retM.get("ReturnType")+"").equals("2001")) {
                    map.put("ReturnType", "0000");
                    map.put("Message", "无法获取设备Id(IMEI)");
                }
            }
            if (map.get("ReturnType")!=null) return map;

            //获得查找词
            String searchStr=(m.get("KeyWord")==null?null:m.get("KeyWord")+"");
            if (StringUtils.isNullOrEmptyOrSpace(searchStr)) {
                map.put("ReturnType", "1002");
                map.put("Message", "无法得到查询串");
                return map;
            }
            //1-获取功能类型，目前只有1内容搜索
            int funType=1;
            try {funType=Integer.parseInt(m.get("FunType")+"");} catch(Exception e) {}
            //2-检索词数量
            int wordSize=10;
            try {wordSize=Integer.parseInt(m.get("WordSize")+"");} catch(Exception e) {}
            //3-返回类型
            int returnType=1;
            try {returnType=Integer.parseInt(m.get("ReturnType")+"");} catch(Exception e) {}

            Owner o=new Owner(201, mUdk.getUserId());
            List<String>[] retls=wordService.searchHotWords(searchStr, o, returnType, wordSize);
            if (retls==null||retls.length==0) map.put("ReturnType", "1011");
            else {
                if (retls.length==1&&(retls[0]==null||retls[0].size()==0)) map.put("ReturnType", "1011");
                else 
                if (retls.length==2&&(retls[0]==null||retls[0].size()==0)&&(retls[1]==null||retls[1].size()==0)) map.put("ReturnType", "1011");
                else 
                if (retls.length>2)  map.put("ReturnType", "1011");
            }
            if (map.get("ReturnType")!=null) return map;

            String tempStr="";
            List<String> tempWords=null;
            if (retls.length==1) {
                tempWords=retls[0];
                for (String word: tempWords) tempStr+=","+word;
                map.put("KeyList", tempStr.substring(1));
            } else if (retls.length==2) {
                tempStr="";
                if ((retls[0]!=null&&retls[0].size()>0)) {
                    tempWords=retls[0];
                    for (String word: tempWords) tempStr+=","+word;
                    map.put("SysKeyList", tempStr.substring(1));
                }
                tempStr="";
                if ((retls[1]!=null&&retls[1].size()>0)) {
                    tempWords=retls[1];
                    for (String word: tempWords) tempStr+=","+word;
                    map.put("MyKeyList", tempStr.substring(1));
                }
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

    @RequestMapping(value="/common/jsonp.do")
    @ResponseBody
    /**
     * 用jsonP的方式获取数据
     * @param request 其中必须有RemoteUrl参数
     * @return json结构
     */
    public String jsonp(HttpServletRequest request) throws IOException {
        //获取参数
        Map<String, Object> m=RequestUtils.getDataFromRequest(request);
        //1-获取地址：
        String remoteUrl=m.get("RemoteUrl")+"";
        if (StringUtils.isNullOrEmptyOrSpace(remoteUrl)||remoteUrl.toLowerCase().equals("null")) {
            return "{\"ReturnType\":\"0000\",\"Message\":\"无法获得远程Url\"}";
        }
        m.remove("RemoteUrl");
        Connection conn=Jsoup.connect(remoteUrl);
        for (String key: m.keySet()) {
            if (m.get(key)!=null) conn.data(key, m.get(key)+"");
        }
        try {
            Document doc=conn.timeout(5000).ignoreContentType(true).get();
            String str=doc.select("body").html().toString();
            str=str.replaceAll("\"", "'");
            str=str.replaceAll("\n", "");
            str=str.replaceAll("&quot;", "\"");
            str=str.replaceAll("\r", "");
            return str;
        } catch(Exception e) {
            return "{\"ReturnType\":\"1000\",\"Message\":\"地址错误\"}";
        }
    }
}