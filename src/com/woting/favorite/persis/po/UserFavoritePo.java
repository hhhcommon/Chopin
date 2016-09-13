package com.woting.favorite.persis.po;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class UserFavoritePo extends BaseObject {
    private static final long serialVersionUID=-8406881716350334538L;

    private String id; //用户词Id
    private int ownerType; //所有者类型
    private String ownerId; //所有者Id,可能是用户也可能是设备
    private String resTableName; //类型：=1是喜欢;=2是举报
    private String resId; //内容Id
    private int sumNum; //次数
    private Timestamp CTime; //记录创建时间

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id=id;
    }
    public String getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId=ownerId;
    }
    public int getOwnerType() {
        return ownerType;
    }
    public void setOwnerType(int ownerType) {
        this.ownerType=ownerType;
    }
    public String getResTableName() {
        return resTableName;
    }
    public void setResTableName(String resTableName) {
        this.resTableName = resTableName;
    }
    public String getResId() {
        return resId;
    }
    public void setResId(String resId) {
        this.resId = resId;
    }
    public int getSumNum() {
        return sumNum;
    }
    public void setSumNum(int sumNum) {
        this.sumNum = sumNum;
    }
    public Timestamp getCTime() {
        return CTime;
    }
    public void setCTime(Timestamp cTime) {
        CTime=cTime;
    }
}