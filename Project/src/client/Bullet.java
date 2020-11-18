package client;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

import core.GameObject;

public class Bullet extends GameObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -405733427159043177L;
	Color color = Color.yellow;

	@Override
	public boolean draw(Graphics g) {
		// using a boolean here so we can block drawing if isActive is false via call to
		// super
		if (super.draw(g)) {
			g.setColor(color);
			g.fillOval(position.x, position.y, size.width, size.height);
		}
		return true;
	}

}
