package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Room implements AutoCloseable {
	private static SocketServer server;// used to refer to accessible server functions
	private String name;
	private final static Logger log = Logger.getLogger(Room.class.getName());
	Random ran = new Random();
	private boolean isDM = false;
	public String receiverName = null;
	private static client.Event event;
	// Commands
	private final static String COMMAND_TRIGGER = "/";
	private final static String CREATE_ROOM = "createroom";
	private final static String JOIN_ROOM = "joinroom";
	private final static String ROLL = "roll";
	private final static String FLIP = "flip";
	private final static String MUTE = "mute";
	private final static String UNMUTE = "unmute";

	public Room(String name) {
		this.name = name;
	}

	public static void setServer(SocketServer server) {
		Room.server = server;
	}

	public String getName() {
		return name;
	}

	private List<ServerThread> clients = new ArrayList<ServerThread>();
	private List<ServerThread> DMclients = new ArrayList<ServerThread>();

	protected synchronized void addClient(ServerThread client) {
		client.setCurrentRoom(this);
		if (clients.indexOf(client) > -1) {
			log.log(Level.INFO, "Attempting to add a client that already exists");
		} else {
			clients.add(client);
			if (client.getClientName() != null) {
				client.sendClearList();
				sendConnectionStatus(client, true, "joined the room " + getName());
				updateClientList(client);
			}
		}
	}

	private void updateClientList(ServerThread client) {
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread c = iter.next();
			if (c != client) {
				boolean messageSent = client.sendConnectionStatus(c.getClientName(), true, null);
			}
		}
	}

	protected synchronized void removeClient(ServerThread client) {
		clients.remove(client);
		if (clients.size() > 0) {
			// sendMessage(client, "left the room");
			sendConnectionStatus(client, false, "left the room " + getName());
		} else {
			cleanupEmptyRoom();
		}
	}

	protected synchronized void diceRoll(ServerThread client) {
		int diceVal = ran.nextInt(6) + 1;
		String res = String.valueOf(diceVal);
		client.send(client.getClientName(), "<font color=green>has rolled the dice... and got a " + res + "!</font>");
	}

	protected synchronized void coinFlip(ServerThread client) {
		String res = "";
		int coinVal = ran.nextInt(2) + 1;
		if (coinVal == 1) {
			res = "heads";
		} else if (coinVal == 2) {
			res = "tails";
		}

		client.send(client.getClientName(), "<font color=blue>has flipped the coin... and got " + res + "!</font>");
	}

	protected synchronized boolean DMCheck(String cname) {
		if (receiverName == null) {
			return false;
		}
		for (ServerThread client : clients) {
			if (client.getClientName().equals(cname)) {
				DMclients.add(client);
				break;
			}
			isDM = true;
		}
		return isDM;
	}

	protected synchronized void muter(String name, ServerThread sender) {
		for (ServerThread client : clients) {
			if (!sender.isMuted(name)) {
				if (client.getClientName().equals(name)) {
					sender.mutedClients.add(client.getClientName());
					boolean messageSent = client.send(sender.getClientName(), sender.getClientName() + " muted you...");
					sender.sendMute(client.getClientName());
					break;
				}
			}
		}
		return;
	}

	protected synchronized void unmuter(String name, ServerThread sender) {
		for (ServerThread client : clients) {
			if (sender.isMuted(name)) {
				if (client.getClientName().equals(name)) {
					sender.mutedClients.remove(client.getClientName());
					boolean messageSent = client.send(sender.getClientName(),
							sender.getClientName() + " unmuted you...");
					sender.sendUnmute(client.getClientName());
					break;
				}
			}
		}
		return;
	}

	private void cleanupEmptyRoom() {
		// If name is null it's already been closed. And don't close the Lobby
		if (name == null || name.equalsIgnoreCase(SocketServer.LOBBY)) {
			return;
		}
		try {
			log.log(Level.INFO, "Closing empty room: " + name);
			close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void joinRoom(String room, ServerThread client) {
		server.joinRoom(room, client);
	}

	protected void joinLobby(ServerThread client) {
		server.joinLobby(client);
	}

	/***
	 * Helper function to process messages to trigger different functionality.
	 * 
	 * @param message The original message being sent
	 * @param client  The sender of the message (since they'll be the ones
	 *                triggering the actions)
	 */
	private String processCommands(String message, ServerThread client) {
		String rsp = null;
		isDM = false;
		receiverName = null;
		try {
			if (message.indexOf(COMMAND_TRIGGER) > -1) {
				String[] comm = message.split(COMMAND_TRIGGER);
				log.log(Level.INFO, message);
				String part1 = comm[1];
				String[] comm2 = part1.split(" ");
				String command = comm2[0];
				if (command != null) {
					command = command.toLowerCase();
				}
				String roomName;
				String nameDec;
				String name;
				switch (command) {
				case CREATE_ROOM:
					roomName = comm2[1];
					if (server.createNewRoom(roomName)) {
						joinRoom(roomName, client);
					}
					break;
				case JOIN_ROOM:
					roomName = comm2[1];
					joinRoom(roomName, client);
					break;
				case ROLL:
					diceRoll(client);
					break;
				case FLIP:
					coinFlip(client);
					break;
				case UNMUTE:
					nameDec = comm2[1];
					String[] nameTemp2 = nameDec.split("@");
					name = nameTemp2[1];
					unmuter(name, client);
					break;
				case MUTE:
					nameDec = comm2[1];
					String[] nameTemp = nameDec.split("@");
					name = nameTemp[1];
					muter(name, client);
					break;
				}

			}

			else {
				if (message.indexOf("@") == 0) {
					String[] comm = message.split("@");
					log.log(Level.INFO, message);
					String part1 = comm[1];
					String[] comm2 = part1.split(" ", 2);
					String name = comm2[0];
					message = comm2[1];
					receiverName = name;

				}

				String workMessage = message;

				if (workMessage.indexOf("~~") > -1) {
					String[] splMessage = workMessage.split("~~");
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
				if (workMessage.indexOf("##") > -1) {
					String[] splMessage = workMessage.split("##");
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

				rsp = workMessage;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rsp;
	}

	// TODO changed from string to ServerThread
	protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread c = iter.next();
			boolean messageSent = c.sendConnectionStatus(client.getClientName(), isConnect, message);
			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + c.getId());
			}
		}
	}

	/***
	 * Takes a sender and a message and broadcasts the message to all clients in
	 * this room. Client is mostly passed for command purposes but we can also use
	 * it to extract other client info.
	 * 
	 * @param sender  The client sending the message
	 * @param message The message to broadcast inside the room
	 */
	protected void sendMessage(ServerThread sender, String message) {
		log.log(Level.INFO, getName() + ": Sending message to " + clients.size() + " clients");
		String resp = processCommands(message, sender);
		if (resp == null) {
			// it was a command, don't broadcast
			return;
		} else if (DMCheck(receiverName)) {
			DMclients.add(sender);
			Iterator<ServerThread> iter = DMclients.iterator();
			while (iter.hasNext()) {
				ServerThread dmclient = iter.next();
				boolean messageSent = dmclient.send(sender.getClientName(), resp);
				if (!messageSent) {
					iter.remove();
					log.log(Level.INFO, "Removed client " + dmclient.getId());
				}

			}
			DMclients.clear();
			return;
		} else {
			Iterator<ServerThread> iter = clients.iterator();
			while (iter.hasNext()) {
				ServerThread client = iter.next();
				if (!client.isMuted(sender.getClientName())) {
					boolean messageSent = client.send(sender.getClientName(), resp);
					if (!messageSent) {
						iter.remove();
						log.log(Level.INFO, "Removed client " + client.getId());
					}

				}
			}
		}
	}

	/***
	 * Will attempt to migrate any remaining clients to the Lobby room. Will then
	 * set references to null and should be eligible for garbage collection
	 */
	@Override
	public void close() throws Exception {
		int clientCount = clients.size();
		if (clientCount > 0) {
			log.log(Level.INFO, "Migrating " + clients.size() + " to Lobby");
			Iterator<ServerThread> iter = clients.iterator();
			Room lobby = server.getLobby();
			while (iter.hasNext()) {
				ServerThread client = iter.next();
				lobby.addClient(client);
				iter.remove();
			}
			log.log(Level.INFO, "Done Migrating " + clients.size() + " to Lobby");
		}
		server.cleanupRoom(this);
		name = null;
		// should be eligible for garbage collection now
	}

}