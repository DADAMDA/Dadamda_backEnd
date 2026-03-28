package com.example.Oauth.bookmark.crawler;

import com.example.Oauth.common.exception.MetadataCrawlException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * URL 접속과 메타데이터 추출을 담당하는 크롤러.
 * <p>
 * Jsoup를 이용해 HTML을 가져오고 OG 메타데이터, 일반 meta 태그, title 태그를 순차적으로 확인한다.
 */
@Component
public class MetadataCrawler {

    private static final int TIMEOUT_MILLIS = 7000;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; DadamdaBot/1.0)";

    /**
     * 주어진 URL의 메타데이터를 수집한다.
     *
     * @param rawUrl 메타데이터를 수집할 원본 URL
     * @return 추출된 북마크 메타데이터
     */
    public BookmarkMetadata crawl(String rawUrl) {
        URI uri = parseUrl(rawUrl);

        try {
            Document document = Jsoup.connect(uri.toString())
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MILLIS)
                    .get();

            String domain = extractDomain(uri);
            String title = firstNonBlank(
                    extractMetaProperty(document, "og:title"),
                    trimToNull(document.title()),
                    extractFirstText(document, "h1"),
                    domain
            );
            String description = firstNonBlank(
                    extractMetaProperty(document, "og:description"),
                    extractMetaName(document, "description"),
                    extractMetaName(document, "twitter:description"),
                    extractFirstText(document, "article p"),
                    extractFirstText(document, "main p"),
                    extractFirstText(document, "p")
            );
            String thumbnail = firstNonBlank(
                    extractMetaProperty(document, "og:image"),
                    extractMetaName(document, "twitter:image"),
                    extractLinkHref(document, "link[rel=image_src]"),
                    extractIcon(document),
                    extractFirstImage(document)
            );

            return new BookmarkMetadata(
                    title,
                    description,
                    thumbnail,
                    domain
            );
        } catch (IOException exception) {
            throw new MetadataCrawlException("Failed to crawl metadata for url: " + rawUrl, exception);
        }
    }

    /**
     * URL 문자열을 검증 가능한 URI로 변환한다.
     *
     * @param rawUrl 검증할 원본 URL
     * @return 검증된 URI
     */
    public URI parseUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }

        try {
            URI uri = new URI(rawUrl.trim());
            String scheme = trimToNull(uri.getScheme());
            String host = trimToNull(uri.getHost());

            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("url must start with http:// or https://");
            }
            if (host == null) {
                throw new IllegalArgumentException("url host is required");
            }

            return uri;
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid URL: " + rawUrl, exception);
        }
    }

    /**
     * URI에서 저장용 도메인을 추출한다.
     *
     * @param uri 도메인을 추출할 URI
     * @return www가 제거된 도메인 문자열
     */
    public String extractDomain(URI uri) {
        String host = trimToNull(uri.getHost());
        if (host == null) {
            throw new IllegalArgumentException("url host is required");
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        return normalizedHost.startsWith("www.") ? normalizedHost.substring(4) : normalizedHost;
    }

    private String extractMetaProperty(Document document, String property) {
        return trimToNull(document.select("meta[property='" + property + "']").attr("content"));
    }

    private String extractMetaName(Document document, String name) {
        return trimToNull(document.select("meta[name='" + name + "']").attr("content"));
    }

    private String extractLinkHref(Document document, String cssQuery) {
        Element element = document.selectFirst(cssQuery);
        if (element == null) {
            return null;
        }

        return normalizeUrl(element.absUrl("href"), element.attr("href"));
    }

    private String extractIcon(Document document) {
        return firstNonBlank(
                extractLinkHref(document, "link[rel='apple-touch-icon']"),
                extractLinkHref(document, "link[rel='shortcut icon']"),
                extractLinkHref(document, "link[rel='icon']")
        );
    }

    private String extractFirstImage(Document document) {
        Element image = document.selectFirst("img[src]");
        if (image == null) {
            return null;
        }

        return normalizeUrl(image.absUrl("src"), image.attr("src"));
    }

    private String extractFirstText(Document document, String cssQuery) {
        Element element = document.selectFirst(cssQuery);
        if (element == null) {
            return null;
        }

        return trimToNull(element.text());
    }

    private String normalizeUrl(String absoluteUrl, String fallbackUrl) {
        return firstNonBlank(trimToNull(absoluteUrl), trimToNull(fallbackUrl));
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null) {
                return candidate;
            }
        }

        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
