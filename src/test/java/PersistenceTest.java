import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DriverManagerDestination;
import com.ninja_squad.dbsetup.generator.ValueGenerators;
import com.ninja_squad.dbsetup.operation.Operation;

import models.Class;
import models.Student;

public class PersistenceTest {

    private static DbSetupTracker dbSetupTracker = new DbSetupTracker();
    
    private static EntityManagerFactory factory;
    private static EntityManager entityManager;
    
    @BeforeClass
    public static void setupClass() {
        
        // setup entitymanager
        factory = Persistence.createEntityManagerFactory( "com.codingzombies.jpahibernate" );
        entityManager = factory.createEntityManager();
        
        // create test data
        final Operation operation =
            Operations.sequenceOf(
                Operations.sequenceOf(
                    Operations.insertInto("students")
                        .withGeneratedValue("ID", ValueGenerators.sequence().startingAt(1000L))    
                        .columns("first_name", "last_name")
                        .values("Dohdoh", "Skater")
                        .values("Sasha", "Alexandra")
                        .values("Sarah", "Kerrigan")
                        .build(),
                    Operations.insertInto("classes")
                        .columns("code", "name")
                        .values("math17", "Algebra and Trigonometry")
                        .values("comm01", "Communication 1")
                        .build(),
                    Operations.insertInto("class_students")
                        .columns("class_id", "student_id")
                        .values("math17", 1000)
                        .values("comm01", 1000)
                        .values("math17", 1001)
                        .values("comm01", 1002)
                        .build()
                    )
                );
        
        // same DbSetup definition as above
        final DbSetup dbSetup = new DbSetup(new DriverManagerDestination("jdbc:h2:./db/repository", "", ""), operation);
        
        // use the tracker to launch the DbSetup.
        dbSetupTracker.launchIfNecessary(dbSetup);
    }

    @AfterClass
    public static void teardownClass() {
        factory.close();
    }

    
    @Before
    public void setUp()  {
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown()  {
        entityManager.getTransaction().commit();
    }
    
    @Test
    public void testSaveObjects() {
        // test if student objects are getting persisted on the database
        final Student s1 = new Student("Paul", "Panda");
        final Student s2 = new Student("Alex", "Lion");
        
        entityManager.persist(s1);
        entityManager.persist(s2);
        
        assertNotNull(getStudent(s1.getId()));
        assertNotNull(getStudent(s2.getId()));
        
        entityManager.remove(s1);
        entityManager.remove(s2);
    }

    @Test
    public void testClassStudentRelationship() {
        
        final Student dohdoh = getStudent(1000L);
        assertEquals("Dohdoh", dohdoh.getFirstName());
        assertEquals("Skater", dohdoh.getLastName());
        final String[] dohdohClasses = dohdoh.getClasses().stream().map(models.Class::getCode).toArray(s -> new String[s]);
        assertArrayEquals(new String[] {"math17",  "comm01"}, dohdohClasses);

        final Student sasha = getStudent(1001L);
        assertEquals("Sasha", sasha.getFirstName());
        assertEquals("Alexandra", sasha.getLastName());
        final String[] sashaClasses = sasha.getClasses().stream().map(models.Class::getCode).toArray(s -> new String[s]);
        assertArrayEquals(new String[] {"math17"}, sashaClasses);

        final Student sarah = getStudent(1002L);
        assertEquals("Sarah", sarah.getFirstName());
        assertEquals("Kerrigan", sarah.getLastName());
        final String[] sarahClasses = sarah.getClasses().stream().map(models.Class::getCode).toArray(s -> new String[s]);
        assertArrayEquals(new String[] {"comm01"}, sarahClasses);
        
        final Class math17 = getClass("math17");
        final String[] mathStudents = math17.getStudents().stream().map(Student::getFirstName).toArray(s -> new String[s]);
        assertArrayEquals(new String[] {"Dohdoh", "Sasha"}, mathStudents);

        final Class comm01 = getClass("comm01");
        final String[] commStudents = comm01.getStudents().stream().map(Student::getFirstName).toArray(s -> new String[s]);
        assertArrayEquals(new String[] {"Dohdoh", "Sarah"}, commStudents);
    }

    @Test
    public void testNamedQuery() {
        final models.Class math = entityManager.createNamedQuery("Class.findClassByCode", models.Class.class).setParameter("code", "math17").getSingleResult();
        assertNotNull(math);
        assertEquals("Algebra and Trigonometry", math.getName());
    }
    
    private Student getStudent(final long id) {
        return entityManager.createQuery("from Student where id = :id", Student.class).setParameter("id", id).getSingleResult();
    }

    private models.Class getClass(final String code) {
        return entityManager.createQuery("from Class where code = :code", models.Class.class).setParameter("code", code).getSingleResult();
    }
    
}
