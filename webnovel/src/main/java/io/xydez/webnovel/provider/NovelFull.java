package io.xydez.webnovel.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.furstenheim.CopyDown;
import io.xydez.webnovel.Chapter;
import io.xydez.webnovel.Provider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class NovelFull implements Provider {
	@Override
	public ArrayList<io.xydez.webnovel.Novel> search(String query) throws IOException {
		ArrayList<io.xydez.webnovel.Novel> novels = new ArrayList<>();

		// GET https://readnovelfull.com/search?keyword=%s
		String url = String.format("https://novelfull.com/search?keyword=%s", URLEncoder.encode(query, "UTF-8"));
		Document doc = Jsoup.connect(url).get();
		for (Element element : doc.select(".list-truyen:not(.list-cat, .list-side) .row")) {
			Elements elements = element.select("> div");
			String image = elements.get(0).select("img").attr("src");
			Element titleElement = elements.get(1).select(".truyen-title a").first();
			String name = titleElement.text();
			String link = titleElement.attr("href");

			novels.add(new ReadNovelFull.Novel(name, image, "https://novelfull.com" + link));
		}

		return novels;
	}

	static class Novel implements io.xydez.webnovel.Novel {
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

			Document chapterList = Jsoup.connect(String.format("https://novelfull.com/ajax/chapter-archive?novelId=%d", id)).get();
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
		public int chapters() {
			return this.chapterLinks.size();
		}

		@Nullable
		@Override
		public Chapter getChapter(int chapter) throws IOException {
			Document doc = Jsoup.connect("https://novelfull.com" + this.chapterLinks.get(chapter)).get();
			String name = doc.select(".chr-title .chr-text").text();

			CopyDown copyDown = new CopyDown();
			String content = copyDown.convert(doc.select("#chr-content").first().html());

			return new Chapter(name, content);
		}
	}
}
