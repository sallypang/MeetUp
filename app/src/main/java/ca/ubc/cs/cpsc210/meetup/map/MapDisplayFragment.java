package ca.ubc.cs.cpsc210.meetup.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.PathOverlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;


import ca.ubc.cs.cpsc210.meetup.R;
import ca.ubc.cs.cpsc210.meetup.model.Building;
import ca.ubc.cs.cpsc210.meetup.model.Course;
import ca.ubc.cs.cpsc210.meetup.model.CourseFactory;
import ca.ubc.cs.cpsc210.meetup.model.Place;
import ca.ubc.cs.cpsc210.meetup.model.PlaceFactory;
import ca.ubc.cs.cpsc210.meetup.model.Section;
import ca.ubc.cs.cpsc210.meetup.model.Student;
import ca.ubc.cs.cpsc210.meetup.model.Schedule;
import ca.ubc.cs.cpsc210.meetup.model.StudentManager;
import ca.ubc.cs.cpsc210.meetup.util.LatLon;
import ca.ubc.cs.cpsc210.meetup.util.SchedulePlot;


/**
 * @author Sally Pang
 * @since 2015-03-19
 * /**
 * Fragment holding the map in the UI.
 */
public class MapDisplayFragment extends Fragment {

    /**
     * Log tag for LogCat messages
     */
    private final static String LOG_TAG = "MapDisplayFragment";

    /**
     * Preference manager to access user preferences
     */
    private SharedPreferences sharedPreferences;

    /**
     * String to know whether we are dealing with MWF or TR schedule.
     * You will need to update this string based on the settings dialog at appropriate
     * points in time. See the project page for details on how to access
     * the value of a setting.
     */
    private String activeDay = "MWF";

    /**
     * A central location in campus that might be handy.
     */
    private final static GeoPoint UBC_MARTHA_PIPER_FOUNTAIN = new GeoPoint(49.264865,
            -123.252782);

    /**
     * Meetup Service URL
     * CPSC 210 Students: Complete the string.
     */
    private final String getStudentURL = "";

    /**
     * FourSquare URLs. You must complete the client_id and client_secret with values
     * you sign up for.
     */
    private static String FOUR_SQUARE_URL = "https://api.foursquare.com/v2/venues/explore";
    private static String FOUR_SQUARE_CLIENT_ID = "1RWGP1INTAMED321X2FSAUXHQQCHA54CAV2YL2JQLOPYTA14";
    private static String FOUR_SQUARE_CLIENT_SECRET = "VQCDBRYYS1I3MLZRM2BWG5KYKFW20KW50UYMK35FD4DTSEPW";


    /**
     * Overlays for displaying my schedules, buildings, etc.
     */
    private List<PathOverlay> scheduleOverlay;
    private ItemizedIconOverlay<OverlayItem> buildingOverlay;
    private OverlayItem selectedBuildingOnMap;

    /**
     * View that shows the map
     */
    private MapView mapView;

    /**
     * Access to domain model objects. Only store "me" in the studentManager for
     * the base project (i.e., unless you are doing bonus work).
     */
    private StudentManager studentManager;

    //private Student randomStudent = null;
    /* List of random students for bonus work
     */
    private Set<Student> randomstudents = null;

    private Student me = null;
    private static int ME_ID = 999999;

    /**
     * Map controller for zooming in/out, centering
     */
    private IMapController mapController;

    // ******************** Android methods for starting, resuming, ...

    // You should not need to touch this method
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        scheduleOverlay = new ArrayList<PathOverlay>();

        // You need to setup the courses for the app to know about. Ideally
        // we would access a web service like the UBC student information system
        // but that is not currently possible
        initializeCourses();

        // Initialize the data for the "me" schedule. Note that this will be
        // hard-coded for now
        initializeMySchedule();

        // You are going to need an overlay to draw buildings and locations on the map
        buildingOverlay = createBuildingOverlay();
    }


    // You should not need to touch this method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
    }

    // You should not need to touch this method
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mapView == null) {
            mapView = new MapView(getActivity(), null);

            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setClickable(true);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);

            mapController = mapView.getController();
            mapController.setZoom(mapView.getMaxZoomLevel() - 2);
            mapController.setCenter(UBC_MARTHA_PIPER_FOUNTAIN);
        }

        return mapView;
    }

    // You should not need to touch this method
    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "onDestroyView");
        ((ViewGroup) mapView.getParent()).removeView(mapView);
        super.onDestroyView();
    }

    // You should not need to touch this method
    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    // You should not need to touch this method
    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
    }

    // You should not need to touch this method
    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
    }

    /**
     * Save map's zoom level and centre. You should not need to
     * touch this method
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        if (mapView != null) {
            outState.putInt("zoomLevel", mapView.getZoomLevel());
            IGeoPoint cntr = mapView.getMapCenter();
            outState.putInt("latE6", cntr.getLatitudeE6());
            outState.putInt("lonE6", cntr.getLongitudeE6());
            Log.i("MapSave", "Zoom: " + mapView.getZoomLevel());
        }
    }

    // ****************** App Functionality

    /**
     * Show my schedule on the map. Every time "me"'s schedule shows, the map
     * should be cleared of all existing schedules, buildings, meetup locations, etc.
     */
    /**
     * @return void:
     */
    public void showMySchedule() {

        Schedule schedule = me.getSchedule();
        activeDay = this.sharedPreferences.getString("dayOfWeek", activeDay);

        SortedSet<Section> section = schedule.getSections(activeDay);

        this.clearSchedules();

        SchedulePlot mySchedulePlot = new SchedulePlot(section, "Sally", "#000000", R.drawable.ic_action_event);
        new GetRoutingForSchedule().execute(mySchedulePlot);

    }


    // CPSC 210 Students: You must complete the implementation of this method.
    // The very last part of the method should call the asynchronous
    // task (which you will also write the code for) to plot the route
    // for "me"'s schedule for the day of the week set in the Settings

    // Asynchronous tasks are a bit onerous to deal with. In order to provide
    // all information needed in one object to plot "me"'s route, we
    // create a SchedulePlot object and pass it to the asynchrous task.
    // See the project page for more details.


    // Get a routing between these points. This line of code creates and calls
    // an asynchronous task to do the calls to MapQuest to determine a route
    // and plots the route.
    // Assumes mySchedulePlot is a create and initialized SchedulePlot object

    // UNCOMMENT NEXT LINE ONCE YOU HAVE INSTANTIATED mySchedulePlot


    /**
     * Retrieve a random student's schedule from the Meetup web service and
     * plot a route for the schedule on the map. The plot should be for
     * the given day of the week as determined when "me"'s schedule
     * was plotted.
     */
    /**
     * @return void:
     */
    public void showRandomStudentsSchedule() {
        // To get a random student's schedule, we have to call the MeetUp web service.
        // Calling this web service requires a network access to we have to
        // do this in an asynchronous task. See below in this class for where
        // you need to implement methods for performing the network access
        // and plotting.
        new GetRandomSchedule().execute();
    }


    /**
     * Clear all schedules on the map
     */
    public void clearSchedules() {
        randomstudents = null;

        OverlayManager om = mapView.getOverlayManager();
        om.clear();
        scheduleOverlay.clear();
        buildingOverlay.removeAllItems();
        om.addAll(scheduleOverlay);
        om.add(buildingOverlay);
        mapView.invalidate();
    }

    /**
     * Find all possible locations at which "me" and random student could meet
     * up for the set day of the week and the set time to meet and the set
     * distance either "me" or random is willing to travel to meet.
     * A meetup is only possible if both "me" and random are free at the
     * time specified in the settings and each of us must have at least an hour
     * (>= 60 minutes) free. You should display dialog boxes if there are
     * conditions under which no meetup can happen (e.g., me or random is
     * in class at the specified time)
     *
     * /**
     *
     * @param none;
     * @return void:
     */

    /**
     * Default Strings to know what distance the user wants, what time of the day it is, and
     * what food the user wants at the moment.
     * Will need to update this string based on the settings dialog at appropriate
     * points in time.
     */
    private String activeKm = "closest_stop_me";
    private String activeTime = "12:00";
    private String activeFood = "Café";

    PlaceFactory placeFactory = PlaceFactory.getInstance();


    public void findMeetupPlace() {

        activeKm = this.sharedPreferences.getString("placeDistance", activeKm);
        activeTime = this.sharedPreferences.getString("timeOfDay", activeTime);
        activeFood = this.sharedPreferences.getString("typeOfFood", activeFood);

        Set<Place> myplaces;
        Set<Place> alotplaces;

        if (randomstudents == null || me.getSchedule() == null) {
            AlertDialog aDialogue = this.createSimpleDialog("Get a friend first!");
            aDialogue.show();

        } else {
            for (Student i : randomstudents) {
                if (i.getSchedule().getSections(activeDay).size() == 0) {
                    AlertDialog aDialogue = this.createSimpleDialog("Your friend has no classes on this day!");
                    aDialogue.show();
                } else if ((me.getSchedule().hasBreakAtTimeOfDay(activeDay, activeTime)) == false) {
                    AlertDialog aDialogue = this.createSimpleDialog("You're not free! Go to class!");
                    aDialogue.show();
                } else if (i.getSchedule().hasBreakAtTimeOfDay(activeDay, activeTime) == false) {
                    AlertDialog aDialog = this.createSimpleDialog("A friend is not free. Catch them later! :^)");
                    aDialog.show();
                } else {
                    Schedule myschedule = me.getSchedule();
                    Building myB = myschedule.whereAmI2(activeDay, activeTime);
                    LatLon mylatlon = myB.getLatLon();

                    Schedule alotschedule = i.getSchedule();
                    Building alotb = alotschedule.whereAmI2(activeDay, activeTime);
                    LatLon alotlatlon = alotb.getLatLon();

                    if (activeFood.equals("No preference")) {
                        myplaces = placeFactory.findPlacesWithinDistance(mylatlon, Integer.parseInt(activeKm));
                        alotplaces = placeFactory.findPlacesWithinDistance(alotlatlon, Integer.parseInt(activeKm));
                    } else {

                        myplaces = placeFactory.findPlacesWithinDistanceWithFood(mylatlon, Integer.parseInt(activeKm), activeFood);

                        alotplaces = placeFactory.findPlacesWithinDistanceWithFood(alotlatlon, Integer.parseInt(activeKm), activeFood);
                    }
                    Set<Place> places = new HashSet<Place>();
                    for (Place p : myplaces) {
                        if (alotplaces.contains(p)) {
                            places.add(p);
                        }
                    }


                    for (Place p : places) {
                        Double lat = p.getLatLon().getLatitude();
                        Double lng = p.getLatLon().getLongitude();
                        GeoPoint geoPoint = new GeoPoint(lat, lng);
                        OverlayItem item = new OverlayItem(p.getName(),
                                "Price: " + p.getPrice() + "\n" +
                                        "Type of Food: " + p.getCategory()
                                , geoPoint);
                        buildingOverlay.addItem(item);

                    }
                }
            }
            OverlayManager om = mapView.getOverlayManager();
            om.add(buildingOverlay);
            mapView.invalidate();
        }

    }


    /**
     * Initialize the PlaceFactory with information from FourSquare
     */
    /**
     * @return void:
     */
    public void initializePlaces() {
        // CPSC 210 Students: You should not need to touch this method, but
        // you will have to implement GetPlaces below.
        new GetPlaces().execute();
    }


    /**
     * Plot all buildings referred to in the given information about plotting
     * a schedule.
     *
     * @param schedulePlot All information about the schedule and route to plot.
     */
    private void plotBuildings(SchedulePlot schedulePlot) {
        if (schedulePlot == null || schedulePlot.getSections() == null) {
            AlertDialog aDialog = this.createSimpleDialog("No schedule to plot!");
            aDialog.show();
        } else {
            SortedSet<Section> sections = schedulePlot.getSections();
            for (Section i : sections) {

                plotABuilding(i.getBuilding(), i.getBuilding().getName(),
                        "Name: " + schedulePlot.getName() + "\n" +
                                "Course: " + i.getCourse().toString() + " " +
                                i.getName() + "\n" +
                                "Time: " + i.getCourseTime().toString()
                        , R.drawable.alien);
            }

            OverlayManager om = mapView.getOverlayManager();
            om.add(buildingOverlay);
            // CPSC 210 Students: You will need to ensure the buildingOverlay is in
            // the overlayManager. The following code achieves this. You should not likely
            // need to touch it
        }
    }

    /**
     * Plot all buildings referred to in the given information about plotting
     * a schedule for randomstudents
     *
     * @param schedulePlot All information about the schedule and route to plot.
     */
    private void plotBuildings2(SchedulePlot schedulePlot) {
        if (schedulePlot == null || schedulePlot.getSections() == null) {
            AlertDialog aDialog = this.createSimpleDialog("No schedule to plot!");
            aDialog.show();
        } else {
            SortedSet<Section> sections = schedulePlot.getSections();
            for (Section i : sections) {

                plotABuilding(i.getBuilding(), i.getBuilding().getName(),
                        "Name: " + schedulePlot.getName() + "\n" +
                                "Course: " + i.getCourse().toString() + " " +
                                i.getName() + "\n" +
                                "Time: " + i.getCourseTime().toString()
                        , R.drawable.friends2);
            }

            OverlayManager om = mapView.getOverlayManager();
            om.add(buildingOverlay);
        }
    }

    /**
     * Plot a building onto the map
     *
     * @param building      The building to put on the map
     * @param title         The title to put in the dialog box when the building is tapped on the map
     * @param msg           The message to display when the building is tapped
     * @param drawableToUse The icon to use. Can be R.drawable.ic_action_place (or any icon in the res/drawable directory)
     */
    private void plotABuilding(Building building, String title, String msg, int drawableToUse) {
        // CPSC 210 Students: You should not need to touch this method
        OverlayItem buildingItem = new OverlayItem(title, msg,
                new GeoPoint(building.getLatLon().getLatitude(), building.getLatLon().getLongitude()));

        //Create new marker
        Drawable icon = this.getResources().getDrawable(drawableToUse);


        //Set the bounding for the drawable
        icon.setBounds(
                0 - icon.getIntrinsicWidth() / 2, 0 - icon.getIntrinsicHeight(),
                icon.getIntrinsicWidth() / 2, 0);

        //Set the new marker to the overlay
        buildingItem.setMarker(icon);
        buildingOverlay.addItem(buildingItem);
    }


    /**
     * Initialize your schedule by coding it directly in. This is the schedule
     * that will appear on the map when you select "Show My Schedule".
     */
    private void initializeMySchedule() {
        // CPSC 210 Students; Implement this method

        studentManager = new StudentManager();
        studentManager.addStudent("Ang", "Sally", ME_ID); // ????????????????????
        studentManager.addSectionToSchedule(ME_ID, "CPSC", 210, "202");
        studentManager.addSectionToSchedule(ME_ID, "STAT", 241, "201");
        studentManager.addSectionToSchedule(ME_ID, "MATH", 221, "202");
        studentManager.addSectionToSchedule(ME_ID, "FREN", 111, "202");

        me = studentManager.get(ME_ID);
        me.getSchedule();


    }

    /**
     * Helper to create simple alert dialog to display message
     *
     * @param msg message to display in alert dialog
     * @return the alert dialog
     */
    private AlertDialog createSimpleDialog(String msg) {
        // CPSC 210 Students; You should not need to modify this method
        AlertDialog.Builder dialogBldr = new AlertDialog.Builder(getActivity());
        dialogBldr.setMessage(msg);
        dialogBldr.setNeutralButton(R.string.ok, null);

        return dialogBldr.create();
    }

    /**
     * Create the overlay used for buildings. CPSC 210 students, you should not need to
     * touch this method.
     *
     * @return An overlay
     */
    private ItemizedIconOverlay<OverlayItem> createBuildingOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());

        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> gestureListener =
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

                    /**
                     * Display building description in dialog box when user taps stop.
                     *
                     * @param index
                     *            index of item tapped
                     * @param oi
                     *            the OverlayItem that was tapped
                     * @return true to indicate that tap event has been handled
                     */
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem oi) {

                        new AlertDialog.Builder(getActivity())
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        if (selectedBuildingOnMap != null) {
                                            mapView.invalidate();
                                        }
                                    }
                                }).setTitle(oi.getTitle()).setMessage(oi.getSnippet())
                                .show();

                        selectedBuildingOnMap = oi;
                        mapView.invalidate();
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem oi) {
                        // do nothing
                        return false;
                    }
                };

        return new ItemizedIconOverlay<OverlayItem>(
                new ArrayList<OverlayItem>(), getResources().getDrawable(
                R.drawable.ic_action_place), gestureListener, rp);
    }


    private ItemizedIconOverlay<OverlayItem> createBuildingOverlay2() {
        ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());

        ItemizedIconOverlay.OnItemGestureListener<OverlayItem> gestureListener =
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

                    /**
                     * Display building description in dialog box when user taps stop.
                     *
                     * @param index
                     *            index of item tapped
                     * @param oi
                     *            the OverlayItem that was tapped
                     * @return true to indicate that tap event has been handled
                     */
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem oi) {

                        new AlertDialog.Builder(getActivity())
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        if (selectedBuildingOnMap != null) {
                                            mapView.invalidate();
                                        }
                                    }
                                }).setTitle(oi.getTitle()).setMessage(oi.getSnippet())
                                .show();

                        selectedBuildingOnMap = oi;
                        mapView.invalidate();
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem oi) {
                        // do nothing
                        return false;
                    }
                };

        return new ItemizedIconOverlay<OverlayItem>(
                new ArrayList<OverlayItem>(), getResources().getDrawable(
                R.drawable.ic_action_location_found), gestureListener, rp);
    }


    /**
     * Create overlay with a specific color
     *
     * @param colour A string with a hex colour value
     */
    private PathOverlay createPathOverlay(String colour) {
        // CPSC 210 Students, you should not need to touch this method
        PathOverlay po = new PathOverlay(Color.parseColor(colour),
                getActivity());
        Paint pathPaint = new Paint();
        pathPaint.setColor(Color.parseColor(colour));
        pathPaint.setStrokeWidth(4.0f);
        pathPaint.setStyle(Paint.Style.STROKE);
        po.setPaint(pathPaint);
        return po;
    }

    /**
     * Create a random colour
     *
     * @return String; the colour in hexcode
     */
    public String createRandomColour() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        String colour = String.format("#%06X", (0xFFFFFF & color));
        return colour;
    }


    // *********************** Asynchronous tasks

    /**
     * This asynchronous task is responsible for contacting the Meetup web service
     * for the schedule of a random student. The task must plot the retrieved
     * student's route for the schedule on the map in a different colour than the "me" schedule
     * or must display a dialog box that a schedule was not retrieved.
     */
    private class GetRandomSchedule extends AsyncTask<Void, Void, SchedulePlot> {

        // Some overview explanation of asynchronous tasks is on the project web page.

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected SchedulePlot doInBackground(Void... params) {

            randomstudents = new HashSet<Student>();

            try {
                JSONObject obj = new JSONObject(this.makeRoutingCall("http://kramer.nss.cs.ubc.ca:8081/getStudent"));

                String name = obj.getString("FirstName");
                String last = obj.getString("LastName");
                int Id = obj.getInt("Id");
                studentManager.addStudent(last, name, Id);
                Student s = studentManager.get(Id);
                randomstudents.add(s);

                JSONArray array = obj.getJSONArray("Sections");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj3 = array.getJSONObject(i);
                    String courseName = obj3.getString("CourseName");
                    int courseNumber = obj3.getInt("CourseNumber");
                    String sectionNumber = obj3.getString("SectionName");
                    studentManager.addSectionToSchedule(Id, courseName, courseNumber, sectionNumber);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
            for (Student i : randomstudents) {

                Schedule schedule = i.getSchedule();

                SortedSet<Section> section = schedule.getSections(activeDay);

                SchedulePlot schedulePlot = new SchedulePlot(section, i.getFirstName(), createRandomColour(), R.drawable.friends2);

                if (section.size() == 0) {
                    return null;
                }

                Iterator<Section> itr = section.iterator();
                Section s = itr.next();
                while (itr.hasNext()) {
                    Section sec = itr.next();
                    Double toLat = sec.getBuilding().getLatLon().getLatitude();
                    Double toLng = sec.getBuilding().getLatLon().getLongitude();
                    Double fromLat = s.getBuilding().getLatLon().getLatitude();
                    Double fromLng = s.getBuilding().getLatLon().getLongitude();

                    try {

                        JSONObject obj6 = new JSONObject(this.makeRoutingCall("http://open.mapquestapi.com/directions/v2/route?key=Fmjtd%7Cluu82luzl1%2Cbg%3Do5-948016&outFormat=json&routeType=pedestrian&timeType=1&enhancedNarrative=false&shapeFormat=raw&generalize=0&locale=en_US&unit=k&from=" + fromLat + "," + fromLng + "&to=" + toLat + "," + toLng));
                        JSONObject obj3 = obj6.getJSONObject("route");
                        JSONObject obj4 = obj3.getJSONObject("shape");
                        JSONArray array6 = obj4.getJSONArray("shapePoints");

                        for (int e = 0; e < array6.length(); e += 2) {
                            double lat = array6.getDouble(e);
                            double lng = array6.getDouble(e + 1);
                            GeoPoint geoPoint = new GeoPoint(lat, lng);
                            geoPoints.add(geoPoint);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    s = sec;
                }


                schedulePlot.setRoute(geoPoints);


                if (schedulePlot.getRoute() == null) {
                    return null;
                } else {

                    return schedulePlot;
                }
            }
            return null;
        }


        private String makeRoutingCall(String httpRequest) throws MalformedURLException, IOException {
            URL url = new URL(httpRequest);
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
            client.getContent();
            InputStream in = client.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String returnString = br.readLine();
            client.disconnect();
            return returnString;
        }

        @Override
        protected void onPostExecute(SchedulePlot schedulePlot) {
            // CPSC 210 students: When this method is called, it will be passed
            // whatever schedulePlot object you created (if any) in doBackground
            // above. Use it to plot the route.

            MapDisplayFragment.this.plotBuildings2(schedulePlot);

            if (schedulePlot == null) {
                AlertDialog aDialog = MapDisplayFragment.this.createSimpleDialog("This friend has no classes on this day!");
                aDialog.show();
            } else if (schedulePlot.getSections().size() == 1) {
                AlertDialog aDialog = MapDisplayFragment.this.createSimpleDialog("No route! Only one class?");
                aDialog.show();
            } else {
                PathOverlay po = createPathOverlay(schedulePlot.getColourOfLine());
                for (int i = 0; schedulePlot.getRoute().size() > i; i++) {
                    GeoPoint geoPoint = schedulePlot.getRoute().get(i);
                    po.addPoint(geoPoint);

                }

                scheduleOverlay.add(po);
                OverlayManager om = mapView.getOverlayManager();
                om.addAll(scheduleOverlay);
                mapView.invalidate();
            }


        }
    }


    /**
     * This asynchronous task is responsible for contacting the MapQuest web service
     * to retrieve a route between the buildings on the schedule and for plotting any
     * determined route on the map.
     */
    private class GetRoutingForSchedule extends AsyncTask<SchedulePlot, Void, SchedulePlot> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected SchedulePlot doInBackground(SchedulePlot... params) {

            // The params[0] element contains the schedulePlot object
            SchedulePlot scheduleToPlot = params[0];
            List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();


            Schedule schedule = me.getSchedule();

            SortedSet<Section> section = schedule.getSections(activeDay);
            Iterator<Section> itr = section.iterator();
            Section s = itr.next();
            while (itr.hasNext()) {
                Section sec = itr.next();

                Double toLat = sec.getBuilding().getLatLon().getLatitude();
                Double toLng = sec.getBuilding().getLatLon().getLongitude();
                Double fromLat = s.getBuilding().getLatLon().getLatitude();
                Double fromLng = s.getBuilding().getLatLon().getLongitude();

                try {
                    JSONObject obj = new JSONObject(this.makeRoutingCall("http://open.mapquestapi.com/directions/v2/route?key=Fmjtd%7Cluu82luzl1%2Cbg%3Do5-948016&outFormat=json&routeType=pedestrian&timeType=1&enhancedNarrative=false&shapeFormat=raw&generalize=0&locale=en_US&unit=k&from=" + fromLat + "," + fromLng + "&to=" + toLat + "," + toLng));
                    JSONObject obj3 = obj.getJSONObject("route");
                    JSONObject obj4 = obj3.getJSONObject("shape");
                    JSONArray array = obj4.getJSONArray("shapePoints");

                    for (int e = 0; e < array.length(); e += 2) {
                        double lat = array.getDouble(e);
                        double lng = array.getDouble(e + 1);
                        GeoPoint geoPoint = new GeoPoint(lat, lng);
                        geoPoints.add(geoPoint);
                    }

                } catch (MalformedURLException e) {
                    Log.e("BadURL", "Hmm", e);
                } catch (IOException e) {
                    Log.e("IOException", "L", e);
                } catch (JSONException e) {
                    Log.e("JSON", "K", e);
                } catch (Exception e) {
                    Log.e("GEN", "ok", e);
                }
                s = sec;


            }
            // set the route to the route i created;
            scheduleToPlot.setRoute(geoPoints);
            if (scheduleToPlot.getRoute() == null) {
                return null;
            } else {
                return scheduleToPlot;
            }
        }

        // CPSC 210 Students: Complete this method. This method should
        // call the MapQuest webservice to retrieve a List<GeoPoint>
        // that forms the routing between the buildings on the
        // schedule. The List<GeoPoint> should be put into
        // scheduleToPlot object.


        /**
         * An example helper method to call a web service
         */
        private String makeRoutingCall(String httpRequest) throws MalformedURLException, IOException {
            URL url = new URL(httpRequest);
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
            client.getContent();
            InputStream in = client.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String returnString = br.readLine();
            client.disconnect();
            return returnString;
        }

        @Override
        protected void onPostExecute(SchedulePlot schedulePlot) {

            // CPSC 210 Students: This method should plot the route onto the map
            // with the given line colour specified in schedulePlot. If there is
            // no route to plot, a dialog box should be displayed.

            MapDisplayFragment.this.plotBuildings(schedulePlot);
            if ((schedulePlot == null) || schedulePlot.getRoute().size() == 0) {
                AlertDialog aDialog = MapDisplayFragment.this.createSimpleDialog("No route today! Only one class?");
                aDialog.show();
            } else {

                PathOverlay po = createPathOverlay(schedulePlot.getColourOfLine());
                for (int i = 0; i < schedulePlot.getRoute().size(); i++) {
                    GeoPoint geoPoint = schedulePlot.getRoute().get(i);
                    po.addPoint(geoPoint);
                }

                scheduleOverlay.add(po);
                OverlayManager om = mapView.getOverlayManager();
                om.addAll(scheduleOverlay);
                mapView.invalidate();
            }


        }

    }

    /**
     * This asynchronous task is responsible for contacting the FourSquare web service
     * to retrieve all places around UBC that have to do with food. It should load
     * any determined places into PlaceFactory and then display a dialog box of how it did
     */

    // https://api.foursquare.com/v2/venues/explore?client_id=1RWGP1INTAMED321X2FSAUXHQQCHA54CAV2YL2JQLOPYTA14&
    // client_secret=VQCDBRYYS1I3MLZRM2BWG5KYKFW20KW50UYMK35FD4DTSEPW&v=20150320&ll=49.2611,-123.2531&section=food&radius=2000
    private class GetPlaces extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... params) {
            String sigh = null;
            try {
                sigh = makeRoutingCall("https://api.foursquare.com/v2/venues/explore?client_id" +
                        "=" + FOUR_SQUARE_CLIENT_ID + "&" +
                        "client_secret=" + FOUR_SQUARE_CLIENT_SECRET + "&v=20150320&" +
                        "ll=" + UBC_MARTHA_PIPER_FOUNTAIN.getLatitude() + "," + UBC_MARTHA_PIPER_FOUNTAIN.getLongitude() + "&section=food&radius=3000");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // CPSC 210 Students: Complete this method to retrieve a string
            // of JSON from FourSquare. Return the string from this method
            System.out.println(sigh);
            return sigh;

        }

        private String makeRoutingCall(String httpRequest) throws MalformedURLException, IOException {
            URL url = new URL(httpRequest);
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
            client.getContent();
            InputStream in = client.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String returnString = br.readLine();
            client.disconnect();
            return returnString;
        }

        protected void onPostExecute(String jSONOfPlaces) {

            try {
                JSONObject obj = new JSONObject(jSONOfPlaces);
                // JSONObject obj = obj1.getJSONObject("JSON");
                JSONObject response = obj.getJSONObject("response");
                JSONArray groups = response.getJSONArray("groups");
                JSONObject zero = groups.getJSONObject(0);
                JSONArray items = zero.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject obj4 = items.getJSONObject(i);

                    JSONObject venue = obj4.getJSONObject("venue");
                    String name = venue.getString("name");

                    JSONObject location = venue.getJSONObject("location");
                    Double lat = location.getDouble("lat");
                    Double lng = location.getDouble("lng");
                    LatLon cord = new LatLon(lat, lng);
                    JSONObject price = venue.getJSONObject("price");
                    int tier = price.getInt("tier");

                    JSONArray categories = venue.getJSONArray("categories");
                    JSONObject zeroo = categories.getJSONObject(0);
                    String category = zeroo.getString("name");

                    Place p = new Place(name, cord, category, tier);
                    placeFactory.add(p);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            AlertDialog aDialog = MapDisplayFragment.this.createSimpleDialog("There are " + placeFactory.getPlaces() + " places!");
            aDialog.show();

        }

        protected PlaceFactory getPlaces() {

            return placeFactory;
        }
    }

    /**
     * Initialize the CourseFactory with some courses.
     */
    private void initializeCourses() {
        // CPSC 210 Students: You can change this data if you desire.
        CourseFactory courseFactory = CourseFactory.getInstance();

        Building dmpBuilding = new Building("DMP", new LatLon(49.261474, -123.248060));

        Course cpsc210 = courseFactory.getCourse("CPSC", 210);
        Section aSection = new Section("202", "MWF", "12:00", "12:50", dmpBuilding);
        cpsc210.addSection(aSection);
        aSection.setCourse(cpsc210);
        aSection = new Section("201", "MWF", "16:00", "16:50", dmpBuilding);
        cpsc210.addSection(aSection);
        aSection.setCourse(cpsc210);
        aSection = new Section("BCS", "MWF", "12:00", "12:50", dmpBuilding);
        cpsc210.addSection(aSection);
        aSection.setCourse(cpsc210);

        Course engl222 = courseFactory.getCourse("ENGL", 222);
        aSection = new Section("007", "MWF", "14:00", "14:50", new Building("Buchanan", new LatLon(49.269258, -123.254784)));
        engl222.addSection(aSection);
        aSection.setCourse(engl222);

        Course scie220 = courseFactory.getCourse("SCIE", 220);
        aSection = new Section("200", "MWF", "18:00", "18:50", new Building("Swing", new LatLon(49.262786, -123.255044)));
        scie220.addSection(aSection);
        aSection.setCourse(scie220);

        Course phil100 = courseFactory.getCourse("PHIL", 100);
        aSection = new Section("101", "MWF", "18:00", "18:50", new Building("Barber", new LatLon(49.267442, -123.252471)));
        phil100.addSection(aSection);
        aSection.setCourse(phil100);

        Course math200 = courseFactory.getCourse("MATH", 200);
        aSection = new Section("201", "MWF", "09:00", "09:50", new Building("Buchanan", new LatLon(49.269258, -123.254784)));
        math200.addSection(aSection);
        aSection.setCourse(math200);

        Course fren102 = courseFactory.getCourse("FREN", 102);
        aSection = new Section("202", "MWF", "11:00", "11:50", new Building("Barber", new LatLon(49.267442, -123.252471)));
        fren102.addSection(aSection);
        aSection.setCourse(fren102);

        Course japn103 = courseFactory.getCourse("JAPN", 103);
        aSection = new Section("002", "MWF", "10:00", "11:50", new Building("Buchanan", new LatLon(49.269258, -123.254784)));
        japn103.addSection(aSection);
        aSection.setCourse(japn103);

        Course scie113 = courseFactory.getCourse("SCIE", 113);
        aSection = new Section("213", "MWF", "18:00", "18:50", new Building("Swing", new LatLon(49.262786, -123.255044)));
        scie113.addSection(aSection);
        aSection.setCourse(scie113);

        Course micb308 = courseFactory.getCourse("MICB", 308);
        aSection = new Section("201", "MWF", "12:00", "12:50", new Building("Woodward", new LatLon(49.264704, -123.247536)));
        micb308.addSection(aSection);
        aSection.setCourse(micb308);

        Course micb307 = courseFactory.getCourse("MICB", 307);
        aSection = new Section("201", "MWF", "18:00", "18:50", new Building("Woodward", new LatLon(49.264704, -123.247536)));
        micb307.addSection(aSection);
        aSection.setCourse(micb307);

        Course math221 = courseFactory.getCourse("MATH", 221);
        aSection = new Section("202", "TR", "11:00", "12:20", new Building("Klinck", new LatLon(49.266112, -123.254776)));
        math221.addSection(aSection);
        aSection.setCourse(math221);

        Course phys203 = courseFactory.getCourse("PHYS", 203);
        aSection = new Section("201", "TR", "09:30", "10:50", new Building("Hennings", new LatLon(49.266400, -123.252047)));
        phys203.addSection(aSection);
        aSection.setCourse(phys203);

        Course crwr209 = courseFactory.getCourse("CRWR", 209);
        aSection = new Section("002", "TR", "12:30", "13:50", new Building("Geography", new LatLon(49.266039, -123.256129)));
        crwr209.addSection(aSection);
        aSection.setCourse(crwr209);

        Course fnh330 = courseFactory.getCourse("FNH", 330);
        aSection = new Section("002", "TR", "15:00", "16:20", new Building("MacMillian", new LatLon(49.261167, -123.251157)));
        fnh330.addSection(aSection);
        aSection.setCourse(fnh330);

        Course cpsc499 = courseFactory.getCourse("CPSC", 430);
        aSection = new Section("201", "TR", "16:20", "17:50", new Building("Liu", new LatLon(49.267632, -123.259334)));
        cpsc499.addSection(aSection);
        aSection.setCourse(cpsc499);

        Course chem250 = courseFactory.getCourse("CHEM", 250);
        aSection = new Section("203", "TR", "10:00", "11:20", new Building("Klinck", new LatLon(49.266112, -123.254776)));
        chem250.addSection(aSection);
        aSection.setCourse(chem250);

        Course eosc222 = courseFactory.getCourse("EOSC", 222);
        aSection = new Section("200", "TR", "11:00", "12:20", new Building("ESB", new LatLon(49.262866, -123.25323)));
        eosc222.addSection(aSection);
        aSection.setCourse(eosc222);

        Course biol201 = courseFactory.getCourse("BIOL", 201);
        aSection = new Section("201", "TR", "14:00", "15:20", new Building("BioSci", new LatLon(49.263920, -123.251552)));
        biol201.addSection(aSection);
        aSection.setCourse(biol201);

        Course stat241 = courseFactory.getCourse("STAT", 241);
        aSection = new Section("201", "MWF", "8:00", "8:50", new Building("ESB", new LatLon(49.262866, -123.25323)));
        stat241.addSection(aSection);
        aSection.setCourse(stat241);

        Course psyc207 = courseFactory.getCourse("PSYC", 207);
        aSection = new Section("201", "MWF", "13:00", "13:50", new Building("ESB", new LatLon(49.262866, -123.25323)));
        psyc207.addSection(aSection);
        aSection.setCourse(psyc207);

        Course fren111 = courseFactory.getCourse("FREN", 111);
        aSection = new Section("202", "MWF", "10:00", "10:50", new Building("Buchanan", new LatLon(49.269258, -123.254784)));
        fren111.addSection(aSection);
        aSection.setCourse(fren111);
    }

}
