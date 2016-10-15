package com.spiritdata.dataanal.visitmanage.core.persistence.pojo;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

/**
 * 访问日志的持久化对象，与数据库中sa_visit_log表对应
 * @author wh
 */
public class VisitLogPo extends BaseObject {
    private static final long serialVersionUID = -5047650691581766045L;

    private String id; //任务组id
    private int ownerType; //任务组所对应的所有者类型（1=注册用户;2=非注册用户(session);3=系统生成）
    private String ownerId; //所有者标识（可能是用户id，也可能是SessionID，也可能是'Sys'）
    private String pointInfo; //可能是GPS坐标，以json格式记录
    private String clientIp; //客户端Ip
    private String clientMac; //客户端Mac地址
    private String equipName; //设备名称
    private String equipVer; //设备型号
    private String exploreName; //浏览器名称
    private String exploreVer; //浏览器型号
    private int objType; //访问对象类型
    private String objId; //访问对象Id
    private String objUrl; //访问对象Url
    private String fromUrl; //访问对象的Url所在页面的Url
    private Timestamp visitTime; //访问时间

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getOwnerType() {
        return ownerType;
    }
    public void setOwnerType(int ownerType) {
        this.ownerType = ownerType;
    }
    public String getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    public String getPointInfo() {
        return pointInfo;
    }
    public void setPointInfo(String pointInfo) {
        this.pointInfo = pointInfo;
    }
    public String getClientIp() {
        return clientIp;
    }
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    public String getClientMac() {
        return clientMac;
    }
    public void setClientMac(String clientMac) {
        this.clientMac = clientMac;
    }
    public String getEquipName() {
        return equipName;
    }
    public void setEquipName(String equipName) {
        this.equipName = equipName;
    }
    public String getEquipVer() {
        return equipVer;
    }
    public void setEquipVer(String equipVer) {
        this.equipVer = equipVer;
    }
    public String getExploreName() {
        return exploreName;
    }
    public void setExploreName(String exploreName) {
        this.exploreName = exploreName;
    }
    public String getExploreVer() {
        return exploreVer;
    }
    public void setExploreVer(String exploreVer) {
        this.exploreVer = exploreVer;
    }
    public int getObjType() {
        return objType;
    }
    public void setObjType(int objType) {
        this.objType = objType;
    }
    public String getObjId() {
        return objId;
    }
    public void setObjId(String objId) {
        this.objId = objId;
    }
    public String getObjUrl() {
        return objUrl;
    }
    public void setObjUrl(String objUrl) {
        this.objUrl = objUrl;
    }
    public String getFromUrl() {
        return fromUrl;
    }
    public void setFromUrl(String fromUrl) {
        this.fromUrl = fromUrl;
    }
    public Timestamp getVisitTime() {
        return visitTime;
    }
    public void setVisitTime(Timestamp visitTime) {
        this.visitTime = visitTime;
    }
}