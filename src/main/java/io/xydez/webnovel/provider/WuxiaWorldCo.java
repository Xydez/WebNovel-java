package io.xydez.webnovel.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.furstenheim.CopyDown;
import io.xydez.webnovel.Chapter;
import io.xydez.webnovel.INovel;
import io.xydez.webnovel.IProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class WuxiaWorldCo implements IProvider {
	@Override
	public String getName() {
		return "WuxiaWorld.co";
	}

	@Override
	public CompletableFuture<List<INovel>> search(String query) {
		return CompletableFuture.supplyAsync(() -> {
			ArrayList<INovel> novels = new ArrayList<>();

			try {
				Document doc = Jsoup.connect(String.format("https://www.wuxiaworld.co/search/%s/1", URLEncoder.encode(query,  "utf-8").replace("+", "%20"))).get();

				for (Element result : doc.select(".result-list .list-item")) {
					String imageUrl = result.select(".item-img").attr("src");
					Element nameElement = result.select(".book-name").first();
					String name = nameElement.text();
					String url = nameElement.attr("href");

					novels.add(new Novel(name, imageUrl, url));
				}
			} catch (Exception e) {
				throw new CompletionException(e);
			}

			return novels;
		});
	}

	static class Novel implements INovel {
		private final String name;
		private final String imageUrl;
		private final String synopsis;
		private final ArrayList<String> chapterUrls = new ArrayList<>();

		public Novel(String name, String imageUrl, String url) throws IOException {
			this.name = name;
			this.imageUrl = imageUrl;

			Document doc = Jsoup.connect("https://wuxiaworld.co" + url).get();

			// .desc
			this.synopsis = doc.select(".desc").text();

			for (Element element : doc.select(".chapter-list .chapter-item")) {
				chapterUrls.add(element.attr("href"));
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
			return this.chapterUrls.size();
		}

		@Override
		public CompletableFuture<Chapter> getChapter(int chapter) throws IOException {
			return CompletableFuture.supplyAsync(() -> {
				try {
					Document doc = Jsoup.connect("https://wuxiaworld.co" + this.chapterUrls.get(chapter)).get();

					String name = doc.select(".chapter-title").text();
					CopyDown copyDown = new CopyDown();
					String content = copyDown.convert(doc.select(".chapter-entity").html());

					return new Chapter(name, content);
				} catch (IOException e) {
					throw new CompletionException(e);
				}
			});
		}
	}
}
