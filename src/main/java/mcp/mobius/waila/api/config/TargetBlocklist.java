package mcp.mobius.waila.api.config;

import java.util.List;

public class TargetBlocklist {
	public String __comment = "This is a blocklist for the target of Jade. You can add registry ids to the \"values\" list. Restart the game to apply changes.";
	public List<String> values = List.of();
	public int version = 1;
}
