package model;

public class Article {
    private String title;
    private String content;
    private String imageUrl;

    public Article(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
} 