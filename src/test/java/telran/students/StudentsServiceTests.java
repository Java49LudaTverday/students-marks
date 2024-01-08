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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.MongoTransactionManager;

import telran.exceptions.NotFoundException;
import telran.students.dto.Mark;
import telran.students.dto.Student;
import telran.students.model.StudentDoc;
import telran.students.repo.StudentRepo;
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
	StudentRepo studentsRepo;
	@Autowired
	DbTestCreation dbCreation;
	@MockBean
	MongoTransactionManager transactionManager;

	@BeforeEach
	void setUp() {
		dbCreation.createDB();
	}
//	@Test
//	void transactionManagerTest() {
//		assertNotNull(transactionManager);
//	}
	

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
		StudentDoc newStudentDoc = studentsRepo.findById(normalStudent.id()).orElse(null);
		Student newStudent = new Student(newStudentDoc.getId(), 
				newStudentDoc.getName(), newStudentDoc.getPhone());
		assertEquals(newStudent, actual);
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
	void updatePhoneNormalFlow() {
		String newPhone = "050-1111111";
		Student actual = studentsService.updatePhone(ID_1, newPhone);
		StudentDoc student = studentsRepo.findById(ID_1).orElse(null);
		assertEquals(actual.phone(), newPhone);
		assertEquals(student.getPhone(), actual.phone());
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
		assertNull(studentsRepo.findById(ID_1).orElse(null));
	}
	@Test
	@DisplayName("Service: trying remove not exist student")
	void removeNotExistStudent() {
		assertThrowsExactly(NotFoundException.class, () -> 
		studentsService.removeStudent(ID_8));
		
	}
	@Test
	@DisplayName("Service: get student by phone")
	void getStudentPhoneTest() {
		Student student2 = dbCreation.getStudent(2);
		assertEquals(student2, studentsService.getStudentByPhone(DbTestCreation.PHONE_2));
		assertNull(studentsService.getStudentByPhone("kuku"));
	}
	@Test
	@DisplayName("Service: get students with phone prefix")
	void getStudentsPhonePrefixTest() {
		List<Student> expected = List.of(dbCreation.getStudent(2));
		String phonePrefix = DbTestCreation.PHONE_2.substring(0, 3);
		List<Student> actual = studentsService.getStudentByPhonePrefix(phonePrefix);
		assertIterableEquals(expected, actual);
		assertTrue(studentsService.getStudentByPhonePrefix("kuku").isEmpty());
	}
	@Test
	@DisplayName("Service: get good students ")
	void getStudentsAllGoodMarksTest() {
		List<Student> expected = List.of(dbCreation.getStudent(4), dbCreation.getStudent(6));
		List<Student> actual = studentsService.getStudentsAllGoodMarks(70);
		assertIterableEquals(expected, actual);
		assertTrue(studentsService.getStudentsAllGoodMarks(100).isEmpty());
		
	}
	@Test
	@DisplayName("Service: get students few marks ")
	void getStudentsFewMarksTest() {
		List<Student> expected = List.of(dbCreation.getStudent(2), dbCreation.getStudent(7));
		List<Student> actual = studentsService.getStudentsFewMarks(2);
		assertIterableEquals(expected, actual);
		assertTrue(studentsService.getStudentsFewMarks(0).isEmpty());		
	}		
	@Test
	@DisplayName("Service: getting students having good marks at the given subject")
	void getStudentsAllGoodMarksSubjectTets_existingSubjectAndScore_expectedListOf2 () {
		List<Student> expected = List.of(dbCreation.getStudent(4), dbCreation.getStudent(6));
		List<Student> actual = studentsService.getStudentsAllGoodMarksSubject(dbCreation.SUBJECT_2, 100);
		assertIterableEquals(expected, actual);
		assertTrue(studentsService.getStudentsAllGoodMarksSubject(dbCreation.SUBJECT_2, 0).isEmpty());	
	}
	@Test
	@DisplayName("Service: getting students number of marks in close range [0,1]")
	void getStudentsMarksAmountBetweenTets_closeRange_expectedListOf2 () {
		List<Student> expected = List.of(dbCreation.getStudent(2), dbCreation.getStudent(7));
		List<Student> actual = studentsService.getStudentsMarksAmountBetween(0,1);
		assertIterableEquals(expected, actual);
		assertTrue(studentsService.getStudentsMarksAmountBetween(5,5).isEmpty());	
	}	

}
