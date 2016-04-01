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

// markdown to html:

var safeRenderer = new marked.Renderer();

safeRenderer.link = function (href, title, text) {
    if (href.indexOf('http://') !== 0 && href.indexOf('https://') !== 0) {
        href = 'http://' + href;
    }
    return '<a target="_blank" rel="nofollow" href="' + href + '">' + text + '</a>';
};

function md2html(md) {
    return marked(md, {
        sanitize: true,
        renderer: safeRenderer
    });
}

function html2md(h) {
    var cleanCode = function (s) {
        var c1 = s.indexOf('<code>');
        if (c1 === -1) {
            return s;
        }
        var c2 = s.indexOf('</code>', c1);
        if (c2 === -1) {
            return s;
        }
        var code = s.substring(c1, c2 + 7);
        if (code.indexOf('<p>') === -1) {
            return s;
        }
        code = code.replace(/\<p\>/g, '').replace(/\<\/p\>/g, '\n');
        var part1 = s.substring(0, c1);
        var part2 = s.substring(c2 + 7);
        return part1 + '<pre>' + code + '</pre>' + part2;
    }

    var old = h;
    for (;;) {
        h = cleanCode(h);
        if (old === h) {
            break;
        }
        old = h;
    }
    var r = toMarkdown(h, {
        gfm: true
    });
    return r;
}

$(function () {
    $('div.x-markdown').each(function () {
        var div = $(this);
        div.html(md2html(div.text()));
        div.removeClass('x-markdown');
    });
});

// create player:

function createVideoPlayer(videoId, onEnd) {
    var player = videojs(videoId, {
        // options
        controlBar: {
            children: [
                'playToggle',
                'progressControl',
                'volumeMenuButton',
                'fullscreenToggle'
            ],
            volumeMenuButton: { 
                inline: false
            }
        }
    }, function() {
        // on ready
        onEnd && onEnd();
    });
}

// to human readable size:

function size2string(value) {
    if (value < 1024) {
        return value + ' bytes';
    }
    value = value / 1024;
    if (value < 1024) {
        return value.toFixed(2) + ' KB';
    }
    value = value / 1024;
    if (value < 1024) {
        return value.toFixed(2) + ' MB';
    }
    value = value / 1024;
    return value.toFixed(2) + ' GB';
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

$(function () {
    $('.x-button-weibo').click(function () {
        authFrom('weibo');
    });
    $('.x-button-qq').click(function () {
        authFrom('qq');
    });
});

// extends jQuery.form:

$(function () {
    console.log('Extends $form...');
    $.fn.extend({
        showFormError: function (err) {
            return this.each(function () {
                var
                    $form = $(this),
                    $alert = $form && $form.find('.uk-alert-danger'),
                    fieldName = err && err.data;
                if (! $form.is('form')) {
                    console.error('Cannot call showFormError() on non-form object.');
                    return;
                }
                $form.find('input').removeClass('uk-form-danger');
                $form.find('select').removeClass('uk-form-danger');
                $form.find('textarea').removeClass('uk-form-danger');
                if ($alert.length === 0) {
                    console.warn('Cannot find .uk-alert-danger element.');
                    return;
                }
                if (err) {
                    $alert.text(err.message ? err.message : (err.error ? err.error : err)).removeClass('uk-hidden').show();
                    if (($alert.offset().top - 60) < $(window).scrollTop()) {
                        $('html,body').animate({ scrollTop: $alert.offset().top - 60 });
                    }
                    if (fieldName) {
                        $form.find('[name=' + fieldName + ']').addClass('uk-form-danger');
                    }
                }
                else {
                    $alert.addClass('uk-hidden').hide();
                    $form.find('.uk-form-danger').removeClass('uk-form-danger');
                }
            });
        },
        showFormLoading: function (isLoading) {
            return this.each(function () {
                var
                    $form = $(this),
                    $submit = $form && $form.find('button[type=submit]'),
                    $buttons = $form && $form.find('button');
                    $i = $submit && $submit.find('i'),
                    iconClass = $i && $i.attr('class');
                if (! $form.is('form')) {
                    console.error('Cannot call showFormLoading() on non-form object.');
                    return;
                }
                if (!iconClass || iconClass.indexOf('uk-icon') < 0) {
                    console.warn('Icon <i class="uk-icon-*>" not found.');
                    return;
                }
                if (isLoading) {
                    $buttons.attr('disabled', 'disabled');
                    $i && $i.addClass('uk-icon-spinner').addClass('uk-icon-spin');
                }
                else {
                    $buttons.removeAttr('disabled');
                    $i && $i.removeClass('uk-icon-spinner').removeClass('uk-icon-spin');
                }
            });
        },
        postJSON: function (url, data, callback) {
            if (arguments.length===2) {
                callback = data;
                data = {};
            }
            console.log('FData: ' + data);
            return this.each(function () {
                var $form = $(this);
                if (! $form.is('form')) {
                    console.error('Cannot call postJSON() on non-form object.');
                    return;
                }
                $form.showFormError();
                $form.showFormLoading(true);
                _httpJSON('POST', url, data, function (err, r) {
                    if (err) {
                        $form.showFormError(err);
                        $form.showFormLoading(false);
                    }
                    callback && callback(err, r);
                });
            });
        }
    });
});

// ajax submit form:

function _httpJSON(method, url, data, callback) {
    var opt = {
        type: method,
        dataType: 'json'
    };
    if (method==='GET') {
        opt.url = url + '?' + data;
    }
    if (method==='POST') {
        opt.url = url;
        opt.data = JSON.stringify(data || {});
        opt.contentType = 'application/json';
        console.log('Data: ' + opt.data);
    }
    console.log(method + ' ' + url);
    $.ajax(opt).done(function (r) {
        if (r && r.error) {
            return callback(r);
        }
        return callback(null, r);
    }).fail(function (jqXHR, textStatus) {
        return callback({'error': 'http_bad_response', 'data': '' + jqXHR.status, 'message': '网络好像出问题了 (HTTP ' + jqXHR.status + ')'});
    });
}

function getJSON(url, data, callback) {
    if (arguments.length===2) {
        callback = data;
        data = {};
    }
    if (typeof (data)==='object') {
        var arr = [];
        $.each(data, function (k, v) {
            arr.push(k + '=' + encodeURIComponent(v));
        });
        data = arr.join('&');
    }
    _httpJSON('GET', url, data, callback);
}

function postJSON(url, data, callback) {
    if (arguments.length===2) {
        callback = data;
        data = {};
    }
    _httpJSON('POST', url, data, callback);
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
    Vue.filter('size', size2string);
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

// init:

function _bindSubmit($form) {
    $form.submit(function (event) {
        event.preventDefault();
        $form.showFormError(null);
        var
            fn_error = $form.attr('fn-error'),
            fn_success = $form.attr('fn-success'),
            fn_data = $form.attr('fn-data'),
            data = fn_data ? window[fn_data]($form) : $form.serialize();
        var
            $submit = $form.find('button[type=submit]'),
            $i = $submit.find('i'),
            iconClass = $i.attr('class');
        if (!iconClass || iconClass.indexOf('uk-icon') < 0) {
            $i = undefined;
        }
        $submit.attr('disabled', 'disabled');
        $i && $i.addClass('uk-icon-spinner').addClass('uk-icon-spin');
        postJSON($form.attr('action-url'), data, function (err, result) {
            $i && $i.removeClass('uk-icon-spinner').removeClass('uk-icon-spin');
            if (err) {
                console.log('postJSON failed: ' + JSON.stringify(err));
                $submit.removeAttr('disabled');
                fn_error ? fn_error() : $form.showFormError(err);
            }
            else {
                var r = fn_success ? window[fn_success](result) : false;
                if (r===false) {
                    $submit.removeAttr('disabled');
                }
            }
        });
    });
    $form.find('button[type=submit]').removeAttr('disabled');
}

$(function () {
    $('form').each(function () {
        var $form = $(this);
        if ($form.attr('action-url')) {
            _bindSubmit($form);
        }
    });

    $('a.x-status-locked').click(function () {
        var modal = $.UIkit.modal('#modal-pay');
        modal.show();
    });

});
