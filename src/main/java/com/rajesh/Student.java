package com.rajesh;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Student {
	
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private String name;
  private String course;
  
//No-arg constructor — JPA/Hibernate REQUIRES this
  public Student() {
	super();
	// TODO Auto-generated constructor stub
  }
  public Student(String name, String course) {
	super();
	this.name = name;
	this.course = course;
  }
  public int getId() {
	return id;
  }
  public void setId(int id) {
	this.id = id;
  }
  public String getName() {
	return name;
  }
  public void setName(String name) {
	this.name = name;
  }
  public String getCourse() {
	return course;
  }
  public void setCourse(String course) {
	this.course = course;
  }
  
  
  
}
