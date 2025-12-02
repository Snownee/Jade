package snownee.jade.util;

import java.text.BreakIterator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Unmodifiable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraft.util.StringRepresentable;
import snownee.jade.overlay.DisplayHelper;

public class WordCutter {
	private final BreakIterator breakIterator;
	private final TokenClassifier classifier;
	private final List<Token> tokens = Lists.newArrayList();
	private int maxWidth;
	private int widthSum;
	private boolean hasColon;

	public WordCutter(BreakIterator breakIterator, TokenClassifier classifier) {
		this.breakIterator = breakIterator;
		this.classifier = classifier;
	}

	public void setText(String text, int maxWidth) {
		Preconditions.checkArgument(maxWidth > 0, "maxWidth must be positive");
		this.maxWidth = maxWidth;
		tokens.clear();
		widthSum = 0;
		hasColon = false;

		breakIterator.setText(text);
		int start = breakIterator.first();
		int depth = 0;
		for (int end = breakIterator.next(); end != BreakIterator.DONE; start = end, end = breakIterator.next()) {
			String word = text.substring(start, end);
			WordCutter.TokenType type = classifier.classify(word);
			if (type == WordCutter.TokenType.LEFT_BRACKET) {
				depth++;
			}
			WordCutter.Token token = new WordCutter.Token(word, type, depth, DisplayHelper.font().width(word));
			if (type == WordCutter.TokenType.RIGHT_BRACKET) {
				depth--;
			}
			tokens.add(token);
			widthSum += token.width;
			hasColon |= type == WordCutter.TokenType.COLON;
		}
	}

	public int width() {
		return widthSum;
	}

	public boolean hasColon() {
		return hasColon;
	}

	public boolean tooLong() {
		return widthSum > maxWidth;
	}

	@Unmodifiable
	public List<Token> tokens() {
		return Collections.unmodifiableList(tokens);
	}

	public int tokenCount() {
		return tokens.size();
	}

	public void trim() {
		for (int i = 0; i < tokens.size() - 1; i++) {
			Token token = tokens.get(i);
			Token next = tokens.get(i + 1);
			if (token.type == TokenType.SEPARATOR && next.type == TokenType.SEPARATOR) {
				widthSum -= next.width;
				tokens.remove(i + 1);
				i--;
			}
		}
		if (!tokens.isEmpty() && tokens.getFirst().type == TokenType.SEPARATOR) {
			widthSum -= tokens.getFirst().width;
			tokens.removeFirst();
		}
		while (!tokens.isEmpty()) {
			Token last = tokens.getLast();
			if (last.type.canRemoveTail()) {
				widthSum -= last.width;
				tokens.removeLast();
			} else {
				break;
			}
		}
	}

	public void removeBracketed() {
		int start = findFirst(token -> token.type == TokenType.LEFT_BRACKET && token.depth == 1);
		if (start == -1) {
			return;
		}
		int end = findFirst(token -> token.type == TokenType.RIGHT_BRACKET && token.depth == 1);
		if (end == -1) {
			return;
		}
		if (start == 0 && end == tokens.size() - 1) {
			return;
		}
		removeRange(start, end + 1);
	}

	public void removeRange(int start, int end) {
		for (int i = start; i < end; i++) {
			widthSum -= tokens.get(i).width;
		}
		tokens.subList(start, end).clear();
	}

	public int findFirst(Predicate<Token> predicate) {
		for (int i = 0; i < tokens.size(); i++) {
			if (predicate.test(tokens.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public String concat(int start, int end) {
		if (start >= end || end > tokens.size()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++) {
			sb.append(tokens.get(i).str);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return concat(0, tokens.size());
	}

	public void cutToMaxWidth(boolean addEllipsis) {
		if (!tooLong()) {
			return;
		}
		Token last;
		do {
			last = tokens.removeLast();
			widthSum -= last.width;
		} while (tooLong());
		String ellipsis = addEllipsis ? "..." : "";
		if (last.type == TokenType.WORD) {
			int expectedWidth = addEllipsis ? Math.max(maxWidth - widthSum - 5, 0) : maxWidth - widthSum;
			if (last.width > expectedWidth) {
				ellipsis = DisplayHelper.font().plainSubstrByWidth(last.str, addEllipsis ? Math.max(expectedWidth - 5, 0) : expectedWidth) +
						ellipsis;
			}
		} else {
			trim();
		}
		tokens.add(new Token(ellipsis, TokenType.SYMBOL, 0, 0));
	}

	public enum TokenType implements StringRepresentable {
		SEPARATOR, WORD, LEFT_BRACKET, RIGHT_BRACKET, SYMBOL, COLON;

		boolean canRemoveTail() {
			return this == SEPARATOR || this == SYMBOL || this == COLON;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	public record Token(String str, TokenType type, int depth, int width) {}

	public interface TokenClassifier {
		TokenType classify(String s);
	}
}
