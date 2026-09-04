package Savina.ftiApp.mapper;

import Savina.ftiApp.dto.responseDTO.StudentAttendanceDto;
import Savina.ftiApp.dto.responseDTO.StudentGradeDto;
import Savina.ftiApp.dto.responseDTO.StudentProfileDto;
import Savina.ftiApp.entity.Student;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class StudentMapper {

    public StudentProfileDto toEmptyProfileDto() {
        return StudentProfileDto.builder()
                .studentId(null)
                .emriMbiemri("Student i panjohur")
                .email("")
                .nrMatrikulimit("")
                .programiStudimit("")
                .vitiAkademik("")
                .totalKredite(0)
                .mesatarjaPonderuar(0.0)
                .mesatarjaFormatted("0.00")
                .build();
    }

    public StudentProfileDto toStudentProfileDto(Student student, Integer totalKredite, double mesatarja, String mesatarjaFormatted) {
        if (student == null) {
            return toEmptyProfileDto();
        }

        String emriMbiemri = "";
        String email = "";
        if (student.getUser() != null) {
            String emri = student.getUser().getEmri() != null ? student.getUser().getEmri() : "";
            String mbiemri = student.getUser().getMbiemri() != null ? student.getUser().getMbiemri() : "";
            emriMbiemri = (emri + " " + mbiemri).trim();
            email = student.getUser().getEmail() != null ? student.getUser().getEmail() : "";
        }

        String programiStudimit = "";
        if (student.getProgram() != null && student.getProgram().getSpecializimi() != null) {
            programiStudimit = student.getProgram().getSpecializimi();
        } else if (student.getProgram() != null && student.getProgram().getDepartment() != null && student.getProgram().getDepartment().getEmerDepartamenti() != null) {
            programiStudimit = student.getProgram().getDepartment().getEmerDepartamenti();
        }

        String vitiAkademik = student.getVitStudimit() != null ? "Viti " + student.getVitStudimit() : "Viti 1";

        return StudentProfileDto.builder()
                .studentId(student.getStudentId())
                .emriMbiemri(emriMbiemri)
                .email(email)
                .nrMatrikulimit(student.getNrMatrikulimit() != null ? student.getNrMatrikulimit() : "")
                .programiStudimit(programiStudimit)
                .vitiAkademik(vitiAkademik)
                .totalKredite(totalKredite != null ? totalKredite : 0)
                .mesatarjaPonderuar(mesatarja)
                .mesatarjaFormatted(mesatarjaFormatted)
                .build();
    }

    public StudentGradeDto toStudentGradeDto(
            Integer gradeId,
            Integer courseId,
            String emriLende,
            Integer kredite,
            String nota,
            Double notaValue,
            Integer studyYear,
            String status,
            String dateGiven) {
        return StudentGradeDto.builder()
                .gradeId(gradeId)
                .courseId(courseId)
                .emriLende(emriLende)
                .kredite(kredite)
                .nota(nota)
                .notaValue(notaValue)
                .studyYear(studyYear)
                .status(status)
                .dateGiven(dateGiven)
                .build();
    }

    public String formatDateAlbanian(LocalDate date) {
        if (date == null) return "-";
        try {
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return date.toString();
        }
    }

    public StudentAttendanceDto toStudentAttendanceDto(
            Integer courseId,
            String emriLende,
            int semCount,
            int semTotal,
            String semStatus,
            List<String> semDates,
            int labCount,
            int labTotal,
            String labStatus,
            List<String> labDates) {
        return StudentAttendanceDto.builder()
                .courseId(courseId)
                .emriLende(emriLende)
                .seminareAbsencesCount(semCount)
                .seminareTotalHours(semTotal)
                .seminareStatus(semStatus)
                .seminareDates(semDates)
                .laboratoreAbsencesCount(labCount)
                .laboratoreTotalHours(labTotal)
                .laboratoreStatus(labStatus)
                .laboratoreDates(labDates)
                .build();
    }
}
