package io.xydez.webnovel;

import io.xydez.webnovel.provider.WuxiaWorld;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class WuxiaWorldTest {
	@Test
	public void test() throws ExecutionException, InterruptedException {
		WuxiaWorld site = new WuxiaWorld();
		CompletableFuture<List<INovel>> futureResults = site.search("Super Gene");
		List<INovel> results = futureResults.get();

		assertNotNull(results);
		assertTrue(results.size() > 0);

		Utility.sequence(results.stream().map(novel -> CompletableFuture.runAsync(() -> {
			try {
				assertNotNull(novel.getName());
				novel.getSynopsis();
				novel.getImageUrl();

				assertTrue(novel.getChapters() > 0);

				CompletableFuture<Chapter> futureChapter = novel.getChapter(0);
				Chapter chapter = futureChapter.get();

				assertNotNull(chapter);
				assertDoesNotThrow(chapter::getName);
				assertNotNull(chapter.getContent());
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		})).collect(Collectors.toList())).get();
	}
}
