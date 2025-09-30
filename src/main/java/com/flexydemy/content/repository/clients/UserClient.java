package com.flexydemy.content.repository.clients;

import com.flexydemy.content.dto.StudentResponse;
import com.flexydemy.content.dto.UserDto;
import com.flexydemy.content.exceptions.ServiceException;
import com.flexydemy.content.model.ClassSchedule;
import com.flexydemy.content.model.Course;
import com.flexydemy.content.model.StudentCourseProgress;
import com.flexydemy.content.repository.ClassScheduleRepository;
import com.flexydemy.content.repository.StudentCourseProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserClient {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    private StudentCourseProgressRepository repo;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Value("${external.user-service.base-url}")
    private String userServiceUrl;

    public Mono<UserDto> findById(String userId, String token) {
        String url = userServiceUrl + "/" + userId;


        return webClientBuilder.build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(UserDto.class)
                .map(userDto -> {
                    // Blocking repository calls
                    List<Course> courses = getCourses(userId);
                    List<StudentCourseProgress> progress = getProgress(userId);
                    List<ClassSchedule> schedules = findEnrolledByUserId(userId);
                    List<String> docIds = findDocumentIdsByUserId(userId);
                    List<String> videoIds = findVideoIdsByUserId(userId);

                    // Enrich UserDto
                    userDto.setCourseList(courses);
                    userDto.setCourseProgressList(progress);
                    userDto.setEnrolledSchedules(schedules);
                    userDto.setResourceDocumentsIds(docIds);
                    userDto.setResourceVideosIds(videoIds);

                    return userDto;
                })
                .subscribeOn(Schedulers.boundedElastic()) // run blocking calls off main thread
                .doOnError(e -> log.error("Failed to fetch user {}: {}", userId, e.getMessage(), e));
    }

    public List<StudentResponse> findAllStudents(String adminId, String token) {
        String url = userServiceUrl + "/students/" + adminId;

        try {
            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToFlux(StudentResponse.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            // Handle exception - log or rethrow
            System.err.println("Error fetching students: " + e.getMessage());
            return Collections.emptyList();
        }
    }



    private List<Course> getCourses (String userId){
        try {
            List<StudentCourseProgress> courseProgressList = repo.findAllByUserId(userId);

            return courseProgressList.stream()
                    .map(StudentCourseProgress::getCourse)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ServiceException("Error retrieving course list for user ID: " + userId +" Error: " +  e.getMessage());
        }

    }

    private List<StudentCourseProgress> getProgress (String userId){
        return repo.findAllByUserId(userId);
    }
    private List<ClassSchedule> findEnrolledByUserId(String userId) {
        try {
            return classScheduleRepository.findByStudentIdsContaining(userId);
        } catch (Exception e) {
            throw new ServiceException("Failed to retrieve class schedules for user ID: " + userId +" Error: " +  e.getMessage());
        }
    }

    private List<String> findDocumentIdsByUserId (String userId){
        List<String> documentIds = new ArrayList<>();
        return documentIds;
    }

    private List<String> findVideoIdsByUserId (String userId){
        List<String> videoIds = new ArrayList<>();
        return videoIds;
    }

}
