package com.woting.discuss.model;

import java.util.HashMap;
import java.util.Map;

import com.spiritdata.framework.core.model.ModelSwapPo;
import com.spiritdata.framework.exceptionC.Plat0006CException;
import com.spiritdata.framework.util.SequenceUUID;
import com.spiritdata.framework.util.StringUtils;
import com.woting.discuss.persis.pojo.DiscussPo;

/**
 * 反馈意见信息<br/>
 * 包括意见的反馈列表
 * @author wh
 */
public class Discuss extends DiscussPo implements  ModelSwapPo {
    private static final long serialVersionUID = 1020093563227522687L;


    @Override
    public void buildFromPo(Object po) {
        if (po==null) throw new Plat0006CException("Po对象为空，无法从空对象得到概念/逻辑对象！");
        if (!(po instanceof DiscussPo)) throw new Plat0006CException("Po对象不是AppOpinionPo的实例，无法从此对象构建字典组对象！");
        DiscussPo _po = (DiscussPo)po;
        this.setId(_po.getId());
        this.setImei(_po.getImei());
        this.setUserId(_po.getUserId());
        this.setArticalId(_po.getArticalId());
        this.setOpinion(_po.getOpinion());
        this.setCTime(_po.getCTime());
    }
    @Override
    public Object convert2Po() {
        DiscussPo ret = new DiscussPo();
        if (StringUtils.isNullOrEmptyOrSpace(this.getId())) ret.setId(SequenceUUID.getUUIDSubSegment(4));
        else ret.setId(this.getId());
        ret.setImei(this.getImei());
        ret.setUserId(this.getUserId());
        ret.setArticalId(this.getArticalId());
        ret.setOpinion(this.getOpinion());
        ret.setCTime(this.getCTime());
        return ret;
    }

    public Map<String, Object> toHashMap4Mobile() {
        Map<String, Object> retM = new HashMap<String, Object>();
        if (!StringUtils.isNullOrEmptyOrSpace(this.id)) retM.put("Id", this.id);
        if (!StringUtils.isNullOrEmptyOrSpace(this.imei)) retM.put("IMEI", this.imei);
        if (!StringUtils.isNullOrEmptyOrSpace(this.userId)) retM.put("UserId", this.userId);
        if (!StringUtils.isNullOrEmptyOrSpace(this.articalId)) retM.put("ContentId", this.articalId);
        if (!StringUtils.isNullOrEmptyOrSpace(this.opinion)) retM.put("Discuss", this.opinion);
        if (this.CTime!=null) retM.put("Time", this.CTime.getTime());
        return retM;
    }
}