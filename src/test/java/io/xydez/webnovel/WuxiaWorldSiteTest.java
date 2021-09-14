package io.xydez.webnovel;

import io.xydez.webnovel.provider.WuxiaWorldSite;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class WuxiaWorldSiteTest {
	@Test
	public void test() throws ExecutionException, InterruptedException {
		WuxiaWorldSite site = new WuxiaWorldSite();
		TestUtility.testProvider(site);
	}
}
