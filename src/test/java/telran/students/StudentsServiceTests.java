package telran.students;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import telran.exceptions.NotFoundException;
import telran.students.dto.Mark;
import telran.students.dto.Student;
import telran.students.service.StudentsService;

@SpringBootTest
class StudentsServiceTests {
	final static long ID_8 = 8l;
	final static long ID_1 = 1l;
	private static final Student normalStudent = new Student(ID_8, "name1", "058-1234567");
	private static final Mark newMark = new Mark("subject10", LocalDate.parse("2024-01-30"), 90);
	private static final Student removedStudent = new Student(ID_1, "name1", "050-1234567");
	@Autowired
	StudentsService studentsService;
	@Autowired
	DbTestCreation dbCreation;

	@BeforeEach
	void setUp() {
		dbCreation.createDB();
	}

	@Test
	@DisplayName("Service: get marks with normal id")
	void getMarksTest() {
		Mark[] marksActual = studentsService.getMarks(1).toArray(Mark[]::new);
		Mark[] marksExpected = dbCreation.getStudentMarks(1);
		assertArrayEquals(marksExpected, marksActual);
	}
	@Test
	@DisplayName("Service: get marks for wrong id")
	void getMarksWrongId() {
		long id = 11l;
		assertThrowsExactly(NotFoundException.class, () ->
		studentsService.getMarks(id));
	}
	@Test
	@DisplayName("Service: add normal student")
	void addNormalStudent() {
		Student actual = studentsService.addStudent(normalStudent);
		assertEquals(actual, normalStudent);
		List<Mark> marks = studentsService.getMarks(ID_8);
		assertTrue(marks.isEmpty());
	}
	@Test
	@DisplayName("Service: add student already exist")
	void addStudentAlreadyExist() {
		studentsService.addStudent(normalStudent);
		assertThrowsExactly(IllegalStateException.class, () ->
		studentsService.addStudent(normalStudent)); 
	}
	@Test
	@DisplayName("Service: update phone number for normal student")
	void updatePhoneNOrmalFlow() {
		String newPhone = "050-1111111";
		Student actual = studentsService.updatePhone(ID_1, newPhone);
		assertEquals(actual.phone(), newPhone);
	}
	@Test
	@DisplayName("Service: update phone number for not exist student")
	void updatePhoneWrongId() {
		String newPhone = "050-1111111";
		assertThrowsExactly(NotFoundException.class, () -> 
		studentsService.updatePhone(ID_8, newPhone));
	}
	@Test
	@DisplayName("Service: add mark for normal Student")
	void addMarkForNormalStudent() {
		List<Mark> actual = studentsService.addMark(ID_1, newMark);
		Mark[] marks = dbCreation.getStudentMarks(ID_1);
		Mark[] newMarks = Arrays.copyOf(marks, marks.length+1);
		newMarks[marks.length] = newMark;
		assertArrayEquals(newMarks, actual.toArray());
	}
	@Test
	@DisplayName("Service: add marks for not exist student")
	void addMarkForWrongID() {
		assertThrowsExactly(NotFoundException.class, () -> 
		studentsService.addMark(ID_8, newMark));
	}
	@Test
	@DisplayName("Service: remove normal student")
	void removeNormalStudent() {
		Student actual = studentsService.removeStudent(ID_1);
		assertEquals(actual, removedStudent);
	}
	@Test
	@DisplayName("Service: trying remove not exist student")
	void removeNotExistStudent() {
		assertThrowsExactly(NotFoundException.class, () -> 
		studentsService.removeStudent(ID_8));
		
	}
			
			

}
