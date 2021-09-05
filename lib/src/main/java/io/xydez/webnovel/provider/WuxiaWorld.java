package io.xydez.webnovel.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.furstenheim.CopyDown;
import io.xydez.webnovel.Chapter;
import io.xydez.webnovel.Novel;
import io.xydez.webnovel.Provider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;

public class WuxiaWorld implements Provider {
	@Override
	public ArrayList<io.xydez.webnovel.Novel> search(String query) throws IOException {
		ArrayList<io.xydez.webnovel.Novel> novels = new ArrayList<>();

		Document doc = Jsoup.connect(String.format("https://wuxiaworld.site/?s=%s&post_type=wp-manga", URLEncoder.encode(query, "UTF-8"))).get();

		for (Element result : doc.select(".search-wrap .row")) {
			Elements cols = result.select("> div");

			// Referer: https://wuxiaworld.site/
			String imageUrl = cols.get(0).select("img").attr("src");

			Elements titleEl = cols.get(1).select(".post-title a");
			String link = titleEl.attr("href");
			String name = titleEl.text();

			novels.add(new Novel(name, imageUrl, link));
		}

		return novels;
	}

	static class Novel implements io.xydez.webnovel.Novel {
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
		public int chapters() {
			return this.chapterLinks.size();
		}

		@Nullable
		@Override
		public Chapter getChapter(int chapter) throws IOException {
			Document doc = Jsoup.connect(chapterLinks.get(chapter)).get();
			CopyDown copyDown = new CopyDown();

			String content = copyDown.convert(doc.select(".read-container .text-left").html());

			return new Chapter(null, content);
		}
	}
}
