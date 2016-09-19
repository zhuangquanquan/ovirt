package org.ovirt.engine.core.common.businessentities;

import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

/**
 * The VmDevice represents a managed or unmanaged VM device.<br>
 * The VmDevice data consists of this entity which holds the immutable fields of the device.<br>
 * Each device have a link to its VM id , an address and optional boot order and additional special device parameters. <br>
 * This BE holds both managed (disk, network interface etc.) and unmanaged (sound, video etc.) devices.
 */

public class VmDevice implements IVdcQueryable, BusinessEntity<VmDeviceId>, Comparable<VmDevice> {

    /**
     * Needed for java serialization/deserialization mechanism.
     */
    private static final long serialVersionUID = 826297167224396854L;

    /**
     * Device id and Vm id pair as a compound key
     */
    private VmDeviceId id;

    /**
     * The device name.
     */
    private String device;

    /**
     * The device type.
     */
    private VmDeviceGeneralType type;

    /**
     * The device address.
     */
    private String address;

    /**
     * The device boot order (if applicable).
     */
    private int bootOrder;

    /**
     * The device special parameters.
     */
    private Map<String, Object> specParams;

    /**
     * The device managed/unmanaged flag
     */
    private boolean isManaged;

    /**
     * The device plugged flag
     */
    private boolean isPlugged;

    /**
     * The device read-only flag
     */
    private Boolean isReadOnly;

    /**
     * The device flag indicating whether the device
     * is a device from a taken snapshot
     */
    private Guid snapshotId;

    /**
     * The device alias.
     */
    private String alias;

    /**
     * The device logical name.
     */
    private String logicalName;

    /**
     * A flag indicates whether the device uses scsi reservation.
     */
    private boolean usingScsiReservation;

    /**
     * Map of custom properties
     */
    private Map<String, String> customProperties;

    /**
     * The passthrough host device this vm device represents in case there is any.
     */
    private String hostDevice;

    public VmDevice() {
        isPlugged = Boolean.FALSE;
        alias = "";
    }

    public VmDevice(VmDeviceId id, VmDeviceGeneralType type, String device, String address,
                    int bootOrder,
                    Map<String, Object> specParams,
                    boolean isManaged,
                    Boolean isPlugged,
                    Boolean isReadOnly,
                    String alias,
                    Map<String, String> customProperties,
                    Guid snapshotId,
                    String logicalName) {
        this(id, type, device, address, bootOrder, specParams, isManaged, isPlugged, isReadOnly, alias, customProperties, snapshotId, logicalName, false);
    }

    public VmDevice(VmDeviceId id, VmDeviceGeneralType type, String device, String address,
                    int bootOrder,
                    Map<String, Object> specParams,
                    boolean isManaged,
                    Boolean isPlugged,
                    Boolean isReadOnly,
                    String alias,
                    Map<String, String> customProperties,
                    Guid snapshotId,
                    String logicalName,
                    boolean isUsingScsiReservation) {
        this.id = id;
        this.type = type;
        this.device = device;
        this.address = address;
        this.bootOrder = bootOrder;
        this.specParams = specParams;
        this.isManaged = isManaged;
        this.isPlugged = isPlugged;
        this.isReadOnly = isReadOnly;
        this.alias = alias;
        this.customProperties = customProperties;
        this.snapshotId = snapshotId;
        this.logicalName = logicalName;
        this.usingScsiReservation = isUsingScsiReservation;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public VmDeviceId getId() {
        return id;
    }

    @Override
    public void setId(VmDeviceId id) {
        this.id = id;
    }

    public Guid getDeviceId() {
        return id.getDeviceId();
    }

    public void setDeviceId(Guid deviceId) {
        id.setDeviceId(deviceId);
    }

    public Guid getVmId() {
        return id.getVmId();
    }

    public void setVmId(Guid vmId) {
        this.id.setVmId(vmId);
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public VmDeviceGeneralType getType() {
        return type;
    }

    public void setType(VmDeviceGeneralType type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getBootOrder() {
        return bootOrder;
    }

    public void setBootOrder(int bootOrder) {
        this.bootOrder = bootOrder;
    }

    public Map<String, Object> getSpecParams() {
        return specParams;
    }

    public void setSpecParams(Map<String, Object> specParams) {
        this.specParams = specParams;
    }

    public boolean getIsManaged() {
        return isManaged;
    }

    public void setIsManaged(boolean isManaged) {
        this.isManaged = isManaged;
    }

    public boolean getIsPlugged() {
        return isPlugged;
    }

    public void setIsPlugged(boolean isPlugged) {
        this.isPlugged = isPlugged;
    }

    public Boolean getIsReadOnly() {
        return isReadOnly == null ? Boolean.FALSE : isReadOnly;
    }

    public void setIsReadOnly(Boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getLogicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public boolean isUsingScsiReservation() {
        return usingScsiReservation;
    }

    public void setUsingScsiReservation(boolean usesScsiReservation) {
        this.usingScsiReservation = usesScsiReservation;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public String getHostDevice() {
        return hostDevice;
    }

    public void setHostDevice(String hostDevice) {
        this.hostDevice = hostDevice;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                device,
                type,
                address,
                bootOrder,
                specParams,
                isManaged,
                isPlugged,
                getIsReadOnly(),
                alias,
                customProperties,
                snapshotId,
                logicalName,
                usingScsiReservation,
                hostDevice
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmDevice)) {
            return false;
        }
        VmDevice other = (VmDevice) obj;
        return Objects.equals(id, other.id)
                && device.equals(other.device)
                && type.equals(other.type)
                && address.equals(other.address)
                && bootOrder == other.bootOrder
                && Objects.equals(specParams, other.specParams)
                && isManaged == other.isManaged
                && getIsPlugged() == other.getIsPlugged()
                && getIsReadOnly().equals(other.getIsReadOnly())
                && alias.equals(other.alias)
                && Objects.equals(customProperties, other.customProperties)
                && Objects.equals(snapshotId, other.snapshotId)
                && Objects.equals(logicalName, other.logicalName)
                && usingScsiReservation == other.usingScsiReservation
                && Objects.equals(hostDevice, other.hostDevice);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", id)
                .append("device", getDevice())
                .append("type", getType())
                .append("bootOrder", getBootOrder())
                .append("specParams", getSpecParams())
                .append("address", getAddress())
                .append("managed", getIsManaged())
                .append("plugged", getIsPlugged())
                .append("readOnly", getIsReadOnly())
                .append("deviceAlias", getAlias())
                .append("customProperties", getCustomProperties())
                .append("snapshotId", getSnapshotId())
                .append("logicalName", getLogicalName())
                .append("usingScsiReservation", isUsingScsiReservation())
                .append("hostDevice", getHostDevice())
                .build();
    }

    @Override
    public int compareTo(VmDevice other) {
        return BusinessEntityComparator.<VmDevice, VmDeviceId>newInstance().compare(this, other);
    }
}
