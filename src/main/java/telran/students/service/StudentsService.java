package telran.students.service;

import java.util.List;

import telran.students.dto.*;

public interface StudentsService {
Student addStudent(Student student);
Student updatePhone(long id, String phone);
List<Mark> addMark (long id, Mark mark);
Student removeStudent (long id);
List<Mark> getMarks (long id);
Student getStudentByPhone(String phoneNumber);
List<Student> getStudentByPhonePrefix(String phonePrefix);
List<Student> getStudentsAllGoodMarks(int thresholdScore);
List<Student> getStudentsFewMarks(int thresholdMarks);
}
