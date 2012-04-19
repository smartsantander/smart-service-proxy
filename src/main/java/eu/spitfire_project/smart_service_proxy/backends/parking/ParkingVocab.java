package eu.spitfire_project.smart_service_proxy.backends.parking;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Vocabulary definitions from C:\Users\massel\temp\parking.ttl 
 * @author Auto-generated by schemagen on 19 Apr 2012 13:37 
 */
public class ParkingVocab {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://spitfire-project.eu/cc/parking";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>A vehicle books a parking lot.</p> */
    public static final Property PARKINGBOOKS = m_model.createProperty( "http://spitfire-project.eu/cc/parkingbooks" );
    
    /** <p>Identification label for a parking lot.</p> */
    public static final Property PARKINGID = m_model.createProperty( "http://spitfire-project.eu/cc/parkingid" );
    
    /** <p>A vehicle leaves a parking lot.</p> */
    public static final Property PARKINGLEAVES = m_model.createProperty( "http://spitfire-project.eu/cc/parkingleaves" );
    
    /** <p>A vehicle occupies a parking lot.</p> */
    public static final Property PARKINGOCCUPIES = m_model.createProperty( "http://spitfire-project.eu/cc/parkingoccupies" );
    
    /** <p>A vehicle reserves a parking lot.</p> */
    public static final Property PARKINGRESERVES = m_model.createProperty( "http://spitfire-project.eu/cc/parkingreserves" );
    
    /** <p>Status of a a parking lot.</p> */
    public static final Property PARKINGSTATUS = m_model.createProperty( "http://spitfire-project.eu/cc/parkingstatus" );
    
    /** <p>The entrance to a parking area.</p> */
    public static final Resource PARKING_AREA_ENTRANCE = m_model.createResource( "http://spitfire-project.eu/cc/parkingAreaEntrance" );
    
    /** <p>The exit from a parking area.</p> */
    public static final Resource PARKING_AREA_EXIT = m_model.createResource( "http://spitfire-project.eu/cc/parkingAreaExit" );
    
    /** <p>A parking lot that is available.</p> */
    public static final Resource PARKING_AVAILABLE_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingAvailableLot" );
    
    /** <p>A parking lot reserved to bicycles.</p> */
    public static final Resource PARKING_BICYCLE_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingBicycleLot" );
    
    /** <p>A parking lot subject to being reserved or booked.</p> */
    public static final Resource PARKING_BOOKABLE_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingBookableLot" );
    
    /** <p>A parking lot that is booked.</p> */
    public static final Resource PARKING_BOOKED_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingBookedLot" );
    
    /** <p>Process of booking a parking facility for a specific individual or set of 
     *  individuals. It involves a purchase act.</p>
     */
    public static final Resource PARKING_BOOKING = m_model.createResource( "http://spitfire-project.eu/cc/parkingBooking" );
    
    /** <p>A parking lot reserved for cars.</p> */
    public static final Resource PARKING_CAR_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingCarLot" );
    
    /** <p>Parking lot covered by a roof.</p> */
    public static final Resource PARKING_COVERED_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingCoveredLot" );
    
    /** <p>An indoor parking area.</p> */
    public static final Resource PARKING_INDOOR_AREA = m_model.createResource( "http://spitfire-project.eu/cc/parkingIndoorArea" );
    
    /** <p>Process of leaving a parking lot.</p> */
    public static final Resource PARKING_LEAVING = m_model.createResource( "http://spitfire-project.eu/cc/parkingLeaving" );
    
    /** <p>Status of a parking lot e.g., booked, reserved or unavailable.</p> */
    public static final Resource PARKING_LOT_STATUS = m_model.createResource( "http://spitfire-project.eu/cc/parkingLotStatus" );
    
    /** <p>A parking lot reserved to motorcycles.</p> */
    public static final Resource PARKING_MOTORCYCLE_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingMotorcycleLot" );
    
    /** <p>Process of occupying a parking lot.</p> */
    public static final Resource PARKING_OCCUPANCY = m_model.createResource( "http://spitfire-project.eu/cc/parkingOccupancy" );
    
    /** <p>An open air parking area.</p> */
    public static final Resource PARKING_OUTDOOR_AREA = m_model.createResource( "http://spitfire-project.eu/cc/parkingOutdoorArea" );
    
    /** <p>The area that includes parking lots.</p> */
    public static final Resource PARKING_PARKING_AREA = m_model.createResource( "http://spitfire-project.eu/cc/parkingParkingArea" );
    
    /** <p>The area in which transportation vehicles can be parked.</p> */
    public static final Resource PARKING_PARKING_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingParkingLot" );
    
    /** <p>Process of reserving a parking facility for a specific individual or set of 
     *  individuals.</p>
     */
    public static final Resource PARKING_RESERVATION = m_model.createResource( "http://spitfire-project.eu/cc/parkingReservation" );
    
    /** <p>A parking lot that is reserved.</p> */
    public static final Resource PARKING_RESERVED_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingReservedLot" );
    
    /** <p>A parking lot that is unavailable.</p> */
    public static final Resource PARKING_UNAVAILABLE_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingUnavailableLot" );
    
    /** <p>Parking lot not covered by any roof.</p> */
    public static final Resource PARKING_UNCOVERED_LOT = m_model.createResource( "http://spitfire-project.eu/cc/parkingUncoveredLot" );
    
}
