package com.ydo4ki.mov_i_d_e;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

/**
 * @author Sulphuris
 * @since 07.10.2024 11:01
 */
@Data
@AllArgsConstructor
public abstract class IComponent {
	protected int gx;
	protected int gy;
	protected int width;
	protected int height;

	public abstract void paint(Graphics2D g);
}
