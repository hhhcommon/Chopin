<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.lang.*"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>

<%@page import="com.spiritdata.framework.FConstants"%>
<%@page import="com.spiritdata.framework.util.RequestUtils"%>
<%@page import="com.spiritdata.framework.util.StringUtils"%>
<%@page import="com.spiritdata.framework.core.cache.SystemCache"%>
<%@page import="com.woting.content.manage.media.service.MediaContentService"%>

<%
  String path=request.getContextPath(); //base Url
  String sid=request.getSession().getId(); //sessionId
  Map<String, Object> m=RequestUtils.getDataFromRequest(request);
  String userId=(m==null||m.get("UserId")==null)?null:m.get("UserId")+"";
  String rootPath=(String)(SystemCache.getCache(FConstants.APPOSPATH).getContent());
  int widthLimit=Integer.parseInt((m==null||m.get("ScreenWidth")==null)?"400":m.get("ScreenWidth")+"");
  boolean isPlaying=false;  
%>
<!DOCTYPE html>
<!-- 内容页的壳子，为web使用 -->
<html>
<head>
<meta charset="UTF-8">

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="cache-control" content="no-cache"/>
<meta http-equiv="pragma" content="no-cache"/>
<meta http-equiv="expires" content="0"/>

<meta name="viewport" content="width=device-width,height=device-height,inital-scale=1.0,maximum-scale=1.0,user-scalable=no;">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta name="format-detection" content="telephone=no">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<!--[if lt IE 8]>
<script>
  alert('本系统已不支持IE6-8，请使用谷歌、火狐等浏览器\n或360、QQ等国产浏览器的极速模式浏览本页面！');
</script>
<![endif]-->

<script src="./resources/plugins/hplus/js/jquery-2.1.1.min.js"></script>
<script src="./resources/js/common.utils.js"></script>
<script src="./resources/js/context.utils.js"></script>
<script src="./resources/js/framework.utils.js"></script>
<script src="./resources/plugins/spiritui/jq.spirit.utils.js"></script>
<script src="./resources/plugins/bootstrap/js/bootstrap.min.js"></script>

<link rel="stylesheet" media="screen" href="./resources/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="./resources/chopin/css/font.css"/>
<link rel="stylesheet" type="text/css" href="./resources/css/common.css"/>
<link rel="stylesheet" type="text/css" href="./resources/css/mainPage.css"/>
<link rel="stylesheet" type="text/css" href="./resources/plugins/spiritui/themes/default/all.css"/>

<title></title>
<style>
html {color:#000; background:#fff;}
body {margin:0;}

/*new=============================*/
.vote_main {
  padding:0 20px;
}
#mainTable {
  width:100%;
}
#mainTable tr:first-child {
  border-top:0;
}
#a_err {
  height:100%;
  width:100%;
  background-color:#A8A8A8;
}
.errImg {
  margin-top:20px;
}
@media screen and (max-width:607px){
.errImg {
  width:100%;
  height:100%;
}
}
._gray {
  filter:progid:DXImageTransform.Microsoft.BasicImage(grayscale=1);
  -webkit-filter: grayscale(100%);
}
._tr {
  height:100px;
  border-top:1px solid #D9D9D9;
  padding: 20px 7px 19px 7px;
}
._portrait {
  width:105px;
  padding-left:5px;
}
._portrait img {
  height:80px;
  width:80px;
  border-radius:40px;
  border: 1px solid #FFA634;
  cursor:pointer;
}
._progress {
  width:*;
}
._desc {
  position:relative;
  width:100%;
  height:30px;
  color:#000;
  font-size:20px;
  font-weight:bold;
  font-family: vista_MS;
  margin-top:25px;
}
._desc_name {
  position:absolute;
  left:0px;
}
._desc_score {
  position:absolute;
  right:0px;
}
._progressBar {
  width:*;
  margin-top:8px;
}
._vote {
  cursor:pointer;
  width:100px;
  padding-left:35px;
}
._vote img {
  height:50px;
  width:50px;
}
._voteText {
  color:#FFA634;
  font-size:18px;
}

@media screen and (max-width:<%=widthLimit%>px){
.vote_main {
  padding:0;
}
._tr {
  height:80px;
  border-top:1px solid #D9D9D9;
}
._portrait {
  width:75px;
  padding-left:15px;
}
._portrait img {
  height:60px;
  width:60px;
  border-radius:30px;
  border:1px solid #FFA634;
  cursor:pointer;
}
._desc {
  position:relative;
  width:100%;
  height:25px;
  color:#000;
  font-size:18px;
  font-weight:bold;
  font-family: vista_MS;
  padding-top:20px;
  margin-top:8px;
}
._desc_score {
  position:absolute;
  right:0px;
  font-weight:normal;
}
._progressBar {
  width:*;
  height:15px;
  margin-top:7px;
}
._vote {
  cursor:pointer;
  width:60px;
  padding-left:10px;
}
._vote img {
  height:40px;
  width:40px;
}
._voteText {
  color:#FFA634;
  font-size:15px;
}
}

</style>
</head>
<body><center>
<%
  //spring bean处理
  ServletContext sc=(SystemCache.getCache(FConstants.SERVLET_CONTEXT)==null?null:(ServletContext)SystemCache.getCache(FConstants.SERVLET_CONTEXT).getContent());
  ApplicationContext ctx=WebApplicationContextUtils.getWebApplicationContext(sc);

  MediaContentService mediaContentService=(MediaContentService)ctx.getBean("mediaContentService");
  List<Map<String, Object>> l=mediaContentService.getPlaySumList(userId);
  System.out.println(l.size());
  if (l==null||l.isEmpty()) {
%>
<div id="a_err"><center>
<img class="errImg" src="./resources/chopin/imgs/nullPage.png"></img>
<script>$("html").css("background-color","#A8A8A8");</script>
<iframe src="session.jsp" width="0" height="0" style="display:none"></iframe>
</center></div>
<%
    return;
  }
%>
<script>
function setDeviceId(sessionId) {
  deviceId=sessionId;
}

var isApp=getUrlParam(window.location.href, "isApp");//如果是App就有这个值
isApp=!(!isApp);
var rootPath=getRootPath();
var bars=[];
var maxVote=0;
var maxId="";
var indexPage=getMainPage();
var deviceId="";
var deviceId=(isApp?getUrlParam(window.location.href, "IMEI"):"");
var pcdType=(isApp?1:3);
</script>
<div id="vote_main" class="vote_main"><table id="mainTable" class="mainTable">
<%
  for (Map<String, Object> onePlayer: l) {
    System.out.println(onePlayer);
    int favoSum=0;
    try {
        favoSum=Integer.parseInt(onePlayer.get("FavoSum")+"");
        favoSum=(favoSum==-1?0:favoSum);
    } catch(Exception e) {
    }
%>
  <tr class='_tr' id='tr_<%=onePlayer.get("ContentId")%>'>
    <td class='_portrait' onclick='showPlayer("<%=onePlayer.get("ContentId")%>")'><img src='./asset/members/portrait/<%=onePlayer.get("UserName")%>.jpg'></img></td>
    <td class='_progress'>
      <div class='_desc'>
        <div class='_desc_name'><%=onePlayer.get("UserName")%></div>
        <div class='_desc_score'><%=favoSum%>票</div>
      </div>
      <div class='_progressBar bar progress progress-warning active'><div id='bar_<%=onePlayer.get("ContentId")%>'></div>
    </div></td>
<%  if ("1".equals(onePlayer.get("IsFavorate")+"")) {%>
    <td class='_vote' title='投票' id='vote_<%=onePlayer.get("ContentId")%>'>
      <div><img src='./resources/chopin/imgs/voteIcon.png'></img></div><div class='m_voteText'>已投票</div>
    </td>
<%  } else {%>
    <td class='_vote _gray' title='投票' onclick='votePlayer("<%=onePlayer.get("ContentId")%>")' id='vote_<%=onePlayer.get("ContentId")%>'>
      <div><img src='./resources/chopin/imgs/voteIcon.png'></img></div><div class='_voteText'>投1票</div>
    </td>
<%  }%>
  </tr>
<%
  }
%>
</table></div>
<iframe src="session.jsp" width="0" height="0" style="display:none"></iframe>
</center></body>

<script>
//主函数
$(function() {
});
</script>
</html>