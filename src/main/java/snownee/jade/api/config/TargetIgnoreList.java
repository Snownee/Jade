package snownee.jade.api.config;

import java.util.List;

public class TargetIgnoreList {
	public String __comment = "This is an ignore list for the target of Jade. You can add registry ids to the \"values\" list.";
	public List<String> values = List.of();
	public int version = 1;
}
