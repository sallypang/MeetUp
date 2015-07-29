package ca.ubc.cs.cpsc210.meetup.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.ubc.cs.cpsc210.meetup.exceptions.IllegalSectionInitialization;
import ca.ubc.cs.cpsc210.meetup.util.CourseTime;

/*
 * Represent a student's schedule consisting of all sections they must attend
 */
public class Schedule {

    // Remember sections on each kind of day
    private SortedSet<Section> MWFSections;
    private SortedSet<Section> TRSections;

    /**
     * Constructor
     */
    public Schedule() {
        MWFSections = new TreeSet<Section>();
        TRSections = new TreeSet<Section>();

    }

    /**
     * Add a section to the student's schedule
     *
     * @param section The section to add to the schedule.
     */
    public void add(Section section) throws IllegalSectionInitialization {
        SortedSet<Section> sections = getSectionsForDayOfWeek(section.getDayOfWeek());
        if (section.getCourse() == null)
            throw new IllegalSectionInitialization(
                    "Course link is not set for " + section.toString());

        sections.add(section);
    }


    /**
     * Retrieve the earliest start time in the schedule on a given day
     *
     * @param dayOfWeek The day of the week, either "MWF" or "TR"
     * @return The CourseTime of the earliest section or null
     */
    public CourseTime startTime(String dayOfWeek) {
        SortedSet<Section> sections = getSectionsForDayOfWeek(dayOfWeek);
        Section earliestSection = sections.first();
        if (earliestSection == null)
            return null;
        else
            return earliestSection.getCourseTime();
    }

    /**
     * Retrieve the latest start time in the schedule on a given day
     *
     * @param dayOfWeek The day of the week, either "MWF" or "TR"
     * @return The CourseTime of the latest section of the day or null
     */
    public CourseTime endTime(String dayOfWeek) {
        SortedSet<Section> sections = getSectionsForDayOfWeek(dayOfWeek);
        Section latestSection = sections.last();
        if (latestSection == null)
            return null;
        else
            return latestSection.getCourseTime();
    }

    /**
     * Find the start time of all one hour breaks less than the end time
     *
     * @param dayOfWeek The day of the week
     * @return The times in HH:MM of the start time of each one-hour break
     */
    public Set<String> getStartTimesOfBreaks(String dayOfWeek) {
        SortedSet<Section> sections = getSectionsForDayOfWeek(dayOfWeek);
        Set<String> startTimes = new HashSet<String>();
        if (sections.size() == 1) {
            Section section = sections.first();
            CourseTime courseTime = section.getCourseTime();
            startTimes.add(courseTime.getEndTime());
        } else if (sections.size() > 1) {
            Iterator<Section> it = sections.iterator();
            Section section = it.next();
            String lastTime = section.getCourseTime().getEndTime();
            while (it.hasNext()) {
                section = it.next();
                String nextTime = section.getCourseTime().getEndTime();

                if (calculateBreakTimeInMinutes(nextTime, lastTime) >= 60) {
                    startTimes.add(lastTime);
                }
                lastTime = nextTime;
            }

        }

        return startTimes;
    }


    /**
     * In which building was I before the given timeOfDay on the given dayOfWeek
     *
     * @param dayOfWeek The day of week of interest, "MWF" or "TR"
     * @param timeOfDay The time of day as "HH"
     * @return The building where the student was last or null if nowhere
     */
    public Building whereAmI2(String dayOfWeek, String timeOfDay) {
        SortedSet<Section> sections = getSectionsForDayOfWeek(dayOfWeek);

        String i = timeOfDay.substring(0, 2); //16
        // String i1 = timeOfDay.substring(3, timeOfDay.length()); //00
        int a1 = (Integer.parseInt(i) * 60); // 960
        //  int a2 = Integer.parseInt(i1); // 0
        // int a3 = a1 + a2; // 960

        if (timeOfDay != null) {
            for (Section s : sections) {
                if (s.getDay().equals(dayOfWeek)) {
                    int h = s.getCourseTime().getStartHours(); //12
                    int h1 = s.getCourseTime().getStartMinutes(); //0
                    int j = s.getCourseTime().getEndHours(); //12
                    int j1 = s.getCourseTime().getEndMinutes(); //50

                    int a = (h * 60) + h1;  // starting time
                    int b = (j * 60) + j1;  // ending time
                    if (a1 >= a) {  // if timeOfDay is greater than startTime
                        sections.add(s);
                    }
                }
            }
            Section sec = sections.last();
            return sec.getBuilding();
        }
        return null;
    }

    /**
     * Retrieve the sets for a particular day of the week
     *
     * @param dayOfWeek The day of week of interest, "MWF" or "TR"
     * @return The sections on a given day of Week
     */
    public SortedSet<Section> getSections(String dayOfWeek) {
        if (dayOfWeek.equals("MWF")) {
            return Collections.unmodifiableSortedSet(MWFSections);
        } else {
            return Collections.unmodifiableSortedSet(TRSections);
        }
    }

    /**
     * Compute the break between two HH:MM strings in minutes
     *
     * @param second The later time
     * @param first  The earlier time
     * @return minutes between
     */
    private int calculateBreakTimeInMinutes(String second, String first) {
        int secondInMinutesIntoDay = calculateMinutesIntoDay(second);
        int firstInMinutesIntoDay = calculateMinutesIntoDay(first);
        System.out.println("minutes is "
                + (secondInMinutesIntoDay - firstInMinutesIntoDay));
        return secondInMinutesIntoDay - firstInMinutesIntoDay;
    }

    /**
     * Transform a HH:MM time into minutes into the day
     *
     * @param aTime HH:MM time
     * @return Minutes since midnight
     */
    private int calculateMinutesIntoDay(String aTime) {
        int colonIndex = aTime.indexOf(":");
        int hours = Integer.parseInt(aTime.substring(0, colonIndex));
        int minutes = Integer.parseInt(aTime.substring(colonIndex + 1,
                aTime.length()));
        return (hours * 60) + minutes;
    }

    /**
     * Retrieve the sets for a particular day of the week
     *
     * @param dayOfWeek The day of week of interest, "MWF" or "TR"
     * @return The sections on that day of week
     */
    public SortedSet<Section> getSectionsForDayOfWeek(String dayOfWeek) {
        if (dayOfWeek.equals("MWF"))
            return MWFSections;
        else
            return TRSections;
    }

    /**
     * Find out if a student has a break at a particular timeOfDay
     * timeOfDay is in HH format;
     *
     * @param dayOfWeek; the day of the week either "MWF" or "TR"
     * @param timeOfDay; the time of day in HH format
     * @return boolean; true if student has a break
     */
    public boolean hasBreakAtTimeOfDay(String dayOfWeek, String timeOfDay) {

        boolean b = false;

        SortedSet<Section> sections = getSectionsForDayOfWeek(dayOfWeek); //all the sections today

        int z = sections.first().getCourseTime().getStartHours() * 60;
        int z1 = sections.first().getCourseTime().getStartMinutes();
        int firstCourse = z + z1; // start time of FIRST course

        int y = sections.last().getCourseTime().getEndHours() * 60;
        int y1 = sections.last().getCourseTime().getEndMinutes();
        int lastCourse = y + y1; // end time of LAST course

        SortedSet<Section> afterSections = new TreeSet<Section>(); // all the sections after timeOfDay
        Set<String> endTimes = getStartTimesOfBreaks(dayOfWeek); // start times of the breaks (AKA end times of classes)

        int chosenTime = Integer.parseInt(timeOfDay.substring(0, 2)) * 60; // timeOfDay as an int and in minutes

        for (Section s : sections) {
            int a = s.getCourseTime().getStartHours() * 60;
            int a1 = s.getCourseTime().getStartMinutes();
            int a0 = a + a1; // start time of every section
            if (a0 >= chosenTime) { // if start time is greater than time of day (ex. 960 > 900)
                afterSections.add(s);
            }
        }

        if (afterSections == null || afterSections.size() == 0) { // if there is no classes after timeOfDay,
            if (chosenTime > firstCourse && chosenTime < lastCourse) { //if time of day is between a class
                return false;
            } else {
                return true;
            }
        } else {

            Section first = afterSections.first();
            int d = first.getCourseTime().getStartHours() * 60;
            int d1 = first.getCourseTime().getStartMinutes();
            int firstCourseAft = d + d1; // start time of first course after time of day

            for (String e : endTimes) {
                if (e.startsWith("8") || e.startsWith("9")) {
                    int g = Integer.parseInt(e.substring(0, 1)) * 60;
                    int g1 = Integer.parseInt(e.substring(2, e.length()));
                    int endTime = g + g1; // endtimes
                    // if timeOfDay is at 12, and my class ends at 11:50, am i free for 60 more min? OR if timeofDay is greater than last course
                    //                                                                               OR if timeOfDay is smaller than first course
                    if ((endTime <= chosenTime && (firstCourseAft - endTime) >= 60) || (firstCourse - chosenTime) >= 60 ||chosenTime > lastCourse) {
                        b = true;
                    } else {
                        b = false;
                    }
                } else {

                    int c = Integer.parseInt(e.substring(0, 2)) * 60;
                    int c1 = Integer.parseInt(e.substring(3, e.length()));
                    int c0 = c + c1; // endtimes in integers
                    for (Section n : sections) {
                        int startingTime = n.getCourseTime().getStartHours() * 60;
                        if (chosenTime == startingTime) { // if time of day equals start of any course then no not free
                            return false;
                        }
                    }

                    if ((c0 <= chosenTime && (firstCourseAft - c0) >= 60) || (firstCourse - chosenTime) >= 60 || chosenTime > lastCourse) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }

        return b;
    }
}

