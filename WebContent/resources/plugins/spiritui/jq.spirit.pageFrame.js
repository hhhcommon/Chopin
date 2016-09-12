/**
 * jQuery spiritui-pageFrame 精灵组件
 * 页面框架。
 * 包括头部，中部，和尾部的一个自适应，可调整的页面框架。
 *
 * Copyright (c) 2014.7 wh
 *
 * Licensed same as jquery - MIT License
 * http://www.opensource.org/licenses/mit-license.php
 */
(function($) {
  //本控件内的全局变量
  var _hScrollbarWidth = 0; //纵向滚动条宽度
  var _wScrollbarWidth = 0; //横向滚动条宽度
  var _topFlag4foot = -1; //脚部，上标志
  var _bottomFlag4foot = -1; //脚部，下标志
  var _hasTop=false, _hasFoot=false;
  var INIT_PARAM={};//初始化参数
  var _bv = getBrowserVersion();
  var _ie8H1=1, _ie8H2=2, _ie8W1=1, _ie8W2=2;
  //默认属性
  var defaults = {
    //页面中所用到的元素的id，只用到三个Div，另，这三个div应在body层
    pageObjs: {
      topId: "topSegment", //头部Id
      mainId: "mainSegment", //主体Id
      footId: "footSegment" //尾部Id
    },

    page_width: 0, //主页面的宽度。<0：宽度不控制；0：宽度自适应；>0：宽度值，页面定宽
    page_height: 0, //主页面的高度。<0：高度不控制；0：高度自适应；>0：高度值，页面定高

    top_height: 120, //顶部高度
    top_shadow_color: null, //头部阴影颜色
    foot_height: 40, //脚部高度

    top_peg: false, //是否钉住头部在顶端。false：顶部随垂直滚动条移动(浮动)；true：顶部钉在顶端
    foot_peg: false, //是否钉住脚部在底端。false：脚部随垂直滚动条移动(浮动)；true：脚部钉在底端

    iframe_height_flag: 1 //具体功能区域（可能是整个中部，也可能是带左侧导航的中部）的iframe高度标志。1：iframe高度与框架匹配；非1：框架高度适应iframe内部高度(反向适应)
  };

  function initPosition() {
    //1-调整中间主体
    $("#_main").css({
      "left": getLeft(), "width": getWidth(), //X轴，宽
      "top": $("body").css("margin-top"), "height": getHeight() //Y轴，高
    });
    //1.1-若出现滚动条，进行处理
    var rh=parseFloat($("#_main").css("height")), rw=parseFloat($("#_main").css("width"));
    if (INIT_PARAM.page_width<=0)  {//若为自适应宽
      if ((rh+caculateHeightOffSet())>wHeight()) rw -= _hScrollbarWidth;//若出现纵向滚动条，则宽度为页面宽度减去滚动条宽度
      //ie兼容
      if (_bv.indexOf("msie")==0) {
        var _v = parseFloat(_bv.substring(5));
        if (_v==8) {
          if (INIT_PARAM.page_height>0) rw -= _hScrollbarWidth;
        }
      }
      if (rw>INIT_PARAM.win_min_width) $("#_main").css({"width": rw});
    }
    if (INIT_PARAM.page_height<=0) {//若为自适应高
      if ((rw+caculateWidthOffSet())>wWidth()) rh -= _wScrollbarWidth;//若出现横向滚动条，则宽度为页面宽度减去滚动条宽度
      if (_bv.indexOf("msie")==0) {
        var _v = parseFloat(_bv.substring(5));
        if (_v==8) rh -= _wScrollbarWidth;
      }
      if (rh>INIT_PARAM.win_min_height) $("#_main").css({"height": rh});
    }
    //2-调整顶部
    if (_hasTop) {
      $("#"+INIT_PARAM.pageObjs.topId).css({
        "position": INIT_PARAM.top_peg?"absolute":"fixed", //形式
        "left": parseFloat($("#_main").css("left"))+($("#_main").css("margin-left")==""?0:parseFloat($("#_main").css("margin-left"))), //X轴，宽
        "width": getViewWidth(INIT_PARAM.pageObjs.topId, "_main"),
        "top": parseFloat($("body").css("margin-top"))+($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top"))), //Y轴，高
        "height":parseInt(INIT_PARAM.top_height)+"px"
      });
    };
    //3-调整脚部
    if (_hasFoot) {
      $("#"+INIT_PARAM.pageObjs.footId).css({
        "position": INIT_PARAM.foot_peg?"absolute":"fixed", //形式
        "left": parseFloat($("#_main").css("left"))+($("#_main").css("margin-left")==""?0:parseFloat($("#_main").css("margin-left"))), //X轴，宽
        "width": getViewWidth(INIT_PARAM.pageObjs.footId, "_main"),
        "height":parseFloat(INIT_PARAM.foot_height) //Y轴，高
      });
    };
    //4-中间分体部分
    var _topHeight = _hasTop?(getViewHeight("_top", INIT_PARAM.pageObjs.topId)-($("#_main").css("padding-top")==""?0:parseFloat($("#_main").css("padding-top")))
      -(($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")?0:parseFloat($("#_main").css("border-top-width")))):0;
    var _footHeight = _hasFoot?(getViewHeight("_foot", INIT_PARAM.pageObjs.footId)-($("#_main").css("padding-bottom")==""?0:parseFloat($("#_main").css("padding-bottom")))
      -(($("#_main").css("border-bottom-width")=="medium"||$("#_main").css("border-bottom-width")=="")?0:parseFloat($("#_main").css("border-bottom-width")))):0;
    $("#_top").css({"width":$("#_main").css("width"), "height": _topHeight});
    if (!_hasTop) $("#_top").hide();
    $("#_foot").css({"width":$("#_main").css("width"), "height": _footHeight});
    if (!_hasFoot) $("#_foot").hide();
    var _view = $("#"+INIT_PARAM.pageObjs.mainId);
    var _ch = parseFloat($("#_main").css("height"))-parseFloat(_topHeight)-parseFloat(_footHeight)
      -((_view.css("margin-top")==""?0:parseFloat(_view.css("margin-top")))+(_view.css("margin-bottom")==""?0:parseFloat(_view.css("margin-bottom")))
      +(_view.css("padding-top")==""?0:parseFloat(_view.css("padding-top")))+(_view.css("padding-bottom")==""?0:parseFloat(_view.css("padding-bottom")))
      +((_view.css("border-top-width")=="medium"||_view.css("border-top-width")=="")?0:parseFloat(_view.css("border-top-width")))+(_view.css("border-bottom-width")=="medium"?0:parseFloat(_view.css("border-bottom-width"))));
    $("#"+INIT_PARAM.pageObjs.mainId).css({"width":getViewWidth(INIT_PARAM.pageObjs.mainId, "_main"), "height": _ch});
    //5-调整脚部top
    if (_hasFoot) {
      $("#"+INIT_PARAM.pageObjs.footId).css({"top":$("#_foot")[0].offsetTop+($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top")))
        +($("body").css("padding-top")==""?0:parseFloat($("body").css("padding-top")))+($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))
        +(($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")?0:parseFloat($("#_main").css("border-top-width")))
        -($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))});
      if (!INIT_PARAM.foot_peg) {//浮动脚部
        var _offsetHeight = $(document).scrollTop()+$(window).height();//窗口绝对高度
        if (INIT_PARAM.page_height>0) {
          if (($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top"))
              +($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))
              +(($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")?0:parseFloat($("#_main").css("border-top-width")))
              +($("#_main").css("padding-top")==""?0:parseFloat($("#_main").css("padding-top")))
              +parseFloat($("#_main").css("height"))
              +($("#_main").css("padding-bottom")==""?0:parseFloat($("#_main").css("padding-bottom")))
              +(($("#_main").css("border-bottom-width")=="medium"||$("#_main").css("border-bottom-width")=="")?0:parseFloat($("#_main").css("border-bottom-width"))))<_offsetHeight) return;
        }
        if (_topFlag4foot==-1) _topFlag4foot = ($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top")))+parseFloat(INIT_PARAM.win_min_height)
          +($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))
          +(($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")?0:parseFloat($("#_main").css("border-top-width")))
          +($("#_main").css("padding-top")==""?0:parseFloat($("#_main").css("padding-top")))
          +($("#_main").css("border-bottom-width")=="medium"?0:parseFloat($("#_main").css("border-bottom-width")))+parseFloat($("#_main").css("padding-bottom"))
          -parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("height"))
          -(($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")))
          -(($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")));
        if (_bottomFlag4foot==-1) _bottomFlag4foot = _topFlag4foot
          +($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))
          +(($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")))
          +($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")))
          +parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("height"))
          +($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")))
          +(($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")));
        var _staticTop4foot=$(window).height()-parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("height"))
          -($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")))
          -(($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")))
          -(($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")));
        var _newTop=0;
        if (_offsetHeight<=_bottomFlag4foot) _newTop = _topFlag4foot; //小于尺度+//高度内
        else _newTop = _staticTop4foot;//大于尺度
        $("#"+INIT_PARAM.pageObjs.footId).css({"top": _newTop});
      }
    }
    if (INIT_PARAM.myInit) INIT_PARAM.myInit();
  };

  function resizePosition() {
    //ie兼容
    if (_bv.indexOf("msie")==0) {
      var _v = parseFloat(_bv.substring(5));
      if (_v==8) {
        _ie8H1=$("#_main").css("height"), _ie8W1=$("#_main").css("width");
        if (_ie8H1==_ie8H2&&_ie8W1==_ie8W2) {
          setTimeout(function(){
            _ie8H1=0,_ie8H2=-1,_ie8W1=0,_ie8W2=-1;
          }, 100);
          return;
        }
      }
    }
    //1-调整中间主体
    $("#_main").css({
      "left": getLeft(), "width": getWidth(), //X轴，宽
      "height": getHeight() //Y轴，高
    });
    //1.1-若出现滚动条，进行处理
    var rh=parseFloat($("#_main").css("height")), rw=parseFloat($("#_main").css("width"));
    if (INIT_PARAM.page_width<=0)  {//若为自适应宽
      if ((rh+caculateHeightOffSet())>wHeight()) rw -= _hScrollbarWidth;//若出现纵向滚动条，则宽度为页面宽度减去滚动条宽度
      if (rw>INIT_PARAM.win_min_width) $("#_main").css({"width": rw});
    }
    if (INIT_PARAM.page_height<=0) {//若为自适应高
      if ((rw+caculateWidthOffSet())>wWidth()) rh -= _wScrollbarWidth;//若出现横向滚动条，则宽度为页面宽度减去滚动条宽度
      //ie8兼容
      if (_bv.indexOf("msie")==0) {
        var _v = parseFloat(_bv.substring(5));
        if (_v==8) rh -= _wScrollbarWidth;
      }
      if (rh>INIT_PARAM.win_min_height) $("#_main").css({"height": rh});
    }
    //2-调整顶部
    if (_hasTop) {
      $("#"+INIT_PARAM.pageObjs.topId).css({
        "width": getViewWidth(INIT_PARAM.pageObjs.topId, "_main"),
        "left": parseFloat($("#_main").css("left"))+($("#_main").css("margin-left")==""?0:parseFloat($("#_main").css("margin-left")))-$(document).scrollLeft()
      });
    }
    //3-调整脚部
    if (_hasFoot) {
      $("#"+INIT_PARAM.pageObjs.footId).css({
        "width": getViewWidth(INIT_PARAM.pageObjs.footId, "_main"),
        "left": parseFloat($("#_main").css("left"))+($("#_main").css("margin-left")==""?0:parseFloat($("#_main").css("margin-left")))-$(document).scrollLeft()
      });
      if (INIT_PARAM.foot_peg) $("#"+INIT_PARAM.pageObjs.footId).css({"left": parseFloat($("#_main").css("left"))+($("#_main").css("margin-left")==""?0:parseFloat($("#_main").css("margin-left")))}); //钉住脚部
    }
    //4-中间分体部分
    if (_hasTop) $("#_top").css({"width":$("#_main").css("width")});
    if (_hasFoot) $("#_foot").css({"width":$("#_main").css("width")});
    var _view = $("#"+INIT_PARAM.pageObjs.mainId);
    var _ch = parseFloat($("#_main").css("height"))-parseFloat($("#_top").css("height"))-parseFloat($("#_foot").css("height"))
      -((_view.css("margin-top")==""?0:parseFloat(_view.css("margin-top")))+(_view.css("margin-bottom")==""?0:parseFloat(_view.css("margin-bottom")))
      +(_view.css("padding-top")==""?0:parseFloat(_view.css("padding-top")))+(_view.css("padding-bottom")==""?0:parseFloat(_view.css("padding-bottom")))
      +((_view.css("border-top-width")=="medium"||_view.css("border-top-width")=="")?0:parseFloat(_view.css("border-top-width")))
      +((_view.css("border-bottom-width")=="medium"||_view.css("border-bottom-width")=="")?0:parseFloat(_view.css("border-bottom-width"))));
    $("#"+INIT_PARAM.pageObjs.mainId).css({"width":getViewWidth(INIT_PARAM.pageObjs.mainId, "_main"), "height": _ch});
    //5-调整脚部top
    if (_hasTop) {
      $("#"+INIT_PARAM.pageObjs.footId).css({"top":$("#_foot")[0].offsetTop
        +($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top")))+($("body").css("padding-top")==""?0:parseFloat($("body").css("padding-top")))
        +($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))
        +(($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")?0:parseFloat($("#_main").css("border-top-width")))
        -($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))});
      if (!INIT_PARAM.foot_peg) {//浮动脚部
        var _offsetHeight = $(document).scrollTop()+$(window).height();//窗口绝对高度
        if (INIT_PARAM.page_height>0) {
          if ((("body").css("margin-top")==""?0:(parseFloat($("body").css("margin-top")))
              +($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))
              +(($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")?0:parseFloat($("#_main").css("border-top-width")))
              +($("#_main").css("padding-top")==""?0:parseFloat($("#_main").css("padding-top")))
              +parseFloat($("#_main").css("height"))
              +($("#_main").css("padding-bottom")==""?0:parseFloat($("#_main").css("padding-bottom")))
              +(($("#_main").css("border-bottom-width")=="medium"||$("#_main").css("border-bottom-width")=="")?0:parseFloat($("#_main").css("border-bottom-width"))))<_offsetHeight) return;
        }
        if (_topFlag4foot==-1) _topFlag4foot = ($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top")))+parseFloat(INIT_PARAM.win_min_height)
          +($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))
          +(($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")?0:parseFloat($("#_main").css("border-top-width")))
          +($("#_main").css("padding-top")==""?0:parseFloat($("#_main").css("padding-top")))
          +(($("#_main").css("border-bottom-width")=="medium"||$("#_main").css("border-bottom-width")=="")?0:parseFloat($("#_main").css("border-bottom-width")))
          +($("#_main").css("padding-bottom")==""?0:parseFloat($("#_main").css("padding-bottom")))
          -parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("height"))
          -(($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")))
          -(($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")));
        if (_bottomFlag4foot==-1) _bottomFlag4foot = _topFlag4foot
          +($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))
          +(($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")))
          +($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")))
          +parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("height"))
          +($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")))
          +(($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")));
        var _staticTop4foot=$(window).height()-parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("height"))
          -($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")))
          -(($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")))
          -($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")))
          -(($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")));
        var _newTop=0;
        if (_offsetHeight<=_topFlag4foot) _newTop = _topFlag4foot; //小于尺度
        else if (_offsetHeight>_topFlag4foot&&_offsetHeight<=_bottomFlag4foot) _newTop = _topFlag4foot-$(document).scrollTop(); //高度内
        else _newTop = _staticTop4foot;//大于尺度
        $("#"+INIT_PARAM.pageObjs.footId).css({"top": _newTop});
      }
    }
    //6-调整晕左边距
    if ($("body>div#_topunder").length==1) {
      if (!$("body>div#_topunder").is(":hidden")) $("body>div#_topunder").css({
        "left": $("#"+INIT_PARAM.pageObjs.topId).css("left"),
        "width": getViewWidth(INIT_PARAM.pageObjs.footId, "_main")
      });
    };
    if (INIT_PARAM.myResize) INIT_PARAM.myResize();

    pfOnScroll();    
    //ie兼容
    if (_bv.indexOf("msie")==0) {
      var _v = parseFloat(_bv.substring(5));
      if (_v==8) _ie8H2=$("#_main").css("height"), _ie8W2=$("#_main").css("width");
    }
  }

  function pfOnScroll() {
    //1-调整顶部
    if (!INIT_PARAM.top_peg&&_hasTop) {
      //Y轴方向
      var _top = parseFloat($("body").css("margin-top"))+($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))-$(document).scrollTop();
      $("#"+INIT_PARAM.pageObjs.topId).css({"top": (_top>($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top"))))?_top:($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top")))});
      //X轴方向
      $("#"+INIT_PARAM.pageObjs.topId).css({"left": parseFloat($("#_main").css("left"))+($("#_main").css("margin-left")==""?0:parseFloat($("#_main").css("margin-left")))-$(document).scrollLeft()});
      //设置晕效果
      if (_top<0&&_hasTop&&!INIT_PARAM.top_peg) {//出现头部下边的晕区域，使得更好看
        if ($("body>div#_topunder").length==0) {
          $("body").append("<div id='_topunder'></div>");
          var topunder = $("body>div#_topunder");
          var topSegment = $("#"+INIT_PARAM.pageObjs.topId);
          //取晕效果颜色；先看是否有设定，若没有设定取头部的下边框，若下边框为0，取底色
          var _topShadowColor = null;
          if (INIT_PARAM.top_shadow_color) _topShadowColor=INIT_PARAM.top_shadow_color;
          else {//下边框
            if (((topSegment.css("border-bottom-width")=="medium"||topSegment.css("border-bottom-width")=="")?0:parseFloat(topSegment.css("border-bottom-width")))>0) {
              _topShadowColor = jqueryColor2HexColor(topSegment.css("border-bottom-color"));
            } else _topShadowColor = jqueryColor2HexColor(topSegment.css("background-color"));
          }
          topunder.css({"border":"1px solid "+_topShadowColor, "padding":"0", "margin":"0",
            "margin-left": topSegment.css("margin-left"),
            "margin-right": topSegment.css("margin-right"),
            "padding-left": topSegment.css("padding-left"),
            "padding-right": topSegment.css("padding-right"),
            "border-left-color": topSegment.css("border-left-color"),
            "border-left-style": topSegment.css("border-left-style"),
            "border-left-width": topSegment.css("border-left-width"),
            "border-right-color": topSegment.css("border-right-color"),
            "border-right-style": topSegment.css("border-right-style"),
            "border-right-width": topSegment.css("border-right-width"),
            "left": parseFloat(topSegment.css("left")),
            "width": parseFloat(topSegment.css("width")),
            "z-index": topSegment.css("z-index")-1,
            "position": "fixed", "height": "1px",
            "top": parseFloat(topSegment.css("top"))+parseFloat(topSegment.css("height"))+(topSegment.css("padding-top")==""?0:parseFloat(topSegment.css("padding-top")))
               +(topSegment.css("padding-bottom")==""?0:parseFloat(topSegment.css("padding-bottom")))+(topSegment.css("margin-top")==""?0:parseFloat(topSegment.css("margin-top")))
               +(topSegment.css("margin-bottom")==""?0:parseFloat(topSegment.css("margin-bottom")))+(topSegment.css("border-top-width")==""?0:parseFloat(topSegment.css("border-top-width")))
               +((topSegment.css("border-bottom-width")=="medium"||topSegment.css("border-bottom-width")=="")?0:parseFloat(topSegment.css("border-bottom-width")))-2,
            "box-shadow": "0px 0px 5px 0px "+ _topShadowColor,
            "-webkit-box-shadow": "0px 0px 5px 0px "+ _topShadowColor,
            "-moz-box-shadow": "0px 0px 5px 0px "+ _topShadowColor
          });
        } else {
          $("body>div#_topunder").css("left", $("#"+INIT_PARAM.pageObjs.topId).css("left")).show();
        }
      } else $("body>div#_topunder").hide();
    }
    //2-调整脚部
    if (!INIT_PARAM.foot_peg&&_hasFoot) {
      //X轴方向
      $("#"+INIT_PARAM.pageObjs.footId).css({"left": parseFloat($("#_main").css("left"))+($("#_main").css("margin-left")==""?0:parseFloat($("#_main").css("margin-left")))-$(document).scrollLeft()});
      //Y轴方向
      var _offsetHeight = $(document).scrollTop()+$(window).height();//窗口绝对高度
      if (INIT_PARAM.page_height>0) {
        if ((($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top")))+($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))
            +(($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")?0:parseFloat($("#_main").css("border-top-width")))
            +($("#_main").css("padding-top")==""?0:parseFloat($("#_main").css("padding-top")))+parseFloat($("#_main").css("height"))
            +($("#_main").css("padding-bottom")==""?0:parseFloat($("#_main").css("padding-bottom")))
            +(($("#_main").css("border-bottom-width")=="medium"||$("#_main").css("border-bottom-width")=="")?0:parseFloat($("#_main").css("border-bottom-width"))))<_offsetHeight) return;
      }
      if (_topFlag4foot==-1) _topFlag4foot = ($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top")))+parseFloat(INIT_PARAM.win_min_height)
        +($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))
        +(($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")?0:parseFloat($("#_main").css("border-top-width")))
        +($("#_main").css("padding-top")==""?0:parseFloat($("#_main").css("padding-top")))
        +(($("#_main").css("border-bottom-width")=="medium"||$("#_main").css("border-bottom-width")=="")?0:parseFloat($("#_main").css("border-bottom-width")))
        +($("#_main").css("padding-bottom")==""?0:parseFloat($("#_main").css("padding-bottom")))
        -parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("height"))
        -(($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")))
        -($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")))
        -($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))
        -($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")))
        -(($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")));
      if (_bottomFlag4foot==-1) _bottomFlag4foot = _topFlag4foot
        +($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))
        +(($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")))
        +($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")))
        +parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("height"))
        +($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")))
        +(($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")));
      var _staticTop4foot=$(window).height()-parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("height"))
        -($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("margin-top")))
        -($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-top")))
        -(($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-top-width")))
        -($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")==""?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("padding-bottom")))
        -(($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="medium"||$("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")=="")?0:parseFloat($("#"+INIT_PARAM.pageObjs.footId).css("border-bottom-width")));
      var _newTop=0;
      if (_offsetHeight<=_topFlag4foot) _newTop = _topFlag4foot; //小于尺度
      else if (_offsetHeight>_topFlag4foot&&_offsetHeight<=_bottomFlag4foot) _newTop = _topFlag4foot-$(document).scrollTop(); //高度内
      else _newTop = _staticTop4foot;//大于尺度
      $("#"+INIT_PARAM.pageObjs.footId).css({"top": _newTop});
    }
    if (INIT_PARAM.myScroll) INIT_PARAM.myScroll();
  }

  //100毫秒后调整页面位置，为其中的控件调整位置准备时间
  function _pfResizeTimeout(){
    setTimeout(resizePosition ,100);
  };

  function initPage(options) {
    //参数合并，传入的参数和默认参数
    var _options = $.extend(true, {}, defaults, options);
    if ($.trim(_options.pageObjs.mainId)=="") return "未指定主体部分Id，无法初始化页面！";
    if ($("#"+_options.pageObjs.mainId).length==0) return "无id为\""+_options.pageObjs.mainId+"\"的元素，无法初始化页面！";
    INIT_PARAM = _options;//绑定参数
    //处理中间的部分
    $("body").append("<div id='_main'><div id='_top'></div><div id='_foot'></div></div>");
    $("body>div#_main>div#_top").after($("#"+_options.pageObjs.mainId));
    //0-预处理
    _hScrollbarWidth=getHScrollbarWidth();//纵向滚动条
    _wScrollbarWidth=getWScrollbarWidth();//横向滚动条
    _hasTop = $.trim(_options.pageObjs.topId)!=""&&$("#"+_options.pageObjs.topId).length==1;//是否有头部
    _hasFoot = $.trim(_options.pageObjs.footId)!=""&&$("#"+_options.pageObjs.footId).length==1;//是否有尾部
    //样式处理
    if (_hasTop) $("#"+_options.pageObjs.topId).addClass("hoverArea").addClass("topSegment");
    if (_hasFoot) $("#"+_options.pageObjs.footId).addClass("hoverArea").addClass("footSegment");

    initPosition();//初始化位置
    $(window).resize(_pfResizeTimeout);//页面调整
    $(window).scroll(pfOnScroll);//滚动条
    return "";
  }

//-以下函数为通用函数-----------------------------------------------------------------
  /**
   * 获取纵向滚动条宽度
   */
  function getHScrollbarWidth() {
    var oldOverflowY=$("html").css("overflow-y");
    $("html").css("overflow-y", "hidden");
    var barWidth=$(window).width();
    $("html").css("overflow-y", "scroll");
    barWidth -= $(window).width();
    $("html").css("overflow-y", oldOverflowY);
    return barWidth;
  }
  /**
   * 获取横向滚动条宽度
   */
  function getWScrollbarWidth() {
    var oldOverflowX=$("html").css("overflow-x");
    $("html").css("overflow-x", "hidden");
    var barHeight=$(window).height();
    $("html").css("overflow-x", "scroll");
    barHeight -= $(window).height();
    $("html").css("overflow-x", oldOverflowX);
    return barHeight;
  }
  /**
   * 计算宽度偏移量
   */
  function caculateWidthOffSet() {
    var ret = ($("body").css("margin-left")==""?0:parseFloat($("body").css("margin-left")))
      +($("#_main").css("margin-left")==""?0:parseFloat($("#_main").css("margin-left")))
      //+($("#_main").css("margin-right")==""?0:parseFloat($("#_main").css("margin-right")))
      +($("#_main").css("padding-left")==""?0:parseFloat($("#_main").css("padding-left")))
      +($("#_main").css("padding-right")==""?0:parseFloat($("#_main").css("padding-right")));
    //IE8兼容
    if (!($("#_main").css("border-left-width")=="medium"||$("#_main").css("border-left-width")=="")) ret += $("#_main").css("border-left-width")==""?0:parseFloat($("#_main").css("border-left-width"));
    if (!($("#_main").css("border-right-width")=="medium"||$("#_main").css("border-right-width")=="")) ret += $("#_main").css("border-right-width")==""?0:parseFloat($("#_main").css("border-right-width"));
    return ret;
  }
  /**
   * 计算高度偏移量
   */
  function caculateHeightOffSet() {
    var ret = ($("body").css("margin-top")==""?0:parseFloat($("body").css("margin-top")))
      +($("#_main").css("margin-top")==""?0:parseFloat($("#_main").css("margin-top")))
      //+($("#_main").css("margin-bottom")==""?0:parseFloat($("#_main").css("margin-bottom")))
      +($("#_main").css("padding-top")==""?0:parseFloat($("#_main").css("padding-top")))
      +($("#_main").css("padding-bottom")==""?0:parseFloat($("#_main").css("padding-bottom")));
    //IE8兼容
    if (!($("#_main").css("border-top-width")=="medium"||$("#_main").css("border-top-width")=="")) ret += ($("#_main").css("border-top-width")==""?0:parseFloat($("#_main").css("border-top-width")));
    if (!($("#_main").css("border-bottom-width")=="medium"||$("#_main").css("border-bottom-width")=="")) ret += ($("#_main").css("border-bottom-width")==""?0:parseFloat($("#_main").css("border-bottom-width")));
    return ret;
  }
  /**
   * 得到窗口的绝对高度和宽度，包括滚动条
   */
  function wWidth() {
    if (_hScrollbarWidth==0) _hScrollbarWidth=getHScrollbarWidth();//纵向滚动条
    return ($(window).height()<$(document).height()&&($("html").css("overflow-y")=="auto"))?($(window).width()+_hScrollbarWidth):$(window).width();
  }
  function wHeight() {
    if (_wScrollbarWidth==0) _wScrollbarWidth=getWScrollbarWidth();//横向滚动条
    return ($(window).width()<$(document).width()&&($("html").css("overflow-x")=="auto"))?($(window).height()+_wScrollbarWidth):$(window).height();
  }
  /**
   * 计算左边距
   */
  function getLeft() {
    var retLeft = parseFloat($("body").css("margin-left"));
    if (INIT_PARAM.page_width>0) {//若指定宽度
      if ((wWidth()-caculateWidthOffSet())>INIT_PARAM.page_width) {
        retLeft = (wWidth()-caculateWidthOffSet()-INIT_PARAM.page_width)/2;
      };
    };
    return retLeft;
  }
  /**
   * 计算宽度，根据是否自动定义宽度，是否有垂直滚动条等计算
   */
  function getWidth() {
    if (INIT_PARAM.page_width>0) return INIT_PARAM.page_width;//若指定宽度，返回定宽
    var retWidth = $("#_main").width();//不控制
    if (INIT_PARAM.page_width<=0) {//若为自适应
      retWidth = wWidth()-caculateWidthOffSet();//主体宽度=窗口宽度-宽度偏移量
      if (retWidth<INIT_PARAM.win_min_width) retWidth=INIT_PARAM.win_min_width;
    };
    return retWidth;
  }
  /**
   * 计算高度，根据是否自动定义高度，是否小雨最小值，是否有垂直滚动条等计算
   */
  function getHeight() {
    if (INIT_PARAM.page_height>0) return INIT_PARAM.page_height;//若指定宽度，返回定宽
    var retHeight = $("#_main").height();//不控制
    if (INIT_PARAM.page_height<=0) {//若为自适应
      retHeight = wHeight()-caculateHeightOffSet();//主体宽度=窗口宽度-宽度偏移量
      if (retHeight<INIT_PARAM.win_min_height) retHeight=INIT_PARAM.win_min_height;
    };
    return retHeight;
  }
  /**
   * 使两个对象的可见宽度相同。
   * viewObjId：显示对象Id
   * targetObjId:被比较目标对象Id
   * return 显示对象的宽度
   */
  function getViewWidth(viewObjId, targetObjId) {
    var _view = $("#"+viewObjId);
    var _target = $("#"+targetObjId);
    return (parseFloat(_target.css("width"))+(_target.css("padding-left")==""?0:parseFloat(_target.css("padding-left")))
      +(_target.css("padding-right")==""?0:parseFloat(_target.css("padding-right")))
      +((_target.css("border-left-width")=="medium"||_target.css("border-left-width")=="")?0:parseFloat(_target.css("border-left-width")))
      +((_target.css("border-right-width")=="medium"||_target.css("border-right-width")=="")?0:parseFloat(_target.css("border-right-width"))))
    -((_view.css("padding-left")==""?0:parseFloat(_view.css("padding-left")))
      +(_view.css("padding-right")==""?0:parseFloat(_view.css("padding-right")))
      +((_view.css("border-left-width")=="medium"||_view.css("border-left-width")=="")?0:parseFloat(_view.css("border-left-width")))
      +((_view.css("border-right-width")=="medium"||_view.css("border-right-width")=="")?0:parseFloat(_view.css("border-right-width"))));
  }
  /**
   * 使两个对象的可见高度相同。
   * viewObjId：显示对象Id
   * targetObjId:被比较目标对象Id
   * return 显示对象的高度
   */
  function getViewHeight(viewObjId, targetObjId) {
    var _view = $("#"+viewObjId);
    var _target = $("#"+targetObjId);
    return (parseFloat(_target.css("height"))+(_target.css("padding-top")==""?0:parseFloat(_target.css("padding-top")))
      +(_target.css("padding-bottom")==""?0:parseFloat(_target.css("padding-bottom")))
      +((_target.css("border-top-width")=="medium"||_target.css("border-top-width")=="")?0:parseFloat(_target.css("border-top-width")))
      +((_target.css("border-bottom-width")=="medium"||_target.css("border-bottom-width")=="")?0:parseFloat(_target.css("border-bottom-width"))))
    -((_view.css("padding-top")==""?0:parseFloat(_view.css("padding-top")))
      +(_view.css("padding-bottom")==""?0:parseFloat(_view.css("padding-bottom")))
      +((_view.css("border-top-width")=="medium"||_view.css("border-top-width")=="")?0:parseFloat(_view.css("border-top-width")))
      +((_view.css("border-bottom-width")=="medium"||_view.css("border-bottom-width")=="")?0:parseFloat(_view.css("border-bottom-width"))));
  }

  //=spiritPageFrame 命名空间中的方法 ===========================================================
  //页面框架主函数
  $.spiritPageFrame = function(options, param) {
    //若参数一为字符串，则直接当作本插件的方法进行处理，这里的this是本插件对应的jquery选择器的选择结果
    if (typeof options=='string') return $.spiritPageFrame.methods[options](this, param);
    return initPage(options);
  };
  //插件方法，参考eaqyUi的写法
  $.spiritPageFrame.methods = {
  };
})(jQuery);
/*
  //默认属性
  $.fn.spiritPageFrame.defaults = {
    //页面中所用到的元素的id，只用到三个Div，另，这三个div应在body层
    pageObjs: {
      topId: "topSegment", //头部Id
      mainId: "mainSegment", //主体Id
      footId: "footSegment" //尾部Id
    },

    page_width: 0, //主页面的宽度。<0：宽度不控制；0：宽度自适应；>0：宽度值，页面定宽
    page_height: 0, //主页面的高度。<0：高度不控制；0：高度自适应；>0：高度值，页面定高

    win_min_width: 640, //页面最小的高度。当窗口高度小于这个值，不对界面位置及尺寸进行调整。主体部分宽度也照此设置
    win_min_height: 480, //页面最小的高度。当窗口高度小于这个值，不对界面位置及尺寸进行调整。主体部分高度也照此设置

    top_height: 120, //顶部高度
    top_shadow_color: null, //头部阴影颜色
    top_peg: false, //是否钉住头部在顶端。false：顶部随垂直滚动条移动(浮动)；true：顶部钉在顶端

    foot_height: 40, //脚部高度
    foot_peg: false, //是否钉住脚部在底端。false：脚部随垂直滚动条移动(浮动)；true：脚部钉在底端

    iframe_height_flag: 1 //具体功能区域（可能是整个中部，也可能是带左侧导航的中部）的iframe高度标志。1：iframe高度与框架匹配；非1：框架高度适应iframe内部高度(反向适应)
  };
*/