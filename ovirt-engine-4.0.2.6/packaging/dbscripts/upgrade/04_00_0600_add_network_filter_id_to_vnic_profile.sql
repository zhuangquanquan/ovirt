--The upgrade script will add a reference of network filter id to vnic_profiles table.
--The script will need to initialize the new network_filter_id column.
--Please note that the vNIC's network filter is being determined as follows:
--If the vlaue of 'EnableMACAntiSpoofingFilterRules' in 'vdc_options' table is 'true',
--'vdsm-no-mac-spoofing' filter will be set on all the vNic profiles,
--otherwise NULL will be set.

SELECT fn_db_add_column ('vnic_profiles', 'network_filter_id', 'UUID');

SELECT fn_db_create_constraint('vnic_profiles', 'network_filter_id_fk', 'FOREIGN KEY(network_filter_id) REFERENCES network_filter(filter_id) ON DELETE SET NULL');

CREATE OR REPLACE FUNCTION __temp_set_default_filter()
RETURNS VOID AS $FUNCTION$
DECLARE
    v_default_filter_id UUID;
BEGIN
    IF (
        SELECT option_value
        FROM vdc_options
        WHERE option_name='EnableMACAntiSpoofingFilterRules')='true' THEN
            v_default_filter_id := (
                SELECT filter_id
                FROM network_filter
                WHERE filter_name='vdsm-no-mac-spoofing'
            );
            UPDATE vnic_profiles SET network_filter_id = v_default_filter_id;
    END IF;
END; $FUNCTION$
LANGUAGE plpgsql;

SELECT  __temp_set_default_filter();
DROP FUNCTION __temp_set_default_filter();

