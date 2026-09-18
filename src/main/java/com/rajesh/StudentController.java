package com.rajesh;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {
    @Autowired
	private StudentRepository repository;
    
    @PostMapping
    public Student addStudent(@RequestBody Student student)
    {
        return repository.save(student);	
    }
    
    @GetMapping
    public List<Student> getAllStudents()
    {
    	return repository.findAll();
    }
    
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable int id)
    {
    	return repository.findById(id).orElse(null);
    }
    
    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable int id,@RequestBody Student update)
    {
    	update.setId(id);
    	return repository.save(update);
    }
    // DELETE
    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable int id) {
        repository.deleteById(id);
        return "Deleted student with id " + id;
    }
   
}
