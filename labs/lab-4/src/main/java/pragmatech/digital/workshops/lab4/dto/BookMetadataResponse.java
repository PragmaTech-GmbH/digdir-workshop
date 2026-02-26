package pragmatech.digital.workshops.lab4.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookMetadataResponse(
  String key,
  String title,
  String subtitle,

  @JsonProperty("number_of_pages")
  Integer numberOfPages,

  @JsonProperty("publish_date")
  String publishDate,

  List<PublisherInfo> publishers,
  List<AuthorInfo> authors,
  List<SubjectInfo> subjects,
  Map<String, String> cover,
  BookIdentifiers identifiers
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PublisherInfo(String name) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record AuthorInfo(String name, String url) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record SubjectInfo(String name, String url) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record BookIdentifiers(
    @JsonProperty("isbn_13") List<String> isbn13,
    @JsonProperty("isbn_10") List<String> isbn10
  ) {}

  public String getMainIsbn() {
    if (identifiers != null) {
      if (identifiers.isbn13() != null && !identifiers.isbn13().isEmpty()) {
        return identifiers.isbn13().get(0);
      }
      if (identifiers.isbn10() != null && !identifiers.isbn10().isEmpty()) {
        return identifiers.isbn10().get(0);
      }
    }
    return null;
  }

  public String getPublisher() {
    if (publishers != null && !publishers.isEmpty()) {
      return publishers.get(0).name();
    }
    return null;
  }

  public String getCoverUrl() {
    if (cover != null) {
      return cover.get("medium");
    }
    return null;
  }
}
