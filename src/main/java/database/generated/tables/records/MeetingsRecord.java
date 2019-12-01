/*
 * This file is generated by jOOQ.
 */
package database.generated.tables.records;


import database.generated.tables.Meetings;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


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
public class MeetingsRecord extends UpdatableRecordImpl<MeetingsRecord> implements Record5<Integer, Integer, Timestamp, Timestamp, Long> {

    private static final long serialVersionUID = -1878340881;

    /**
     * Setter for <code>public.meetings.id</code>.
     */
    public MeetingsRecord setId(Integer value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.meetings.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.meetings.section_id</code>.
     */
    public MeetingsRecord setSectionId(Integer value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.meetings.section_id</code>.
     */
    public Integer getSectionId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.meetings.begin_date</code>.
     */
    public MeetingsRecord setBeginDate(Timestamp value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.meetings.begin_date</code>.
     */
    public Timestamp getBeginDate() {
        return (Timestamp) get(2);
    }

    /**
     * Setter for <code>public.meetings.end_date</code>.
     */
    public MeetingsRecord setEndDate(Timestamp value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.meetings.end_date</code>.
     */
    public Timestamp getEndDate() {
        return (Timestamp) get(3);
    }

    /**
     * Setter for <code>public.meetings.duration</code>.
     */
    public MeetingsRecord setDuration(Long value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.meetings.duration</code>.
     */
    public Long getDuration() {
        return (Long) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Integer, Integer, Timestamp, Timestamp, Long> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Integer, Integer, Timestamp, Timestamp, Long> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Meetings.MEETINGS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return Meetings.MEETINGS.SECTION_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field3() {
        return Meetings.MEETINGS.BEGIN_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field4() {
        return Meetings.MEETINGS.END_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field5() {
        return Meetings.MEETINGS.DURATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component2() {
        return getSectionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component3() {
        return getBeginDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component4() {
        return getEndDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component5() {
        return getDuration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value2() {
        return getSectionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value3() {
        return getBeginDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value4() {
        return getEndDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value5() {
        return getDuration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeetingsRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeetingsRecord value2(Integer value) {
        setSectionId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeetingsRecord value3(Timestamp value) {
        setBeginDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeetingsRecord value4(Timestamp value) {
        setEndDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeetingsRecord value5(Long value) {
        setDuration(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeetingsRecord values(Integer value1, Integer value2, Timestamp value3, Timestamp value4, Long value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MeetingsRecord
     */
    public MeetingsRecord() {
        super(Meetings.MEETINGS);
    }

    /**
     * Create a detached, initialised MeetingsRecord
     */
    public MeetingsRecord(Integer id, Integer sectionId, Timestamp beginDate, Timestamp endDate, Long duration) {
        super(Meetings.MEETINGS);

        set(0, id);
        set(1, sectionId);
        set(2, beginDate);
        set(3, endDate);
        set(4, duration);
    }
}
