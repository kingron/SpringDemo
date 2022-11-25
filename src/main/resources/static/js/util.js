/**
/* 对Date的扩展，将 Date 转化为指定格式的String
/* 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
/* 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
/* 例子：
/* (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2019-01-02 10:19:04.423
/* (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2019-1-2 10:19:4.18
*/
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }

    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
}

/*
    给字符串增加 format 函数
*/
String.format = function(src){
	if (arguments.length === 0) return null;
	var args = Array.prototype.slice.call(arguments, 1);
	return src.replace(/\{(\d+)\}/g, function(m, i){
		return args[i];
	});
};

/**
    类似安卓 Toast 提示效果
*/
function Toast(msg, duration) {
    duration = isNaN(duration) ? 5000 : duration;
    var m = document.createElement('div');
    m.innerHTML = msg.replaceAll("\n", "<br/>");
    m.style.cssText = "min-width: 150px;opacity: 0.9;height:auto;color: rgb(255, 255, 255);text-align: center;border-radius: 8px;position: absolute;top: 50%;left: 50%;transform: translate(-50%, -50%);z-index: 999;background: rgb(0, 0, 0);font-size: 14px;padding: 20px 20px 20px 20px";
    document.body.appendChild(m);
    setTimeout(function () {
        var d = 0.5;
        m.style.webkitTransition = '-webkit-transform ' + d + 's ease-in, opacity ' + d + 's ease-in';
        m.style.opacity = '0';
        setTimeout(function () {
            document.body.removeChild(m)
        }, d * 1000);
    }, duration);
}

function tableDelete(table, from, to) {
    to = isNaN(to) || to == null ? table.rows.length - 1 : to;
    for (var i = to; i >= from; i--) {
        table.deleteRow(i);
    }
}

function tableAddRow(table, ...rest) {
    var row = table.insertRow();
    rest.forEach((item) => {
        row.insertCell().textContent = item;
    });
    return row;
}