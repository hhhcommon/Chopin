package com.woting.gather;

import java.util.Map;

import com.spiritdata.dataanal.visitmanage.core.persistence.pojo.VisitLogPo;
import com.spiritdata.dataanal.visitmanage.run.mem.VisitMemoryService;
import com.spiritdata.framework.util.StringUtils;
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
    public static void SaveLogFromAPI(MobileUDKey udk, Map<String, Object> data) throws InterruptedException {
        VisitLogPo vlp=new VisitLogPo();

        vlp.setOwnerId(udk.getUserId());
        vlp.setOwnerType(201);
        //1-GPS信息
        String temp=data.get("pointInfo")==null?null:data.get("pointInfo")+"";
        vlp.setPointInfo(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        //2-客户端Ip
        temp=data.get("clientIp")==null?null:data.get("clientIp")+"";
        vlp.setClientIp(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        //3-客户端网卡地址：目前是设备Id(IMEI)
        vlp.setClientMac(udk.getDeviceId());
        //4-设备名称：目前是设备分类号
        vlp.setEquipName(udk.getPCDType()+"");
        //5-设备型号：目前是设备型号(MobileClass)
        temp=data.get("MobileClass")==null?null:data.get("MobileClass")+"";
        vlp.setEquipVer(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        //7-浏览器名称
        //8-浏览器版本
        //9-访问实体的类型
        temp=data.get("ObjType")==null?null:data.get("ObjType")+"";
        vlp.setObjType(StringUtils.isNullOrEmptyOrSpace(temp)?null:Integer.parseInt(temp));
        //A-访问实体的Id
        temp=data.get("ObjId")==null?null:data.get("ObjId")+"";
        vlp.setObjId(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        //B-访问的地址
        temp=data.get("V_Url")==null?null:data.get("V_Url")+"";
        vlp.setObjUrl(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        //C-从那个地址访问的，在这里是所有的参数
        temp=data.get("AllParam")==null?null:data.get("AllParam")+"";
        vlp.setFromUrl(StringUtils.isNullOrEmptyOrSpace(temp)?null:temp);
        
        VisitMemoryService vms=VisitMemoryService.getInstance();
        vms.put2Queue(vlp);
    }
}