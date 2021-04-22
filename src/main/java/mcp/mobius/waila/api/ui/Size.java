package mcp.mobius.waila.api.ui;

public class Size {

	public static final Size ZERO = new Size(0, 0);

	public final int width;
	public final int height;

	public Size(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
