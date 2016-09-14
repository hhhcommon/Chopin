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

import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.ui.tree.ZTree;
import com.spiritdata.framework.util.StringUtils;
import com.woting.WtContentMngConstants;
import com.woting.cm.core.channel.mem._CacheChannel;
import com.woting.cm.core.channel.model.Channel;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.dict.mem._CacheDictionary;
import com.woting.cm.core.dict.model.DictDetail;
import com.woting.cm.core.dict.model.DictModel;
import com.spiritdata.framework.util.RequestUtils;

import org.jsoup.nodes.Document;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

@Controller
@RequestMapping(value="/common/")
public class CommonController {
	@Resource
	private ChannelService channelService;

	/**
	 * 获取内容分类树
	 * @param request
	 * @return
	 */
    @RequestMapping(value="getCataTreeWithSelf.do")
    @ResponseBody
    public Map<String,Object> getCataTreeWithSelf(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        Map<String, Object> m=RequestUtils.getDataFromRequest(request);
        String cataId=(String)m.get("cataId");
        if (!StringUtils.isNullOrEmptyOrSpace(cataId)&&!cataId.equals("null")) {
            @SuppressWarnings("unchecked")
			_CacheDictionary _cd=( (CacheEle<_CacheDictionary>)SystemCache.getCache(WtContentMngConstants.CACHE_DICT)).getContent();
            try {
                DictModel dm=_cd.getDictModelById(cataId);
                ZTree<DictDetail> eu1=new ZTree<DictDetail>(dm.dictTree);
                map.put("jsonType", "1");
                map.put("data", eu1.toTreeMap());
            } catch (Exception e) {
                map.put("jsonType", "2");
                map.put("err", e.getMessage());
                e.printStackTrace();
            }
        } else {
            map.put("jsonType", "2");
            map.put("err", "无法得到正确的分类Id");
        }
        return map;
    }

    /**
     * 获取栏目列表
     * @param request
     * @return
     */
    @RequestMapping(value="getChannelTreeWithSelf.do")
    @ResponseBody
    public Map<String,Object> getChannelTreeWithSelf(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
         _CacheChannel _cd=channelService.loadCache();
         try {
			ZTree<Channel> zc = new ZTree<>(_cd.channelTree);
			map.put("jsonType", "1");
            map.put("data", zc.toTreeMap());
		} catch (Exception e) {
			map.put("jsonType", "2");
            map.put("err", e.getMessage());
			e.printStackTrace();
		}
        return map;
    }

    @RequestMapping(value="jsonp.do")
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
        Document doc=conn.timeout(5000).ignoreContentType(true).get();

        String str=doc.select("body").html().toString();
        str=str.replaceAll("\"", "'");
        str=str.replaceAll("\n", "");
        str=str.replaceAll("&quot;", "\"");
        str=str.replaceAll("\r", "");

        return str;
    }
}