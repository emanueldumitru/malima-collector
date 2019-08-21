package com.orange.malimacollector.service.Rundeck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.orange.malimacollector.config.MachineConfiguration;
import com.orange.malimacollector.entities.RundeckEntities.Job;
import com.orange.malimacollector.entities.RundeckEntities.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;

@Service
public class RundeckService {
    @Autowired
    private MachineConfiguration config;

    public String buildURL(int choice){
        String newURL = this.config.getWebsites()[5].getLocalAddress();
        switch (choice){
            case 1:
                newURL += "api/1/projects";
                break;
            case 2:
                newURL += "api/14/project/";
                break;
        }
        return newURL;
    }

    public String curlCommand(String URL) {
        String command = "curl -H \"Accept: application/json\" " + URL;
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.directory(Paths.get("C:/Windows/System32").toFile());
        try {
            Process process = processBuilder.start();
            StringBuilder sb = new StringBuilder();
            InputStreamReader in = null;
            in = new InputStreamReader(process.getInputStream(), Charset.defaultCharset());
            BufferedReader bufferedReader = new BufferedReader(in);
            if (bufferedReader != null) {
                int cp;
                while ((cp = bufferedReader.read()) != -1) {
                    sb.append((char) cp);
                }
                bufferedReader.close();
            }
            in.close();
            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Project[] projectFromJsonString(String json) throws IOException {
        return getProjectReader().readValue(json);
    }

    public static String projectToJsonString(Project[] obj) throws JsonProcessingException {
        return getProjectWriter().writeValueAsString(obj);
    }

    public static Job[] jobFromJsonString(String json) throws IOException {
        return getJobReader().readValue(json);
    }

    public static String jobToJsonString(Job[] obj) throws JsonProcessingException {
        return getJobWriter().writeValueAsString(obj);
    }

    private static ObjectReader projectReader;
    private static ObjectWriter projectWriter;

    private static ObjectReader jobReader;
    private static ObjectWriter jobWriter;

    private static void instantiateProjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        projectReader = mapper.reader(Project[].class);
        projectWriter = mapper.writerFor(Project[].class);
    }

    private static void instantiateJobMapper() {
        ObjectMapper mapper = new ObjectMapper();
        jobReader = mapper.reader(Job[].class);
        jobWriter = mapper.writerFor(Job[].class);
    }

    private static ObjectReader getProjectReader() {
        if (projectReader == null) instantiateProjectMapper();
        return projectReader;
    }

    private static ObjectWriter getProjectWriter() {
        if (projectWriter == null) instantiateProjectMapper();
        return projectWriter;
    }

    private static ObjectReader getJobReader() {
        if (jobReader == null) instantiateJobMapper();
        return jobReader;
    }

    private static ObjectWriter getJobWriter() {
        if (jobWriter == null) instantiateJobMapper();
        return jobWriter;
    }

    public Object handler(int choice){
        String URL;
        String content;
        switch (choice){
            case 1:
                URL = buildURL(1);
                URL += ("?authtoken=" + this.config.getWebsites()[5].getAdminPassword());
                content = curlCommand(URL);
                try {
                    return projectFromJsonString(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            case 2:
                URL = buildURL(2);
                Project[] projects = (Project[]) handler(1);
                ArrayList<Job[]> jobCollection = new ArrayList<>();
                for (Project project : projects){
                    String newURL = URL + project.getName() + "/jobs??authtoken=" + this.config.getWebsites()[5].getAdminPassword();
                    content = curlCommand(newURL);
                    try {
                        jobCollection.add(jobFromJsonString(content));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return jobCollection;
            default:
                return null;
        }
    }
}
