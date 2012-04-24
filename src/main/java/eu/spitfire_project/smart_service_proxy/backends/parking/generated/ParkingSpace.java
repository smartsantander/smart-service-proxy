
package eu.spitfire_project.smart_service_proxy.backends.parking.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ParkingSpace complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParkingSpace">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parkingSpaceType" type="{http://smartsantander.eu/parkingservice/types}ParkingSpaceType"/>
 *         &lt;element name="currentStatus" type="{http://smartsantander.eu/parkingservice/types}SpaceCurrentStatusType"/>
 *         &lt;element name="parkingSpaceCoordinates" type="{http://smartsantander.eu/parkingservice/types}GPSCoordinates"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParkingSpace", propOrder = {
    "parkingSpaceType",
    "currentStatus",
    "parkingSpaceCoordinates"
})
public class ParkingSpace {

    @XmlElement(required = true)
    protected ParkingSpaceType parkingSpaceType;
    @XmlElement(required = true)
    protected SpaceCurrentStatusType currentStatus;
    @XmlElement(required = true)
    protected GPSCoordinates parkingSpaceCoordinates;

    /**
     * Gets the value of the parkingSpaceType property.
     * 
     * @return
     *     possible object is
     *     {@link ParkingSpaceType }
     *     
     */
    public ParkingSpaceType getParkingSpaceType() {
        return parkingSpaceType;
    }

    /**
     * Sets the value of the parkingSpaceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParkingSpaceType }
     *     
     */
    public void setParkingSpaceType(ParkingSpaceType value) {
        this.parkingSpaceType = value;
    }

    /**
     * Gets the value of the currentStatus property.
     * 
     * @return
     *     possible object is
     *     {@link SpaceCurrentStatusType }
     *     
     */
    public SpaceCurrentStatusType getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Sets the value of the currentStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link SpaceCurrentStatusType }
     *     
     */
    public void setCurrentStatus(SpaceCurrentStatusType value) {
        this.currentStatus = value;
    }

    /**
     * Gets the value of the parkingSpaceCoordinates property.
     * 
     * @return
     *     possible object is
     *     {@link GPSCoordinates }
     *     
     */
    public GPSCoordinates getParkingSpaceCoordinates() {
        return parkingSpaceCoordinates;
    }

    /**
     * Sets the value of the parkingSpaceCoordinates property.
     * 
     * @param value
     *     allowed object is
     *     {@link GPSCoordinates }
     *     
     */
    public void setParkingSpaceCoordinates(GPSCoordinates value) {
        this.parkingSpaceCoordinates = value;
    }

}
