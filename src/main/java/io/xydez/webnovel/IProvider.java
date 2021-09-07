package io.xydez.webnovel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IProvider {
	CompletableFuture<List<INovel>> search(String query);
}
