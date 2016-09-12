package com.woting.content.broadcast.persis.pojo;

import java.sql.Timestamp;

import com.spiritdata.framework.core.model.BaseObject;

public class BroadcastPo extends BaseObject {
    private static final long serialVersionUID = 6179978815708546435L;

    private String id; //主键
    private String bcTitle; //电台名称
    private int bcPubType; //电台所属类型：1-组织表,2-文本
    private String bcPubId; //电台所属集团Id
    private String bcPublisher; //电台所属集团名称
    private String bcImg; //电台图标
    private String bcUrl; //电台官网网址
    private String desc; //电台描述
    private Timestamp CTime; //记录创建时间
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBcTitle() {
		return bcTitle;
	}
	public void setBcTitle(String bcTitle) {
		this.bcTitle = bcTitle;
	}
	public int getBcPubType() {
		return bcPubType;
	}
	public void setBcPubType(int bcPubType) {
		this.bcPubType = bcPubType;
	}
	public String getBcPubId() {
		return bcPubId;
	}
	public void setBcPubId(String bcPubId) {
		this.bcPubId = bcPubId;
	}
	public String getBcPublisher() {
		return bcPublisher;
	}
	public void setBcPublisher(String bcPublisher) {
		this.bcPublisher = bcPublisher;
	}
	public String getBcImg() {
		return bcImg;
	}
	public void setBcImg(String bcImg) {
		this.bcImg = bcImg;
	}
	public String getBcUrl() {
		return bcUrl;
	}
	public void setBcUrl(String bcUrl) {
		this.bcUrl = bcUrl;
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