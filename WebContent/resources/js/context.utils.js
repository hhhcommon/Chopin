/****************************************
 * 上下文js
 ****************************************/

/**
 * 得到框架首页
 */
function getMainPage() {
  var mainPage=null, win=window, pWin=null;
  while (!mainPage) {
    if (win.IS_MAINPAGE) mainPage=win;
    else {
      pWin=win.top;
      if (pWin==win||pWin==null) pWin=win.opener;
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
  var topWin=window.top;
  while (topWin.openner) topWin=topWin.operner.top;
  return topWin;
}

/**
 * 得到全局变量
 */
function getGlobelData() {
  var _mp=getMainPage();
  return (_mp?_mp.GLOBAL_DATA:null);
}

/**
 * 得到根地址
 */
function getRootPath() {
  var _gd=getGlobelData();
  return (_gd?(_gd.rootPath?_gd.rootPath:"/"):"/");
}

/**
 * 全局变量处理
 */
var GLOBEL={
  /*
   * 获得全局变量中的一个属性
   * @param attrName 属性名称
   * @return 返回全局变量中属性attrName所对应的属性值
   *         1-若参数attrName不存在，则返回void(0)
   *         2-若全局变量本身不存在，则返回void(0)
   *         3-若全局变量中不存在该属性，返回null
   */
  getAttr: function(attrName) {
    if (!attrName) return void(0);
    var _gd=getGlobelData();
    if (!_gd) return void(0);
    return (_gd[attrName]?_gd[attrName]:null);
  },
  /*
   * 设置一个全局变量中的属性
   * @param attrName 属性名称
   * @param value 属性值
   * @return 若设置成功返回true
   *         1-若参数attrName不存在，则返回void(0)
   *         2-若参数value不存在，则返回void(0)
   *         3-若全局变量本身不存在，则返回void(0)
   *         4-若attrName已经在全局变量中存在，且不为空，则返回false
   */
  setAttr: function(attrName, value) {
    if (!attrName) return void(0);
    if (value===void(0)) return void(0);
    var _gd=getGlobelData();
    if (!_gd) return void(0);

    if (!(_gd[attrName]===void(0))) return false;
    _gd[attrName]=value;
    return true;
  },
  /*
   * 强制设置一个全局变量中的属性
   * @param attrName 属性名称
   * @param value 属性值
   * @return 若设置成功返回true
   *         1-若参数attrName不存在，则返回void(0)
   *         2-若参数value不存在，则返回void(0)
   *         3-若全局变量本身不存在，则返回void(0)
   */
  forceSetAttr: function(attrName, value) {
    if (!attrName) return void(0);
    if (value===void(0)) return void(0);
    var _gd=getGlobelData();
    if (!_gd) return void(0);

    _gd[attrName]=value;
    return true;
  },
  /*
   * 修改一个属性的值
   * @param attrName 属性名称
   * @param value 属性值
   * @return 若修改成功返回true
   *         1-若参数attrName不存在，则返回void(0)
   *         2-若参数value不存在，则返回void(0)
   *         3-若全局变量本身不存在，则返回void(0)
   *         4-若attrName未在全局变量中，返回false，无法修改
   */
  updateAttr: function(attrName, value) {
    if (!attrName) return void(0);
    if (value===void(0)) return void(0);
    var _gd=getGlobelData();
    if (!_gd) return void(0);

    if (_gd[attrName]===void(0)) return false;
    _gd[attrName]=value;
    return true;
  },
  /*
   * 删除一个属性本身
   * @param attrName 属性名称
   * @return 若删除成功返回true
   *         1-若参数attrName不存在，则返回void(0)
   *         2-若全局变量本身不存在，则返回void(0)
   *         3-若attrName未在全局变量中，返回false，无法删除，否则返回true，删除成功
   */
  removeAttr: function(attrName) {
    if (!attrName) return void(0);
    var _gd=getGlobelData();
    if (!_gd) return void(0);

    if (_gd[attrName]===void(0)) return false;
    delete _gd[attrName];
    return true;
  },
  /*
   * 清除属性的值，使其值为null
   * @param attrName 属性名称
   * @return 若清空成功返回true
   *         1-若参数attrName不存在，则返回void(0)
   *         2-若全局变量本身不存在，则返回void(0)
   *         3-若attrName未在全局变量中，返回false，无法清空，否则返回true，设置attrName的值为null
   */
  cleanAttr: function(attrName) {
    if (!attrName) return void(0);
    var _gd=getGlobelData();
    if (!_gd) return void(0);

    if (_gd[attrName]===void(0)) return false;
    _gd[attrName]=null;
    return true;
  }
}