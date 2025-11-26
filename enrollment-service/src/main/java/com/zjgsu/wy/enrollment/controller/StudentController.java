package com.zjgsu.wy.enrollment.controller;

import com.zjgsu.wy.enrollment.common.ApiResponse;
import com.zjgsu.wy.enrollment.model.Student;
import com.zjgsu.wy.enrollment.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生控制器
 */
@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {
    
    @Autowired
    private StudentService studentService;

    /**
     * 查询所有学生
     * GET /api/students
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        List<Student> students = studentService.findAll();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    /**
     * 根据ID查询学生
     * GET /api/students/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable String id) {
        Student student = studentService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    /**
     * 创建学生
     * POST /api/students
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Student>> createStudent(@Valid @RequestBody Student student) {
        Student createdStudent = studentService.create(student);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("学生创建成功", createdStudent));
    }

    /**
     * 更新学生信息
     * PUT /api/students/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> updateStudent(
            @PathVariable String id, 
            @Valid @RequestBody Student student) {
        Student updatedStudent = studentService.update(id, student);
        return ResponseEntity.ok(ApiResponse.success("学生信息更新成功", updatedStudent));
    }

    /**
     * 删除学生
     * DELETE /api/students/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteStudent(@PathVariable String id) {
        studentService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("学生删除成功", null));
    }
}
