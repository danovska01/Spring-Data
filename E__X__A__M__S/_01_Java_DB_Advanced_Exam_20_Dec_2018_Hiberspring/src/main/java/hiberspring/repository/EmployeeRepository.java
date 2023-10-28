package hiberspring.repository;

import hiberspring.domain.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCardNumber(String card);

    // List<Employee> findAllByBranch_NameIn(Set<String> branchNamesWithMoreThanOneProduct);

    //Order the data by full name in alphabetical order,
    // and then by length of position in descending order.
    @Query("SELECT e FROM Employee e WHERE e.branch.name IN :branchNames " +
            "ORDER BY e.firstName  ASC, e.lastName ASC, CHAR_LENGTH(e.position)")
    List<Employee> findAllByBranch_NameInOrderByFirstNameAscLastNameAscLengthOfPositionDesc(Set<String> branchNames);
}
