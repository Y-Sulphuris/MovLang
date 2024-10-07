package com.ydo4ki.mov_i_d_e;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Sulphuris
 * @since 07.10.2024 9:54
 */
public class De extends Canvas implements MouseListener, KeyListener {
	public static final String title = "i[0d] 0e : 1";

	private final Collection<IComponent> components = new ArrayList<>();

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		for (IComponent component : components) {
			component.paint((Graphics2D) g);
		}
		//super.paint(g);
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

		de.addKeyListener(de);
		de.addMouseListener(de);

		de.components.add(new IFrame(5, 5, 20, 20));

		frame.setVisible(true);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		System.out.println("kt: " + e.paramString());
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println("kp: " + e.paramString());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		System.out.println("kr: " + e.paramString());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("mc: " + e.paramString());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("mp: " + e.paramString());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println("mr: " + e.paramString());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		System.out.println("me: " + e.paramString());
	}

	@Override
	public void mouseExited(MouseEvent e) {
		System.out.println("mex: " + e.paramString());
	}
}
