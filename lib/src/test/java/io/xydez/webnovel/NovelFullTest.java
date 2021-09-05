package io.xydez.webnovel;

import io.xydez.webnovel.provider.NovelFull;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NovelFullTest {
	@Test
	public void test() throws IOException {
		NovelFull site = new NovelFull();
		ArrayList<Novel> results = site.search("Super Gene");
		assertNotNull(results);
		assertTrue(results.size() > 0);

		for (Novel novel : results) {
			assertNotNull(novel.getName());
			novel.getSynopsis();
			novel.getImageUrl();

			assertTrue(novel.chapters() > 0);

			Chapter chapter = novel.getChapter(0);
			assertNotNull(chapter);
			chapter.getName();
			assertNotNull(chapter.getContent());
		}
	}
}
