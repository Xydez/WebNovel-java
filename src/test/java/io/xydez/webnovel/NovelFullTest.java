package io.xydez.webnovel;

import io.xydez.webnovel.provider.NovelFull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class NovelFullTest {
	@Test
	public void test() throws ExecutionException, InterruptedException {
		NovelFull site = new NovelFull();
		TestUtility.testProvider(site);
	}
}
