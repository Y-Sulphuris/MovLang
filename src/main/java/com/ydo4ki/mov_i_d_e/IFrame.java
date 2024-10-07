package com.ydo4ki.mov_i_d_e;

import java.awt.*;

/**
 * @author Sulphuris
 * @since 07.10.2024 11:01
 */
public class IFrame extends IComponent {
	public IFrame(int gx, int gy, int width, int height) {
		super(gx, gy, width, height);
	}

	@Override
	public void paint(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(gx, gy, width, height);
	}
}
