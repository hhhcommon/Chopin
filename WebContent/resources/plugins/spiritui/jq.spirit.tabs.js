/**
 * jQuery spiritui-spiritTabs 精灵组件
 * 页标签。
 * 标签只负责标签部分的展示，具体点击标签的功能(比如，刷新一个页面区域)，此插件不处理。
 * 通过每个标签的click事件来完成
 *
 * Copyright (c) 2014.7 wh
 *
 * Licensed same as jquery - MIT License
 * http://www.opensource.org/licenses/mit-license.php
 */
 
(function($) {
  //默认属性
  var defaults = {
    id: null,           //标识
    mutualType: false,  //两页标签的交互区域的处理模式，若为false，则无交互区域，用css处理交互，若为true则有交互区域，交互用图片来处理
    mutualStyle: {       //交互区样式，当mutualType=true生效
      width: "10px",     //交互区宽度
      firstCss:"",       //最左边未选中交互区样式，要是json格式的
      firstSelCss:"",    //最左边选中交互区样式，要是json格式的
      lastCss:"",        //最右边未选中交互区样式，要是json格式的
      lastSelCss:"",     //最右边选中交互区样式，要是json格式的
      middleLCss:"",     //中间未选中左交互区样式，要是json格式的
      middleRCss:"",     //中间未选中右交互区样式，要是json格式的
      middleSelLCss:"",  //中间选中左交互区样式，要是json格式的
      middleSelRCss:""   //中间未选中右交互区样式，要是json格式的
    },
    defaultTab: {        //默认的页签规则，若每个页标签不设定自己的规则，则所有页签的规则以此为准
      maxTextLength: 100,//最大宽度:大于此值,遮罩主
      normalCss: "",     //常态css样式(未选中，鼠标未悬停)，可包括边框/字体/背景，注意，要是json格式的
      mouseOverCss: "",  //鼠标悬停样式，可包括边框/字体/背景，注意，要是json格式的
      selCss: ""         //选中后样式，可包括边框/字体/背景，注意，要是json格式的
    },
    tabs:[], //页标签数组
    onClick:function(title, tabOpts, tabDomObj){}
  };

  /**
   * 生成UUID，默认为36位
   */
  var CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".split("");  
  function getUuid(len,radix) {
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
   * 处理tabs，包括创建和应用新的属性
   */
  function doTabs(target, options) {
    var _options = $.data(target, 'spiritTabs');//取得原来绑定到对象上的spiritTabs数据

    //合并tabs
    var newTabs = _options?(_options.tabs?_options.tabs:[]):[];
    if (_options&&_options.tabs) _options.tabs=[];
    var j=0, olen=newTabs.length;
    var i=0, len=0, aTab=null;
    if (options&&options.tabs&&options.tabs.length>0) len=options.tabs.length;
    if (len>0) {
      for (; i<len; i++) {
        aTab=options.tabs[i];
        aTab._index=i;
        var hasFound=false;
        if (olen>0) {
          for (j=0; j<olen; j++){
            var aTab_o=newTabs[j];
            if (aTab.id) hasFound=(aTab.id==aTab_o.id);
            else hasFound=(aTab.title==aTab_o.title);
            if (hasFound) {//合并tabs的属性
              var _id=aTab_o.id;
              aTab_o=$.extend(true, {}, aTab_o, aTab);
              aTab_o.id=_id;
              break;
            }
          }
        }
        if (!hasFound) {//把新的tab插入tabs
          if (!aTab.id) {
            var uId="sui-tabID-"+getUuid(6), flag=true, count=0;
            while(flag&&count<5) {
              for (j=0; j<olen; j++) {
                if (newTabs[j].id==uId) {flag=false; break;}
              }
              if (!flag) uId="sui-tabID-"+getUuid(6);
              flag=!flag;
              count++;
            }
            if (count>=5) uId=t+getUuid(3);
            aTab.id=uId;
          }
          aTab=$.extend(true, {}, defaults.defaultTab, _options?(_options.defaultTab?_options.defaultTab:null):null, options.defaultTab, aTab);
          newTabs[olen]=aTab;
          olen++;
        }
      }
    }
    _options = _options?$.extend(true, {}, _options, options):$.extend(true, {}, defaults, options);
    _options.tabs=newTabs;
    _options.container=_options.container?$.extend(true, {}, _options.container, options.container):options.container;
    //绑定变量
    $.data(target, 'spiritTabs', _options);

    //对容器宽高进行处理
    if (_options.container) {
      $(target).addClass("tabsContainer").css("overflow", "hidden");
      if (_options.container.styleCss&&_options.container.styleCss!="") $(target).css(_options.container.styleCss);
      if (_options.container.width&&_options.container.width!="") $(target).spiritUtils("setWidthByViewWidth", parseFloat(_options.container.width));
      if (_options.container.height&&_options.container.height!="") $(target).spiritUtils("setHeightByViewHeight", parseFloat(_options.container.height));
    }

    //处理数据
    //处理交互区类型
    if (_options.mutualType) {
      if (!_options.mutualStyle) _options.mutualType=false;
      else if (!_options.mutualStyle.width) _options.mutualType=false;
    }
    //根据数据绘制控件
    i=0, len=(_options?(_options.tabs?_options.tabs.length:0):0);
    var _tabLeft=0;
    for (;i<len; i++) {
      //画标签
      //处理交互区
      if (_options.mutualType) {
        var _m=$(target).find("#tab_m_"+i);
        if (_m.length==0) _m=$("<div id='tab_m_"+i+"'></div>");
        _m.attr("class", "").addClass("tab_m");
        if (i==0) {//画第一个交互区
          _m.addClass("tab_m_f_n");
          if (_options.mutualStyle&&_options.mutualStyle.firstCss&&_options.mutualStyle.firstCss!="") _m.css(_options.mutualStyle.firstCss);
          if (aTab.mutualStyle&&aTab.mutualStyle.firstCss&&aTab.mutualStyle.firstCss!="") _m.css(aTab.mutualStyle.firstCss);
        } else {//画周期交互区
          _m.addClass("tab_m_nr");
          if (_options.mutualStyle&&_options.mutualStyle.middleRCss&&_options.mutualStyle.middleRCss!="") _m.css(_options.mutualStyle.middleRCss);
          if (aTab.mutualStyle&&aTab.mutualStyle.middleRCss&&aTab.mutualStyle.middleRCss!="") _m.css(aTab.mutualStyle.middleRCss);
        }
        _m.appendTo(target);
        _m.spiritUtils("setWidthByViewWidth", _options.mutualStyle.width);
        _m.spiritUtils("setHeightByViewHeight", $(target).height());
        _m.css({"left":_tabLeft});
        _tabLeft += parseFloat(_options.mutualStyle.width);
      }

      var t_normal_cls = (i==0?"tab_normal_f":(i==(len-1)?"tab_normal_l":"tab_normal"));
      aTab = _options.tabs[i];
      //查找是否已经有了tab
      var to=$(target).find("#"+aTab.id);
      if (to.length==0) {//未找到
        to=$("<div id='"+aTab.id+"' _index='"+(i)+"'></div>");
        to.appendTo(target);
      }
      to.html("").attr("class", "");

      to.addClass("tab").addClass(t_normal_cls);
      if (aTab.normalCss&&aTab.normalCss!="") to.css(aTab.normalCss);
      var titleDiv=$("<div id='t_"+aTab.id+"'>"+aTab.title+"</div>").addClass("tab_title");
      titleDiv.css({"height":(parseFloat(to.css("font-size"))+2), "vertical-align":"bottom", "valign":"bottom"});
      titleDiv.css("line-height",titleDiv.css("height"));
      if (getBrowserVersion().indexOf("msie")>0) titleDiv.css("margin-top","2px");
      titleDiv.appendTo(to);
      titleDiv.css({"left":_options.mutualType?5:10});
      if (titleDiv.width()>aTab.maxTextLength) {
        titleDiv.css({"width":aTab.maxTextLength});
        to.attr("title", aTab.title);
        titleDiv.css({"left":_options.mutualType?8:15});
      }
      to.css({"left": _tabLeft, "width": titleDiv.width()+(_options.mutualType?10:20)});
      to.spiritUtils("setHeightByViewHeight", $(target).height());
      to.css("line-height", to.css("height"));
      titleDiv.css({"top":(to.height()-titleDiv.height())/2});
      _tabLeft += to.spiritUtils("getViewWidth");
      if (_options.mutualType) {
        if (i==len-1) {//画最后一个互区
          var _lastM=$(target).find("#tab_m_"+len);
          if (_lastM.length==0) _lastM=$("<div id='tab_m_"+len+"'></div>");
          _lastM.attr("class", "").addClass("tab_m").addClass("tab_m_l_n");
          if (_options.mutualStyle&&_options.mutualStyle.lastCss&&_options.mutualStyle.lastCss!="") _lastM.css(_options.mutualStyle.lastCss);
          if (aTab.mutualStyle&&aTab.mutualStyle.lastCss&&aTab.mutualStyle.lastCss!="") _lastM.css(aTab.mutualStyle.lastCss);
          _lastM.appendTo(target);
          _lastM.spiritUtils("setWidthByViewWidth", _options.mutualStyle.width);
          _lastM.spiritUtils("setHeightByViewHeight", $(target).height());
          _lastM.css({"left": _tabLeft});
          _tabLeft += parseFloat(_options.mutualStyle.width);
        }
      }
      //绑定标签和数据
      $("#"+aTab.id).data("tabData", aTab);

      //鼠标效果
      to.mouseover(function(){
        if ($(this).hasClass("tab_sel")) return;
        $(this).addClass("tab_mouseOver");
        var tabData=$(this).data("tabData");
        if (tabData.mouseOverCss&&tabData.mouseOverCss!="") $(this).css(tabData.mouseOverCss);
      }).mouseout(function(){
        if ($(this).hasClass("tab_sel")) return;
        $(this).removeClass("tab_mouseOver");
        var tabData=$(this).data("tabData");
        if (tabData.normalCss&&tabData.normalCss!="") $(this).css(tabData.normalCss);
      });

      //点击
      to.bind("click", function() {
        if ($(this).hasClass("tab_sel")) return;//若已被选中，则什么也不做
        //任何标签都不显示
        var _index=parseInt($(this).attr("_index"));
        var tabData=null, aTab=null;
        var _tabs = $(target).find(".tab");
        var j=0, len=_tabs.length;
        for(;j<len;j++) {
          aTab=_tabs[j];
          tabData=$(aTab).data("tabData");
          var _lm=$(target).find("#tab_m_"+j);
          $(_lm).attr("class", "").addClass("tab_m");
          $(aTab).attr("class", "").addClass("tab");
          if (j==0) {
            $(aTab).addClass("tab_normal").addClass("tab_normal_f");
            if ($(target).data("spiritTabs").mutualType) {
              _lm.addClass("tab_m_f_n");
              if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.firstCss&&$(target).data("spiritTabs").mutualStyle.firstCss!="") $(_lm).css($(target).data("spiritTabs").mutualStyle.firstCss);
              if (tabData.mutualStyle&&tabData.mutualStyle.firstCss&&tabData.mutualStyle.firstCss!="") $(_lm).css(tabData.mutualStyle.firstCss);
            }
          } else if (j==(len-1)) {
            $(aTab).addClass("tab_normal").addClass("tab_normal_l");
            if ($(target).data("spiritTabs").mutualType) {
              _lm.addClass("tab_m_nr");
              if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.middleRCss&&$(target).data("spiritTabs").mutualStyle.middleRCss!="") $(_lm).css($(target).data("spiritTabs").mutualStyle.middleRCss);
              if (tabData.mutualStyle&&tabData.mutualStyle.middleRCss&&tabData.mutualStyle.middleRCss!="") $(_lm).css(tabData.mutualStyle.middleRCss);
              var _rm=$(target).find("#tab_m_"+(j+1));
              $(_rm).attr("class", "").addClass("tab_m");
              _rm.addClass("tab_m_l_n");
              if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.lastCss&&$(target).data("spiritTabs").mutualStyle.lastCss!="") $(_rm).css($(target).data("spiritTabs").mutualStyle.lastCss);
              if (tabData.mutualStyle&&tabData.mutualStyle.lastCss&&tabData.mutualStyle.lastCss!="") $(_rm).css(tabData.mutualStyle.lastCss);
            }
          } else {
            $(aTab).addClass("tab_normal");
            if ($(target).data("spiritTabs").mutualType) {
              if (j<=_index) {
                _lm.addClass("tab_m_nl");
                if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.middleLCss&&$(target).data("spiritTabs").mutualStyle.middleLCss!="") $(_lm).css($(target).data("spiritTabs").mutualStyle.middleLCss);
                if (tabData.mutualStyle&&tabData.mutualStyle.middleLCss&&tabData.mutualStyle.middleLCss!="") $(_lm).css(tabData.mutualStyle.middleLCss);
              } else {
                _lm.addClass("tab_m_nr");
                if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.middleRCss&&$(target).data("spiritTabs").mutualStyle.middleRCss!="") $(_lm).css($(target).data("spiritTabs").mutualStyle.middleRCss);
                if (tabData.mutualStyle&&tabData.mutualStyle.middleRCss&&tabData.mutualStyle.middleRCss!="") $(_lm).css(tabData.mutualStyle.middleRCss);
              }
            }
          }
          if (tabData.normalCss&&tabData.normalCss!="") $(aTab).css(tabData.normalCss);
        }
        tabData = $(this).data("tabData");
        //设置选中
        $(this).addClass("tab_sel");
        if (_index==0) {
          $(this).addClass("tab_sel_f");
        } else if (_index==(len-1)) {
          $(this).addClass("tab_sel_l");
        }
        if (tabData.selCss&&tabData.selCss!="") $(this).css(tabData.selCss);

        if ($(target).data("spiritTabs").mutualType) {
          var _lm=$(target).find("#tab_m_"+_index);
          var _rm=$(target).find("#tab_m_"+(_index+1));
          $(_lm).attr("class", "").addClass("tab_m");
          $(_rm).attr("class", "").addClass("tab_m");
          if (_index==0) {
            $(_lm).addClass("tab_m_f_s");
            if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.firstSelCss&&$(target).data("spiritTabs").mutualStyle.firstSelCss!="") $(_lm).css($(target).data("spiritTabs").mutualStyle.firstSelCss);
            if (tabData.mutualStyle&&tabData.mutualStyle.firstCss&&tabData.mutualStyle.firstCss!="") $(_lm).css(tabData.mutualStyle.firstSelCss);
            $(_rm).addClass("tab_m_sr");
            if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.middleSelRCss&&$(target).data("spiritTabs").mutualStyle.middleSelRCss!="") $(_rm).css($(target).data("spiritTabs").mutualStyle.middleSelRCss);
            if (tabData.mutualStyle&&tabData.mutualStyle.middleSelRCss&&tabData.mutualStyle.middleSelRCss!="") $(_rm).css(tabData.mutualStyle.middleSelRCss);
          } else if (_index==(len-1)) {
            $(_lm).addClass("tab_m_sl");
            if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.middleSelLCss&&$(target).data("spiritTabs").mutualStyle.middleSelLCss!="") $(_lm).css($(target).data("spiritTabs").mutualStyle.middleSelLCss);
            if (tabData.mutualStyle&&tabData.mutualStyle.middleSelLCss&&tabData.mutualStyle.middleSelLCss!="") $(_lm).css(tabData.mutualStyle.middleSelLCss);
            $(_rm).addClass("tab_m_l_s");
            if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.lastSelCss&&$(target).data("spiritTabs").mutualStyle.lastSelCss!="") $(_rm).css($(target).data("spiritTabs").mutualStyle.lastSelCss);
            if (tabData.mutualStyle&&tabData.mutualStyle.lastSelCss&&tabData.mutualStyle.lastSelCss!="") $(_rm).css(tabData.mutualStyle.lastSelCss);
          } else {
            $(_lm).addClass("tab_m_sl");
            if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.middleSelLCss&&$(target).data("spiritTabs").mutualStyle.middleSelLCss!="") $(_lm).css($(target).data("spiritTabs").mutualStyle.middleSelLCss);
            if (tabData.mutualStyle&&tabData.mutualStyle.middleSelLCss&&tabData.mutualStyle.middleSelLCss!="") $(_lm).css(tabData.mutualStyle.middleSelLCss);
            $(_rm).addClass("tab_m_sr");
            if ($(target).data("spiritTabs").mutualStyle&&$(target).data("spiritTabs").mutualStyle.middleSelRCss&&$(target).data("spiritTabs").mutualStyle.middleSelRCss!="") $(_rm).css($(target).data("spiritTabs").mutualStyle.middleSelRCss);
            if (tabData.mutualStyle&&tabData.mutualStyle.middleSelRCss&&tabData.mutualStyle.middleSelRCss!="") $(_rm).css(tabData.mutualStyle.middleSelRCss);
          }
          _lm.spiritUtils("setWidthByViewWidth", _options.mutualStyle.width);
          _lm.spiritUtils("setHeightByViewHeight", $(target).height());
          _rm.spiritUtils("setWidthByViewWidth", _options.mutualStyle.width);
          _rm.spiritUtils("setHeightByViewHeight", $(target).height());
        }
        //调用用户定义点击事件
        if (tabData.onClick) eval(tabData.onClick);//该页签的点击事件
        else { //调用总的点击事件
        	if (_options.onClick) _options.onClick(tabData.title, tabData, this);
        }
      });
    }
  }

  //页标签主函数
  $.fn.spiritTabs = function(options, param) {
    //若参数一为字符串，则直接当作本插件的方法进行处理，这里的this是本插件对应的jquery选择器的选择结果
    if (typeof options=='string') return $.fn.spiritTabs.methods[options](this, param);

    var i=0, _length=this.length;
    if (_length>0) for (; i<_length; i++) doTabs(this[i], options);
    return this;
  };
  //插件方法，参考eaqyUi的写法
  $.fn.spiritTabs.methods = {
      	
  };
})(jQuery);
/**
  defaults = {
    id: null,           //标识
    container: {        //容器
      styleCss: {},     //容器样式，要是json格式的
      width: "400px",   //容器的宽度，若无此信息，容器宽度以styleCss为准，否则以此信息为准
      height: "30px",   //容器的高度，若无此信息，容器宽高以styleCss为准，否则以此信息为准
    },
    mutualType: false,  //两页标签的交互区域的处理模式，若为false，则无交互区域，用css处理交互，若为true则有交互区域，交互用图片来处理
    mutualStyle: {      //交互区样式，当mutualType=true生效
      width: "10px",    //交互区宽度
      firstSelCss:"",   //最左边选中交互区样式，要是json格式的
      lastCss:"",       //最右边未选中交互区样式，要是json格式的
      lastSelCss:"",    //最右边选中交互区样式，要是json格式的
      middleLCss:"",    //中间未选中左交互区样式，要是json格式的
      middleRCss:"",    //中间未选中右交互区样式，要是json格式的
      middleSelLCss:"", //中间选中左交互区样式，要是json格式的
      middleSelRCss:""  //中间未选中右交互区样式，要是json格式的
    },
    defaultTab: {       //默认的页签规则，若每个页标签不设定自己的规则，则所有页签的规则以此为准
      maxTextLength: 30,//最大宽度:大于此值,遮罩主
      normalCss: "",    //常态css样式(未选中，鼠标未悬停)，可包括边框/字体/背景，注意，要是json格式的
      mouseOverCss: "", //鼠标悬停样式，可包括边框/字体/背景，注意，要是json格式的
      selCss: ""        //选中后样式，可包括边框/字体/背景，注意，要是json格式的
    },
    tabs:[{ //页标签数组
      title:"",         //页签标题
      onClick:          //单击事件，若再此设置了单击事件，则总单击事件失效
    }],

    onClick:function(title, tabOpts){} //总单击事件
  };
 */