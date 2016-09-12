$(function(){
  var rootPath=getRootPath();
  //控制播放的节目的顺序号
	var listNum=0;
	//获取播放元素容器
	var audio=$("audio")[0];
	//打开链接时，自动播放对应声音资源
	$("#jmAudio")[0].play();
	$(".playControl").addClass("play");

	//播放控制面板的播放控制
	$(".playControl").on("click",function(){
		if($("#jmAudio")[0]){
			if($(".playControl").hasClass("play")){
				$(".playControl").removeClass("play");
				$("#jmAudio")[0].pause();
			}else{
				$(".playControl").addClass("play");
				$("#jmAudio")[0].play();
			}
		}else{
			if($(".playControl").hasClass("play")){
				$.each($(".play_state"), function(i,val){
				    if(val.className!="play_state"){
				    	$(val).removeClass("playing").addClass("play_pause");
				    	return false;
				    }
				});
				$(".playControl").removeClass("play");
				audio.pause();
			}else{
				$.each($(".play_state"), function(i,val){
				    if(val.className!="play_state"){
				    	$(val).removeClass("play_pause").addClass("playing");
				    	return false;
				    }
				});
				$(".playControl").addClass("play");
				audio.play();
			}
		}
		
	});

	//资源准备就绪后，获取声音长度，否则NaN
	$(audio).on("canplay",function(){
		$(".fullTime").text(formatTime($(this)[0].duration));
	});
	
	//实时获取播放时长和改变进度条进度
	audio.addEventListener("timeupdate",function(){
	    if (!isNaN(this.duration)) {
	        //播放进度条
	        var progressValue = this.currentTime/this.duration*($(".currentMusicBar").width());
	        $('.currentMusicBarRound')[0].style.left = parseInt(progressValue) + 'px';

	        //播放时长
	        if(formatTime(Math.round(this.currentTime))!=0){
	        		$(".playTime").text(formatTime(Math.round(this.currentTime)));
	        }
	    	
	    };
	},false);

	//推荐声音列表的播放控制
	$(".ulBox").on("click",".audioLi",function(){
		//声音列表变化及声音的控制
		if($(this).children("span").hasClass("playing")){
			audioPause($(this),audio);
		}else if($(this).children("span").hasClass("play_pause")){
			audioPlay($(this),audio);
		}else{
			$.each($(".play_state"), function(i,val){
			    if(val.className!="play_state"){
					audio.pause();
			    }

			});
			$(".audioLi").children("span").removeClass("playing").removeClass("play_pause").end().children(".audioIntro").children("h3").css({"color":"#fea734"});
			$(this).children("span").addClass("playing").end().children(".audioIntro").children("h3").css({"color":"#e96600"});
			$(".palyCtrlBox").children("h4").text($(this).children(".audioIntro").children("h3").text());
			audio.src=$(this).attr("data-src");
			audio.play();

			//判断第一个和最后一个，对上一个、下一个样式进行控制
			$.each($(".play_state"), function(i,val){
			    if(val.className!="play_state"){
					audio.pause();
			    }
			});
			if($(this).first().hasClass("")){

			}else{

			}
		}

		//从推荐列表选择声音时，播放面板的控制变化
		if($(this).children("span").hasClass("playing")){
			$(".playControl").addClass("play");
		}else{
			//播放当前资源，并改变播放按钮状态
			$(".playControl").removeClass("play");
		}
	});
	//下一个声音
	var isList=false;
	$(".next").on("click",function(){
		if(isList){
			$.each($(".play_state"), function(i,val){
				//alert($(".audioLi").length);
				//如果有正在播放的列表，那么播放下一条，如果没有，则播放第一条
			    if(val.className!="play_state"){
			    	
			    	//判断是否为最后一个
			    	if(i>=$(".audioLi").length-1){
		    			return false;
		    		}
		    		if(i==$(".audioLi").length-2){
		    			$(".next").css({"background":"url(./imgs/audio_play.png) no-repeat 0 -192px","cursor":"default"});
		    		}
		    		if(i>=0){
		    			$(".previous").css({"background":"url(./imgs/audio_play.png) no-repeat 0 -84px","cursor":"pointer"});
		    		}
		    		$(".audioLi").children("span").removeClass("playing").removeClass("play_pause").end().children(".audioIntro").children("h3").css({"color":"#fea734"});
		    		listNum=i+1;
		    		$(".palyCtrlBox").children("h4").text($(".audioLi").eq(listNum).children(".audioIntro").children("h3").text());
					audio.src=$(".audioLi").eq(listNum).data("src");
			    	audioPlay($(".audioLi").eq(listNum),audio,true);
			    	$(".playControl").addClass("play");
			    	return false;
			    }
			});
		}else{
			$(".palyCtrlBox").children("h4").text($(".audioLi").eq(listNum).children(".audioIntro").children("h3").text());
			audio.src=$(".audioLi").eq(listNum).data("src");
			audioPlay($(".audioLi").eq(listNum),audio,true);
			$(".playControl").addClass("play");
			$(audio).removeAttr("id");
			isList=true;
		}
	});
	//上一个声音
	$(".previous").on("click",function(){
			$.each($(".play_state"), function(i,val){
			    if(val.className!="play_state"){
			    	//判断是否为第一个
		    		if(i<=0){
		    			return false;
		    		}else if(i==1){
		    			$(".previous").css({"background":"url(./imgs/audio_play.png) no-repeat 0 -162px","cursor":"default"});
		    		}
		    		if(i<=$(".audioLi").length-1){
		    			$(".next").css({"background":"url(./imgs/audio_play.png) no-repeat 0 -116px","cursor":"pointer"});
		    		}
		    		listNum=i-1;
		    		
		    		$(".audioLi").children("span").removeClass("playing").removeClass("play_pause").end().children(".audioIntro").children("h3").css({"color":"#fea734"});
		    		$(".palyCtrlBox").children("h4").text($(".audioLi").eq(listNum).children(".audioIntro").children("h3").text());
					audio.src=$(".audioLi").eq(listNum).data("src");
			    	audioPlay($(".audioLi").eq(listNum),audio,true);
			    	$(".playControl").addClass("play");
			    	return false;
			    
			}
		});
	});

	//请求推荐资源列表
	var searchStr=$(".palyCtrlBox").children("h4").text();
	$.ajax({
        url: rootPath+"common/jsonp.do",
        type:"POST",
        dataType:"json",
        data:{
        	"RemoteUrl":"http://www.wotingfm.com:808/wt/searchByText.do",
        	"IMEI":"3279A27149B24719991812E6ADBA5583",
        	"PCDType":"3",
        	"SearchStr":searchStr,
        	"ResultType":"0",
        	"PageType":"0",
        	"Page":"0",
        	"PageSize":"20"
        },
        success: function(resultData) {
	        var jsonData=$.parseJSON(resultData);
          if (jsonData.ReturnType=="1001"){
            loadRecomList(jsonData);
          }
        },
        error: function(jqXHR){
          alert("发生错误" + jqXHR.status);
        }
    });
	//打开APP或下载
	$(".downLoad").click(function(){

		window.location=$("#jmAudio").attr("jmOpenApp");
	    
        window.setTimeout(function () {
            window.location.href= "http://www.wotingfm.com/download/WoTing.apk";
        },2000);
	});
	
});

    function audioPause(obj,audio){
    	obj.children("span").removeClass("playing").addClass("play_pause");
		audio.pause();
    }
    function audioPlay(obj,audio,listPlay){
		if(listPlay){
			obj.children("span").addClass("playing").end().children(".audioIntro").children("h3").css({"color":"#e96600"});
		}else{
			obj.children("span").removeClass("play_pause").addClass("playing");
		}
		audio.play();
    }
//毫秒转时分秒格式
function formatTime(longTime){
	var time=parseFloat(longTime);
	if(time!=null && time !=""){
		if(time<60){
			var s=time;
			if(s<10){
				time="00:0"+s;
			}else{
				time="00:"+s;
			}
		}else if(time>60 && time<3600){
			var m=parseInt(time / 60);
			var s=parseInt(time %60);
			time=m+":"+s;
		}else if(time>=3600 && time<86400){
			var h=parseInt(time / 3600);
			var m=parseInt(time % 3600 /60);
			var s=parseInt(time %3600 %60 %60);
			time=h+":"+m+":"+s;
		}
	}
	return time;
}

//创建相关推荐资源列表
function loadRecomList(resultData){
	var actListLength=resultData.ResultList.List.length;
	 //声明下面需要创建的节点
	var liEle,audioEle,imgDiv,imgOne,imgTwo,imgThree,introDiv,introH,introP,playState;
	//循环加载列表
	for(var i=0;i<actListLength;i++){
	    liEle=$("<li class='audioLi'></li>");
	    liEle.attr({"data-src":resultData.ResultList.List[i].ContentPlay});
	    imgDiv=$("<div class='audioImg'></div>");
	    imgOne=$("<div class='boxF'></div>");
	    imgTwo=$("<div class='boxS'></div>");
	    imgThree=$("<div class='boxT'></div>");
	    imgThree.css({"background-image":"url("+resultData.ResultList.List[i].ContentImg+")"});
	    imgDiv.append(imgOne.append(imgTwo.append(imgThree)));
	    introDiv=$("<div class='audioIntro'>");
	    introH=$("<h3></h3");
	    introH.text(resultData.ResultList.List[i].ContentName);
	    introP=$("<p></p>");
	    introP.text(resultData.ResultList.List[i].ContentDesc);
	    introDiv.append(introH).append(introP);
	    playState=$("<span class='play_state'></span>");
	    
	    liEle.append(imgDiv).append(introDiv).append(playState);
	    $(".ulBox").append(liEle);
	}
}
	