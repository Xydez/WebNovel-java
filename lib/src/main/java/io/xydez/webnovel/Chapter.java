package io.xydez.webnovel;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.NonNull;

public class Chapter {
	@Nullable
	private String name;

	private String content;

	public Chapter(@Nullable String name, @NonNull String content) {
		this.name = name;
		this.content = content;
	}

	@Nullable
	public String getName() {
		return name;
	}

	@NonNull
	public String getContent() {
		return content;
	}
}
