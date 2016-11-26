// main.js

// patch:
if (! window.console) {
    window.console = {
        log: function() {},
        info: function() {},
        error: function () {},
        warn: function () {},
        debug: function () {}
    };
}

if (! String.prototype.trim) {
    String.prototype.trim = function() {
        return this.replace(/^\s+|\s+$/g, '');
    };
}

function showSignin(refreshAfterSignIn) {
    var modal = UIkit.modal('#modal-signin', {
        bgclose: false,
        center: true
    });
    window.refreshAfterSignIn = refreshAfterSignIn;
    modal.show();
}

function encodeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

// parse query string as object:

function parseQueryString() {
    var
        q = location.search,
        r = {},
        i, pos, s, qs;
    if (q && q.charAt(0)==='?') {
        qs = q.substring(1).split('&');
        for (i=0; i<qs.length; i++) {
            s = qs[i];
            pos = s.indexOf('=');
            if (pos <= 0) {
                continue;
            }
            r[s.substring(0, pos)] = decodeURIComponent(s.substring(pos+1)).replace(/\+/g, ' ');
        }
    }
    return r;
}

function gotoPage(i) {
    var r = parseQueryString();
    r.page = i;
    location.assign('?' + $.param(r));
}

function toSmartDate(timestamp) {
    if (typeof(timestamp)==='string') {
        timestamp = parseInt(timestamp);
    }
    if (isNaN(timestamp)) {
        return '';
    }

    var
        today = new Date(g_time),
        now = today.getTime(),
        s = '1分钟前',
        t = now - timestamp;
    if (t > 604800000) {
        // 1 week ago:
        var that = new Date(timestamp);
        var
            y = that.getFullYear(),
            m = that.getMonth() + 1,
            d = that.getDate(),
            hh = that.getHours(),
            mm = that.getMinutes();
        s = y===today.getFullYear() ? '' : y + '年';
        s = s + m + '月' + d + '日' + hh + ':' + (mm < 10 ? '0' : '') + mm;
    }
    else if (t >= 86400000) {
        // 1-6 days ago:
        s = Math.floor(t / 86400000) + '天前';
    }
    else if (t >= 3600000) {
        // 1-23 hours ago:
        s = Math.floor(t / 3600000) + '小时前';
    }
    else if (t >= 60000) {
        s = Math.floor(t / 60000) + '分钟前';
    }
    return s;
}

$(function() {
    $('.x-smartdate').each(function() {
        $(this).removeClass('x-smartdate').text(toSmartDate($(this).attr('date')));
    });
});

$.postJSON = function (url, data, callback) {
	$.ajax({
		type: 'POST',
		url: url,
		data: JSON.stringify(data || {}),
		contentType: 'application/json'
	}).done(function (r) {
		if (r && r.error) {
			return callback(r);
		}
		return callback(null, r);
	}).fail(function (jqXHR, textStatus) {
		var err = {
			error: 'http_bad_response',
			field: '' + jqXHR.status,
			message: '网络好像出问题了 (HTTP ' + jqXHR.status + ')'
		};
		try {
			err = JSON.parse(jqXHR.responseText);
		} catch (e) {
			console.log(e);
		}
		return callback(err);
	});
};

// JS Template:

function Template(tpl) {
    var
        fn,
        match,
        code = ['var r=[];\nvar _html = function (str) { return str.replace(/&/g, \'&amp;\').replace(/"/g, \'&quot;\').replace(/\'/g, \'&#39;\').replace(/</g, \'&lt;\').replace(/>/g, \'&gt;\'); };'],
        re = /\{\s*([a-zA-Z\.\_0-9()]+)(\s*\|\s*safe)?\s*\}/m,
        addLine = function (text) {
            code.push('r.push(\'' + text.replace(/\'/g, '\\\'').replace(/\n/g, '\\n').replace(/\r/g, '\\r') + '\');');
        };
    while (match = re.exec(tpl)) {
        if (match.index > 0) {
            addLine(tpl.slice(0, match.index));
        }
        if (match[2]) {
            code.push('r.push(String(this.' + match[1] + '));');
        }
        else {
            code.push('r.push(_html(String(this.' + match[1] + ')));');
        }
        tpl = tpl.substring(match.index + match[0].length);
    }
    addLine(tpl);
    code.push('return r.join(\'\');');
    fn = new Function(code.join('\n'));
    this.render = function (model) {
        return fn.apply(model);
    };
}

// signin with oauth:

var isDesktop = (function() {
    var ua = navigator.userAgent.toLowerCase();
    return ua.indexOf('windows nt')>=0 || ua.indexOf('macintosh')>=0;
})();

window.onAuthCallback = function (user) {
    var modal = $.UIkit.modal('#modal-signin');
    modal.hide();
    g_user = {
        id: user.id,
        name: user.name,
        image: user.image_url
    };
    // update user info:
    $('.x-user-name').text(g_user.name);
    // update css:
    $('#x-doc-style').text('.x-display-if-signin {}\n.x-display-if-not-signin { display: none; }');
    // reload if neccessary:
    if (window.refreshAfterSignIn) {
    	location.reload();
    }
};

function authFrom(provider) {
    var
        url = '/auth/from/' + provider,
        popupId = 'webacademy_auth_id_window';
    if (isDesktop) {
        var w = window.open(url + '?jscallback=onAuthCallback', popupId, 'top=200,left=400,width=720,height=400,directories=no,menubar=no,toolbar=no,resizable=no');
        w.focus();
    }
    else {
        location.assign(url);
    }
}

// register custom filters for Vue:

if (typeof(Vue)!=='undefined') {
    Vue.filter('datetime', function (value) {
    	if (!value) {
    		return '';
    	}
        var d = value;
        if (typeof(value)==='number') {
            d = new Date(value);
        }
        if (d.getFullYear && d.getMonth && d.getDate) {
            return d.getFullYear() + '-' + (d.getMonth() + 1) + '-' + d.getDate() + ' ' + d.getHours() + ':' + d.getMinutes();
        }
        return '' + d;
    });
    Vue.filter('date', function (value) {
    	if (!value) {
    		return '';
    	}
        var d = value;
        if (typeof(value)==='number') {
            d = new Date(value);
        }
        if (d.getFullYear && d.getMonth && d.getDate) {
            return d.getFullYear() + '-' + (d.getMonth() + 1) + '-' + d.getDate();
        }
        return '' + d;
    });
    Vue.filter('duration', function (value) {
    	var m = Math.floor(value / 60);
    	var s = Math.round(value % 60);
    	if (m === 0) {
    		return s + '\'';
    	}
    	return m + '\'' + s + '\'';
    });
    Vue.filter('abbr', function (value) {
    	if (value && value.length && value.length > 10) {
    		return value.substring(0, 10) + '...';
    	}
    	return value;
    });
    Vue.filter('shortEmail', function (value) {
        if (value && value.length && value.length > 30) {
            return '...' + value.substring(value.length - 25);
        }
        return value;
    });
    Vue.filter('shortUrl', function (value) {
        if (value && value.length && value.length > 30) {
            var n = value.indexOf('/', 8);
            if (n != (-1)) {
                return value.substring(0, n+1) + '...' + value.substring(value.length - 10);
            }
            return value.substring(value.length - 25) + '...';
        }
        return value;
    });
}

function createPagination(dom, page) {
	var s = ['<ul class="uk-pagination">'];
	if (page.totalItems === 0) {
		s.push('<li><span>No items available</span></li>');
	} else {
		// show 1, 2, 3:
		var index = [1, 2, 3];
		// show index:
		index.push(page.pageIndex - 2);
		index.push(page.pageIndex - 1);
		index.push(page.pageIndex);
		index.push(page.pageIndex + 1);
		index.push(page.pageIndex + 2);
		// show last 98, 99, 100:
		index.push(page.totalPages - 2);
		index.push(page.totalPages - 1);
		index.push(page.totalPages);
		index = _.filter(_.uniq(index), function (num) {
			return num >= 1 && num <= page.totalPages;
		});
		if (page.pageIndex >= 7) {
			index.splice(3, 0, '...');
		}
		if ((page.totalPages - page.pageIndex) > 5) {
			index.splice(index.length - 3, 0, '...');
		}
		_.each(index, function (num) {
			if (num === '...') {
				s.push('<li><span>...</span></li>');
			}
			else if (num === page.pageIndex) {
				s.push('<li class="uk-active"><span>' + num + '</span></li>');
			}
			else {
				s.push('<li><a href="#0" onclick="gotoPage(' + num + ')">' + num + '</a></li>');
			}
		});
	}
	s.push('</ul>');
	$(dom).html(s.join(''));
}

//********************************************************************************

function redirect(url) {
    var
        hash_pos = url.indexOf('#'),
        query_pos = url.indexOf('?'),
        hash = '';
    if (hash_pos >=0 ) {
        hash = url.substring(hash_pos);
        url = url.substring(0, hash_pos);
    }
    url = url + (query_pos >= 0 ? '&' : '?') + 't=' + new Date().getTime() + hash;
    console.log('redirect to: ' + url);
    location.assign(url);
}
