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
import com.woting.WtContentMngConstants;
import com.woting.cm.core.dict.mem._CacheDictionary;
import com.woting.cm.core.dict.model.DictDetail;
import com.woting.cm.core.dict.model.DictModel;
import com.woting.cm.core.dict.service.DictService;

/**
 * 分类(字典)信息前台控制
 * @author wanghui
 */
@Controller
@RequestMapping(value="/baseinfo/")
public class CatalogController {
    @Resource
    private DictService dictService;

    /**
     * 为前台显示获取分类(字典树)
     */
    @RequestMapping(value="getCataTree4View.do")
    @ResponseBody
    public Map<String,Object> getCataTree4View(HttpServletRequest request) {
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
            String catalogType=(m.get("CatalogType")==null?null:m.get("CatalogType")+"");
            String catalogId=(m.get("CatalogId")==null?null:m.get("CatalogId")+"");
            String treeType=(m.get("TreeViewType")==null?"ZTREE":(m.get("TreeViewType")+"").toUpperCase());
            int sizeLimit=(m.get("SizeLimit")==null?0:Integer.parseInt(m.get("SizeLimit")+""));

            if (StringUtils.isNullOrEmptyOrSpace(catalogType)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[CatalogType]");
                return map;
            }
            if (!(treeType.equals("ZTREE")||treeType.equals("EASYUITREE"))) {
                map.put("ReturnType", "1003");
                map.put("Message", "未知的显示树类型["+m.get("TreeViewType")+"]");
                return map;
            }

            _CacheDictionary _cd=((CacheEle<_CacheDictionary>)SystemCache.getCache(WtContentMngConstants.CACHE_DICT)).getContent();
            try {
                DictModel dm=_cd.getDictModelById(catalogType);
                TreeNode<DictDetail> root=dm.dictTree;
                if (!StringUtils.isNullOrEmptyOrSpace(catalogId)) {
                    root=(TreeNode<DictDetail>)root.findNode(catalogId);
                }
                if (root==null) {
                    map.put("ReturnType", "1002");
                    map.put("Message", "未找到分类");
                    return map;
                }
                UiTree<DictDetail> uiTree=null;
                if (root.getAllCount()<sizeLimit||sizeLimit==0) {
                    if (treeType.equals("ZTREE")) uiTree=new ZTree<DictDetail>(root);
                    else
                    if (treeType.equals("EASYUITREE")) uiTree=new EasyUiTree<DictDetail>(root);
                } else {
                    if (treeType.equals("ZTREE")) uiTree=new ZTree<DictDetail>(root, sizeLimit);
                    else
                    if (treeType.equals("EASYUITREE")) uiTree=new EasyUiTree<DictDetail>(root, sizeLimit);
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
     * 添加一个分类
     */
    @RequestMapping(value="addCatalog.do")
    @ResponseBody
    public Map<String,Object> addCatalog(HttpServletRequest request) {
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
            String catalogType=(m.get("CatalogType")==null?null:m.get("CatalogType")+"");
            String catalogId=(m.get("CatalogId")==null?null:m.get("CatalogId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(catalogType)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[CatalogType]");
                return map;
            }
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
            //1-组织参数
            DictDetail dd=new DictDetail(); //字典项业务对象
            dd.setMId(catalogType);
            if (!StringUtils.isNullOrEmptyOrSpace(catalogId)) dd.setParentId(catalogId);
            dd.setDdName(name);
            dd.setAliasName((data.get("AliasName")==null?null:data.get("AliasName")+""));
            dd.setBCode((data.get("BCode")==null?null:data.get("BCode")+""));
            try {
                dd.setOrder((data.get("Sort")==null?0:Integer.parseInt(data.get("Sort")+"")));
            } catch (Exception e) {
                map.put("ReturnType", "0000");
                map.put("Message", "参数[Sort]需要是整数，当前Sort值为["+data.get("Sort")+"]");
                return map;
            }
            dd.setDesc((data.get("Descn")==null?null:data.get("Descn")+""));
            try {
                int validate=(data.get("Validate")==null?1:Integer.parseInt(data.get("Validate")+""));
                dd.setIsValidate(validate>2&&validate<1?1:validate); //默认是生效的
            } catch (Exception e) {
                map.put("ReturnType", "0000");
                map.put("Message", "参数[Validate]需要是整数，当前Validate值为["+data.get("Validate")+"]");
                return map;
            }
            //2-加入字典项
            String ret=dictService.insertDictDetail(dd);
            if (ret.equals("2")) {
                map.put("ReturnType", "1002");
                map.put("Message", "未找到分类");
            } else if (ret.equals("3")) {
                map.put("ReturnType", "1002");
                map.put("Message", "未找到父结点");
            } else if (ret.equals("4")) {
                map.put("ReturnType", "1003");
                map.put("Message", "名称重复");
            } else if (ret.equals("5")) {
                map.put("ReturnType", "1004");
                map.put("Message", "bCode重复");
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
     * 修改一个分类
     */
    @RequestMapping(value="updateCatalog.do")
    @ResponseBody
    public Map<String,Object> updateCatalog(HttpServletRequest request) {
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
            String catalogType=(m.get("CatalogType")==null?null:m.get("CatalogType")+"");
            String catalogId=(m.get("CatalogId")==null?null:m.get("CatalogId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(catalogType)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[CatalogType]");
                return map;
            }
            if (StringUtils.isNullOrEmptyOrSpace(catalogId)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[CatalogId]");
                return map;
            }
            Map<String,Object> data=(Map<String,Object>)m.get("Data");
            if (data==null||data.isEmpty()) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[Data]");
                return map;
            }
            //1-组织参数
            DictDetail dd=new DictDetail(); //字典项业务对象
            dd.setMId(catalogType);
            dd.setId(catalogId);
            dd.setDdName(data.get("Name")==null?null:data.get("Name")+"");;
            dd.setParentId((data.get("ParentId")==null?null:data.get("ParentId")+""));
            dd.setAliasName((data.get("AliasName")==null?null:data.get("AliasName")+""));
            dd.setBCode((data.get("BCode")==null?null:data.get("BCode")+""));
            try {
                dd.setOrder((data.get("Sort")==null?0:Integer.parseInt(data.get("Sort")+"")));
            } catch (Exception e) {
                map.put("ReturnType", "0000");
                map.put("Message", "参数[Sort]需要是整数，当前Sort值为["+data.get("Sort")+"]");
                return map;
            }
            try {
                int validate=(data.get("Validate")==null?0:Integer.parseInt(data.get("Validate")+""));
                dd.setIsValidate(validate>2&&validate<1?1:validate); //默认是生效的
            } catch (Exception e) {
                map.put("ReturnType", "0000");
                map.put("Message", "参数[Validate]需要是整数，当前Validate值为["+data.get("Validate")+"]");
                return map;
            }
            dd.setDesc((data.get("Descn")==null?null:data.get("Descn")+""));
            //2-修改字典项
            int ret=dictService.updateDictDetail(dd);
            if (ret==1) {
                map.put("ReturnType", "1001");
            } else if (ret==2) {
                map.put("ReturnType", "1002");
                map.put("Message", "未找到分类");
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
     * 删除一个分类
     */
    @RequestMapping(value="delCatalog.do")
    @ResponseBody
    public Map<String,Object> delCatalog(HttpServletRequest request) {
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
            String catalogType=(m.get("CatalogType")==null?null:m.get("CatalogType")+"");
            if (StringUtils.isNullOrEmptyOrSpace(catalogType)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[CatalogType]");
                return map;
            }
            String catalogId=(m.get("CatalogId")==null?null:m.get("CatalogId")+"");
            if (StringUtils.isNullOrEmptyOrSpace(catalogId)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获取需要的参数[CatalogId]");
                return map;
            }
            //默认是不允许强制删除的
            boolean force=(m.get("Force")==null?0:Integer.parseInt(m.get("Force")+""))==1;
            //1-组织参数
            DictDetail dd=new DictDetail(); //字典项业务对象
            dd.setMId(catalogType);
            dd.setId(catalogId);
            //2-删除典项
            String ret=dictService.delDictDetail(dd, force);
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