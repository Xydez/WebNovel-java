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
        TestUtility.testProvider(site);
    }
}
