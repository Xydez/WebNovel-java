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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class NovelGate implements IProvider {
    @Override
    public String getName() {
        return "NovelGate.net";
    }

    @Override
    public CompletableFuture<List<INovel>> search(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ArrayList<INovel> novels = new ArrayList<>();
                Document doc = Jsoup.connect(String.format("https://novelgate.net/search/%s", URLEncoder.encode(query, "utf-8"))).get();

                for (Element element : doc.select(".list-film .film-item a")) {
                    String url = element.attr("href");
                    String title = element.attr("title");

                    novels.add(new Novel(title, url));
                }

                return novels;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    static class Novel implements INovel {
        private final String title;
        private final String synopsis;
        private final ArrayList<String> chapterUrls = new ArrayList<>();

        public Novel(String title, String url) throws IOException {
            this.title = title;

            Document doc = Jsoup.connect(url).get();

            CopyDown copyDown = new CopyDown();
            this.synopsis = copyDown.convert(doc.select(".film-content").html());

            for (Element element : doc.select(".book li a")) {
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
        public int getChapters() {
            return this.chapterUrls.size();
        }

        @Override
        public CompletableFuture<Chapter> getChapter(int chapter) throws IOException {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Document doc = Jsoup.connect(chapterUrls.get(chapter)).get();

                    String name = doc.select(".episode-name").text();
                    CopyDown copyDown = new CopyDown();
                    String content = copyDown.convert(doc.select("#chapter-body").html());

                    return new Chapter(name, content);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            });
        }
    }
}
