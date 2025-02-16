package net.javaguides.springboot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserGetOneDTO {
    private String full_name;
    private int age;
    private String education;
    private double middle_grade;

    public UserGetOneDTO(String full_name, int age, String education, double middle_grade) {
        this.full_name = full_name;
        this.age = age;
        this.education = education;
        this.middle_grade = middle_grade;
    }
}