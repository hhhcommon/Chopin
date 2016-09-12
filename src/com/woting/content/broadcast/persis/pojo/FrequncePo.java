package com.woting.content.broadcast.persis.pojo;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class FrequncePo extends BaseObject {
    private static final long serialVersionUID = -2761176635168605839L;

    private String id;//直播流Id
    private String bcId;//对应电台Id
    private String areaCode;//来源:蜻蜓，官网等
    private String frequnce;//来源:蜻蜓，官网等
    private Timestamp CTime; //记录创建时间
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getBcId() {
        return bcId;
    }
    public void setBcId(String bcId) {
        this.bcId = bcId;
    }
    public String getAreaCode() {
        return areaCode;
    }
    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }
    public String getFrequnce() {
        return frequnce;
    }
    public void setFrequnce(String frequnce) {
        this.frequnce = frequnce;
    }
    public Timestamp getCTime() {
        return CTime;
    }
    public void setCTime(Timestamp cTime) {
        CTime = cTime;
    }
}