/**
 * jQuery spiritui-accordion 精灵组件
 * 手风琴控件。
 *
 * Copyright (c) 2014.8 wh
 *
 * Licensed same as jquery - MIT License
 * http://www.opensource.org/licenses/mit-license.php
 */

(function($) {
	//本控件内的全局变量
	var _bv = getBrowserVersion();

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
   * 处理accordion，包括创建和应用新的属性
   */
  function doAccordions(target, options) {
    var _options = $.data(target, 'spiritAccordions');//取得原来绑定到对象上的spiritAccordions数据

    //合并accordion
    var newAccordions = _options?(_options.accordions?_options.accordions:[]):[];
    if (_options&&_options.accordions) _options.accordions=[];
    var j=0, olen=newAccordions.length;
    var i=0, len=0, anAccordion=null;
    if (options&&options.accordions&&options.accordions.length>0) len=options.accordions.length;
    if (len>0) {
      for (; i<len; i++) {
        anAccordion=options.accordions[i];
        anAccordion._index=i;
        var hasFound=false;
        if (olen>0) {
          for (j=0; j<olen; j++){
            var anAccordion_o=newAccordions[j];
            if (anAccordion.id) hasFound=(anAccordion.id==anAccordion_o.id);
            else hasFound=(anAccordion.title==anAccordion_o.title);
            if (hasFound) {//合并accordions的属性
              var _id=anAccordion_o.id;
              anAccordion_o=$.extend(true, {}, anAccordion_o, anAccordion);
              anAccordion_o.id=_id;
              break;
            }
          }
        }
        if (!hasFound) {//把新的accordion插入accordions
          if (!anAccordion.id) {
            var uId="sui-accordionID-"+getUuid(6), flag=true, count=0;
            while(flag&&count<5) {
              for (j=0; j<olen; j++) {
                if (newAccordions[j].id==uId) {flag=false; break;}
              }
              if (!flag) uId="sui-accordionID-"+getUuid(6);
              flag=!flag;
              count++;
            }
            if (count>=5) uId=t+getUuid(3);
            anAccordion.id=uId;
          }
          anAccordion=$.extend(true, {}, $.fn.spiritAccordions.defaults.defaultAccordion, _options?(_options.defaultAccordion?_options.defaultAccordion:null):null, options.defaultAccordion, anAccordion);
          newAccordions[olen]=anAccordion;
          olen++;
        }
      }
    }
    _options = _options?$.extend(true, {}, _options, options):$.extend(true, {}, $.fn.spiritAccordions.defaults, options);
    _options.accordions=newAccordions;
    _options.container=_options.container?$.extend(true, {}, _options.container, options.container):options.container;
    //绑定变量
    $.data(target, 'spiritAccordions', _options);

    //对容器宽高进行处理
    if (_options.container) {
      $(target).addClass("accordionsContainer").css("overflow", "hidden");
      if (_options.container.styleCss&&_options.container.styleCss!="") $(target).css(_options.container.styleCss);
      if (_options.container.width&&_options.container.width!="") $(target).spiritUtils("setWidthByViewWidth", parseFloat(_options.container.width));
      if (_options.container.height&&_options.container.height!="") $(target).spiritUtils("setHeightByViewHeight", parseFloat(_options.container.height));
    }

    //根据数据绘制控件
    i=0, len=(_options?(_options.accordions?_options.accordions.length:0):0);
    for (;i<len; i++) {
      anAccordion = _options.accordions[i];
      //查找是否已经有了accordioin
      var ao=$(target).find("#"+anAccordion.id);
      if (ao.length==0) {//未找到
        ao=$("<div id='"+anAccordion.id+"' _index='"+(i)+"'></div>");
        ao_header=$("<div id='AH_"+anAccordion.id+"' class='accordion_header'></div>");
        ao_body=$("<div id='AB_"+anAccordion.id+"' class='accordion_body'></div>");
        ao_header.appendTo(ao);
        ao_body.appendTo(ao);
        ao.appendTo(target);
      } else {
      	
      }
    }
  }

  //手风琴主函数
  $.fn.spiritAccordions = function(options, param) {
    //若参数一为字符串，则直接当作本插件的方法进行处理，这里的this是本插件对应的jquery选择器的选择结果
    if (typeof options=='string') return $.fn.spiritAccordions.methods[options](this, param);
    var i=0, _length=this.length;
    if (_length>0) for (; i<_length; i++) doAccordions(this[i], options);
    return this;
  };
  //插件方法，参考eaqyUi的写法
  $.fn.spiritAccordions.methods = {
  };

  //默认属性
  $.fn.spiritAccordions.defaults = {
    id: null, //标识
    hasIcon: false,      //是否需要标题Icon
    container: {          //容器
      styleCss: {},       //容器样式，要是json格式的
      width: "400px",     //容器的宽度，若无此信息，容器宽度以styleCss为准，否则以此信息为准
      height: "30px",     //容器的高度，若无此信息，容器宽高以styleCss为准，否则以此信息为准
    },
    defaultAccordion: {   //默认的手风琴规则，若每个手风琴不设定自己的规则，则所有手风琴的规则以此为准
      maxTextLength: 100, //最大宽度:大于此值,遮罩住超出的字符
      icon: {             //标题Icon设置，这里是默认设置
        url: "",          //图片url
        width: 20,        //图片宽度
        height: 20        //图片高度
      },
      normalCss: "",      //常态css样式(未选中，鼠标未悬停)，可包括边框/字体/背景，注意，要是json格式的
      mouseOverCss: "",   //鼠标悬停样式，可包括边框/字体/背景，注意，要是json格式的
      selCss: ""          //选中后样式，可包括边框/字体/背景，注意，要是json格式的
    },
    accordions:[] //手风琴数组
  };
})(jQuery);
/**
$.fn.spiritAccordions.defaults = {
  id: null,             //标识
  container: {          //容器
    styleCss: {},       //容器样式，要是json格式的
    width: "400px",     //容器的宽度，若无此信息，容器宽度以styleCss为准，否则以此信息为准
    height: "30px",     //容器的高度，若无此信息，容器宽高以styleCss为准，否则以此信息为准
  },
  hasIcon: false,       //是否需要标题Icon
  defaultAccordion: {   //默认的手风琴规则，若每个手风琴不设定自己的规则，则所有手风琴的规则以此为准
    maxTextLength: 100, //最大宽度:大于此值,遮罩住超出的字符
    icon: {             //标题Icon设置，这里是默认设置
      url: "",          //图片url
      width: 20,        //图片宽度
      height: 20        //图片高度
    },
    normalCss: "",      //常态css样式(未选中，鼠标未悬停)，可包括边框/字体/背景，注意，要是json格式的
    mouseOverCss: "",   //鼠标悬停样式，可包括边框/字体/背景，注意，要是json格式的
    selCss: ""          //选中后样式，可包括边框/字体/背景，注意，要是json格式的
  },
  accordions:[{ //手风琴数组
    title
  }]
};
*/