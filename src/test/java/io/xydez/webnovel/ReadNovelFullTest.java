package io.xydez.webnovel;

import io.xydez.webnovel.provider.ReadNovelFull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class ReadNovelFullTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        ReadNovelFull site = new ReadNovelFull();
        TestUtility.testProvider(site);
    }
}
