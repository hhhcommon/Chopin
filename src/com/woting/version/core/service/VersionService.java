package com.woting.version.core.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.spiritdata.framework.core.cache.SystemCache;
import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.core.model.Page;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.StringUtils;
import com.woting.ChopinConstants;
import com.woting.version.core.model.Version;
import com.woting.version.core.model.VersionConfig;

@Lazy(true)
@Service
public class VersionService {
    @Resource(name="defaultDAO")
    private MybatisDAO<Version> verDao;
    @Resource(name="defaultDAO")
    private MybatisDAO<VersionConfig> verCfgDao;

    @PostConstruct
    public void initParam() {
        verDao.setNamespace("P_VERSION");
        verCfgDao.setNamespace("P_VERSION");
    }

    //版本配置Begin======================================================================================================
    /**
     * 得到版本配置信息
     * @return 版本配置信息
     */
    public VersionConfig getVerConfig() {
        return verCfgDao.getInfoObject("getCfgList", null);
    }
    /**
     * 保存版本配置信息
     * @return 若保存成功返回1，否则返回2
     */
    public int saveVerConfig(VersionConfig vfg) {
        VersionConfig oldCfg=getVerConfig();
        boolean changed=
            (!StringUtils.isNullOrEmptyOrSpace(vfg.getPubUrl())&&!vfg.getPubUrl().equals(oldCfg.getPubUrl()))
          ||(!StringUtils.isNullOrEmptyOrSpace(vfg.getPubFileName())&&!vfg.getPubFileName().equals(oldCfg.getPubFileName()))
          ||(!StringUtils.isNullOrEmptyOrSpace(vfg.getPubStorePath())&&!vfg.getPubStorePath().equals(oldCfg.getPubStorePath()))
          ||(!StringUtils.isNullOrEmptyOrSpace(vfg.getVerGoodsStorePath())&&!vfg.getVerGoodsStorePath().equals(oldCfg.getVerGoodsStorePath()))
            ;
        if (!changed) return 2;

        return verCfgDao.update("updateCfg", vfg);
    }
    //版本配置End======================================================================================================

    //版本Begin======================================================================================================
    /**
     * 根据所给版本号version，获得该版本号详细信息
     * @param version 所给版本号
     * @return 版本信息，若所给版本号不存在，返回null
     */
    public Version getVersion(String version) {
        if (StringUtils.isNullOrEmptyOrSpace(version)) return null;
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("version", version);
        try {
            return verDao.getInfoObject(param);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断该版本是否是最新的版本
     * @param version
     * @return
     */
    public Map<String, Object> judgeVersion(String version) {
        Map<String, Object> retm=new HashMap<String, Object>();
        try {
            Map<String, Object> param=new HashMap<String, Object>();
            param.put("version", version);
            Version appVer=verDao.getInfoObject(param);
            Version curVer=verDao.getInfoObject("getCurrentPubVersion", null); //获取当前版本
            if (curVer==null) return null;

            boolean mastUpdate=false;
            List<String> noVersionList=new ArrayList<String>();

            int beginVerNum=(appVer==null?1:appVer.getId());
            int endVerNum=curVer.getId();
            param.clear();
            param.put("appVerId", beginVerNum);
            param.put("curVerId", endVerNum);
            List<Version> l=verDao.queryForList("getNoVersionList", param);
            if (l!=null&&!l.isEmpty()) {
                for (Version _v: l) {
                    noVersionList.add(_v.getVersion());
                    if (mastUpdate==false) mastUpdate=(_v.getVersion().indexOf(".X.")!=-1);
                }
            }

            retm.put("MastUpdate", mastUpdate?"1":"0");
            retm.put("NoVersionList", noVersionList);
            retm.put("CurVersion", curVer.toViewMap4App());
            VersionConfig vc=(SystemCache.getCache(ChopinConstants.APP_VERSIONCONFIG)==null?null:(VersionConfig)SystemCache.getCache(ChopinConstants.APP_VERSIONCONFIG).getContent());
            if (vc==null) vc=getVerConfig();
            if (vc!=null) retm.put("DownLoadUrl", vc.getPubUrl());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return retm;
    }


    /**
     * 得到所有历史的发布过的版本信息
     * @param id 相对于此id的历史版本
     * @return 历史版本信息
     */
    public List<Version> getAllPubHistVerList(int id) {
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("id", id);
        return verDao.queryForList("getAllPubHistVerList", param);
    }

    /**
     * 根据条件获得版本列表，并按页数返回
     * @param conditionMap 存放条件的Map
     * @param page 获得第几页
     * @param pageSize 每页条数
     * @return 版本列表
     */
    public Page<Version> getVersionList(Map<String, Object> conditionMap, int page, int pageSize) {
        if (page<1) page=1;
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("sortByClause", "id desc");
        //条件处理，暂不处理
        return verDao.pageQuery(param, page, pageSize);
    }

    /**
     * 判断是否能够插入：若上一个最新版本已经处在发布状态，则可以插入，若上一个最新版本未发布，则不允许发布(需要对这个版本先进行处理)
     * @return 若能插入返回1，若不能插入返回0
     */
    public int judgeInsert() {
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("isCurVer", 1);
        Version v=verDao.getInfoObject(param);
        if (v==null) return 1;
        int pubFlag=v.getPubFlag();
        if (pubFlag==1||Math.abs(pubFlag)==3) return 1;
        return 0;
    }

    /**
     * 插入新的数据
     * @param version 版本号
     * @param force 是否强制
     */
    public void insert(Version version, int force) {
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("isCurVer", 1);
        Version v=verDao.getInfoObject(param);
        boolean canInsert=false;
        if (v==null) canInsert=true;
        if (!canInsert) {
            int pubFlag=v.getPubFlag();
            if (force==1) {
                canInsert=true;
                if (pubFlag==1||pubFlag==2) v.setPubFlag(3);
                if (pubFlag==0) v.setPubFlag(-3);
                verDao.update(v);
            } else {
                if (pubFlag==1||Math.abs(pubFlag)==3) canInsert=true;
                else canInsert=false;
            }
        }

        if (canInsert) {
            param.clear();
            param.put("isCurVer", 0);
            verDao.update("updateAll", param);
            verDao.insert(version);
        }
    }

    /**
     * 修改现有数据
     * @param version 版本号
     * @param pubFlag 发布状态
     * @param descn 版本描述
     * @param bug 版本Bug修改记录
     * @return 返回值：-1无数据可处理；0没有变化，不必更新；1更新成功
     */
    public int update(Version v) {
        Map<String, Object> param=new HashMap<String, Object>();
        //得到原有的数据
        param.put("id", v.getId());
        Version _v=verDao.getInfoObject(param);
        if (_v==null) return -1;
        boolean changed=false;
        if (!changed&&!StringUtils.isNullOrEmptyOrSpace(v.getAppName())) {
            changed=!v.getAppName().equals(_v.getAppName());
        }
        if (!changed&&!StringUtils.isNullOrEmptyOrSpace(v.getVersion())) {
            changed=!v.getVersion().equals(_v.getVersion());
        }
        if (!changed&&!StringUtils.isNullOrEmptyOrSpace(v.getVerMemo())) {
            changed=!v.getVerMemo().trim().equals(_v.getVerMemo().trim());
        }
        if (!changed&&!StringUtils.isNullOrEmptyOrSpace(v.getBugMemo())) {
            changed=!v.getBugMemo().trim().equals(_v.getBugMemo().trim());
        }
        if (!changed) {
            changed=!(v.getPubFlag()==_v.getPubFlag());
        }
        //文件比较？？先不做
        if (!changed) return 0;
        verDao.update(v);
        return 1;
    }

    /**
     * 判断是否能够删除：若上一个最新版本已经处在发布状态，则可以插入，若上一个最新版本未发布，则不允许发布(需要对这个版本先进行处理)
     * @param verId 欲删除的版本的Id
     * @return 若能插入返回1，若不能插入返回0，-1没有对应的版本信息
     */
    public int judgeDel(int verId) {
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("id", verId);
        Version v=verDao.getInfoObject(param);
        if (v==null) return -1;
        if (v.getPubFlag()==0||v.getPubFlag()==-3) return 1;
        int count=verDao.getCount("newerVerCount", param);
        if (count==0) return 0;
        return 1;
    }

    /**
     * 删除一条记录
     * @param verId 欲删除的版本的Id
     * @return 返回值：-1无数据可处理；0不允许删除；1删除成功
     */
    public int delete(int verId) {
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("id", verId);
        Version v=verDao.getInfoObject(param);
        if (v==null) return -1;
        int ret=0;
        if (v.getPubFlag()==0||v.getPubFlag()==-3) ret=1;
        if (ret==0) {
            List<Version> newerVl=verDao.queryForList("newerVerList", param);
            if (newerVl!=null&&newerVl.size()!=0) {//可以删除
                Version _v=newerVl.get(0);
                String extHisPatchInfo=_v.getExtHisPatchInfo();
                List<Map<String, String>> bugL, descnL;
                if (extHisPatchInfo==null) {
                    bugL=new ArrayList<Map<String, String>>();
                    descnL=new ArrayList<Map<String, String>>();
                } else {
                    Map<String, Object> _extInfo=(Map<String, Object>)JsonUtils.jsonToObj(_v.getExtHisPatchInfo(), Map.class);
                    bugL=(List<Map<String, String>>)_extInfo.get("BugList");
                    descnL=(List<Map<String, String>>)_extInfo.get("DescnList");
                }
                Map<String, String> oneVer=v.toViewMap4View();
                Map<String, String> bugMap=new HashMap<String, String>();
                bugMap.put("Version", oneVer.get("Version"));
                bugMap.put("PubTime", oneVer.get("PubTime"));
                bugMap.put("Bug", oneVer.get("BugPatch"));
                bugL.add(bugMap);
                Map<String, String> descnMap=new HashMap<String, String>();
                bugMap.put("Version", oneVer.get("Version"));
                bugMap.put("PubTime", oneVer.get("PubTime"));
                bugMap.put("Descn", oneVer.get("Descn"));
                descnL.add(descnMap);
                extHisPatchInfo="{\"BugList\":"+JsonUtils.objToJson(bugL)+",\"DescnList\":"+JsonUtils.objToJson(descnL)+"}";
                _v.setExtHisPatchInfo(extHisPatchInfo);
                verDao.update(_v);
                ret=1;
            }
        }
        if (ret==1) verDao.delete(param);
        return ret;
    }

    /**
     * 更改版本发布状态
     * @param verId 欲更改的版本的Id
     * @param operType 操作类型
     * @return 100成功修改， 101没有对应的版本信息， 状态值（原先版本的状态值）说明不能从该源状态改为要更改的状态
     */
    public int changeFlag(int verId, int operType) {
        Map<String, Object> param=new HashMap<String, Object>();
        param.put("id", verId);
        Version v=verDao.getInfoObject(param);
        if (v==null) return 101;
        int _oldFlag=v.getPubFlag();
        if (operType==1) {//改为发布
            if (Math.abs(_oldFlag)==3) return _oldFlag;
            param.put("PubTime", new Timestamp(System.currentTimeMillis()));
            param.put("PubFlag", operType);
        } else if (operType==2) {//撤销发布
            if (_oldFlag!=1) return _oldFlag;
            param.put("PubFlag", operType);
        } else if (operType==3) {//作废
            param.put("PubFlag", (_oldFlag==0?-3:3));
        }
        verDao.update(param);
        return 100;
    }
    //版本End======================================================================================================

    /**
     * 判断版本号是否符合要求
     * @param version 版本号
     * @param verId 若是新增verId=-1;若是修改verId=所修改的版本的Id
     * @return 1符合要求;2版本域问题;3版本号小;4编译号小
     */
    public int validateVer(String version, int verId) {
        if (!isValiVerOfDomain(version)) return 2;
        String vd[]=version.split("\\.");

        Map<String, Object> param=new HashMap<String, Object>();
        if (verId!=-1) param.put("lessId", verId);

        Version v=verDao.getInfoObject("getCmpVer", param);
        while (v!=null&&!isValiVerOfDomain(v.getVersion())) {
            param.put("lessId", v.getId());
            v=verDao.getInfoObject("getCmpVer", param);
        }
        if (v==null) return 1;
        String _vd[]=(v.getVersion()).split("\\.");
        if (Integer.parseInt(_vd[4])>Integer.parseInt(vd[4])) return 4;
        if (Integer.parseInt(_vd[0])>Integer.parseInt(vd[0])) return 3;
        if (Integer.parseInt(_vd[0])<Integer.parseInt(vd[0])) return 1;
        if (Integer.parseInt(_vd[1])>Integer.parseInt(vd[1])) return 3;
        if (Integer.parseInt(_vd[1])<Integer.parseInt(vd[1])) return 1;
        if (Integer.parseInt(_vd[2])>Integer.parseInt(vd[2])) return 3;
        if (Integer.parseInt(_vd[2])<Integer.parseInt(vd[2])) return 1;
        return 1;
    }
    /*
     * 版本号的域规则是否符合
     * @param version 版本号
     * @return true符合
     */
    private boolean isValiVerOfDomain(String version) {
        if (StringUtils.isNullOrEmptyOrSpace(version)) return false;
        String vd[]=version.split("\\.");
        if (vd.length!=5) return false;
        if (vd[3].length()!=1) return false;
        char c=vd[3].toCharArray()[0];
        if (!(c>='A'&&c<='Z')) return false;
        try { Integer.parseInt(vd[0]); } catch(Exception e) {return false;}
        try { Integer.parseInt(vd[1]); } catch(Exception e) {return false;}
        try { Integer.parseInt(vd[2]); } catch(Exception e) {return false;}
        try { Integer.parseInt(vd[4]); } catch(Exception e) {return false;}
        return true;
    }
    //版本End======================================================================================================
}