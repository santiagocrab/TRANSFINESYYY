package com.transfinesy.web;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorMessage", "Page not found");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorMessage", "Internal server error");
                if (exception != null) {
                    Throwable ex = (Throwable) exception;
                    model.addAttribute("exception", ex.getClass().getName());
                    model.addAttribute("exceptionMessage", ex.getMessage());

                    StringBuilder stackTrace = new StringBuilder();
                    for (StackTraceElement element : ex.getStackTrace()) {
                        stackTrace.append(element.toString()).append("\n");
                    }
                    model.addAttribute("stackTrace", stackTrace.toString());
                }
            }
        }

        model.addAttribute("pageTitle", "Error");
        return "error";
    }
}

