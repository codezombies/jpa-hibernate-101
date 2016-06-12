package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "classes")
@NamedQuery(name = "Class.findClassByCode", query = "from Class where code = :code")
public class Class {

    @Id
    private String code;

    private String name;

    @ManyToMany
    @JoinTable(name = "class_students", joinColumns = @JoinColumn(name = "class_id"), inverseJoinColumns = @JoinColumn(name = "student_id"))
    private List<Student> students = new ArrayList<>();

    public Class() {
    }

    public Class(final String code, final String name) {
        super();
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(final List<Student> students) {
        this.students = students;
    }

    public void addStudents(final Student... students) {
        final List<Student> studentList = Arrays.asList(students);
        this.students.addAll(studentList);
    }

    @Override
    public String toString() {
        return "Class [code=" + code + ", name=" + name + "]";
    }

}
