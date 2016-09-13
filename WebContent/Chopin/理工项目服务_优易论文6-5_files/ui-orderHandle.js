(function($) {
	$.fn.disable = function() {
		var maskContainer;
		maskContainer = '<div class="maskContainer"></div>';
		$(this).append(maskContainer);
	}
	$.fn.enable = function() {
		var maskContainer;
		maskContainer = '<div class="maskContainer"></div>';
		$(this).remove(maskContainer);
	}
})(jQuery);

var $initialPrice; //翻译初始价格
var translationUploadFile = ""; //翻译文件列表
var dynamicIncrease = 0; //动态列表
var swfurl = $("#currentPath").val() + '/uploadify/uploadify.swf';
var uploader = $("#currentPath").val() + '/uploadify/uploadify.php';
var currentFileId;
var tempArray = new Array();
var uploadJson = [];




//初始化参数
var orderSet = {
	ajaxLoad: 0,
	turnitin: {
		turnitinMode: 0, //0 上传 1  文本
		wordCount: 0, //字数统计
		eachWord: 5000, // 5000 字
		currency: "USD",
		initialPrice: 5, //初始价格
		lastPrice: 0, //小计
		fileList: []
	},
	//语法校正
	grammar: {
		initialPrice: 68,
		lastPrice: 0,
		fileList: []
	},
	//文书
	as: {
		total: 0, //总计
		sumItem: 0,
		fileList: [],
		resume: {
			title: "简历制作",
			initialPrice: 99, //初始价格
			initialTime: 4, // 初始天数
			currentNumber: 0, //份
			lastPrice: 0, //小计
			remarks: "", //订单备注
			urgentState: 0, // 0 不加急， 1 加急  默认为0
			urgentTime: 2, // 加急天数
			urgentpriceMarkup: 1.5, //  加急价格比率  原价 * 1.5
			hasSubItem: 0 //是否含有子项     0  没有   1 有
		},
		recommend: {
			title: "推荐信写作",
			initialPrice: 80,
			initialTime: 5, // 初始天数
			currentNumber: 0,
			lastPrice: 0,
			remarks: "", //订单备注
			urgentState: 0, // 0 不加急， 1 加急  默认为0
			urgentTime: 2, // 加急天数
			urgentpriceMarkup: 1.5, //  加急价格比率  原价 * 1.5
			hasSubItem: 0 //是否含有子项     0  没有   1 有
		},
		essay: {
			title: "留学Essay",
			initialPrice: 40, //初始价格
			initialTime: 4, // 初始天数
			currentNumber: 0,
			lastPrice: 0,
			remarks: "", //订单备注
			urgentState: 0, // 0 不加急， 1 加急  默认为0
			urgentTime: 2, // 加急天数
			urgentpriceMarkup: 1.5, //  加急价格比率  原价 * 1.5
			hasSubItem: 1 //是否含有子项     0  没有   1 有
		},
		ps: {
			title: "个人陈述写作",
			initialPrice: 40, //初始价格
			initialTime: 4, // 初始天数
			currentNumber: 0,
			lastPrice: 0,
			remarks: "", //订单备注
			urgentState: 0, // 0 不加急， 1 加急  默认为0
			urgentTime: 2, // 加急天数
			urgentpriceMarkup: 1.5, //  加急价格比率  原价 * 1.5
			hasSubItem: 1 //是否含有子项     0  没有   1 有
		}
	},
	polytechnic: {
		lastPrice: 0,
		fileList: []
	}
}

$(function() {
	//论文检测
	turnitin_fun.uploadFile();
	turnitin_fun.validate();

	// 切换检测方式.
	$(".btn-switch-turnitin-type").click(function() {
		if ($(this).find('input').val() == "paste") {
			turnitin_fun.calculation();
			$(".modeSwitching1").addClass("hidden");
			$(".modeSwitching2").removeClass("hidden");
			orderSet.turnitin.turnitinMode = 1;
		} else if ($(this).find('input').val() == "upload") {
			turnitin_fun.wdzs_count();
			$(".modeSwitching1").removeClass("hidden")
			$(".modeSwitching2").addClass("hidden");
			orderSet.turnitin.turnitinMode = 0;
		}
	});

	//语法校正
	grammar_fun.uploadFile();
	grammar_fun.validate();

	//学术翻译
	translate_fun.uploadFile();
	translate_fun.validate();
	//根据翻译用途修改单价
	$initialPrice = $("input[name='oUse']:checked").val();
	jQuery(".unitPrice").text($initialPrice);
	jQuery("input[name='oUse']").change(function() {
		jQuery(".unitPrice").text($(this).val());
		translate_fun.total();
	});
});

// Turnitin检测对象
var turnitin_fun = new Object({
	//翻译验证配置
	turnitin_validate_config: {
		submitHandler: function(form) {
			turnitin_fun.submit();
		}
	},
	//字数统计费用
	wdzs_count: function() {
		var statistics = 0;
		$(".wdzs").each(function() {
			statistics = statistics + parseInt($(this).val());
		});
		orderSet.turnitin.wordCount = statistics * orderSet.turnitin.eachWord; //总字数
		orderSet.turnitin.lastPrice = (statistics * orderSet.turnitin.initialPrice).toFixed(2); // 统计价格
		$(".autofy").text(orderSet.turnitin.lastPrice);
	},
	//统计字数
	words_count: function($zs) {},
	//计算费用
	calculation: function() {
		var text = $("#turnitinContent").val();
		orderSet.turnitin.wordCount = turnitin_fun.wordStatic(text);
		$("#varn_words").text(orderSet.turnitin.wordCount); //设置当前字数
		num = Math.ceil(orderSet.turnitin.wordCount / orderSet.turnitin.eachWord);
		orderSet.turnitin.lastPrice = (num * orderSet.turnitin.initialPrice).toFixed(2); //设置当前价格
		$(".autofy").text(orderSet.turnitin.lastPrice);
	},
	//textarea字数统计
	wordStatic: function(inputStr) {
		// 获取输入内容长度并更新到界面
		var value = inputStr;
		// 将换行符，前后空格不计算为单词数
		value = value.replace(/[\u0021-\u002f\u003a-\u0040\u005b-\u0060\u007b-\u007e]/g, "");
		value = value.replace(/[\u0030-\u0039\u0041-\u005a\u0061-\u007a]+/g, "数");
		value = value.replace(/\n|\r|\s+/gi, "");
		var length = 0;
		var match = value.match(/./g);
		if (match) {
			length = match.length;
		} else if (value) {
			length = 1;
		}
		return length;
	},
	//上传检测文件
	uploadFile: function(argument) {
		var fileArray = new Array();
		var fileNumber = 0;
		$div = '<div id="${fileID}" class="uploadify-queue-item form-horizontal">\
		            <div class="cancel">\
		                <a class="${fileID} canceltips" href="javascript:$(\'#${instanceID}\').uploadify(\'cancel\', \'${fileID}\')">X</a>\
		            </div>\
		            <span class="fileName">${fileName} (${fileSize})</span><span class="data"></span>\
		            <div class="zs form-group">\
						<div class="zsname col-sm-3 control-label">论文检测字数：</div><div class="col-sm-9"><select   name="contactCheckWords" onchange="turnitin_fun.wdzs_count()" class="form-control required wdzs"><option value="1" selected="selected">≤5000</option><option value="2">5001~10000</option><option value="3">10001~15000</option><option value="4">15001~20000 </option><option value="5">20001~25000</option><option value="6">30000</option></select></div>\
		            </div>\
		        </div>';

		$('#file_upload').uploadify({
			'swf': swfurl, //指定上传控件的主体文件
			'uploader': uploader, //指定服务器端上传处理文件
			'successTimeout': 99999,
			'buttonCursor': 'hand',
			'multi': true, //是否支持多文件上传
			'height': '34',
			'width': '106',
			'fileSizeLimit': '2MB', //限制文件上传大小
			'fileTypeDesc': '支持的格式：',
			'queueSizeLimit': 3,
			'uploadLimit': 3,
			'auto': false, //选定文件后是否自动上传，默认false
			'removeCompleted': false,
			'fileTypeExts': '*.rar;*.zip;*.doc;*.docx;*.wps;*.ppt;*.xls;*.txt',
			'buttonText': ' <i class="uicon-library"></i>  选择文件...', //上传按钮内容显示文本
			'itemTemplate': $div,
			onUploadStart: function(file) {},
			onSelect: function(file) {
				turnitin_fun.wdzs_count();
			},
			onCancel: function(file) {
				//更新现在的价格。
				statistics = parseInt($("#" + file.id).find('.wdzs').val());
				beforePrice = $(".autofy").text();
				beforePrice = beforePrice - statistics * 5;
				$(".autofy").text(beforePrice.toFixed(2));
			},
			onQueueComplete: function(queueData) {
				jQuery(".btn-submit-turnitin").removeAttr('disabled').html(' 提交订单 ');
			},
			//其他配置项
			'onUploadSuccess': function(file, data, response) {
				$("#" + file.id).attr('id', $("#" + file.id).attr('id') + '_success').removeAttr('style');
				fileNumber++;
				var dataObj = eval("(" + data + ")"); //转换为json对象
				orderSet.turnitin.fileList.push(dataObj.filefullPath);
				$('#pregress').html('<i class="uicon-bookmark"></i> 已成功上传' + orderSet.turnitin.fileList.length + '个文件');
				turnitin_fun.wdzs_count();
				$(".data").text("");
				$("." + file.id).attr('href', 'javascript:;').attr("title", "已成功上传").removeClass('canceltips').addClass('successUploadTips');
			},
			onSelectError: uploadify_onSelectError,
			onUploadError: uploadify_onUploadError,
			onFallback: function() {
				$(".fileuploadarea").html("无法进行上传，请把附件发送至客服邮箱");
			}
		});
	},
	validate: function() {
		$(".turnitin-order-form").validate(turnitin_fun.turnitin_validate_config);
	},
	submit: function() {
		$_submit = jQuery(".btn-submit-turnitin");
		// 检测方式
		if (orderSet.turnitin.turnitinMode == 0) { // 上传方式
			turnitin_fun.wdzs_count();
			var ufile = JSON.stringify(orderSet.turnitin.fileList);
			var uTextDetection = "";
			if (ufile == "" || ufile == "[]") {
				banner_alert('请上传需要检测的文件');
				$(".canceltips").parent().parent().css("border", "2px solid #F80909");
				return false;
			}
			$dl = $(".canceltips").text();
			if ($dl != "") {
				banner_alert('队列中还有未上传的文件.');
				$(".canceltips").parent().parent().css("border", "2px solid #F80909");
				return false;
			}
		} else { // 文本方式
			turnitin_fun.calculation();
			var ufile = "";
			var uTextDetection = $("#turnitinContent").val();
			if (orderSet.turnitin.wordCount < 100) {
				banner_alert('论文内容至少100字, 当前' + orderSet.turnitin.wordCount + '字.');
				return false;
			}
		}
		var fy = orderSet.turnitin.lastPrice;
		var uWords = orderSet.turnitin.wordCount;
		sns_json = '{ "snsqq":"' + $("#qq").val() + '" , "snsWechat":"' + $("#wechat").val() + '" }';
		$("input[name='sns']").val(sns_json);
		$("input[name='ufy']").val(fy);
		$("input[name='uWords']").val(uWords);
		$("input[name='ufile']").val(ufile);
		var ajax_data = $(".turnitin-order-form").serializeArray();
		$_submit.attr('disabled', 'disabled').html(' 正在提交... ');
		jQuery.post('/wp-admin/admin-ajax.php', ajax_data,
			function(data) {
				data.status == 1 ? location.replace(data.link) : $_submit.removeAttr('disabled').html(' 确认,提交订单 ');
			}
		);
	}
});

// 语法校正服务
var grammar_fun = new Object({
	updatePriceDay:function() {
		$zs = $(".wdzs").val();
		if (!(typeof($zs) === 'undefined')) {
			if ($zs <= 5) {
				needTime = " 1-3 天";
			} else {
				needTime = " 3-7 天";
			}
			$(".needTime").text(needTime);
			orderSet.grammar.lastPrice = $zs * orderSet.grammar.initialPrice;
			$(".autofy").text(orderSet.grammar.lastPrice);
		}
	},
	uploadFile: function() {
		$grammardiv = '<div id="${fileID}" class="uploadify-queue-item form-horizontal">\
                <div class="cancel">\
                    <a class="${fileID} canceltips" href="javascript:$(\'#${instanceID}\').uploadify(\'cancel\', \'${fileID}\')">X</a>\
            </div>\
            <span class="fileName">${fileName} (${fileSize})</span><span class="data"></span>\
            <div class="zs form-group">\
				<label class="zsname col-sm-3 control-label">文件字数：</label><div class="col-sm-9"><select  name="contactCheckWords" onchange="grammar_fun.updatePriceDay()" class="form-control required wdzs"><option value="1" selected="selected">1~1000 words</option><option value="2">1001~2000 words</option><option value="3">2001~3000 words</option><option value="4">3001~4000 words</option><option value="5">4001~5000 words</option><option value="6">5001~6000 words</option><option value="7">6001~7000 words</option><option value="8">7001~8000 words</option><option value="9">8001~9000 words</option><option value="10">9001~10000 words</option><option value="11">10001~11000 words</option><option value="12">11001~12000 words</option><option value="13">12001~13000 words</option><option value="14">13001~14000 words</option><option value="15">14001~15000 words</option><option value="16">15001~16000 words</option><option value="17">16001~17000 words</option><option value="18">17001~18000 words</option><option value="19">18001~19000 words</option><option value="20">19001~20000 words</option></select></div>\
            </div>\
        </div>';
		var grammar_uploadify_config = {
			'swf': swfurl, //指定上传控件的主体文件
			'uploader': uploader, //指定服务器端上传处理文件
			'successTimeout': 99999,
			'buttonClass': 'file_select_button',
			'buttonCursor': 'hand',
			//是否支持多文件上传
			'multi': true,
			'auto': false, //选定文件后是否自动上传，默认false
			'height': '34',
			'width': '106',
			//限制文件上传大小
			'fileSizeLimit': '2MB',
			'fileTypeDesc': '支持的格式：',
			'queueSizeLimit': 1,
			'uploadLimit': 1,
			'removeCompleted': false,
			'fileTypeExts': '*.rar;*.zip;*.jpg;*.png;*.bmp;*.pdf;*.doc;*.docx;*.wps;*.ppt;*.xls;*.txt',
			//上传按钮内容显示文本
			'buttonText': ' <i class="uicon-library"></i>  选择文件...',
			'itemTemplate': $grammardiv,
			onUploadStart: function(file) {},
			onSelect: function(file) {
				grammar_fun.updatePriceDay();
			},
			onCancel: function(file) {
				banner_alert("已取消文件")
			},
			onQueueComplete: function(queueData) {},
			//其他配置项
			'onUploadSuccess': function(file, data, response) {
				$("#" + file.id).attr('id', $("#" + file.id).attr('id') + '_success').removeAttr('style');
				grammar_fun.updatePriceDay();
				var dataObj = eval("(" + data + ")"); //转换为json对象
				orderSet.grammar.fileList.push(dataObj.filefullPath); //上传文件存放处
				$("." + file.id).attr('href', 'javascript:;').attr("title", "已成功上传").removeClass('canceltips').addClass('successUploadTips');
			},
			onFallback: function() {},
			onUploadProgress: function(file, bytesUploaded, bytesTotal, totalBytesUploaded, totalBytesTotal) {},
			onSelectError: uploadify_onSelectError,
			onUploadError: uploadify_onUploadError,
		}
		$('#grammar_file_upload').uploadify(grammar_uploadify_config);
	},
	validate: function() {
		//提交订单表单
		$(".grammar-order-form").validate({
			submitHandler: function(form) { //表单提交句柄,为一回调函数，带一个参数：form
				grammar_fun.submit();
			}
		});
	},
	submit:function() {
		//更新价格以及字数
		grammar_fun.updatePriceDay();
		$_submit = $(".btn-submit-grammar");
		sns_json = '{ "snsqq":"' + $("#qq").val() + '" , "snsWechat":"' + $("#wechat").val() + '" }';
		$("input[name='sns']").val(sns_json);
		$("input[name='ufy']").val(orderSet.grammar.lastPrice);
		$("input[name='uWords']").val(words_count(1000));
		var ufile = JSON.stringify(orderSet.grammar.fileList);
		if (ufile == "" || ufile == "[]") {
			banner_alert('请上传需要检测的文件.');
			return false;
		}
		$dl = $(".canceltips").text();
		if ($dl != "") {
			banner_alert('队列中还有未上传的文件.');
			$(".canceltips").parent().parent().css("border", "2px solid #F80909");
			return false;
		}
		$("input[name='ufile']").val(ufile);
		$(".wdzs").attr('disabled', true);
		var ajax_data = $(".grammar-order-form").serializeArray();
		$_submit.attr('disabled', true).html(' 正在提交... ');
		jQuery.post('/wp-admin/admin-ajax.php', ajax_data,
			function(data) {
				data.status == 1 ? location.replace(data.link) : $_submit.removeAttr('disabled').html(' 确认,提交订单 ');
			}
		);
	}
});

//学术翻译服务
var translate_fun = new Object({
	unitPrice: function() {
		var statistics = 0;
		$(".wdzs").each(function() {
			statistics = statistics + parseInt($(this).val());
		});
		return statistics;
	},
	total: function() {
		$initialPrice = translate_fun.unitPrice() * $("input[name='oUse']:checked").val();
		jQuery(".autofy").text($initialPrice);
	},
	uploadFile: function() {
		var translateFileArray = new Array();
		var translateFileNumber = 0;
		$fydiv = '<div id="${fileID}" class="uploadify-queue-item form-horizontal">\
		                <div class="cancel">\
		                    <a class="${fileID} canceltips" href="javascript:$(\'#${instanceID}\').uploadify(\'cancel\', \'${fileID}\')">X</a>\
		                </div>\
		                <span class="fileName">${fileName} (${fileSize})</span><span class="data"></span>\
		                <div class="zs form-group">\
							<label class="zsname col-sm-3 control-label">文件字数：</label><div class="col-sm-9"><select  name="contactCheckWords" onchange="translate_fun.total()" class="form-control required wdzs"><option value="1" selected="selected">1~500 words</option><option value="2">501~1000 words</option><option value="3">1001~1500 words</option><option value="4">1501~2000 words</option><option value="5">2001~2500 words</option><option value="6">2501~3000 words</option><option value="7">3001~3500 words</option><option value="8">3501~4000 words</option><option value="9">4001~4500 words</option><option value="10">4501~5000 words</option><option value="11">5001~5500 words</option><option value="12">5501~6000 words</option><option value="13">6001~6500 words</option><option value="14">6501~7000 words</option><option value="15">7001~7500 words</option><option value="16">7501~8000 words</option><option value="17">8001~8500 words</option><option value="18">8501~9000 words</option><option value="19">9001~9500 words</option><option value="20">9501~10000 words</option></select></div>\
		                </div>\
		            </div>';
		//翻译上传配置
		var fanyi_uloadify_config = {
			'swf': swfurl, //指定上传控件的主体文件
			'uploader': uploader, //指定服务器端上传处理文件
			'successTimeout': 99999,
			'buttonClass': 'file_select_button',
			'buttonCursor': 'hand',
			//是否支持多文件上传
			'multi': true,
			'auto': false, //选定文件后是否自动上传，默认false
			'height': '34',
			'width': '106',
			//限制文件上传大小
			'fileSizeLimit': '2MB',
			'fileTypeDesc': '支持的格式：',
			'queueSizeLimit': 3,
			'uploadLimit': 3,
			'removeCompleted': false,
			'fileTypeExts': '*.rar;*.zip;*.jpg;*.png;*.bmp;*.pdf;*.doc;*.docx;*.wps;*.ppt;*.xls;*.txt',
			//上传按钮内容显示文本
			'buttonText': '<i class="uicon-library"></i> 选择文件...',
			'itemTemplate': $fydiv,
			onUploadStart: function(file) {
				jQuery(".translateOrderBtn").attr('disabled', true).html(' 正在上传... ');
			},
			onSelect: function(file) {
				translate_fun.total();
			},
			onCancel: function(file) {
				if ($("#" + file.id).length > 0) {
					//更新现在的价格。
					statistics = parseInt($("#" + file.id).find('.wdzs').val());
					beforePrice = $(".autofy").text();
					$(".autofy").text(beforePrice - statistics * $("input[name='oUse']:checked").val());
				}
			},
			onQueueComplete: function(queueData) {
				jQuery(".translateOrderBtn").removeAttr('disabled').html(' 提交订单 ');
			},
			//其他配置项
			'onUploadSuccess': function(file, data, response) {
				var dataObj = eval("(" + data + ")"); //转换为json对象
				translateFileArray.push(dataObj.filefullPath); //赋值
				translationUploadFile = JSON.stringify(translateFileArray);
				$initialPrice = translate_fun.unitPrice() * $("input[name='oUse']:checked").val();
				jQuery(".autofy").text($initialPrice);
				$("." + file.id).attr('href', 'javascript:;').attr("title", "已成功上传").removeClass('canceltips').addClass('successUploadTips');
			},
			onFallback: function() {},
			onUploadProgress: function(file, bytesUploaded, bytesTotal, totalBytesUploaded, totalBytesTotal) {},
			onSelectError: uploadify_onSelectError,
			onUploadError: uploadify_onUploadError,
		}
		$('#fy_file_upload').uploadify(fanyi_uloadify_config);
	},
	validate: function() {
		//翻译验证配置
		var fanyi_validate_config = {
			submitHandler: function(form) {
				translate_fun.submit();
			}
		}
		$(".fanyi-order-form").validate(fanyi_validate_config);
	},
	submit: function() {
		$initialPrice = translate_fun.unitPrice() * $("input[name='oUse']:checked").val();
		jQuery(".autofy").text($initialPrice);

		$_submit = jQuery(".btn-submit-translate");
		var ufile = translationUploadFile;
		var fy = $.trim($(".autofy").text());
		sns_json = '{ "snsqq":"' + $("#qq").val() + '" , "snsWechat":"' + $("#wechat").val() + '" }';
		if (ufile == "") {
			banner_alert('请上传需要检测的文件.');
			return false;
		}
		$dl = $(".canceltips").text();
		if ($dl != "") {
			banner_alert('队列中还有未上传的文件.');
			return false;
		}


		$("input[name='ufy']").val(fy);
		$("input[name='uWords']").val(words_count(500));
		$("input[name='oUse']").val(jQuery('input[type="radio"][name="oUse"]:checked').attr('title'));
		$("input[name='oLanguage']").val(jQuery('input[type="radio"][name="oLanguage"]:checked').attr('title'));
		$("input[name='ufile']").val(ufile);
		$("input[name='sns']").val(sns_json);



		var ajax_data = $(".fanyi-order-form").serializeArray();
		$(".wdzs").attr('disabled', 'disabled');

		$_submit.attr('disabled', 'disabled').html(' 正在提交... ');
		jQuery.post('/wp-admin/admin-ajax.php', ajax_data,
			function(data) {
				data.status == 1 ? location.replace(data.link) : $_submit.removeAttr('disabled').html(' 确认,提交订单 ');
			}
		);
	}
});



































//@ begin 留学文书订单
$(function() {

	//验证段落
	$(".application_Form").Validform({
		btnSubmit: ".applicationOrderBtn",
		showAllError: true,
		callback: function(form) {
			applicationOrderNow();
			return false;
		},
		tiptype: function(msg, o, cssctl) {
			if (!o.obj.is("form")) { //验证表单元素时o.obj为该表单元素，全部验证通过提交表单时o.obj为该表单对象;
				var objtip = o.obj.siblings(".ui_tips");
				cssctl(objtip, o.type);
				objtip.html(msg);
				objtip.prev().removeClass('ui_icon_success').addClass('ui_icon');
				objtip.prev().prev().addClass('highlight2');
				if (o.type == 2) {
					objtip.prev().removeClass('ui_icon').addClass('ui_icon_success');
					objtip.text(msg);
					objtip.prev().prev().removeClass('highlight2');

				}
			}

		}
	});
	//生成服务项目
	jQuery(".serviceProject li").on("click", function() {
		$(".shoppingItem").show();
		$(".requirement-remarks").show();
		//orderSet.as.display = "block";
		//addfoo(this);
		itemClass = $(this).attr('class');
		//通过点击更新服务项目数字
		addItemByClass(itemClass);
	});

	//+ 号增加项目
	jQuery(".itemSubPlus").on("click", function() {
		$(".shoppingItem").show();
		$(".requirement-remarks").show();
		//orderSet.as.display = "block";
		//addfoo(this);
		itemClass = $(this).attr('data-class');
		//通过点击更新服务项目数字
		addItemByClass(itemClass);
	});

	//子项目删除指定项目
	jQuery(".deleteIcon").on("click", function() {
		$parentObject = $(this).parent();
		originalClass = $(this).attr('data-class');
		deleteItemByLabel($parentObject, originalClass);
		setOrderItemPriceBycls(originalClass, 1);
	});

	//父项目删除指定项目
	jQuery(".itemSubReduction").on("click", function() {
		originalClass = $(this).attr('data-class');
		if (orderSet.as[originalClass].currentNumber == 1) {
			$(itemClass).children('.shoppingCartnum').children('.itemSubReduction').addClass('no-plus');
			ui_gritter_img('warning', '消息提示', '购买数量必须为大于0的整数.');
			return false;
		}
		orderSet.as[originalClass].currentNumber--;
		itemClass = ".shoppingCart .body ." + originalClass;
		setOrderItemPriceBycls(originalClass, $(this).attr("data-nosubItem"));
		//设置文本框   份
		$(itemClass).children('.shoppingCartnum').children('.num').val(orderSet.as[originalClass].currentNumber);

	});

	//字数改变更新价格
	jQuery(".SELECTZS").on("change", function() {
		itemClass = $(this).attr('data-class');
		setOrderItemPriceBycls(itemClass, 1);
	});

	//更新介绍
	jQuery(".serviceProject li").on("hover", function() {
		var introduction = {
			'resume': '<p>个人简历是申请外国高校及港澳等地高校时，由申请人撰写的关于个人教育背景，个人工作、科研经历以及获得的各种荣誉的总结。个人简历作为重要的留学文书材料，其质量的高低会直接影响到申请的成功率，其重要性不言而喻。对于留学简历的书写，一定要注意必须尽力保证字迹的工整、清楚，最好用打字机打印。一份成功的留学简历的秘诀在于：既不要言过其实，又必须充分反映自己的实际水平。</p>',
			'recommend': '<p>推荐信从第三者的角度去客观的评价留学申请者的学习水平、学习能力和潜在发展方向，以及待人处事的能力、主观能动性、适应能力等等。推荐信是除自述外最重要的学校申请文件，起着从侧面介绍、证明的作用。由于其客观性，推荐信能起到自述所起不到的作用。因而，推荐信的内容更应客观具体，不能虚夸粉饰。</p>',
			'essay': '<p>留学Essay是一个展示自己主观想法、个性、性格、特性的地方，它是留学文书申请中的重要材料之一。大学希望在这些文章之中看到你的与众不同，看到你无法通过客观事实体现的部分，如果只是重复罗列自己的成就、奖项反而丧失了一个很好的展示自己想法、特性的机会。Application Essay的内容不应该是申请学生的简单背景介绍或者学习成绩的机械罗列，而是着重应该写学生的思考、观察、反省以及从经历中所学到的东西。</p>',
			'ps': '<p>个人陈述就是常说的PS，出国留学文书中的个人陈述能弥补硬件方面的不足，为自己的申请加分，因此常常能决定申请的成败。PS是在申请过程中按照学校要求来写一篇有关申请人背景，学术成就和未来研究和职业目标的文章。一篇成功的个人陈述应不但应该语言流畅，逻辑严谨，层次分明，更要充分显示申请人的才华并抓住审阅人的注意力。</p>',
		}
		c = $(this).attr("class");
		cntitle = $(this).attr("data-title-cn");
		entitle = $(this).attr("data-title-en");
		$(".orderIntroduction").html("<h3>" + cntitle + "</h3><small>" + entitle + "</small>");
		$(".itemIntroduction").html(introduction[c]);
	});

	//设置加急服务
	jQuery(".label-urgent").click(function() {
		originalClass = $(this).attr('data-class');
		itemClass = ".shoppingCart .body ." + originalClass;
		if ($(this).attr("data-state") == "0") {
			$(this).children().addClass('U-checked');
			$(this).attr("data-state", 1); //更新加急状态
			$(this).parent().parent().find('.service-time').text(orderSet.as[originalClass].urgentTime); //更新页面天数
		} else {
			$(this).children().removeClass('U-checked');
			$(this).attr("data-state", 0); //更新加急状态
			$(this).parent().parent().find('.service-time').text(orderSet.as[originalClass].initialTime); //更新页面天数
		}
		orderSet.as[originalClass].urgentState = $(this).attr("data-state"); //更新加急状态
		setOrderItemPriceBycls(originalClass, $(this).attr("data-nosubItem"));
	});

	//移除服务
	jQuery(".deleteitem").click(function() {
		originalClass = $(this).attr('data-class');
		itemClass = ".shoppingCart .body ." + originalClass;
		removeItem(originalClass);
		if ($(this).attr('data-nosubItem') == 1) {
			removalElements = '.label_' + originalClass;
			$(itemClass).find(removalElements).remove();
			//指定元素textarea 元素 重置
			$orderCurrentTextarea = $(itemClass).find(".orderRemarks");
			$orderCurrentTextarea.height(58.4);
		}
		$(itemClass).fadeOut();
	});

	//备注设置
	jQuery(".shoppingItem .orderRemarks").blur(function() {
		itemClass = $(this).attr('data-class');
		orderSet.as[itemClass].remarks = $(this).val();
	});

	//详细设置
	jQuery(".pullDown").click(function() {
		originalClass = $(this).next().attr('data-class');
		itemClass = ".shoppingCart .body ." + originalClass;
		$(".subItem").hide();

		//显示当前的子项目
		if ($(this).hasClass('shrinkage')) {
			$(this).removeClass('shrinkage');
			$(itemClass + " .subItem").hide();
		} else {
			$(".shoppingCart .body").find('.pullDown').removeClass('shrinkage');
			$(this).addClass('shrinkage');
			$(itemClass + " .subItem").show();
		}
	});
	var uploadTypeDom = {
		'resume': '<div class="file" id="${fileID}" title="${fileName}">\
							<div class="cancel" title="删除">\
	                        	<a class="${fileID} canceltips" href="javascript:$(\'#${instanceID}\').uploadify(\'cancel\', \'${fileID}\')"></a>\
	                    	</div>\
							<div class="filetype" >\
							</div>\
							<span class="fileName">${fileSize}</span>\
							<span class="data"></span>\
						</div>'
	}
	var fileFormat = ['bmp', 'doc', 'docx', 'jpg', 'pdf', 'png', 'rar', 'text', 'xls', 'zip'];
	$('#ws_file_upload_button').uploadify({
		'debug': false,
		'swf': swfurl, //指定上传控件的主体文件
		'uploader': uploader, //指定服务器端上传处理文件
		'successTimeout': 99999,
		'buttonClass': 'file_select_button',
		'buttonCursor': 'hand',
		//是否支持多文件上传
		'multi': true,
		'auto': false, //选定文件后是否自动上传，默认false
		'height': '30',
		'width': '112',
		//限制文件上传大小
		'fileSizeLimit': '2MB',
		'fileTypeDesc': '支持的格式：',
		'queueSizeLimit': 3,
		'uploadLimit': 3,
		'removeCompleted': false,
		'fileTypeExts': '*.rar;*.zip;*.jpg;*.png;*.bmp;*.pdf;*.doc;*.docx;*.wps;*.ppt;*.xls;*.txt',
		//上传按钮内容显示文本
		'buttonText': '<i class="uicon-computer fa-lg"></i>添加文件',
		'progressData': 'percentage',
		'itemTemplate': uploadTypeDom.resume,
		onUploadStart: function(file) {},
		onSelect: function(file) {
			$(".uploadbtn").show();
			s = file.type;
			s = s.replace(".", "");
			fileFormat.indexOf('1') == 0 ? s = s : s = "text";
			uploadCurrentPath = $("#currentPath").val()
			img = "<img src=" + uploadCurrentPath + "/img/order/icon/icon-" + s + ".png>";
			$("#" + file.id).children('.filetype').html(img);
			$("#" + file.id).children('.data').addClass("p_txt").html("等待上传");
		},
		onCancel: function(file) {

		},
		onQueueComplete: function(queueData) {

		},
		'onUploadSuccess': function(file, data, response) {
			var dataObj = eval("(" + data + ")"); //转换为json对象
			addFileJsonForUploadJson(dataObj.filefullPath);
			$("#" + file.id).children('.data').addClass("p_txt").html("上传完毕");
			$("#" + file.id).children('.cancel').html("");
		}
	});
});

//增加子项目
function addItemByClass(itemClass) {
	//设置cls
	originalClass = itemClass;
	//设置 子 cls
	itemClass = ".shoppingCart .body ." + itemClass;
	//获取当前服务项目的值
	// orderSet.as[originalClass].currentNumber = parseInt($(itemClass).children('.shoppingCartnum').children('.num').val());
	//隐藏所有的子项目
	$(".subItem").hide();
	$(".shoppingCart .body").find('.pullDown').removeClass('shrinkage');
	//显示当前的子项目
	$(itemClass + " .subItem").show();
	$(itemClass).find('.pullDown').addClass('shrinkage');
	//判断单个项目不能超过5份
	if (orderSet.as[originalClass].currentNumber < 5) {
		if ($(itemClass).is(":hidden")) {
			$(itemClass).show();
		}
		orderSet.as[originalClass].currentNumber++;
	} else {
		ui_gritter_img('warning', '消息提示', '单个项目购买数量不能超过5份.');
		return false; //exit
	}
	//设置购买数量
	$(itemClass).children('.shoppingCartnum').children('.num').val(orderSet.as[originalClass].currentNumber);
	//判断是否是没有子项目的。
	if ($(itemClass).hasClass('noSubItem')) {
		setOrderItemPriceBycls(originalClass, 0);
	} else {
		//设置文本域的高度
		$(itemClass + " .rightSubItem .orderRemarks").height($(itemClass + " .rightSubItem .orderRemarks").height() + 32.4);
		//生成SELECT字数选择框
		itemZs = '<label class="label_' + originalClass + '"><i class="deleteIcon" title="删除" data-class="' + originalClass + '"></i> 字数：<select class="' + originalClass + '_SELECT SELECTZS" data-class="' + originalClass + '"><option value="1"  selected="selected">250 Words</option><option value="2">500 Words</option><option value="3">750 Words</option><option value="4">1000 Words (~4 pages)</option><option value="5">1250 Words (~5 pages)</option><option value="6">1500 Words (~6 pages)</option><option value="7">1750 Words (~7 pages)</option><option value="8">2000 Words (~8 pages)</option><option value="9">2250 Words (~9 pages)</option><option value="10">2500 Words (~10 pages)</option><option value="11">2750 Words (~11 pages)</option><option value="12">3000 Words (~12 pages)</option><option value="13">3250 Words (~13 pages)</option><option value="14">3500 Words (~14 pages)</option><option value="15">3750 Words (~15 pages)</option><option value="16">4000 Words (~16 pages)</option><option value="17">4250 Words (~17 pages)</option><option value="18">4500 Words (~18 pages)</option><option value="19">4750 Words (~19 pages)</option><option value="20">5000 Words (~20 pages)</option></select></label>';
		//添加字数选择框 //字数替换
		$(itemClass + " .leftSubItem").append(itemZs);
		setOrderItemPriceBycls(originalClass, 1);
	}
}

//删除子项目
function deleteItemByLabel(parentObject, originalClass) {
	if (orderSet.as[originalClass].currentNumber == 1) {
		ui_gritter_img('warning', '消息提示', '购买数量必须为大于0的整数<br />不需要此服务项目请删除');
		return false;
	}
	orderSet.as[originalClass].currentNumber--; // 数量减一
	//指定元素textarea 元素 重置
	$orderCurrentTextarea = $parentObject.parent().next().find(".orderRemarks");
	$orderCurrentTextarea.height($orderCurrentTextarea.height() - 32.4);
	$(".shoppingCart .body ." + originalClass).children('.shoppingCartnum').children('.num').val(orderSet.as[originalClass].currentNumber);
	parentObject.remove(); //移除指定元素
}

//移除服务
function removeItem(itemClass) {
	originalClass = itemClass;
	itemClass = ".shoppingCart .body ." + originalClass;
	orderSet.as[originalClass].currentNumber = 0;
	setOrderItemPriceBycls(originalClass, 0);
	$(itemClass).children('.initialPrice').text(initialUnitPrice);
	$(itemClass + " .subitemPrice").text(orderSet.as[originalClass].lastPrice);
	$(itemClass).children('.shoppingCartnum').children('.num').val(orderSet.as[originalClass].currentNumber);
}

//设置订单金额ByClass
function setOrderItemPriceBycls(originalClass, isWords) {
	itemClass = ".shoppingCart .body ." + originalClass;
	//更新初始价格
	if (orderSet.as[originalClass].urgentState == "0") {
		initialUnitPrice = orderSet.as[originalClass].initialPrice.toFixed(2);
	} else {
		initialUnitPrice = (orderSet.as[originalClass].initialPrice * orderSet.as[originalClass].urgentpriceMarkup).toFixed(2);
	}
	//设置初始价格
	$(itemClass).children('.initialPrice').text(initialUnitPrice);
	if (isWords == 1) { //判断是否含有字数选择框
		var statistics = 0;
		$("." + originalClass + "_SELECT").each(function() {
			statistics = statistics + parseInt($(this).val());
		});
		unitPrice = orderSet.as[originalClass].urgentState == "1" ? orderSet.as[originalClass].initialPrice * orderSet.as[originalClass].urgentpriceMarkup : orderSet.as[originalClass].initialPrice;
		//计算价格小计
		orderSet.as[originalClass].lastPrice = (statistics * unitPrice).toFixed(2);
	} else {
		unitPrice = orderSet.as[originalClass].urgentState == "1" ? orderSet.as[originalClass].initialPrice * orderSet.as[originalClass].urgentpriceMarkup : orderSet.as[originalClass].initialPrice;
		//计算价格小计
		orderSet.as[originalClass].lastPrice = (orderSet.as[originalClass].currentNumber * unitPrice).toFixed(2);
	}
	//设置价格小计
	$(itemClass + " .subitemPrice").text(orderSet.as[originalClass].lastPrice);

	setTotal();
	$(".dollartotal").text(orderSet.as.total);
	$(".sumItem").text(orderSet.as.sumItem);
}

//总计 &  数量
function setTotal() {
	var total = 0;
	var sumItem = 0;
	for (var i in orderSet.as) {
		if (typeof(orderSet.as[i].lastPrice) != 'undefined') {
			total = total + parseFloat(orderSet.as[i].lastPrice);
			sumItem = sumItem + parseFloat(orderSet.as[i].currentNumber);
		}
	}
	orderSet.as.sumItem = sumItem;
	orderSet.as.total = total.toFixed(2);
}

//增加新json 对象
function addFileJsonForUploadJson(elementURL) {
	var fileSingle = elementURL;
	orderSet.as.fileList.push(fileSingle);
}

//提交订单
function applicationOrderNow() {
	$self = jQuery(".applicationOrderBtn");
	var uname = $("#contactName").val();
	var umail = $("#contactMail").val();
	var utel = $("#contactAreaCode").val() + $("#contactTel").val();
	sns_json = '{ "snsqq":"' + $("#contactQQ").val() + '" , "snsWechat":"' + $("#contactWechat").val() + '" }';
	var ufile = JSON.stringify(orderSet.as.fileList);
	var fy = orderSet.as.total;
	if (orderSet.as.sumItem == 0) {
		ui_gritter_img('warning', '消息提示', '未选择服务项目.');
		return false;
	}
	ufile = ufile == "[]" ? "" : ufile;
	if (ufile == "") {
		ui_gritter_img('warning', '消息提示', '请上传1~3个文件.');
		return false;
	}

	var detailedInfo = JSON.stringify(treatmentInstrumentForJson());
	var ajax_data = {
		action: "ui_application_order_now",
		uname: uname,
		umail: umail,
		utel: utel,
		usns: sns_json,
		ufile: ufile,
		ufy: fy,
		detailedInfo: detailedInfo
	};
	$self.addClass('loading').attr('disabled', true).html(' 正在提交 ');
	jQuery(".shoppingItem").disable();
	jQuery(".serviceProject").disable();
	jQuery.post('/wp-admin/admin-ajax.php', ajax_data,
		function(data) {
			if (data == undefined || data == "" || data == null || data == 0) {
				$self.removeClass('loading').removeAttr('disabled').html(' 提交订单 ');
			} else {
				var dataObj = eval("(" + data + ")"); //转换为json对象
				location.href = dataObj.paymentLink;
			}
			jQuery(".shoppingItem").enable();
			jQuery(".serviceProject").enable();
		});
}

//字数字符串设置
function zstjStr(originalClass) {
	reutnStr = "";
	$("." + originalClass + "_SELECT").each(function() {
		reutnStr = reutnStr + $(this).find("option:selected").text() + "  -  ";
	});
	var reg = /-$/gi;
	reutnStr = reutnStr.replace(reg, "");
	return reutnStr;
}

//生成字符串
function treatmentInstrumentForJson() {
	var data_json = [];
	var orderRemarks = "";
	var wordsExplanation = "";
	var j = 0;
	for (i in orderSet.as) {
		j++;
		if (j > 3) {
			if (orderSet.as[i].currentNumber > 0) {
				if (orderSet.as[i].urgentState == 0) {
					isUrgent = "非加急订单";
					isUrgent += ",加急单价：- ";
					isUrgent += ",加急天数：- ,";
				} else {
					isUrgent = "加急订单";
					urgentUnit = orderSet.as[i].initialPrice * orderSet.as[i].urgentpriceMarkup;
					isUrgent += ",加急单价：USD " + urgentUnit.toFixed(2);
					isUrgent += ",加急天数：" + orderSet.as[i].urgentTime + ",";
				}
				if (orderSet.as[i].remarks != "") {
					orderRemarks = ",订单备注：" + orderSet.as[i].remarks;
				}
				estimateDays = ",预计天数：" + orderSet.as[i].initialTime;

				//字数说明
				if (orderSet.as[i].hasSubItem == 1) {
					wordsExplanation = ",字数说明：" + zstjStr(i);
				}
				//字符串连接
				tStr = "服务项目：" + orderSet.as[i].title + ", 数量：" + orderSet.as[i].currentNumber + " 份,单价：USD " + orderSet.as[i].initialPrice + estimateDays + " ,加急服务：" + isUrgent + "小计金额： USD " + orderSet.as[i].lastPrice + orderRemarks + wordsExplanation;
				data_json.push(tStr);
			}
		}
	}
	return data_json;
}
//@ end  留学文书订单





//@ end 语法校正订单End

//@ begin 理工项目订单
$(function() {
	//验证段落
	$(".polytechnic_Form").Validform({
		datatype: {
			"country": function(gets, self, curform, regxp) {
				errorElement = $(".verifCountry").find('.errtips');
				errorElement.hide();
				if ($(".verifCountry").find('#contactCountry').val() == "") {
					errorElement.show();
					errorElement.children(".tb").removeClass('ui_icon_success ui_icon_select ui_icon_select_success').addClass('ui_icon  ui_icon_success_select');
					errorElement.children(".ui_tips").removeClass('Validform_checktip Validform_right').addClass('Validform_checktip Validform_wrong').html("请选择留学国家.");
				} else if ($(".verifCountry").find('#contactCountry').val() == "-1") {
					errorElement.show();
					if ($("#userCountryOfStudy").val() == "") {
						errorElement.children(".tb").removeClass('ui_icon_success ui_icon_success_select').addClass('ui_icon ui_icon_select');
						errorElement.children(".ui_tips").removeClass('Validform_checktip Validform_right').addClass('Validform_checktip Validform_wrong').html("请输入留学国家.");
					} else {
						errorElement.children(".tb").removeClass('ui_icon ui_icon_select').addClass('ui_icon_success ui_icon_select_success');
						errorElement.children(".ui_tips").removeClass('Validform_checktip Validform_wrong').addClass('Validform_checktip Validform_right').html("通过信息验证！");
						return true;
					}
				} else {
					errorElement.show();
					errorElement.children(".tb").removeClass('ui_icon ui_icon_select ui_icon_select_success').addClass('ui_icon_success ui_icon_success_select');
					errorElement.children(".ui_tips").removeClass('Validform_checktip Validform_wrong ').addClass('Validform_checktip Validform_right').html("通过信息验证！");
					return true;
				}
				return false;
			}
		},
		showAllError: true,
		btnSubmit: ".polytechnicOrderBtn",
		callback: function(form) {
			polytechnicOrderNow();
			return false;
		},
		tiptype: function(msg, o, cssctl) {
			if (!o.obj.is("form")) { //验证表单元素时o.obj为该表单元素，全部验证通过提交表单时o.obj为该表单对象;
				var objtip = o.obj.siblings(".ui_tips");
				cssctl(objtip, o.type);
				objtip.html(msg);
				objtip.prev().removeClass('ui_icon_success').addClass('ui_icon');
				objtip.prev().prev().addClass('highlight2');
				if (o.type == 2) {
					objtip.prev().removeClass('ui_icon').addClass('ui_icon_success');
					objtip.text(msg);
					objtip.prev().prev().removeClass('highlight2');
				}
			}
		}
	});
	var fileArray = new Array();
	$polytechnicdiv = '<div id="${fileID}" class="uploadify-queue-item">\
	                <div class="cancel">\
	                    <a class="${fileID} canceltips" href="javascript:$(\'#${instanceID}\').uploadify(\'cancel\', \'${fileID}\')">X</a>\
	                </div>\
	                <span class="fileName">${fileName}</span><span class="filesize">(${fileSize})</span><span class="data"></span>\
	            </div>';

	$('#polytechnic_file_upload').uploadify({
		'debug': false,
		'swf': swfurl, //指定上传控件的主体文件
		'uploader': uploader, //指定服务器端上传处理文件
		'successTimeout': 99999,
		'buttonClass': 'file_select_button',
		'buttonCursor': 'hand',
		//是否支持多文件上传
		'multi': true,
		'auto': false, //选定文件后是否自动上传，默认false
		'height': '28',
		'width': '106',
		//限制文件上传大小
		'fileSizeLimit': '2MB',
		'fileTypeDesc': '支持的格式：',
		'queueSizeLimit': 3,
		'uploadLimit': 3,
		'removeCompleted': false,
		'fileTypeExts': '*.rar;*.zip;*.jpg;*.png;*.bmp;*.pdf;*.doc;*.docx;*.wps;*.ppt;*.xls;*.txt',
		//上传按钮内容显示文本
		'buttonText': '<i class="uicon-computer"></i>  Add files...',
		'itemTemplate': $polytechnicdiv,
		onUploadStart: function(file) {},
		onSelect: function(file) {},
		onCancel: function(file) {},
		onQueueComplete: function(queueData) {},
		//其他配置项
		'onUploadSuccess': function(file, data, response) {
			var dataObj = eval("(" + data + ")"); //转换为json对象
			orderSet.polytechnic.fileList.push(dataObj.filefullPath); //赋值
			$("." + file.id).attr('href', 'javascript:;').attr("title", "已成功上传").removeClass('canceltips').addClass('successUploadTips');
		},
		onFallback: function() {

		},
		onUploadProgress: function(file, bytesUploaded, bytesTotal, totalBytesUploaded, totalBytesTotal) {

		},
		onSelectError: function(file, errorCode, errorMsg) {

		}
	});


	//留学国家 选"其他"
	$("#contactCountry").change(function() {
		$(".verifCountry").find('.errtips').hide();
		if ($("#contactCountry option:selected").val() == "-1") {
			$("#contactCountry").css("width", "66px");
			$("#userCountryOfStudy").show().css({
				"width": "158px",
				"margin-left": "5px"
			});
		} else {
			$("#userCountryOfStudy").hide();
			$("#contactCountry").css("width", "243px");
		}
	});

	//项目类别 选"其他"
	$("#PolytechnicProject").change(function() {
		if ($("#PolytechnicProject option:selected").val() == "-1") {
			$("#PolytechnicProject").css("width", "66px");
			$(".projectType").show().css({
				"width": "158px",
				"margin-left": "5px"
			});
		} else {
			$(".projectType").hide();
			$("#PolytechnicProject").css("width", "243px");
		}
	});

	//开发技术 选"其他"
	$("#developmentLanguage").change(function() {
		if ($("#developmentLanguage option:selected").val() == "-1") {
			$("#developmentLanguage").css("width", "66px");
			$(".delanguage").show().css({
				"width": "158px",
				"margin-left": "5px"
			});
		} else {
			$(".delanguage").hide();
			$(this).css("width", "243px");
		}
	});

	//操作系统 选"其他"
	$("#operatingSystem").change(function() {
		if ($("#operatingSystem option:selected").val() == "-1") {
			$("#operatingSystem").css("width", "66px");
			$(".opSystem").show().css({
				"width": "158px",
				"margin-left": "5px"
			});
		} else {
			$(".opSystem").hide();
			$(this).css("width", "243px");
		}
	});

	//数据库 选"其他"
	$("#Database").change(function() {
		if ($("#Database option:selected").val() == "-1") {
			$("#Database").css("width", "66px");
			$(".data").show().css({
				"width": "158px",
				"margin-left": "5px"
			});
		} else {
			$(".data").hide();
			$(this).css("width", "243px");
		}
	});
});

function polytechnicOrderNow() {
	$self = jQuery(".polytechnicOrderBtn");
	var uname = $("#contactName").val();
	var umail = $("#contactMail").val();
	var utel = $("#contactAreaCode").val() + $("#contactTel").val();
	sns_json = '{ "snsqq":"' + $("#contactQQ").val() + '" , "snsWechat":"' + $("#contactWechat").val() + '" }';
	var ufile = JSON.stringify(orderSet.polytechnic.fileList);
	var fy = orderSet.polytechnic.lastPrice;
	//留学国家
	var foreignCountries = $("#contactCountry option:selected").val() != "-1" ? $("#contactCountry option:selected").text() : $("#userCountryOfStudy").val();
	//项目标题
	var itemTitle = $("#ProjectTitle").val();
	//学术层次
	var academicLevel = $("#academicLevel").val();
	//项目类别
	var polytechnicProject = $("#PolytechnicProject option:selected").val() != "-1" ? $("#PolytechnicProject option:selected").text() : $(".projectType").val();
	//开发语言
	var developmentLanguage = $("#developmentLanguage option:selected").val() != "-1" ? $("#developmentLanguage option:selected").text() : $(".delanguage").val();
	// 数据库
	var database = $("#Database option:selected").val() != "-1" ? $("#Database option:selected").text() : $(".data").val();
	var oRemarks = $("#orderRemarks").val();
	var ajax_data = {
		action: "ui_polytechnic_order_now",
		uname: uname,
		umail: umail,
		utel: utel,
		usns: sns_json,
		ufile: ufile,
		ufy: fy,
		foreignCountries: foreignCountries,
		itemTitle: itemTitle,
		academicLevel: academicLevel,
		polytechnicProject: polytechnicProject,
		developmentLanguage: developmentLanguage,
		database: database,
		oRemarks: oRemarks
	};
	$self.addClass('loading').attr('disabled', true).html(' 正在提交 ');
	jQuery.post('/wp-admin/admin-ajax.php', ajax_data,
		function(data) {
			if (data == undefined || data == "" || data == null || data == 0 || data == "-1") {
				$self.removeClass('loading').removeAttr('disabled').html(' 提交订单 ');
			} else {
				location.href = "promptMsg?sid=" + data;
			}
		});
}
//@ end  理工项目订单End


//==================================全局jsfunction==================================
function alert($initialPrice) {}



//统计字数
function words_count($zs) {
	var statistics = 0;
	$(".wdzs").each(function() {
		statistics = statistics + parseInt($(this).val());
	});
	return statistics * $zs;
}
