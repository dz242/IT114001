package client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

public class ClientUI extends JFrame implements Event {
	/**
	 * 
	 */
	private static String muteName;
	private static String unmuteName;
	private static User muteClient;
	private static User unmuteClient;
	private static User messUser;
	private static String messName;
	private static User lastMessUser;
	private static String lastMessName;

	private static final long serialVersionUID = 1L;
	CardLayout card;
	ClientUI self;
	JPanel textArea;
	JPanel userPanel;
	List<User> users = new ArrayList<User>();
	private final static Logger log = Logger.getLogger(ClientUI.class.getName());

	public ClientUI(String title) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(400, 400));
		setLocationRelativeTo(null);
		self = this;
		setTitle(title);
		card = new CardLayout();
		setLayout(card);
		createConnectionScreen();
		createUserInputScreen();
		createPanelRoom();
		createPanelUserList();
		showUI();
	}

	void createConnectionScreen() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel hostLabel = new JLabel("Host:");
		JTextField host = new JTextField("127.0.0.1");
		panel.add(hostLabel);
		panel.add(host);
		JLabel portLabel = new JLabel("Port:");
		JTextField port = new JTextField("3000");
		panel.add(portLabel);
		panel.add(port);
		JButton button = new JButton("Next");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String _host = host.getText();
				String _port = port.getText();
				if (_host.length() > 0 && _port.length() > 0) {
					try {
						connect(_host, _port);
						self.next();
					} catch (IOException e1) {
						e1.printStackTrace();
						// TODO handle error properly
						log.log(Level.SEVERE, "Error connecting");
					}
				}
			}

		});
		panel.add(button);
		this.add(panel);
	}

	void createUserInputScreen() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel userLabel = new JLabel("Username:");
		JTextField username = new JTextField();
		panel.add(userLabel);
		panel.add(username);
		JButton button = new JButton("Join");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String name = username.getText();
				if (name != null && name.length() > 0) {
					SocketClient.setUsername(name);
					self.next();
				}
			}

		});
		panel.add(button);
		this.add(panel);
	}

	void createPanelRoom() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		textArea = new JPanel();
		textArea.setLayout(new BoxLayout(textArea, BoxLayout.Y_AXIS));
		textArea.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		panel.add(scroll, BorderLayout.CENTER);

		JPanel input = new JPanel();
		input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
		JTextField text = new JTextField();
		input.add(text);
		JButton button = new JButton("Send");
		text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "sendAction");
		text.getActionMap().put("sendAction", new AbstractAction() {
			public void actionPerformed(ActionEvent actionEvent) {
				button.doClick();
			}
		});

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (text.getText().length() > 0) {
					SocketClient.sendMessage(text.getText());
					text.setText("");
				}
			}

		});
		input.add(button);
		panel.add(input, BorderLayout.SOUTH);
		this.add(panel);
	}

	void createPanelUserList() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		userPanel = new JPanel();
		userPanel.setPreferredSize(new Dimension(100, userPanel.getSize().height));
		JScrollPane scroll = new JScrollPane(userPanel);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scroll, BorderLayout.CENTER);
		scroll.setMaximumSize(new Dimension(100, textArea.getSize().height));
		textArea.getParent().getParent().getParent().add(panel, BorderLayout.EAST);
	}

	void addClient(String name) {
		User u = new User(name);
		u.setPreferredSize(new Dimension(userPanel.getSize().width, 30));
		userPanel.add(u);
		users.add(u);
		System.out.println(userPanel.getSize());
		pack();
	}

	void removeClient(User client) {
		userPanel.remove(client);
		client.removeAll();
		userPanel.revalidate();
		userPanel.repaint();
	}

	/*
	 * replaceClient() is what ties this whole thing together. This was made to
	 * carry out visual changes to text in the User List Panel. It does this by
	 * first removing the old User from the list and adding in the same User with
	 * their modifications made by the methods that call replaceClient(). Ingenious
	 */

	void replaceClient(String name, User client) {
		if (name != null && client != null) {

			removeClient(client);
			log.log(Level.INFO, "removed client");

			addClient(name);
			log.log(Level.INFO, "added client");

			log.log(Level.INFO, "Completed replaceClient");
		} else {
			log.log(Level.INFO, "Failed replaceClient");
		}
	}

	/*
	 * chatFile() creates a file in the project folder, StringBuilder sb takes in
	 * the components of the JEditorPane to extract every message currently in the
	 * chat, w, the FileWriter, writes in the results of StringBuilder sb, the
	 * 'false' parameter in FileWriter makes it so that it overwrites the file every
	 * time /save is used
	 */

	void chatFile() {
		try {
			File f = new File("Chat.txt");
			if (f.createNewFile()) {
				log.log(Level.INFO, "Created a chat file " + f.getName());
			} else {
				log.log(Level.INFO, "Chat file already exists...");
			}
		} catch (IOException e) {
			log.log(Level.INFO, "Something ain't right");
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		Component[] comps = textArea.getComponents();
		for (Component x : comps) {
			JEditorPane e = (JEditorPane) x;
			if (x != null) {
				sb.append(e.getText() + System.lineSeparator());
			}
		}
		try {
			FileWriter w = new FileWriter("Chat.txt", false);
			w.write(sb.toString());
			w.close();
			log.log(Level.INFO, "Wrote to Chat.txt");
		} catch (IOException e) {
			log.log(Level.INFO, "Something ain't right");
			e.printStackTrace();
		}
	}

	/***
	 * Attempts to calculate the necessary dimensions for a potentially wrapped
	 * string of text. This isn't perfect and some extra whitespace above or below
	 * the text may occur
	 * 
	 * @param str
	 * @return
	 */
	int calcHeightForText(String str) {
		FontMetrics metrics = self.getGraphics().getFontMetrics(self.getFont());
		int hgt = metrics.getHeight();
		int adv = metrics.stringWidth(str);
		final int PIXEL_PADDING = 6;
		Dimension size = new Dimension(adv, hgt + PIXEL_PADDING);
		final float PADDING_PERCENT = 1.1f;
		// calculate modifier to line wrapping so we can display the wrapped message
		int mult = (int) Math.floor(size.width / (textArea.getSize().width * PADDING_PERCENT));
		// System.out.println(mult);
		mult++;
		return size.height * mult;
	}

	void addMessage(String str) {
		JEditorPane entry = new JEditorPane();
		entry.setContentType("text/html");
		entry.setEditable(false);
		entry.setLayout(null);
		entry.setText(str);
		Dimension d = new Dimension(textArea.getSize().width, calcHeightForText(str));
		// attempt to lock all dimensions
		entry.setMinimumSize(d);
		entry.setPreferredSize(d);
		entry.setMaximumSize(d);
		textArea.add(entry);

		pack();
		System.out.println(entry.getSize());
		JScrollBar sb = ((JScrollPane) textArea.getParent().getParent()).getVerticalScrollBar();
		sb.setValue(sb.getMaximum());
	}

	void next() {
		card.next(this.getContentPane());
	}

	void previous() {
		card.previous(this.getContentPane());
	}

	void connect(String host, String port) throws IOException {
		SocketClient.callbackListener(this);
		SocketClient.connectAndStart(host, port);
	}

	void showUI() {
		pack();
		Dimension lock = textArea.getSize();
		textArea.setMaximumSize(lock);
		setVisible(true);
	}

	@Override
	public void onClientConnect(String clientName, String message) {
		log.log(Level.INFO, String.format("%s: %s", clientName, message));
		addClient(clientName);
		if (message != null && !message.equals("")) {
			self.addMessage(String.format("%s: %s", clientName, message));
		}
	}

	@Override
	public void onClientDisconnect(String clientName, String message) {
		log.log(Level.INFO, String.format("%s: %s", clientName, message));
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
			User u = iter.next();
			if (u.getName() == clientName) {
				removeClient(u);
				iter.remove();
				self.addMessage(String.format("%s: %s", clientName, message));
				break;
			}

		}
	}

	@Override
	public void onSave() {
		// TODO Auto-generated method stub
		chatFile();
	}

	/*
	 * In order to highlight the last person who sent a message, we have to first
	 * un-highlight the previous last person to send a message. We set the
	 * background back to light gray after checking to make sure nothing is null,
	 * which it would be if this is the first time someone sent a message. We then
	 * iterate through the Users and check if the person who sent this message
	 * matches their names. If so, we set their background to orange, and then we
	 * set them as the lastMessUser to make sure they are un-highlighted after the
	 * next message comes in. Any attempted messages from muted clients will not set
	 * off this method.
	 */

	@Override
	public void onMessageReceive(String clientName, String message) {
		log.log(Level.INFO, "Last Messenger: " + lastMessName);
		if (lastMessUser != null && lastMessName != null) {
			lastMessUser.setBackground(Color.lightGray);
			lastMessUser.setOpaque(false);
		}
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
			User u = iter.next();
			if (u.getName().equals(clientName)) {
				messUser = u;
				messName = clientName;
			}
		}
		messUser.setBackground(Color.orange);
		messUser.setOpaque(true);

		log.log(Level.INFO, "Current Messenger: " + messName + ", Last Messenger: " + lastMessName);
		lastMessUser = messUser;
		lastMessName = messName;

		log.log(Level.INFO, String.format("%s: %s", clientName, message));
		self.addMessage(String.format("%s: %s", clientName, message));
	}

	@Override
	public void onChangeRoom() {
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
			User u = iter.next();
			removeClient(u);
			iter.remove();
		}
	}
	/*
	 * onMute() first compares the clientName that was passed in to the list of
	 * User's names. If there is a match, we take the muted client and its name, add
	 * in some HTML to make it gray, and send those to replaceClient(). I used
	 * global variables because I can't modify while I'm iterating through the
	 * ArrayList, as you could see by the commented out try/catch statements. I
	 * tried band-aid patching but it didn't work, so I just said fuhgeddaboutit and
	 * made global variables.
	 */

	@Override
	public void onMute(String clientName) {
		// TODO Auto-generated method stub
		log.log(Level.INFO, "Tiggered sendMute()");
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
			// try {
			String name = clientName;
			User client = iter.next();
			if (name.equals(client.getName())) {
				muteName = client.getName();
				muteClient = client;
				log.log(Level.INFO, "Passed name check");
			}
			// } catch (ConcurrentModificationException e) {
			// log.log(Level.INFO, "Caught ConcurrentModification");
			// }
		}
		replaceClient("<font color=silver>" + muteName + "</font>", muteClient);
		log.log(Level.INFO, "Reached replaceClient with " + muteName + " and " + muteClient.getName());
	}

	/*
	 * onUnmute() just does the opposite. This time, it checks the User's name
	 * against the clientName with HTML tags attached to them to make sure they are
	 * equal, and then sends just clientName and User to replaceClient(). This was
	 * made before I found out you can manipulate text color with JPanel methods,
	 * and I am too stubborn to change it back, so I just saved it for the
	 * Last-Message-Highlight part
	 */

	@Override
	public void onUnmute(String clientName) {
		// TODO Auto-generated method stub
		log.log(Level.INFO, "Tiggered sendUnmute()");
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
			// try {
			String name = "<font color=silver>" + clientName + "</font>";
			User client = iter.next();
			if (name.equals(client.getName())) {
				unmuteName = clientName;
				unmuteClient = client;
				log.log(Level.INFO, "Passed name check");
			}
			// } catch (ConcurrentModificationException e) {
			// log.log(Level.INFO, "Caught ConcurrentModification");
			// }
		}
		replaceClient(unmuteName, unmuteClient);
		log.log(Level.INFO, "Reached replaceClient with " + unmuteName + " and " + unmuteClient.getName());
	}

	public static void main(String[] args) {
		ClientUI ui = new ClientUI("Discord Lite");
		if (ui != null) {
			log.log(Level.FINE, "Started");
		}
	}

}