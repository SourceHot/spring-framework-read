package org.sourcehot.spring.qualifier;

import org.springframework.beans.factory.annotation.Autowired;

public class PersonService {
    @Autowired
//    @PersonQualifier(status = "status_teacher", quality = "quality_teacher")
    @PersonQualifier(status = "status_student", quality = "quality_student")
    private PersonS personS;

    public PersonS getPerson() {
        return personS;
    }

    public void setPerson(PersonS person) {
        this.personS = person;
    }
} 