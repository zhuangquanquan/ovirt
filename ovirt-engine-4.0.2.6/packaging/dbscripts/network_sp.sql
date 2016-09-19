


----------------------------------------------------------------
-- [network] Table
--
CREATE OR REPLACE FUNCTION Insertnetwork (
    v_addr VARCHAR(50),
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_id UUID,
    v_name VARCHAR(50),
    v_subnet VARCHAR(20),
    v_gateway VARCHAR(20),
    v_type INT,
    v_vlan_id INT,
    v_stp BOOLEAN,
    v_storage_pool_id UUID,
    v_mtu INT,
    v_vm_network BOOLEAN,
    v_provider_network_provider_id UUID,
    v_provider_network_external_id TEXT,
    v_qos_id UUID,
    v_label TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO network (
        addr,
        description,
        free_text_comment,
        id,
        name,
        subnet,
        gateway,
        type,
        vlan_id,
        stp,
        storage_pool_id,
        mtu,
        vm_network,
        provider_network_provider_id,
        provider_network_external_id,
        qos_id,
        label
        )
    VALUES (
        v_addr,
        v_description,
        v_free_text_comment,
        v_id,
        v_name,
        v_subnet,
        v_gateway,
        v_type,
        v_vlan_id,
        v_stp,
        v_storage_pool_id,
        v_mtu,
        v_vm_network,
        v_provider_network_provider_id,
        v_provider_network_external_id,
        v_qos_id,
        v_label
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatenetwork (
    v_addr VARCHAR(50),
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_id UUID,
    v_name VARCHAR(50),
    v_subnet VARCHAR(20),
    v_gateway VARCHAR(20),
    v_type INT,
    v_vlan_id INT,
    v_stp BOOLEAN,
    v_storage_pool_id UUID,
    v_mtu INT,
    v_vm_network BOOLEAN,
    v_provider_network_provider_id UUID,
    v_provider_network_external_id TEXT,
    v_qos_id UUID,
    v_label TEXT
    )
RETURNS VOID
    --The [network] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE network
    SET addr = v_addr,
        description = v_description,
        free_text_comment = v_free_text_comment,
        name = v_name,
        subnet = v_subnet,
        gateway = v_gateway,
        type = v_type,
        vlan_id = v_vlan_id,
        stp = v_stp,
        storage_pool_id = v_storage_pool_id,
        mtu = v_mtu,
        vm_network = v_vm_network,
        provider_network_provider_id = v_provider_network_provider_id,
        provider_network_external_id = v_provider_network_external_id,
        qos_id = v_qos_id,
        label = v_label
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletenetwork (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM network
    WHERE id = v_id
    FOR UPDATE;

    DELETE
    FROM network
    WHERE id = v_id;

    -- Delete the network's permissions
    DELETE
    FROM permissions
    WHERE object_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromnetwork (
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE NOT v_is_filtered
        OR EXISTS (
            SELECT 1
            FROM user_network_permissions_view
            WHERE user_id = v_user_id
                AND entity_id = network.id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetnetworkByid (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_network_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetnetworkByName (v_networkName VARCHAR(50))
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE name = v_networkName;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkByNameAndDataCenter (
    v_name VARCHAR(50),
    v_storage_pool_id UUID
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    WHERE network.name = v_name
        AND network.storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkByNameAndCluster (
    v_name VARCHAR(50),
    v_cluster_id UUID
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    WHERE network.name = v_name
        AND EXISTS (
            SELECT 1
            FROM network_cluster
            WHERE network.id = network_cluster.network_id
                AND network_cluster.cluster_id = v_cluster_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetManagementNetworkByCluster (v_cluster_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    WHERE id = (
            SELECT network_id
            FROM network_cluster
            WHERE network_cluster.cluster_id = v_cluster_id
                AND network_cluster.management
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworkByStoragePoolId (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE storage_pool_id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_network_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = network.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS networkViewClusterType CASCADE;
CREATE TYPE networkViewClusterType AS (
        id uuid,
        name VARCHAR(50),
        description VARCHAR(4000),
        free_text_comment TEXT,
        type INT,
        addr VARCHAR(50),
        subnet VARCHAR(20),
        gateway VARCHAR(20),
        vlan_id INT,
        stp BOOLEAN,
        storage_pool_id UUID,
        mtu INT,
        vm_network BOOLEAN,
        label TEXT,
        provider_network_provider_id UUID,
        provider_network_external_id TEXT,
        qos_id UUID,
        network_id UUID,
        cluster_id UUID,
        status INT,
        is_display BOOLEAN,
        required BOOLEAN,
        migration BOOLEAN,
        management BOOLEAN,
        is_gluster BOOLEAN
        );

CREATE OR REPLACE FUNCTION GetAllNetworkByClusterId (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF networkViewClusterType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT network.id,
        network.name,
        network.description,
        network.free_text_comment,
        network.type,
        network.addr,
        network.subnet,
        network.gateway,
        network.vlan_id,
        network.stp,
        network.storage_pool_id,
        network.mtu,
        network.vm_network,
        network.label,
        network.provider_network_provider_id,
        network.provider_network_external_id,
        network.qos_id,
        network_cluster.network_id,
        network_cluster.cluster_id,
        network_cluster.status,
        network_cluster.is_display,
        network_cluster.required,
        network_cluster.migration,
        network_cluster.management,
        network_cluster.is_gluster
    FROM network
    INNER JOIN network_cluster
        ON network.id = network_cluster.network_id
    WHERE network_cluster.cluster_id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_network_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = network.id
                )
            )
    ORDER BY network.name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworksByQosId (v_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE qos_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworksByNetworkProviderId (v_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network
    WHERE provider_network_provider_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworkViewsByNetworkProviderId (v_id UUID)
RETURNS SETOF network_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_view
    WHERE provider_network_provider_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworkLabelsByDataCenterId (v_id UUID)
RETURNS SETOF TEXT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT label
    FROM network
    WHERE network.storage_pool_id = v_id
        AND label IS NOT NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;

--The GetByFK stored procedure cannot be created because the [network] table doesn't have at least one foreign key column or the foreign keys are also primary keys.
----------------------------------------------------------------
-- [vds_interface] Table
--
CREATE OR REPLACE FUNCTION Insertvds_interface (
    v_addr VARCHAR(20),
    v_bond_name VARCHAR(50),
    v_bond_type INT,
    v_gateway VARCHAR(20),
    v_id UUID,
    v_is_bond BOOLEAN,
    v_reported_switch_type VARCHAR(6),
    v_bond_opts VARCHAR(4000),
    v_mac_addr VARCHAR(20),
    v_name VARCHAR(50),
    v_network_name VARCHAR(50),
    v_speed INT,
    v_subnet VARCHAR(20),
    v_boot_protocol INT,
    v_type INT,
    v_vds_id UUID,
    v_base_interface VARCHAR(50),
    v_vlan_id INT,
    v_mtu INT,
    v_bridged BOOLEAN,
    v_labels TEXT,
    v_ipv6_boot_protocol INT,
    v_ipv6_address VARCHAR(50),
    v_ipv6_prefix INT,
    v_ipv6_gateway VARCHAR(50),
    v_ad_partner_mac VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vds_interface (
        addr,
        bond_name,
        bond_type,
        gateway,
        id,
        is_bond,
        reported_switch_type,
        bond_opts,
        mac_addr,
        name,
        network_name,
        speed,
        subnet,
        boot_protocol,
        type,
        VDS_ID,
        base_interface,
        vlan_id,
        mtu,
        bridged,
        labels,
        ipv6_address,
        ipv6_gateway,
        ipv6_prefix,
        ipv6_boot_protocol,
        ad_partner_mac
        )
    VALUES (
        v_addr,
        v_bond_name,
        v_bond_type,
        v_gateway,
        v_id,
        v_is_bond,
        v_reported_switch_type,
        v_bond_opts,
        v_mac_addr,
        v_name,
        v_network_name,
        v_speed,
        v_subnet,
        v_boot_protocol,
        v_type,
        v_vds_id,
        v_base_interface,
        v_vlan_id,
        v_mtu,
        v_bridged,
        v_labels,
        v_ipv6_address,
        v_ipv6_gateway,
        v_ipv6_prefix,
        v_ipv6_boot_protocol,
        v_ad_partner_mac
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatevds_interface (
    v_addr VARCHAR(20),
    v_bond_name VARCHAR(50),
    v_bond_type INT,
    v_gateway VARCHAR(20),
    v_id UUID,
    v_is_bond BOOLEAN,
    v_reported_switch_type VARCHAR(6),
    v_bond_opts VARCHAR(4000),
    v_mac_addr VARCHAR(20),
    v_name VARCHAR(50),
    v_network_name VARCHAR(50),
    v_speed INT,
    v_subnet VARCHAR(20),
    v_boot_protocol INT,
    v_type INT,
    v_vds_id UUID,
    v_base_interface VARCHAR(50),
    v_vlan_id INT,
    v_mtu INT,
    v_bridged BOOLEAN,
    v_labels TEXT,
    v_ipv6_address VARCHAR(50),
    v_ipv6_gateway VARCHAR(50),
    v_ipv6_prefix INT,
    v_ipv6_boot_protocol INT,
    v_ad_partner_mac VARCHAR(50)
    )
RETURNS VOID
    --The [vds_interface] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vds_interface
    SET addr = v_addr,
        bond_name = v_bond_name,
        bond_type = v_bond_type,
        gateway = v_gateway,
        is_bond = v_is_bond,
        reported_switch_type = v_reported_switch_type,
        bond_opts = v_bond_opts,
        mac_addr = v_mac_addr,
        name = v_name,
        network_name = v_network_name,
        speed = v_speed,
        subnet = v_subnet,
        boot_protocol = v_boot_protocol,
        type = v_type,
        VDS_ID = v_vds_id,
        base_interface = v_base_interface,
        vlan_id = v_vlan_id,
        _update_date = LOCALTIMESTAMP,
        mtu = v_mtu,
        bridged = v_bridged,
        labels = v_labels,
        ipv6_address = v_ipv6_address,
        ipv6_gateway = v_ipv6_gateway,
        ipv6_prefix = v_ipv6_prefix,
        ipv6_boot_protocol = v_ipv6_boot_protocol,
        ad_partner_mac = v_ad_partner_mac
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletevds_interface (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vds_interface
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Clear_network_from_nics (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vds_interface
    SET network_name = NULL
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getinterface_viewByvds_id (
    v_vds_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds_interface_view
    WHERE vds_id = v_vds_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vds_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vds_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS host_networks_by_cluster_rs CASCADE;
CREATE TYPE host_networks_by_cluster_rs AS (
        vds_id UUID,
        network_name VARCHAR
        );

CREATE OR REPLACE FUNCTION GetHostNetworksByCluster (v_cluster_id UUID)
RETURNS SETOF host_networks_by_cluster_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_static.vds_id,
        vds_interface.network_name
    FROM vds_static
    INNER JOIN vds_interface
        ON vds_interface.vds_id = vds_static.vds_id
            AND vds_static.cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getinterface_viewByAddr (
    v_cluster_id UUID,
    v_addr VARCHAR(50)
    )
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_interface_view.*
    FROM vds_interface_view
    INNER JOIN vds_static
        ON vds_interface_view.vds_id = vds_static.vds_id
    WHERE vds_interface_view.addr = v_addr
        AND vds_static.cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsManagedInterfaceByVdsId (
    v_vds_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds_interface_view
    -- Checking if the 2nd bit in the type column is set, meaning that the interface is managed
    WHERE vds_id = v_vds_id
        AND (type & 2) = 2
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vds_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vds_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsInterfacesByNetworkId (v_network_id UUID)
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_interface_view.*
    FROM vds_interface_view
    INNER JOIN vds
        ON vds.vds_id = vds_interface_view.vds_id
    INNER JOIN network_cluster
        ON network_cluster.cluster_id = vds.cluster_id
    INNER JOIN network
        ON network.id = network_cluster.network_id
            AND network.name = vds_interface_view.network_name
    WHERE network.id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsInterfaceById (v_vds_interface_id UUID)
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds_interface_view
    WHERE id = v_vds_interface_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsInterfaceByName (
    v_host_id UUID,
    v_name VARCHAR(50)
    )
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vds_interface_view
    WHERE name = v_name
        AND vds_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetInterfacesByClusterId (v_cluster_id UUID)
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_interface_view.*
    FROM vds_interface_view
    INNER JOIN vds_static
        ON vds_interface_view.vds_id = vds_static.vds_id
    WHERE vds_static.cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetInterfacesByDataCenterId (v_data_center_id UUID)
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_interface_view.*
    FROM vds_interface_view
    INNER JOIN vds_static
        ON vds_interface_view.vds_id = vds_static.vds_id
    INNER JOIN cluster
        ON vds_static.cluster_id = cluster.cluster_id
    WHERE cluster.storage_pool_id = v_data_center_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_interface] Table
----------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertVmInterface (
    v_id UUID,
    v_mac_addr VARCHAR(20),
    v_name VARCHAR(50),
    v_speed INT,
    v_vnic_profile_id UUID,
    v_vm_guid UUID,
    v_vmt_guid UUID,
    v_type INT,
    v_linked BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_interface (
        id,
        mac_addr,
        name,
        speed,
        vnic_profile_id,
        vm_guid,
        vmt_guid,
        type,
        linked
        )
    VALUES (
        v_id,
        v_mac_addr,
        v_name,
        v_speed,
        v_vnic_profile_id,
        v_vm_guid,
        v_vmt_guid,
        v_type,
        v_linked
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVmInterface (
    v_id UUID,
    v_mac_addr VARCHAR(20),
    v_name VARCHAR(50),
    v_speed INT,
    v_vnic_profile_id UUID,
    v_vm_guid UUID,
    v_vmt_guid UUID,
    v_type INT,
    v_linked BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_interface
    SET mac_addr = v_mac_addr,
        name = v_name,
        speed = v_speed,
        vnic_profile_id = v_vnic_profile_id,
        vm_guid = v_vm_guid,
        vmt_guid = v_vmt_guid,
        type = v_type,
        _update_date = LOCALTIMESTAMP,
        linked = v_linked
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmInterface (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM vm_interface
    WHERE id = v_id
    FOR

    UPDATE;

    DELETE
    FROM vm_interface
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfaceByVmInterfaceId (v_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmInterfaces ()
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfacesByVmId (v_vm_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface
    WHERE vm_guid = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfaceByTemplateId (v_template_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface
    WHERE vmt_guid = v_template_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfacesByNetworkId (v_network_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface.*
    FROM vm_interface
    INNER JOIN vnic_profiles
        ON vm_interface.vnic_profile_id = vnic_profiles.id
    INNER JOIN vm_static
        ON vm_interface.vm_guid = vm_static.vm_guid
    WHERE vnic_profiles.network_id = v_network_id
        AND vm_static.entity_type = 'VM';
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmTemplateInterfacesByNetworkId (v_network_id UUID)
RETURNS SETOF vm_interface STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface.*
    FROM vm_interface
    INNER JOIN vm_static
        ON vm_interface.vmt_guid = vm_static.vm_guid
    INNER JOIN vnic_profiles
        ON vm_interface.vnic_profile_id = vnic_profiles.id
    WHERE vnic_profiles.network_id = v_network_id
        AND vm_static.entity_type = 'TEMPLATE';
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetMacsByDataCenterId (v_data_center_id UUID)
RETURNS SETOF VARCHAR STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT mac_addr
    FROM vm_interface
    WHERE EXISTS (
            SELECT 1
            FROM vm_static
            INNER JOIN cluster
                ON vm_static.cluster_id = cluster.cluster_id
            WHERE cluster.storage_pool_id = v_data_center_id
                AND vm_static.vm_guid = vm_interface.vm_guid
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- VM Interface View
----------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetAllFromVmNetworkInterfaceViews ()
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmNetworkInterfaceViewByVmNetworkInterfaceViewId (v_id UUID)
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetPluggedVmInterfacesByMac (v_mac_address VARCHAR(20))
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view
    WHERE mac_addr = v_mac_address
        AND is_plugged = true;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmNetworkInterfaceViewByVmId (
    v_vm_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view
    WHERE vm_guid = v_vm_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmNetworkInterfaceToMonitorByVmId (v_vm_id UUID)
RETURNS SETOF vm_interface_monitoring_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_monitoring_view
    WHERE vm_guid = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmNetworkInterfaceViewByTemplateId (
    v_template_id UUID,
    v_user_id UUID,
    v_is_filtered boolean
    )
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_view
    WHERE vmt_guid = v_template_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_template_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_template_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmInterfaceViewsByNetworkId (v_network_id UUID)
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface_view.*
    FROM vm_interface_view
    INNER JOIN vnic_profiles
        ON vnic_profiles.id = vm_interface_view.vnic_profile_id
    WHERE vnic_profiles.network_id = v_network_id
        AND vm_interface_view.vm_entity_type = 'VM';
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmTemplateInterfaceViewsByNetworkId (v_network_id UUID)
RETURNS SETOF vm_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_interface_view.*
    FROM vm_interface_view
    INNER JOIN vnic_profiles
        ON vnic_profiles.id = vm_interface_view.vnic_profile_id
    WHERE vnic_profiles.network_id = v_network_id
        AND vm_interface_view.vm_entity_type = 'TEMPLATE';
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vm_interface_statistics] Table
--
CREATE OR REPLACE FUNCTION Getvm_interface_statisticsById (v_id UUID)
RETURNS SETOF vm_interface_statistics STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_interface_statistics
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Insertvm_interface_statistics (
    v_id UUID,
    v_rx_drop DECIMAL(18, 0),
    v_rx_rate DECIMAL(18, 0),
    v_rx_total BIGINT,
    v_rx_offset BIGINT,
    v_tx_drop DECIMAL(18, 0),
    v_tx_rate DECIMAL(18, 0),
    v_tx_total BIGINT,
    v_tx_offset BIGINT,
    v_iface_status INT,
    v_sample_time FLOAT,
    v_vm_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_interface_statistics (
        id,
        rx_drop,
        rx_rate,
        rx_total,
        rx_offset,
        tx_drop,
        tx_rate,
        tx_total,
        tx_offset,
        vm_id,
        iface_status,
        sample_time
        )
    VALUES (
        v_id,
        v_rx_drop,
        v_rx_rate,
        v_rx_total,
        v_rx_offset,
        v_tx_drop,
        v_tx_rate,
        v_tx_total,
        v_tx_offset,
        v_vm_id,
        v_iface_status,
        v_sample_time
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatevm_interface_statistics (
    v_id UUID,
    v_rx_drop DECIMAL(18, 0),
    v_rx_rate DECIMAL(18, 0),
    v_rx_total BIGINT,
    v_rx_offset BIGINT,
    v_tx_drop DECIMAL(18, 0),
    v_tx_rate DECIMAL(18, 0),
    v_tx_total BIGINT,
    v_tx_offset BIGINT,
    v_iface_status INT,
    v_sample_time FLOAT,
    v_vm_id UUID
    )
RETURNS VOID
    --The [vm_interface_statistics] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vm_interface_statistics
    SET rx_drop = v_rx_drop,
        rx_rate = v_rx_rate,
        rx_total = v_rx_total,
        rx_offset = v_rx_offset,
        tx_drop = v_tx_drop,
        tx_rate = v_tx_rate,
        tx_total = v_tx_total,
        tx_offset = v_tx_offset,
        vm_id = v_vm_id,
        iface_status = v_iface_status,
        sample_time = v_sample_time,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletevm_interface_statistics (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM vm_interface_statistics
    WHERE id = v_id
    FOR

    UPDATE;

    DELETE
    FROM vm_interface_statistics
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [network_cluster] Table
--
CREATE OR REPLACE FUNCTION GetVmGuestAgentInterfacesByVmId (
    v_vm_id UUID,
    v_user_id UUID,
    v_filtered BOOLEAN
    )
RETURNS SETOF vm_guest_agent_interfaces STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vm_guest_agent_interfaces
    WHERE vm_id = v_vm_id
        AND (
            NOT v_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVmGuestAgentInterfacesByVmId (v_vm_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vm_guest_agent_interfaces
    WHERE vm_id = v_vm_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVmGuestAgentInterface (
    v_vm_id UUID,
    v_interface_name VARCHAR(50),
    v_mac_address VARCHAR(59),
    v_ipv4_addresses TEXT,
    v_ipv6_addresses TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_guest_agent_interfaces (
        vm_id,
        interface_name,
        mac_address,
        ipv4_addresses,
        ipv6_addresses
        )
    VALUES (
        v_vm_id,
        v_interface_name,
        v_mac_address,
        v_ipv4_addresses,
        v_ipv6_addresses
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [vds_interface_statistics] Table
--
CREATE OR REPLACE FUNCTION Insertvds_interface_statistics (
    v_id UUID,
    v_rx_drop DECIMAL(18, 0),
    v_rx_rate DECIMAL(18, 0),
    v_rx_total BIGINT,
    v_rx_offset BIGINT,
    v_tx_drop DECIMAL(18, 0),
    v_tx_rate DECIMAL(18, 0),
    v_tx_total BIGINT,
    v_tx_offset BIGINT,
    v_iface_status INT,
    v_sample_time FLOAT,
    v_vds_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vds_interface_statistics (
        id,
        rx_drop,
        rx_rate,
        rx_total,
        rx_offset,
        tx_drop,
        tx_rate,
        tx_total,
        tx_offset,
        vds_id,
        iface_status,
        sample_time
        )
    VALUES (
        v_id,
        v_rx_drop,
        v_rx_rate,
        v_rx_total,
        v_rx_offset,
        v_tx_drop,
        v_tx_rate,
        v_tx_total,
        v_tx_offset,
        v_vds_id,
        v_iface_status,
        v_sample_time
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatevds_interface_statistics (
    v_id UUID,
    v_rx_drop DECIMAL(18, 0),
    v_rx_rate DECIMAL(18, 0),
    v_rx_total BIGINT,
    v_rx_offset BIGINT,
    v_tx_drop DECIMAL(18, 0),
    v_tx_rate DECIMAL(18, 0),
    v_tx_total BIGINT,
    v_tx_offset BIGINT,
    v_iface_status INT,
    v_sample_time FLOAT,
    v_vds_id UUID
    )
RETURNS VOID
    --The [vds_interface_statistics] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vds_interface_statistics
    SET rx_drop = v_rx_drop,
        rx_rate = v_rx_rate,
        rx_total = v_rx_total,
        rx_offset = v_rx_offset,
        tx_drop = v_tx_drop,
        tx_rate = v_tx_rate,
        tx_total = v_tx_total,
        tx_offset = v_tx_offset,
        vds_id = v_vds_id,
        iface_status = v_iface_status,
        sample_time = v_sample_time,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletevds_interface_statistics (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT id
    INTO v_val
    FROM vds_interface_statistics
    WHERE id = v_id
    FOR

    UPDATE;

    DELETE
    FROM vds_interface_statistics
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [network_cluster] Table
--
CREATE OR REPLACE FUNCTION Insertnetwork_cluster (
    v_cluster_id UUID,
    v_network_id UUID,
    v_status INT,
    v_is_display BOOLEAN,
    v_required BOOLEAN,
    v_migration BOOLEAN,
    v_management BOOLEAN,
    v_is_gluster BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO network_cluster (
        cluster_id,
        network_id,
        status,
        is_display,
        required,
        migration,
        management,
        is_gluster
        )
    VALUES (
        v_cluster_id,
        v_network_id,
        v_status,
        v_is_display,
        v_required,
        v_migration,
        v_management,
        v_is_gluster
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatenetwork_cluster (
    v_cluster_id UUID,
    v_network_id UUID,
    v_status INT,
    v_is_display BOOLEAN,
    v_required BOOLEAN,
    v_migration BOOLEAN,
    v_management BOOLEAN,
    v_is_gluster BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET status = v_status,
        is_display = v_is_display,
        required = v_required,
        migration = v_migration,
        management = v_management,
        is_gluster = v_is_gluster
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatenetwork_cluster_status (
    v_cluster_id UUID,
    v_network_id UUID,
    v_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET status = v_status
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletenetwork_cluster (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM network_cluster
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromnetwork_cluster ()
RETURNS SETOF network_cluster STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_cluster;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromnetwork_clusterByClusterId (v_cluster_id UUID)
RETURNS SETOF network_cluster STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_cluster
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllManagementNetworksByDataCenterId (v_data_center_id UUID)
RETURNS SETOF network STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network.*
    FROM network
    INNER JOIN network_cluster
        ON network.id = network_cluster.network_id
    INNER JOIN cluster
        ON network_cluster.cluster_id = cluster.cluster_id
    WHERE cluster.storage_pool_id = v_data_center_id
        AND network_cluster.management;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromnetwork_clusterByNetworkId (v_network_id UUID)
RETURNS SETOF network_cluster STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_cluster
    WHERE network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getnetwork_clusterBycluster_idAndBynetwork_id (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS SETOF network_cluster STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_cluster
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetvmStaticByGroupIdAndNetwork (
    v_groupId UUID,
    v_networkName VARCHAR(50)
    )
RETURNS SETOF vm_static STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_static.*
    FROM vm_static
    INNER JOIN vm_interface_view
        ON vm_static.vm_guid = vm_interface_view.vm_guid
            AND network_name = v_networkName
            AND vm_static.cluster_id = v_groupId;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_network_exclusively_as_display (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET is_display = true
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;

    IF FOUND THEN
        UPDATE network_cluster
        SET is_display = false
        WHERE cluster_id = v_cluster_id
            AND network_id != v_network_id;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_network_exclusively_as_migration (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET migration = true
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;

    IF FOUND THEN
        UPDATE network_cluster
        SET migration = false
        WHERE cluster_id = v_cluster_id
            AND network_id != v_network_id;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_network_exclusively_as_gluster (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET is_gluster = COALESCE(network_id = v_network_id, false)
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_network_exclusively_as_management (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_cluster
    SET management = true
    WHERE cluster_id = v_cluster_id
        AND network_id = v_network_id;

    IF FOUND THEN
        UPDATE network_cluster
        SET management = false
        WHERE cluster_id = v_cluster_id
            AND network_id != v_network_id;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  Vnic Profile
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetVnicProfileByVnicProfileId (v_id UUID)
RETURNS SETOF vnic_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertVnicProfile (
    v_id UUID,
    v_name VARCHAR(50),
    v_network_id UUID,
    v_network_qos_id UUID,
    v_port_mirroring BOOLEAN,
    v_passthrough BOOLEAN,
    v_custom_properties TEXT,
    v_description TEXT,
    v_network_filter_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vnic_profiles (
        id,
        name,
        network_id,
        network_qos_id,
        port_mirroring,
        passthrough,
        custom_properties,
        description,
        network_filter_id
        )
    VALUES (
        v_id,
        v_name,
        v_network_id,
        v_network_qos_id,
        v_port_mirroring,
        v_passthrough,
        v_custom_properties,
        v_description,
        v_network_filter_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVnicProfile (
    v_id UUID,
    v_name VARCHAR(50),
    v_network_id UUID,
    v_network_qos_id UUID,
    v_port_mirroring BOOLEAN,
    v_passthrough BOOLEAN,
    v_custom_properties TEXT,
    v_description TEXT,
    v_network_filter_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vnic_profiles
    SET id = v_id,
        name = v_name,
        network_id = v_network_id,
        network_qos_id = v_network_qos_id,
        port_mirroring = v_port_mirroring,
        passthrough = v_passthrough,
        custom_properties = v_custom_properties,
        description = v_description,
        _update_date = LOCALTIMESTAMP,
        network_filter_id = v_network_filter_id
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVnicProfile (v_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    DELETE
    FROM vnic_profiles
    WHERE id = v_id;

    -- Delete the vnic profiles permissions
    DELETE
    FROM permissions
    WHERE object_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVnicProfiles ()
RETURNS SETOF vnic_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfilesByNetworkId (v_network_id UUID)
RETURNS SETOF vnic_profiles STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles
    WHERE network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  Vnic Profile View
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetVnicProfileViewByVnicProfileViewId (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vnic_profile_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vnic_profiles_view.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVnicProfileViews (
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE NOT v_is_filtered
        OR EXISTS (
            SELECT 1
            FROM user_vnic_profile_permissions_view
            WHERE user_id = v_user_id
                AND entity_id = vnic_profiles_view.id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfileViewsByNetworkId (
    v_network_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE network_id = v_network_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vnic_profile_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vnic_profiles_view.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfileViewsByDataCenterId (
    v_id UUID,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE data_center_id = v_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vnic_profile_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vnic_profiles_view.id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVnicProfileViewsByNetworkQosId (v_network_qos_id UUID)
RETURNS SETOF vnic_profiles_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM vnic_profiles_view
    WHERE network_qos_id = v_network_qos_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetIscsiIfacesByHostIdAndStorageTargetId (
    v_host_id UUID,
    v_target_id VARCHAR(50)
    )
RETURNS SETOF vds_interface_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_interface_view.*
    FROM vds_interface_view,
        network_cluster,
        network,
        iscsi_bonds_networks_map,
        iscsi_bonds_storage_connections_map
    WHERE iscsi_bonds_storage_connections_map.connection_id = v_target_id
        AND iscsi_bonds_storage_connections_map.iscsi_bond_id = iscsi_bonds_networks_map.iscsi_bond_id
        AND iscsi_bonds_networks_map.network_id = network.id
        AND network.id = network_cluster.network_id
        AND network.name = vds_interface_view.network_name
        AND network_cluster.cluster_id = vds_interface_view.cluster_id
        AND vds_interface_view.vds_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getActiveMigrationNetworkInterfaceForHost (v_host_id UUID)
RETURNS SETOF active_migration_network_interfaces STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM active_migration_network_interfaces
    WHERE vds_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  hostNicVfsConfig
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertHostNicVfsConfig (
    v_id UUID,
    v_nic_id UUID,
    v_is_all_networks_allowed BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO host_nic_vfs_config (
        id,
        nic_id,
        is_all_networks_allowed
        )
    VALUES (
        v_id,
        v_nic_id,
        v_is_all_networks_allowed
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateHostNicVfsConfig (
    v_id UUID,
    v_nic_id UUID,
    v_is_all_networks_allowed BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE host_nic_vfs_config
    SET id = v_id,
        nic_id = v_nic_id,
        is_all_networks_allowed = v_is_all_networks_allowed,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteHostNicVfsConfig (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM host_nic_vfs_config
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostNicVfsConfigById (v_id UUID)
RETURNS SETOF host_nic_vfs_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_nic_vfs_config
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVfsConfigByNicId (v_nic_id UUID)
RETURNS SETOF host_nic_vfs_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_nic_vfs_config
    WHERE nic_id = v_nic_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromHostNicVfsConfigs ()
RETURNS SETOF host_nic_vfs_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_nic_vfs_config;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllVfsConfigByHostId (v_host_id UUID)
RETURNS SETOF host_nic_vfs_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT host_nic_vfs_config.*
    FROM host_nic_vfs_config
    INNER JOIN vds_interface
        ON host_nic_vfs_config.nic_id = vds_interface.id
    WHERE vds_interface.vds_id = v_host_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-------------------------------------------------------------------------------------------
-- Network attachments
-------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetNetworkAttachmentByNetworkAttachmentId (v_id UUID)
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_attachments
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertNetworkAttachment (
    v_id UUID,
    v_network_id UUID,
    v_nic_id UUID,
    v_boot_protocol VARCHAR(20),
    v_address VARCHAR(20),
    v_netmask VARCHAR(20),
    v_gateway VARCHAR(20),
    v_ipv6_boot_protocol VARCHAR(20),
    v_ipv6_address VARCHAR(50),
    v_ipv6_prefix INT,
    v_ipv6_gateway VARCHAR(50),
    v_custom_properties TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO network_attachments (
        id,
        network_id,
        nic_id,
        boot_protocol,
        address,
        netmask,
        gateway,
        ipv6_boot_protocol,
        ipv6_address,
        ipv6_prefix,
        ipv6_gateway,
        custom_properties
        )
    VALUES (
        v_id,
        v_network_id,
        v_nic_id,
        v_boot_protocol,
        v_address,
        v_netmask,
        v_gateway,
        v_ipv6_boot_protocol,
        v_ipv6_address,
        v_ipv6_prefix,
        v_ipv6_gateway,
        v_custom_properties
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateNetworkAttachment (
    v_id UUID,
    v_network_id UUID,
    v_nic_id UUID,
    v_boot_protocol VARCHAR(20),
    v_address VARCHAR(20),
    v_netmask VARCHAR(20),
    v_gateway VARCHAR(20),
    v_ipv6_boot_protocol VARCHAR(20),
    v_ipv6_address VARCHAR(50),
    v_ipv6_prefix INT,
    v_ipv6_gateway VARCHAR(50),
    v_custom_properties TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE network_attachments
    SET network_id = v_network_id,
        nic_id = v_nic_id,
        boot_protocol = v_boot_protocol,
        address = v_address,
        netmask = v_netmask,
        gateway = v_gateway,
        custom_properties = v_custom_properties,
        ipv6_boot_protocol = v_ipv6_boot_protocol,
        ipv6_address = v_ipv6_address,
        ipv6_prefix = v_ipv6_prefix,
        ipv6_gateway = v_ipv6_gateway,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteNetworkAttachment (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM network_attachments
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromNetworkAttachments ()
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_attachments;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkAttachmentsByNicId (v_nic_id UUID)
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_attachments
    WHERE nic_id = v_nic_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkAttachmentsByNetworkId (v_network_id UUID)
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_attachments
    WHERE network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkAttachmentsByHostId (v_host_id UUID)
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_attachments
    WHERE EXISTS (
            SELECT 1
            FROM vds_interface
            WHERE network_attachments.nic_id = vds_interface.id
                AND vds_interface.vds_id = v_host_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkAttachmentByNicIdAndNetworkId (
    v_nic_id UUID,
    v_network_id UUID
    )
RETURNS SETOF network_attachments STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT na.*
    FROM network_attachments na
    WHERE na.network_id = v_network_id
        AND na.nic_id = v_nic_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION RemoveNetworkAttachmentByNetworkId (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM network_attachments na
    WHERE na.network_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  vfsConfigNetworks
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertVfsConfigNetwork (
    v_vfs_config_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vfs_config_networks (
        vfs_config_id,
        network_id
        )
    VALUES (
        v_vfs_config_id,
        v_network_id
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVfsConfigNetwork (
    v_vfs_config_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vfs_config_networks
    WHERE vfs_config_id = v_vfs_config_id
        AND network_id = v_network_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllVfsConfigNetworks (v_vfs_config_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vfs_config_networks
    WHERE vfs_config_id = v_vfs_config_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworksByVfsConfigId (v_vfs_config_id UUID)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT network_id
    FROM vfs_config_networks
    WHERE vfs_config_id = v_vfs_config_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------------
--  vfsConfigLabels
----------------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertVfsConfigLabel (
    v_vfs_config_id UUID,
    v_label TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vfs_config_labels (
        vfs_config_id,
        label
        )
    VALUES (
        v_vfs_config_id,
        v_label
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVfsConfigLabel (
    v_vfs_config_id UUID,
    v_label TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vfs_config_labels
    WHERE vfs_config_id = v_vfs_config_id
        AND label = v_label;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllVfsConfigLabels (v_vfs_config_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM vfs_config_labels
    WHERE vfs_config_id = v_vfs_config_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetLabelsByVfsConfigId (v_vfs_config_id UUID)
RETURNS SETOF TEXT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT label
    FROM vfs_config_labels
    WHERE vfs_config_id = v_vfs_config_id;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllNetworkFilters ()
RETURNS SETOF network_filter STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_filter;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllSupportedNetworkFiltersByVersion (v_version VARCHAR(40))
RETURNS SETOF network_filter STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_filter
    WHERE v_version >= version;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkFilterById (v_filter_id UUID)
RETURNS SETOF network_filter STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_filter
    WHERE filter_id = v_filter_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNetworkFilterByName (v_filter_name VARCHAR(50))
RETURNS SETOF network_filter STABLE
AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM network_filter
    WHERE filter_name like v_filter_name;
END;$PROCEDURE$
LANGUAGE plpgsql;
