package com.demo;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.demo.dto.EmployeeDetails;
import com.demo.dto.EmployeeProject;
import com.demo.model.Address;
import com.demo.model.Department;
import com.demo.model.DesignProject;
import com.demo.model.Employee;
import com.demo.model.PhoneType;
import com.demo.model.Project;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DemoApplicationTests {

	@PersistenceContext
	private EntityManager manager;
	
	@Test
	public void testSelect() {
		List<Employee> employees = manager.createQuery(
				"select e from Employee e", Employee.class)
				.getResultList();
		
		assertEquals(10, employees.size());
	}

	@Test
	public void testDifferentEntityAsResult() {
		List<Department> departments = manager.createQuery(
				"select distinct e.department from Employee e", Department.class)
				.getResultList();
		
		assertEquals(5, departments.size());
	}
	
	@Test
	public void testProjection() {
		List<Object[]> employees = manager.createQuery(
				"select e.name, e.salary from Employee e " +
				"order by e.id", Object[].class)
				.getResultList();
		
		assertEquals(10, employees.size());
		assertEquals("SMITH", employees.get(0)[0]);
		assertEquals(800.0, (double) employees.get(0)[1], 0.0001);
	}
	
	@Test
	public void testConstructorExpression() {
		List<EmployeeDetails> employees = manager.createQuery(
				"select " +
				"	new com.demo.dto.EmployeeDetails(" +
				"   e.name, e.salary, e.department.name) " +
				"from Employee e", EmployeeDetails.class)
				.getResultList();
		
		assertEquals(10, employees.size());
	}
	
	@Test
	public void testJoinOperator() {
		List<Department> departments = manager.createQuery(
				"select distinct d from Employee e " + 
				"join e.department d", Department.class)
				.getResultList();
		
		assertEquals(5, departments.size());
	}
	
	@Test
	public void testJoinConditionWhereClause() {
		List<EmployeeDetails> employees = manager.createQuery(
				"select " +
				"	new com.demo.dto.EmployeeDetails(" +
				"   e.name, e.salary, e.department.name) " +
				"from Employee e, Department d " +
				"where e.department = d and d.name = 'TI'", 
				EmployeeDetails.class).getResultList();
		
		assertEquals(3, employees.size());
	}
	
	@Test
	public void testMapJoin() {
		List<Object[]> objs = manager.createQuery(
				"select e.name, key(p), value(p) from Employee e " + 
				"join e.phones p where key(p) in ('WORK')", Object[].class)
				.getResultList();
		
		assertEquals("ANA", objs.get(0)[0]);
		assertEquals(PhoneType.WORK, objs.get(0)[1]);
		assertEquals("333 3333333", objs.get(0)[2]);
	}
	
	@Test
	public void testOuterJoin() {
		List<EmployeeDetails> employees = manager.createQuery(
				"select " +
				"	new com.demo.dto.EmployeeDetails(" +
				"   e.name, e.salary, e.department.name) " +
				"from Employee e left outer join e.department d ", 
				EmployeeDetails.class).getResultList();
		
		assertEquals(10, employees.size());
	}
	
	@Test
	public void testJoinWithON() {
		List<EmployeeDetails> employees = manager.createQuery(
				"select " +
				"	new com.demo.dto.EmployeeDetails(" +
				"   e.name, e.salary, e.department.name) " +
				"from Employee e inner join e.department d " +
				"on d.name like 'S%'", 
				EmployeeDetails.class).getResultList();
		
		assertEquals(4, employees.size());
	}
	
	@Test
	public void testFetchJoin() {
		List<Address> addresses = manager.createQuery(
				"select a from Address a join fetch a.employee ", 
				Address.class).getResultList();
		
		assertEquals(3, addresses.size());
		assertNotNull(addresses.get(0).getEmployee());
	}
	
	@Test
	public void testBetweenExpression() {
		List<Employee> employees = manager.createQuery(
			"select e from Employee e " + 
			"where e.salary between 1000 and 2000", Employee.class)
				.getResultList();
		
		assertEquals(3, employees.size());
	}
	
	@Test
	public void testLikeExpression() {
		List<Department> departments = manager.createQuery(
			"select d from Department d " + 
			"where d.name like 'S%'", Department.class)
				.getResultList();
		
		assertEquals(2, departments.size());
	}
	
	@Test
	public void testSubquery() {
		List<Employee> employees = manager.createQuery(
			"select e from Employee e " + 
			"where e.salary = (" +
			"	select max(salary) from Employee)", 
			Employee.class).getResultList();
		
		assertEquals(1, employees.size());
		assertEquals("MARTIN", employees.get(0).getName());
		assertEquals(4000.0, employees.get(0).getSalary(), 0.0001);
	}
	
	@Test
	public void testSubqueryCorrelated() {
		List<Employee> employees = manager.createQuery(
			"select e from Employee e " + 
			"where exists (" +
			"	select 1 from e.phones p " +
			"	where key(p) = 'CELL')",
			Employee.class).getResultList();
		
		assertEquals(2, employees.size());
	}
	
	@Test
	public void testInExpression() {
		List<Employee> employees = manager.createQuery(
			"select e from Employee e " + 
			"where e.department.name in ('ADMINISTRATIVE', 'HR')",
			Employee.class).getResultList();
		
		assertEquals(3, employees.size());
	}
	
	@Test
	public void testIsEmpty() {
		List<Project> projects = manager.createQuery(
			"select p from Project p " + 
			"where p.employees is empty",
			Project.class).getResultList();
		
		assertEquals(1, projects.size());
	}
	
	@Test
	public void testMemberOf() {
		Employee clark = manager.getReference(Employee.class, 9);
		
		List<Project> projects = manager.createQuery(
			"select p from Project p " + 
			"where :employee member of p.employees", Project.class)
			.setParameter("employee", clark)
			.getResultList();
		
		assertEquals(2, projects.size());
	}
	
	@Test
	public void testExistsExpression() {
		List<Employee> employees = manager.createQuery(
			"select e from Employee e where not exists ( " + 
			"	select 1 from Project p where e member of p.employees)", Employee.class)
			.getResultList();
		
		assertEquals(5, employees.size());
	}
	
	@Test
	public void testSubclassDiscrimination() {
		
		List<Project> projects = manager.createQuery(
				"select p from Project p where type(p) = :type", Project.class)
				.setParameter("type", DesignProject.class)
				.getResultList();
		
		assertEquals(3, projects.size());
	}
	
	@Test
	public void testDowncasting() {
		List<EmployeeProject> result = manager.createQuery(
			"select distinct new com.demo.dto.EmployeeProject( "
		  + "e.name, p.name, p.qaRating) from Project p "
		  + "inner join p.employees e "
		  + "where treat(p as QualityProject).qaRating > 3", EmployeeProject.class)
				.getResultList();
		
		assertEquals(3, result.size());
	}
	
	@Test
	public void testFuctionSize() {
		List<Project> projects = manager.createQuery(
			"select p from Project p where size(p.employees) = 1", Project.class)
			.getResultList();
		
		assertEquals(1, projects.size());
	}
	
	@Test
	public void testFuctionLength() {
		List<String> departments = manager.createQuery(
			"select d.name from Department d where length(d.name) = 2", String.class)
			.getResultList();
		
		assertTrue(departments.contains("TI"));
		assertTrue(departments.contains("HR"));
	}
	
	@Test
	public void testCaseExpression() {
		List<Object[]> projects = manager.createQuery(
			"select " +
			"	p.name, " +
			"	case type(p) " +
			"	when DesignProject then 'Development' " +
			"	when QualityProject then 'QA' " +
			"	end " +
			"from Project p " + 
			"where p.employees is empty", Object[].class).getResultList();
		
		assertEquals("PROJECT D", projects.get(0)[0]);
		assertEquals("Development", projects.get(0)[1]);
	}
	
	@Test
	public void testOrderBy() {
		List<String> departments = manager.createQuery(
			"select d.name from Department d order by d.name desc", String.class)
				.getResultList();
		
		assertEquals("TI", departments.get(0));
		assertEquals("ADMINISTRATIVE", departments.get(4));
	}
	
	@Test
	public void testCount() {
		Long count = manager.createQuery(
			"select count(*) from Department d", Long.class)
				.getSingleResult();
		
		assertEquals(new Long(5), count);
	}
	
	@Test
	public void testSum() {
		Double total = manager.createQuery(
			"select sum(e.salary) from Employee e", Double.class)
				.getSingleResult();
		
		assertEquals(19725.0, total, 0.0001);
	}
	
	@Test
	public void testGroupByClause() {
		List<Object[]> departments = manager.createQuery(
			" select d.name, count(e) from Employee e " +
			" join e.department d " +
			" group by d.name " +
			" order by d.name", Object[].class).getResultList();
		
		assertEquals("ADMINISTRATIVE", departments.get(0)[0]);
		assertEquals(new Long(2), departments.get(0)[1]);
	}
	
	@Test
	public void testHavingClause() {
		List<Object[]> departments = manager.createQuery(
			" select d.name, avg(e.salary) from Employee e " +
			" join e.department d " +
			" group by d.name " +
			" having avg(e.salary) = 1500.0", Object[].class).getResultList();
		
		assertEquals("HR", departments.get(0)[0]);
		assertEquals(1500.0, (double) departments.get(0)[1], 0.00001);
	}
	
}
