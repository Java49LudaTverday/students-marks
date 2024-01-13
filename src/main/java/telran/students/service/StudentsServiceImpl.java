package telran.students.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.BucketOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.students.service.StudentsService;
import telran.exceptions.NotFoundException;
import telran.students.dto.*;
import telran.students.model.StudentDoc;
import telran.students.repo.StudentRepo;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentsServiceImpl implements StudentsService {
	private static final int SCORE_BEST_STUDENT = 80;
	final StudentRepo studentRepo;
	final MongoTemplate mongoTemplate;

	@Override
	@Transactional
	public Student addStudent(Student student) {
		long id = student.id();
		if (studentRepo.existsById(id)) {
			throw new IllegalStateException(String.format("Student %d already exists", id));
		}
		studentRepo.save(StudentDoc.of(student));
		log.debug("saved {}", student);
		return student;
	}

	@Override
	public Student updatePhone(long id, String phone) {
		StudentDoc studentDoc = getStudent(id);
		String oldPhone = studentDoc.getPhone();
		log.debug("student {}, old phone number {}, new phone number {}", id, oldPhone, phone);
		studentDoc.setPhone(phone);
		studentRepo.save(studentDoc);
		return studentDoc.build();
	}

	private StudentDoc getStudent(long id) {
		return studentRepo.findById(id)
				.orElseThrow(() -> new NotFoundException(String.format("Student %d not found", id)));
	}

	@Override
	@Transactional
	public List<Mark> addMark(long id, Mark mark) {
		StudentDoc studentDoc = getStudent(id);
		studentDoc.addMark(mark);
		studentRepo.save(studentDoc);
		log.debug("student {}, added mark {}", id, mark);
		return studentDoc.getMarks();
	}

	@Override
	@Transactional
	public Student removeStudent(long id) {
		StudentDoc studentDoc = studentRepo.findStudentNoMarks(id);
		if (studentDoc == null) {
			throw new NotFoundException(String.format("student %d not found", id));
		}
		studentRepo.deleteById(id);
		log.debug("removed student {}, marks {}", id, studentDoc.getMarks());
		return studentDoc.build();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Mark> getMarks(long id) {
		StudentDoc studentDoc = studentRepo.findStudentMarks(id);
		if (studentDoc == null) {
			throw new NotFoundException(String.format("student %d not found", id));
		}
		log.debug("id {}, name {}, phone {}, marks {}", studentDoc.getId(), studentDoc.getName(), studentDoc.getPhone(),
				studentDoc.getMarks());
		return studentDoc.getMarks();
	}

	@Override
	public Student getStudentByPhone(String phoneNumber) {
		IdName studentDoc = studentRepo.findByPhone(phoneNumber);
		Student res = null;
		if (studentDoc != null) {
			res = new Student(studentDoc.getId(), studentDoc.getName(), phoneNumber);
		}
		return res;
	}

	@Override
	public List<Student> getStudentByPhonePrefix(String phonePrefix) {
		List<IdNamePhone> students = studentRepo.findByPhoneRegex(phonePrefix + ".+");
		log.debug("number of this students having phone prefix {} is {}", phonePrefix, students.size());
		return getStudents(students);
	}

	private List<Student> getStudents(List<IdNamePhone> students) {
		return students.stream().map(inp -> new Student(inp.getId(), inp.getName(), inp.getPhone())).toList();
	}

	@Override
	public List<Student> getStudentsAllGoodMarks(int thresholdScore) {
		List<IdNamePhone> students = studentRepo.findByGoodMarks(thresholdScore);

		return getStudents(students);
	}

	@Override
	public List<Student> getStudentsFewMarks(int thresholdMarks) {
		List<IdNamePhone> students = studentRepo.findByFewMarks(thresholdMarks);

		return getStudents(students);
	}

	@Override
	public List<Student> getStudentsAllGoodMarksSubject(String subject, int thresholdScore) {
		List<IdNamePhone> students = studentRepo.findStudentsAllGoodMarksSubject(subject, thresholdScore);
		List<Student> res = getStudents(students);
		log.trace("gets list of Students {}", res);
		return res;
	}

	@Override
	public List<Student> getStudentsMarksAmountBetween(int min, int max) {
		log.debug("received min {} and max {} values", min, max);
		List<IdNamePhone> students = studentRepo.findStudentsMarksAmountBetween(min, max);
		List<Student> res = getStudents(students);
		log.trace("gets list of Students {}", res);
		return res;
	}

	@Override
	public List<Mark> getStudentSubjectMarks(long id, String subject) {
		if (!studentRepo.existsById(id)) {
			throw new NotFoundException(String.format("student with id %d not found", id));
		}
		MatchOperation matchStudent = Aggregation.match(Criteria.where("id").is(id));
		UnwindOperation unwindOperation = Aggregation.unwind("marks");
		MatchOperation matchMarksSubject = Aggregation.match(Criteria.where("marks.subject").is(subject));
		ProjectionOperation projectionOperation = Aggregation.project("marks.score", "marks.date");
		Aggregation pipeLine = Aggregation.newAggregation(matchStudent, unwindOperation, matchMarksSubject,
				projectionOperation);
		var aggregationResult = mongoTemplate.aggregate(pipeLine, StudentDoc.class, Document.class);
		List<Document> listDocuments = aggregationResult.getMappedResults();
		log.debug("listDocuments: {}", listDocuments);
		List<Mark> result = listDocuments.stream().map(d -> new Mark(subject,
				d.getDate("date").toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), d.getInteger("score")))
				.toList();
		;
		log.debug("result: {}", result);
		return result;
	}

	@Override
	public List<NameAvgScore> getStudentAvgScore(int avgScoreThreshold) {
		UnwindOperation unwindOperation = Aggregation.unwind("marks");
		GroupOperation groupOperation = Aggregation.group("name").avg("marks.score").as("avgScore");
		MatchOperation matchOperation = Aggregation.match(Criteria.where("avgScore").gt(avgScoreThreshold));
		SortOperation sortOperation = Aggregation.sort(Direction.DESC, "avgScore");
		Aggregation pipeline = Aggregation.newAggregation(unwindOperation, groupOperation, matchOperation,
				sortOperation);

		List<NameAvgScore> result = mongoTemplate.aggregate(pipeline, StudentDoc.class, Document.class)
				.getMappedResults().stream()
				.map(d -> new NameAvgScore(d.getString("_id"), d.getDouble("avgScore").intValue())).toList();
		log.debug("result: {}", result);
		return result;
	}

	@Override
	public List<Mark> getStudentMarksAtDates(long id, LocalDate from, LocalDate to) {
		// TODO
		// returns list of Mark objects of the required student at the given dates
		// Filtering and projection should be done at DB server
		// LocalDate from and LocalDate to is checking on Controller and from less than to
		if (!studentRepo.existsById(id)) {
			throw new NotFoundException(String.format("Student with id %d not found", id));
		}
		MatchOperation matchOperation = Aggregation.match(Criteria.where("id").is(id));
		UnwindOperation unwindOperation = Aggregation.unwind("marks");
		MatchOperation matchDates = Aggregation.match(Criteria.where("marks.date").gte(from).lte(to));
		ProjectionOperation projectionOperation = Aggregation.project("marks.score", "marks.date", "marks.subject");
		Aggregation pipeline = Aggregation.newAggregation(matchOperation, unwindOperation, matchDates,
				projectionOperation);
		var aggregationResult = mongoTemplate.aggregate(pipeline, StudentDoc.class, Document.class);
		List<Document> listDocuments = aggregationResult.getMappedResults();
		log.debug("listDocuments: {}", listDocuments);
		List<Mark> res = listDocuments.stream()
				.map(d -> new Mark(d.getString("subject"),
						d.getDate("date").toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
						d.getInteger("score")))
				.toList();
		log.debug("marks: {}", res);
		return res;
	}

	@Override
	public List<String> getBestStudents(int nStudents) {
		// TODO
		// returns list of a given number of the best students
		// Best students are the ones who have most scores greater than 80
		/*************************************/
		UnwindOperation unwind = Aggregation.unwind("marks");
		GroupOperation group = Aggregation.group("name").avg("marks.score").as("avgScore");
		MatchOperation match = Aggregation.match(Criteria.where("avgScore").gte(SCORE_BEST_STUDENT));
		ProjectionOperation projection = Aggregation.project("name", "avgScore");
		SortOperation sort = Aggregation.sort(Direction.DESC, "avgScore");
		LimitOperation limit = Aggregation.limit(nStudents);

		Aggregation pipeline = Aggregation.newAggregation(unwind, group, match, projection, limit, sort);
		var aggregationResult = mongoTemplate.aggregate(pipeline, StudentDoc.class, Document.class);
		List<Document> listDocuments = aggregationResult.getMappedResults();
		log.debug("listDocuments: {}", listDocuments);
		List<String> res = listDocuments.stream().map(d -> d.getString("_id")).toList();
		log.debug("students: {}", res);
		return res;
	}

	@Override
	public List<String> getWorstStudents(int nStudents) {
		// TODO
		// returns list of a given number of the worst students
		// Worst students are the ones who have least sum's of all scores
		// Students who have no scores at all should be considered as worst
		/*************************************/
		// instead of GroupOperation to apply AggregationExpression (with
		// AccumulatorOperators.Sum) and ProjectionOperation for adding new fields with
		// computed values
		/*************************************/
		AggregationExpression agrExpr = AggregationExpression.from(AccumulatorOperators.Sum.sumOf("marks.score"));
		ProjectionOperation projection = Aggregation.project("name").and(agrExpr).as("scores");
		SortOperation sort = Aggregation.sort(Direction.ASC, "scores");
		LimitOperation limit = Aggregation.limit(nStudents);

		Aggregation pipeline = Aggregation.newAggregation(projection, sort, limit);
		var aggregationResult = mongoTemplate.aggregate(pipeline, StudentDoc.class, Document.class);
		List<Document> listDocuments = aggregationResult.getMappedResults();
		log.debug("listDocuments: {}", listDocuments);
		List<String> res = listDocuments.stream().map(d -> d.getString("name")).toList();
		log.debug("students: {}", res);
		return res;

	}

}
