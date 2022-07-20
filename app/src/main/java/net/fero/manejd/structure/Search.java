package net.fero.manejd.structure;

import java.util.HashMap;

public class Search {
    public String siteName;
    public String siteUrl;
    public String email;
    public String username;
    public String password = null;
    private final HashMap<String, String> asMap;

    public Search(String siteName, String siteUrl, String email, String username) {
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.email = email;
        this.username = username;

        asMap = new HashMap<>();

        asMap.put("site_name", siteName);
        asMap.put("site_url", siteUrl);
        asMap.put("email", email);
        asMap.put("username", username);
    }

    public Search(String siteName, String siteUrl, String email, String username, String password) {
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.email = email;
        this.username = username;
        this.password = password;

        asMap = new HashMap<>();

        asMap.put("site_name", siteName);
        asMap.put("site_url", siteUrl);
        asMap.put("email", email);
        asMap.put("username", username);
    }

    @Override
    public String toString() {
        return "Search{" +
                "siteName='" + siteName + '\'' +
                ", siteUrl='" + siteUrl + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    public HashMap<String, String> getAsMap() {
        return asMap;
    }

    public boolean isEmpty() {
        return siteName == null && siteUrl == null && email == null && username == null;
    }
}
