
package client;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class User extends JPanel {
	private String name;
	private JLabel nameField;

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