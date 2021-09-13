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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class ReadNovelFull implements IProvider {
	@Override
	public CompletableFuture<List<INovel>> search(String query) {
		return CompletableFuture.supplyAsync(() -> {
			ArrayList<INovel> novels = new ArrayList<>();

			try {
				// GET https://readnovelfull.com/search?keyword=%s
				String url = String.format("https://readnovelfull.com/search?keyword=%s", URLEncoder.encode(query, "UTF-8"));
				Document doc = Jsoup.connect(url).get();
				for (Element element : doc.select(".list-novel:not(.list-genre, .list-side) .row")) {
					Elements elements = element.select("> div");
					String image = elements.get(0).select("img").attr("src");
					Element titleElement = elements.get(1).select(".novel-title a").first();
					if (titleElement == null) {
						throw new RuntimeException("Title element could not be found");
					}

					String name = titleElement.text();
					String link = titleElement.attr("href");

					novels.add(new Novel(name, image, "https://readnovelfull.com" + link));
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
		private final ArrayList<String> chapterLinks = new ArrayList<>();

		public Novel(String name, String imageUrl, String link) throws IOException {
			this.name = name;
			this.imageUrl = imageUrl;

			Document doc = Jsoup.connect(link).get();
			this.synopsis = doc.select("#tab-description .desc-text p").stream().map(Element::text).collect(Collectors.joining("\n\n"));

			int id = Integer.parseInt(doc.select("#rating").attr("data-novel-id"));

			Document chapterList = Jsoup.connect(String.format("https://readnovelfull.com/ajax/chapter-archive?novelId=%d", id)).get();
			for (Element element : chapterList.select(".list-chapter li a")) {
				this.chapterLinks.add(element.attr("href"));
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

		@Nullable
		@Override
		public CompletableFuture<Chapter> getChapter(int chapter) throws IOException {
			return CompletableFuture.supplyAsync(() -> {
				Document doc = null;
				try {
					doc = Jsoup.connect("https://readnovelfull.com" + this.chapterLinks.get(chapter)).get();
				} catch (IOException e) {
					throw new CompletionException(e);
				}
				String name = doc.select(".chr-title .chr-text").text();

				CopyDown copyDown = new CopyDown();
				Element element = doc.select("#chr-content").first();
				if (element == null) {
					throw new CompletionException(new RuntimeException("Element with id 'chr-content' could not be found"));
				}
				String content = copyDown.convert(element.html());

				return new Chapter(name, content);
			});
		}
	}
}
