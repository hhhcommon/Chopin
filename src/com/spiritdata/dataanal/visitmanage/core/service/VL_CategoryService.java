package com.spiritdata.dataanal.visitmanage.core.service;

import java.util.List;
import java.util.Map;

import com.spiritdata.dataanal.common.model.Owner;
import com.spiritdata.dataanal.visitmanage.core.enumeration.ObjType;
import com.spiritdata.dataanal.visitmanage.core.persistence.pojo.VisitLogPo;

/**
 * 访问日志分类服务接口，目前只有报告日志
 * @author wh
 */
public interface VL_CategoryService {

    /**
     * 
     * @return 分类，是ObjType类型
     */
    public ObjType getCategory();

    /**
     * 从持久化存储中获得某类未访问对象数据，并返回
     * @return 未访问对象数据
     */
    public Map<Owner, List<?>> load_getNoVisitData();

    /**
     * 比较对象信息和日志信息是否指向同一个目标
     * @param cateObj 分类对象
     * @param vl 日志信息
     * @return 若目标一致，则返回true
     */
    public boolean compare(Object cateObj, VisitLogPo vlp);
}