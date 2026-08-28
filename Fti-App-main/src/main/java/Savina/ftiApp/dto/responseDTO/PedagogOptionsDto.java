package Savina.ftiApp.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedagogOptionsDto {
    private String professorName;
    private String professorEmail;
    private List<String> academicYears;
    private List<String> types;
    private List<DepartmentOptionDto> departments;
    private List<CourseOptionDto> courses;
    private List<ClassOptionDto> classes;
    private List<ProgramOptionDto> programs;
    private Boolean isLektor;
    private Boolean hasRegistry;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentOptionDto {
        private Integer id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseOptionDto {
        private Integer id;
        private String name;
        private Integer studyYear;
        private Integer departmentId;
        private List<Integer> departmentIds;
        private String programName;
        private List<String> branches;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassOptionDto {
        private Integer id;
        private String name;
        private Integer studyYear;
        private Integer courseId;
        private List<Integer> courseIds;
        private Integer departmentId;
        private Integer programId;
        private String roleType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgramOptionDto {
        private Integer id;
        private String name;
        private Integer departmentId;
    }
}

