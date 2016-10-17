package com.spiritdata.dataanal.visitmanage.run.mem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.spiritdata.dataanal.common.model.Owner;
import com.spiritdata.dataanal.visitmanage.core.persistence.pojo.VisitLogPo;


/**
 * 访问内存对象
 * @author wh
 */
public class VisitMemory {
    //java的占位单例模式===begin
    private static class InstanceHolder {
        public static VisitMemory instance = new VisitMemory();
    }
    public static VisitMemory getInstance() {
        InstanceHolder.instance.init();
        return InstanceHolder.instance;
    }

    //使用者未访问对象的存储结构
    protected Map<String, Map<Owner, List<?>>> ownersNoVisitData = null;
    //访问日志的队列：访问后，日志信息先放入本队列，之后再由一个线程把他写入持久化中，目前是数据库
    protected BlockingQueue<VisitLogPo> visitQueue = null;

    /**
     * 参数初始化，必须首先执行这个方法，访问日志内存类才能使用
     */
    public void init() {
        if (ownersNoVisitData==null) ownersNoVisitData = new ConcurrentHashMap<String, Map<Owner, List<?>>>();
        if (visitQueue==null) visitQueue = new LinkedBlockingQueue<VisitLogPo>();
    }
}