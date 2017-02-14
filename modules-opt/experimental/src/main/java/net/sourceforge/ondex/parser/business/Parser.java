package net.sourceforge.ondex.parser.business;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.parser.ONDEXParser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for parsing business relation spreadsheets from IC.
 *
 * @author taubertj
 */
public class Parser extends ONDEXParser
{

    /**
     * Class representing a unique combination for a person from the business
     * spreadsheet.
     *
     * @author taubertj
     */
    public class PersonKey {

        public String company;

        public String contact;

        public String job;

        public PersonKey(String contact, String company, String job) {
            this.contact = contact;
            this.company = company;
            this.job = job;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PersonKey) {
                PersonKey key = (PersonKey) obj;
                return this.contact.equals(key.contact)
                        && this.company.equals(key.company)
                        && this.job.equals(key.job);
            } else
                return false;
        }

        @Override
        public int hashCode() {
            String id = contact + company + job;
            return id.hashCode();
        }

    }

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new FileArgumentDefinition(FileArgumentDefinition.INPUT_FILE, FileArgumentDefinition.INPUT_FILE_DESC, true, true, false, false)
        };
    }

    @Override
    public String getName() {
        return "Business relation parser";
    }

    @Override
    public String getVersion() {
        return "11.12.2009";
    }

    @Override
    public String getId() {
        return "business";
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {

        File file = new File((String) args.getUniqueValue(FileArgumentDefinition.INPUT_FILE));

        // open excel workbook
        Workbook workbook = Workbook.getWorkbook(file);

        // document should start on first sheet
        Sheet sheet = workbook.getSheet(0);

        // advisory boards treated as context
        Map<String, ONDEXConcept> advisoryBoards = new HashMap<String, ONDEXConcept>();

        // projects treated as context
        Map<String, ONDEXConcept> projects = new HashMap<String, ONDEXConcept>();

        // departments treated as context
        Map<String, ONDEXConcept> departments = new HashMap<String, ONDEXConcept>();

        // faculties treated as context
        Map<String, ONDEXConcept> faculties = new HashMap<String, ONDEXConcept>();

        // persons could be appear in multiple projects
        Map<PersonKey, ONDEXConcept> persons = new HashMap<PersonKey, ONDEXConcept>();

        // companies are represented as CVs
        Map<String, DataSource> companies = new HashMap<String, DataSource>();

        // concept class for persons
        ConceptClass ccPerson = this.graph.getMetaData().getFactory()
                .createConceptClass("Person", "Person");

        // concept class for advisory boards
        ConceptClass ccAdvisoryBoard = this.graph.getMetaData().getFactory()
                .createConceptClass("AdvisoryBoard", "Advisory Board");

        // concept class for projects
        ConceptClass ccProject = this.graph.getMetaData().getFactory()
                .createConceptClass("Project", "Project");

        // concept class for department
        ConceptClass ccDepartment = this.graph.getMetaData().getFactory()
                .createConceptClass("Department", "Department");

        // concept class for faculty
        ConceptClass ccFaculty = this.graph.getMetaData().getFactory()
                .createConceptClass("Faculty", "Faculty");

        // evidence type for internal people
        EvidenceType internal = this.graph.getMetaData().getFactory()
                .createEvidenceType("internal", "internal");

        // evidence type for external people
        EvidenceType external = this.graph.getMetaData().getFactory()
                .createEvidenceType("external", "external");

        // this is the standard DataSource for all grouping entities like AB, Project,
        // Department or Faculty
        DataSource elementOf = this.graph.getMetaData().getFactory().createDataSource(
                "spreadsheet", "Spreadsheet Data");

        // relation between person and advisory board
        RelationType partOfAB = this.graph.getMetaData().getFactory()
                .createRelationType("partOfAB", "Part of Advisory Board");

        // relation between person and project
        RelationType partOfProject = this.graph.getMetaData().getFactory()
                .createRelationType("partOfProject", "Part of Project");

        // relation between person and department
        RelationType partOfDept = this.graph.getMetaData().getFactory()
                .createRelationType("partOfDept", "Part of Department");

        // relation between department and faculty
        RelationType partOfFac = this.graph.getMetaData().getFactory()
                .createRelationType("partOfFac", "Part of Faculty");

        // ignore header row
        for (int i = 1; i < sheet.getRows(); i++) {

            Cell ie = sheet.getCell(0, i);
            String internalExternal = ie.getContents();

            Cell ab = sheet.getCell(1, i);
            String advisoryBoardName = ab.getContents();

            Cell p = sheet.getCell(2, i);
            String projectName = p.getContents();

            Cell c = sheet.getCell(3, i);
            String contact = c.getContents();

            Cell j = sheet.getCell(4, i);
            String jobTitle = j.getContents();

            Cell comp = sheet.getCell(5, i);
            String company = comp.getContents();

            Cell d = sheet.getCell(6, i);
            String departmentName = d.getContents();

            Cell fac = sheet.getCell(7, i);
            String facultyName = fac.getContents();

            // decide evidence type as internal / external
            EvidenceType evidenceIE;
            if (internalExternal.equals("I")) {
                evidenceIE = internal;
            } else {
                evidenceIE = external;
            }

            // deal with current person
            ONDEXConcept person;

            // unique identification of person
            PersonKey key = new PersonKey(contact, company, jobTitle);
            if (!persons.containsKey(key)) {

                // companies are treated as CVs
                String companyid = company.replaceAll("\\s", "_");
                if (!companies.containsKey(companyid)) {
                    DataSource dataSource = this.graph.getMetaData().getFactory().createDataSource(
                            companyid, company);
                    companies.put(companyid, dataSource);
                }

                // new person concept
                person = this.graph.getFactory().createConcept(jobTitle,
                        companies.get(companyid), ccPerson, evidenceIE);
                person.addTag(person); // self context is important
                person.createConceptName(contact, true);
                persons.put(key, person);
            } else {
                // existing person might be involved in internal and/or external
                person = persons.get(key);
                person.addEvidenceType(evidenceIE);
            }

            // if person is part of a advisory board create relation
            if (advisoryBoardName != null && advisoryBoardName.length() > 0) {
                ONDEXConcept advisoryBoard;
                if (!advisoryBoards.containsKey(advisoryBoardName)) {

                    // new advisory board concept
                    advisoryBoard = this.graph.getFactory().createConcept(
                            advisoryBoardName, elementOf, ccAdvisoryBoard,
                            evidenceIE);
                    advisoryBoard.addTag(advisoryBoard); // self context is
                    // important
                    advisoryBoard.createConceptName(advisoryBoardName, true);
                    advisoryBoards.put(advisoryBoardName, advisoryBoard);
                } else {
                    advisoryBoard = advisoryBoards.get(advisoryBoardName);
                }

                // bi directional context between person and advisory board
                advisoryBoard.addTag(person);
                person.addTag(advisoryBoard);

                // new part of relation with both context
                ONDEXRelation r = this.graph.getFactory().createRelation(
                        person, advisoryBoard, partOfAB, evidenceIE);
                r.addTag(advisoryBoard);
                r.addTag(person);
            }

            // if person is part of a project create relation
            if (projectName != null && projectName.length() > 0) {
                ONDEXConcept project;
                if (!projects.containsKey(projectName)) {

                    // new project concept
                    project = this.graph.getFactory().createConcept(
                            projectName, elementOf, ccProject, evidenceIE);
                    project.addTag(project); // self context is important
                    project.createConceptName(projectName, true);
                    projects.put(projectName, project);
                } else {
                    project = projects.get(projectName);
                }

                // bi directional context between person and project
                project.addTag(person);
                person.addTag(project);

                // new part of relation with both context
                ONDEXRelation r = this.graph.getFactory().createRelation(
                        person, project, partOfProject, evidenceIE);
                r.addTag(project);
                r.addTag(person);
            }

            // if a person is part of a department create relation
            if (departmentName != null && departmentName.length() > 0) {
                ONDEXConcept department;
                if (!departments.containsKey(departmentName)) {

                    // new department concept
                    department = this.graph.getFactory()
                            .createConcept(departmentName, elementOf,
                                    ccDepartment, evidenceIE);
                    department.addTag(department); // self context is
                    // important
                    department.createConceptName(departmentName, true);
                    departments.put(departmentName, department);

                    // a department belongs to a faculty
                    ONDEXConcept faculty;
                    if (!faculties.containsKey(facultyName)) {
                        faculty = this.graph.getFactory().createConcept(
                                facultyName, elementOf, ccFaculty, evidenceIE);
                        faculty.addTag(faculty); // self context is
                        // important
                        faculty.createConceptName(facultyName, true);
                        faculties.put(facultyName, faculty);
                    } else {
                        faculty = faculties.get(facultyName);
                    }

                    // bi directional context between department and faculty
                    faculty.addTag(department);
                    department.addTag(faculty);

                    // new part of relation with both context
                    ONDEXRelation r = this.graph.getFactory().createRelation(
                            department, faculty, partOfFac, evidenceIE);
                    r.addTag(department);
                    r.addTag(faculty);
                } else {
                    department = departments.get(departmentName);
                }

                // check if relation to department already captured
                ONDEXRelation r = this.graph.getRelation(person, department,
                        partOfDept);
                if (r == null) {

                    // bi directional context between person and department
                    department.addTag(person);
                    person.addTag(department);

                    // new part of relation with both context
                    r = this.graph.getFactory().createRelation(person,
                            department, partOfDept, evidenceIE);
                    r.addTag(department);
                    r.addTag(person);
                } else {
                    // existing relation but maybe different I/E
                    r.addEvidenceType(evidenceIE);
                }
            }
        }
    }

}
