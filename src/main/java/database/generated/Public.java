/*
 * This file is generated by jOOQ.
 */
package database.generated;


import database.generated.tables.Courses;
import database.generated.tables.FlywaySchemaHistory;
import database.generated.tables.Meetings;
import database.generated.tables.Sections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1027009341;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.courses</code>.
     */
    public final Courses COURSES = database.generated.tables.Courses.COURSES;

    /**
     * The table <code>public.flyway_schema_history</code>.
     */
    public final FlywaySchemaHistory FLYWAY_SCHEMA_HISTORY = database.generated.tables.FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY;

    /**
     * The table <code>public.meetings</code>.
     */
    public final Meetings MEETINGS = database.generated.tables.Meetings.MEETINGS;

    /**
     * The table <code>public.sections</code>.
     */
    public final Sections SECTIONS = database.generated.tables.Sections.SECTIONS;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        List result = new ArrayList();
        result.addAll(getSequences0());
        return result;
    }

    private final List<Sequence<?>> getSequences0() {
        return Arrays.<Sequence<?>>asList(
            Sequences.COURSES_ID_SEQ,
            Sequences.MEETINGS_ID_SEQ,
            Sequences.SECTIONS_ID_SEQ);
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            Courses.COURSES,
            FlywaySchemaHistory.FLYWAY_SCHEMA_HISTORY,
            Meetings.MEETINGS,
            Sections.SECTIONS);
    }
}