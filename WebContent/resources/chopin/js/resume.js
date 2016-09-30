window.onload = function(){
  var wid = document.documentElement.clientWidth;
  var newfs = wid*100/640;
    //wid*默认的字体大小/设计图的大小
  var html = document.getElementsByTagName('html')[0];
  html.style.fontSize = newfs+"px";
}
window.onresize = function(){
  var wid = document.documentElement.clientWidth;
  var newfs = wid*100/640;
  var html = document.getElementsByTagName('html')[0];
  html.style.fontSize = newfs+"px";
}