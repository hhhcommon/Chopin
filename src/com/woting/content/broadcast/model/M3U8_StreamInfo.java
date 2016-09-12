package com.woting.content.broadcast.model;

public class M3U8_StreamInfo {

	private int programid; //该值是一个十进制整数，惟一地标识一个在playlist文件范围内的特定的描述
	private long bandwidth; //带宽，必须有
	private String codecs; //指定流的编码类型
	
	
	public int getProgramid() {
		return programid;
	}
	public void setProgramid(int programid) {
		this.programid = programid;
	}
	public long getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(long bandwidth) {
		this.bandwidth = bandwidth;
	}
	public String getCodecs() {
		return codecs;
	}
	public void setCodecs(String codecs) {
		this.codecs = codecs;
	}
}
