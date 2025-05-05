package com.example.demo.api;

import com.example.demo.model.entity.Student;
import com.example.demo.model.io.request.StudentRequest;
import com.example.demo.service.itf.StudentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class StudentAPI {

    @Autowired
    StudentService studentService;
    //add new student
    // /api/student/ => POST
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity createStudent(@Valid @RequestBody StudentRequest student) {
        Student newStudent = studentService.createNewStudent(student);
        return ResponseEntity.ok(newStudent);
    }

    //get current student list
    // api/student/ => GET
    @GetMapping
    public ResponseEntity getAllStudent() {
        List<Student> students = studentService.getAllStudent();
        return ResponseEntity.ok(students);
    }

    @PutMapping("{studentID}")
    public ResponseEntity update(@PathVariable long studentID,@Valid @RequestBody Student student) {
        Student updatedStudent = studentService.updateStudent(studentID, student);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("{studentID}")
    public ResponseEntity delete(@PathVariable long studentID) {
        Student deletedStudent = studentService.deleteStudent(studentID);
        return ResponseEntity.ok(deletedStudent);
    }
}
