package Savina.ftiApp.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. Burime statike ose faqe qe nuk gjenden (404)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoResourceFound(NoResourceFoundException ex) {
        log.debug("Resource not found: {}", ex.getResourcePath());
        return new ErrorResponse("Burimi nuk u gjet: " + ex.getResourcePath(), 404);
    }

    /**
     * 2. Validimi i DTO-ve me @Valid (@NotBlank, @Size, @Email, etc.) (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation error: {}", msg);
        return new ErrorResponse(msg != null ? msg : "Te dhenat e derguara jane te pavlefshme.", 400);
    }

    /**
     * 3. Validimet ne nivel parametri/entiteti me ConstraintViolationException (400)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("Constraint violation: {}", msg);
        return new ErrorResponse(msg.isEmpty() ? "Kufizimet e validimit u shkelen." : msg, 400);
    }

    /**
     * 4. Gabime biznesi te hedhura me IllegalArgumentException (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument error: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage(), 400);
    }

    /**
     * 5. Gabime gjendjeje te pavlefshme me IllegalStateException (400)
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state error: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage(), 400);
    }

    /**
     * 6. Format i pasakte i trupit te kerkeses (JSON i pavlefshem) (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON body: {}", ex.getMessage());
        return new ErrorResponse("Formati i te dhenave te kerkeses eshte i pasakte (JSON i pavlefshem).", 400);
    }

    /**
     * 7. Tip i pasakte parametri ne URL/Query (p.sh. shkronja ne vend te numrit per ID) (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        log.warn("Parameter type mismatch for '{}': {}", paramName, ex.getMessage());
        return new ErrorResponse("Parametri '" + paramName + "' ka format ose tip te pasakte.", 400);
    }

    /**
     * 8. Mungese e nje parametri te detyrueshem ne query (@RequestParam required) (400)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return new ErrorResponse("Parametri i detyrueshem '" + ex.getParameterName() + "' mungon.", 400);
    }

    /**
     * 9. Konflikte ne Databaze (Unique constraints, Foreign Keys) (400)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return new ErrorResponse("Ky veprim nuk mund te kryhet sepse te dhenat jane te lidhura me rekorde te tjera ose ekzistojne tashme ne sistem.", 400);
    }

    /**
     * 10. Akses i ndaluar nga Spring Security (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return new ErrorResponse("Nuk keni te drejta te mjaftueshme per te kryer kete veprim.", 403);
    }

    /**
     * 11. Autentifikim i deshtuar (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return new ErrorResponse("Autentifikimi deshtoi. Kredencialet jane te pasakta ose sesioni ka skaduar.", 401);
    }

    /**
     * 12. Madhesia e skedarit tejkalon limitin (400)
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("File upload size exceeded: {}", ex.getMessage());
        return new ErrorResponse("Madhesia e skedarit tejkalon limitin maksimal te lejuar.", 400);
    }

    /**
     * 13. Metoda HTTP nuk mbeshtetet per endpoint-in (p.sh. POST ne vend te GET) (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMethod());
        return new ErrorResponse("Metoda HTTP '" + ex.getMethod() + "' nuk mbeshtetet per kete rruge.", 405);
    }

    /**
     * 14. RuntimeException biznesi (nga orElseThrow ose kontrolle ne service)
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRuntimeException(RuntimeException ex) {
        log.warn("Runtime business error: {}", ex.getMessage());
        String msg = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : "Ndodhi nje gabim gjate perpunimit te kerkeses.";
        return new ErrorResponse(msg, 400);
    }

    /**
     * 15. Fallback per te gjitha gabimet e papritura te sistemit (500)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        log.error("Unhandled server exception: {}", ex.getMessage(), ex);
        return new ErrorResponse("Ndodhi nje gabim i papritur ne server.", 500);
    }
}
