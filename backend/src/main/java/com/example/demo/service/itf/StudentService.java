package com.example.demo.service.itf;

import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.Student;
import com.example.demo.exception.DuplicateEntity;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.io.request.StudentRequest;
import com.example.demo.repository.StudentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    StudentRepository studentRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    ModelMapper modelMapper;

    //Create
    public Student createNewStudent(StudentRequest studentRequest) {
        try {
            Student student = modelMapper.map(studentRequest, Student.class);

            // Xác định account nào tạo Student này
            // đã thông qua filter -> thông tin account gửi request này đang được lưu ở security context holder
            Account accountRequest = authenticationService.getCurrentAccount();
            student.setAccount(accountRequest);
            return studentRepository.save(student);
        } catch (Exception e) {
            throw new DuplicateEntity("Duplicate Student Code!!!");
        }
    }

    //Read
    public List<Student> getAllStudent() {
        List<Student> students = studentRepository.findStudentsByIsDeletedFalse();
        return students;
    }

    //Update
    public Student updateStudent(long id, Student student) {
        // Find Student
        Student oldStudent = getStudentById(id);
        //Update Info
        oldStudent.setStudentCode(student.getStudentCode());
        oldStudent.setName(student.getName());
        oldStudent.setScore(student.getScore());
        //Save to db
        return studentRepository.save(oldStudent);
    }

    public Student getStudentById(long id) {
        Student oldStudent = studentRepository.findStudentById(id);
        if (oldStudent == null) throw new EntityNotFoundException("Student not found");
        return oldStudent;
    }

    //Delete
    public Student deleteStudent(long id) {
        Student oldStudent = getStudentById(id);
        oldStudent.setDeleted(true);

        return studentRepository.save(oldStudent);
    }
}
