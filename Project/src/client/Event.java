package client;

public interface Event {
	void onClientConnect(String clientName, String message);

	void onClientDisconnect(String clientName, String message);

	void onMessageReceive(String clientName, String message);

	void onChangeRoom();

//In Event we get our onMute and onUnmute methods ready to be implemented in ClientUI
	void onMute(String clientName);

	void onUnmute(String clientName);

//In Event, onSave takes to the class where it is implemented, which is ClientUI
	void onSave();
}