function action(mode, type, selection) {
	if (cm.getNpc() >= 9901000) {
            cm.sendNext("Hello #h0#, I am in the Hall of Fame for reaching LEVEL 200.");
	} else {
			cm.sendOk("I am currently not coded.\r\nMy ID is: " + cm.getNpc() + "\r\n\r\nPlease contact an Admin!")
            cm.dispose();
	}
}