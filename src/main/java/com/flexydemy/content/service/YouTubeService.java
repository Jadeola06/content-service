package com.flexydemy.content.service;

import com.flexydemy.content.dto.LiveStreamDTO;
import com.flexydemy.content.enums.LessonType;
import com.flexydemy.content.exceptions.AuthorizationException;
import com.flexydemy.content.exceptions.BadRequestException;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.exceptions.VideoUploadException;
import com.flexydemy.content.model.*;
import com.flexydemy.content.model.LiveStream;
import com.flexydemy.content.repository.LiveStreamRepository;
import com.flexydemy.content.repository.CourseRepository;
import com.flexydemy.content.repository.LessonRepository;
import com.flexydemy.content.repository.VideoRepository;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.AlreadyConnectedException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;

@Service
public class YouTubeService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);

    private static final String APPLICATION_NAME = "flexydemy";

    @Value("${youtube.token-path}")
    private String TOKENS_DIRECTORY_PATH;
    private static final String COMPANY_USER_ID = "flexydemy-youtube";

    private final VideoRepository videoRepository;
    private final LessonRepository lessonRepository;

    private final CourseRepository courseRepository;

    private final LiveStreamRepository liveStreamRepository;


    private static final List<String> YOUTUBE_SCOPES = List.of(
            YouTubeScopes.YOUTUBE_UPLOAD,
            YouTubeScopes.YOUTUBE,
            YouTubeScopes.YOUTUBE_FORCE_SSL
    );

    @Autowired
    public YouTubeService(VideoRepository videoRepository, LessonRepository lessonRepository, CourseRepository courseRepository, LiveStreamRepository liveStreamRepository) {
        this.videoRepository = videoRepository;
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.liveStreamRepository = liveStreamRepository;
    }

//    @PostConstruct
//    public void initYouTubeTokens() {
//        File tokenDir = new File(TOKENS_DIRECTORY_PATH);
//        boolean shouldGenerateToken = !tokenDir.exists() || tokenDir.listFiles() == null || tokenDir.listFiles().length == 0;
//
//        if (shouldGenerateToken) {
//            logger.info("YouTube token directory is missing or empty. Generating new tokens...");
//            generateTokens();
//        } else {
//            logger.info("YouTube tokens already exist. Checking token validity...");
//
//            try {
//                // Load the existing credentials
//                Credential credential = flow.loadCredential(COMPANY_USER_ID);
//
//                if (credential != null) {
//                    // Check if the token is expired and needs refreshing
//                    if (credential.getExpiresInSeconds() <= 0) {
//                        logger.info("Access token has expired. Refreshing...");
//
//                        // Attempt to refresh the token
//                        if (credential.refreshToken()) {
//                            logger.info("Token refreshed successfully.");
//                            // Save the refreshed token back to the data store
//                            saveCredentialToDataStore(credential);
//                        } else {
//                            logger.warn("Unable to refresh the token.");
//                        }
//                    } else {
//                        logger.info("Existing token is still valid.");
//                    }
//                } else {
//                    logger.warn("No valid credentials found in the data store.");
//                    generateTokens(); // If no credentials found, generate new ones
//                }
//            } catch (TokenResponseException e) {
//                logger.error("Error refreshing token: {}", e.getMessage(), e);
//                generateTokens(); // In case refresh fails, generate new tokens
//            } catch (IOException e) {
//                logger.error("IO error while loading credentials: {}", e.getMessage(), e);
//            }
//        }
//
//
//
//    }

    @PostConstruct
    public void initYouTubeTokens() {
        try {
            Credential credential = getYoutubeCredential();
            logger.info("YouTube credentials are present and loaded for '{}'", COMPANY_USER_ID);
        } catch (AuthorizationException e) {
            logger.warn("YouTube credentials not found. You must authorize the YouTube account before using this service.");
        }
    }
    @Transactional
    public String uploadVideo(MultipartFile file, String title, String description, String tutorId, String lessonId) {
        try {
            if (file == null || file.isEmpty()) throw new BadRequestException("Uploaded video cannot be empty.");
            if (title == null || title.isBlank()) throw new BadRequestException("Video title is required.");
            if (lessonId == null || lessonId.isBlank()) throw new BadRequestException("Lesson ID is required.");

            // Fetch lesson and validate
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

            lesson.setLessonType(LessonType.VIDEO);

            if (!lesson.getCourse().getTutor().getTutorId().equals(tutorId)) {
                throw new BadRequestException("You are not authorized to upload a video to this course.");
            }

            // Get tutor from lesson's course
            Tutor tutor = lesson.getCourse().getTutor();

            Credential credential = getYoutubeCredential();

            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            HttpRequestInitializer requestInitializer = request -> {
                credential.initialize(request);
                request.setConnectTimeout(60000);
                request.setReadTimeout(60000);
            };

            YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, requestInitializer)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Prepare metadata
            Video videoMetadata = new Video();
            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(title);
            snippet.setDescription(description);
            videoMetadata.setSnippet(snippet);

            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("unlisted");
            videoMetadata.setStatus(status);

            InputStreamContent mediaContent = new InputStreamContent("video/*", file.getInputStream());
            YouTube.Videos.Insert videoInsert = youtube.videos()
                    .insert("snippet,status", videoMetadata, mediaContent);

            Video uploadedVideo = videoInsert.execute();

            // Create and save LectureVideo
            LectureVideo lectureVideo = new LectureVideo();
            lectureVideo.setYoutubeVideoId(uploadedVideo.getId());
            lectureVideo.setYoutubeVideoUrl("https://www.youtube.com/watch?v=" + uploadedVideo.getId()); // <-- Save full URL
            lectureVideo.setTitle(title);
            lectureVideo.setDescription(description);
            lectureVideo.setTutor(tutor);
            lectureVideo.setCourse(lesson.getCourse());
            lectureVideo.setSubject(lesson.getVideo().getSubject());

            if (uploadedVideo.getSnippet().getThumbnails() != null &&
                    uploadedVideo.getSnippet().getThumbnails().getDefault() != null) {
                lectureVideo.setThumbnailUrl(uploadedVideo.getSnippet().getThumbnails().getDefault().getUrl());
            }

            videoRepository.save(lectureVideo);

            // Associate video with lesson
            lesson.setVideo(lectureVideo);
            lessonRepository.save(lesson);

            return uploadedVideo.getId();

        } catch (BadRequestException | AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Video upload failed", e);
            throw new VideoUploadException("Failed to upload video to YouTube: " + e.getMessage(), e);
        }
    }

    public LiveStreamResponse createLiveBroadcast(LiveStreamDTO liveStreamDTO, boolean isOneOnOne) {
        try {
            Course course = courseRepository.findById(liveStreamDTO.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

            if (!course.getTutor().getTutorId().equals(liveStreamDTO.getTutorId())) {
                throw new BadRequestException("You are not authorized to start this live stream.");
            }

            Credential credential = getYoutubeCredential();

            YouTube youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(APPLICATION_NAME).build();

            // 1. Create the live broadcast
            LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
            broadcastSnippet.setTitle(liveStreamDTO.getTitle());
            broadcastSnippet.setDescription(liveStreamDTO.getDescription());
            //broadcastSnippet.setScheduledStartTime());

            LiveBroadcastStatus status = new LiveBroadcastStatus();
            status.setPrivacyStatus("unlisted");

            LiveBroadcastContentDetails contentDetails = new LiveBroadcastContentDetails();
            contentDetails.setRecordFromStart(!isOneOnOne);       // Automatically start recording
            contentDetails.setEnableAutoStart(true);       // Start the broadcast when the ingestion stream starts

            LiveBroadcast broadcast = new LiveBroadcast()
                    .setKind("youtube#liveBroadcast")
                    .setSnippet(broadcastSnippet)
                    .setStatus(status)
                    .setContentDetails(contentDetails);

            YouTube.LiveBroadcasts.Insert broadcastInsert = youtube.liveBroadcasts()
                    .insert("snippet,status", broadcast);
            LiveBroadcast returnedBroadcast = broadcastInsert.execute();

            // 2. Create the stream
            LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
            streamSnippet.setTitle(liveStreamDTO.getTitle() + " Stream");

            CdnSettings cdnSettings = new CdnSettings();
            cdnSettings.setFormat("720p");
            cdnSettings.setIngestionType("rtmp");

            com.google.api.services.youtube.model.LiveStream stream = new com.google.api.services.youtube.model.LiveStream()
                    .setKind("youtube#liveStream")
                    .setSnippet(streamSnippet)
                    .setCdn(cdnSettings);

            YouTube.LiveStreams.Insert streamInsert = youtube.liveStreams()
                    .insert("snippet,cdn", stream);
            com.google.api.services.youtube.model.LiveStream returnedStream = streamInsert.execute();

            // 3. Bind the broadcast to the stream
            YouTube.LiveBroadcasts.Bind bindRequest = youtube.liveBroadcasts()
                    .bind(returnedBroadcast.getId(), "id,contentDetails")
                    .setStreamId(returnedStream.getId());

            bindRequest.execute();

            IngestionInfo ingestionInfo = returnedStream.getCdn().getIngestionInfo();

            String liveStreamId = "";
            if (!isOneOnOne){
                LiveStream liveStream = new LiveStream();
                liveStream.setCourse(course);
                liveStream.setYoutubeBroadcastId(returnedBroadcast.getId());
                liveStream.setYoutubeStreamId(returnedStream.getId());
                liveStream.setIngestionAddress(ingestionInfo.getIngestionAddress());
                liveStream.setStreamKey(ingestionInfo.getStreamName());
                liveStream.setWatchUrl("https://www.youtube.com/watch?v=" + returnedBroadcast.getId());
                liveStream.setTitle(liveStreamDTO.getTitle());
                liveStream.setDescription(liveStreamDTO.getDescription());
                liveStream.setScheduledTime(liveStreamDTO.getStartDateTime());

                LiveStream savedLiveStream = liveStreamRepository.save(liveStream);
                liveStreamId = savedLiveStream.getId();
            }


            // 4. Package and return the result
            return new LiveStreamResponse(
                    returnedBroadcast.getId(),
                    returnedStream.getId(),
                    ingestionInfo.getIngestionAddress(),
                    ingestionInfo.getStreamName(),
                    "https://www.youtube.com/watch?v=" + returnedBroadcast.getId(),
                    liveStreamId

            );

        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create YouTube live broadcast: " + e.getMessage(), e);
            throw new RuntimeException("Error setting up YouTube Live stream", e);
        }
    }

    public record LiveStreamResponse(String broadcastId, String streamId, String rtmpUrl, String streamKey,
                                     String youtubeWatchUrl, String liveStreamId) {
        public String getFullIngestionUrl() {
                return rtmpUrl + "/" + streamKey;
            }
    }
    //Only on Local
    private void generateTokens() {
        try {
            InputStream in = getClass().getResourceAsStream("/credentials/credentials.json");
            if (in == null) {
                throw new RuntimeException("Missing credentials.json in resources/credentials");
            }

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    GsonFactory.getDefaultInstance(), new InputStreamReader(in));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientSecrets,
                    YOUTUBE_SCOPES
            )
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8181)
                    .setCallbackPath("/oauth2callback")
                    .build();

            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                    .authorize(COMPANY_USER_ID);


            logger.info("YouTube account authorized successfully. Credential saved for user: {}", COMPANY_USER_ID);

        } catch (IOException e) {
            logger.error("IO error while authorizing YouTube access: {}", e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            logger.error("Security error initializing YouTube auth: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during OAuth authorization: {}", e.getMessage(), e);
            if (e instanceof TokenResponseException) {
                TokenResponseException tokenEx = (TokenResponseException) e;
                if ("invalid_grant".equals(tokenEx.getDetails().getError())) {
                    // Handle the case where the token is expired or revoked
                    logger.info("Refresh token invalid or revoked. Starting new OAuth flow...");
                    generateTokens(); // Re-authenticate the user by generating new tokens
                }
            }
        }
    }

    public void deleteLiveBroadcast(String broadcastId) {
        try {
            Credential credential = getYoutubeCredential();
            YouTube youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(APPLICATION_NAME).build();

            youtube.videos().delete(broadcastId).execute();
        } catch (Exception e) {
            logger.error("Failed to delete YouTube broadcast: " + broadcastId, e);
        }
    }

    public void stopLiveStream(String broadcastId) {
        try {
            Credential credential = getYoutubeCredential();

            YouTube youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(APPLICATION_NAME).build();

            // 1. Transition the broadcast to 'complete'
            YouTube.LiveBroadcasts.Transition transitionRequest = youtube.liveBroadcasts()
                    .transition("complete", broadcastId, "status,snippet,contentDetails");

            LiveBroadcast completedBroadcast = transitionRequest.execute();

            logger.info("Live stream stopped: {}", completedBroadcast.getId());

        } catch (IOException | GeneralSecurityException e) {
            logger.error("Failed to stop livestream", e);
            throw new RuntimeException("Error stopping YouTube livestream", e);
        }
    }


    private Credential getYoutubeCredential() {
        try {
            InputStream in = getClass().getResourceAsStream("/credentials/credentials.json");
            if (in == null) {
                throw new AuthorizationException("Missing YouTube API credentials.json file.");
            }

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    GsonFactory.getDefaultInstance(), new InputStreamReader(in));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientSecrets,
                    YOUTUBE_SCOPES
            )
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

            Credential credential = flow.loadCredential(COMPANY_USER_ID);
            if (credential == null || credential.getAccessToken() == null) {
                throw new AuthorizationException("YouTube credentials not found or invalid. Please authorize first.");
            }

            if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
                logger.info("Access token expired or near expiry. Refreshing...");
                boolean refreshed = credential.refreshToken();
                if (!refreshed) {
                    throw new AuthorizationException("Failed to refresh YouTube access token.");
                }
            }

            return credential;

        } catch (Exception e) {
            logger.error("Failed to load or Refresh YouTube credentials", e);
            throw new AuthorizationException("Failed to authorize YouTube access: " + e.getMessage(), e);
        }
    }





}
