package com.flexydemy.content.bootstrap;

import com.flexydemy.content.enums.LessonType;
import com.flexydemy.content.enums.Class_Categories;
import com.flexydemy.content.enums.TutorStatus;
import com.flexydemy.content.model.*;
import com.flexydemy.content.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!prod")
public class CourseInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final TutorRepository tutorRepository;
    private final LessonRepository lessonRepository;

    private final VideoRepository videoRepository;
    private final FlashCardSetRepository flashCardSetRepository;
    private final FlashCardRepository flashCardRepository;

    public CourseInitializer(CourseRepository courseRepository, TutorRepository tutorRepository, LessonRepository lessonRepository, VideoRepository videoRepository, FlashCardSetRepository flashCardSetRepository, FlashCardRepository flashCardRepository) {
        this.courseRepository = courseRepository;
        this.tutorRepository = tutorRepository;
        this.lessonRepository = lessonRepository;
        this.videoRepository = videoRepository;
        this.flashCardSetRepository = flashCardSetRepository;
        this.flashCardRepository = flashCardRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (courseRepository.count() == 0) {
            Tutor defaultTutor = new Tutor();
            defaultTutor.setUserId("722bef21-5078-484f-a9d4-f6f4a321053c");
            defaultTutor.setResumeCollected(true);
            defaultTutor.setStatus(TutorStatus.APPROVED);
            defaultTutor.setVerified(true);

            Tutor savedTutor = tutorRepository.save(defaultTutor);


            List<Course> defaultCourses = List.of(
                    new Course("Biology Basics", "Cell structure and biology foundations",
                            Class_Categories.Science, "Grade 9", true, "5 weeks", 0, 0.0, savedTutor),
                    new Course("Physics Principles", "Newton's laws, motion, and energy",
                            Class_Categories.Science, "Grade 11", true, "6 weeks", 0, 0.0, savedTutor),

                    new Course("Introduction to Algebra", "Linear equations and variables",
                            Class_Categories.Mathematics, "Grade 9", true, "4 weeks", 0, 0.0, savedTutor),
                    new Course("Geometry Fundamentals", "Angles, shapes, and theorems",
                            Class_Categories.Mathematics, "Grade 10", true, "5 weeks", 0, 0.0, savedTutor),

                    new Course("Intro to Programming", "Basics of Python programming",
                            Class_Categories.Technology_and_Computer_Science, "Grade 10", true, "6 weeks", 0, 0.0, savedTutor),
                    new Course("Web Development", "HTML, CSS, and JavaScript essentials",
                            Class_Categories.Technology_and_Computer_Science, "Grade 11", true, "6 weeks", 0, 0.0, savedTutor),

                    new Course("STEM Lab Experiments", "Hands-on integrated STEM projects",
                            Class_Categories.STEM, "Grade 11", true, "7 weeks", 0, 0.0, savedTutor),
                    new Course("Engineering Design", "Bridge design, circuits, and robotics",
                            Class_Categories.STEM, "Grade 12", true, "8 weeks", 0, 0.0, savedTutor),

                    new Course("Marketing 101", "Introduction to marketing principles and branding",
                            Class_Categories.Business_and_Commerce, "Grade 12", true, "6 weeks", 0, 0.0, savedTutor),
                    new Course("Entrepreneurship", "Building and pitching a business idea",
                            Class_Categories.Business_and_Commerce, "Grade 11", true, "6 weeks", 0, 0.0, savedTutor),

                    new Course("Psychology Basics", "Introduction to human behavior and mental processes",
                            Class_Categories.Social_Sciences, "Grade 10", true, "5 weeks", 0, 0.0, savedTutor),
                    new Course("Sociology and Society", "Understanding groups and cultures",
                            Class_Categories.Social_Sciences, "Grade 12", true, "5 weeks", 0, 0.0, savedTutor),

                    new Course("Human Anatomy", "Study of major body systems and functions",
                            Class_Categories.Health_and_Life_Sciences, "Grade 11", true, "8 weeks", 0, 0.0, savedTutor),
                    new Course("Nutrition and Wellness", "Healthy living, food science, and diet",
                            Class_Categories.Health_and_Life_Sciences, "Grade 9", true, "6 weeks", 0, 0.0, savedTutor),

                    new Course("Automotive Basics", "Intro to car maintenance and engine systems",
                            Class_Categories.Vocational_and_Technical, "Grade 12", true, "9 weeks", 0, 0.0, savedTutor),
                    new Course("Construction Fundamentals", "Building materials and safety",
                            Class_Categories.Vocational_and_Technical, "Grade 11", true, "7 weeks", 0, 0.0, savedTutor),

                    new Course("Philosophy and Ethics", "Explore logic, ethics, and moral reasoning",
                            Class_Categories.Arts_and_Humanities, "Grade 12", true, "4 weeks", 0, 0.0, savedTutor),
                    new Course("World History and Art", "Cultural heritage and visual arts",
                            Class_Categories.Arts_and_Humanities, "Grade 10", true, "6 weeks", 0, 0.0, savedTutor),

                    new Course("Environmental Studies", "Blending science and social sciences for climate understanding",
                            Class_Categories.Interdisciplinary, "Grade 10", true, "6 weeks", 0, 0.0, savedTutor),
                    new Course("Innovation & Society", "Interdisciplinary look at invention and impact",
                            Class_Categories.Interdisciplinary, "Grade 11", true, "7 weeks", 0, 0.0, savedTutor),

                    new Course("Create Your Own Course", "Design your custom learning experience",
                            Class_Categories.Custom, "Grade 11", true, "Self-paced", 0, 0.0, savedTutor),
                    new Course("Flexible Study Module", "Tailor content based on your needs",
                            Class_Categories.Custom, "Grade 12", true, "Self-paced", 0, 0.0, savedTutor)
            );

            List<Lesson> lessonsToSave = new ArrayList<>();
            List<LectureVideo> lectureVideosToSave = new ArrayList<>();
            List<FlashCardSet> flashCardSetsToSave = new ArrayList<>();
            List<FlashCard> flashCardsToSave = new ArrayList<>();

            courseRepository.saveAll(defaultCourses).forEach(course -> {
                Lesson lesson = new Lesson();
                lesson.setCourse(course);
                lesson.setTitle("Introduction to " + course.getCourseTitle());
                lesson.setAbout("This is an introductory lesson for the course " + course.getCourseTitle() + ".");
                lesson.setTranscript("");
                lesson.setNotes("");
                lesson.setLessonType(LessonType.TEXT); // Assuming text by default
                lesson.setSequenceNumber(1);
                lesson.setDuration("10 minutes");
                lesson.setQuizNeeded(false);
                lesson.setPassingScore(0);
                lesson.setAverageRating(0.0);

                LectureVideo lectureVideo = new LectureVideo();
                lectureVideo.setTitle("Introduction to " + course.getCourseTitle());
                lectureVideo.setYoutubeVideoId("ZORqU7cSaPo");
                lectureVideo.setDescription("This is an introductory lesson for the course " + course.getCourseTitle() + ".");
                lectureVideo.setSubject(lesson.getCourse().getSubject());
                lectureVideo.setThumbnailUrl("https://i.ytimg.com/vi/ZORqU7cSaPo/default.jpg");
                lectureVideo.setUploadedBy("Mock Admin");
                lectureVideo.setTutor(savedTutor);
                lectureVideo.setCourse(course);

                lesson.setVideo(lectureVideo);



                lessonsToSave.add(lesson);
                lectureVideosToSave.add(lectureVideo);
            });

            tutorRepository.save(savedTutor);
            lessonRepository.saveAll(lessonsToSave);
            videoRepository.saveAll(lectureVideosToSave);

            for (Course course : defaultCourses) {
                FlashCardSet flashCardSet = new FlashCardSet();
                flashCardSet.setCourse(course);
                flashCardSet.setTitle("Flashcards for " + course.getCourseTitle());
                flashCardSet.setSubject(course.getSubject());
                flashCardSet.setCreatedByTutor(savedTutor);

                // Select cards depending on subject
                List<FlashCard> cardsForThisCourse = new ArrayList<>();

                switch (course.getCourseTitle()) {
                    case "Biology Basics" -> {
                        FlashCard c1 = new FlashCard(flashCardSet, "Cells are the smallest unit of life.", "True", "TRUE_FALSE", List.of("True", "False"));
                        FlashCard c2 = new FlashCard(flashCardSet, "Which organelle is the powerhouse of the cell?", "Mitochondria", "OPTIONS", List.of("Nucleus", "Mitochondria", "Ribosome", "Chloroplast"));
                        cardsForThisCourse.addAll(List.of(c1, c2));
                    }
                    case "Physics Principles" -> {
                        FlashCard c3 = new FlashCard(flashCardSet, "Newton’s First Law is also known as the law of inertia.", "True", "TRUE_FALSE", List.of("True", "False"));
                        FlashCard c4 = new FlashCard(flashCardSet, "What is the SI unit of force?", "Newton", "OPTIONS", List.of("Joule", "Watt", "Newton", "Pascal"));
                        cardsForThisCourse.addAll(List.of(c3, c4));
                    }
                    case "Introduction to Algebra" -> {
                        FlashCard c5 = new FlashCard(flashCardSet, "x + 2 = 5, then x = ?", "3", "OPTIONS", List.of("2", "3", "5", "7"));
                        FlashCard c6 = new FlashCard(flashCardSet, "An equation always has only one solution.", "False", "TRUE_FALSE", List.of("True", "False"));
                        cardsForThisCourse.addAll(List.of(c5, c6));
                    }
                    case "Intro to Programming" -> {
                        FlashCard c7 = new FlashCard(flashCardSet, "Python uses indentation to define code blocks.", "True", "TRUE_FALSE", List.of("True", "False"));
                        FlashCard c8 = new FlashCard(flashCardSet, "Which symbol is used for comments in Python?", "#", "OPTIONS", List.of("//", "#", "/* */", "--"));
                        cardsForThisCourse.addAll(List.of(c7, c8));
                    }
                    case "World History and Art" -> {
                        FlashCard c9 = new FlashCard(flashCardSet, "The Renaissance began in Italy.", "True", "TRUE_FALSE", List.of("True", "False"));
                        FlashCard c10 = new FlashCard(flashCardSet, "Which artist painted the Mona Lisa?", "Leonardo da Vinci", "OPTIONS", List.of("Michelangelo", "Leonardo da Vinci", "Raphael", "Donatello"));
                        cardsForThisCourse.addAll(List.of(c9, c10));
                    }
                    case "Human Anatomy" -> {
                        FlashCard c11 = new FlashCard(flashCardSet, "The heart has four chambers.", "True", "TRUE_FALSE", List.of("True", "False"));
                        FlashCard c12 = new FlashCard(flashCardSet, "Which organ is responsible for filtering blood?", "Kidney", "OPTIONS", List.of("Lungs", "Liver", "Kidney", "Stomach"));
                        cardsForThisCourse.addAll(List.of(c11, c12));
                    }
                    case "Marketing 101" -> {
                        FlashCard c13 = new FlashCard(flashCardSet, "The 4 Ps of marketing include Product, Price, Place, and Promotion.", "True", "TRUE_FALSE", List.of("True", "False"));
                        FlashCard c14 = new FlashCard(flashCardSet, "Market segmentation is dividing a market into distinct groups of...", "Consumers", "OPTIONS", List.of("Products", "Brands", "Consumers", "Services"));
                        cardsForThisCourse.addAll(List.of(c13, c14));
                    }
                    case "Psychology Basics" -> {
                        FlashCard c15 = new FlashCard(flashCardSet, "Sigmund Freud is associated with psychoanalysis.", "True", "TRUE_FALSE", List.of("True", "False"));
                        cardsForThisCourse.add(c15);
                    }
                }

                if (!cardsForThisCourse.isEmpty()) {
                    flashCardSetsToSave.add(flashCardSet);
                    flashCardsToSave.addAll(cardsForThisCourse);
                }
            }

            // Finally persist
            flashCardSetRepository.saveAll(flashCardSetsToSave);
            flashCardRepository.saveAll(flashCardsToSave);
            System.out.println("20+ default courses inserted.");
        } else {
            System.out.println("Courses already exist. Skipping initialization.");
        }
    }

}
