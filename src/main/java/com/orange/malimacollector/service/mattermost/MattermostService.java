package com.orange.malimacollector.service.mattermost;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orange.malimacollector.config.MachineConfiguration;
import com.orange.malimacollector.entities.mattermost.Channel;
import com.orange.malimacollector.entities.mattermost.PostList;
import com.orange.malimacollector.entities.mattermost.Teams;
import com.orange.malimacollector.entities.mattermost.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MattermostService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MachineConfiguration config;

    public String buildURL(int choice){
        String newURL = this.config.getWebsites()[4].getLocalAddress();
        switch (choice){
            case 1:
                newURL += "users/me";
                break;
            case 2:
                newURL += "teams";
                break;
            case 3:
                newURL += "teams/";
                break;
            case 4:
                newURL += "channels/";
                break;
            default:
                newURL += "";
        }
        return newURL;
    }

    public String getData(String url){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.config.getWebsites()[4].getAdminPassword());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    public String getData(String url, Teams team, String searchTerm){
        url += (team.getID() + "/posts/search");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.config.getWebsites()[4].getAdminPassword());
        String requestJSON = "{\"terms\": \"" + searchTerm +
                "\",\"is_or_search\":true,\"time_zone_offset\": 0,\"include_deleted_channels\":true,\"page\": 0,\"per_page\": 60}";
        HttpEntity<String> entity = new HttpEntity<>(requestJSON, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    // Serialize/deserialize helpers

    public static User userFromJsonString(String json) throws IOException {
        return getUserObjectReader().readValue(json);
    }

    public static Teams[] teamsFromJsonString(String json) throws IOException {
        return getTeamsObjectReader().readValue(json);
    }

    public static Channel[] channelFromJsonString(String json) throws IOException {
        return getChannelObjectReader().readValue(json);
    }

    public static PostList postFromJsonString(String json) throws IOException {
        return getPostObjectReader().readValue(json);
    }

    private static ObjectReader userReader;

    private static ObjectReader teamReader;

    private static ObjectReader channelReader;

    private static ObjectReader postReader;

    private static void instantiateUserMapper() {
        ObjectMapper mapper = new ObjectMapper();
        userReader = mapper.reader(User.class);
    }

    private static void instantiateTeamsMapper() {
        ObjectMapper mapper = new ObjectMapper();
        teamReader = mapper.reader(Teams[].class);
    }

    private static void instantiateChannelMapper() {
        ObjectMapper mapper = new ObjectMapper();
        channelReader = mapper.reader(Channel[].class);
    }

    private static void instantiatePostMapper() {
        ObjectMapper mapper = new ObjectMapper();
        postReader = mapper.reader(PostList.class);
    }

    private static ObjectReader getUserObjectReader() {
        if (userReader == null) instantiateUserMapper();
        return userReader;
    }

    private static ObjectReader getTeamsObjectReader() {
        if (teamReader == null) instantiateTeamsMapper();
        return teamReader;
    }

    private static ObjectReader getChannelObjectReader() {
        if (channelReader == null) instantiateChannelMapper();
        return channelReader;
    }

    private static ObjectReader getPostObjectReader() {
        if (postReader == null) instantiatePostMapper();
        return postReader;
    }

    public Object handler(int choice){
        String url;
        String content;
        switch (choice){
            case 1:
                url = buildURL(1);
                content = getData(url);
                try {
                    return userFromJsonString(content);
                } catch (IOException e) {
                    logger.error("Mattermost User Error:" + e.getMessage());
                    return null;
                }
            case 2:
                url = buildURL(2);
                content = getData(url);
                try {
                    return teamsFromJsonString(content);
                } catch (IOException e) {
                    logger.error("Mattermost Team Error:" + e.getMessage());
                    return null;
                }
            default:
                return null;
        }
    }

    public Object handler(Teams team){
        String url = buildURL(3) + team.getID() + "/channels";
        String content = getData(url);
        try {
            return channelFromJsonString(content);
        } catch (IOException e) {
            logger.error("Mattermost Channel Error:" + e.getMessage());
            return null;
        }
    }

    public Object handler(Channel channel) {
        String url = buildURL(4) + channel.getID() + "/posts";
        String content = getData(url);
        try {
            return postFromJsonString(content);
        } catch (IOException e) {
            logger.error("Mattermost Posts Error:" + e.getMessage());
            return null;
        }
    }

    public List<String> handler(String searchTerm, Teams team) {
        String url = buildURL(3);
        ArrayList<String> posts = new ArrayList<>();
        JsonObject o = new JsonParser().parse(getData(url, team, searchTerm)).getAsJsonObject();

        for(Map.Entry<String, JsonElement> entry : o.entrySet()) {
            if (entry.getKey().equals("posts")) {
                JsonObject obj = new JsonParser().parse(entry.getValue().toString()).getAsJsonObject();
                for (Map.Entry<String, JsonElement> val : obj.entrySet()) {
                    JsonObject content = new JsonParser().parse(val.getValue().toString()).getAsJsonObject();
                    for (Map.Entry<String, JsonElement> ct : content.entrySet()) {
                        if (ct.getKey().equals("message")) {
                            posts.add(ct.getValue().toString());
                        }
                    }
                }
            }
        }
        return posts;
    }
}