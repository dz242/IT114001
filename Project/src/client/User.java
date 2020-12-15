
package client;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class User extends JPanel {
	private String name;
	private JLabel nameField;

	/*
	 * User was changed to a JLabel in order to support HTML tags and color changes.
	 * A new empty border was made to keep the text from sticking to the edge of the
	 * panel.
	 */

	public User(String name) {
		this.name = name;
		nameField = new JLabel("<html>" + name + "</html>");
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.add(nameField);
	}

	public String getName() {
		return name;
	}

}