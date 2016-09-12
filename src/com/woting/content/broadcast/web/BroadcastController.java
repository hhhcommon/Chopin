package com.woting.content.broadcast.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spiritdata.framework.util.RequestUtils;
import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.core.model.Page;
import com.spiritdata.framework.ui.tree.EasyUiTree;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.StringUtils;
import com.woting.WtContentMngConstants;
import com.woting.cm.core.channel.mem._CacheChannel;
import com.woting.cm.core.channel.model.Channel;
import com.woting.cm.core.dict.mem._CacheDictionary;
import com.woting.cm.core.dict.model.DictDetail;
import com.woting.cm.core.dict.model.DictModel;
import com.woting.cm.core.dict.persis.po.DictRefResPo;
import com.woting.content.broadcast.service.BroadcastService;

@Controller
@RequestMapping(value="/content/bc/")
public class BroadcastController {
    @Resource
    private BroadcastService bcService;

    @RequestMapping(value="add.do")
    @ResponseBody
    public Map<String,Object> addBroadcast(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        Map<String, Object> m=RequestUtils.getDataFromRequest(request);
        bcService.add(m);
        map.put("returnType","1001");
        return map;
    }
    @RequestMapping(value="update.do")
    @ResponseBody
    public Map<String,Object> updateBroadcast(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        Map<String, Object> m=RequestUtils.getDataFromRequest(request);
        bcService.update(m);
        map.put("returnType","1001");
        return map;
    }

    @RequestMapping(value="loadBc.do")
    @ResponseBody
    public Page<Map<String,Object>> loadBc(HttpServletRequest request) {
        Page<Map<String,Object>> _p=new Page<Map<String, Object>>();
        Map<String, Object> m=RequestUtils.getDataFromRequest(request);
        _p = bcService.getViewList(m);
        
        Collection<Map<String,Object>> retResult=_p.getResult();
        if (retResult!=null&&retResult.size()>0) {
            String ids="";
            for (Map<String,Object> one: retResult) {//此次扫描，得到所有的Id
                ids+=",'"+one.get("id")+"'";
            }
            List<DictRefResPo> rcrpL = bcService.getCataRefList(ids.substring(1));
            if (rcrpL!=null&&rcrpL.size()>0) {
                for (Map<String,Object> one: retResult) {//此次扫描，填充数据
                    ids=""+one.get("id");
                    String areaName="", typeName="";
                    boolean up=false, down=false;
                    for (int i=0; i<rcrpL.size(); i++) {
                        if (up&&down) break;
                        DictRefResPo rcrp=rcrpL.get(i);
                        if (rcrp.getResId().equals(ids)) {
                            if (!up) up=true;
//                            if (rcrp.getDictMid().equals("1")) typeName+=","+rcrp.getTitle();
//                            else if (rcrp.getDictMid().equals("2")) areaName+=","+rcrp.getPathNames();
                            if (i==rcrpL.size()-1) down=true;
                        } else {
                            if (up) down=true;
                        }
                    }
                    if (up&&down) {
                        one.put("typeName", (StringUtils.isNullOrEmptyOrSpace(typeName)?"":typeName.substring(1)));
                        one.put("areaName", (StringUtils.isNullOrEmptyOrSpace(areaName)?"":areaName.substring(1)));
                    }
                }
            }
        }
        System.out.println(JsonUtils.objToJson(_p));
        return _p;
    }

    @RequestMapping(value="delBc.do")
    @ResponseBody
    public Map<String,Object> delBc(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        Map<String, Object> m=RequestUtils.getDataFromRequest(request);
        bcService.del(m.get("ids")+"");
        return map;
    }

    @RequestMapping(value="getCataTrees4View.do")
    @ResponseBody
    //这是一个临时方法
    public Map<String,Object> getCataTrees4View(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
        @SuppressWarnings("unchecked")
		CacheEle<_CacheDictionary> cache=((CacheEle<_CacheDictionary>)SystemCache.getCache(WtContentMngConstants.CACHE_DICT));
        if (cache==null) {
            map.put("jsonType", "0");
            map.put("data", "没有数据");
        } else {
            try {
                _CacheDictionary _cd = ((CacheEle<_CacheDictionary>)SystemCache.getCache(WtContentMngConstants.CACHE_DICT)).getContent();
                DictModel dm=_cd.getDictModelById("1");
                EasyUiTree<DictDetail> eu1=new EasyUiTree<DictDetail>(dm.dictTree);
                l.add(eu1.toTreeMap());
                dm=_cd.getDictModelById("2");
                eu1=new EasyUiTree<DictDetail>(dm.dictTree);
                l.add(eu1.toTreeMap());
                dm=_cd.getDictModelById("3");
                eu1=new EasyUiTree<DictDetail>(dm.dictTree);
                l.add(eu1.toTreeMap());
                _CacheChannel _cc=((CacheEle<_CacheChannel>)SystemCache.getCache(WtContentMngConstants.CACHE_CHANNEL)).getContent();
                EasyUiTree<Channel> eu2=new EasyUiTree<Channel>(_cc.channelTree);
                l.add(eu2.toTreeMap());
                map.put("jsonType", "1");
                map.put("data", l);
            } catch (CloneNotSupportedException e) {
                map.put("jsonType", "2");
                map.put("err", e.getMessage());
                e.printStackTrace();
            }
        }
        return map;
    }

    @RequestMapping(value="getInfo.do")
    @ResponseBody
    public Map<String,Object> getInfo(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        Map<String, Object> m=RequestUtils.getDataFromRequest(request);
        m=bcService.getBroadcastInfo(m.get("bcId")+"");
        if (m==null) {
            map.put("returnType","1002");
        } else {
            map.put("bcInfo", m);
            map.put("returnType","1001");
        }
        return map;
    }
    
    @RequestMapping(value="getBcList.do")
    @ResponseBody
    public Map<String,Object> getBcList(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        Map<String, Object> m=RequestUtils.getDataFromRequest(request);
        String userid = m.get("UserId")+"";
        String page = m.get("Page")+"";
        String pagesize = m.get("PageSize")+"";
        String errMsg = "";
        
        System.out.println(userid+"#"+page+"#"+pagesize);
        
        if(userid.toLowerCase().equals("null")) errMsg+=",无用户信息";
        if(page.toLowerCase().equals("null")) errMsg+=",无页码信息";
        if(pagesize.toLowerCase().equals("null")) errMsg+=",无每页条数信息";
        if (!StringUtils.isNullOrEmptyOrSpace(errMsg)) {
            errMsg=errMsg.substring(1);
            map.put("ReturnType", "1002");
            map.put("Message", errMsg);
            return map;
        }
        int pagenum = Integer.valueOf(page);
        int pagesizenum = Integer.valueOf(pagesize);
        List<Map<String, Object>> bclist = bcService.getBroadcastListInfo(pagenum, pagesizenum);
        
        if(bclist==null) {
        	map.put("ReturnType", "1002");
        	map.put("Message", "无数据");
        	return map;
        }
        map.put("ReturnType", "1001");
        map.put("ResultList", bclist);
        map.put("AllCount", bclist.size());
        return map;
    }
    
    @RequestMapping(value="getSQLList.do")
    @ResponseBody
    public Map<String,Object> getSQLList(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        
        
        
//       
//        List<Map<String, Object>> bclist = bcService.getBroadcastListInfo(pagenum, pagesizenum);
//        
//        if(bclist==null) {
//        	map.put("ReturnType", "1002");
//        	map.put("Message", "无数据");
//        	return map;
//        }
//        map.put("ReturnType", "1001");
//        map.put("ResultList", bclist);
//        map.put("AllCount", bclist.size());
        return map;
    }
}