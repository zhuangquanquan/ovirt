

----------------------------------------------------------------
-- [all_disks] View
--
CREATE OR REPLACE FUNCTION GetAllFromDisks (
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF all_disks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM all_disks
    WHERE (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_disk_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = disk_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDiskByDiskId (
    v_disk_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF all_disks_including_memory STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM all_disks_including_memory
    WHERE image_group_id = v_disk_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_disk_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_disk_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDisksVmGuid (
    v_vm_guid UUID,
    v_only_plugged BOOLEAN,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF all_disks_for_vms STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM all_disks_for_vms
    WHERE vm_id = v_vm_guid
        AND (
            NOT v_only_plugged
            OR is_plugged
            )
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_disk_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = disk_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetDisksVmGuids (
  v_vm_guids UUID[]
)
  RETURNS SETOF all_disks_for_vms STABLE AS $PROCEDURE$
BEGIN
  RETURN QUERY
  SELECT *
  FROM all_disks_for_vms
  WHERE vm_id = ANY(v_vm_guids);
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS disks_basic_rs CASCADE;
CREATE TYPE disks_basic_rs AS (
        disk_id UUID,
        disk_alias VARCHAR(255),
        size BIGINT
        );

CREATE OR REPLACE FUNCTION GetDisksVmGuidBasicView (
    v_vm_guid UUID,
    v_only_plugged BOOLEAN,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF disks_basic_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT disk_id,
        disk_alias,
        size
    FROM images
    LEFT JOIN base_disks
        ON images.image_group_id = base_disks.disk_id
    LEFT JOIN vm_device
        ON vm_device.device_id = image_group_id
            AND (
                NOT v_only_plugged
                OR is_plugged
                )
    WHERE vm_device.vm_id = v_vm_guid
        AND images.active = true
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_disk_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = disk_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmBootActiveDisk (v_vm_guid UUID)
RETURNS SETOF all_disks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT all_disks.*
    FROM all_disks
    INNER JOIN vm_device
        ON vm_device.device_id = all_disks.image_group_id
    INNER JOIN disk_vm_element
        ON disk_vm_element.disk_id = all_disks.image_group_id
    WHERE vm_device.vm_id = v_vm_guid
        AND disk_vm_element.is_boot = TRUE
        AND vm_device.snapshot_id IS NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Returns all the attachable disks in the storage pool
-- If storage pool is ommited, all the attachable disks are retrurned.
-- in case vm id is provided, returning all the disks in SP that are not attached to the vm
CREATE OR REPLACE FUNCTION GetAllAttachableDisksByPoolId (
    v_storage_pool_id UUID,
    v_vm_id uuid,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF all_disks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT all_disks.*
    FROM all_disks
    WHERE (
            v_storage_pool_id IS NULL
            OR all_disks.storage_pool_id = v_storage_pool_id
            )
        AND (
            all_disks.number_of_vms = 0
            OR all_disks.shareable
            )
        -- ImageStatus.ILLEGAL=4 / imagestatus IS NULL -> LunDiski / ImageStatus.Locked=2
        AND (
            all_disks.imagestatus IS NULL
            OR (
                all_disks.imagestatus != 4
                AND all_disks.imagestatus != 2
                )
            )
        AND (
            v_vm_id IS NULL
            OR v_vm_id NOT IN (
                SELECT vm_id
                FROM vm_device
                WHERE vm_device.device_id = all_disks.image_group_id
                )
            )
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_disk_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = disk_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromDisksByDiskStorageType (
    v_disk_storage_type SMALLINT,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF all_disks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM all_disks
    WHERE disk_storage_type = v_disk_storage_type
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_disk_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = disk_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;


