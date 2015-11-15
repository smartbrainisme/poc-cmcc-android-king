//չ������Ч��
function show(e, state) {
	if (state == 1) {
		// ƽ��չ��
		$(e).animate({
			height : "320px"
		}, 600);
		// �л�����
		$(e).css("background-image", "url(img/box_bg_open_lager.png)");
		// �л���ͷ����
		$(e).find(".arrow").attr("src", "img/iconfont-zuo copy.png");
		// ���¼���2���open
		$(e).attr("onclick", "show(this,2)");
		// ��ʾ����
		$(e).find(".intro").fadeIn(500);

	} else {
		$(e).animate({
			height : "116px"
		}, 800, function() {
			$(e).css("background-image", "url(img/box_bg_little.png)");
			$(e).find(".arrow").attr("src", "img/iconfont-zuo copy 2.png");
			$(e).attr("onclick", "show(this,1)");

		});
		$(e).find(".intro").fadeOut(500);
	}
}