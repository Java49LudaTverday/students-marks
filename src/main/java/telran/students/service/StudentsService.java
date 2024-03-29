package telran.students.service;

import java.time.LocalDate;
import java.util.List;

import telran.students.dto.*;

public interface StudentsService {
	Student addStudent(Student student);

	Student updatePhone(long id, String phone);

	List<Mark> addMark(long id, Mark mark);

	Student removeStudent(long id);

	List<Mark> getMarks(long id);

	Student getStudentByPhone(String phoneNumber);

	List<Student> getStudentByPhonePrefix(String phonePrefix);

	List<Student> getStudentsAllGoodMarks(int thresholdScore);

	List<Student> getStudentsFewMarks(int thresholdMarks);

	/************************************************************************************/
//getting students who have at least one score of a given subject and all scores of that subject
//greater than or equal a given threshold
	List<Student> getStudentsAllGoodMarksSubject(String subject, int thresholdScore);

	/*********************************************************************************/
//getting students having number of marks in a closed range of the given values
//nMarks >= min && nMarks <= max
	List<Student> getStudentsMarksAmountBetween(int min, int max);

//CH#75
	List<Mark> getStudentSubjectMarks(long id, String subject);

	List<NameAvgScore> getStudentAvgScore(int avgScoreThreshold);

//HW #75
	List<Mark> getStudentMarksAtDates(long id, LocalDate from, LocalDate to);

	List<String> getBestStudents(int nStudents);

	List<String> getWorstStudents(int nStudents);
}
