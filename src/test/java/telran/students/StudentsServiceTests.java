package telran.students;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import telran.exceptions.NotFoundException;
import telran.students.dto.Mark;
import telran.students.service.StudentsService;

@SpringBootTest
class StudentsServiceTests {
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

}
