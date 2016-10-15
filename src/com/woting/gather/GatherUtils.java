package com.woting.gather;

import java.util.Map;

import com.spiritdata.dataanal.visitmanage.run.mem.VisitMemoryService;
import com.woting.passport.mobile.MobileUDKey;

/**
 * 收集数据的方法类
 * @author wanghui
 *
 */
public abstract class GatherUtils {
    /**
     * 从Api收集数据，并进行存储
     * @param udk 用户key信息
     * @param data 收集到的数据
     */
    public static void fromAPI(MobileUDKey udk, Map<String, Object> data) {
        VisitMemoryService vms=VisitMemoryService.getInstance();
        //ssvms.put2Queue(vlp);
    }
}