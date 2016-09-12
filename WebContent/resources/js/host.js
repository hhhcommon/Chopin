var rootPath=getRootPath();
var userId="123",dataParam={}
//公共ajax请求
function getContentList(obj) {
	$.ajax({
	  type : "POST",
	  url : obj.url,
	  dataType : "json",
	  data : JSON.stringify(obj),
	  beforeSend : function() {
		  if(!obj.opeType){
			  $(".actList").html("<div style='text-align:center;height:300px;line-height:200px;'>数据加载中...</div>");
		  }
	  },
	  success : function(resultData) {
	    if (resultData.ReturnType == "1001") {
	    	if(!obj.opeType){
	    		$(".actList").html(""); //再重新创建新的数据集时，先清空之前的
    	        $(".totalCount").text(resultData.ResultList.AllCount);
		        ContentListLoad(resultData);
		    }else{
	    		switch(obj.opeType){
	    			case 'conAdd':
	    			    break;
	        		case 'conUpdate':
	        			break;
	        		case 'conDel':
	        			alert("删除成功");
	        			//操作成功后，再次请求列表
	        			if(dataParam['mediaType']=="AUDIO"){
	        				dataParam.url=rootPath+"content/media/getHostMediaList.do";
	        			}else{
	        				dataParam.url=rootPath+"content/seq/getHostSeqMediaList.do";
	        			}
	        			delete dataParam["opeType"];
	        			delete dataParam["mediaType"];
	        			getContentList(dataParam);
	        			break;
	        	    case 'conPub':
	        		  	alert("发布成功");
	        		    break;
	        	    default:	
	    		}
	    	}
	    } else {
	    	$(".actList").html("<div style='text-align:center;height:300px;line-height:200px;'>"+resultData.Message+"</div>");
	    }
	  },
	  error : function(jqXHR) {
	    $(".actList").html("<div style='text-align:center;height:300px;line-height:200px;'>获取数据发生错误："+ jqXHR.status+ "</div>");
	  }
	});
}
//创建节目列表DOM树
function ContentListLoad(actList) {
  var actListLength = actList.ResultList.AllCount;
  var mediaType=actList.ResultList.List[0].MediaType;
  if (actListLength == 0) {
    $(".actList").html("<div style='text-align:center;height:500px;line-height:300px;'>还没有创建节目哦！</div>");
  } else {
    //声明下面需要创建的节点，以便添加内容和添加到文档中
    var actListDiv,listDiv,imgDiv,imgA,thumbImg,imgShade,conUpdate,conEye,conDel,conPub,infoDiv,infoH,infHA,infoP1,infoP2;
    //循环加载列表
    for (var i = 0; i < actListLength; i++) {
      listDiv = $("<div class='listBox'></div>");
      listDiv.attr({
      	"contentId": actList.ResultList.List[i].ContentId,
      	"contentName": actList.ResultList.List[i].ContentName,
      	"contentDesc": actList.ResultList.List[i].ContentDesc,
      	"contentImg": actList.ResultList.List[i].ContentImg,
      	"contentSubjectWord": actList.ResultList.List[i].ContentSubjectWord
     });
      imgDiv = $("<div></div>");
      imgA=$("<a href='javascript:;'></a>");
      thumbImg = $("<img alt='节目封面图片'>");
      thumbImg.attr({'src':actList.ResultList.List[i].ContentImg});
      imgShade=$("<div class='imgShade'></div>");
      conUpdate=$("<i class='fa fa-pencil' opeType='conUpdate' title='修改'></i>");
      conEye=$("<i class='fa fa-eye' opeType='conEye' title='浏览'></i>");
      conDel=$("<i class='fa fa-trash-o' opeType='conDel' title='删除'></i>");
      conPub=$("<i class='fa fa-plus-square-o' opeType='conPub' title='发布'></i>");
      infoDiv = $("<div class='infoBox'>");
      infoH = $("<h4></h4>");
      infoHA=$("<a href='javascript:;'></a>");
      imgShade.append(conUpdate).append(conEye).append(conDel).append(conPub);
      infoHA.text(actList.ResultList.List[i].ContentName);
      infoP2 = $("<p class='lastTime'></p>");
      infoP2.text((actList.ResultList.List[i].CTime).slice(0,10));
      if(mediaType=="SEQU"){
          listDiv.attr({
//  	    "contentCatalogsId": actList.ResultList.List[i].ContentCatalogs[0].CataDid,
//    	    "contentChannelId": actList.ResultList.List[i].ContentPubChannels[0].ChannelId
    	  });
    	  imgDiv.addClass("imgBox");
    	  infoP1 = $("<p class='subCount'></p>");
          infoP1.text(actList.ResultList.List[i].SubCount+"个声音");
          infoDiv.append(infoH.append(infoHA)).append(infoP1).append(infoP2);
      }else{
    	  listDiv.attr({"contentSeqId": actList.ResultList.List[i].ContentSeqId});
    	  imgDiv.addClass("subImg");
    	  infoDiv.append(infoH.append(infoHA)).append(infoP2);
      }
      imgDiv.append(imgA.append(thumbImg)).append(imgShade);
      listDiv.append(imgDiv).append(infoDiv);
      $(".actList").append(listDiv);
    }
  }
}
//获取分类列表
/*
 * 修改节目时，传递原有的分类ID，以便将其设置到选中状态供修改
 * 添加节目时，则不需要
 * */
    function getCatalogs(){
      $.ajax({
        type: "POST",    
        url:rootPath+"common/getCataTreeWithSelf.do",
        dataType: "json",
        data:{cataId: "3"},
        success: function(catalogsList) {
          if (catalogsList.jsonType=="1") {
	    	  catalogsListLoad(catalogsList);
          }
        }     
      });
    }
    function catalogsListLoad(catalogsList,catalog){
      var listLength=catalogsList.data.children.length;
      var opt;
      for(var i=0;i<listLength;i++){
        opt=$("<option></option>");
        opt.val(catalogsList.data.children[i].id).text(catalogsList.data.children[i].name);
        $("#ContentCatalogsId").append(opt);
      }
    }
  
    //获取栏目列表
    function getChannel(){
      $.ajax({
        type: "POST",    
        url:rootPath+"common/getChannelTreeWithSelf.do",
        dataType: "json",
        success: function(channelList) {
          if (channelList.jsonType=="1"){channelListLoad(channelList);}
        }
      });
    }
    function channelListLoad(channelList){
      var listLength=channelList.data.children.length;
      var opt;
      for(var i=0;i<listLength;i++){
        opt=$("<option></option>");
        opt.val(channelList.data.children[i].id);
        opt.text(channelList.data.children[i].name);
        $("#ContentChannelId").append(opt);
      }
    }
    
    //添加/修改单体时，获取专辑列表
    function getSeqMediaList(){
        $.ajax({
          type: "POST",    
          url:rootPath+"content/seq/getHostSeqMediaList.do",
          dataType: "json",
          data:{"UserId":userId},
          success: function(seqMediaList) {
            if(seqMediaList.ReturnType == "1001"){seqMediaListLoad(seqMediaList);} 
          }
        });
      }
    function seqMediaListLoad(seqMediaList){
        var listLength=seqMediaList.ResultList.AllCount;
        var opt;
        for(var i=0;i<listLength;i++){
          opt=$("<option></option>");
          opt.val(seqMediaList.ResultList.List[i].ContentId);
          opt.text(seqMediaList.ResultList.List[i].ContentName);
          $("#ContentSeqId").append(opt);
        }
    }    