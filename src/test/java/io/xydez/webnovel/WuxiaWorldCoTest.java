package io.xydez.webnovel;

import io.xydez.webnovel.provider.WuxiaWorldCo;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class WuxiaWorldCoTest {
	@Test
	public void test() throws ExecutionException, InterruptedException {
		WuxiaWorldCo site = new WuxiaWorldCo();
		TestUtility.testProvider(site);
	}
}
