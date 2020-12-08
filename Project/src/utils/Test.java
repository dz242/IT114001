package utils;

public class Test {

	public static void main(String[] args) {
		String message = "This word is __bold__";
		String workMessage = message;

		if (workMessage.indexOf("@@") > -1) {
			String[] splMessage = workMessage.split("@@");
			String splPhrase = "";
			splPhrase += splMessage[0];
			for (int x = 1; x < splMessage.length; x++) {
				if (x % 2 == 0) {
					splPhrase += splMessage[x];
				} else {
					splPhrase += "<b>" + splMessage[x] + "</b>";
				}
			}
			workMessage = splPhrase;
		}
		if (workMessage.indexOf("//") > -1) {
			String[] splMessage = workMessage.split("//");
			String splPhrase = "";
			splPhrase += splMessage[0];
			for (int x = 1; x < splMessage.length; x++) {
				if (x % 2 == 0) {
					splPhrase += splMessage[x];
				} else {
					splPhrase += "<i>" + splMessage[x] + "</i>";
				}
			}
			workMessage = splPhrase;
		}
		if (workMessage.indexOf("__") > -1) {
			String[] splMessage = workMessage.split("__");
			String splPhrase = "";
			splPhrase += splMessage[0];
			for (int x = 1; x < splMessage.length; x++) {
				if (x % 2 == 0) {
					splPhrase += splMessage[x];
				} else {
					splPhrase += "<u>" + splMessage[x] + "</u>";
				}
			}
			workMessage = splPhrase;
		}
		if (workMessage.indexOf(";r;") > -1) {
			String[] splMessage = workMessage.split(";r;");
			String splPhrase = "";
			splPhrase += splMessage[0];
			for (int x = 1; x < splMessage.length; x++) {
				if (x % 2 == 0) {
					splPhrase += splMessage[x];
				} else {
					splPhrase += "<font color=red>" + splMessage[x] + "</font>";
				}
			}
			workMessage = splPhrase;
		}
		message = workMessage;

		System.out.println(message);
	}
}
