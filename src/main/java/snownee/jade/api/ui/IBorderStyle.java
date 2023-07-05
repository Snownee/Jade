package snownee.jade.api.ui;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

@ScheduledForRemoval(inVersion = "1.20")
public interface IBorderStyle {

	IBorderStyle width(int px);

	IBorderStyle color(int color);
}
