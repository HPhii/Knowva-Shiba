package com.example.demo.repository;

import com.example.demo.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Student findStudentById(long id);

    //Lấy danh sách sinh viên chưa bị delete
    //findStudentsByIsDeletedFalseAndScore...();
    // ByIsDeletedFalse = "WHERE isDeleted=False"
    List<Student> findStudentsByIsDeletedFalse();

}
