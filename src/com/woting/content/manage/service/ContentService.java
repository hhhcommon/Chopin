package com.woting.content.manage.service;

import org.springframework.stereotype.Service;


@Service
public class ContentService {

	/** 计算分享地址的功能 */
	public static final String preAddr = "http://www.wotingfm.com:908/CM/mweb";// 分享地址前缀

	public static final String getShareUrl_JM(String preUrl, String contentId) {// 的到节目的分享地址
		return preUrl + "/jm/" + contentId + "/content.html";
	}

	public static final String getShareUrl_ZJ(String preUrl, String contentId) {// 的到专辑的分享地址
		return preUrl + "/zj/" + contentId + "/content.html";
	}

	public static final String getShareUrl_DT(String preUrl, String contentId) {// 的到电台的分享地址
		return preUrl + "/dt/" + contentId + "/content.html";
	}
}