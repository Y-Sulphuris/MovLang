package com.ydo4ki.mov_i_d_e;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

/**
 * @author Sulphuris
 * @since 07.10.2024 9:54
 */
public class De extends Canvas {
	public static final String title = "i[0d] 0e : 1";

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		//super.paint(g);
	}

	public void redraw() {
		BufferStrategy bf = getBufferStrategy();
		if (bf == null) {
			createBufferStrategy(3);
			bf = getBufferStrategy();
		}

		Graphics g = bf.getDrawGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		// render
		g.dispose();
		bf.show();
		System.out.println("draw");
	}

	public static void main(String[] args) {
		De de = new De();
		de.setPreferredSize(new Dimension(800, 600));

		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(de);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		//de.redraw(); // :/
	}
}
