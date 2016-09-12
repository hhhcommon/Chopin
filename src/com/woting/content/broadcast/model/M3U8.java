package com.woting.content.broadcast.model;

import java.util.ArrayList;
import java.util.List;

public class M3U8 {

	private boolean extm3u; //m3u8文件头
	private int version; //hls的协议版本号，暗示媒体流的兼容性
	private long mediasequence; //第一个TS分片的序列号
	private long targetduration; //每个分片TS的最大的时长（单位毫秒）
	private M3U8_StreamInfo streamInfo ; //指定一个包含多媒体信息的 media URI 作为PlayList
	private List<M3U8_ExtInfo> extinflist = new ArrayList<M3U8_ExtInfo>(); //分片TS的信息，如时长，带宽
	
	public boolean isExtm3u() {
		return extm3u;
	}
	public void setExtm3u(boolean extm3u) {
		this.extm3u = extm3u;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public long getMediasequence() {
		return mediasequence;
	}
	public void setMediasequence(long mediasequence) {
		this.mediasequence = mediasequence;
	}
	public long getTargetduration() {
		return targetduration;
	}
	public void setTargetduration(long targetduration) {
		this.targetduration = targetduration;
	}
	public M3U8_StreamInfo getStreamInfo() {
		return streamInfo;
	}
	public void setStreamInfo(M3U8_StreamInfo streamInfo) {
		this.streamInfo = streamInfo;
	}
	public List<M3U8_ExtInfo> getExtinflist() {
		return extinflist;
	}
	public void setExtinflist(List<M3U8_ExtInfo> extinflist) {
		this.extinflist = extinflist;
	}

	public boolean isOrNoEffective() {
		if(streamInfo!=null&&streamInfo.getCodecs()!=null) return true;
		if(extinflist!=null&&extinflist.size()>0) return true;
		return false;
	}
}
