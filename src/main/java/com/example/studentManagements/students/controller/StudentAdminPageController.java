package com.example.studentManagements.students.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/students")
public class StudentAdminPageController {

    @ModelAttribute("currentMenu")
    public String currentMenu() {
        return "students";
    }

    @GetMapping({"", "/"})
    public String index() {
        return "admin/students/index";
    }
}
