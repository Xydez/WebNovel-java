package io.xydez.webnovel;

import io.xydez.webnovel.provider.BoxNovel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BoxNovelTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        BoxNovel site = new BoxNovel();
        TestUtility.testProvider(site);
    }
}
