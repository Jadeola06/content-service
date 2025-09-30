package com.flexydemy.content.utils;

import com.flexydemy.content.dto.School;
import com.flexydemy.content.dto.UpdateTutorDTO;
import com.flexydemy.content.enums.Class_Categories;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;




import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeParser {

    public String parseResume(MultipartFile file) throws IOException {
        String content = "";

        if (file.getOriginalFilename().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                content = pdfStripper.getText(document);
            }
        } else if (file.getOriginalFilename().endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                content = extractor.getText();
            }
//        } else if (file.getOriginalFilename().endsWith(".doc")) {
//            try (HWPFDocument doc = new HWPFDocument(file.getInputStream())) {
//                WordExtractor extractor = new WordExtractor(doc);
//                content = extractor.getText();
//            }
        } else {
            throw new IllegalArgumentException("Unsupported file format");
        }

        return content;
    }

    public UpdateTutorDTO extractTutorInfo(String content) {
        UpdateTutorDTO dto = new UpdateTutorDTO();
        dto.setAreaOfExpertise(extractExpertise(content));
        dto.setSchools(extractUniversities(content));
        dto.setQualifications(extractQualifications(content)); // ← Add this line
        return dto;
    }

    private List<Class_Categories> extractExpertise(String content) {
        List<Class_Categories> matched = new ArrayList<>();
        String lowerContent = content.toLowerCase();

        Map<Class_Categories, List<String>> keywords = Map.of(
                Class_Categories.Science, List.of("biology", "chemistry", "physics", "science"),
                Class_Categories.Mathematics, List.of("mathematics", "math", "algebra", "geometry", "calculus"),
                Class_Categories.Technology_and_Computer_Science, List.of("computer science", "software", "java", "python", "ai", "machine learning"),
                Class_Categories.Business_and_Commerce, List.of("business", "finance", "commerce", "accounting"),
                Class_Categories.Health_and_Life_Sciences, List.of("medicine", "nursing", "pharmacy", "health", "life science"),
                Class_Categories.Social_Sciences, List.of("psychology", "sociology", "anthropology", "social science"),
                Class_Categories.Arts_and_Humanities, List.of("literature", "philosophy", "history", "arts", "humanities"),
                Class_Categories.Vocational_and_Technical, List.of("mechanic", "electrician", "plumbing", "technical", "vocational"),
                Class_Categories.STEM, List.of("stem", "engineering", "robotics", "tech"),
                Class_Categories.Interdisciplinary, List.of("interdisciplinary", "multidisciplinary")
        );

        for (Map.Entry<Class_Categories, List<String>> entry : keywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerContent.contains(keyword.toLowerCase())) {
                    matched.add(entry.getKey());
                    break; // avoid duplicates
                }
            }
        }

        return matched;
    }

    private List<String> extractQualifications(String content) {
        List<String> qualifications = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");

        // Basic pattern: e.g. "Bachelor of Science in Physics", "Diploma in Fine Arts"
        Pattern qualificationPattern = Pattern.compile(
                "(?i)(bachelor|master|phd|diploma|certificate|certification|associate degree|b\\.sc|m\\.sc|b\\.a|m\\.a)[^\\n,.;]*"
        );

        for (String line : lines) {
            Matcher matcher = qualificationPattern.matcher(line);
            while (matcher.find()) {
                String match = matcher.group().trim();
                if (!qualifications.contains(match)) {
                    qualifications.add(match);
                }
            }
        }

        return qualifications.stream()
                .map(q -> Arrays.stream(q.split(" "))
                        .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase())
                        .collect(Collectors.joining(" ")))
                .toList();
    }

    private List<School> extractUniversities(String content) {
        List<School> universities = new ArrayList<>();

        // Split by line for easier parsing
        String[] lines = content.split("\\r?\\n");
        Pattern uniPattern = Pattern.compile("(?i).*(university|college).*");
        Pattern yearPattern = Pattern.compile("(\\d{4}).*?(\\d{4})");
        Pattern degreePattern = Pattern.compile("(?i)(Bachelor|Master|PhD|Diploma|Associate|B\\.?Sc|M\\.?Sc|B\\.A|M\\.A)");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (uniPattern.matcher(line).matches()) {
                School uni = new School();
                uni.setName(line.trim());

                // Look ahead for degree and year info
                for (int j = i + 1; j <= i + 3 && j < lines.length; j++) {
                    Matcher yearMatcher = yearPattern.matcher(lines[j]);
                    if (yearMatcher.find()) {
                        uni.setFromYear(yearMatcher.group(1));
                        uni.setToYear(yearMatcher.group(2));
                    }

                    Matcher degreeMatcher = degreePattern.matcher(lines[j]);
                    if (degreeMatcher.find()) {
                        uni.setCertification(degreeMatcher.group(1));
                    }
                }

                universities.add(uni);
            }
        }

        return universities;
    }

}

