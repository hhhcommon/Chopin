//优惠券
var coupon_config = {
	last_query_code: '',
	last_query_message: '',
	state: 0,
	money: 0
}
$(function() {
	$.datetimepicker.setLocale('zh');
	var currentDate = new Date();
	var currentDate = currentDate.getFullYear()+"/"+(currentDate.getMonth()+1)+"/"+currentDate.getDate();
	$('.datetimepicker').datetimepicker({value:currentDate,step:30,minDate:currentDate});


	var fileArray = new Array();
	swfurl = ui_config.theme_path + '/uploadify/uploadify.swf';
	uploader = ui_config.theme_path + '/uploadify/uploadify.php';

	//留学国家/地区
	$('#orderCountry').change(function() {
		otherSelect($(this), $("#orderCountryText"));
	});

	$('#orderFormat').change(function() {
		otherSelect($(this), $("#orderFormatText"));
	});

	$('#citationNumber').change(function() {
		otherSelect($(this), $("#citationNumberText"));
	});


	//上传文件
	$('#file_upload').uploadify({
		'swf': swfurl, //指定上传控件的主体文件
		'uploader': uploader, //指定服务器端上传处理文件
		'successTimeout': 99999,
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
		'fileTypeExts': '*.rar;*.zip;*.jpg;*.png;*.bmp;*.pdf;*.doc;*.docx;*.wps;*.ppt;*.xls;*.txt;*.rtf',
		//上传按钮内容显示文本
		'buttonText': ' <i class="uicon-library"></i>  选择文件...',
		//其他配置项
		'onUploadSuccess': function(file, data, response) {
			var dataObj = eval("(" + data + ")"); //转换为json对象
			fileArray.push(dataObj.filefullPath); //赋值
			$("#uploadFile").val($(fileArray).stringify());
			$('#pregress').html('<i class="fa fa-check"></i>已成功上传 <b>' + fileArray.length + '</b> 个文件').addClass('alert alert-upload_success');
		},
		'onSelectError': function(file, errorCode, errorMsg) {
			switch (errorCode) {
				case -100:
					alert("上传的文件数量已经超出系统限制的" + $('#file_upload').uploadify('settings', 'queueSizeLimit') + "个文件！");
					break;
				case -110:
					alert("文件 [" + file.name + "] 大小超出系统限制的" + $('#file_upload').uploadify('settings', 'fileSizeLimit') + "大小！");
					break;
				case -120:
					alert("文件 [" + file.name + "] 大小异常！");
					break;
				case -130:
					alert("文件 [" + file.name + "] 类型不正确！");
					break;
				default:
					break;
			}
		},
		onUploadProgress: function(file, bytesUploaded, bytesTotal, totalBytesUploaded, totalBytesTotal) {},
		onSelectError: function(file, errorCode, errorMsg) {
			log(errorCode)
			log(this.queueData.errorMsg)
		}
	});


	//提交订单表单
	$(".order-form").validate({
		ignore: ".coupon-text",
		submitHandler: function(form) { //表单提交句柄,为一回调函数，带一个参数：form
			//$("#orderAddOrderbtn").html(' 正在提交订单,请稍等片刻 ').addClass('loading').attr('disabled', true);
			sns_json = '{ "snsqq":"' + $("#contactOrderQQ").val() + '" , "snsWechat":"' + $("#contactOrderWechat").val() + '" , "lyfs":"' + $("#edit-field-order-ways-to-know-us-code-und").val() + '" }';
			$("input[name='postQQorSkype']").val(sns_json);
			var ajax_data = $(form).serializeArray();
			$action = {
				name: "action",
				value: "ui_create_order"
			};
			ajax_data.unshift($action); //增加执行方法。

			$("#orderAddOrderbtn").html(' 正在提交订单 , 请稍等片刻... ').addClass('loading').attr('disabled', 'disabled');
			jQuery.post('/wp-admin/admin-ajax.php', ajax_data,
				function(data) {
					if (data.status == 1) {
						location.href = "?s";
					} else {
						$("#orderAddOrderbtn").html(' 确认,提交订单 ').removeClass('loading').removeAttr('disabled');
					}
				}
			);
			//alert("提交表单");
			//form.submit();   //提交表单
		},
		rules: {
			orderMail: {
				required: true,
				email: true
			},
			orderCountry: {
				isSelected: true
			},
			orderCountryText: {
				isInputOhter: "#orderCountry"
			},
			orderFormat: {
				isSelected: true
			},
			orderFormatText: {
				isInputOhter: "#orderFormat"
			},
			citationNumber: {
				isSelected: true
			},
			citationNumberText: {
				isInputOhter: "#citationNumber"
			},
		},
		messages: {
			orderMail: {
				email: "请填写正确的电子邮件"
			},
			orderCountry: {
				isSelected: "请选择您留学的国家/地区"
			},
			orderFormat: {
				isSelected: "请选择一个引文样式"
			},
			citationNumber: {
				isSelected: "请选择一个引文数量"
			},
			orderCountryText: {
				isInputOhter: "请输入您留学的国家"
			},
			orderFormatText: {
				isInputOhter: "请输入一个引文样式"
			},
			citationNumberText: {
				isInputOhter: "请输入一个引文数量"
			},
		}
	});

	// 验证是否选中任何一项
	jQuery.validator.addMethod("isSelected", function(value, element) {
		return (value != "_none") ? true : false;
	}, "请选择一项");

	// 验证其他
	jQuery.validator.addMethod("isInputOhter", function(value, element, param) {
		if ($(param).val() != "-1") {
			return true;
		} else if ($(param).val() == "-1" && $(element).val() != "") {
			return true;
		}
	}, "请输入您留学的国家");


	//使用优惠券
	$('.user-coupon-btn').click(function() {
		var $parent = $(".user-coupon-btn").parent().parent();
		//移除上次提醒内容
		$parent.find(".coupon-error").remove();
		var __self = $(".user-coupon-btn");
		$couponCode = $.trim($(".coupon-text").val());
		if (coupon_config.last_query_code.toLowerCase() != $couponCode.toLowerCase() && $couponCode.length == 8) {
			var ajax_data = {
				action: 'ui_coupon_verify_coupons',
				couponsCode: $couponCode //优惠券编号
			};
			__self.html('正在验证优惠券').addClass('loading').attr('disabled', true);
			jQuery.post('/wp-admin/admin-ajax.php', ajax_data, function(data) {
				//优惠券验证成功
				if (data.status == 1) {
					$(".input-group-coupon").addClass('hidden');
					//设置
					coupon_config.money = data.money;
					//更新提醒
					$parent.append('<label for="contactOrderCoupons" class="coupon-error use-coupon-success">' + data.message + ' <button type="button" class="btn btn-xs btn-danger cancel-use-coupon-btn">取消使用</button></label>');
					$("input[name='use-coupon-code']").val($('#contactOrderCoupons').val());
					$("input[name='use-coupon-money']").val(coupon_config.money);
				} else {
					//更新提醒
					$parent.append('<label for="contactOrderCoupons" class="coupon-error">' + data.message + '</label>');
					$("input[name='use-coupon-code']").val("");
				}

				//记录上一次查询的优惠券，防止重复
				coupon_config.last_query_code = $('#contactOrderCoupons').val();
				coupon_config.last_query_message = data.message;

				__self.html('<i class="uicon-increase"></i> 使用优惠券').removeClass('loading').removeAttr('disabled');
			});
		} else if ($couponCode.length != 8) {
			$parent.append('<label for="contactOrderCoupons" class="coupon-error">请输入8位的优惠券密码</label>');
		} else if (coupon_config.last_query_code == $couponCode) {
			$("input[name='use-coupon-money']").val(coupon_config.money);
			$(".input-group-coupon").addClass('hidden');
			$("input[name='use-coupon-code']").val(coupon_config.last_query_code);
			$parent.append('<label for="contactOrderCoupons" class="coupon-error use-coupon-success">' + coupon_config.last_query_message + ' <button type="button" class="btn btn-xs btn-danger cancel-use-coupon-btn">取消使用</button></label>');
		}
	});

	//取消使用优惠券
	$("#three").on('click', '.cancel-use-coupon-btn', function() {
		$("input[name='use-coupon-code']").val("");
		var $parent = $(this).parent();
		$parent.remove();
		$(".input-group-coupon").removeClass('hidden');
		$(".members-choose-coupons").val("");
	});

	//会员选择优惠券
	$(".members-choose-coupons").change(function() {
		__self = $(this);
		if (__self.val() != "") {
			$("#contactOrderCoupons").val(__self.val());
			$('.cancel-use-coupon-btn').trigger('click');
			$('.user-coupon-btn').trigger('click');
		}
	});
})

/*//同步优惠券
function is_coupons_list() {
	$couponCode = $("#contactOrderCoupons");
	$('input:radio[name=coupons-item]').each(function(index, el) {
		if ($couponCode.val().toLowerCase() == $(el).val().toLowerCase()) {
			$(el).attr('checked', true);
			return false;
		};
	});
	if ($('input:radio[name=coupons-item]:checked').val() == "") {
		$(".user-coupon-btn").parent().find(".formtips").remove();
	};
}*/

$.fn.stringify = function() {
	return JSON.stringify(this);
}

function otherSelect($this, $showtext) {
	var $parent = $this.parent();
	if ($this.val() == "-1") {
		$parent.addClass('showhidden');
		$showtext.removeClass('hidden');
	} else {
		$parent.removeClass('showhidden');
		$showtext.addClass('hidden').removeClass('error').next().remove();
	}
}
