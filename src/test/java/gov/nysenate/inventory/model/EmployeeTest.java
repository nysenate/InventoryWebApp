package gov.nysenate.inventory.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class EmployeeTest {

    @Test
    public void testNameFormatting() {
        Employee emp = new Employee(1, "First", "Last", "M", "Mr");
        String acutalFormattedName = emp.getFullName();
        assertThat(acutalFormattedName, is("First M Last Mr"));

        emp = new Employee(1, "First", "Last", null, "Mr");
        acutalFormattedName = emp.getFullName();
        assertThat(acutalFormattedName, is("First Last Mr"));

        emp = new Employee(1, "First", "Last", null, null);
        acutalFormattedName = emp.getFullName();
        assertThat(acutalFormattedName, is("First Last"));
    }

}