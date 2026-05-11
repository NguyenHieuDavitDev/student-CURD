package com.example.studentManagements;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.Year;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("currentYear")
    public int currentYear() {
        return Year.now().getValue();
    }
}
