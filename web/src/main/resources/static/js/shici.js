// shici.js

function printPoem() {
	var
		w = window.open('about:blank', 'printPoemWindow', 'width=640,height=480,scrollbars=1'),
		scr = '<scr' + 'ipt>\n setTimeout(function () { window.print() }, 500) \n</scr' + 'ipt>';
		pre = '<!DOCTYPE html>\n<html>\n<head>\n<meta charset="utf-8">\n<meta name="viewport" content="width=device-width, initial-scale=1"><title>{{ poem.name }}</title><link rel="stylesheet" href="/static/css/shici.css" />\n' + scr + '\n</head>\n<body style="margin: 25px">\n',
		post = '</body>\n</html>',
		html = pre + $('#poem').html() + post;
	w.document.open();
	w.document.write(html);
	w.document.close();
}

function createQRCode(poemId) {
	var qrcode = new QRCode("qr-link", {
        text: location.protocol + '//' + location.host + '/poem/' + poemId,
        width: 128,
        height: 128,
        colorDark : "#f42334",
        colorLight : "#ffffff",
        correctLevel : QRCode.CorrectLevel.L
	});
}
