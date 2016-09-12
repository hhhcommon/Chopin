//框架通用方法集，处理与框架结构相关逻辑
//需要window.utils.js的支持
/**
 * 得到框架首页
 */
function getMainPage() {
  var mainPage = null, win = window, pWin = null;
  while (!mainPage) {
    if (win.IS_MAINPAGE) mainPage = win;
    else {
      pWin = win.top;
      if (pWin==win||pWin==null) pWin = win.opener;
      if (pWin==win||pWin==null) break;
      win=pWin;
    }
  }
  return mainPage;
}

//对DOM对象的处理
/**
 * 找到顶层页面
 */
function getTopWin() {
  var topWin = window.top;
  while (topWin.openner) topWin=topWin.operner.top;
  return topWin;
}

//===========windows 处理
/**
 * 创建并打开easyUi窗口
 * @param winOption是一个js对象，目前支持如下参数
 * winOption.title 窗口标题
 * winOption.url 窗口内嵌的iframe的url
 * winOption.height 窗口高度
 * winOption.width 窗口宽度
 * winOption.icon_css 窗口图标，是css中的名称
 * winOption.icon_url 窗口图标，图标的url，若设置了此参数，icon_css失效
 * winOption.modal 是否是模态窗口，默认为模态窗口
 * winOption.expandAttr 窗口的扩展属性，可定义iframe的id，是javaScript对象，如expandAttr={"frameID":"iframeID"}
 * @returns 返回生成窗口的UUID
 */
function openWinInMain(winOption) {
  var mainPage = getMainPage();
  if (mainPage) return mainPage.newWin(winOption);//在框架主界面中打开easyUi窗口
  else return newWin(winOption);//在本界面中打开easyUi窗口
}

/**
 * 根据winId，得到easyUi窗口
 * @param winId 窗口的ID
 */
function getWinInMain(winId) {
  var mainPage = getMainPage();
  if (mainPage) mainPage.getWin(winId);
  else getWin(winId);
}

/**
 * 关闭并销毁easyUi窗口
 * @param winId 窗口的ID
 */
function closeWinInMain(winId) {
  var mainPage = getMainPage();
  if (mainPage) mainPage.closeWin(winId);
  else closeWin(winId);
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
function openSWinInMain(winOption) {
  var mainPage = getMainPage();
  if (mainPage) return mainPage.newSWin(winOption);//在框架主界面中打开简单模态窗口
  else return newSWin(winOption);//在本界面中打开简单模态窗口
}

/**
 * 根据winId，得到简单模态
 * @param winId 窗口的ID
 */
function getSWinInMain(winId) {
  var mainPage = getMainPage();
  var _win;
  if (mainPage) _win=mainPage.getSWin(winId);
  else _win=getSWin(winId);
  return _win;
}

/**
 * 关闭并销毁简单模态
 * @param winId 窗口的ID
 */
function closeSWinInMain(winId) {
  var mainPage = getMainPage();
  if (mainPage) mainPage.closeSWin(winId);
  else closeSWin(winId);
}

/**
 * 主页面中弹出对话框，调用easyui的message
 * @param title 标题
 * @param msg   消息内容
 * @param icon  图标 info,error
 * @param fn    回调函数
 */
function showAlert(title, msg, icon, fn) {
  var mPage=getMainPage();
  if (!mPage) alert(msg);
  else {
    if (icon&&fn) mPage.$.messager.alert(title,msg,icon,fn);
    else if (icon) mPage.$.messager.alert(title,msg,icon);  
    else if (title&&msg) mPage.$.messager.alert(title,msg);  
    else mPage.$.messager.alert(title,title);
  }
}

/**
 * 主页面中弹出对话框，调用easyui的message
 * @param title 标题
 * @param msg   消息内容
 * @param fn    回调函数
 */
function showConfirm(title, msg, fn) {
  var mPage=getMainPage();
  if (mPage) {
    if (fn) mPage.$.messager.confirm(title,msg,function(data){fn(data);});        
    else mPage.$.messager.confirm(title,msg,function(data){});        
  } else {
    var res = confirm(msg);
    if (fn) fn(res);
  }
}