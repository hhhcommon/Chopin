/**
 * jQuery spirit 通用方法
 */

(function($) {
  $.fn.spiritUtils = function(options, param) {
    if (typeof options == 'string'){
      return $.fn.spiritUtils.methods[options](this, param);
    }
  };

  $.fn.spiritUtils.methods = {
    //计算对象的绝对宽度
    getAbsWidth: function (jObj) {
      if (jObj.length!=1) return "不能对多于一个对象计算绝对宽度！";
      return jObj.width()+($(jObj).css("margin-left")==""?0:parseFloat($(jObj).css("margin-left")))
        +(($(jObj).css("border-left-width")=="medium"||$(jObj).css("border-left-width")=="")?0:parseFloat($(jObj).css("border-left-width")))
        +($(jObj).css("padding-left")==""?0:parseFloat($(jObj).css("padding-left")))
        +($(jObj).css("padding-right")==""?0:parseFloat($(jObj).css("padding-right")))
        +(($(jObj).css("border-right-width")=="medium"||$(jObj).css("border-right-width")=="")?0:parseFloat($(jObj).css("border-right-width")))
        +($(jObj).css("margin-right")==""?0:parseFloat($(jObj).css("margin-right")));
    },
    //计算对象的绝对高度
    getAbsHeight: function (jObj) {
      if (jObj.length!=1) return "不能对多于一个对象计算绝对高度！";
      return jObj.height()+($(jObj).css("margin-top")==""?0:parseFloat($(jObj).css("margin-top")))
        +(($(jObj).css("border-top-width")=="medium"||$(jObj).css("border-top-width")=="")?0:parseFloat($(jObj).css("border-top-width")))
        +($(jObj).css("padding-top")==""?0:parseFloat($(jObj).css("padding-top")))
        +($(jObj).css("padding-bottom")==""?0:parseFloat($(jObj).css("padding-bottom")))
        +(($(jObj).css("border-bottom-width")=="medium"||$(jObj).css("border-bottom-width")=="")?0:parseFloat($(jObj).css("border-bottom-width")))
        +($(jObj).css("margin-bottom")==""?0:parseFloat($(jObj).css("margin-bottom")));
    },
    //计算对象的显示宽度
    getViewWidth: function (jObj) {
      if (jObj.length!=1) return "不能对多于一个对象计算显示宽度！";
      return jObj.width()+(($(jObj).css("border-left-width")=="medium"||$(jObj).css("border-left-width")=="")?0:parseFloat($(jObj).css("border-left-width")))
        +($(jObj).css("padding-left")==""?0:parseFloat($(jObj).css("padding-left")))
        +($(jObj).css("padding-right")==""?0:parseFloat($(jObj).css("padding-right")))
        +(($(jObj).css("border-right-width")=="medium"||$(jObj).css("border-right-width")=="")?0:parseFloat($(jObj).css("border-right-width")));
    },
    //计算对象的显示高度
    getViewHeight: function (jObj) {
      if (jObj.length!=1) return "不能对多于一个对象计算显示高度！";
      return jObj.height()+(($(jObj).css("border-top-width")=="medium"||$(jObj).css("border-top-width")=="")?0:parseFloat($(jObj).css("border-top-width")))
        +($(jObj).css("padding-top")==""?0:parseFloat($(jObj).css("padding-top")))
        +($(jObj).css("padding-bottom")==""?0:parseFloat($(jObj).css("padding-bottom")))
        +(($(jObj).css("border-bottom-width")=="medium"||$(jObj).css("border-bottom-width")=="")?0:parseFloat($(jObj).css("border-bottom-width")));
    },
    //根据显示宽度，得到对象的设置宽度
    getSetWidth: function (jObj, viewWidth) {
      if (jObj.length!=1) return "不能对多于一个对象计算设置宽度！";
      var retW = parseFloat(viewWidth)-((($(jObj).css("border-left-width")=="medium"||$(jObj).css("border-left-width")=="")?0:parseFloat($(jObj).css("border-left-width")))
        +($(jObj).css("padding-left")==""?0:parseFloat($(jObj).css("padding-left")))
        +($(jObj).css("padding-right")==""?0:parseFloat($(jObj).css("padding-right")))
        +(($(jObj).css("border-right-width")=="medium"||$(jObj).css("border-right-width")=="")?0:parseFloat($(jObj).css("border-right-width"))));
      return retW>0?retW:0;
    },
    //根据显示宽度，设置对象的宽度
    setWidthByViewWidth: function (jObj, viewWidth) {
      if (jObj.length!=1) return "不能对多于一个对象计算设置宽度并设置！";
      var retW = parseFloat(viewWidth)-((($(jObj).css("border-left-width")=="medium"||$(jObj).css("border-left-width")=="")?0:parseFloat($(jObj).css("border-left-width")))
        +($(jObj).css("padding-left")==""?0:parseFloat($(jObj).css("padding-left")))
        +($(jObj).css("padding-right")==""?0:parseFloat($(jObj).css("padding-right")))
        +(($(jObj).css("border-right-width")=="medium"||$(jObj).css("border-left-width")=="")?0:parseFloat($(jObj).css("border-right-width"))));
      $(jObj).css("width", retW>0?retW:0);
      return retW>0?retW:0;
    },
    //根据显示高度，得到对象的设置高度
    getSetHeight: function (jObj, viewHeight) {
      if (jObj.length!=1) return "不能对多于一个对象计算设置高度！";
      var retH = parseFloat(viewHeight)-((($(jObj).css("border-top-width")=="medium"||$(jObj).css("border-top-width")=="")?0:parseFloat($(jObj).css("border-top-width")))
        +($(jObj).css("padding-top")==""?0:parseFloat($(jObj).css("padding-top")))
        +($(jObj).css("padding-bottom")==""?0:parseFloat($(jObj).css("padding-bottom")))
        +($(jObj).css(("border-bottom-width")=="medium"||$(jObj).css("border-bottom-width")=="")?0:parseFloat($(jObj).css("border-bottom-width"))));
      return retH>0?retH:0;
    },
    //根据显示高度，设置对象的高度
    setHeightByViewHeight: function (jObj, viewHeight) {
      if (jObj.length!=1) return "不能对多于一个对象计算设置高度并设置！";
      var retH = parseFloat(viewHeight)-((($(jObj).css("border-top-width")=="medium"||$(jObj).css("border-top-width")=="")?0:parseFloat($(jObj).css("border-top-width")))
        +($(jObj).css("padding-top")==""?0:parseFloat($(jObj).css("padding-top")))
        +($(jObj).css("padding-bottom")==""?0:parseFloat($(jObj).css("padding-bottom")))
        +(($(jObj).css("border-bottom-width")=="medium"||$(jObj).css("border-bottom-width")=="")?0:parseFloat($(jObj).css("border-bottom-width"))));
      $(jObj).css("height", retH>0?retH:0);
      return retH>0?retH:0;
    }
  };
})(jQuery);