package io.xydez.webnovel.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.furstenheim.CopyDown;
import io.xydez.webnovel.Chapter;
import io.xydez.webnovel.INovel;
import io.xydez.webnovel.IProvider;
import io.xydez.webnovel.Utility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class WuxiaWorldSite implements IProvider {
	@Override
	public String getName() {
		return "WuxiaWorld.site";
	}

	@Override
	public CompletableFuture<List<INovel>> search(String query) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Document doc = Jsoup.connect(String.format("https://wuxiaworld.site/?s=%s&post_type=wp-manga", URLEncoder.encode(query, "UTF-8"))).get();

				return Utility.sequence(doc.select(".search-wrap .row").stream().map(result -> CompletableFuture.supplyAsync(() -> {
					Elements cols = result.select("> div");

					// Referer: https://wuxiaworld.site/
					String imageUrl = cols.get(0).select("img").attr("src");

					Elements titleEl = cols.get(1).select(".post-title a");
					String link = titleEl.attr("href");
					String name = titleEl.text();

					try {
						return (INovel)new Novel(name, imageUrl, link);
					} catch (IOException e) {
						throw new CompletionException(e);
					}
				})).collect(Collectors.toList())).get();
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}

	static class Novel implements INovel {
		private final String name;
		private final String imageUrl;
		private final String synopsis;
		private final LinkedList<String> chapterLinks = new LinkedList<>();

		public Novel(String name, String imageUrl, String link) throws IOException {
			this.name = name;
			this.imageUrl = imageUrl;

			CopyDown copyDown = new CopyDown();

			Document doc = Jsoup.connect(link).get();

			this.synopsis = copyDown.convert(doc.select(".description-summary .summary__content").html());

			int postId = Integer.parseInt(doc.select("#manga-chapters-holder").attr("data-id"));

			Document chaptersDoc = Jsoup.connect("https://wuxiaworld.site/wp-admin/admin-ajax.php").data("action", "manga_get_chapters", "manga", String.valueOf(postId)).post();

			for (Element element : chaptersDoc.select(".main.version-chap li")) {
				chapterLinks.addFirst(element.select("a").attr("href"));
			}
		}

		@NonNull
		@Override
		public String getName() {
			return this.name;
		}

		@Nullable
		@Override
		public String getSynopsis() {
			return this.synopsis;
		}

		@Nullable
		@Override
		public String getImageUrl() {
			return this.imageUrl;
		}

		@Override
		public int getChapters() {
			return this.chapterLinks.size();
		}

		@Override
		public CompletableFuture<Chapter> getChapter(int chapter) {
			return CompletableFuture.supplyAsync(() -> {
				Document doc = null;
				try {
					doc = Jsoup.connect(chapterLinks.get(chapter)).get();
				} catch (IOException e) {
					throw new CompletionException(e);
				}

				CopyDown copyDown = new CopyDown();

				String content = copyDown.convert(doc.select(".read-container .text-left").html());

				return new Chapter(null, content);
			});
		}
	}
}
