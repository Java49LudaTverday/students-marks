package telran.students;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import telran.students.repo.StudentRepo;
import telran.students.dto.*;
import telran.students.model.*;

@Component
@RequiredArgsConstructor
public class DbTestCreation {

	final StudentRepo studentRepo;
//*************************
	final static long ID_1 = 1l;
	final static String NAME_1 = "name1";
	final static String PHONE_1 = "050-1234567";
//**************************
	final static long ID_2 = 2l;
	final static String NAME_2 = "name2";
	final static String PHONE_2 = "052-1234567";
//*****************************
	final static long ID_3 = 3l;
	final static String NAME_3 = "name3";
	final static String PHONE_3 = "053-1234567";
//**************************
	final static long ID_4 = 4l;
	final static String NAME_4 = "name4";
	final static String PHONE_4 = "054-1234567";
//***************************
	final static long ID_5 = 5l;
	final static String NAME_5 = "name5";
	final static String PHONE_5 = "055-1234567";
//***************************
	final static long ID_6 = 6l;
	final static String NAME_6 = "name6";
	final static String PHONE_6 = "056-1234567";
//***************************
	final static long ID_7 = 7l;
	final static String NAME_7 = "name7";
	final static String PHONE_7 = "057-1234567";
//***************************
	final static String SUBJECT_1 = "subject1";
	final static String SUBJECT_2 = "subject2";
	final static String SUBJECT_3 = "subject3";
	final static String SUBJECT_4 = "subject4";
//****************************
	private static final LocalDate DATE_1 = LocalDate.parse("2023-10-20");
	private static final LocalDate DATE_2 = LocalDate.parse("2023-11-20");
	private static final LocalDate DATE_3 = LocalDate.parse("2023-12-20");
	private static final LocalDate DATE_4 = LocalDate.parse("2024-01-20");
//****************************
	Student[] students = { new Student(ID_1, NAME_1, PHONE_1), 
			new Student(ID_2, NAME_2, PHONE_2),
			new Student(ID_3, NAME_3, PHONE_3), 
			new Student(ID_4, NAME_4, PHONE_4), 
			new Student(ID_5, NAME_5, PHONE_5),
			new Student(ID_6, NAME_6, PHONE_6), 
			new Student(ID_7, NAME_7, PHONE_7) };
//****************************
	Mark[][] marks = {
			{
				//ID1
				new Mark(SUBJECT_1, DATE_1, 80),
				new Mark(SUBJECT_1, DATE_2, 90),
				new Mark(SUBJECT_2, DATE_2, 70)
				
			},
			{
				//ID2
				new Mark(SUBJECT_3, DATE_1, 70)
			},
			{
				//ID3
				new Mark(SUBJECT_1, DATE_1, 80),
				new Mark(SUBJECT_4, DATE_3, 70)
			},
			{
				//ID4
				new Mark(SUBJECT_2, DATE_1, 100),
				new Mark(SUBJECT_3, DATE_3, 90),
				new Mark(SUBJECT_4, DATE_4, 90)
			},
			{
				//ID5
				new Mark(SUBJECT_1, DATE_1, 70),
				new Mark(SUBJECT_3, DATE_2, 70)				
			},
			{
				//ID6
				new Mark(SUBJECT_1, DATE_1, 100),
				new Mark(SUBJECT_2, DATE_2, 100),
				new Mark(SUBJECT_3, DATE_3, 100),
				new Mark(SUBJECT_4, DATE_4, 100)
			},
			{
				//ID7
				//haven`t scores				
			}
	};
	
	public void createDB () {
		studentRepo.deleteAll();
		List<StudentDoc> studentDocs = IntStream.range(0, students.length)
		.mapToObj(this::indexToStudent).toList();
		studentRepo.saveAll(studentDocs);

	}
	public Mark[] getStudentMarks(long id) {
		return marks[(int)(id - 1)];
	}
	StudentDoc indexToStudent(int index) {
		StudentDoc res = StudentDoc.of(students[index]);
		for(Mark mark: marks[index]) {
			res.addMark(mark);
		}
		return res;
	}
	public Student getStudent(long id) {
		return students[(int) (id -1)];
	}
}
