package io.xydez.webnovel.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.github.furstenheim.CopyDown;
import io.xydez.webnovel.Chapter;
import org.json.simple.JSONArray;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;

import io.xydez.webnovel.IProvider;
import io.xydez.webnovel.INovel;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class BoxNovel implements IProvider {
    @Override
	public CompletableFuture<List<INovel>> search(String query) {
		return CompletableFuture.supplyAsync(() -> {
            ArrayList<INovel> novels = new ArrayList<>();

			try {
                // POST
                // action: wp-manga-search-manga
                // title: <query>
                String json = Jsoup.connect("https://boxnovel.com/wp-admin/admin-ajax.php")
                        .ignoreContentType(true)
                        .method(Connection.Method.POST)
                        .data("action", "wp-manga-search-manga", "title", query)
                        .execute().body();

                JSONParser parser = new JSONParser();
                JSONObject object = (JSONObject)parser.parse(json);

                if (!(boolean)object.get("success")) {
                    throw new RuntimeException("Success is not true");
                }

                for (Object o : (JSONArray)object.get("data")) {
                    // { title: string, type: string, url: string }
                    JSONObject value = (JSONObject)o;
                    String title = (String)value.get("title");
                    String url = (String)value.get("url");

                    novels.add(new Novel(title, url));
                }
            } catch (Exception e) {
                throw new CompletionException(e);
            }

            return novels;
		});
	}

    static class Novel implements INovel {
        private final String title;
        private final String synopsis;
        private final ArrayList<String> chapterUrls = new ArrayList<>();

        public Novel(String title, String url) throws IOException {
            this.title = title;

            System.out.println(title);

            // POST
            // action: manga_views
            // manga: <id>

            Document doc = Jsoup.connect(url).get();
            CopyDown copyDown = new CopyDown();
            this.synopsis = copyDown.convert(doc.select("#editdescription").html());

            Document chapterDoc = Jsoup.connect(url + "ajax/chapters/").post();
            for (Element element : chapterDoc.select(".wp-manga-chapter a")) {
                this.chapterUrls.add(element.attr("href"));
            }
        }

        @NonNull
        @Override
        public String getName() {
            return this.title;
        }

        @Nullable
        @Override
        public String getSynopsis() {
            return this.synopsis;
        }

        @Nullable
        @Override
        public String getImageUrl() {
            return null;
        }

        @Override
        public int chapters() {
            return this.chapterUrls.size();
        }

        @Override
        public CompletableFuture<Chapter> getChapter(int chapter) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Document doc = Jsoup.connect(chapterUrls.get(chapter)).get();

                    String name = doc.select(".breadcrumb .active").text();

                    CopyDown copyDown = new CopyDown();
                    String content = copyDown.convert(doc.select(".reading-content .text-left").html());

                    return new Chapter(name, content);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            });
        }
    }
}
