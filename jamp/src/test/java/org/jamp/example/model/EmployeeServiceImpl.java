package org.jamp.example.model;

public class EmployeeServiceImpl implements EmployeeService{

    @Override
    @SuppressWarnings("nls")
    public void addEmployee(Employee emp, int salary, float rate, Integer rank,
            String description) {
        System.out.printf("Employee Service %s %s  %s %s\n", emp, salary, rate, rank);
        
    }

}
