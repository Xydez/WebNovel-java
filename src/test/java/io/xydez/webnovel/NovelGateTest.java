package io.xydez.webnovel;

import io.xydez.webnovel.provider.BoxNovel;
import io.xydez.webnovel.provider.NovelGate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NovelGateTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        NovelGate site = new NovelGate();
        CompletableFuture<List<INovel>> futureResults = site.search("Lightning is the only way");
        List<INovel> results = futureResults.get();

        assertNotNull(results);
        assertTrue(results.size() > 0);

        CompletableFuture.allOf(results.stream().map(novel -> CompletableFuture.runAsync(() -> {
            try {
                assertNotNull(novel.getName());
                novel.getSynopsis();
                novel.getImageUrl();

                assertTrue(novel.chapters() > 0);

                CompletableFuture<Chapter> futureChapter = novel.getChapter(0);
                Chapter chapter = futureChapter.get();

                assertNotNull(chapter);
                assertDoesNotThrow(chapter::getName);
                assertNotNull(chapter.getContent());
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        })).toArray(CompletableFuture<?>[]::new)).get();
    }
}
