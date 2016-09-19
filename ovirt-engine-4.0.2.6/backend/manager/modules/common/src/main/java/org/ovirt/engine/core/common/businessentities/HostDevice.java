package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class HostDevice implements IVdcQueryable, BusinessEntity<HostDeviceId>, Nameable {

    private static final String CAPABILITY_PCI = "pci";

    private Guid hostId;
    private String deviceName;
    private String parentDeviceName;
    private String capability;
    private Integer iommuGroup;
    private String productName;
    private String productId;
    private String vendorName;
    private String vendorId;
    private String parentPhysicalFunction;
    private Integer totalVirtualFunctions;
    private String networkInterfaceName;
    private Guid vmId;
    private String driver;
    private boolean assignable;

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setCapability(String capability) {
        this.capability = capability;
    }

    public String getCapability() {
        return capability;
    }

    public void setIommuGroup(Integer iommuGroup) {
        this.iommuGroup = iommuGroup;
    }

    public Integer getIommuGroup() {
        return iommuGroup;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setParentDeviceName(String parentDeviceName) {
        this.parentDeviceName = parentDeviceName;
    }

    public String getParentDeviceName() {
        return parentDeviceName;
    }

    public String getParentPhysicalFunction() {
        return parentPhysicalFunction;
    }

    public void setParentPhysicalFunction(String parentPhysicalFunction) {
        this.parentPhysicalFunction = parentPhysicalFunction;
    }

    public String getNetworkInterfaceName() {
        return networkInterfaceName;
    }

    public void setNetworkInterfaceName(String networkInterfaceName) {
        this.networkInterfaceName = networkInterfaceName;
    }

    public Integer getTotalVirtualFunctions() {
        return totalVirtualFunctions;
    }

    public void setTotalVirtualFunctions(Integer totalVirtualFunctions) {
        this.totalVirtualFunctions = totalVirtualFunctions;
    }

    public void setAssignable(boolean assignable) {
        this.assignable = assignable;
    }

    public boolean isAssignable() {
        return assignable;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public Guid getVmId() {
        return vmId;
    }

    @Override
    public HostDeviceId getId() {
        return new HostDeviceId(hostId, deviceName);
    }

    @Override
    public void setId(HostDeviceId id) {
        setHostId(id.getHostId());
        setDeviceName(id.getDeviceName());
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public boolean isPci() {
        return CAPABILITY_PCI.equals(getCapability());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HostDevice)) {
            return false;
        }
        HostDevice other = (HostDevice) obj;
        return Objects.equals(getId(), other.getId())
                && Objects.equals(parentDeviceName, other.parentDeviceName)
                && Objects.equals(capability, other.capability)
                && Objects.equals(iommuGroup, other.iommuGroup)
                && Objects.equals(productName, other.productName)
                && Objects.equals(productId, other.productId)
                && Objects.equals(vendorName, other.vendorName)
                && Objects.equals(vendorId, other.vendorId)
                && Objects.equals(parentPhysicalFunction, other.parentPhysicalFunction)
                && Objects.equals(totalVirtualFunctions, other.totalVirtualFunctions)
                && Objects.equals(networkInterfaceName, other.networkInterfaceName)
                && Objects.equals(driver, other.driver)
                && Objects.equals(assignable, other.assignable)
                && Objects.equals(vmId, other.vmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getId(),
                parentDeviceName,
                capability,
                iommuGroup,
                productName,
                productId,
                vendorName,
                vendorId,
                parentPhysicalFunction,
                totalVirtualFunctions,
                networkInterfaceName,
                driver,
                assignable,
                vmId
        );
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("hostId", hostId)
                .append("deviceName", deviceName)
                .append("parentDeviceName", parentDeviceName)
                .append("capability", capability)
                .append("iommuGroup", iommuGroup)
                .append("productName", productName)
                .append("productId", productId)
                .append("vendorName", vendorName)
                .append("vendorId", vendorId)
                .append("parentPhysicalFunction", parentPhysicalFunction)
                .append("totalVirtualFunctions", totalVirtualFunctions)
                .append("networkInterfaceName", networkInterfaceName)
                .append("driver", driver)
                .append("assignable", assignable)
                .append("vmId", vmId)
                .build();
    }

    @Override
    public String getName() {
        return getDeviceName();
    }
}
