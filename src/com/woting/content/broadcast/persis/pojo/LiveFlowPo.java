package com.woting.content.broadcast.persis.pojo;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class LiveFlowPo extends BaseObject {
    private static final long serialVersionUID = -8315162892011249812L;

    private String id;//直播流Id
    private String bcId;//对应电台Id
    private int bcSrcType; //来源类型：1-组织表,2-文本
    private String bcSrcId; //来源Id，当bcSrcType=1
    private String bcSource;//来源名称:蜻蜓，官网等
    private String flowURI;//来源:蜻蜓，官网等
    private Integer isMain;//是否是主直播流
    private String desc; //直播流描述
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
	public int getBcSrcType() {
		return bcSrcType;
	}
	public void setBcSrcType(int bcSrcType) {
		this.bcSrcType = bcSrcType;
	}
	public String getBcSrcId() {
		return bcSrcId;
	}
	public void setBcSrcId(String bcSrcId) {
		this.bcSrcId = bcSrcId;
	}
	public String getBcSource() {
		return bcSource;
	}
	public void setBcSource(String bcSource) {
		this.bcSource = bcSource;
	}
	public String getFlowURI() {
		return flowURI;
	}
	public void setFlowURI(String flowURI) {
		this.flowURI = flowURI;
	}
	public Integer getIsMain() {
		return isMain;
	}
	public void setIsMain(Integer isMain) {
		this.isMain = isMain;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Timestamp getCTime() {
		return CTime;
	}
	public void setCTime(Timestamp cTime) {
		CTime = cTime;
	}
}