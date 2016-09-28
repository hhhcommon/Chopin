$(document).ready(function(){
  var pw=$(".pic img").width();
  var ph=$(".pic img").height();
  $("#myVideo").css({"width":pw,"height":0.9*ph});
  $("#myAudio").css({"width":pw});
});
$(window).resize(function(){
  var pw=$(".pic img").width();
  var ph=$(".pic img").height();
  $("#myVideo").css({"width":pw,"height":0.9*ph});
  $("#myAudio").css({"width":pw});
});