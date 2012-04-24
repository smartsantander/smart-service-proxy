
package eu.spitfire_project.smart_service_proxy.backends.parking.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ParkingLot complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParkingLot">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parkingLotAddress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parkingSpaces" type="{http://smartsantander.eu/parkingservice/types}ParkingSpace" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParkingLot", propOrder = {
    "parkingLotAddress",
    "parkingSpaces"
})
public class ParkingLot {

    @XmlElement(required = true)
    protected String parkingLotAddress;
    protected List<ParkingSpace> parkingSpaces;

    /**
     * Gets the value of the parkingLotAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParkingLotAddress() {
        return parkingLotAddress;
    }

    /**
     * Sets the value of the parkingLotAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParkingLotAddress(String value) {
        this.parkingLotAddress = value;
    }

    /**
     * Gets the value of the parkingSpaces property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parkingSpaces property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParkingSpaces().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ParkingSpace }
     * 
     * 
     */
    public List<ParkingSpace> getParkingSpaces() {
        if (parkingSpaces == null) {
            parkingSpaces = new ArrayList<ParkingSpace>();
        }
        return this.parkingSpaces;
    }

}
