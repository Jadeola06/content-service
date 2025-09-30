package com.flexydemy.content.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class DailyService {

    @Value("${daily.api.url}")
    private String dailyApiUrl;

    @Value("${daily.api.key}")
    private String dailyApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createRoom() {
        String url = dailyApiUrl + "/rooms";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + dailyApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("privacy", "private"); // room is joinable via token only
        body.put("properties", Map.of(
                "enable_screenshare", true,
                "enable_chat", true,
                "start_video_off", true,
                "start_audio_off", true
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("name").asText(); // room name
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Daily API response", e);
            }
        }
        throw new RuntimeException("Daily API room creation failed: " + response.getBody());
    }

    public String createMeetingToken(String roomName, String userName, boolean isOwner) {
        String url = dailyApiUrl + "/meeting-tokens";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + dailyApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("properties", Map.of(
                "room_name", roomName,
                "user_name", userName,
                "is_owner", isOwner
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("token").asText();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Daily API response", e);
            }
        }
        throw new RuntimeException("Daily API token creation failed: " + response.getBody());
    }}

