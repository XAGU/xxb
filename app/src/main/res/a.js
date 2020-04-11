if($("body > div > p > span").text() == "分"){
	$("body > div > div > p").after("<span class='BlueBtn' onclick='rework()'>打回重做</span>");   
	function rework() {
		var values = redo.toString().match(/(?<=")\d*?(?=")/g);
		var workRelationId = values[0];
		var classId = values[1];
		var relationAnswerId = values[2];
		var courseId = values[3];
		var studentId = "44399444";
		$(".cx_alert-txt").html("确认要打回作业吗?");
		$("#okBtn").html("打回");
		$(".cx_alert").css("display", "block");
		$(".cx_alert-box").css("display", "block");
		$("#okBtn").unbind();
		$("#cancelBtn").unbind();
		$("#okBtn").on("click", function() {
			$.ajax({
				type : "get",
				url : "/work/phone/reWork",
				dataType : "json",
				data : {
					"workRelationId" : workRelationId,
					"classId" : classId,
					"courseId" : courseId,
					"relationAnswerId" : relationAnswerId,
					"studentId" : studentId
				},
				success : function(data) {
					if(data.status == true) {
						$(".cx_alert").css("display", "none");
						$(".cx_alert-box").css("display", "none");
						jsBridge.postNotification('CLIENT_REWORK_SUCCESS', data.msg);
					} else {
					$(".cx_alert").css("display", "none");
						$(".cx_alert-box").css("display", "none");
						openWindowHintClient(1, data.msg, null, 1000);
					}
				}
			});
		});
		
		$("#cancelBtn").on("click", function() {
			$(".cx_alert").css("display", "none");
			$(".cx_alert-box").css("display", "none");
		});
	}
}
if($("body > div.startBtn > input[type=button]").attr("value")=="继续答题"){
	$("body > div.startBtn > input[type=button]").before("<span class='BlueBtn' onclick='rework()'>延长时间</span><br>");
	(function($){
		$.getUrlParam = function(name){
			var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
			var r = window.location.search.substr(1).match(reg);
			if (r!=null) return unescape(r[2]); return null;
		}
	})(jQuery);	    
    var script = document.createElement('script');
    script.src = "/js/work/phone/iosSelectRem.js?v=2018-0704-1618";
    document.getElementsByTagName('head')[0].appendChild(script);


    let linkElm = document.createElement('link');
    linkElm.setAttribute('rel', 'stylesheet');
    linkElm.setAttribute('type', 'text/css');
    linkElm.setAttribute('href', '/css/work/phone/iosSelectRem.css?v=2018-0704-1618');
    document.head.appendChild(linkElm);



    function chooseDone() {
    	var checkBoxList = $(".checked");
    	showTime();

    }
    debugger;
    // 初始化时间
    var now = new Date();
    var nowYear = now.getFullYear();
    var nowMonth = now.getMonth() + 1;
    var nowDate = now.getDate();
    var nowHour = now.getHours();
    var nowMinute = now.getMinutes();
    // 数据初始化
    function formatYear (nowYear) {
    	var arr = [];
        for (var i = nowYear; i <= nowYear + 10; i++) {
    		arr.push({
    			id: i + '',
    			value: i + '年'
    		});
    	}
    	return arr;
    }
    function formatMonth () {
    	var arr = [];
    	for (var i = 1; i <= 12; i++) {
    		arr.push({
    			id: i + '',
    			value: i + '月'
    		});
    	}
    	return arr;
    }
    function formatDate (count) {
    	var arr = [];
    	for (var i = 1; i <= count; i++) {
    		arr.push({
    			id: i + '',
    			value: i + '日'
    		});
    	}
    	return arr;
    }
    var yearData = function(callback) {
    	callback(formatYear(nowYear))
    }
    var monthData = function (year, callback) {
    	callback(formatMonth());
    };
    var dateData = function (year, month, callback) {
    	if (/^(1|3|5|7|8|10|12)$/.test(month)) {
    		callback(formatDate(31));
    	}
    	else if (/^(4|6|9|11)$/.test(month)) {
    		callback(formatDate(30));
    	}
    	else if (/^2$/.test(month)) {
    		if (year % 4 === 0 && year % 100 !==0 || year % 400 === 0) {
    			callback(formatDate(29));
    		}
    		else {
    			callback(formatDate(28));
    		}
    	}
    	else {
    		console.log(month);
    		throw new Error('month is illegal');
    	}
    };
    var hourData = function(one, two, three, callback) {
    	var hours = [];
    	for (var i = 0,len = 24; i < len; i++) {
    		hours.push({
    			id: i,
    			value: i + '时'
    		});
    	}
    	callback(hours);
    };
    var minuteData = function(one, two, three, four, callback) {
    	var minutes = [];
    	for (var i = 0, len = 60; i < len; i++) {
    		minutes.push({
    			id: i,
    			value: i + '分'
    		});
    	}
    	callback(minutes);
    };

    var myendtime = '';

    function showTime() {
    	var oneLevelId = nowYear;
    	var twoLevelId = nowMonth;
    	var threeLevelId = nowDate;
    	var fourLevelId = nowHour;
    	var fiveLevelId = nowMinute;
    	var iosSelect = new IosSelect(5, [yearData, monthData, dateData, hourData, minuteData], {
    		title : '加时',
    		itemHeight : 0.6786,
    		headerHeight : 0.819,
    		relation : [1, 1, 0, 0],
    		itemShowCount : 7,
    		cssUnit : 'rem',
    		oneLevelId : oneLevelId,
    		twoLevelId : twoLevelId,
    		threeLevelId : threeLevelId,
    		fourLevelId : fourLevelId,
    		fiveLevelId : fiveLevelId,
    		callback : function(selectOneObj, selectTwoObj, selectThreeObj, selectFourObj, selectFiveObj) {
    			nowYear = selectOneObj.id;
    			nowMonth = selectTwoObj.id;
    			nowDate = selectThreeObj.id;
    			nowHour = selectFourObj.id;
    			nowMinute = selectFiveObj.id;

    			var yt = selectOneObj.id;
    			var Mt = selectTwoObj.id;
    			Mt = addZero(Mt);

    			var dt = selectThreeObj.id;
    			dt = addZero(dt);

    			var Ht = selectFourObj.id;
    			Ht = addZero(Ht);

    			var mt = selectFiveObj.id;
    			mt = addZero(mt);

    			myendtime = yt + "-" + Mt + "-" + dt + " " + Ht + ":" + mt;
    			addTime();
    		}
    	});
    }

    function addtime() {
    	var extraTime = myendtime;
    	if (extraTime.length == 0) {
    		openWindowHintClient(1, "请设置加时时间!", null, 1000);
    		return false;
    	}
    	extraTime = extraTime + ":00";

    	var nowTime = getCurrentTime();
    	if(nowTime >= extraTime) {
    		openWindowHintClient(1, "加时需大于当前时间！", null, 1000);
    		//showTime();
    		return false;
    	}

    	var endTime = $("#endTime").val();
    	if(endTime >= extraTime) {
    		openWindowHintClient(1, "加时需大于截止时间！", null, 1000);
    		//showTime();
    		return false;
    	}


    	$.ajax({
    		type : "get",
    		url : "/work/add-time",
    		dataType : "json",
    		data : {
    			"ids" : ids,
    			"time" : extraTime,
    			"workId" : workId,
    			"classId" : classId,
    			"courseId" : courseId
    		},
    		success : function(data) {
    			openWindowHintClient(1, data.msg, function() {
    				jsBridge.postNotification("CLIENT_REFRESH_STATUS", {
    					"status" : 1
    				});
    				jsBridge.postNotification('CLIENT_EXIT_LEVEL', {
    					message : ''
    				});
    			}, 1000);
    		}
    	});
    }

    function addZero(i) {
    	if (i < 10) {
    		i = "0" + i;
    	}
    	return i;
    }

    function getCurrentTime() {
    	var now = new Date();
    	var year = now.getFullYear();
    	var month = now.getMonth() + 1;
    	month = addZero(month);
    	var date = now.getDate();
    	date = addZero(date);
        var hour = now.getHours();
        hour = addZero(hour);
        var minute = now.getMinutes();
        minute = addZero(minute);
        var time = year + "-" + month + "-" + date + " " + hour + ":" + minute + ":00";
    	return time;
    }
    function addTime() {
    	var extraTime = myendtime;
    	if (extraTime.length == 0) {
    		openWindowHintClient(1, "请设置加时时间!", null, 1000);
    		return false;
    	}
    	extraTime = extraTime + ":00";

    	var nowTime = getCurrentTime();
    	if(nowTime >= extraTime) {
    		openWindowHintClient(1, "加时需大于当前时间！", null, 1000);
    		//showTime();
    		return false;
    	}

    	var endTime = $("#endTime").val();
    	if(endTime >= extraTime) {
    		openWindowHintClient(1, "加时需大于截止时间！", null, 1000);
    		//showTime();
    		return false;
    	}

    	var workId = $.getUrlParam('taskrefId');
    	var ids = $.getUrlParam('cpi');
    	var classId = $.getUrlParam('classId');
    	var courseId = $.getUrlParam('courseId');
    	$(".cx_alert-txt").html("确认要延长作业时间吗?");
    	$("#okBtn").html("延长");
    	$(".cx_alert").css("display", "block");
    	$(".cx_alert-box").css("display", "block");
    	$("#okBtn").unbind();
    	$("#cancelBtn").unbind();
    	$("#okBtn").on("click", function() {
    		$.ajax({
    			type : "get",
    			url : "/work/add-time",
    			dataType : "json",
    			data : {
    				"ids" : ids,
    				"time" : extraTime,
    				"workId" : workId,
    				"classId" : classId,
    				"courseId" : courseId
    			},
    			success : function(data) {
    				if(data.status == true) {
    					$(".cx_alert").css("display", "none");
    					$(".cx_alert-box").css("display", "none");
    					jsBridge.postNotification('CLIENT_REWORK_SUCCESS', data.msg);
    				} else {
    				$(".cx_alert").css("display", "none");
    					$(".cx_alert-box").css("display", "none");
    					openWindowHintClient(1, data.msg, null, 1000);
    				}
    			}
    		});
    	});

    	$("#cancelBtn").on("click", function() {
    		$(".cx_alert").css("display", "none");
    		$(".cx_alert-box").css("display", "none");
    	});
    }
}