function getBrowserVersion() {
  var browser = {};
  var userAgent = navigator.userAgent.toLowerCase();

  var s;
  (s = userAgent.match(/msie ([\d.]+)/)) ? browser.ie = s[1] :
  (s = userAgent.match(/net\sclr\s([\d.]+)/)) ? browser.ie = userAgent.match(/rv:([\d.]+)/)[1] :
  (s = userAgent.match(/firefox\/([\d.]+)/)) ? browser.firefox = s[1] :
  (s = userAgent.match(/chrome\/([\d.]+)/)) ? browser.chrome = s[1] :
  (s = userAgent.match(/opera.([\d.]+)/)) ? browser.opera = s[1] :
  (s = userAgent.match(/version\/([\d.]+).*safari/)) ? browser.safari = s[1] : 0;

  var version = browser.ie? 'msie '+browser.ie:
    browser.firefox?'firefox ' + browser.firefox:
    browser.chrome?'chrome ' + browser.chrome:
    browser.opera? 'opera ' + browser.opera:
    browser.safari?'safari ' + browser.safari:
    '未知';

  return version;
}
function _getExploreInfo() {
  var _b = getBrowserVersion();
  var ret = new Object();
  if (_b=="未知") {
    ret.exploreName="未知";
  } else {
  	if (_b.indexOf(" ")==-1) ret.exploreName=_b;
  	else {
      ret.exploreName=_b.substring(0, _b.indexOf(" "));
      ret.exploreVer=_b.substring(_b.indexOf(" ")+1);;
  	}
  }
  return ret;
}

var GatherFun = {
  _enterPage:function() {
    if (_gatherImg!=null) {
      var _src="http://localhost:1108/Chopin/vLog/_gather.jpg";
      //收集信息
      var params = {};
      if (document) {//文档信息，title，url以及来源url
        params.domain = document.domain || '';
        params.objUrl = window.location.href || '';
        params.objId = document.title || '';
        params.fromUrl = document.referrer || '';
      }
      if (window && window.screen) {//当前页面大小
        params.sh = window.screen.height||0;
        params.sw = window.screen.width||0;
        params.cd = window.screen.colorDepth||0;
      }
      if(navigator) params.lang = navigator.language||'';//当前所使用语言
      //用户信息？
      //页面语义信息
      //浏览器信息
      _temp = _getExploreInfo();
      if (_temp) {
      	params.exploreName=_temp.exploreName;
        if (_temp.exploreVer) params.exploreVer=_temp.exploreVer;
      }
      //解析_maq配置
//      if(_maq) {
//          for(var i in _maq) {
//              switch(_maq[i][0]) {
//                  case '_setAccount':
//                      params.account = _maq[i][1];
//                      break;
//                  default:
//                      break;
//              }
//          }
//      }
      //拼接参数串
      var args = '';
      for(var i in params) {
        if(args != '') args += '&';
        args += i + '=' + encodeURIComponent(params[i]);
      }
      _src+='?objType=99&'+args;
      alert(_src);
      _gatherImg.attr("src", _src);
    }
  }  
};

//进入时，即传入数据
var _gatherImg = null;
var done = false;

//加载过程
(function () {
  if (window.jQuery) {
    _gatherImg = $("<img id='_gatchImg'></img>");//窗口主对象
    GatherFun._enterPage();
  } else {
    var _jqS = document.createElement('script');
    _jqS.type = 'text/javascript';
    _jqS.language = 'javascript';
    _jqS.async = false;
    _jqS.src = 'http://www.wotingfm.com/Chopin/resources/plugins/jquery/jquery-1.10.2.min.js';
    _jqS.onload = _jqS.onreadystatechange = function() {
      if (!done && (!_jqS.readyState || _jqS.readyState == 'loaded' || _jqS.readyState == 'complete')){
        done = true;
        _jqS.onload = _jqS.onreadystatechange = null;
        _gatherImg = $("<img id='_gatchImg'></img>");//窗口主对象
        GatherFun._enterPage();
      }
    }
    var _jq = document.getElementsByTagName('script')[0];
    _jq.parentNode.insertBefore(_jqS, _jq);
  }
})();

//点击传入数据