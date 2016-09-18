package com.woting.version.manage.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spiritdata.framework.core.model.Page;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.StringUtils;
import com.spiritdata.framework.util.RequestUtils;
import com.woting.passport.mobile.MobileParam;
import com.woting.passport.mobile.MobileUDKey;
import com.woting.version.core.model.Version;
import com.woting.version.core.model.VersionConfig;
import com.woting.version.core.service.VersionService;

@Lazy(true)
@Controller
@RequestMapping(value="/version/")
public class VersionController {
    private VersionConfig verCfg=null;
    @Resource
    private VersionService verService;

    @RequestMapping(value="getVerCfg.do")
    @ResponseBody
    public Map<String,Object> getVerCfg(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获取版本配置信息
            if (this.verCfg==null) verCfg=verService.getVerConfig();
            if (verCfg==null) {
                map.put("ReturnType", "1011");
                map.put("Message", "获得版本配置信息为空");
            } else {
                map.put("ReturnType", "1001");
                map.put("VerCfgInfo", verCfg.toHashMap4View());
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

    @RequestMapping(value="saveVerCfg.do")
    @ResponseBody
    public Map<String,Object> updateVerCfg(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            VersionConfig vfg=new VersionConfig();
            if (m!=null&&m.get("PubUrl")!=null&&!StringUtils.isNullOrEmptyOrSpace(m.get("PubUrl")+"")) vfg.setPubUrl(m.get("PubUrl")+"");
            if (m!=null&&m.get("PubFileName")!=null&&!StringUtils.isNullOrEmptyOrSpace(m.get("PubFileName")+"")) vfg.setPubFileName(m.get("PubFileName")+"");
            if (m!=null&&m.get("PubStorePath")!=null&&!StringUtils.isNullOrEmptyOrSpace(m.get("PubStorePath")+"")) vfg.setPubStorePath(m.get("PubStorePath")+"");
            if (m!=null&&m.get("VerGoodsStorePath")!=null&&!StringUtils.isNullOrEmptyOrSpace(m.get("VerGoodsStorePath")+"")) vfg.setVerGoodsStorePath(m.get("VerGoodsStorePath")+"");
            if (m==null
               || (StringUtils.isNullOrEmptyOrSpace(vfg.getPubUrl())
                 &&StringUtils.isNullOrEmptyOrSpace(vfg.getPubFileName())
                 &&StringUtils.isNullOrEmptyOrSpace(vfg.getPubStorePath())
                 &&StringUtils.isNullOrEmptyOrSpace(vfg.getVerGoodsStorePath()))
               ) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获得有用参数");
                return map;
            }
            int result=verService.saveVerConfig(vfg);
            if (result!=1&&result!=2) throw new Exception("未知问题，无法保存");
            if (result==1) {
                map.put("ReturnType", "1001");
                if (this.verCfg==null) verCfg=verService.getVerConfig();
                else {
                    if (!StringUtils.isNullOrEmptyOrSpace(vfg.getPubUrl())) this.verCfg.setPubUrl(vfg.getPubUrl());
                    if (!StringUtils.isNullOrEmptyOrSpace(vfg.getPubFileName())) this.verCfg.setPubFileName(vfg.getPubFileName());
                    if (!StringUtils.isNullOrEmptyOrSpace(vfg.getPubStorePath())) this.verCfg.setPubStorePath(vfg.getPubStorePath());
                    if (!StringUtils.isNullOrEmptyOrSpace(vfg.getVerGoodsStorePath())) this.verCfg.setVerGoodsStorePath(vfg.getVerGoodsStorePath());
                }
            } else {
                map.put("ReturnType", "1002");
                map.put("Message", "未进行任何修改，无需保存");
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

    @RequestMapping(value="getVersionInfo.do")
    @ResponseBody
    public Map<String,Object> getVersionInfo(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m==null) m=new HashMap<String, Object>();
            String version=m.get("Version")==null?null:m.get("Version")+"";
            //2-业务处理：获得版本信息
            Version v=verService.getVersion(version);
            //3-处理返回值
            if (v==null) {
                map.put("ReturnType", "1011");
                map.put("Message", "该版本号无对应版本信息");
            } else {
                map.put("ReturnType", "1001");
                Map<String, String> vViewMap=v.toViewMap4View();
                if (v.getIsCurVer()==1&&v.getPubFlag()>0) {
                    if (this.verCfg==null) verCfg=verService.getVerConfig();
                    vViewMap.put("PubUrl", verCfg.getPubUrl());
                }
                map.put("VersionInfo", vViewMap);
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

    @RequestMapping(value="getVerDescnHistory.do")
    @ResponseBody
    public Map<String,Object> getVerDescnHistory(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            String version=m.get("Version")==null?null:m.get("Version")+"";
            //2-业务处理：获得版本信息
            Version v=verService.getVersion(version);
            List<Map<String, String>> bugHistList=new ArrayList<Map<String, String>>();
            List<Map<String, String>> descnHistList=new ArrayList<Map<String, String>>();
            if (v!=null) {
                Map<String, String> oneVer=v.toViewMap4View();
                Map<String, String> oneBug=new HashMap<String, String>();
                oneBug.put("Version", oneVer.get("Version"));
                oneBug.put("PubTime", oneVer.get("PubTime"));
                oneBug.put("Bug", v.getBugMemo()==null?"":v.getBugMemo());
                bugHistList.add(oneBug);
                Map<String, String> oneDescn=new HashMap<String, String>();
                oneDescn.put("Version", oneVer.get("Version"));
                oneDescn.put("PubTime", oneVer.get("PubTime"));
                oneDescn.put("Descn", v.getVerMemo()==null?"":v.getVerMemo());
                descnHistList.add(oneDescn);
                
                List<Version> allVerList=verService.getAllPubHistVerList(v.getId());
                if (allVerList!=null&&!allVerList.isEmpty()) {
                    for (Version _v: allVerList) {
                        //处理本身
                        oneVer=_v.toViewMap4View();
                        oneBug=new HashMap<String, String>();
                        oneBug.put("Version", oneVer.get("Version"));
                        oneBug.put("PubTime", oneVer.get("PubTime"));
                        oneBug.put("Bug", _v.getBugMemo()==null?"":_v.getBugMemo());
                        bugHistList.add(oneBug);
                        oneDescn=new HashMap<String, String>();
                        oneDescn.put("Version", oneVer.get("Version"));
                        oneDescn.put("PubTime", oneVer.get("PubTime"));
                        oneDescn.put("Descn", _v.getVerMemo()==null?"":_v.getVerMemo());
                        descnHistList.add(oneDescn);
                        //处理extHisPatchInfo
                        if (!StringUtils.isNullOrEmptyOrSpace(_v.getExtHisPatchInfo())) {
                            try {
                                Map<String, Object> _extInfo=(Map<String, Object>)JsonUtils.jsonToObj(_v.getExtHisPatchInfo(), Map.class);
                                if (_extInfo.get("BugList")!=null) {
                                    bugHistList.addAll((List<Map<String, String>>)_extInfo.get("BugList"));
                                }
                                if (_extInfo.get("DescnList")!=null) {
                                    descnHistList.addAll((List<Map<String, String>>)_extInfo.get("DescnList"));
                                }
                            } catch(Exception e) {
                            }
                        }
                    }
                }
            }
            //3-处理返回值
            if (v==null) {
                map.put("ReturnType", "1011");
                map.put("Message", "该版本号无对应版本信息");
            } else {
                map.put("ReturnType", "1001");
                Map<String, Object> resultMap=new HashMap<String, Object>();
                resultMap.putAll(v.toViewMap4View());
                resultMap.remove("Descn");
                resultMap.remove("BugPatch");
                resultMap.remove("StoreFile");
                resultMap.remove("IsCur");
                resultMap.remove("ApkSize");
                resultMap.remove("LastModifyTime");
                resultMap.remove("CreateTime");
                resultMap.put("BugPatch", bugHistList);
                resultMap.put("Descn", descnHistList);
                map.put("VerDescnBugs", resultMap);
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

    @RequestMapping(value="getVerList.do")
    @ResponseBody
    public Map<String,Object> getVerList(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            String condition=(m==null?"":(m.get("QueryCondition")==null?null:m.get("QueryCondition")+""));
            Map<String, Object> conditionMap=null;
            if (!StringUtils.isNullOrEmptyOrSpace(condition)) conditionMap=(Map<String, Object>)JsonUtils.jsonToObj(condition, Map.class);//得到每页条数
            int page=1;
            if (m!=null&&m.get("Page")!=null) {
                try {page=Integer.parseInt(m.get("Page")+"");} catch(Exception e) {};
            }
            int pageSize=10;
            if (m!=null&&m.get("PageSize")!=null) {
                try {pageSize=Integer.parseInt(m.get("PageSize")+"");} catch(Exception e) {};
            }
            int toLast=0;
            if (m!=null&&m.get("GotoLast")!=null) {
                try {toLast=Integer.parseInt(m.get("GotoLast")+"");} catch(Exception e) {};
            }

            //2-业务处理：获得版本信息
            if (toLast==1) page=1;
            Page<Version> p=verService.getVersionList(conditionMap, page, pageSize);
            if (toLast==1) {
                int lastPage=p.getDataCount()/pageSize;
                lastPage+=p.getDataCount()%pageSize==0?0:1;
                page=lastPage;
                p=verService.getVersionList(conditionMap, page, pageSize);
            }
            if (p.getDataCount()>0&&p.getResult().size()==0) {//页数超出了，到最后一页
                int lastPage=p.getDataCount()/pageSize;
                lastPage+=p.getDataCount()%pageSize==0?0:1;
                if (page>lastPage) {
                    page=lastPage;
                    p=verService.getVersionList(conditionMap, page, pageSize);
                }
            }

            //3-处理返回值
            if (p==null||p.getDataCount()==0||p.getResult().isEmpty()) {
                map.put("ReturnType", "1011");
                map.put("Message", "无对应的版本列表");
            } else {
                map.put("ReturnType", "1001");
                Map<String, Object> resultMap=new HashMap<String, Object>();
                resultMap.put("AllCount",p.getDataCount()+"");
                resultMap.put("CurPage",p.getPageIndex()+"");
                resultMap.put("PageSize",p.getPageSize()+"");
                List<Map<String, String>> verList=new ArrayList<Map<String, String>>();
                for (Version _v:p.getResult()) {
                    Map<String, String> oneV=_v.toViewMap4View();
                    if (_v.getIsCurVer()==1&&_v.getPubFlag()>0) {
                        if (this.verCfg==null) verCfg=verService.getVerConfig();
                        oneV.put("PubUrl", verCfg.getPubUrl());
                    }
                    verList.add(oneV);
                }
                resultMap.put("VerList", verList);
                map.put("ResultList", resultMap);
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

    @RequestMapping(value="allowInsert.do")
    @ResponseBody
    public Map<String,Object> allowInsert(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数：不需要
            //2-业务处理：获得版本信息，并处理返回值
            map.put("ReturnType", "1001");
            map.put("AllowInsert", verService.judgeInsert());
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    @RequestMapping(value="insertVer.do")
    @ResponseBody
    public Map<String,Object> insertVersion(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            //版本
            String version=m.get("Version")==null?null:m.get("Version")+"";
            String appName=m.get("AppName")==null?null:m.get("AppName")+"";
            //版本状态
            int pubFlag=0;
            if (m.get("PubFlag")!=null) {
                try {pubFlag=Integer.parseInt(m.get("PubFlag")+"");} catch(Exception e) {};
            }
            //是否强制
            int force=0;
            if (m.get("Force")!=null) {
                try {force=Integer.parseInt(m.get("Force")+"");} catch(Exception e) {};
            }
            //说明
            String descn=m.get("Descn")==null?null:m.get("Descn")+"";
            //bug记录
            String bug=m.get("BugPatch")==null?null:m.get("BugPatch")+"";

            //2-业务处理
            //2.1-参数是否符合规则
            if (StringUtils.isNullOrEmptyOrSpace(descn) //参数是否都获得齐全
              ||StringUtils.isNullOrEmptyOrSpace(version)
              ||StringUtils.isNullOrEmptyOrSpace(appName)) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获得有用参数");
                return map;
            }
            int validateVer=verService.validateVer(version, -1); //版本号是否符合规则
            if (validateVer!=1) {
                if (validateVer==2) {
                    map.put("ReturnType", "2001");
                    map.put("Message", "版本号不合法，版本域不符合规定");
                } else if (validateVer==3) {
                    map.put("ReturnType", "2002");
                    map.put("Message", "版本号不合法，版本号小于之前的版本");
                } else if (validateVer==4) {
                    map.put("ReturnType", "2003");
                    map.put("Message", "版本号不合法，编译号小于之前的版本");
                }
                return map;
            }
            if (force==0&&verService.judgeInsert()==0) {//判断是否可以加入
                map.put("ReturnType", "1002");
                map.put("Message", "还有未发布的版本，不能新增版本");
                return map;
            }
            if (force==1) {//作废之前未发布的版本
                //TODO
            }
            //2.2-插入方法调用
            Version v=new Version();
            v.setAppName(appName);
            v.setVersion(version);
            v.setVerMemo(descn);
            v.setBugMemo(bug);
            v.setPubFlag(pubFlag);
            v.setApkFile("文件名，不包括路径");//待改
            v.setApkSize(234);//待改
            v.setIsCurVer(1);

            verService.insert(v,force);

            map.put("ReturnType", "1001");
            map.put("Message", "新增成功");
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    @RequestMapping(value="updateVer.do")
    @ResponseBody
    public Map<String,Object> updateVersion(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            //Id
            int verId=-1;
            if (m.get("VerId")!=null) {
                try {verId=Integer.parseInt(m.get("VerId")+"");} catch(Exception e) {};
            }
            //版本
            String version=m.get("Version")==null?null:m.get("Version")+"";
            String appName=m.get("AppName")==null?null:m.get("AppName")+"";
            //版本状态
            int pubFlag=0;
            if (m.get("PubFlag")!=null) {
                try {pubFlag=Integer.parseInt(m.get("PubFlag")+"");} catch(Exception e) {};
            }
            //说明
            String descn=m.get("Descn")==null?null:m.get("Descn")+"";
            //bug记录
            String bug=m.get("BugPatch")==null?null:m.get("BugPatch")+"";

            //2-业务处理
            //2.1-参数是否符合规则
            if (StringUtils.isNullOrEmptyOrSpace(descn) //参数是否都获得齐全
              ||StringUtils.isNullOrEmptyOrSpace(version)
              ||StringUtils.isNullOrEmptyOrSpace(appName)
              ||verId==-1) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获得有用参数");
                return map;
            }
            int validateVer=verService.validateVer(version, verId); //版本号是否符合规则
            if (validateVer!=1) {
                if (validateVer==2) {
                    map.put("ReturnType", "2001");
                    map.put("Message", "版本号不合法，版本域不符合规定");
                } else if (validateVer==3) {
                    map.put("ReturnType", "2002");
                    map.put("Message", "版本号不合法，版本号小于之前的版本");
                } else if (validateVer==4) {
                    map.put("ReturnType", "2003");
                    map.put("Message", "版本号不合法，编译号小于之前的版本");
                }
                return map;
            }
            //2.2-修改方法调用
            Version v=new Version();
            v.setId(verId);
            v.setAppName(appName);
            v.setVersion(version);
            v.setVerMemo(descn);
            v.setBugMemo(bug);
            v.setPubFlag(pubFlag);
            v.setApkFile("文件名，不包括路径");
            v.setApkSize(234);
            v.setIsCurVer(1);

            int ret=verService.update(v);
            if (ret==-1) {
                map.put("ReturnType", "1002");
                map.put("Message", "没有对应的版本信息，无法修改");
            } else if (ret==0) {
                map.put("ReturnType", "1003");
                map.put("Message", "未进行任何修改，无需保存");
            } else {
                map.put("ReturnType", "1001");
                map.put("Message", "修改成功");
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

    @RequestMapping(value="allowDel.do")
    @ResponseBody
    public Map<String,Object> allowDel(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            //Id
            int verId=-1;
            if (m.get("VerId")!=null) {
                try {verId=Integer.parseInt(m.get("VerId")+"");} catch(Exception e) {};
            }
            if (verId==-1) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获得有用参数");
                return map;
            }
            //2-业务处理：获得版本信息，并处理返回值
            map.put("ReturnType", "1001");
            map.put("AllowDel", verService.judgeDel(verId));
            return map;
        } catch(Exception e) {
            e.printStackTrace();
            map.put("ReturnType", "T");
            map.put("TClass", e.getClass().getName());
            map.put("Message", e.getMessage());
            return map;
        }
    }

    @RequestMapping(value="delVer.do")
    @ResponseBody
    public Map<String,Object> delVer(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            //Id
            int verId=-1;
            if (m.get("VerId")!=null) {
                try {verId=Integer.parseInt(m.get("VerId")+"");} catch(Exception e) {};
            }
            if (verId==-1) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获得有用参数");
                return map;
            }

            //2-业务处理
            int ret=verService.delete(verId);
            if (ret==-1)  {
                map.put("ReturnType", "1002");
                map.put("Message", "没有对应的版本信息，无法删除");
            } else if (ret==0) {
                map.put("ReturnType", "1003");
                map.put("Message", "本版本不允许被删除");
            } else {
                map.put("ReturnType", "1001");
                map.put("Message", "删除成功");
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

    @RequestMapping(value="chgFlag.do")
    @ResponseBody
    public Map<String,Object> chgFlag(HttpServletRequest request) {
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            //0-判断权限：目前没有这个功能
            //1-获得参数
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            //Id
            int verId=-1;
            if (m.get("VerId")!=null) {
                try {verId=Integer.parseInt(m.get("VerId")+"");} catch(Exception e) {};
            }
            //操作类型OperType
            int operType=-1;
            if (m.get("OperType")!=null) {
                try {operType=Integer.parseInt(m.get("OperType")+"");} catch(Exception e) {};
            }

            //2-业务处理
            //2.1-参数是否符合规则
            if (verId==-1||operType==-1) {
                map.put("ReturnType", "0000");
                map.put("Message", "无法获得有用参数");
                return map;
            }
            //2.2-更改状态
            int ret=verService.changeFlag(verId, operType);
            if (ret==-1)  {
                map.put("ReturnType", "1002");
                map.put("Message", "没有对应的版本信息，无法删除");
            } else if (ret==0) {
                map.put("ReturnType", "1003");
                map.put("Message", "本版本不允许被删除");
            } else {
                map.put("ReturnType", "1001");
                map.put("Message", "删除成功");
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

    @RequestMapping(value="judgeVersion.do")
    @ResponseBody
    public Map<String,Object> judgeVersion(HttpServletRequest request) {//不需要登录
        Map<String,Object> map=new HashMap<String, Object>();
        try {
            Map<String, Object> m=RequestUtils.getDataFromRequest(request);
            if (m!=null&&m.size()>0) {
                MobileUDKey mUdk=MobileParam.build(m).getUserDeviceKey();
                if (mUdk!=null) map.putAll(mUdk.toHashMapAsBean());

                //1-获取App版本号
                String version=m.get("Version")==null?null:(m.get("Version")+"");
                if (StringUtils.isNullOrEmptyOrSpace(version)) {
                    map.put("ReturnType", "1002");
                    map.put("Message", "无法获得版本号信息");
                    return map;
                }

                //2-判断并获取版本
                m=verService.judgeVersion(version);
                if (m==null) {
                    map.put("ReturnType", "1003");
                    map.put("Message", "无法获得当前发布版本");
                } else {
                    map.put("ReturnType", "1001");
                    map.putAll(m);
                }
            }
            map.put("ServerStatus", "1");
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