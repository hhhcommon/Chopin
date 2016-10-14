<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.lang.*"%>
<%@page import="java.io.File"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="javax.servlet.ServletContext"%>

<%@page import="org.apache.commons.io.FileUtils"%>

<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>

<%@page import="com.spiritdata.framework.FConstants"%>
<%@page import="com.spiritdata.framework.util.RequestUtils"%>
<%@page import="com.spiritdata.framework.util.StringUtils"%>
<%@page import="com.spiritdata.framework.util.JsonUtils"%>
<%@page import="com.spiritdata.framework.util.DateUtils"%>
<%@page import="com.spiritdata.framework.core.cache.SystemCache"%>

<%@page import="javax.imageio.ImageIO"%>
<%@page import="java.awt.image.BufferedImage"%>

<%@page import="com.woting.content.manage.media.service.MediaContentService"%>

<%@page import="org.jsoup.nodes.Document"%>
<%@page import="org.jsoup.Jsoup"%>
<%@page import="org.jsoup.nodes.Element"%>
<%@page import="org.jsoup.select.Elements"%>

<%
  String path=request.getContextPath(); //base Url
  String sid=request.getSession().getId(); //sessionId
  Map<String, Object> m=RequestUtils.getDataFromRequest(request);
  String contentId=(m==null||m.get("ContentId")==null)?null:m.get("ContentId")+"";
  String userId=(m==null||m.get("UserId")==null)?null:m.get("UserId")+"";
  String rootPath=(String)(SystemCache.getCache(FConstants.APPOSPATH).getContent());
  int widthLimit=Integer.parseInt((m==null||m.get("ScreenWidth")==null)?"440":m.get("ScreenWidth")+"");
  boolean isPlaying=false;  
%>
<!DOCTYPE html>
<!-- 内容页的壳子，为app使用 -->
<html>
<head>
<meta charset="UTF-8">

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="cache-control" content="no-cache"/>
<meta http-equiv="pragma" content="no-cache"/>
<meta http-equiv="expires" content="0"/>
<meta name="viewport" content="width=device-width,height=device-height,inital-scale=1.0,maximum-scale=1.0,user-scalable=no;">
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
<script src="./resources/plugins/spiritui/jq.spirit.pageFrame.js"></script>

<link rel="stylesheet" type="text/css" href="./resources/css/common.css"/>
<link rel="stylesheet" type="text/css" href="./resources/css/mainPage.css"/>
<link rel="stylesheet" type="text/css" href="./resources/plugins/spiritui/themes/default/all.css"/>
<title></title>
<style>
html { color:#000; background:#fff; }
body { padding:0; }

#a_content {
  height:100%;
  width:100%;
}
#a_title {
  margin-top:8px;
  height:23px;
  text-align:center;
  align:center;
  line-height:34px;
  font-size:20px;
  font-family:"黑体";
  font-weight:bold;
  display:inline-block;
  width:80%;
}
#a_time {
  padding-top:8px;
  height:24px;
  text-align:center;
  align:center;
  line-height:24px;
  font-size:14px;
}
#_time {
  height:24px;
  text-align:bottom;
  align:center;
  line-height:24px;
  font-size:14px;
}
#a_source {
  height:20px;
  text-align:center;
  align:center;
  line-height:20px;
  font-size:14px;
}
#_source {
  height:20px;
  align:center;
  line-height:20px;
  text-align:bottom;
  font-size:14px;
}
#a_img {
  margin-top:8px;
  padding-left:10px;
  padding-right:10px;
}
.a_media {
  position:relative;
  margin-top:8px;
  padding-left:10px;
  padding-right:10px;
  height:30px;
}
.a_nullhtml {
  margin-top:8px;
  padding-left:10px;
  padding-right:10px;
  width:800px;
  height:60px;
}
.a_nullhtml .word {
  width:100%;
}

.a_nullhtml p {
  width:100%;
  text-align:left;
  font-size:16px;
  line-height:150%;
}

._video {
  width:500px;
}
._audio {
  width:500px;
  height:30px;
}
.a_html {
  width:800px;
  padding-left:10px;
  padding-right:10px;
  margin-top:8px;
}
.errImg {
  margin-top:20px;
}
#a_err {
  height:100%;
  width:100%;
  background-color:#A8A8A8;
}

.conpetitorContent {
  margin-top:8px;
  padding-left:10px;
  padding-right:10px;
}
.word {
  text-align:left;
  font-size:16px;
  line-height:150%;
}
.word p {
  text-align:left;
  font-size:16px;
  line-height:150%;
}
.conpetitorContent .pic {
  margin:5px 0;
}
.conpetitorContent .video {
  margin:5px 0;
}
.conpetitorContent .audio {
  margin:5px 0;
}

.pl_img {
  width:40%;
}
.pl_lable {
  width:25%;
  font-size:16px;
  font-weight:bold;
  margin-top:10px 0;
  color:#6D6D6D;
  font-style:italic;
  padding-left:5px;
  text-align:right;
}
.pl_value {
  width:35%;
  font-size:18px;
  font-weight:normal;
  padding-left:8px;
}
.pl_desc {
  text-align:left;
  font-size:16px;
  line-height:150%;
  padding: 0 7px;
  margin-top:8px;
}

@media screen and (max-width:607px){
.errImg {
  width:100%;
  height:100%;
}
}

@media screen and (max-width:<%=widthLimit%>px){
._img {
  width:100%;
  height:100%;
}
._video {
  width:100%;
}
._audio {
  width:100%;
}
.a_html {
  width:100%;
  padding:0;
  margin-top:8px;
}
.a_nullhtml {
  margin-top:8px;
  padding-left:10px;
  padding-right:10px;
  width:100%;
  height:60px;
}

.pl_img {
  width:40%;
}
.pl_lable {
  width:25%;
}
.pl_value {
  width:35%;
}
.pl_img img {
  width:100%;
}
}
</style>
</head>

<body><center>
<%
  if (StringUtils.isNullOrEmptyOrSpace(contentId)) {
%>
<div id="a_err"><center>
<img class="errImg" src="./resources/chopin/imgs/nullPage.png"></img>
</center></div>
<%
    return;
  }

  //spring bean处理
  ServletContext sc=(SystemCache.getCache(FConstants.SERVLET_CONTEXT)==null?null:(ServletContext)SystemCache.getCache(FConstants.SERVLET_CONTEXT).getContent());
  ApplicationContext ctx=WebApplicationContextUtils.getWebApplicationContext(sc);

  MediaContentService mediaContentService=(MediaContentService)ctx.getBean("mediaContentService");
  Map<String, Object> contents=mediaContentService.getContentInfo(userId, contentId);
  try {
    contents=mediaContentService.getContentInfo(userId, contentId);
  } catch(Exception e) {
  }
  if (contents==null) {
%>
<div id="a_err"><center>
<img class="errImg" src="./resources/chopin/imgs/nullPage.png"></img>
</center></div>
<%
    return;
  }
  //标题
  String title=contents.get("ContentName")==null?null:contents.get("ContentName")+"";
  if (StringUtils.isNullOrEmptyOrSpace(title)) {
%>
<div id="a_err"><center>
<img class="errImg" src="./resources/chopin/imgs/nullPage.png"></img>
</center></div>
<%
    return;
  }
  //主图片
  String imgUrl=contents.get("ContentImg")==null?null:contents.get("ContentImg")+"";
  String showImg=contents.get("LangDid")==null?"false":contents.get("LangDid")+"";
  //主媒体
  String mediaUrl=contents.get("ContentSubjectWord")==null?null:contents.get("ContentSubjectWord")+"";
  //主内容
  String htmlUrl=contents.get("ContentPlay")==null?null:contents.get("ContentPlay")+"";
  //读取文档内容
  if (!StringUtils.isNullOrEmptyOrSpace(htmlUrl)) {
      htmlUrl=htmlUrl.substring(htmlUrl.indexOf("dataCenter")+10);
      htmlUrl=rootPath+"dataCenter"+htmlUrl;
      try {
          htmlUrl=FileUtils.readFileToString(new File(htmlUrl), "utf-8");
          int pos1=htmlUrl.indexOf("<body>");
          int pos2=htmlUrl.indexOf("</body>");
          if (pos1!=-1&&pos2!=-1) {
              htmlUrl=htmlUrl.substring(pos1+6, pos2);
              //替换所有的img
          } else htmlUrl="";
      } catch(Exception e) {
          htmlUrl="";
      }
  }
  if ((StringUtils.isNullOrEmptyOrSpace(imgUrl)&&showImg.equals("true"))&&(StringUtils.isNullOrEmptyOrSpace(mediaUrl)||mediaUrl.length()<5)&&(StringUtils.isNullOrEmptyOrSpace(htmlUrl)||htmlUrl.length()<10)) {
%>
<div id="a_err"><center>
<img class="errImg" src="./resources/chopin/imgs/nullPage.png"></img>
</center></div>
<%
    return;
  }
  //一般文章页
  String time=(contents.get("ContentPubTime")+"").substring(0, 10);//发布时间
  String source=contents.get("ContentSource")==null?null:contents.get("ContentSource")+"";//来源
%>
<div id="a_content">
  <div id="a_title"><%=title%></div>
  <div id="a_time"><span id="_time"><%=time%></span></div>
<%if (!StringUtils.isNullOrEmptyOrSpace(source)) {%>
  <div id="a_source"><span id="_source">来源:<%=source%></span></div>
<%}%>
<%
  if (!StringUtils.isNullOrEmptyOrSpace(imgUrl)&&showImg.equals("true")) {
    //读取图片
    String imgFile=imgUrl.substring(imgUrl.indexOf("dataCenter")+10);
    imgFile=rootPath+"dataCenter"+imgFile;
    File pic=new File(imgFile);
    try {
      BufferedImage sourceImg=ImageIO.read(new FileInputStream(pic));
      if (sourceImg.getWidth()<widthLimit) {
%>
  <div id="a_img"><center><img src="<%=imgUrl%>"></img></center></div>
<%    } else {%>
  <div id="a_img"><center><img class="_img" src="<%=imgUrl%>"></img></center></div>
<%    }
    } catch(Exception e) {
    }
  }
  if (!StringUtils.isNullOrEmptyOrSpace(mediaUrl)) {
    //视音频
    String voiceExtName="mp3;m4a;m3u8";
    String _mf="";
    int pos1=mediaUrl.lastIndexOf("/");
    int pos2=mediaUrl.lastIndexOf("\\");
    int pos=(pos1>pos2?pos1:pos2);
    if (pos!=-1) _mf=mediaUrl.substring(pos+1);
    String[] _sv=voiceExtName.split(";");
    boolean isVoice=false;
    for (int i=0; i<_sv.length; i++) {
      if (_mf.indexOf("."+_sv[i]+"?")>0||_mf.length()==(_mf.indexOf("."+_sv[i])+_sv[i].length()+1)) {
        isVoice=true;
        break;
      }
    }
    isPlaying=true;
    if (isVoice) {
%>
  <div id="a_media" class="a_media"><audio class="_audio" loop="loop" controls="true" autoplay="true" src="<%=mediaUrl%>"></audio></div>
<% } else { %>
  <div id="a_media" class="a_media"><iframe class="_video" frameborder="no" scrolling="no" src="<%=mediaUrl%>"></iframe></div>
<%
    }
  }
  if (!StringUtils.isNullOrEmptyOrSpace(htmlUrl)) {
    Document doc=Jsoup.parse("<body>"+htmlUrl+"</body>");
    if ((contents.get("ContentStatus")+"").equals("1")) {//处理选手页
      String playImg=doc.select(".photo").select("img").attr("src");
      Elements infos=doc.select(".value");
      List<String> il=new ArrayList<String>();
      for (Element _info: infos) {
        il.add(_info.html());
      }
      String instruction=doc.select(".instruction").html();
      doc=Jsoup.parse("<body><div style='padding: 0 7px;'><table><tr><td rowspan=6 class='pl_img'><img src='"+playImg+"'/></td>"
              +"<td class='pl_lable'>&nbsp;</td><td class='pl_value'></td></tr>"
        +"<tr><td class='pl_lable'>组　别:</td><td class='pl_value'>"+il.get(2)+"</td></tr>"
        +"<tr><td class='pl_lable'>性　别:</td><td class='pl_value'>"+il.get(1)+"</td></tr>"
        +"<tr><td class='pl_lable'>籍　贯:</td><td class='pl_value'>"+il.get(3)+"</td></tr>"
        +"<tr><td class='pl_lable'>等　级:</td><td class='pl_value'>"+il.get(4)+"</td></tr>"
        +"<tr><td class='pl_lable'>证件后4位:</td><td class='pl_value'>"+il.get(5)+"</td></tr></table></div>"
        +"<div class='pl_desc'>"+instruction+"</div></body>");
    } else { //处理文章
      //处理文章中的图
      Elements imgs=doc.select("img");
      for (Element _img: imgs) {
        //读取图片
        String imgSrc=_img.attr("src");
        String imgFile=_img.attr("src");
        imgFile=imgFile.substring(imgFile.indexOf("dataCenter")+10);
        imgFile=rootPath+"dataCenter"+imgFile;
        File pic=new File(imgFile);
        try {
          BufferedImage sourceImg=ImageIO.read(new FileInputStream(pic));
          if (sourceImg.getWidth()<widthLimit) {
            _img.parent().html("<img src='"+imgSrc+"'/>");
          } else {
            _img.parent().html("<img src='"+imgSrc+"' class='_img'/>");
          }
        } catch(Exception e) {
          _img.parent().html("");
        }
      }
      //处理文章中的视频
      Elements videos=doc.select(".video");
      for (Element _video: videos) {
        _video.html("<iframe class='_video' frameborder='no' scrolling='no' src='"+_video.select("source").attr("src")+"'></iframe>");
        isPlaying=true;
      }
      //处理文章中的音频
      Elements audios=doc.select(".audio");
      for (Element _audio: audios) {
        _audio.html("<audio class='_audio' loop='loop' controls='true' "+(isPlaying?"":"autoplay='true'")+" src='"+_audio.select("source").attr("src")+"'></audio>");
        if (!isPlaying) isPlaying=true;
      }
    }
    htmlUrl=doc.body().html();
%>
  <div class="a_html"><%=htmlUrl%><p style="height:20px;">&nbsp;</p></div>
<%
  } else {
    String nullHtml="<p>&nbsp;</p>";
//    String contentPub=contents.get("ContentPub")==null?null:contents.get("ContentPub")+"";
//    if (!StringUtils.isNullOrEmptyOrSpace(contentPub)) nullHtml+="<p>感谢本内容的提供者："+(contentPub.equals("admin")?"我听科技":contentPub)+"</p>";
//    String cTime=contents.get("CTime")==null?null:contents.get("CTime")+"";
//    if (!StringUtils.isNullOrEmptyOrSpace(cTime)) nullHtml+="<p>提供时间："+cTime.substring(0, 10)+"</p>";
%>
  <div class="a_nullhtml"><div class="word"><%=nullHtml%></div></div>
<%
  }
%>
</div>
</center></body>
<script>
//主函数
$(function() {
  if ($(window).width()<<%=widthLimit%>) {
    $("._video").attr("width", $(window).width());
    $("._video").attr("height", ($(window).width()-10)*0.6);
    $(".video").height($("._video").height());
    $(".a_media").height($("._video").height());
  } else {
    $("._video").attr("width", "500px");
    $("._video").attr("height", "300px");
    $(".video").height($("._video").height());
    $(".a_media").height($("._video").height());
  }
  window.setTimeout(function() {
    var sumH=$("body").height();
    try {
      window.parent.setMainHeight(sumH);
    } catch(e) {}
  }, 100);
});
</script>
</html>