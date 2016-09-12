/**
 * 基于jquery，基于??
 */
//初始化title文字
$(function() {
  $("div .pagination>.first").attr("title", "首页");
  $("div .pagination>.previous").attr("title", "上页");
  $("div .pagination>.next").attr("title", "下页");
  $("div .pagination>.last").attr("title", "尾页");
});

//设置页显示情况
function setPage(pageData) {
  if (!pageData||!pageData.AllPage) {//都设置为不可用
    $("div .pagination>span").each(function(){$(this).addClass('disabled')});
    $(".toPage").attr("readonly","true").attr("disabled","true");
    $("div .pagination>.totalPage").html("");
    return;
  }
  $("div .pagination>.totalPage").html(pageData.AllPage);
  if (pageData.AllPage<=1) {
    $("div .pagination>span").each(function(){$(this).addClass('disabled')});
    $(".toPage").attr("readonly","true").attr("disabled","true");
  } else {
    $(".toPage").removeAttr("disabled").removeAttr("readonly");
    $(".toPage").removeClass('disabled');
    $(".toPage").val(pageData.Page);
    $("div .pagination>.jump").removeClass('disabled');
    if (pageData.Page<=1) {
      $("div .pagination>.first").addClass('disabled');
      $("div .pagination>.previous").addClass('disabled');
      $("div .pagination>.next").removeClass('disabled');
      $("div .pagination>.last").removeClass('disabled');
    } else if (pageData.Page>=pageData.AllPage) {
      $("div .pagination>.first").removeClass('disabled');
      $("div .pagination>.previous").removeClass('disabled');
      $("div .pagination>.next").addClass('disabled');
      $("div .pagination>.last").addClass('disabled');
    } else {
      $("div .pagination>.first").removeClass('disabled');
      $("div .pagination>.previous").removeClass('disabled');
      $("div .pagination>.next").removeClass('disabled');
      $("div .pagination>.last").removeClass('disabled');
    }
  }
}