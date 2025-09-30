package com.flexydemy.content.dto;


import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.enums.ExamType;
import com.flexydemy.content.model.ClassSchedule;
import com.flexydemy.content.model.Course;
import com.flexydemy.content.model.ProfileRole;
import com.flexydemy.content.model.StudentCourseProgress;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString()
public class UserDto {
    private String userId;
    private String tutorId;

    private String email;
    private String bio;

    private String username;

    private String firstName;
    private String lastName;

    private String description;

    private String profileImageUrl;

    private String phoneNumber;
    private String address;

    private String gender;
    private LocalDate dob;

    private String country;
    private String city;
    private String state;


    private String language;

    private List<Course> courseList;
    private double rating;

    private Class_Categories classCategory;
    private ExamType exam;
    private Set<ProfileRole> role;

    private List<StudentCourseProgress> courseProgressList;

    private List<String> resourceDocumentsIds;
    
    private List<String> resourceVideosIds;

    private List<ClassSchedule> enrolledSchedules;


}
