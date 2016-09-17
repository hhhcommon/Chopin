var rootPath=getRootPath();
//得到栏目导航条信息
function getChannelNavList(){
  var _data={"ChannelId":""};
  $.ajax({
    type: "POST",
    url:rootPath+"baseinfo/getChannelTree4View.do",
    dataType: "json",
    data:_data,
    success:function(ChannelNavList){
      if(ChannelNavList.ReturnType=="1001"){
        loadChannelNavListLoad(ChannelNavList);
      }
    },
    error:function(jqXHR){
      alert("发生错误" + jqXHR.status);
    }
  });
}

//加载栏目导航条
function loadChannelNavListLoad(ChannelNavList){
  var pLia,liA,liUl,liUlLiA,liUlLi;
  for(var i=0;i<ChannelNavList.Data.children.length;i++){
    if(ChannelNavList.Data.children[i].children){//如果导航有二级栏目
      pLia=$("<li class='service-list'></li>");
      liA=$("<a href='#'>"+ChannelNavList.Data.children[i].name+"</a>");
      liUl=$("<ul class='sub-menu'></ul>");
      for(var j=0;j<ChannelNavList.Data.children[i].children.length;j++){
        liUlLiA=$("<li><a href='#'>"+ChannelNavList.Data.children[i].children[j].name+"</a></li>");
        liUlLi=liUl.append(liUlLiA);
      }
      pLia.append(liA).append(liUlLi);
    }else{//如果导航没有二级栏目
      pLia=$("<li class='hidden-xs'><a href='#'>"+ChannelNavList.Data.children[i].name+"</a></li>");
    }
    $('#navbar #main-nav .nav').append(pLia);
  }
  /*设置相关的样式*/
  $('#navbar #main-nav .nav li:first').addClass("current-menu-item");
  $("#navbar #main-nav .nav li").hover(function(){
    $(this).children(".sub-menu").css({"display":"block"});
    $(this).addClass("on");
  },function(){
    $(this).children(".sub-menu").css({"display":"none"});
    $(this).removeClass("on");
  })
}

//点击搜索栏里的搜索按钮出现相关内容
function searchContent(){
  var _data={"SearchStr":$(".field").val()};
  $.ajax({
    type: "POST",
    url:rootPath+"searchByText.do",
    dataType: "json",
    data:_data,
    success:function(searchList){
      if(searchList.ReturnType=="1001"){
        loadSearchList();
      }
    },
    error:function(jqXHR){
      alert("发生错误" + jqXHR.status);
    }
  });
}

//加载查询列表
function loadSearchList(){
  
}

//页面加载出来的时候加载轮播图
function lunbo(){
  $.ajax({
    type: "POST",
    url:rootPath+"directContent.do",
    dataType: "json",
    success:function(returnList){
      if(returnList.ReturnType=="1001"){
        
      }
    },
    error:function(jqXHR){
      alert("发生错误" + jqXHR.status);
    }
  });
}
