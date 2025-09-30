package com.flexydemy.content.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Class_Categories {
    Arts_and_Humanities("Arts and Humanities"),
    Science("Science"),
    Mathematics("Mathematics"),
    Technology_and_Computer_Science("Technology and Computer Science"),
    STEM("STEM"),
    Business_and_Commerce("Business and Commerce"),
    Social_Sciences("Social Sciences"),
    Health_and_Life_Sciences("Health and Life Sciences"),
    Vocational_and_Technical("Vocational and Technical"),
    Interdisciplinary("Interdisciplinary"),
    Custom("Custom");

    private final String name;

    Class_Categories(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getAllNames() {
        return Arrays.stream(Class_Categories.values())
                .map(Class_Categories::getName)
                .collect(Collectors.toList());
    }
}
