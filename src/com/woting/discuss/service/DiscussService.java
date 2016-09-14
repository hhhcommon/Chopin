package com.woting.discuss.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.spiritdata.framework.core.dao.mybatis.MybatisDAO;
import com.spiritdata.framework.core.model.Page;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.discuss.model.Discuss;
import com.woting.discuss.persis.po.DiscussPo;

public class DiscussService {
    @Resource(name="defaultDAO")
    private MybatisDAO<DiscussPo> discussDao;

    @PostConstruct
    public void initParam() {
        discussDao.setNamespace("WT_DISCUSS");
    }

    /**
     * 得到重复意见
     * @param opinion 意见信息
     * @return 创建用户成功返回1，否则返回0
     */
    public List<Discuss> getDuplicates(Discuss discuss) {
        try {
            List<Discuss> ret=new ArrayList<Discuss>();
            List<DiscussPo> _ret=discussDao.queryForList("getDuplicates", discuss.toHashMapAsBean());
            if (_ret!=null&&!_ret.isEmpty()) {
                for (DiscussPo dpo: _ret) {
                    Discuss ele=new Discuss();
                    ele.buildFromPo(dpo);
                    ret.add(ele);
                }
            }
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
    public int insertDiscuss(Discuss discuss) {
        int i=0;
        try {
            discuss.setId(SequenceUUID.getUUIDSubSegment(4));
            discussDao.insert(discuss.convert2Po());
            i=1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * 删除评论
     * @param discuss 要删除的评论信息
     * @return 0-删除失败;1删除成功;-1无对应的评论无法删除;-2无权删除
     */
    public int delDiscuss(Discuss discuss) {
        try {
            Map<String, Object> param=((DiscussPo)discuss.convert2Po()).toHashMapAsBean();
            DiscussPo dpo=discussDao.getInfoObject(param);
            if (dpo==null) return -1;
            if (discuss.getImei().equals(dpo.getImei())&&discuss.getUserId().equals(dpo.getUserId())) {
                discussDao.delete(param);
                return 1;
            } else return -2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 根据文章Id获得文章的评论列表
     * @param userId 用户Id
     * @param imei 设备编码
     * @return 意见及反馈列表
     */
    public List<Discuss> getArticleDiscusses(String articalId, int page, int pageSize) {
        try {
            Map<String, String> param=new HashMap<String, String>();
            param.put("articalId", articalId);
            List<DiscussPo> ol=null;
            if (page>=0) { //分页
                if (page==0) page=1;
                if (pageSize<0) pageSize=10;
                Page<DiscussPo> p=this.discussDao.pageQuery(param, page, pageSize);
                if (!p.getResult().isEmpty()) {
                    ol=new ArrayList<DiscussPo>();
                    ol.addAll(p.getResult());
                }
            } else { //获得所有
                ol=this.discussDao.queryForList(param);
            }
            if (ol!=null&&ol.size()>0) {
                List<Discuss> ret=new ArrayList<Discuss>();
                if (ol!=null&&!ol.isEmpty()) {
                    for (DiscussPo dpo: ol) {
                        Discuss ele=new Discuss();
                        ele.buildFromPo(dpo);
                        ret.add(ele);
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