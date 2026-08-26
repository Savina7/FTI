package Savina.ftiApp.mapper;

import Savina.ftiApp.dto.requestDTO.ProfessorPreEnrollmentRequest;
import Savina.ftiApp.dto.responseDTO.ProfessorAdminDto;
import Savina.ftiApp.entity.Department;
import Savina.ftiApp.entity.Professor;
import Savina.ftiApp.entity.ProfessorPreEnrollment;
import org.springframework.stereotype.Component;

@Component
public class ProfessorMapper {

    public ProfessorAdminDto toDto(ProfessorPreEnrollment pe) {
        if (pe == null) return null;

        String deptName = pe.getDepartment() != null ? pe.getDepartment().getEmerDepartamenti() : "";

        ProfessorAdminDto dto = new ProfessorAdminDto();
        dto.setId(pe.getPreEnrollmentId());
        dto.setEmri(pe.getEmri());
        dto.setMbiemri(pe.getMbiemri());
        dto.setEmail(pe.getEmail());
        dto.setDepartment(deptName);
        dto.setStatus("PARAREGJISTRUAR");
        return dto;
    }

    public ProfessorAdminDto toDto(Professor p) {
        if (p == null) return null;

        String deptName = p.getDepartment() != null ? p.getDepartment().getEmerDepartamenti() : "";
        String emri = p.getUser() != null ? p.getUser().getEmri() : "";
        String mbiemri = p.getUser() != null ? p.getUser().getMbiemri() : "";
        String email = p.getUser() != null ? p.getUser().getEmail() : "";

        ProfessorAdminDto dto = new ProfessorAdminDto();
        dto.setId(p.getProfessorId());
        dto.setEmri(emri);
        dto.setMbiemri(mbiemri);
        dto.setEmail(email);
        dto.setDepartment(deptName);
        dto.setStatus("VERIFIKUAR");
        return dto;
    }

    public ProfessorPreEnrollment toPreEnrollmentEntity(ProfessorPreEnrollmentRequest req, Department dept) {
        if (req == null) return null;

        ProfessorPreEnrollment pe = new ProfessorPreEnrollment();
        pe.setEmri(req.getEmri() != null ? req.getEmri().trim() : null);
        pe.setMbiemri(req.getMbiemri() != null ? req.getMbiemri().trim() : null);
        pe.setEmail(req.getEmail() != null ? req.getEmail().trim().toLowerCase() : null);
        pe.setDepartment(dept);
        return pe;
    }
}
