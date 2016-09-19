package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Collection;
import java.util.Collections;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class CloneSingleCinderDiskCommand<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {

    private CinderDisk disk;

    public CloneSingleCinderDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStorageDomainId(parameters.getStorageDomainId());
    }

    @Override
    protected void executeCommand() {
        lockImage();
        CinderDisk cinderDisk = getDisk();
        cinderDisk.setDiskAlias(getParameters().getDiskAlias());
        String volumeId = getNewVolumeCinderDisk(cinderDisk);
        cinderDisk.setId(Guid.createGuidFromString(volumeId));
        cinderDisk.setImageId(Guid.createGuidFromString(volumeId));
        cinderDisk.setImageStatus(ImageStatus.LOCKED);
        cinderDisk.setVolumeClassification(VolumeClassification.Volume);
        cinderDisk.setVmSnapshotId(getParameters().getVmSnapshotId());

        // If we clone a disk from snapshot, update the volume with the appropriate parameters.
        if (!cinderDisk.getActive()) {
            cinderDisk.setActive(true);
            cinderDisk.setParentId(Guid.Empty);
        }
        addCinderDiskTemplateToDB(cinderDisk);

        getReturnValue().setActionReturnValue(cinderDisk.getId());
        getParameters().setDestinationImageId(Guid.createGuidFromString(volumeId));
        getParameters().setContainerId(Guid.createGuidFromString(volumeId));
        persistCommand(getParameters().getParentCommand(), true);
        setSucceeded(true);
    }

    private String getNewVolumeCinderDisk(CinderDisk cinderDisk) {
        String volumeId;
        if (cinderDisk.getActive()) {
            volumeId = getCinderBroker().cloneDisk(cinderDisk);
        } else {
            volumeId = getCinderBroker().cloneVolumeFromSnapshot(cinderDisk, cinderDisk.getImageId());
        }
        return volumeId;
    }

    protected void addCinderDiskTemplateToDB(final CinderDisk cinderDisk) {
        TransactionSupport.executeInNewTransaction(() -> {
            getBaseDiskDao().save(cinderDisk);
            getImageDao().save(cinderDisk.getImage());
            DiskImageDynamic diskDynamic = new DiskImageDynamic();
            diskDynamic.setId(cinderDisk.getImageId());
            getDiskImageDynamicDao().save(diskDynamic);
            ImageStorageDomainMap image_storage_domain_map = new ImageStorageDomainMap(cinderDisk.getImageId(),
                    cinderDisk.getStorageIds().get(0), cinderDisk.getQuotaId(), cinderDisk.getDiskProfileId());
            getDbFacade().getImageStorageDomainMapDao().save(image_storage_domain_map);

            getCompensationContext().snapshotNewEntity(image_storage_domain_map);
            getCompensationContext().snapshotNewEntity(diskDynamic);
            getCompensationContext().snapshotNewEntity(cinderDisk.getImage());
            getCompensationContext().snapshotNewEntity(cinderDisk);
            getCompensationContext().stateChanged();
            return null;
        });
    }

    protected CinderDisk getDisk() {
        if (disk == null) {
            disk = (CinderDisk) getDiskImageDao().getSnapshotById(getImageId());
        }
        return disk;
    }

    @Override
    protected void lockImage() {
        ImagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.LOCKED);
    }

    @Override
    public CommandCallback getCallback() {
        return new CloneSingleCinderDiskCommandCallback();
    }

    @Override
    protected void endSuccessfully() {
        ImagesHandler.updateImageStatus(getParameters().getDestinationImageId(), ImageStatus.OK);
        ImagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.OK);
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        ImagesHandler.updateImageStatus(getParameters().getDestinationImageId(), ImageStatus.ILLEGAL);
        removeCinderDisk();
        ImagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.OK);
        setSucceeded(true);
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getStorageDomainId()));
    }

    private void removeCinderDisk() {
        runInternalAction(VdcActionType.RemoveCinderDisk,
                buildRevertParameters(getParameters().getDestinationImageId()),
                null);
    }

    private RemoveCinderDiskParameters buildRevertParameters(Guid cinderDiskId) {
        RemoveCinderDiskParameters removeDiskParams = new RemoveCinderDiskParameters(cinderDiskId);
        removeDiskParams.setLockVM(false);
        removeDiskParams.setShouldBeLogged(false);
        return removeDiskParams;
    }
}
