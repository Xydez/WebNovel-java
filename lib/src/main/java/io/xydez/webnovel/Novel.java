package io.xydez.webnovel;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;

public interface Novel {
	@NonNull
	String getName();

	@Nullable
	String getSynopsis();

	@Nullable
	String getImageUrl();

	/**
	 * Get the length of the novel in chapters
	 * @return length of the novel in chapters
	 */
	int chapters();

	/**
	 * Get a specific chapter from the novel
	 * @param chapter The chapter to get
	 * @return The chapter
	 */
	@Nullable
	Chapter getChapter(int chapter) throws IOException;
}
