package com.woting.discuss.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.discuss.model.Discuss;
import com.woting.discuss.persis.pojo.DiscussPo;

public class DiscussService {
    @Resource(name="defaultDAO")
    private MybatisDAO<DiscussPo> discussDao;

    @PostConstruct
    public void initParam() {
        discussDao.setNamespace("WT_DISCUSS");
    }

    /**
     * 得到意见
     * @param opinion 意见信息
     * @return 创建用户成功返回1，否则返回0
     */
    public List<DiscussPo> getDuplicates(DiscussPo opinion) {
        try {
            return discussDao.queryForList("getDuplicates", opinion.toHashMapAsBean());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存用户所提意见
     * @param opinion 意信息
     * @return 创建用户成功返回1，否则返回0
     */
    public int insertOpinion(DiscussPo opinion) {
        int i=0;
        try {
            opinion.setId(SequenceUUID.getUUIDSubSegment(4));
            discussDao.insert(opinion);
            i=1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * 根据用户指标（userId或Imei）得到意见及反馈列表
     * @param userId 用户Id
     * @param imei 设备编码
     * @return 意见及反馈列表
     */
    public List<DiscussPo> getOpinionsByOnwerId(String userId, String imei) {
        try {
            Map<String, String> param = new HashMap<String, String>();
            param.put("userId", userId);
            param.put("imei", imei);
            List<DiscussPo> ol = this.discussDao.queryForList("getListByUserId", param);
            if (ol!=null&&ol.size()>0) {
                List<DiscussPo> ret = new ArrayList<DiscussPo>();
                Discuss item = null;
                List<DiscussPo> rol = this.discussDao.queryForList("getListByUserId", param);
                if (rol!=null&&rol.size()>0) {
                    int i=0;
                    DiscussPo arop=rol.get(i);
                    for (DiscussPo op: ol) {
                        item=new Discuss();
                        item.buildFromPo(op);
                        if (i<rol.size()) {
                            while (arop.getId().equals(op.getId())) {
//                                item.addOneRe(arop);
                                if (++i==rol.size()) break;
                                arop=rol.get(i);
                            }
                        }
                        ret.add(item);
                    }
                } else {
                    for (DiscussPo op: ol) {
                        item=new Discuss();
                        item.buildFromPo(op);
                        ret.add(item);
                    }
                }
                return ret;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}