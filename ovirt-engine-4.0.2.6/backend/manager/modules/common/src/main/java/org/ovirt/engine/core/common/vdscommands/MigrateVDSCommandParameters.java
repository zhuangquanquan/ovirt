package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.MigrationMethod;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class MigrateVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String srcHost;
    private Guid dstVdsId;
    private String dstHost;
    private MigrationMethod migrationMethod;
    private boolean tunnelMigration;
    private String dstQemu;
    private Version clusterVersion;
    private Integer migrationDowntime;
    private Boolean autoConverge;
    private Boolean migrateCompressed;
    private String consoleAddress;
    private Integer maxBandwidth;
    private Boolean enableGuestEvents;

    private Map<String, Object> convergenceSchedule;

    public MigrateVDSCommandParameters(Guid vdsId, Guid vmId, String srcHost, Guid dstVdsId,
                                       String dstHost, MigrationMethod migrationMethod, boolean tunnelMigration,
                                       String dstQemu, Version clusterVersion, int migrationDowntime,
                                       Boolean autoConverge, Boolean migrateCompressed, String consoleAddress,
                                       Integer maxBandwidth, Map<String, Object> convergenceSchedule, Boolean enableGuestEvents) {
        super(vdsId, vmId);
        this.srcHost = srcHost;
        this.dstVdsId = dstVdsId;
        this.dstHost = dstHost;
        this.migrationMethod = migrationMethod;
        this.tunnelMigration = tunnelMigration;
        this.dstQemu = dstQemu;
        this.clusterVersion = clusterVersion;
        this.migrationDowntime = migrationDowntime;
        this.autoConverge = autoConverge;
        this.migrateCompressed = migrateCompressed;
        this.consoleAddress = consoleAddress;
        this.maxBandwidth = maxBandwidth;
        this.convergenceSchedule = convergenceSchedule;
        this.enableGuestEvents = enableGuestEvents;
    }

    public String getSrcHost() {
        return srcHost;
    }

    public Guid getDstVdsId() {
        return dstVdsId;
    }

    public String getDstHost() {
        return dstHost;
    }

    public MigrationMethod getMigrationMethod() {
        return migrationMethod;
    }

    public boolean isTunnelMigration() {
        return tunnelMigration;
    }

    public String getDstQemu() {
        return dstQemu;
    }

    public int getMigrationDowntime() {
        return migrationDowntime;
    }

    public Boolean getMigrateCompressed() {
        return migrateCompressed;
    }

    public void setMigrateCompressed(Boolean migrateCompressed) {
        this.migrateCompressed = migrateCompressed;
    }

    public Boolean getAutoConverge() {
        return autoConverge;
    }

    public void setAutoConverge(Boolean autoConverge) {
        this.autoConverge = autoConverge;
    }

    public MigrateVDSCommandParameters() {
        migrationMethod = MigrationMethod.OFFLINE;
    }

    public void setClusterVersion(Version clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    public Version getClusterVersion() {
        return clusterVersion;
    }

    public String getConsoleAddress() {
        return consoleAddress;
    }

    public void setConsoleAddress(String consoleAddress) {
        this.consoleAddress = consoleAddress;
    }

    public Map<String, Object> getConvergenceSchedule() {
        return convergenceSchedule;
    }

    public void setConvergenceSchedule(Map<String, Object> convergenceSchedule) {
        this.convergenceSchedule = convergenceSchedule;
    }

    public Integer getMaxBandwidth() {
        return maxBandwidth;
    }

    public void setMaxBandwidth(Integer maxBandwidth) {
        this.maxBandwidth = maxBandwidth;
    }

    public Boolean isEnableGuestEvents() {
        return enableGuestEvents;
    }

    public void setEnableGuestEvents(Boolean enableGuestEvents) {
        this.enableGuestEvents = enableGuestEvents;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("srcHost", getSrcHost())
                .append("dstVdsId", getDstVdsId())
                .append("dstHost", getDstHost())
                .append("migrationMethod", getMigrationMethod())
                .append("tunnelMigration", isTunnelMigration())
                .append("migrationDowntime", getMigrationDowntime())
                .append("autoConverge", getAutoConverge())
                .append("migrateCompressed", getMigrateCompressed())
                .append("consoleAddress", getConsoleAddress())
                .append("maxBandwidth", getMaxBandwidth())
                .append("enableGuestEvents", isEnableGuestEvents())
                .append("convergenceSchedule", getConvergenceSchedule());
    }
}
