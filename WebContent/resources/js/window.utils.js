/**
 * 打开windows的数组
 */
var winArray = [];
var _zIndex = 1000;
var sWinArray = [];

/**
 * 得到窗口的ID
 * @returns 窗口ID
 */
function getWinUUID() {
  //得到UUID
  var _uuid="";
  var isID=false;
  while (!isID) {
    _uuid=getUUID();
    isID=true;
    $(winArray).each(function(){
      if (this.winID==_uuid) {
        isID=false;
        return;
      }
    });
  }
  return _uuid;
}

/**
 * 创建并打开easyUi的窗口
 * @param winOption是一个js对象，目前支持如下参数
 * winOption.title 窗口标题
 * winOption.url 窗口内嵌的iframe的url
 * winOption.height 窗口高度
 * winOption.width 窗口宽度
 * winOption.icon_css 窗口图标，是css中的名称
 * winOption.icon_url 窗口图标，图标的url，若设置了此参数，icon_css失效
 * winOption.modal 是否是模态窗口，默认为模态窗口
 * winOption.expandAttr 窗口的扩展属性，可定义iframe的id，是javaScript对象，如expandAttr={"frameID":"iframeID"}
 * winOption.top 窗口top
 * winOption.left 窗口left
 * @returns 返回生成窗口的UUID
 */
function newWin(winOption) {
  if (!winOption) {
    $.messager.alert('新建窗口错误','请指定窗口参数!','error');
    return ;
  } else {
  	alert(winOption.url);
    if (!winOption.url) {
      $.messager.alert('新建窗口错误','窗口参数url必须指定!','error');
      return ;
    }
  }
  //得到UUID
  var _uuid = getWinUUID();
  if (!_uuid) return ;
  //在顶层窗口创建对象
  var newWinDiv = window.document.createElement("div");
  var newWin = window.document.createElement("iframe");
  $(newWin).attr("width", "100%").attr("height", "100%")
    .attr("scrolling", "no")
    .attr("frameborder", "no")
    .attr("src", winOption.url.indexOf("?")==-1?winOption.url+"?_winID="+_uuid:winOption.url+"&_winID="+_uuid);
  if (winOption.expandAttr) {
    if (winOption.expandAttr.frameID) $(newWin).attr("id", winOption.expandAttr.frameID);
  }
  $(newWin).appendTo($(newWinDiv));

  //esayUi win处理
  var top = ($(window).height() - parseInt(winOption.height?winOption.height:0))*0.5;
  if (winOption.top&&winOption.top!=""&&((parseInt(winOption.top)+"")!="NaN")) top=parseInt(winOption.top);
  var left = ($(window).width() - parseInt(winOption.width?winOption.width:0))*0.5;
  if (winOption.left&&winOption.left!=""&&((parseInt(winOption.left)+"")!="NaN")) left=parseInt(winOption.left);
  $(newWinDiv).window({
    title: winOption.title?winOption.title:"",
    width: parseInt(winOption.width?winOption.width:400),
    height: parseInt(winOption.height?winOption.height:300),
    top: top,
    left: left,
    modal: ((winOption.modal==undefined)?true:winOption.modal),
    collapsible: false,
    shadow: true,
    closed: true,
    resizable:false,
    draggable: true,
    inline: false,
    minimizable: false,
    maximizable: false,
    onBeforeClose: function(){
      var winID=$(newWinDiv).attr("winID");
      var _i=-1;
      $(winArray).each(function(i){
        if (winID==this.winID) {
          _i=i;
          return ;
        }
      });
      if (_i!=-1) winArray.splice(_i,1);
    },
    onClose: function() {
      $(newWinDiv).window("destroy");
    }
  });
  //处理窗口图标icon_css
  if (winOption.icon_css) {
    $(newWinDiv).window({iconCls: winOption.icon_css});
  }
  //处理窗口图标icon_url
  if (winOption.icon_url) {
    $(newWinDiv).window({iconCls: "abc"});//设置图标区域 background
    //设置效果
    var winObj = $(newWinDiv).parent();
    var tempObj = winObj.parent().find(".panel-icon");
    $(tempObj).css("background", "url('"+winOption.icon_url+"') no-repeat center center");
  }
  $(newWinDiv).attr("winID", _uuid);
  $(newWinDiv).window("open");
  if (winOption.expandAttr) $(newWinDiv).expandAttr = winOption.expandAttr;
  //全局变量处理
  winArray.push({"winID": _uuid, "winOBJ": $(newWinDiv)});
  return _uuid;
}

/**
 * 关闭并销毁easyUi窗口
 * @param winId easyUi窗口的ID
 */
function closeWin(winId) {
  var find = false;
  $(winArray).each(function(){
    if (this.winID==winId) {
      this.winOBJ.window('close');
      return ;
    }
  });
  if (!find) $.messager.alert('关闭窗口错误', '未找到Id='+winId+'的窗口，无法关闭!','error');
}

/**
 * 根据winId得到easyUi窗口
 * @param winId easyUi窗口的ID
 */
function getWin(winId) {
  var ret=null;
  $(winArray).each(function(){
    if (this.winID==winId) {
      ret = this.winOBJ;
      return ;
    }
  });
  if (!ret) $.messager.alert('得到窗口对象错误', '未找到Id='+winId+'的窗口，无法得到窗口对象!','error');
  return ret;
}

/**
 * 创建并打开简单模态窗口，注意这里总是模态窗口
 * @param winOption是一个js对象，目前支持如下参数
 * winOption.height 窗口高度
 * winOption.width 窗口宽度
 * winOption.url 窗口内嵌的iframe的url
 * winOption.content 窗口内的html内容，其与url属性是互斥的，但url的优先级更高

 * winOption.headCss 窗口头样式：主要是高度和背景色
 * winOption.title 窗口标题
 * winOption.titleCss 标题样式
 * winOption.iconCss 窗口图标的css
 * winOption.iconUrl 窗口图标的url

 * winOption.expandAttr 窗口的扩展属性，可定义iframe的id，是javaScript对象，如expandAttr={"frameID":"iframeID"}
 * @returns 返回生成窗口的UUID
 */
function newSWin(winOption) {
  if (!winOption) {
    $.messager.alert('新建窗口错误','请指定窗口参数!<br/>','error');
    return ;
  } else {
    if ((!winOption.url||$.trim(winOption.url)=="")&&(!winOption.content||$.trim(winOption.content)=="")) {
      $.messager.alert('新建窗口错误', '参数中的url或content属性至少指定一个!<br/>','error');
      return ;
    }
  }

  winOption.onBeforeClose=function(sWinId) {
    var _i=-1;
    var i=0;
    var len=sWinArray.length;
    for (;i<len;i++) {
      if (sWinId==sWinArray[i].sWinId) {
        _i=i;
        break;
      }
    }
    if (_i!=-1) sWinArray.splice(_i,1);
  };
  _zIndex = _zIndex+2;
  winOption.zIndex = _zIndex;
  var newSWin = $.spiritSimpleWin(winOption);
  var _uuid = newSWin.getId();
  sWinArray.push({"sWinId": _uuid, "sWinOBJ": newSWin});
  newSWin.open();
  return _uuid;
}

/**
 * 关闭并销毁简单模态窗口
 * @param sWinId 简单模态窗口的ID
 */
function closeSWin(sWinId) {
  var _i=-1;
  var i=0;
  var len=sWinArray.length;
  for (;i<len;i++) {
    if (sWinId==sWinArray[i].sWinId) {
      _i=i;
      sWinArray[i].sWinOBJ.close(sWinArray.length-1);
      break;
    }
  }
  if (_i==-1) $.messager.alert('关闭窗口错误','未找到Id='+winId+'的窗口，无法关闭!','error');
  else sWinArray.removeByIndex(_i);
}

/**
 * 根据winId得到简单模态窗口
 * @param sWinId 简单模态窗口的ID
 */
function getSWin(sWinId) {
  var ret=null;
  var i=0, len=sWinArray.length;
  for (; i<len; i++) {
    if (sWinId==sWinArray[i].sWinId) {
      ret=sWinArray[i].sWinOBJ;
      break;
    }
  }
  if (!ret) $.messager.alert('得到窗口对象错误', '未找到Id='+winId+'的窗口，无法得到窗口对象!','error');
  return ret;
}