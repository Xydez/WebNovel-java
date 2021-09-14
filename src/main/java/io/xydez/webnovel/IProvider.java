package io.xydez.webnovel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IProvider {
	String getName();
	CompletableFuture<List<INovel>> search(String query);
}
