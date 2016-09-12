/**
 * 通用Javascript函数，注意这里不要出现任何非基本javascript的方法，如jquery的方法。
 */

/**
 * 用于调试，得到对象中的元素及其值
 * @param obj 检测对象
 * @returns {String} 对象中元素的信息
 */
function allFields(obj) {
  var i=0;
  var props = "";
  if (obj==null) props="[allPrposCount=0]\nnull";
  else {
    for(var p in obj) {
      i=i+1;
      if (typeof(p)!="function") {
        if ((obj[p]+"").indexOf("[")!==0) {
          props += i+":"+p+"="+obj[p]+";\n";
        }
      }
    }
    props = "[allPrposCount="+i+"]\n"+props;
  }
  return props;
}

/**
 * 判断对象是否为空，为{}或null返回true
 */
function isEmpty(obj) {
  if (!obj) return true;
  for (var name in obj) return false;
  return true;
};

/**
 * 判断对象是否未定义，或者为空，或者长度为0
 */
function isUndefinedNullEmpty(obj) {
  if (!obj) return true;
  if (typeof(obj)=="string") return (obj.length==0);
  if (typeof(obj)=="object") for (var name in obj) return false;
  return false;
};

/**
 * 获得url中参数名为paramName的参数值
 * 如果url没有名称为paramName的参数返回null
 * 如果url中参数paramName的值为空，返回值也是空。(如:userName=&password=a或userName&password=a)取userName值为""
 * @returns {String} 在url中指定的paramName参数的值
 */
function getUrlParam(url, paramName) {
  if (!paramName&&!url) return null;
  var _url = url+"";
  if (_url.indexOf("?")==-1) return null;
  _url = "&"+_url.substring(_url.indexOf("?")+1);
  var pos=_url.lastIndexOf("&"+paramName+"=");
  if (pos==-1) return null;
  _url=_url.substring(pos+paramName.length+2);
  return _url.indexOf("&")==-1?_url:_url.substring(0, _url.indexOf("&"));
}

/**
 * 得到当前浏览器版本
 * @returns {String}
 */
function getBrowserVersion() {
  var browser = {};
  var userAgent = navigator.userAgent.toLowerCase();

  var s;
  (s = userAgent.match(/msie ([\d.]+)/)) ? browser.ie = s[1] :
  (s = userAgent.match(/net\sclr\s([\d.]+)/)) ? browser.ie = userAgent.match(/rv:([\d.]+)/)[1] :
  (s = userAgent.match(/firefox\/([\d.]+)/)) ? browser.firefox = s[1] :
  (s = userAgent.match(/chrome\/([\d.]+)/)) ? browser.chrome = s[1] :
  (s = userAgent.match(/opera.([\d.]+)/)) ? browser.opera = s[1] :
  (s = userAgent.match(/version\/([\d.]+).*safari/)) ? browser.safari = s[1]:0;

  var version = browser.ie? 'msie '+browser.ie:
    browser.firefox?'firefox ' + browser.firefox:
    browser.chrome?'chrome ' + browser.chrome:
    browser.opera? 'opera ' + browser.opera:
    browser.safari?'safari ' + browser.safari:
    '未知';

  return version;
}

/**
 * 生成UUID，默认为36位
 */
var CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".split("");  
function getUUID(len,radix) {
  var chars = CHARS, uuid = [];
  radix = radix||chars.length;
  if (len) {
    for (var i=0; i<len; i++) uuid[i] = chars[0 | Math.random()*radix];  
  } else {
    var r;
    uuid[8] = uuid[13] = uuid[18] = uuid[23] = "-";
    uuid[14] = "4";
    for (var i=0; i<36; i++) {
      if (!uuid[i]) {
        r = 0 | Math.random()*16;
        uuid[i] = chars[(i == 19)?(r&0x3)|0x8:r];
      }
    }
  }
  return uuid.join("");
}

/**
 * 把json串转换为对象
 * @param jsonStr json串
 * @returns javascript对象
 */
function str2JsonObj(jsonStr) {
  return eval("(" +jsonStr+ ")");
}

/**
 * 将form表单元素的值转化为对象。
 * 在ajax提交form时，可提交序列化后的对象。
 * 注意，若其中有file字段，将不能正确处理。
 * param form 需要序列化的form元素的id
 */
function formField2Object(formId) {
  var o = {};
  var _form = $("#"+formId).form();
  if (_form) {
    $.each(_form.serializeArray(),function(){
      if(o[this['name']]){
        o[this['name']]=o[this['name']]+","+this['value'];
      }else{
        o[this['name']]=this['value'];
      }
    });
  }
  return o;
}

function jqueryColor2HexColor(jqueryColor) {
  var rgb = jqueryColor.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
  if (!rgb) return "#FFFFFF";
  if (rgb.length!=4) return "#FFFFFF";
  rgb= "#" + hex(rgb[1]) + hex(rgb[2]) + hex(rgb[3]);
  return rgb;
  function hex(x) {
    return ("0" + parseInt(x).toString(16)).slice(-2);
  };
}

/**
 * 判断串是否是合法的Url
 */
function isURL(str) {
  //转换为小写
	/**
  str = str.toLowerCase();
  String domainRules = "com.cn|net.cn|org.cn|gov.cn|com.hk|公司|中国|网络|com|net|org|int|edu|gov|mil|arpa|Asia|biz|info|name|pro|coop|aero|museum|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cf|cg|ch|ci|ck|cl|cm|cn|co|cq|cr|cu|cv|cx|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|eh|es|et|ev|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gh|gi|gl|gm|gn|gp|gr|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|in|io|iq|ir|is|it|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|ml|mm|mn|mo|mp|mq|mr|ms|mt|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nt|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|pt|pw|py|qa|re|ro|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|st|su|sy|sz|tc|td|tf|tg|th|tj|tk|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|va|vc|ve|vg|vn|vu|wf|ws|ye|yu|za|zm|zr|zw";
  String regex = "^((https|http|ftp|rtsp|mms)?://)"  
    + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@  
    + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184  
    + "|" // 允许IP和DOMAIN（域名） 
    + "(([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]+\\.)?" // 域名- www.  
    + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名  
    + "("+domainRules+"))" // first level domain- .com or .museum  
    + "(:[0-9]{1,4})?" // 端口- :80  
    + "((/?)|" // a slash isn't required if there is no file name  
    + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";  
  Pattern pattern = Pattern.compile(regex);
  Matcher isUrl = pattern.matcher(str);
  return isUrl.matches();
  */
}
/**
 * 对Date的扩展，将 Date 转化为指定格式的String 
 * 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， 
 * 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
 * 例子： 
 * (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 
 * (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
 * @param date.Format
 * @returns 时间的字符串类型
 * @author: meizz
 */
Date.prototype.format = function(fmt) {
  var o = {
   "M+":this.getMonth()+1,                 //月份
   "d+":this.getDate(),                    //日
   "h+":this.getHours()%12==0?12:this.getHours()%12,//12小时
   "H+":this.getHours(), //24小时		   
   "m+":this.getMinutes(),                 //分
   "s+":this.getSeconds(),                 //秒
   "q+":Math.floor((this.getMonth()+3)/3), //季度
   "S" :this.getMilliseconds()             //毫秒
  };
  var fmt;
  if (/(y+)/.test(fmt)) fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4-RegExp.$1.length));
  for (var k in o) {
    if (new RegExp("("+ k +")").test(fmt)) {
      if (k=="S") {
      	fmt = fmt.replace("S", ("00"+o["S"]).substr(("00"+o["S"]).length-3));
      } else {
        fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]):(("00"+ o[k]).substr((""+ o[k]).length))); 
      }
    }
  }
  return fmt;
}

/** 
 * 取当前月的第一天，用fmt进行格式化后返回
 */
function getCurMonthFirstDay_format(fmt) {
  return getDateMonthFirstDay_format(new Date(), fmt);
}
/** 
 * 取当前月的最后一天，用fmt进行格式化后返回
 */
function getCurMonthLastDay_format(fmt) {
  return getDateMonthLastDay_format(new Date(), fmt);
}
/** 
 * 取某时间所在月的第一天，用fmt进行格式化后返回
 */
function getDateMonthFirstDay_format(date, fmt) {
  return (new Date(date.getFullYear(), date.getMonth(), 1)).format(fmt);
}
/** 
 * 取当前月的最后一天，用fmt进行格式化后返回
 */
function getDateMonthLastDay_format(date, fmt) {
  return (new Date((new Date(date.getFullYear(), date.getMonth()+1, 1)).getTime()-(1000*60*60*24))).format(fmt);
}

/**
 * 从Html中得到干净的字符串
 */
function getPureStrFromHTML(htmlStr) {
  var pureStr=htmlStr.replace(/(\n)/g, "");
  pureStr=pureStr.replace(/(\t)/g, "");
  pureStr=pureStr.replace(/(\r)/g, "");
  pureStr=pureStr.replace(/<\/?[^>]*>/g, "");
  return pureStr.replace(/\s*/g, "");  
}

//扩展方法
/**
 * 扩展String属性：得到中英混排文字符串长度
 */
String.prototype.cnLength = function () {
  return ((this.replace(/[^x00-xFF]/g, "**")).length);
};

/**
 * 删除数组中的元素
 */
Array.prototype.removeByIndex = function (i){
  if (i>=0 && i<this.length) this.splice(i,1);
};

/**
 * 在指定位置i插入对象item
 * @param i 插入的数组中的位置
 * @param item 元素对象
 */
Array.prototype.insertAt = function (index, item) {
  this.splice(index, 0, item);
};