package com.woting.content.basicinfo.web;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spiritdata.framework.core.cache.CacheEle;
import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.core.model.tree.TreeNode;
import com.spiritdata.framework.ui.tree.EasyUiTree;
import com.spiritdata.framework.ui.tree.UiTree;
import com.spiritdata.framework.ui.tree.ZTree;
import com.spiritdata.framework.util.RequestUtils;
import com.spiritdata.framework.util.StringUtils;
import com.woting.ChopinConstants;
import com.woting.cm.core.channel.mem._CacheChannel;
import com.woting.cm.core.channel.model.Channel;
import com.woting.cm.core.channel.service.ChannelService;
import com.woting.cm.core.common.model.Owner;

/**
 * 栏目信息前台控制
 * @author wanghui
 */
@Controller
@RequestMapping(value="/baseinfo/")
public class ChannelController {
    @Resource
    ChannelService channelService;

    /**
     * 为前台显示获取栏目
     */
    @RequestMapping(value="getChannelTree4View.do")
    @ResponseBody
    public Map<String,Object> getChannelTree4View(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //权限管理，目前不做
            //0-获取参数
            String channelId=null;
            String treeType="ZTREE";
            int sizeLimit=0;
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m!=null&&m.size()>0) {
                channelId=(m.get("ChannelId")==null?null:m.get("ChannelId")+"");
                treeType=(m.get("TreeViewType")==null?"ZTREE":(m.get("TreeViewType")+"").toUpperCase());
                sizeLimit=(m.get("SizeLimit")==null?0:Integer.parseInt(m.get("SizeLimit")+""));
            }

            if (!(treeType.equals("ZTREE")||treeType.equals("EASYUITREE"))) {
                map.put("ReturnType", "1003");
                map.put("Message", "未知的显示树类型["+m.get("TreeViewType")+"]");
                return map;
            }

            _CacheChannel _cc=((CacheEle<_CacheChannel>)SystemCache.getCache(ChopinConstants.CACHE_CHANNEL)).getContent();
            try {
                TreeNode<Channel> root=_cc.channelTree;
                if (!StringUtils.isNullOrEmptyOrSpace(channelId)) {
                    root=_cc.channelTreeMap.get(channelId);
                }
                if (root==null) {
                    map.put("ReturnType", "1002");
                    map.put("Message", "未找到对应栏目");
                    return map;
                }
                UiTree<Channel> uiTree=null;
                if (root.getAllCount()<sizeLimit||sizeLimit==0) {
                    if (treeType.equals("ZTREE")) uiTree=new ZTree<Channel>(root);
                    else
                    if (treeType.equals("EASYUITREE")) uiTree=new EasyUiTree<Channel>(root);
                } else {
                    if (treeType.equals("ZTREE")) uiTree=new ZTree<Channel>(root, sizeLimit);
                    else
                    if (treeType.equals("EASYUITREE")) uiTree=new EasyUiTree<Channel>(root, sizeLimit);
                }
                if (uiTree!=null) {
                    map.put("ReturnType", "1001");
                    map.put("Data", uiTree.toTreeMap());
                } else {
                    map.put("ReturnType", "1011");
                }
            } catch (CloneNotSupportedException e) {
                map.put("jsonType", "2");
                map.put("err", e.getMessage());
                e.printStackTrace();
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
     * 添加一个栏目
     */
    @RequestMapping(value="addChannel.do")
    @ResponseBody
    public Map<String,Object> addChannel(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            //权限管理，目前不做
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "参数解释错误");
                return map;
            }
            String channelId=(m.get("ChannelId")==null?null:m.get("ChannelId")+"");
            Map<String,Object> data=(Map<String,Object>)m.get("Data");
            if (data==null||data.isEmpty()) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[Data]");
                return map;
            }
            String name=(data.get("Name")==null?null:data.get("Name")+"");
            if (StringUtils.isNullOrEmptyOrSpace(name)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[Name]");
                return map;
            }
            Map<String, Object> owner=(data.get("Owner")==null?null:(Map)data.get("Owner"));
            if (owner==null||owner.get("OwnerType")==null||owner.get("OwnerId")==null) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[Owner]");
                return map;
            }
            try {
                Integer.parseInt(owner.get("OwnerType")+"");
            } catch(Exception e) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[owner]");
                return map;
            }
            //1-组织参数
            Channel c=new Channel(); //栏目业务对象
            if (!StringUtils.isNullOrEmptyOrSpace(channelId)) c.setParentId(channelId);
            c.setChannelName(name);
            Owner o=new Owner(Integer.parseInt(owner.get("OwnerType")+""), owner.get("OwnerId")+"");
            c.setOwner(o);
            try {
                int validate=(data.get("Validate")==null?1:Integer.parseInt(data.get("Validate")+""));
                c.setIsValidate(validate>2&&validate<1?1:validate); //默认是生效的
            } catch (Exception e) {
                map.put("ReturnType", "0000");
                map.put("Message", "参数[Validate]需要是整数，当前Validate值为["+data.get("Validate")+"]");
                return map;
            }
            c.setOrder((data.get("Sort")==null?0:Integer.parseInt(data.get("Sort")+"")));
            c.setContentType((data.get("ContentType")==null?"0":data.get("ContentType")+""));
            c.setDescn((data.get("Descn")==null?null:data.get("Descn")+""));
            //2-加入字典项
            String ret=channelService.insertChannel(c);
            if (ret.equals("2")) {
                map.put("ReturnType", "1002");
                map.put("Message", "未找到父亲结点");
            } else if (ret.equals("3")) {
                map.put("ReturnType", "1003");
                map.put("Message", "名称重复");
            } else if (ret.indexOf("err:")==0) {
                map.put("ReturnType", "T");
                map.put("Message", ret.substring(ret.indexOf("err:")+4));
            } else {
                map.put("ReturnType", "1001");
                map.put("CatagoryId", ret);
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
     * 修改一个栏目
     */
    @RequestMapping(value="updateChannel.do")
    @ResponseBody
    public Map<String,Object> updateChannel(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            //权限管理，目前不做
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "参数解释错误");
                return map;
            }
            String channelId=(m.get("ChannelId")==null?null:m.get("ChannelId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(channelId)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[ChannelId]");
                return map;
            }
            Map<String,Object> data=(Map<String,Object>)m.get("Data");
            if (data==null||data.isEmpty()) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[Data]");
                return map;
            }
            //1-组织参数
            Channel c=new Channel(); //栏目业务对象
            c.setId(channelId);
            c.setParentId((data.get("ParentId")==null?null:data.get("ParentId")+""));
            c.setChannelName(data.get("Name")==null?null:data.get("Name")+"");
            Map<String, Object> owner=(data.get("Owner")==null?null:(Map)data.get("Owner"));
            if (owner!=null&&owner.get("OwnerType")!=null&&owner.get("OwnerId")!=null) {
                try {
                    Owner o=new Owner(Integer.parseInt(owner.get("OwnerType")+""), owner.get("OwnerId")+"");
                    c.setOwner(o);
                } catch(Exception e) {
                }
            }
            try {
                int validate=(data.get("Validate")==null?0:Integer.parseInt(data.get("Validate")+""));
                c.setIsValidate(validate>2&&validate<1?1:validate); //默认是生效的
            } catch (Exception e) {
                map.put("ReturnType", "0000");
                map.put("Message", "参数[Validate]需要是整数，当前Validate值为["+data.get("Validate")+"]");
                return map;
            }
            c.setOrder((data.get("Sort")==null?0:Integer.parseInt(data.get("Sort")+"")));
            c.setContentType((data.get("ContentType")==null?"0":data.get("ContentType")+""));
            c.setDescn((data.get("Descn")==null?null:data.get("Descn")+""));
            //2-修改字典项
            int ret=channelService.updateChannel(c);
            if (ret==1) {
                map.put("ReturnType", "1001");
            } else if (ret==2) {
                map.put("ReturnType", "1002");
                map.put("Message", "未找到栏目");
            } else if (ret==3) {
                map.put("ReturnType", "1002");
                map.put("Message", "未找到对应分类项");
            } else if (ret==4) {
                map.put("ReturnType", "1003");
                map.put("Message", "名称重复");
            } else if (ret==5) {
                map.put("ReturnType", "1004");
                map.put("Message", "bCode重复");
            } else if (ret==6) {
                map.put("ReturnType", "1005");
                map.put("Message", "与原信息相同，不必修改");
            } else {
                map.put("ReturnType", "T");
                map.put("Message", "未知异常");
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
     * 删除一个栏目
     */
    @RequestMapping(value="delChannel.do")
    @ResponseBody
    public Map<String,Object> delChannel(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-获取参数
            //权限管理，目前不做
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null||m.size()==0) {
                map.put("ReturnType", "0000");
                map.put("Message", "参数解释错误");
                return map;
            }
            String channelId=(m.get("ChannelId")==null?null:m.get("ChannelId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(channelId)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[ChannelId]");
                return map;
            }
            //默认是不允许强制删除的
            boolean force=(m.get("Force")==null?0:Integer.parseInt(m.get("Force")+""))==1;
            //1-组织参数
            Channel c=new Channel(); //字典项业务对象
            c.setId(channelId);
            //2-删除典项
            String ret=channelService.delChannel(c, force);
            if (ret.equals("1")) {
                map.put("ReturnType", "1001");
            } else if (ret.equals("2")) {
                map.put("ReturnType", "1002");
                map.put("Message", "未找到对应结点");
            } else {
                String s[]=ret.split("::");
                if (s.length!=2||!s[0].equals("3")) {
                    map.put("ReturnType", "T");
                    map.put("Message", "未知异常");
                } else {
                    map.put("ReturnType", "1003");
                    map.put("Message", s[1]);
                }
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