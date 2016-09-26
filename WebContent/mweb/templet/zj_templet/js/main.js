var rootPath=getRootPath();
$(function(){
	var page=2;
	var nextPage="false";
	//控制播放按钮的播与停
	var audio=$("<audio></audio>")[0];
	$(".audioBox").on("click",".playBtn",function(){
		if($(this).hasClass("play")){
			$(this).removeClass("play");
			audio.pause();
		}else{
			if($(".playBtn").hasClass("play")){
				audio.pause();
				$(".playBtn").removeClass("play");
			}
			audio.src=$(this).parent(".audioLi").attr("data-src");
			//播放当前资源，并改变当前按钮状态
			$(this).addClass("play");
			audio.play();
		}
	});

	function loadMore(resultData){
		var actListLength=resultData.ResultList.length;
		 //声明下面需要创建的节点
	    var liEle,listDiv,titleA,titleH,titleP,playA;
	    //循环加载列表
	    for(var i=0;i<actListLength;i++){
	        liEle=$("<li class='audioLi'></li>");
	        liEle.attr({"data-src":resultData.ResultList[i].ContentURI});
	        listDiv=$("<div class='audioIntro'></div>");
	        titleA=$("<a href='javascript:;'></a>");
	        titleH=$("<h3></h3>");
	        titleH.text(resultData.ResultList[i].ContentName);
	        titleP=$("<p></p>");
	        titleP.text(resultData.ResultList[i].ContentPubTime);
	        titleA.append(titleH);
	        listDiv.append(titleA).append(titleP);
	        playA=$("<a href='javascript:;' class='playBtn'></a>");

	        liEle.append(listDiv).append(playA);
	        $(".audioBox").append(liEle);
	    }
	}
	
	//添加滚动条事件
	window.onscroll=function(){
		//当滚动到最底部以上60像素时， 加载新内容  
		if($(document).height() - $(this).scrollTop() - $(this).height()<60){
			$.ajax({
		        type: "POST",
		        url:rootPath+"content/getZJSubPage.do",
		        dataType: "json",
		        data:{Page:page,ContentId:$(".zjIntro").attr("contentId")},
		        success: function(resultData) {
		            if (resultData.ReturnType=="1001"){
		            	loadMore(resultData);
		            	if(resultData.NextPage=="true"){
		            		page++;
		            	}
		            }else{
		            	return;
		            }
		        },
		        error: function(jqXHR){  
		           alert("发生错误" + jqXHR.status);
		        }     
		    });
		}
	}
	//打开APP或下载
	$(".downLoad").click(function(){
		window.location=$(".zjIntro").attr("zjOpenApp");
        window.setTimeout(function () {
            window.location.href= "http://182.92.175.134/download/WoTing.apk";
        },2000);
	});

});