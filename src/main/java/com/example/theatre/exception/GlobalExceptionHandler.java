package com.example.theatre.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Глобальный обработчик исключений
 * Обрабатывает все исключения приложения и возвращает пользователю понятные сообщения об ошибках
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleRuntimeException(RuntimeException ex) {
        logger.error("RuntimeException occurred: ", ex);
        ModelAndView mav = new ModelAndView("error");
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            // Try to get a more specific message from the exception
            if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                message = ex.getCause().getMessage();
            } else {
                message = "Произошла ошибка при выполнении операции";
            }
        }
        mav.addObject("errorMessage", message);
        mav.addObject("errorType", "RuntimeException");
        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("IllegalArgumentException occurred: ", ex);
        ModelAndView mav = new ModelAndView("error");
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "Некорректные данные запроса";
        }
        mav.addObject("errorMessage", message);
        mav.addObject("errorType", "IllegalArgumentException");
        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("AccessDeniedException occurred: ", ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", "У вас нет прав доступа к этому ресурсу");
        mav.addObject("errorType", "AccessDeniedException");
        return mav;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoHandlerFoundException(NoHandlerFoundException ex) {
        logger.warn("NoHandlerFoundException occurred: ", ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", "Запрашиваемая страница не найдена");
        mav.addObject("errorType", "NotFound");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception ex) {
        logger.error("Unexpected exception occurred: ", ex);
        ModelAndView mav = new ModelAndView("error");
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            if (ex.getCause() != null && ex.getCause().getMessage() != null) {
                message = ex.getCause().getMessage();
            } else {
                message = "Произошла непредвиденная ошибка. Пожалуйста, попробуйте позже.";
            }
        }
        mav.addObject("errorMessage", message);
        mav.addObject("errorType", "Exception");
        return mav;
    }
}

