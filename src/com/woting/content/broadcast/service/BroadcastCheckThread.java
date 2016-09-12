package com.woting.content.broadcast.service;

import java.util.List;
import java.util.Map;

public class BroadcastCheckThread extends Thread {

	private List<Map<String, Object>> getlist;
	private List<Map<String, Object>> putlist;
	
	public BroadcastCheckThread(List<Map<String, Object>> getlist, List<Map<String, Object>> putlist) {
		this.getlist = getlist;
		this.putlist = putlist;
	}
	
	private synchronized Map<String, Object> getBcList() {
		return getlist.remove(0);
	}
	
	private synchronized void addEffectiveList(Map<String, Object> m) {
		putlist.add(m);
	}
	
	@Override
	public void run() {
		
	}
}
