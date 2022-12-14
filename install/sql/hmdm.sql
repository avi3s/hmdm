--
-- PostgreSQL database dump
--

-- Dumped from database version 15.1
-- Dumped by pg_dump version 15.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: hmdm
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO hmdm;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: hmdm
--

COMMENT ON SCHEMA public IS '';


--
-- Name: mdm_app_version_comparison_index(text); Type: FUNCTION; Schema: public; Owner: hmdm
--

CREATE FUNCTION public.mdm_app_version_comparison_index(version_text text) RETURNS text
    LANGUAGE plpgsql
    AS $$

DECLARE
    parts          TEXT[];
    DECLARE i      INT;
    DECLARE result TEXT;
    DECLARE part   TEXT;
BEGIN
    IF version_text IS NULL THEN
        return -1000000;
    END IF;

    IF LENGTH(TRIM(version_text)) = 0 THEN
        return -1000000;
    END IF;

    result = '';

    parts = STRING_TO_ARRAY(version_text, '.');

    FOR i IN 1 .. ARRAY_UPPER(parts, 1)
        LOOP
            part = REGEXP_REPLACE(parts[i], '[^0-9]+', '', 'g');

            IF LENGTH(TRIM(part)) = 0 THEN
                part = '0';
            END IF;

            result = result || LPAD(part, 10, '0');
        END LOOP;

    RETURN result;
END;
$$;


ALTER FUNCTION public.mdm_app_version_comparison_index(version_text text) OWNER TO hmdm;

--
-- Name: mdm_config_app_upgrade(bigint, bigint); Type: FUNCTION; Schema: public; Owner: hmdm
--

CREATE FUNCTION public.mdm_config_app_upgrade(configid bigint, appid bigint) RETURNS integer
    LANGUAGE plpgsql
    AS $$

BEGIN
    -- Deleting existing record for latest version of app for configuration
    DELETE
    FROM configurationApplications
    WHERE configurationId = configId
      AND applicationId = appId
      AND applicationVersionId = (SELECT latestVersion FROM applications WHERE applications.id = appId);

    -- Change the current record for installed version of application to refer to latest version
    UPDATE configurationApplications
    SET applicationVersionId = (SELECT latestVersion FROM applications WHERE applications.id = appId)
    WHERE configurationId = configId
      AND applicationId = appId
      AND action = 1;


    -- Upgrade reference to main application if necessary
    UPDATE configurations
    SET mainAppId = (SELECT latestVersion FROM applications WHERE applications.id = appId)
    WHERE configurations.id = configId
      AND NOT configurations.mainAppId IS NULL
      AND EXISTS(SELECT 1
                 FROM applicationVersions
                 WHERE applicationVersions.id = configurations.mainAppId
                   AND applicationVersions.applicationId = appId);

    -- Upgrade reference to content application if necessary
    UPDATE configurations
    SET contentAppId = (SELECT latestVersion FROM applications WHERE applications.id = appId)
    WHERE configurations.id = configId
      AND NOT configurations.contentAppId IS NULL
      AND EXISTS(SELECT 1
                 FROM applicationVersions
                 WHERE applicationVersions.id = configurations.contentAppId
                   AND applicationVersions.applicationId = appId);

    RETURN 0;
END;
$$;


ALTER FUNCTION public.mdm_config_app_upgrade(configid bigint, appid bigint) OWNER TO hmdm;

--
-- Name: mdm_device_launcher_version(text, text); Type: FUNCTION; Schema: public; Owner: hmdm
--

CREATE FUNCTION public.mdm_device_launcher_version(launcherapppkg text, device_info text) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
    deviceApps    json;
    DECLARE i     INT;
    DECLARE count INT;
    DECLARE app   json;
BEGIN
    IF launcherAppPkg IS NULL THEN
        RETURN NULL;
    END IF;

    IF device_info IS NULL THEN
        RETURN NULL;
    END IF;

    deviceApps = device_info::json -> 'applications';
    count = json_array_length(deviceApps);

    FOR i IN 1 .. count
        LOOP
            app = deviceApps ->> (i - 1);
            IF (app ->> 'pkg') = launcherAppPkg THEN
                RETURN app ->> 'version';
            END IF;
        END LOOP;

    RETURN NULL;
END
$$;


ALTER FUNCTION public.mdm_device_launcher_version(launcherapppkg text, device_info text) OWNER TO hmdm;

--
-- Name: mdm_device_permissions_index(text); Type: FUNCTION; Schema: public; Owner: hmdm
--

CREATE FUNCTION public.mdm_device_permissions_index(device_info text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    permissions    json;
    DECLARE i      INT;
    DECLARE count  INT;
    DECLARE result INT;
BEGIN
    IF device_info IS NULL THEN
        return -1;
    END IF;

    IF LENGTH(TRIM(device_info)) = 0 THEN
        return -1;
    END IF;

    permissions = device_info::json -> 'permissions';

    count = json_array_length(permissions::json);

    result = 0;

    FOR i IN 1 .. count
        LOOP
            result = result + (permissions::json ->> (i - 1))::int;
        END LOOP;


    RETURN result;
END ;
$$;


ALTER FUNCTION public.mdm_device_permissions_index(device_info text) OWNER TO hmdm;

--
-- Name: mdm_resolve_device_property(text, text); Type: FUNCTION; Schema: public; Owner: hmdm
--

CREATE FUNCTION public.mdm_resolve_device_property(server_data text, device_data text) RETURNS text
    LANGUAGE plpgsql
    AS $$
BEGIN
    server_data = COALESCE(server_data, '');
    device_data = COALESCE(device_data, '');

    IF (server_data = device_data) THEN
        RETURN server_data;
    END IF;

    IF LENGTH(device_data) > 0 THEN
        RETURN device_data;
    END IF;

    RETURN server_data;
END
$$;


ALTER FUNCTION public.mdm_resolve_device_property(server_data text, device_data text) OWNER TO hmdm;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: applicationfilestocopytemp; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.applicationfilestocopytemp (
    url character varying(500),
    "?column?" text,
    newurl character varying(500)
);


ALTER TABLE public.applicationfilestocopytemp OWNER TO hmdm;

--
-- Name: applications; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.applications (
    id integer NOT NULL,
    pkg character varying(100) NOT NULL,
    name character varying(500) NOT NULL,
    showicon boolean DEFAULT false NOT NULL,
    customerid bigint,
    system boolean DEFAULT false NOT NULL,
    latestversion integer,
    runafterinstall boolean DEFAULT false NOT NULL,
    type character varying(10) DEFAULT 'app'::character varying NOT NULL,
    icontext character varying(256),
    iconid integer,
    runatboot boolean DEFAULT false NOT NULL,
    usekiosk boolean DEFAULT false NOT NULL
);


ALTER TABLE public.applications OWNER TO hmdm;

--
-- Name: applications_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.applications_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.applications_id_seq OWNER TO hmdm;

--
-- Name: applications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.applications_id_seq OWNED BY public.applications.id;


--
-- Name: applicationversions; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.applicationversions (
    id integer NOT NULL,
    applicationid integer NOT NULL,
    version character varying(50) NOT NULL,
    url character varying(500),
    apkhash character varying(100),
    split boolean DEFAULT false NOT NULL,
    urlarmeabi text,
    urlarm64 text,
    versioncode integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.applicationversions OWNER TO hmdm;

--
-- Name: applicationversions_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.applicationversions_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.applicationversions_id_seq OWNER TO hmdm;

--
-- Name: applicationversions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.applicationversions_id_seq OWNED BY public.applicationversions.id;


--
-- Name: applicationversionstemp; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.applicationversionstemp (
    to_be_deleted boolean,
    to_be_replaced boolean,
    id integer,
    newapplicationid integer,
    newapplicationversionid integer,
    newurl character varying(500),
    name character varying(500),
    pkg character varying(100),
    version character varying(100),
    url character varying(500),
    customerid bigint,
    ismastercustomer boolean,
    masterappexists boolean,
    masterversionexists boolean
);


ALTER TABLE public.applicationversionstemp OWNER TO hmdm;

--
-- Name: configurationapplicationparameters; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.configurationapplicationparameters (
    id integer NOT NULL,
    configurationid integer NOT NULL,
    applicationid integer NOT NULL,
    skipversioncheck boolean DEFAULT false NOT NULL
);


ALTER TABLE public.configurationapplicationparameters OWNER TO hmdm;

--
-- Name: configurationapplicationparameters_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.configurationapplicationparameters_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.configurationapplicationparameters_id_seq OWNER TO hmdm;

--
-- Name: configurationapplicationparameters_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.configurationapplicationparameters_id_seq OWNED BY public.configurationapplicationparameters.id;


--
-- Name: configurationapplications; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.configurationapplications (
    id integer NOT NULL,
    configurationid integer NOT NULL,
    applicationid integer NOT NULL,
    remove boolean DEFAULT false NOT NULL,
    showicon boolean DEFAULT false NOT NULL,
    applicationversionid integer,
    action integer DEFAULT 1 NOT NULL,
    screenorder integer,
    keycode integer,
    bottom boolean DEFAULT false NOT NULL
);


ALTER TABLE public.configurationapplications OWNER TO hmdm;

--
-- Name: configurationapplications_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.configurationapplications_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.configurationapplications_id_seq OWNER TO hmdm;

--
-- Name: configurationapplications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.configurationapplications_id_seq OWNED BY public.configurationapplications.id;


--
-- Name: configurationapplicationsettings; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.configurationapplicationsettings (
    id integer NOT NULL,
    applicationid integer NOT NULL,
    name character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    value text,
    comment text,
    readonly boolean DEFAULT false NOT NULL,
    extrefid integer NOT NULL,
    lastupdate bigint NOT NULL
);


ALTER TABLE public.configurationapplicationsettings OWNER TO hmdm;

--
-- Name: configurationapplicationsettings_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.configurationapplicationsettings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.configurationapplicationsettings_id_seq OWNER TO hmdm;

--
-- Name: configurationapplicationsettings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.configurationapplicationsettings_id_seq OWNED BY public.configurationapplicationsettings.id;


--
-- Name: configurationfiles; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.configurationfiles (
    id integer NOT NULL,
    configurationid integer NOT NULL,
    description text,
    devicepath text NOT NULL,
    externalurl text,
    filepath text,
    checksum text,
    remove boolean DEFAULT false NOT NULL,
    lastupdate bigint DEFAULT (EXTRACT(epoch FROM now()) * (1000)::numeric) NOT NULL,
    fileid integer,
    replacevariables boolean DEFAULT false NOT NULL
);


ALTER TABLE public.configurationfiles OWNER TO hmdm;

--
-- Name: configurationfiles_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.configurationfiles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.configurationfiles_id_seq OWNER TO hmdm;

--
-- Name: configurationfiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.configurationfiles_id_seq OWNED BY public.configurationfiles.id;


--
-- Name: configurations; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.configurations (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    type integer DEFAULT 0 NOT NULL,
    password character varying(100),
    backgroundcolor character varying(20),
    textcolor character varying(20),
    backgroundimageurl character varying(500),
    iconsize text DEFAULT 'SMALL'::text NOT NULL,
    desktopheader text DEFAULT 'NO_HEADER'::text NOT NULL,
    usedefaultdesignsettings boolean DEFAULT true NOT NULL,
    customerid bigint,
    gps boolean,
    bluetooth boolean,
    wifi boolean,
    mobiledata boolean,
    mainappid integer,
    eventreceivingcomponent character varying(512),
    kioskmode boolean DEFAULT false NOT NULL,
    qrcodekey text DEFAULT md5((random())::text) NOT NULL,
    contentappid integer,
    autoupdate boolean DEFAULT false NOT NULL,
    blockstatusbar boolean DEFAULT false NOT NULL,
    systemupdatetype integer DEFAULT 0 NOT NULL,
    systemupdatefrom character varying(10),
    systemupdateto character varying(10),
    usbstorage boolean,
    requestupdates character varying(20) DEFAULT 'DONOTTRACK'::character varying NOT NULL,
    pushoptions character varying(20) DEFAULT 'mqttWorker'::character varying NOT NULL,
    autobrightness boolean,
    brightness integer DEFAULT 180,
    managetimeout boolean DEFAULT false,
    timeout integer DEFAULT 60,
    lockvolume boolean DEFAULT false,
    wifissid character varying(256),
    wifipassword character varying(256),
    wifisecuritytype character varying(16),
    passwordmode character varying(50),
    kioskhome boolean,
    kioskrecents boolean,
    kiosknotifications boolean,
    kiosksysteminfo boolean,
    kioskkeyguard boolean,
    orientation integer,
    rundefaultlauncher boolean,
    timezone character varying(200),
    allowedclasses text,
    newserverurl text,
    locksafesettings boolean,
    disablescreenshots boolean,
    restrictions text,
    defaultfilepath text DEFAULT '/'::text NOT NULL,
    keepalivetime integer,
    managevolume boolean,
    volume integer,
    showwifi boolean,
    mobileenrollment boolean DEFAULT false NOT NULL,
    desktopheadertemplate text,
    kiosklockbuttons boolean,
    scheduleappupdate boolean,
    appupdatefrom character varying(10),
    appupdateto character varying(10),
    disablelocation boolean DEFAULT false NOT NULL,
    apppermissions character varying(20) DEFAULT 'GRANTALL'::character varying NOT NULL
);


ALTER TABLE public.configurations OWNER TO hmdm;

--
-- Name: configurations_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.configurations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.configurations_id_seq OWNER TO hmdm;

--
-- Name: configurations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.configurations_id_seq OWNED BY public.configurations.id;


--
-- Name: customers; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.customers (
    id integer NOT NULL,
    name character varying(50) NOT NULL,
    description text,
    filesdir text NOT NULL,
    master boolean DEFAULT false NOT NULL,
    prefix character varying(100) NOT NULL,
    registrationtime bigint,
    lastlogintime bigint,
    accounttype integer DEFAULT 0 NOT NULL,
    expirytime bigint,
    devicelimit integer DEFAULT 3 NOT NULL,
    customerstatus character varying(100),
    email character varying(50)
);


ALTER TABLE public.customers OWNER TO hmdm;

--
-- Name: customers_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.customers_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.customers_id_seq OWNER TO hmdm;

--
-- Name: customers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.customers_id_seq OWNED BY public.customers.id;


--
-- Name: databasechangelog; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.databasechangelog (
    id character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    filename character varying(255) NOT NULL,
    dateexecuted timestamp without time zone NOT NULL,
    orderexecuted integer NOT NULL,
    exectype character varying(10) NOT NULL,
    md5sum character varying(35),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(20),
    contexts character varying(255),
    labels character varying(255),
    deployment_id character varying(10)
);


ALTER TABLE public.databasechangelog OWNER TO hmdm;

--
-- Name: databasechangeloglock; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby character varying(255)
);


ALTER TABLE public.databasechangeloglock OWNER TO hmdm;

--
-- Name: deviceapplicationsettings; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.deviceapplicationsettings (
    id integer NOT NULL,
    applicationid integer NOT NULL,
    name character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    value text,
    comment text,
    readonly boolean DEFAULT false NOT NULL,
    extrefid integer NOT NULL,
    lastupdate bigint NOT NULL
);


ALTER TABLE public.deviceapplicationsettings OWNER TO hmdm;

--
-- Name: deviceapplicationsettings_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.deviceapplicationsettings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.deviceapplicationsettings_id_seq OWNER TO hmdm;

--
-- Name: deviceapplicationsettings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.deviceapplicationsettings_id_seq OWNED BY public.deviceapplicationsettings.id;


--
-- Name: devicegroups; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.devicegroups (
    id integer NOT NULL,
    deviceid integer NOT NULL,
    groupid integer NOT NULL
);


ALTER TABLE public.devicegroups OWNER TO hmdm;

--
-- Name: devicegroups_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.devicegroups_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.devicegroups_id_seq OWNER TO hmdm;

--
-- Name: devicegroups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.devicegroups_id_seq OWNED BY public.devicegroups.id;


--
-- Name: devices; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.devices (
    id integer NOT NULL,
    number character varying(100) NOT NULL,
    description text,
    lastupdate bigint DEFAULT 0 NOT NULL,
    configurationid integer NOT NULL,
    oldconfigurationid integer,
    info text,
    imei character varying(50),
    phone character varying(20),
    customerid bigint,
    imeiupdatets bigint,
    custom1 text,
    custom2 text,
    custom3 text,
    oldnumber character varying(100),
    fastsearch character varying(100),
    enrolltime bigint
);


ALTER TABLE public.devices OWNER TO hmdm;

--
-- Name: devices_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.devices_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.devices_id_seq OWNER TO hmdm;

--
-- Name: devices_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.devices_id_seq OWNED BY public.devices.id;


--
-- Name: devicestatuses; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.devicestatuses (
    deviceid integer NOT NULL,
    configfilesstatus character varying(100),
    applicationsstatus character varying(100)
);


ALTER TABLE public.devicestatuses OWNER TO hmdm;

--
-- Name: groups; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.groups (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    customerid bigint
);


ALTER TABLE public.groups OWNER TO hmdm;

--
-- Name: groups_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.groups_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.groups_id_seq OWNER TO hmdm;

--
-- Name: groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.groups_id_seq OWNED BY public.groups.id;


--
-- Name: icons; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.icons (
    id integer NOT NULL,
    customerid integer NOT NULL,
    name character varying(64) NOT NULL,
    fileid integer NOT NULL
);


ALTER TABLE public.icons OWNER TO hmdm;

--
-- Name: icons_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.icons_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.icons_id_seq OWNER TO hmdm;

--
-- Name: icons_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.icons_id_seq OWNED BY public.icons.id;


--
-- Name: pendingpushes; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.pendingpushes (
    id integer NOT NULL,
    messageid integer NOT NULL,
    status integer DEFAULT 0 NOT NULL,
    createtime bigint NOT NULL,
    sendtime bigint
);


ALTER TABLE public.pendingpushes OWNER TO hmdm;

--
-- Name: pendingpushes_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.pendingpushes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pendingpushes_id_seq OWNER TO hmdm;

--
-- Name: pendingpushes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.pendingpushes_id_seq OWNED BY public.pendingpushes.id;


--
-- Name: permissions; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.permissions (
    id integer NOT NULL,
    name character varying(50) NOT NULL,
    description text,
    superadmin boolean DEFAULT false NOT NULL
);


ALTER TABLE public.permissions OWNER TO hmdm;

--
-- Name: permissions_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.permissions_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.permissions_id_seq OWNER TO hmdm;

--
-- Name: permissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.permissions_id_seq OWNED BY public.permissions.id;


--
-- Name: plugin_audit_log; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_audit_log (
    id integer NOT NULL,
    createtime bigint DEFAULT (EXTRACT(epoch FROM now()) * (1000)::numeric) NOT NULL,
    customerid integer,
    userid integer,
    login character varying(100),
    action character varying(100),
    payload text,
    ipaddress character varying(500),
    errorcode integer DEFAULT 0
);


ALTER TABLE public.plugin_audit_log OWNER TO hmdm;

--
-- Name: plugin_audit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_audit_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_audit_log_id_seq OWNER TO hmdm;

--
-- Name: plugin_audit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_audit_log_id_seq OWNED BY public.plugin_audit_log.id;


--
-- Name: plugin_deviceinfo_deviceparams; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_deviceinfo_deviceparams (
    id integer NOT NULL,
    deviceid integer NOT NULL,
    customerid integer NOT NULL,
    ts bigint NOT NULL
);


ALTER TABLE public.plugin_deviceinfo_deviceparams OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_device; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_deviceinfo_deviceparams_device (
    id integer NOT NULL,
    recordid integer NOT NULL,
    batterylevel integer,
    batterycharging character varying(20),
    ip character varying(50),
    keyguard boolean,
    ringvolume integer,
    wifi boolean,
    mobiledata boolean,
    gps boolean,
    bluetooth boolean,
    usbstorage boolean,
    memorytotal integer,
    memoryavailable integer
);


ALTER TABLE public.plugin_deviceinfo_deviceparams_device OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_device_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_deviceinfo_deviceparams_device_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_deviceinfo_deviceparams_device_id_seq OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_device_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_deviceinfo_deviceparams_device_id_seq OWNED BY public.plugin_deviceinfo_deviceparams_device.id;


--
-- Name: plugin_deviceinfo_deviceparams_gps; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_deviceinfo_deviceparams_gps (
    id integer NOT NULL,
    recordid integer NOT NULL,
    state character varying(20),
    lat double precision,
    lon double precision,
    alt double precision,
    speed double precision,
    course double precision
);


ALTER TABLE public.plugin_deviceinfo_deviceparams_gps OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_gps_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_deviceinfo_deviceparams_gps_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_deviceinfo_deviceparams_gps_id_seq OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_gps_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_deviceinfo_deviceparams_gps_id_seq OWNED BY public.plugin_deviceinfo_deviceparams_gps.id;


--
-- Name: plugin_deviceinfo_deviceparams_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_deviceinfo_deviceparams_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_deviceinfo_deviceparams_id_seq OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_deviceinfo_deviceparams_id_seq OWNED BY public.plugin_deviceinfo_deviceparams.id;


--
-- Name: plugin_deviceinfo_deviceparams_mobile; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_deviceinfo_deviceparams_mobile (
    id integer NOT NULL,
    recordid integer NOT NULL,
    rssi integer,
    carrier character varying(50),
    data boolean,
    ip character varying(50),
    state character varying(20),
    simstate character varying(20),
    tx bigint,
    rx bigint
);


ALTER TABLE public.plugin_deviceinfo_deviceparams_mobile OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_mobile2; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_deviceinfo_deviceparams_mobile2 (
    id integer NOT NULL,
    recordid integer NOT NULL,
    rssi integer,
    carrier character varying(50),
    data boolean,
    ip character varying(50),
    state character varying(20),
    simstate character varying(20),
    tx bigint,
    rx bigint
);


ALTER TABLE public.plugin_deviceinfo_deviceparams_mobile2 OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_mobile2_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_deviceinfo_deviceparams_mobile2_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_deviceinfo_deviceparams_mobile2_id_seq OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_mobile2_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_deviceinfo_deviceparams_mobile2_id_seq OWNED BY public.plugin_deviceinfo_deviceparams_mobile2.id;


--
-- Name: plugin_deviceinfo_deviceparams_mobile_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_deviceinfo_deviceparams_mobile_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_deviceinfo_deviceparams_mobile_id_seq OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_mobile_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_deviceinfo_deviceparams_mobile_id_seq OWNED BY public.plugin_deviceinfo_deviceparams_mobile.id;


--
-- Name: plugin_deviceinfo_deviceparams_wifi; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_deviceinfo_deviceparams_wifi (
    id integer NOT NULL,
    recordid integer NOT NULL,
    rssi integer,
    ssid character varying(500),
    security character varying(500),
    state character varying(20),
    ip character varying(50),
    tx bigint,
    rx bigint
);


ALTER TABLE public.plugin_deviceinfo_deviceparams_wifi OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_wifi_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_deviceinfo_deviceparams_wifi_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_deviceinfo_deviceparams_wifi_id_seq OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_deviceparams_wifi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_deviceinfo_deviceparams_wifi_id_seq OWNED BY public.plugin_deviceinfo_deviceparams_wifi.id;


--
-- Name: plugin_deviceinfo_settings; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_deviceinfo_settings (
    id integer NOT NULL,
    customerid integer NOT NULL,
    datapreserveperiod integer DEFAULT 30 NOT NULL,
    senddata boolean DEFAULT false NOT NULL,
    intervalmins integer DEFAULT 15 NOT NULL
);


ALTER TABLE public.plugin_deviceinfo_settings OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_settings_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_deviceinfo_settings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_deviceinfo_settings_id_seq OWNER TO hmdm;

--
-- Name: plugin_deviceinfo_settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_deviceinfo_settings_id_seq OWNED BY public.plugin_deviceinfo_settings.id;


--
-- Name: plugin_devicelocations_history; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_devicelocations_history (
    id integer NOT NULL,
    deviceid integer NOT NULL,
    ts bigint NOT NULL,
    lat double precision NOT NULL,
    lon double precision NOT NULL
);


ALTER TABLE public.plugin_devicelocations_history OWNER TO hmdm;

--
-- Name: plugin_devicelocations_history_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_devicelocations_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_devicelocations_history_id_seq OWNER TO hmdm;

--
-- Name: plugin_devicelocations_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_devicelocations_history_id_seq OWNED BY public.plugin_devicelocations_history.id;


--
-- Name: plugin_devicelocations_latest; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_devicelocations_latest (
    id integer NOT NULL,
    deviceid integer NOT NULL,
    ts bigint NOT NULL,
    lat double precision NOT NULL,
    lon double precision NOT NULL
);


ALTER TABLE public.plugin_devicelocations_latest OWNER TO hmdm;

--
-- Name: plugin_devicelocations_latest_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_devicelocations_latest_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_devicelocations_latest_id_seq OWNER TO hmdm;

--
-- Name: plugin_devicelocations_latest_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_devicelocations_latest_id_seq OWNED BY public.plugin_devicelocations_latest.id;


--
-- Name: plugin_devicelocations_settings; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_devicelocations_settings (
    id integer NOT NULL,
    customerid integer NOT NULL,
    datapreserveperiod integer DEFAULT 30 NOT NULL,
    tileserverurl text DEFAULT 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'::text NOT NULL,
    updatetime integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.plugin_devicelocations_settings OWNER TO hmdm;

--
-- Name: plugin_devicelocations_settings_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_devicelocations_settings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_devicelocations_settings_id_seq OWNER TO hmdm;

--
-- Name: plugin_devicelocations_settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_devicelocations_settings_id_seq OWNED BY public.plugin_devicelocations_settings.id;


--
-- Name: plugin_devicelog_log; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_devicelog_log (
    id integer NOT NULL,
    createtime bigint,
    customerid integer NOT NULL,
    deviceid integer NOT NULL,
    applicationid integer NOT NULL,
    ipaddress character varying(512),
    severity text,
    severityorder integer,
    message text
);


ALTER TABLE public.plugin_devicelog_log OWNER TO hmdm;

--
-- Name: plugin_devicelog_log_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_devicelog_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_devicelog_log_id_seq OWNER TO hmdm;

--
-- Name: plugin_devicelog_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_devicelog_log_id_seq OWNED BY public.plugin_devicelog_log.id;


--
-- Name: plugin_devicelog_setting_rule_devices; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_devicelog_setting_rule_devices (
    ruleid integer NOT NULL,
    deviceid integer NOT NULL
);


ALTER TABLE public.plugin_devicelog_setting_rule_devices OWNER TO hmdm;

--
-- Name: plugin_devicelog_settings; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_devicelog_settings (
    id integer NOT NULL,
    customerid integer NOT NULL,
    logspreserveperiod integer DEFAULT 30 NOT NULL
);


ALTER TABLE public.plugin_devicelog_settings OWNER TO hmdm;

--
-- Name: plugin_devicelog_settings_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_devicelog_settings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_devicelog_settings_id_seq OWNER TO hmdm;

--
-- Name: plugin_devicelog_settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_devicelog_settings_id_seq OWNED BY public.plugin_devicelog_settings.id;


--
-- Name: plugin_devicelog_settings_rules; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_devicelog_settings_rules (
    id integer NOT NULL,
    settingid integer NOT NULL,
    name character varying(120) NOT NULL,
    active boolean DEFAULT true NOT NULL,
    applicationid integer NOT NULL,
    severity text NOT NULL,
    filter text,
    groupid integer,
    configurationid integer
);


ALTER TABLE public.plugin_devicelog_settings_rules OWNER TO hmdm;

--
-- Name: plugin_devicelog_settings_rules_id_seq1; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_devicelog_settings_rules_id_seq1
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_devicelog_settings_rules_id_seq1 OWNER TO hmdm;

--
-- Name: plugin_devicelog_settings_rules_id_seq1; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_devicelog_settings_rules_id_seq1 OWNED BY public.plugin_devicelog_settings_rules.id;


--
-- Name: plugin_devicereset_status; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_devicereset_status (
    id integer NOT NULL,
    deviceid integer NOT NULL,
    statusresetrequested bigint,
    statusresetconfirmed bigint,
    rebootrequested bigint,
    rebootconfirmed bigint,
    devicelocked boolean DEFAULT false NOT NULL,
    lockmessage text,
    password text
);


ALTER TABLE public.plugin_devicereset_status OWNER TO hmdm;

--
-- Name: plugin_devicereset_status_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_devicereset_status_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_devicereset_status_id_seq OWNER TO hmdm;

--
-- Name: plugin_devicereset_status_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_devicereset_status_id_seq OWNED BY public.plugin_devicereset_status.id;


--
-- Name: plugin_knox_rules; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_knox_rules (
    id integer NOT NULL,
    configurationid integer NOT NULL,
    rule text,
    tabletype text DEFAULT 'OUTGOING_CALL'::text NOT NULL,
    ruletype text DEFAULT 'BLACKLIST'::text NOT NULL
);


ALTER TABLE public.plugin_knox_rules OWNER TO hmdm;

--
-- Name: plugin_knox_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_knox_rules_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_knox_rules_id_seq OWNER TO hmdm;

--
-- Name: plugin_knox_rules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_knox_rules_id_seq OWNED BY public.plugin_knox_rules.id;


--
-- Name: plugin_messaging_messages; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_messaging_messages (
    id integer NOT NULL,
    customerid integer NOT NULL,
    deviceid integer NOT NULL,
    ts bigint NOT NULL,
    message character varying(5000),
    status integer NOT NULL
);


ALTER TABLE public.plugin_messaging_messages OWNER TO hmdm;

--
-- Name: plugin_messaging_messages_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_messaging_messages_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_messaging_messages_id_seq OWNER TO hmdm;

--
-- Name: plugin_messaging_messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_messaging_messages_id_seq OWNED BY public.plugin_messaging_messages.id;


--
-- Name: plugin_openvpn_defaults; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_openvpn_defaults (
    id integer NOT NULL,
    customerid integer NOT NULL,
    removevpns text,
    removeall boolean DEFAULT false NOT NULL,
    vpnname text,
    vpnconfig text,
    vpnurl text,
    connect boolean DEFAULT false NOT NULL,
    alwayson boolean DEFAULT false NOT NULL
);


ALTER TABLE public.plugin_openvpn_defaults OWNER TO hmdm;

--
-- Name: plugin_openvpn_defaults_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_openvpn_defaults_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_openvpn_defaults_id_seq OWNER TO hmdm;

--
-- Name: plugin_openvpn_defaults_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_openvpn_defaults_id_seq OWNED BY public.plugin_openvpn_defaults.id;


--
-- Name: plugin_photo_photo; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_photo_photo (
    id integer NOT NULL,
    createtime timestamp without time zone,
    lat double precision,
    lng double precision,
    path text NOT NULL,
    deviceid integer NOT NULL,
    customerid integer NOT NULL,
    thumbnailimagepath text,
    contenttype text,
    address text
);


ALTER TABLE public.plugin_photo_photo OWNER TO hmdm;

--
-- Name: plugin_photo_photo_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_photo_photo_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_photo_photo_id_seq OWNER TO hmdm;

--
-- Name: plugin_photo_photo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_photo_photo_id_seq OWNED BY public.plugin_photo_photo.id;


--
-- Name: plugin_photo_photo_places; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_photo_photo_places (
    id integer NOT NULL,
    photoid integer NOT NULL,
    pointid character varying(1024) NOT NULL,
    pointaddress text NOT NULL
);


ALTER TABLE public.plugin_photo_photo_places OWNER TO hmdm;

--
-- Name: plugin_photo_photo_places_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_photo_photo_places_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_photo_photo_places_id_seq OWNER TO hmdm;

--
-- Name: plugin_photo_photo_places_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_photo_photo_places_id_seq OWNED BY public.plugin_photo_photo_places.id;


--
-- Name: plugin_photo_places; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_photo_places (
    id integer NOT NULL,
    customerid integer NOT NULL,
    placeid character varying(1024) NOT NULL,
    lat double precision NOT NULL,
    lng double precision NOT NULL,
    address text,
    reserve text
);


ALTER TABLE public.plugin_photo_places OWNER TO hmdm;

--
-- Name: plugin_photo_places_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_photo_places_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_photo_places_id_seq OWNER TO hmdm;

--
-- Name: plugin_photo_places_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_photo_places_id_seq OWNED BY public.plugin_photo_places.id;


--
-- Name: plugin_photo_settings; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_photo_settings (
    id integer NOT NULL,
    customerid integer NOT NULL,
    tracklocation boolean DEFAULT true NOT NULL,
    trackingoffwarning text,
    sendphoto boolean DEFAULT false NOT NULL,
    imagepaths text,
    imagedeletiondelay integer,
    addtext boolean DEFAULT false NOT NULL,
    backgroundcolor character varying(20),
    textcolor character varying(20),
    transparency integer,
    textcontent text,
    linkphototoplace boolean DEFAULT false NOT NULL,
    searchplaceradius integer DEFAULT 0 NOT NULL,
    nontransmittedpaths text,
    includestandardimagepaths boolean DEFAULT false NOT NULL,
    filetypes text,
    directory text,
    purgedays integer DEFAULT 0 NOT NULL,
    nametemplate text
);


ALTER TABLE public.plugin_photo_settings OWNER TO hmdm;

--
-- Name: plugin_photo_settings_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_photo_settings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_photo_settings_id_seq OWNER TO hmdm;

--
-- Name: plugin_photo_settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_photo_settings_id_seq OWNED BY public.plugin_photo_settings.id;


--
-- Name: plugin_push_messages; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugin_push_messages (
    id integer NOT NULL,
    customerid integer NOT NULL,
    deviceid integer NOT NULL,
    ts bigint NOT NULL,
    messagetype character varying(255),
    payload text
);


ALTER TABLE public.plugin_push_messages OWNER TO hmdm;

--
-- Name: plugin_push_messages_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugin_push_messages_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugin_push_messages_id_seq OWNER TO hmdm;

--
-- Name: plugin_push_messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugin_push_messages_id_seq OWNED BY public.plugin_push_messages.id;


--
-- Name: plugins; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.plugins (
    id integer NOT NULL,
    identifier character varying(50) NOT NULL,
    name text NOT NULL,
    description text,
    createtime timestamp without time zone DEFAULT now() NOT NULL,
    disabled boolean DEFAULT false NOT NULL,
    javascriptmodulefile character varying(200),
    functionsviewtemplate character varying(200),
    settingsviewtemplate character varying(200),
    namelocalizationkey character varying(200) DEFAULT 'plugin.name.not.specified'::character varying NOT NULL,
    settingspermission character varying(200),
    functionspermission character varying(200),
    devicefunctionspermission character varying(200),
    enabledfordevice boolean DEFAULT false NOT NULL
);


ALTER TABLE public.plugins OWNER TO hmdm;

--
-- Name: plugins_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.plugins_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.plugins_id_seq OWNER TO hmdm;

--
-- Name: plugins_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.plugins_id_seq OWNED BY public.plugins.id;


--
-- Name: pluginsdisabled; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.pluginsdisabled (
    pluginid integer NOT NULL,
    customerid integer NOT NULL
);


ALTER TABLE public.pluginsdisabled OWNER TO hmdm;

--
-- Name: pushmessages; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.pushmessages (
    id integer NOT NULL,
    messagetype character varying(50) NOT NULL,
    deviceid integer NOT NULL,
    payload text
);


ALTER TABLE public.pushmessages OWNER TO hmdm;

--
-- Name: pushmessages_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.pushmessages_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pushmessages_id_seq OWNER TO hmdm;

--
-- Name: pushmessages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.pushmessages_id_seq OWNED BY public.pushmessages.id;


--
-- Name: settings; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.settings (
    id integer NOT NULL,
    backgroundcolor character varying(20),
    textcolor character varying(20),
    backgroundimageurl character varying(500),
    iconsize text DEFAULT 'SMALL'::text NOT NULL,
    desktopheader text DEFAULT 'NO_HEADER'::text NOT NULL,
    customerid bigint,
    usedefaultlanguage boolean DEFAULT true NOT NULL,
    language character varying(20),
    createnewdevices boolean DEFAULT false NOT NULL,
    newdevicegroupid integer,
    newdeviceconfigurationid integer,
    phonenumberformat character varying(50) DEFAULT '+9 (999) 999-99-99'::character varying,
    custompropertyname1 character varying(200),
    custompropertyname2 character varying(200),
    custompropertyname3 character varying(200),
    custommultiline1 boolean DEFAULT false NOT NULL,
    custommultiline2 boolean DEFAULT false NOT NULL,
    custommultiline3 boolean DEFAULT false NOT NULL,
    customsend1 boolean DEFAULT false NOT NULL,
    customsend2 boolean DEFAULT false NOT NULL,
    customsend3 boolean DEFAULT false NOT NULL,
    desktopheadertemplate text,
    senddescription boolean DEFAULT false NOT NULL,
    passwordreset boolean DEFAULT false NOT NULL,
    passwordlength integer DEFAULT 0 NOT NULL,
    passwordstrength integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.settings OWNER TO hmdm;

--
-- Name: settings_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.settings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.settings_id_seq OWNER TO hmdm;

--
-- Name: settings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.settings_id_seq OWNED BY public.settings.id;


--
-- Name: trialkey; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.trialkey (
    id integer NOT NULL,
    keycode character varying(50) NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.trialkey OWNER TO hmdm;

--
-- Name: trialkey_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.trialkey_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.trialkey_id_seq OWNER TO hmdm;

--
-- Name: trialkey_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.trialkey_id_seq OWNED BY public.trialkey.id;


--
-- Name: uploadedfiles; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.uploadedfiles (
    id integer NOT NULL,
    customerid integer NOT NULL,
    filepath text NOT NULL,
    uploadtime bigint DEFAULT (EXTRACT(epoch FROM now()) * (1000)::numeric) NOT NULL
);


ALTER TABLE public.uploadedfiles OWNER TO hmdm;

--
-- Name: uploadedfiles_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.uploadedfiles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.uploadedfiles_id_seq OWNER TO hmdm;

--
-- Name: uploadedfiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.uploadedfiles_id_seq OWNED BY public.uploadedfiles.id;


--
-- Name: userconfigurationaccess; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.userconfigurationaccess (
    id integer NOT NULL,
    userid integer NOT NULL,
    configurationid integer NOT NULL
);


ALTER TABLE public.userconfigurationaccess OWNER TO hmdm;

--
-- Name: userconfigurationaccess_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.userconfigurationaccess_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.userconfigurationaccess_id_seq OWNER TO hmdm;

--
-- Name: userconfigurationaccess_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.userconfigurationaccess_id_seq OWNED BY public.userconfigurationaccess.id;


--
-- Name: userdevicegroupsaccess; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.userdevicegroupsaccess (
    id integer NOT NULL,
    userid integer NOT NULL,
    groupid integer NOT NULL
);


ALTER TABLE public.userdevicegroupsaccess OWNER TO hmdm;

--
-- Name: userdevicegroupsaccess_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.userdevicegroupsaccess_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.userdevicegroupsaccess_id_seq OWNER TO hmdm;

--
-- Name: userdevicegroupsaccess_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.userdevicegroupsaccess_id_seq OWNED BY public.userdevicegroupsaccess.id;


--
-- Name: userhints; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.userhints (
    id integer NOT NULL,
    userid integer NOT NULL,
    hintkey character varying(100) NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.userhints OWNER TO hmdm;

--
-- Name: userhints_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.userhints_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.userhints_id_seq OWNER TO hmdm;

--
-- Name: userhints_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.userhints_id_seq OWNED BY public.userhints.id;


--
-- Name: userhinttypes; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.userhinttypes (
    hintkey character varying(100) NOT NULL
);


ALTER TABLE public.userhinttypes OWNER TO hmdm;

--
-- Name: userrolepermissions; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.userrolepermissions (
    roleid integer NOT NULL,
    permissionid integer NOT NULL
);


ALTER TABLE public.userrolepermissions OWNER TO hmdm;

--
-- Name: userroles; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.userroles (
    id integer NOT NULL,
    name character varying(50) NOT NULL,
    description text,
    superadmin boolean DEFAULT false NOT NULL
);


ALTER TABLE public.userroles OWNER TO hmdm;

--
-- Name: userroles_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.userroles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.userroles_id_seq OWNER TO hmdm;

--
-- Name: userroles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.userroles_id_seq OWNED BY public.userroles.id;


--
-- Name: userrolesettings; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.userrolesettings (
    id integer NOT NULL,
    roleid integer NOT NULL,
    customerid integer NOT NULL,
    columndisplayeddevicestatus boolean,
    columndisplayeddevicedate boolean,
    columndisplayeddevicenumber boolean,
    columndisplayeddevicemodel boolean,
    columndisplayeddevicepermissionsstatus boolean,
    columndisplayeddeviceappinstallstatus boolean,
    columndisplayeddeviceconfiguration boolean,
    columndisplayeddeviceimei boolean,
    columndisplayeddevicephone boolean,
    columndisplayeddevicedesc boolean,
    columndisplayeddevicegroup boolean,
    columndisplayedlauncherversion boolean,
    columndisplayedbatterylevel boolean,
    columndisplayeddevicefilesstatus boolean,
    columndisplayeddefaultlauncher boolean,
    columndisplayedcustom1 boolean,
    columndisplayedcustom2 boolean,
    columndisplayedcustom3 boolean,
    columndisplayedmdmmode boolean,
    columndisplayedkioskmode boolean,
    columndisplayedandroidversion boolean,
    columndisplayedenrollmentdate boolean,
    columndisplayedserial boolean
);


ALTER TABLE public.userrolesettings OWNER TO hmdm;

--
-- Name: userrolesettings_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.userrolesettings_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.userrolesettings_id_seq OWNER TO hmdm;

--
-- Name: userrolesettings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.userrolesettings_id_seq OWNED BY public.userrolesettings.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: hmdm
--

CREATE TABLE public.users (
    id integer NOT NULL,
    login character varying(30) NOT NULL,
    email character varying(50),
    name character varying(50),
    password character varying(40) NOT NULL,
    customerid bigint,
    userroleid integer,
    alldevicesavailable boolean DEFAULT true NOT NULL,
    allconfigavailable boolean DEFAULT true NOT NULL,
    passwordreset boolean DEFAULT false NOT NULL,
    authtoken character varying(40),
    passwordresettoken character varying(40)
);


ALTER TABLE public.users OWNER TO hmdm;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: hmdm
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO hmdm;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hmdm
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: applications id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.applications ALTER COLUMN id SET DEFAULT nextval('public.applications_id_seq'::regclass);


--
-- Name: applicationversions id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.applicationversions ALTER COLUMN id SET DEFAULT nextval('public.applicationversions_id_seq'::regclass);


--
-- Name: configurationapplicationparameters id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplicationparameters ALTER COLUMN id SET DEFAULT nextval('public.configurationapplicationparameters_id_seq'::regclass);


--
-- Name: configurationapplications id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplications ALTER COLUMN id SET DEFAULT nextval('public.configurationapplications_id_seq'::regclass);


--
-- Name: configurationapplicationsettings id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplicationsettings ALTER COLUMN id SET DEFAULT nextval('public.configurationapplicationsettings_id_seq'::regclass);


--
-- Name: configurationfiles id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationfiles ALTER COLUMN id SET DEFAULT nextval('public.configurationfiles_id_seq'::regclass);


--
-- Name: configurations id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurations ALTER COLUMN id SET DEFAULT nextval('public.configurations_id_seq'::regclass);


--
-- Name: customers id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.customers ALTER COLUMN id SET DEFAULT nextval('public.customers_id_seq'::regclass);


--
-- Name: deviceapplicationsettings id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.deviceapplicationsettings ALTER COLUMN id SET DEFAULT nextval('public.deviceapplicationsettings_id_seq'::regclass);


--
-- Name: devicegroups id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devicegroups ALTER COLUMN id SET DEFAULT nextval('public.devicegroups_id_seq'::regclass);


--
-- Name: devices id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devices ALTER COLUMN id SET DEFAULT nextval('public.devices_id_seq'::regclass);


--
-- Name: groups id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.groups ALTER COLUMN id SET DEFAULT nextval('public.groups_id_seq'::regclass);


--
-- Name: icons id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.icons ALTER COLUMN id SET DEFAULT nextval('public.icons_id_seq'::regclass);


--
-- Name: pendingpushes id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.pendingpushes ALTER COLUMN id SET DEFAULT nextval('public.pendingpushes_id_seq'::regclass);


--
-- Name: permissions id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.permissions ALTER COLUMN id SET DEFAULT nextval('public.permissions_id_seq'::regclass);


--
-- Name: plugin_audit_log id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_audit_log ALTER COLUMN id SET DEFAULT nextval('public.plugin_audit_log_id_seq'::regclass);


--
-- Name: plugin_deviceinfo_deviceparams id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams ALTER COLUMN id SET DEFAULT nextval('public.plugin_deviceinfo_deviceparams_id_seq'::regclass);


--
-- Name: plugin_deviceinfo_deviceparams_device id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_device ALTER COLUMN id SET DEFAULT nextval('public.plugin_deviceinfo_deviceparams_device_id_seq'::regclass);


--
-- Name: plugin_deviceinfo_deviceparams_gps id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_gps ALTER COLUMN id SET DEFAULT nextval('public.plugin_deviceinfo_deviceparams_gps_id_seq'::regclass);


--
-- Name: plugin_deviceinfo_deviceparams_mobile id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_mobile ALTER COLUMN id SET DEFAULT nextval('public.plugin_deviceinfo_deviceparams_mobile_id_seq'::regclass);


--
-- Name: plugin_deviceinfo_deviceparams_mobile2 id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_mobile2 ALTER COLUMN id SET DEFAULT nextval('public.plugin_deviceinfo_deviceparams_mobile2_id_seq'::regclass);


--
-- Name: plugin_deviceinfo_deviceparams_wifi id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_wifi ALTER COLUMN id SET DEFAULT nextval('public.plugin_deviceinfo_deviceparams_wifi_id_seq'::regclass);


--
-- Name: plugin_deviceinfo_settings id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_settings ALTER COLUMN id SET DEFAULT nextval('public.plugin_deviceinfo_settings_id_seq'::regclass);


--
-- Name: plugin_devicelocations_history id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_history ALTER COLUMN id SET DEFAULT nextval('public.plugin_devicelocations_history_id_seq'::regclass);


--
-- Name: plugin_devicelocations_latest id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_latest ALTER COLUMN id SET DEFAULT nextval('public.plugin_devicelocations_latest_id_seq'::regclass);


--
-- Name: plugin_devicelocations_settings id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_settings ALTER COLUMN id SET DEFAULT nextval('public.plugin_devicelocations_settings_id_seq'::regclass);


--
-- Name: plugin_devicelog_log id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_log ALTER COLUMN id SET DEFAULT nextval('public.plugin_devicelog_log_id_seq'::regclass);


--
-- Name: plugin_devicelog_settings id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_settings ALTER COLUMN id SET DEFAULT nextval('public.plugin_devicelog_settings_id_seq'::regclass);


--
-- Name: plugin_devicelog_settings_rules id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_settings_rules ALTER COLUMN id SET DEFAULT nextval('public.plugin_devicelog_settings_rules_id_seq1'::regclass);


--
-- Name: plugin_devicereset_status id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicereset_status ALTER COLUMN id SET DEFAULT nextval('public.plugin_devicereset_status_id_seq'::regclass);


--
-- Name: plugin_knox_rules id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_knox_rules ALTER COLUMN id SET DEFAULT nextval('public.plugin_knox_rules_id_seq'::regclass);


--
-- Name: plugin_messaging_messages id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_messaging_messages ALTER COLUMN id SET DEFAULT nextval('public.plugin_messaging_messages_id_seq'::regclass);


--
-- Name: plugin_openvpn_defaults id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_openvpn_defaults ALTER COLUMN id SET DEFAULT nextval('public.plugin_openvpn_defaults_id_seq'::regclass);


--
-- Name: plugin_photo_photo id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_photo ALTER COLUMN id SET DEFAULT nextval('public.plugin_photo_photo_id_seq'::regclass);


--
-- Name: plugin_photo_photo_places id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_photo_places ALTER COLUMN id SET DEFAULT nextval('public.plugin_photo_photo_places_id_seq'::regclass);


--
-- Name: plugin_photo_places id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_places ALTER COLUMN id SET DEFAULT nextval('public.plugin_photo_places_id_seq'::regclass);


--
-- Name: plugin_photo_settings id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_settings ALTER COLUMN id SET DEFAULT nextval('public.plugin_photo_settings_id_seq'::regclass);


--
-- Name: plugin_push_messages id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_push_messages ALTER COLUMN id SET DEFAULT nextval('public.plugin_push_messages_id_seq'::regclass);


--
-- Name: plugins id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugins ALTER COLUMN id SET DEFAULT nextval('public.plugins_id_seq'::regclass);


--
-- Name: pushmessages id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.pushmessages ALTER COLUMN id SET DEFAULT nextval('public.pushmessages_id_seq'::regclass);


--
-- Name: settings id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.settings ALTER COLUMN id SET DEFAULT nextval('public.settings_id_seq'::regclass);


--
-- Name: trialkey id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.trialkey ALTER COLUMN id SET DEFAULT nextval('public.trialkey_id_seq'::regclass);


--
-- Name: uploadedfiles id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.uploadedfiles ALTER COLUMN id SET DEFAULT nextval('public.uploadedfiles_id_seq'::regclass);


--
-- Name: userconfigurationaccess id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userconfigurationaccess ALTER COLUMN id SET DEFAULT nextval('public.userconfigurationaccess_id_seq'::regclass);


--
-- Name: userdevicegroupsaccess id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userdevicegroupsaccess ALTER COLUMN id SET DEFAULT nextval('public.userdevicegroupsaccess_id_seq'::regclass);


--
-- Name: userhints id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userhints ALTER COLUMN id SET DEFAULT nextval('public.userhints_id_seq'::regclass);


--
-- Name: userroles id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userroles ALTER COLUMN id SET DEFAULT nextval('public.userroles_id_seq'::regclass);


--
-- Name: userrolesettings id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userrolesettings ALTER COLUMN id SET DEFAULT nextval('public.userrolesettings_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: applicationfilestocopytemp; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.applicationfilestocopytemp (url, "?column?", newurl) FROM stdin;
\.


--
-- Data for Name: applications; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.applications (id, pkg, name, showicon, customerid, system, latestversion, runafterinstall, type, icontext, iconid, runatboot, usekiosk) FROM stdin;
1	com.android.systemui	System UI	f	1	t	10000	f	app	\N	\N	f	f
2	com.android.bluetooth	Bluetooth Service	f	1	t	10001	f	app	\N	\N	f	f
3	com.google.android.gms	Google Services	f	1	t	10002	f	app	\N	\N	f	f
34	com.android.email	Email client	t	1	t	10033	f	app	\N	\N	f	f
9	com.android.chrome	Chrome Browser	t	1	t	10008	f	app	\N	\N	f	f
10	com.sec.android.app.browser	Browser (Samsung)	t	1	t	10009	f	app	\N	\N	f	f
11	com.samsung.android.video	Samsung Video	f	1	t	10010	f	app	\N	\N	f	f
12	com.android.providers.media	Media Service	f	1	t	10011	f	app	\N	\N	f	f
13	com.android.gallery3d	Gallery	t	1	t	10012	f	app	\N	\N	f	f
14	com.sec.android.gallery3d	Gallery (Samsung)	t	1	t	10013	f	app	\N	\N	f	f
15	com.android.vending	Google Payment support	f	1	t	10014	f	app	\N	\N	f	f
16	com.samsung.android.app.memo	Notes (Samsung)	t	1	t	10015	f	app	\N	\N	f	f
35	com.android.documentsui	File manager extension	f	1	t	10034	f	app	\N	\N	f	f
5	com.google.android.packageinstaller	Package installer (Google)	f	1	t	10004	f	app	\N	\N	f	f
17	com.android.packageinstaller	Package Installer	f	1	t	10016	f	app	\N	\N	f	f
18	com.samsung.android.calendar	Calendar (Samsung)	t	1	t	10017	f	app	\N	\N	f	f
19	com.android.calculator2	Calculator (generic)	t	1	t	10018	f	app	\N	\N	f	f
20	com.sec.android.app.popupcalculator	Calculator (Samsung)	t	1	t	10019	f	app	\N	\N	f	f
21	com.android.camera	Camera (generic)	t	1	t	10020	f	app	\N	\N	f	f
22	com.huawei.camera	Camera (Huawei)	t	1	t	10021	f	app	\N	\N	f	f
23	org.codeaurora.snapcam	Camera (Lenovo)	t	1	t	10022	f	app	\N	\N	f	f
24	com.mediatek.camera	Camera (Mediatek)	t	1	t	10023	f	app	\N	\N	f	f
25	com.sec.android.app.camera	Camera (Samsung, legacy)	t	1	t	10024	f	app	\N	\N	f	f
26	com.sec.android.camera	Camera (Samsung)	t	1	t	10025	f	app	\N	\N	f	f
27	com.google.android.apps.maps	Google Maps	t	1	t	10026	f	app	\N	\N	f	f
28	com.touchtype.swiftkey	Swiftkey keyboard extension	f	1	t	10027	f	app	\N	\N	f	f
29	com.android.contacts	Contacts	t	1	t	10028	f	app	\N	\N	f	f
31	com.sec.android.app.myfiles	File Manager (Samsung)	t	1	t	10030	f	app	\N	\N	f	f
32	com.android.settings	Settings (usually must be disabled!)	f	1	t	10031	f	app	\N	\N	f	f
33	com.sec.android.inputmethod	Keyboard settings (Samsung)	f	1	t	10032	f	app	\N	\N	f	f
36	com.samsung.android.email.provider	Email service (Samsung)	f	1	t	10035	f	app	\N	\N	f	f
37	android	Android system package	f	1	t	10036	f	app	\N	\N	f	f
38	com.android.mms	Messaging (generic)	t	1	t	10037	f	app	\N	\N	f	f
39	com.google.android.apps.messaging	Messaging (Google)	t	1	t	10038	f	app	\N	\N	f	f
40	com.android.dialer	Phone (generic UI)	t	1	t	10039	f	app	\N	\N	f	f
41	com.sec.phone	Phone (Samsung)	t	1	t	10040	f	app	\N	\N	f	f
42	com.android.phone	Phone (generic service)	t	1	t	10041	f	app	\N	\N	f	f
43	com.huaqin.filemanager	File manager (Lenovo)	t	1	t	10042	f	app	\N	\N	f	f
6	com.google.android.apps.photos	Gallery (Google)	t	1	t	10005	f	app	\N	\N	f	f
4	com.google.android.apps.docs	Google Drive	t	1	t	10003	f	app	\N	\N	f	f
30	com.huawei.android.launcher	Default launcher (Huawei)	f	1	t	10029	f	app	\N	\N	f	f
8	com.android.browser	Browser (generic)	t	1	t	10007	f	app	\N	\N	f	f
46	com.hmdm.launcher	Headwind MDM	f	1	f	10045	f	app	\N	\N	f	f
47	com.huawei.android.internal.app	Huawei Launcher Selector	f	1	t	10046	f	app	\N	\N	f	f
48	com.hmdm.pager	Headwind MDM Pager Plugin	t	1	f	10047	f	app	\N	\N	f	f
49	com.hmdm.phoneproxy	Dialer Helper	t	1	f	10048	f	app	\N	\N	f	f
50	com.hmdm.emuilauncherrestarter	Headwind MDM update helper	f	1	f	10049	f	app	\N	\N	f	f
51	com.miui.cleanmaster	MIUI Clean Master	f	1	t	10050	f	app	\N	\N	f	f
52	com.miui.gallery	MIUI Gallery	t	1	t	10051	f	app	\N	\N	f	f
53	com.miui.notes	MIUI Notes	t	1	t	10052	f	app	\N	\N	f	f
54	com.miui.global.packageinstaller	MIUI Package Installer	f	1	t	10053	f	app	\N	\N	f	f
55	com.miui.msa.global	MIUI Permissions Manager	f	1	t	10054	f	app	\N	\N	f	f
56	com.miui.securitycenter	MIUI Security Center	f	1	t	10055	f	app	\N	\N	f	f
57	com.xiaomi.discover	Xiaomi Updater	f	1	t	10056	f	app	\N	\N	f	f
58	com.google.android.permissioncontroller	Permission Controller	f	1	t	10057	f	app	\N	\N	f	f
59	com.samsung.accessibility	Samsung Accessibility	f	1	t	10058	f	app	\N	\N	f	f
60	com.android.updater	System Update Service	f	1	t	10059	f	app	\N	\N	f	f
61	com.android.printspooler	Print Service	f	1	t	10060	f	app	\N	\N	f	f
62	com.google.android.documentsui	File Manager Extension (Google)	f	1	t	10061	f	app	\N	\N	f	f
63	com.google.android.contacts	Contacts (Google)	t	1	t	10062	f	app	\N	\N	f	f
64	com.google.android.dialer	Dialer (Google)	t	1	t	10063	f	app	\N	\N	f	f
65	com.samsung.android.app.notes	Samsung Notes	t	1	t	10064	f	app	\N	\N	f	f
66	com.hmdglobal.camera2	Nokia Camera (new)	t	1	t	10065	f	app	\N	\N	f	f
67	com.hmdglobal.app.camera	Nokia Camera	t	1	t	10066	f	app	\N	\N	f	f
68	com.samsung.android.dialer	Samsung Dialer	t	1	t	10067	f	app	\N	\N	f	f
69	com.samsung.android.app.contacts	Samsung Contacts	t	1	t	10068	f	app	\N	\N	f	f
70	com.samsung.android.messaging	Samsung Messaging	f	1	t	10069	f	app	\N	\N	f	f
71	com.sec.android.app.launcher	Samsung Launcher (for Recents)	f	1	t	10070	f	app	\N	\N	f	f
72	com.google.android.apps.photos	Photos (Google)	t	1	t	10071	f	app	\N	\N	f	f
73	com.google.android.apps.nbu.files	File Manager (Google)	t	1	t	10072	f	app	\N	\N	f	f
74	com.android.settings.intelligence	Samsung Search Settings	f	1	t	10073	f	app	\N	\N	f	f
75	com.huawei.bluetooth	Huawei Bluetooth	f	1	t	10074	f	app	\N	\N	f	f
76	com.google.android.gms.setup	Google Services Setup	f	1	t	10075	f	app	\N	\N	f	f
77	com.samsung.android.app.telephonyui	Samsung Telephony	f	1	t	10076	f	app	\N	\N	f	f
\.


--
-- Data for Name: applicationversions; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.applicationversions (id, applicationid, version, url, apkhash, split, urlarmeabi, urlarm64, versioncode) FROM stdin;
10000	1	0	\N	\N	f	\N	\N	0
10001	2	0	\N	\N	f	\N	\N	0
10002	3	0	\N	\N	f	\N	\N	0
10003	4	0	\N	\N	f	\N	\N	0
10004	5	0	\N	\N	f	\N	\N	0
10005	6	0	\N	\N	f	\N	\N	0
10007	8	0	\N	\N	f	\N	\N	0
10008	9	0	\N	\N	f	\N	\N	0
10009	10	0	\N	\N	f	\N	\N	0
10010	11	0	\N	\N	f	\N	\N	0
10011	12	0	\N	\N	f	\N	\N	0
10012	13	0	\N	\N	f	\N	\N	0
10013	14	0	\N	\N	f	\N	\N	0
10014	15	0	\N	\N	f	\N	\N	0
10015	16	0	\N	\N	f	\N	\N	0
10016	17	0	\N	\N	f	\N	\N	0
10017	18	0	\N	\N	f	\N	\N	0
10018	19	0	\N	\N	f	\N	\N	0
10019	20	0	\N	\N	f	\N	\N	0
10020	21	0	\N	\N	f	\N	\N	0
10021	22	0	\N	\N	f	\N	\N	0
10022	23	0	\N	\N	f	\N	\N	0
10023	24	0	\N	\N	f	\N	\N	0
10024	25	0	\N	\N	f	\N	\N	0
10025	26	0	\N	\N	f	\N	\N	0
10026	27	0	\N	\N	f	\N	\N	0
10027	28	0	\N	\N	f	\N	\N	0
10028	29	0	\N	\N	f	\N	\N	0
10029	30	0	\N	\N	f	\N	\N	0
10030	31	0	\N	\N	f	\N	\N	0
10031	32	0	\N	\N	f	\N	\N	0
10032	33	0	\N	\N	f	\N	\N	0
10033	34	0	\N	\N	f	\N	\N	0
10034	35	0	\N	\N	f	\N	\N	0
10035	36	0	\N	\N	f	\N	\N	0
10036	37	0	\N	\N	f	\N	\N	0
10037	38	0	\N	\N	f	\N	\N	0
10038	39	0	\N	\N	f	\N	\N	0
10039	40	0	\N	\N	f	\N	\N	0
10040	41	0	\N	\N	f	\N	\N	0
10041	42	0	\N	\N	f	\N	\N	0
10042	43	0	\N	\N	f	\N	\N	0
10045	46	_HMDM_VERSION_	https://h-mdm.com/files/_HMDM_APK_	\N	f	\N	\N	0
10046	47	0	\N	\N	f	\N	\N	0
10047	48	1.02	https://h-mdm.com/files/pager-1.02.apk	\N	f	\N	\N	0
10048	49	1.02	https://h-mdm.com/files/phoneproxy-1.02.apk	\N	f	\N	\N	0
10049	50	1.04	https://h-mdm.com/files/LauncherRestarter-1.04.apk	\N	f	\N	\N	0
10050	51	0	\N	\N	f	\N	\N	0
10051	52	0	\N	\N	f	\N	\N	0
10052	53	0	\N	\N	f	\N	\N	0
10053	54	0	\N	\N	f	\N	\N	0
10054	55	0	\N	\N	f	\N	\N	0
10055	56	0	\N	\N	f	\N	\N	0
10056	57	0	\N	\N	f	\N	\N	0
10057	58	0	\N	\N	f	\N	\N	0
10058	59	0	\N	\N	f	\N	\N	0
10059	60	0	\N	\N	f	\N	\N	0
10060	61	0	\N	\N	f	\N	\N	0
10061	62	0	\N	\N	f	\N	\N	0
10062	63	0	\N	\N	f	\N	\N	0
10063	64	0	\N	\N	f	\N	\N	0
10064	65	0	\N	\N	f	\N	\N	0
10065	66	0	\N	\N	f	\N	\N	0
10066	67	0	\N	\N	f	\N	\N	0
10067	68	0	\N	\N	f	\N	\N	0
10068	69	0	\N	\N	f	\N	\N	0
10069	70	0	\N	\N	f	\N	\N	0
10070	71	0	\N	\N	f	\N	\N	0
10071	72	0	\N	\N	f	\N	\N	0
10072	73	0	\N	\N	f	\N	\N	0
10073	74	0	\N	\N	f	\N	\N	0
10074	75	0	\N	\N	f	\N	\N	0
10075	76	0	\N	\N	f	\N	\N	0
10076	77	0	\N	\N	f	\N	\N	0
\.


--
-- Data for Name: applicationversionstemp; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.applicationversionstemp (to_be_deleted, to_be_replaced, id, newapplicationid, newapplicationversionid, newurl, name, pkg, version, url, customerid, ismastercustomer, masterappexists, masterversionexists) FROM stdin;
\.


--
-- Data for Name: configurationapplicationparameters; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.configurationapplicationparameters (id, configurationid, applicationid, skipversioncheck) FROM stdin;
\.


--
-- Data for Name: configurationapplications; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.configurationapplications (id, configurationid, applicationid, remove, showicon, applicationversionid, action, screenorder, keycode, bottom) FROM stdin;
2	1	8	f	t	10007	1	\N	\N	f
3	1	37	f	f	10036	1	\N	\N	f
4	1	2	f	f	10001	1	\N	\N	f
5	1	10	f	t	10009	1	\N	\N	f
6	1	19	f	f	10018	1	\N	\N	f
7	1	20	f	f	10019	1	\N	\N	f
8	1	18	f	f	10017	1	\N	\N	f
9	1	21	f	t	10020	1	\N	\N	f
10	1	22	f	t	10021	1	\N	\N	f
11	1	23	f	t	10022	1	\N	\N	f
12	1	24	f	t	10023	1	\N	\N	f
13	1	26	f	t	10025	1	\N	\N	f
14	1	25	f	t	10024	1	\N	\N	f
15	1	9	f	t	10008	1	\N	\N	f
16	1	29	f	t	10028	1	\N	\N	f
17	1	30	f	f	10029	1	\N	\N	f
18	1	34	f	t	10033	1	\N	\N	f
19	1	36	f	f	10035	1	\N	\N	f
20	1	35	f	f	10034	1	\N	\N	f
21	1	43	f	f	10042	1	\N	\N	f
22	1	31	f	f	10030	1	\N	\N	f
23	1	13	f	f	10012	1	\N	\N	f
24	1	6	f	f	10005	1	\N	\N	f
25	1	14	f	f	10013	1	\N	\N	f
26	1	4	f	f	10003	1	\N	\N	f
27	1	27	f	f	10026	1	\N	\N	f
28	1	15	f	f	10014	1	\N	\N	f
29	1	3	f	f	10002	1	\N	\N	f
30	1	33	f	f	10032	1	\N	\N	f
31	1	12	f	f	10011	1	\N	\N	f
32	1	38	f	t	10037	1	\N	\N	f
33	1	39	f	t	10038	1	\N	\N	f
34	1	16	f	f	10015	1	\N	\N	f
35	1	5	f	f	10004	1	\N	\N	f
36	1	17	f	f	10016	1	\N	\N	f
37	1	42	f	t	10041	1	\N	\N	f
38	1	40	f	t	10039	1	\N	\N	f
39	1	41	f	t	10040	1	\N	\N	f
40	1	11	f	f	10010	1	\N	\N	f
41	1	28	f	f	10027	1	\N	\N	f
42	1	1	f	f	10000	1	\N	\N	f
43	1	46	f	f	10045	1	\N	\N	f
44	1	47	f	f	10046	1	\N	\N	f
45	1	48	f	t	10047	1	\N	\N	f
46	1	50	f	f	10049	1	\N	\N	f
48	2	8	f	t	10007	1	\N	\N	f
49	2	37	f	f	10036	1	\N	\N	f
50	2	2	f	f	10001	1	\N	\N	f
51	2	21	f	t	10020	1	\N	\N	f
52	2	9	f	t	10008	1	\N	\N	f
53	2	29	f	t	10028	1	\N	\N	f
54	2	34	f	t	10033	1	\N	\N	f
55	2	35	f	f	10034	1	\N	\N	f
56	2	13	f	f	10012	1	\N	\N	f
57	2	6	f	f	10005	1	\N	\N	f
58	2	4	f	f	10003	1	\N	\N	f
59	2	27	f	f	10026	1	\N	\N	f
60	2	15	f	f	10014	1	\N	\N	f
61	2	3	f	f	10002	1	\N	\N	f
62	2	12	f	f	10011	1	\N	\N	f
63	2	38	f	t	10037	1	\N	\N	f
64	2	39	f	t	10038	1	\N	\N	f
65	2	5	f	f	10004	1	\N	\N	f
66	2	17	f	f	10016	1	\N	\N	f
67	2	42	f	t	10041	1	\N	\N	f
68	2	40	f	t	10039	1	\N	\N	f
69	2	28	f	f	10027	1	\N	\N	f
70	2	1	f	f	10000	1	\N	\N	f
71	2	46	f	f	10045	1	\N	\N	f
72	2	48	f	t	10047	1	\N	\N	f
73	2	49	f	t	10048	1	\N	\N	f
74	2	50	f	f	10049	1	\N	\N	f
75	2	51	f	f	10050	1	\N	\N	f
76	2	52	f	f	10051	1	\N	\N	f
77	2	53	f	f	10052	1	\N	\N	f
78	2	54	f	f	10053	1	\N	\N	f
79	2	55	f	f	10054	1	\N	\N	f
80	2	56	f	f	10055	1	\N	\N	f
81	2	57	f	f	10056	1	\N	\N	f
82	1	59	f	f	10058	1	\N	\N	f
83	1	60	f	f	10059	1	\N	\N	f
84	1	61	f	f	10060	1	\N	\N	f
85	1	62	f	f	10061	1	\N	\N	f
86	1	63	f	t	10062	1	\N	\N	f
87	1	64	f	t	10063	1	\N	\N	f
88	1	66	f	t	10065	1	\N	\N	f
89	1	67	f	t	10066	1	\N	\N	f
90	1	68	f	t	10067	1	\N	\N	f
91	1	69	f	t	10068	1	\N	\N	f
92	1	71	f	f	10070	1	\N	\N	f
93	1	74	f	f	10073	1	\N	\N	f
94	1	75	f	f	10074	1	\N	\N	f
95	1	76	f	f	10075	1	\N	\N	f
96	1	77	f	f	10076	1	\N	\N	f
\.


--
-- Data for Name: configurationapplicationsettings; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.configurationapplicationsettings (id, applicationid, name, type, value, comment, readonly, extrefid, lastupdate) FROM stdin;
\.


--
-- Data for Name: configurationfiles; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.configurationfiles (id, configurationid, description, devicepath, externalurl, filepath, checksum, remove, lastupdate, fileid, replacevariables) FROM stdin;
\.


--
-- Data for Name: configurations; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.configurations (id, name, description, type, password, backgroundcolor, textcolor, backgroundimageurl, iconsize, desktopheader, usedefaultdesignsettings, customerid, gps, bluetooth, wifi, mobiledata, mainappid, eventreceivingcomponent, kioskmode, qrcodekey, contentappid, autoupdate, blockstatusbar, systemupdatetype, systemupdatefrom, systemupdateto, usbstorage, requestupdates, pushoptions, autobrightness, brightness, managetimeout, timeout, lockvolume, wifissid, wifipassword, wifisecuritytype, passwordmode, kioskhome, kioskrecents, kiosknotifications, kiosksysteminfo, kioskkeyguard, orientation, rundefaultlauncher, timezone, allowedclasses, newserverurl, locksafesettings, disablescreenshots, restrictions, defaultfilepath, keepalivetime, managevolume, volume, showwifi, mobileenrollment, desktopheadertemplate, kiosklockbuttons, scheduleappupdate, appupdatefrom, appupdateto, disablelocation, apppermissions) FROM stdin;
1	Common - Minimal	Suitable for generic Android devices; minimum of apps installed	0	12345678			\N	SMALL	NO_HEADER	t	1	\N	\N	\N	\N	10045	com.hmdm.launcher.AdminReceiver	f	6fb9c8dc81483173a0c0e9f8b2e46be1	\N	f	f	0	\N	\N	\N	DONOTTRACK	mqttAlarm	\N	180	f	60	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	/	300	\N	\N	\N	f	\N	\N	\N	\N	\N	f	GRANTALL
2	MIUI (Xiaomi Redmi)	Optimized for MIUI-running devices	0	12345678			\N	SMALL	NO_HEADER	t	1	\N	\N	\N	\N	10045	com.hmdm.launcher.AdminReceiver	f	8e6ca072ddb926a1af61578dfa9fc334	\N	f	f	0	\N	\N	\N	DONOTTRACK	mqttAlarm	\N	180	f	60	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	/	300	\N	\N	\N	f	\N	\N	\N	\N	\N	f	GRANTALL
\.


--
-- Data for Name: customers; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.customers (id, name, description, filesdir, master, prefix, registrationtime, lastlogintime, accounttype, expirytime, devicelimit, customerstatus, email) FROM stdin;
1	DEFAULT	Default customer account used for managing the already existing application data in SHARED usage scenario	DEFAULT	f	e1-	\N	\N	0	\N	3	\N	\N
2	ADMIN	Global customer account used for managing the application data for all customers in SHARED usage scenario	ADMIN	t	e2-	\N	\N	0	\N	3	\N	\N
\.


--
-- Data for Name: databasechangelog; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) FROM stdin;
notification-07.06.2019-11:13	isv	notification.changelog.xml	2022-12-13 22:19:43.201533	1	EXECUTED	8:8cbadde4bc297953a70b665948922b58	sql	Create pushMessages table	\N	3.6.3	common	\N	0950183182
notification-07.06.2019-11:45	isv	notification.changelog.xml	2022-12-13 22:19:43.211629	2	EXECUTED	8:51f79de3cf4cb6e0e7c39628007da6d5	sql	Create pendingPushes table	\N	3.6.3	common	\N	0950183182
plugin-audit-04.10.2019-16:38	isv	audit.changelog.xml	2022-12-13 22:19:43.757631	3	EXECUTED	8:442d1ca4547c484c76d1aff23e3d5d70	sql	Register audit plugin	\N	3.6.3	common	\N	0950183748
plugin-audit-04.10.2019-16:40	isv	audit.changelog.xml	2022-12-13 22:19:43.765604	4	EXECUTED	8:6c28eab71ee6b783584ba54c79578f6e	sql	Permissions for audit plugin access	\N	3.6.3	common	\N	0950183748
plugin-audit-04.10.2019-16:42	isv	audit.changelog.xml	2022-12-13 22:19:43.780027	5	EXECUTED	8:60bcd63dab63aa5550d1153f203a336f	sql	Table,new: plugin_audit_log	\N	3.6.3	common	\N	0950183748
plugin-audit-23.02.2020-16:42	seva	audit.changelog.xml	2022-12-13 22:19:43.783093	6	EXECUTED	8:3fe8a99bf3507b1d44f3a8ca88f250b4	sql	Column,new: errorCode	\N	3.6.3	common	\N	0950183748
plugin-audit-22.05.2020-12:47	seva	audit.changelog.xml	2022-12-13 22:19:43.789645	7	EXECUTED	8:bdbe28dcbeee7da6b6cfa83cb41d3d90	sql	Fix user role permissions assuming admin and super-admin have fixed ids	\N	3.6.3	common	\N	0950183748
plugin-contacts-16.06.2020-10:12	seva	contacts.changelog.xml	2022-12-13 22:19:44.017248	8	EXECUTED	8:9b09ecfc9d7bb4e2666dda9ae8b81490	sql	Plugin,new: contacts	\N	3.6.3	common	\N	0950184012
plugin-contacts-16.06.2020-10:13	seva	contacts.changelog.xml	2022-12-13 22:19:44.027322	9	EXECUTED	8:daecae987c075a025a2a937f9d385e74	sql	Permission,new: plugin_contacts_access	\N	3.6.3	common	\N	0950184012
plugin-deviceexport-12.09.2019-13:23	isv	deviceexport.changelog.xml	2022-12-13 22:19:44.218933	10	EXECUTED	8:4b85125813f8b2f24ca3ba56346229f7	sql	Register deviceexport plugin	\N	3.6.3	common	\N	0950184213
plugin-deviceexport-10.09.2019-16:22	isv	deviceexport.changelog.xml	2022-12-13 22:19:44.225969	11	EXECUTED	8:414804fce2c516a6fbd126064f2a8fe1	sql	Permissions for deviceexport plugin access	\N	3.6.3	common	\N	0950184213
plugin-deviceexport-22.05.2020-12:47	seva	deviceexport.changelog.xml	2022-12-13 22:19:44.232122	12	EXECUTED	8:b693c5fb80eaef75dc32014cea94f7ed	sql	Fix user role permissions assuming admin and super-admin have fixed ids	\N	3.6.3	common	\N	0950184213
plugin-deviceimport-10.09.2019-16:20	isv	deviceimport.changelog.xml	2022-12-13 22:19:44.434596	13	EXECUTED	8:3fd4db5f02378e6997fa7e5b25c6456b	sql	Register deviceimport plugin	\N	3.6.3	common	\N	0950184429
plugin-deviceimport-10.09.2019-16:22	isv	deviceimport.changelog.xml	2022-12-13 22:19:44.441631	14	EXECUTED	8:4275322d7456b4ae4b4b75ebb7144e8e	sql	Permissions for deviceimport plugin access	\N	3.6.3	common	\N	0950184429
plugin-deviceimport-22.05.2020-12:47	seva	deviceimport.changelog.xml	2022-12-13 22:19:44.447697	15	EXECUTED	8:592c8cfb06293db960f6c78abb481113	sql	Fix user role permissions assuming admin and super-admin have fixed ids	\N	3.6.3	common	\N	0950184429
plugin-deviceinfo-22.10.2019-13:34	isv	deviceinfo.changelog.xml	2022-12-13 22:19:44.647408	16	EXECUTED	8:c669eedc6acd20e78f4afa0d1410ddcf	sql	Plugin,new: deviceinfo	\N	3.6.3	common	\N	0950184642
plugin-deviceinfo-22.10.2019-13:36	isv	deviceinfo.changelog.xml	2022-12-13 22:19:44.654417	17	EXECUTED	8:09be67d6ee00774484fff372f3a1a2a7	sql	Permission,new: plugin_deviceinfo_access	\N	3.6.3	common	\N	0950184642
plugin-devicelocations-07.11.2019-12:36	isv	devicelocations.changelog.xml	2022-12-13 22:19:44.875559	18	EXECUTED	8:044462fd0b746987e8b55b07af8015e5	sql	Plugin,new: devicelocations	\N	3.6.3	common	\N	0950184871
plugin-devicelocations-07.11.2019-12:37	isv	devicelocations.changelog.xml	2022-12-13 22:19:44.8824	19	EXECUTED	8:d09dc37db1090fab3dd408624a926da6	sql	Permission,new: plugin_devicelocations_access	\N	3.6.3	common	\N	0950184871
plugin-devicelocations-07.11.2019-12:38	isv	devicelocations.changelog.xml	2022-12-13 22:19:44.885889	20	EXECUTED	8:4afba7aaa5248df257d19bc33d2cabd6	sql	Permission,new: plugin_devicelocations_access	\N	3.6.3	common	\N	0950184871
plugin-devicelocations-07.11.2019-12:40	isv	devicelocations.changelog.xml	2022-12-13 22:19:44.900816	21	EXECUTED	8:1bd89372a181ebbcfb23ca71379e72db	sql	Table,new: plugin_devicelocations_settings	\N	3.6.3	common	\N	0950184871
plugin-devicelocations-07.11.2019-15:39	isv	devicelocations.changelog.xml	2022-12-13 22:19:44.912398	22	EXECUTED	8:a8108e423f67bb6a9d45736a135ef9bc	sql	Table,new: plugin_devicelocations_latest	\N	3.6.3	common	\N	0950184871
plugin-devicelocations-07.11.2019-15:42	isv	devicelocations.changelog.xml	2022-12-13 22:19:44.925978	23	EXECUTED	8:ffca30907ea460eae3f5a242fa877a0e	sql	Table,new: plugin_devicelocations_history	\N	3.6.3	common	\N	0950184871
plugin-devicelocations-22.05.2020-12:47	seva	devicelocations.changelog.xml	2022-12-13 22:19:44.930216	24	EXECUTED	8:b4b1cf729428622c9e87cf2f488bfafa	sql	Fix user role permissions assuming admin and super-admin have fixed ids	\N	3.6.3	common	\N	0950184871
plugin-devicelocations-21.08.2022-13:58	seva	devicelocations.changelog.xml	2022-12-13 22:19:44.932498	25	EXECUTED	8:9195a237d9ceb9c8c1a5fe4545bf364e	sql	Added an option to process additional location updates	\N	3.6.3	common	\N	0950184871
plugin-devicelog-10.07.2019-14:45	isv	db.changelog.xml	2022-12-13 22:19:45.129815	26	EXECUTED	8:794673c30dfba1dc70df09c4823d27cd	sql	Register devicelog plugin	\N	3.6.3	common	\N	0950185126
plugin-devicelog-10.07.2019-15:01	isv	db.changelog.xml	2022-12-13 22:19:45.135649	27	EXECUTED	8:ff440606c26b760a842f5292986a3b6e	sql	Permissions for devicelog plugin access	\N	3.6.3	common	\N	0950185126
plugin-devicelog-11.09.19-18:38	isv	db.changelog.xml	2022-12-13 22:19:45.138786	28	EXECUTED	8:00813f6512bf16ba5240bf8269419df4	sql	Set: plugins.enabledForDevice	\N	3.6.3	common	\N	0950185126
plugin-devicelog-22.05.2020-12:47	seva	db.changelog.xml	2022-12-13 22:19:45.143634	29	EXECUTED	8:da8146058de1b7a448ad51014fa7fdc3	sql	Fix user role permissions assuming admin and super-admin have fixed ids	\N	3.6.3	common	\N	0950185126
plugin-devicelog-10.07.2019-17:34	isv	db.changelog.xml	2022-12-13 22:19:45.337013	30	EXECUTED	8:4d088c94af4f9fb7b4240f202e1173bf	sql	Create plugin_devicelog_log	\N	3.6.3	common	\N	0950185324
plugin-devicelog-12.07.2019-12:52	isv	db.changelog.xml	2022-12-13 22:19:45.343457	31	EXECUTED	8:f2c90eb1f6bb30221d65f533cca83397	sql	Create plugin_devicelog_settings	\N	3.6.3	common	\N	0950185324
plugin-devicelog-12.07.2019-12:55	isv	db.changelog.xml	2022-12-13 22:19:45.348745	32	EXECUTED	8:be1fa67a9f3e477f8910910feed4e95f	sql	Insert plugin_devicelog_settings	\N	3.6.3	common	\N	0950185324
plugin-devicereset-28.10.2019-13:42	isv	devicereset.changelog.xml	2022-12-13 22:19:45.57261	33	EXECUTED	8:85170047ff3cfbafbbd920932d239c0d	sql	Plugin,new: devicereset	\N	3.6.3	common	\N	0950185568
plugin-devicereset-28.10.2019-13:43	isv	devicereset.changelog.xml	2022-12-13 22:19:45.576897	34	EXECUTED	8:52f7c05450fd9c2f602f3289963a0f51	sql	Permission,new: plugin_devicereset_access	\N	3.6.3	common	\N	0950185568
plugin-devicereset-28.10.2019-13:44	isv	devicereset.changelog.xml	2022-12-13 22:19:45.587254	35	EXECUTED	8:a01801287fe0788e032be78e49441217	sql	Table,new: plugin_devicereset_status	\N	3.6.3	common	\N	0950185568
plugin-devicereset-19.03.2020-11:21	seva	devicereset.changelog.xml	2022-12-13 22:19:45.590545	36	EXECUTED	8:07913320b5f52c479748d12ff3156f22	sql	Add new options: reboot, lock	\N	3.6.3	common	\N	0950185568
plugin-devicereset-19.03.2020-13:34	seva	devicereset.changelog.xml	2022-12-13 22:19:45.595268	37	EXECUTED	8:e6bd890938e00bd66c9e8a97b27ec26a	sql	Add lock message	\N	3.6.3	common	\N	0950185568
plugin-devicereset-20.05.2020-19:51	seva	devicereset.changelog.xml	2022-12-13 22:19:45.598297	38	EXECUTED	8:5362c4fc5c290e258c5a1828062b777c	sql	Add lock message	\N	3.6.3	common	\N	0950185568
plugin-devicereset-22.05.2020-12:47	seva	devicereset.changelog.xml	2022-12-13 22:19:45.603922	39	EXECUTED	8:044ddc6bc63c88cdd2193e8353186c71	sql	Fix user role permissions assuming admin and super-admin have fixed ids	\N	3.6.3	common	\N	0950185568
plugin-knox-22.03.2021-19:44	seva	knox.changelog.xml	2022-12-13 22:19:45.818191	40	EXECUTED	8:40511ca8f5db0b24977ed3d3e3645236	sql	Plugin,new: knox	\N	3.6.3	common	\N	0950185793
plugin-messaging-28.12.2019-10:51	seva	messaging.changelog.xml	2022-12-13 22:19:46.13192	41	EXECUTED	8:1dcb0bc4da3f9ceb3fa66ab224dac9ad	sql	Plugin,new: messaging	\N	3.6.3	common	\N	0950186128
plugin-messaging-28.12.2019-10:52	seva	messaging.changelog.xml	2022-12-13 22:19:46.139409	42	EXECUTED	8:076fcd3afd7f3134985bc8371b0190d7	sql	Permission,new: plugin_messaging_send, plugin_messaging_delete	\N	3.6.3	common	\N	0950186128
plugin-messaging-28.12.2019-10:53	seva	messaging.changelog.xml	2022-12-13 22:19:46.151699	43	EXECUTED	8:647bc02c741d762d061c12d3e9145869	sql	Table,new: plugin_messaging_messages	\N	3.6.3	common	\N	0950186128
plugin-openvpn-12.05.2021-9:12	seva	openvpn.changelog.xml	2022-12-13 22:19:46.366942	44	EXECUTED	8:faeec4b8961ae924152302ae2203b7c5	sql	Plugin,new: openvpn	\N	3.6.3	common	\N	0950186342
plugin-photo-07.02.2019-10:00	isv	db.changelog.xml	2022-12-13 22:19:46.579678	45	EXECUTED	8:b0d2d09f018cede1a993e12affd05166	sql	Create photo_	\N	3.6.3	common	\N	0950186559
plugin-photo-07.02.2019-15:11	isv	db.changelog.xml	2022-12-13 22:19:46.584346	46	EXECUTED	8:52896a9e9c9505e5e28c54f4263239af	sql	Register photo plugin	\N	3.6.3	common	\N	0950186559
plugin-photo-08.02.2019-16:06	isv	db.changelog.xml	2022-12-13 22:19:46.593277	47	EXECUTED	8:3ecc1fdce72cd6f8dfc90b2cb6bf77ec	sql	Settings table	\N	3.6.3	common	\N	0950186559
plugin-photo-12.02.2019-15:22	isv	db.changelog.xml	2022-12-13 22:19:46.597089	48	EXECUTED	8:ae2b3726767fcab2a3258aa74575d76a	sql	Settings table	\N	3.6.3	common	\N	0950186559
plugin-photo-13.02.2019-12:25	isv	db.changelog.xml	2022-12-13 22:19:46.598801	49	EXECUTED	8:15d1c39fe7be6c4eae46bd855396a37c	sql	Settings sendPhoto	\N	3.6.3	common	\N	0950186559
plugin-photo-26.02.2019-17:17	isv	db.changelog.xml	2022-12-13 22:19:46.601297	50	EXECUTED	8:5e0bd003fa844f28f980bbec42cc17a6	sql	Settings imagePaths, imageDeletionDelay	\N	3.6.3	common	\N	0950186559
plugin-photo-29.03.2019-16:02	isv	db.changelog.xml	2022-12-13 22:19:46.602928	51	EXECUTED	8:65d8028495d5ed3253e42bc7c4331a82	sql	Photo address	\N	3.6.3	common	\N	0950186559
plugin-photo-30.05.2019-12:29	isv	db.changelog.xml	2022-12-13 22:19:46.605719	52	EXECUTED	8:9f9d935aab3ee0f851c4e39ae00b8fad	sql	Set the localization key for plugin name	\N	3.6.3	common	\N	0950186559
plugin-photo-08.06.2019-17:17	isv	db.changelog.xml	2022-12-13 22:19:46.609126	53	EXECUTED	8:7ed9b4c2f542f0794c73e1d200954d6b	sql	Settings addText, backgroundColor, textColor, transparency, textContent	\N	3.6.3	common	\N	0950186559
plugin-photo-27.06.19-13:36	isv	db.changelog.xml	2022-12-13 22:19:46.613007	54	EXECUTED	8:b4a90d0eb51ee33ed05b83df1f5e4218	sql	Adding index on deviceId, createTime to plugin_photo_photo table	\N	3.6.3	common	\N	0950186559
plugin-photo-11.09.19-18:38	isv	db.changelog.xml	2022-12-13 22:19:46.614755	55	EXECUTED	8:f6977250fbcac22dd92f0795346f2360	sql	Set: plugins.enabledForDevice	\N	3.6.3	common	\N	0950186559
plugin-photo-08.10.19-13:55	isv	db.changelog.xml	2022-12-13 22:19:46.621824	56	EXECUTED	8:2f7914236fbadf6518e568cf308b1ccf	sql	Table, new: plugin_photo_places	\N	3.6.3	common	\N	0950186559
plugin-photo-08.10.2019-18:30	isv	db.changelog.xml	2022-12-13 22:19:46.624196	57	EXECUTED	8:125a36ec3fe80af025395cee11e58e33	sql	Columns,new: plugin_photo_settings#linkPhotoToPlace,searchPlaceRadius	\N	3.6.3	common	\N	0950186559
plugin-photo-09.10.2019-13:46	isv	db.changelog.xml	2022-12-13 22:19:46.627334	58	EXECUTED	8:21e60cc1e175fdcf7a15ea4fb1e73a25	sql	Index,new: plugin_photo_places#customerId,LOWER(placeId)	\N	3.6.3	common	\N	0950186559
plugin-photo-09.10.19-14:59	isv	db.changelog.xml	2022-12-13 22:19:46.635325	59	EXECUTED	8:33fd2c06ffbf1c0a1d8ca30f2d01057c	sql	Table, new: plugin_photo_photo_places	\N	3.6.3	common	\N	0950186559
plugin-photo-21.11.2019-13:12	isv	db.changelog.xml	2022-12-13 22:19:46.637764	60	EXECUTED	8:6bcae5a6e81a24226d7cdbe8451eb5f9	sql	Columns,new: plugin_photo_settings#nonTransmittedPaths,includeStandardImagePaths	\N	3.6.3	common	\N	0950186559
plugin-photo-22.05.2020-12:47	seva	db.changelog.xml	2022-12-13 22:19:46.641597	61	EXECUTED	8:149cd69fb2ede6c6e40fb6c6da5480ab	sql	Fix user role permissions assuming admin and super-admin have fixed ids	\N	3.6.3	common	\N	0950186559
plugin-photo-03.02.2021-11:18	seva	db.changelog.xml	2022-12-13 22:19:46.643699	62	EXECUTED	8:694e771c8005b5729ff2090fca861516	sql	Columns,new: plugin_photo_settings#fileTypes,directory,purgeDays	\N	3.6.3	common	\N	0950186559
plugin-photo-19.05.2021-10:55	seva	db.changelog.xml	2022-12-13 22:19:46.646021	63	EXECUTED	8:6a47722e20df3efde0673c2992ce88bc	sql	Add permission to view photos	\N	3.6.3	common	\N	0950186559
plugin-photo-19.11.2021-11:36	seva	db.changelog.xml	2022-12-13 22:19:46.647476	64	EXECUTED	8:a87558f6f379ac00ba2e61480187444d	sql	Columns,new: plugin_photo_settings#nameTemplate	\N	3.6.3	common	\N	0950186559
plugin-push-14.05.2022-15:11	seva	push.changelog.xml	2022-12-13 22:19:46.855922	65	EXECUTED	8:0aad3ebf3b31134c1bbfd4ead914b97d	sql	Plugin,new: push	\N	3.6.3	common	\N	0950186835
plugin-platform-07.02.2019-14:16	isv	db.changelog.xml	2022-12-13 22:23:47.718946	66	EXECUTED	8:c3f88a687f75d364044a49485c16e678	sql	Create plugins table	\N	3.6.3	common	\N	0950427692
plugin-platform-30.05.2019-12:09	isv	db.changelog.xml	2022-12-13 22:23:47.72529	67	EXECUTED	8:2718138941250625c84cf6b027d98984	sql	Add nameLocalizationKey to plugins	\N	3.6.3	common	\N	0950427692
plugin-platform-15.07.2019-01:48	isv	db.changelog.xml	2022-12-13 22:23:47.728964	68	EXECUTED	8:37a81c80230483adcaab91a28106f1ec	sql	Add settingsPermission, functionsPermission, deviceFunctionsPermission to plugins	\N	3.6.3	common	\N	0950427692
plugin-platform-11.09.2019-18:36	isv	db.changelog.xml	2022-12-13 22:23:47.732268	69	EXECUTED	8:63713b73183bfbff1e8ad11fe2e6e916	sql	Column,new: plugins.enabledForDevice	\N	3.6.3	common	\N	0950427692
plugin-platform-29.10.2019-17:55	isv	db.changelog.xml	2022-12-13 22:23:47.738643	70	EXECUTED	8:413f2867f08080aa11406970fead8bbc	sql	Permission,new: plugins_customer_access_management	\N	3.6.3	common	\N	0950427692
plugin-platform-16.06.2020-16:08	seva	db.changelog.xml	2022-12-13 22:23:47.741457	71	EXECUTED	8:66a0f0aa054adf9842b9ebbfb89daf43	sql	Update name column type	\N	3.6.3	common	\N	0950427692
plugin-platform-05.05.21-11:48	seva	db.changelog.xml	2022-12-13 22:23:47.749612	72	EXECUTED	8:63fedb2df95d22428d16635ac19fbf1e	sql	Drop legacy licensing data	\N	3.6.3	common	\N	0950427692
plugin-devicelog-12.07.2019-13:32	isv	db.changelog.xml	2022-12-13 22:23:49.372857	73	EXECUTED	8:303abb0fd1c09e1dff7eecc64821d6c5	sql	Create plugin_devicelog_setting_rules	\N	3.6.3	common	\N	0950429351
plugin-devicelog-12.07.2019-13:50	isv	db.changelog.xml	2022-12-13 22:23:49.380024	74	EXECUTED	8:ad1bc2cb97600b3af16b46269a9d816f	sql	Create plugin_devicelog_setting_rule_devices	\N	3.6.3	common	\N	0950429351
plugin-deviceinfo-22.10.2019-13:38	isv	deviceinfo.changelog.xml	2022-12-14 15:40:44.466786	75	EXECUTED	8:22775afa3e1213510a9a494ca77c9117	sql	Table,new: plugin_deviceinfo_settings	\N	3.6.3	common	\N	1012644431
plugin-deviceinfo-22.10.2019-15:28	isv	deviceinfo.changelog.xml	2022-12-14 15:40:44.481002	76	EXECUTED	8:3ebce08f26d424050ba49f741ab79e3a	sql	Table,new: plugin_deviceinfo_deviceParams	\N	3.6.3	common	\N	1012644431
plugin-deviceinfo-22.10.2019-15:31	isv	deviceinfo.changelog.xml	2022-12-14 15:40:44.492736	77	EXECUTED	8:76d10eae565213626c5db4d3e74632d1	sql	Table,new: plugin_deviceinfo_deviceParams_device	\N	3.6.3	common	\N	1012644431
plugin-deviceinfo-22.10.2019-16:02	isv	deviceinfo.changelog.xml	2022-12-14 15:40:44.504344	78	EXECUTED	8:749d6ebe8b2748334c99334af4371534	sql	Table,new: plugin_deviceinfo_deviceParams_wifi	\N	3.6.3	common	\N	1012644431
plugin-deviceinfo-22.10.2019-16:29	isv	deviceinfo.changelog.xml	2022-12-14 15:40:44.513588	79	EXECUTED	8:8cf960deac394e5b29229d892bc7929f	sql	Table,new: plugin_deviceinfo_deviceParams_gps	\N	3.6.3	common	\N	1012644431
plugin-deviceinfo-22.10.2019-16:48	isv	deviceinfo.changelog.xml	2022-12-14 15:40:44.522741	80	EXECUTED	8:a544a20082d13e1e992ad83fe8434e7e	sql	Table,new: plugin_deviceinfo_deviceParams_mobile	\N	3.6.3	common	\N	1012644431
plugin-deviceinfo-22.10.2019-16:49	isv	deviceinfo.changelog.xml	2022-12-14 15:40:44.533135	81	EXECUTED	8:ca20385273858e28fa7ec33dd1c7ecaa	sql	Table,new: plugin_deviceinfo_deviceParams_mobile2	\N	3.6.3	common	\N	1012644431
plugin-deviceinfo-29.10.2019-14:03	isv	deviceinfo.changelog.xml	2022-12-14 15:40:44.539198	82	EXECUTED	8:e711195832c54179b4d1518bebdddc57	sql	Columns,new: plugin_deviceinfo_settings#sendData,intervalMins	\N	3.6.3	common	\N	1012644431
plugin-deviceinfo-26.01.2021-13:23	seva	deviceinfo.changelog.xml	2022-12-14 15:40:44.546264	83	EXECUTED	8:1de02d3197b9b6300a5bd7aa56e21ce2	sql	Columns,new: plugin_deviceinfo_deviceparams_device#usbStorage,memoryTotal,memoryAvailable	\N	3.6.3	common	\N	1012644431
\.


--
-- Data for Name: databasechangeloglock; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.databasechangeloglock (id, locked, lockgranted, lockedby) FROM stdin;
1	f	\N	\N
\.


--
-- Data for Name: deviceapplicationsettings; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.deviceapplicationsettings (id, applicationid, name, type, value, comment, readonly, extrefid, lastupdate) FROM stdin;
\.


--
-- Data for Name: devicegroups; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.devicegroups (id, deviceid, groupid) FROM stdin;
\.


--
-- Data for Name: devices; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.devices (id, number, description, lastupdate, configurationid, oldconfigurationid, info, imei, phone, customerid, imeiupdatets, custom1, custom2, custom3, oldnumber, fastsearch, enrolltime) FROM stdin;
1	h0001	My first Android device	0	1	\N	\N	\N	\N	1	\N	\N	\N	\N	\N	h0001	\N
\.


--
-- Data for Name: devicestatuses; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.devicestatuses (deviceid, configfilesstatus, applicationsstatus) FROM stdin;
1	OTHER	FAILURE
\.


--
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.groups (id, name, customerid) FROM stdin;
\.


--
-- Data for Name: icons; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.icons (id, customerid, name, fileid) FROM stdin;
\.


--
-- Data for Name: pendingpushes; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.pendingpushes (id, messageid, status, createtime, sendtime) FROM stdin;
\.


--
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.permissions (id, name, description, superadmin) FROM stdin;
101	edit_device_app_settings	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╤Ç╨╡╨┤╨░╨║╤é╨╕╤Ç╨╛╨▓╨░╨╜╨╕╤Ä ╨╕ ╨┤╨╛╨▒╨░╨▓╨╗╨╡╨╜╨╕╤Ä ╨╜╨░╤ü╤é╤Ç╨╛╨╡╨║ ╨┐╤Ç╨╕╨╗╨╛╨╢╨╡╨╜╨╕╤Å ╨┤╨╗╤Å ╤â╤ü╤é╤Ç╨╛╨╣╤ü╤é╨▓╨░	f
5	add_config	Add new empty configurations	f
6	copy_config	Duplicate/copy configurations	f
102	push_api	Send Push messages to devices via REST API	f
1	superadmin	Super-administrator functions for the whole system	t
2	settings	Access to system settings	f
3	configurations	Access to configurations, applications and files	f
4	edit_devices	Access to devices	f
100	edit_device_desc	Access to image removal (image plugin)	f
103	plugin_audit_access	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╨░╤â╨┤╨╕╤é╤â ╨┤╨╡╨╣╤ü╤é╨▓╨╕╨╣ ╨┐╨╛╨╗╤î╨╖╨╛╨▓╨░╤é╨╡╨╗╨╡╨╣	f
104	plugin_contacts_access	Access to contact management	f
105	plugin_deviceexport_access	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╤ì╨║╤ü╨┐╨╛╤Ç╤é╨╕╤Ç╨╛╨▓╨░╨╜╨╕╤Ä ╤â╤ü╤é╤Ç╨╛╨╣╤ü╤é╨▓ ╨▓ Excel-╤ä╨░╨╣╨╗	f
106	plugin_deviceimport_access	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╨╕╨╝╨┐╨╛╤Ç╤é╨╕╤Ç╨╛╨▓╨░╨╜╨╕╤Ä ╤â╤ü╤é╤Ç╨╛╨╣╤ü╤é╨▓ ╨╕╨╖ Excel-╤ä╨░╨╣╨╗╨░ (XLS ╨╕ XLSX)	f
107	plugin_deviceinfo_access	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╨┤╨╡╤é╨░╨╗╤î╨╜╨╛╨╣ ╨╕ ╨┤╨╕╨╜╨░╨╝╨╕╤ç╨╡╤ü╨║╨╛╨╣ ╨╕╨╜╤ä╨╛╤Ç╨╝╨░╤å╨╕╨╕ ╨╛╨▒ ╤â╤ü╤é╤Ç╨╛╨╣╤ü╤é╨▓╨░╤à	f
108	plugin_devicelocations_access	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╨╛╤é╨╛╨▒╤Ç╨░╨╢╨╡╨╜╨╕╤Ä ╤â╤ü╤é╤Ç╨╛╨╣╤ü╤é╨▓ ╨╜╨░ ╨║╨░╤Ç╤é╨╡	f
109	plugin_devicelocations_settings_access	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╨╜╨░╤ü╤é╤Ç╨╛╨╣╨║╨░╨╝ ╨┐╨╗╨░╨│╨╕╨╜╨░ "╨ú╤ü╤é╤Ç╨╛╨╣╤ü╤é╨▓╨░ ╨╜╨░ ╨║╨░╤Ç╤é╨╡"	f
110	plugin_devicelog_access	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╨╢╤â╤Ç╨╜╨░╨╗╨░╨╝ ╤â╤ü╤é╤Ç╨╛╨╣╤ü╤é╨▓	f
111	plugin_devicereset_access	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╤ü╨▒╤Ç╨╛╤ü╤â ╨╜╨░╤ü╤é╤Ç╨╛╨╡╨║ ╤â╤ü╤é╤Ç╨╛╨╣╤ü╤é╨▓ ╨┤╨╛ ╨╖╨░╨▓╨╛╨┤╤ü╨║╨╕╤à	f
112	plugin_knox_access	Can setup Knox features	f
113	plugin_messaging_send	Can send messages to devices	f
114	plugin_messaging_delete	Can delete and update message history	f
115	plugin_openvpn_access	Can configure OpenVPN	f
116	plugin_photo_remove_photo	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╤â╨┤╨░╨╗╨╡╨╜╨╕╤Ä ╤ä╨╛╤é╨╛╨│╤Ç╨░╤ä╨╕╨╣	f
117	plugin_photo_access	Access to photo plugin	f
118	plugin_push_send	Can send push messages to devices	f
119	plugin_push_delete	Can delete and update Push message history	f
120	plugins_customer_access_management	╨ÿ╨╝╨╡╨╡╤é ╨┤╨╛╤ü╤é╤â╨┐ ╨║ ╤â╨┐╤Ç╨░╨▓╨╗╨╡╨╜╨╕╤Ä ╤ü╨┐╨╕╤ü╨║╨╛╨╝ ╨╕╤ü╨┐╨╛╨╗╤î╨╖╤â╨╡╨╝╤ï╤à ╨┐╨╗╨░╨│╨╕╨╜╨╛╨▓ ╨╜╨░ ╤â╤Ç╨╛╨▓╨╜╨╡ ╤â╤ç╨╡╤é╨╜╨╛╨╣ ╨╖╨░╨┐╨╕╤ü╨╕ ╤ü╨▓╨╛╨╡╨╣ ╨╛╤Ç╨│╨░╨╜╨╕╨╖╨░╤å╨╕╨╕	f
\.


--
-- Data for Name: plugin_audit_log; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_audit_log (id, createtime, customerid, userid, login, action, payload, ipaddress, errorcode) FROM stdin;
\.


--
-- Data for Name: plugin_deviceinfo_deviceparams; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_deviceinfo_deviceparams (id, deviceid, customerid, ts) FROM stdin;
\.


--
-- Data for Name: plugin_deviceinfo_deviceparams_device; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_deviceinfo_deviceparams_device (id, recordid, batterylevel, batterycharging, ip, keyguard, ringvolume, wifi, mobiledata, gps, bluetooth, usbstorage, memorytotal, memoryavailable) FROM stdin;
\.


--
-- Data for Name: plugin_deviceinfo_deviceparams_gps; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_deviceinfo_deviceparams_gps (id, recordid, state, lat, lon, alt, speed, course) FROM stdin;
\.


--
-- Data for Name: plugin_deviceinfo_deviceparams_mobile; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_deviceinfo_deviceparams_mobile (id, recordid, rssi, carrier, data, ip, state, simstate, tx, rx) FROM stdin;
\.


--
-- Data for Name: plugin_deviceinfo_deviceparams_mobile2; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_deviceinfo_deviceparams_mobile2 (id, recordid, rssi, carrier, data, ip, state, simstate, tx, rx) FROM stdin;
\.


--
-- Data for Name: plugin_deviceinfo_deviceparams_wifi; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_deviceinfo_deviceparams_wifi (id, recordid, rssi, ssid, security, state, ip, tx, rx) FROM stdin;
\.


--
-- Data for Name: plugin_deviceinfo_settings; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_deviceinfo_settings (id, customerid, datapreserveperiod, senddata, intervalmins) FROM stdin;
\.


--
-- Data for Name: plugin_devicelocations_history; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_devicelocations_history (id, deviceid, ts, lat, lon) FROM stdin;
\.


--
-- Data for Name: plugin_devicelocations_latest; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_devicelocations_latest (id, deviceid, ts, lat, lon) FROM stdin;
\.


--
-- Data for Name: plugin_devicelocations_settings; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_devicelocations_settings (id, customerid, datapreserveperiod, tileserverurl, updatetime) FROM stdin;
\.


--
-- Data for Name: plugin_devicelog_log; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_devicelog_log (id, createtime, customerid, deviceid, applicationid, ipaddress, severity, severityorder, message) FROM stdin;
\.


--
-- Data for Name: plugin_devicelog_setting_rule_devices; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_devicelog_setting_rule_devices (ruleid, deviceid) FROM stdin;
\.


--
-- Data for Name: plugin_devicelog_settings; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_devicelog_settings (id, customerid, logspreserveperiod) FROM stdin;
1	1	30
2	2	30
\.


--
-- Data for Name: plugin_devicelog_settings_rules; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_devicelog_settings_rules (id, settingid, name, active, applicationid, severity, filter, groupid, configurationid) FROM stdin;
\.


--
-- Data for Name: plugin_devicereset_status; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_devicereset_status (id, deviceid, statusresetrequested, statusresetconfirmed, rebootrequested, rebootconfirmed, devicelocked, lockmessage, password) FROM stdin;
\.


--
-- Data for Name: plugin_knox_rules; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_knox_rules (id, configurationid, rule, tabletype, ruletype) FROM stdin;
\.


--
-- Data for Name: plugin_messaging_messages; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_messaging_messages (id, customerid, deviceid, ts, message, status) FROM stdin;
\.


--
-- Data for Name: plugin_openvpn_defaults; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_openvpn_defaults (id, customerid, removevpns, removeall, vpnname, vpnconfig, vpnurl, connect, alwayson) FROM stdin;
\.


--
-- Data for Name: plugin_photo_photo; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_photo_photo (id, createtime, lat, lng, path, deviceid, customerid, thumbnailimagepath, contenttype, address) FROM stdin;
\.


--
-- Data for Name: plugin_photo_photo_places; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_photo_photo_places (id, photoid, pointid, pointaddress) FROM stdin;
\.


--
-- Data for Name: plugin_photo_places; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_photo_places (id, customerid, placeid, lat, lng, address, reserve) FROM stdin;
\.


--
-- Data for Name: plugin_photo_settings; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_photo_settings (id, customerid, tracklocation, trackingoffwarning, sendphoto, imagepaths, imagedeletiondelay, addtext, backgroundcolor, textcolor, transparency, textcontent, linkphototoplace, searchplaceradius, nontransmittedpaths, includestandardimagepaths, filetypes, directory, purgedays, nametemplate) FROM stdin;
\.


--
-- Data for Name: plugin_push_messages; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugin_push_messages (id, customerid, deviceid, ts, messagetype, payload) FROM stdin;
\.


--
-- Data for Name: plugins; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.plugins (id, identifier, name, description, createtime, disabled, javascriptmodulefile, functionsviewtemplate, settingsviewtemplate, namelocalizationkey, settingspermission, functionspermission, devicefunctionspermission, enabledfordevice) FROM stdin;
\.


--
-- Data for Name: pluginsdisabled; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.pluginsdisabled (pluginid, customerid) FROM stdin;
\.


--
-- Data for Name: pushmessages; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.pushmessages (id, messagetype, deviceid, payload) FROM stdin;
\.


--
-- Data for Name: settings; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.settings (id, backgroundcolor, textcolor, backgroundimageurl, iconsize, desktopheader, customerid, usedefaultlanguage, language, createnewdevices, newdevicegroupid, newdeviceconfigurationid, phonenumberformat, custompropertyname1, custompropertyname2, custompropertyname3, custommultiline1, custommultiline2, custommultiline3, customsend1, customsend2, customsend3, desktopheadertemplate, senddescription, passwordreset, passwordlength, passwordstrength) FROM stdin;
1	#1c40e3	#fcfcfc	\N	SMALL	NO_HEADER	1	t	\N	f	\N	\N	+9 (999) 999-99-99	\N	\N	\N	f	f	f	f	f	f	\N	f	f	0	0
\.


--
-- Data for Name: trialkey; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.trialkey (id, keycode, created) FROM stdin;
\.


--
-- Data for Name: uploadedfiles; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.uploadedfiles (id, customerid, filepath, uploadtime) FROM stdin;
\.


--
-- Data for Name: userconfigurationaccess; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.userconfigurationaccess (id, userid, configurationid) FROM stdin;
\.


--
-- Data for Name: userdevicegroupsaccess; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.userdevicegroupsaccess (id, userid, groupid) FROM stdin;
\.


--
-- Data for Name: userhints; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.userhints (id, userid, hintkey, created) FROM stdin;
\.


--
-- Data for Name: userhinttypes; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.userhinttypes (hintkey) FROM stdin;
hint.step.1
hint.step.2
hint.step.3
hint.step.4
\.


--
-- Data for Name: userrolepermissions; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.userrolepermissions (roleid, permissionid) FROM stdin;
1	1
2	2
2	3
2	4
3	3
3	4
1	100
2	100
3	100
100	100
1	101
2	101
1	5
2	5
1	6
2	6
3	6
1	102
2	102
3	102
1	103
2	103
1	104
2	104
3	104
1	105
2	105
1	106
2	106
1	107
2	107
3	107
100	107
1	108
2	108
3	108
100	108
1	109
2	109
1	110
2	110
1	111
2	111
1	112
2	112
1	113
2	113
3	113
100	113
1	114
2	114
1	115
2	115
1	116
2	116
1	117
2	117
3	117
100	117
1	118
2	118
3	118
100	118
1	119
2	119
\.


--
-- Data for Name: userroles; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.userroles (id, name, description, superadmin) FROM stdin;
1	Super-Admin	Can sign in as any user. In shared mode, manages corporate accounts	t
2	Admin	Full access to the control panel	f
3	User	Limited access to the control panel	f
100	Observer	Read-only access to the control panel	f
\.


--
-- Data for Name: userrolesettings; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.userrolesettings (id, roleid, customerid, columndisplayeddevicestatus, columndisplayeddevicedate, columndisplayeddevicenumber, columndisplayeddevicemodel, columndisplayeddevicepermissionsstatus, columndisplayeddeviceappinstallstatus, columndisplayeddeviceconfiguration, columndisplayeddeviceimei, columndisplayeddevicephone, columndisplayeddevicedesc, columndisplayeddevicegroup, columndisplayedlauncherversion, columndisplayedbatterylevel, columndisplayeddevicefilesstatus, columndisplayeddefaultlauncher, columndisplayedcustom1, columndisplayedcustom2, columndisplayedcustom3, columndisplayedmdmmode, columndisplayedkioskmode, columndisplayedandroidversion, columndisplayedenrollmentdate, columndisplayedserial) FROM stdin;
1	1	1	t	t	t	\N	t	t	t	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
2	2	1	t	t	t	\N	t	t	t	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
3	3	1	t	t	t	\N	t	t	t	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
4	100	1	t	t	t	\N	t	t	t	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: hmdm
--

COPY public.users (id, login, email, name, password, customerid, userroleid, alldevicesavailable, allconfigavailable, passwordreset, authtoken, passwordresettoken) FROM stdin;
\.


--
-- Name: applications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.applications_id_seq', 77, true);


--
-- Name: applicationversions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.applicationversions_id_seq', 10076, true);


--
-- Name: configurationapplicationparameters_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.configurationapplicationparameters_id_seq', 1, false);


--
-- Name: configurationapplications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.configurationapplications_id_seq', 96, true);


--
-- Name: configurationapplicationsettings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.configurationapplicationsettings_id_seq', 1, false);


--
-- Name: configurationfiles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.configurationfiles_id_seq', 1, false);


--
-- Name: configurations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.configurations_id_seq', 2, true);


--
-- Name: customers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.customers_id_seq', 100, false);


--
-- Name: deviceapplicationsettings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.deviceapplicationsettings_id_seq', 1, false);


--
-- Name: devicegroups_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.devicegroups_id_seq', 1, false);


--
-- Name: devices_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.devices_id_seq', 1, true);


--
-- Name: groups_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.groups_id_seq', 1, false);


--
-- Name: icons_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.icons_id_seq', 1, false);


--
-- Name: pendingpushes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.pendingpushes_id_seq', 1, false);


--
-- Name: permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.permissions_id_seq', 120, true);


--
-- Name: plugin_audit_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_audit_log_id_seq', 1, false);


--
-- Name: plugin_deviceinfo_deviceparams_device_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_deviceinfo_deviceparams_device_id_seq', 1, false);


--
-- Name: plugin_deviceinfo_deviceparams_gps_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_deviceinfo_deviceparams_gps_id_seq', 1, false);


--
-- Name: plugin_deviceinfo_deviceparams_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_deviceinfo_deviceparams_id_seq', 1, false);


--
-- Name: plugin_deviceinfo_deviceparams_mobile2_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_deviceinfo_deviceparams_mobile2_id_seq', 1, false);


--
-- Name: plugin_deviceinfo_deviceparams_mobile_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_deviceinfo_deviceparams_mobile_id_seq', 1, false);


--
-- Name: plugin_deviceinfo_deviceparams_wifi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_deviceinfo_deviceparams_wifi_id_seq', 1, false);


--
-- Name: plugin_deviceinfo_settings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_deviceinfo_settings_id_seq', 1, false);


--
-- Name: plugin_devicelocations_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_devicelocations_history_id_seq', 1, false);


--
-- Name: plugin_devicelocations_latest_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_devicelocations_latest_id_seq', 1, false);


--
-- Name: plugin_devicelocations_settings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_devicelocations_settings_id_seq', 1, false);


--
-- Name: plugin_devicelog_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_devicelog_log_id_seq', 1, false);


--
-- Name: plugin_devicelog_settings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_devicelog_settings_id_seq', 2, true);


--
-- Name: plugin_devicelog_settings_rules_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_devicelog_settings_rules_id_seq1', 1, false);


--
-- Name: plugin_devicereset_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_devicereset_status_id_seq', 1, false);


--
-- Name: plugin_knox_rules_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_knox_rules_id_seq', 1, false);


--
-- Name: plugin_messaging_messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_messaging_messages_id_seq', 1, false);


--
-- Name: plugin_openvpn_defaults_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_openvpn_defaults_id_seq', 1, false);


--
-- Name: plugin_photo_photo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_photo_photo_id_seq', 1, false);


--
-- Name: plugin_photo_photo_places_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_photo_photo_places_id_seq', 1, false);


--
-- Name: plugin_photo_places_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_photo_places_id_seq', 1, false);


--
-- Name: plugin_photo_settings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_photo_settings_id_seq', 1, false);


--
-- Name: plugin_push_messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugin_push_messages_id_seq', 1, false);


--
-- Name: plugins_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.plugins_id_seq', 1, false);


--
-- Name: pushmessages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.pushmessages_id_seq', 1, false);


--
-- Name: settings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.settings_id_seq', 1, true);


--
-- Name: trialkey_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.trialkey_id_seq', 1, false);


--
-- Name: uploadedfiles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.uploadedfiles_id_seq', 1, false);


--
-- Name: userconfigurationaccess_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.userconfigurationaccess_id_seq', 1, false);


--
-- Name: userdevicegroupsaccess_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.userdevicegroupsaccess_id_seq', 1, false);


--
-- Name: userhints_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.userhints_id_seq', 1, false);


--
-- Name: userroles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.userroles_id_seq', 100, true);


--
-- Name: userrolesettings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.userrolesettings_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hmdm
--

SELECT pg_catalog.setval('public.users_id_seq', 1, false);


--
-- Name: applications applications_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_pr_key PRIMARY KEY (id);


--
-- Name: applicationversions applicationversions_app_version_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.applicationversions
    ADD CONSTRAINT applicationversions_app_version_key UNIQUE (applicationid, version);


--
-- Name: applicationversions applicationversions_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.applicationversions
    ADD CONSTRAINT applicationversions_pr_key PRIMARY KEY (id);


--
-- Name: configurationapplicationparameters cap_config_application_unique; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplicationparameters
    ADD CONSTRAINT cap_config_application_unique UNIQUE (configurationid, applicationid);


--
-- Name: configurationapplicationparameters configuration_application_parameters_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplicationparameters
    ADD CONSTRAINT configuration_application_parameters_pr_key PRIMARY KEY (id);


--
-- Name: configurationapplications configuration_applications_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplications
    ADD CONSTRAINT configuration_applications_pr_key PRIMARY KEY (id);


--
-- Name: configurationapplicationsettings configurationapplicationsettings_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplicationsettings
    ADD CONSTRAINT configurationapplicationsettings_pr_key PRIMARY KEY (id);


--
-- Name: configurationfiles configurationfiles_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationfiles
    ADD CONSTRAINT configurationfiles_pr_key PRIMARY KEY (id);


--
-- Name: configurations configurations_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurations
    ADD CONSTRAINT configurations_pr_key PRIMARY KEY (id);


--
-- Name: customers customer_filesdir_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customer_filesdir_key UNIQUE (filesdir);


--
-- Name: customers customer_name_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customer_name_key UNIQUE (name);


--
-- Name: customers customers_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_pr_key PRIMARY KEY (id);


--
-- Name: customers customers_prefix_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_prefix_key UNIQUE (prefix);


--
-- Name: databasechangeloglock databasechangeloglock_pkey; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.databasechangeloglock
    ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id);


--
-- Name: deviceapplicationsettings deviceapplicationsettings_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.deviceapplicationsettings
    ADD CONSTRAINT deviceapplicationsettings_pr_key PRIMARY KEY (id);


--
-- Name: devicegroups devicegroups_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devicegroups
    ADD CONSTRAINT devicegroups_pr_key PRIMARY KEY (id);


--
-- Name: devices devices_number_unique; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devices
    ADD CONSTRAINT devices_number_unique UNIQUE (number);


--
-- Name: devices devices_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devices
    ADD CONSTRAINT devices_pr_key PRIMARY KEY (id);


--
-- Name: devicestatuses devicestatuses_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devicestatuses
    ADD CONSTRAINT devicestatuses_pr_key PRIMARY KEY (deviceid);


--
-- Name: groups groups_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_pr_key PRIMARY KEY (id);


--
-- Name: icons icons_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.icons
    ADD CONSTRAINT icons_pr_key PRIMARY KEY (id);


--
-- Name: users login_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT login_key UNIQUE (login);


--
-- Name: pendingpushes pending_push_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.pendingpushes
    ADD CONSTRAINT pending_push_pr_key PRIMARY KEY (id);


--
-- Name: pendingpushes pendingpushes_messageid_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.pendingpushes
    ADD CONSTRAINT pendingpushes_messageid_key UNIQUE (messageid);


--
-- Name: permissions permissions_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pr_key PRIMARY KEY (id);


--
-- Name: plugin_audit_log plugin_audit_log_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_audit_log
    ADD CONSTRAINT plugin_audit_log_pr_key PRIMARY KEY (id);


--
-- Name: plugin_deviceinfo_deviceparams_device plugin_deviceinfo_deviceparams_device_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_device
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_device_pr_key PRIMARY KEY (id);


--
-- Name: plugin_deviceinfo_deviceparams_device plugin_deviceinfo_deviceparams_device_recordid_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_device
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_device_recordid_key UNIQUE (recordid);


--
-- Name: plugin_deviceinfo_deviceparams_gps plugin_deviceinfo_deviceparams_gps_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_gps
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_gps_pr_key PRIMARY KEY (id);


--
-- Name: plugin_deviceinfo_deviceparams_gps plugin_deviceinfo_deviceparams_gps_recordid_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_gps
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_gps_recordid_key UNIQUE (recordid);


--
-- Name: plugin_deviceinfo_deviceparams_mobile2 plugin_deviceinfo_deviceparams_mobile2_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_mobile2
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_mobile2_pr_key PRIMARY KEY (id);


--
-- Name: plugin_deviceinfo_deviceparams_mobile2 plugin_deviceinfo_deviceparams_mobile2_recordid_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_mobile2
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_mobile2_recordid_key UNIQUE (recordid);


--
-- Name: plugin_deviceinfo_deviceparams_mobile plugin_deviceinfo_deviceparams_mobile_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_mobile
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_mobile_pr_key PRIMARY KEY (id);


--
-- Name: plugin_deviceinfo_deviceparams_mobile plugin_deviceinfo_deviceparams_mobile_recordid_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_mobile
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_mobile_recordid_key UNIQUE (recordid);


--
-- Name: plugin_deviceinfo_deviceparams plugin_deviceinfo_deviceparams_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_pr_key PRIMARY KEY (id);


--
-- Name: plugin_deviceinfo_deviceparams_wifi plugin_deviceinfo_deviceparams_wifi_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_wifi
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_wifi_pr_key PRIMARY KEY (id);


--
-- Name: plugin_deviceinfo_deviceparams_wifi plugin_deviceinfo_deviceparams_wifi_recordid_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_wifi
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_wifi_recordid_key UNIQUE (recordid);


--
-- Name: plugin_deviceinfo_settings plugin_deviceinfo_settings_customer_unique; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_settings
    ADD CONSTRAINT plugin_deviceinfo_settings_customer_unique UNIQUE (customerid);


--
-- Name: plugin_deviceinfo_settings plugin_deviceinfo_settings_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_settings
    ADD CONSTRAINT plugin_deviceinfo_settings_pr_key PRIMARY KEY (id);


--
-- Name: plugin_devicelocations_history plugin_devicelocations_history_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_history
    ADD CONSTRAINT plugin_devicelocations_history_pr_key PRIMARY KEY (id);


--
-- Name: plugin_devicelocations_latest plugin_devicelocations_latest_device_unique; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_latest
    ADD CONSTRAINT plugin_devicelocations_latest_device_unique UNIQUE (deviceid);


--
-- Name: plugin_devicelocations_latest plugin_devicelocations_latest_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_latest
    ADD CONSTRAINT plugin_devicelocations_latest_pr_key PRIMARY KEY (id);


--
-- Name: plugin_devicelocations_settings plugin_devicelocations_settings_customer_unique; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_settings
    ADD CONSTRAINT plugin_devicelocations_settings_customer_unique UNIQUE (customerid);


--
-- Name: plugin_devicelocations_settings plugin_devicelocations_settings_settings_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_settings
    ADD CONSTRAINT plugin_devicelocations_settings_settings_pr_key PRIMARY KEY (id);


--
-- Name: plugin_devicelog_log plugin_devicelog_log_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_log
    ADD CONSTRAINT plugin_devicelog_log_pr_key PRIMARY KEY (id);


--
-- Name: plugin_devicelog_settings plugin_devicelog_settings_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_settings
    ADD CONSTRAINT plugin_devicelog_settings_pr_key PRIMARY KEY (id);


--
-- Name: plugin_devicelog_settings_rules plugin_devicelog_settings_rules_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_settings_rules
    ADD CONSTRAINT plugin_devicelog_settings_rules_pr_key PRIMARY KEY (id);


--
-- Name: plugin_devicereset_status plugin_devicereset_status_device_unique; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicereset_status
    ADD CONSTRAINT plugin_devicereset_status_device_unique UNIQUE (deviceid);


--
-- Name: plugin_devicereset_status plugin_devicereset_status_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicereset_status
    ADD CONSTRAINT plugin_devicereset_status_pr_key PRIMARY KEY (id);


--
-- Name: plugins plugin_identifier_unq; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugins
    ADD CONSTRAINT plugin_identifier_unq UNIQUE (identifier);


--
-- Name: plugin_knox_rules plugin_knox_settings_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_knox_rules
    ADD CONSTRAINT plugin_knox_settings_pr_key PRIMARY KEY (id);


--
-- Name: plugin_messaging_messages plugin_messaging_messages_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_messaging_messages
    ADD CONSTRAINT plugin_messaging_messages_pr_key PRIMARY KEY (id);


--
-- Name: plugin_openvpn_defaults plugin_openvpn_defaults_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_openvpn_defaults
    ADD CONSTRAINT plugin_openvpn_defaults_pr_key PRIMARY KEY (id);


--
-- Name: plugin_photo_photo_places plugin_photo_photo_places_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_photo_places
    ADD CONSTRAINT plugin_photo_photo_places_pr_key PRIMARY KEY (id);


--
-- Name: plugin_photo_photo plugin_photo_photo_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_photo
    ADD CONSTRAINT plugin_photo_photo_pr_key PRIMARY KEY (id);


--
-- Name: plugin_photo_places plugin_photo_places_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_places
    ADD CONSTRAINT plugin_photo_places_pr_key PRIMARY KEY (id);


--
-- Name: plugin_photo_settings plugin_photo_settings_customerid_unq; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_settings
    ADD CONSTRAINT plugin_photo_settings_customerid_unq UNIQUE (customerid);


--
-- Name: plugin_photo_settings plugin_photo_settings_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_settings
    ADD CONSTRAINT plugin_photo_settings_pr_key PRIMARY KEY (id);


--
-- Name: plugin_push_messages plugin_push_messages_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_push_messages
    ADD CONSTRAINT plugin_push_messages_pr_key PRIMARY KEY (id);


--
-- Name: plugins plugins_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugins
    ADD CONSTRAINT plugins_pr_key PRIMARY KEY (id);


--
-- Name: pushmessages push_message_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.pushmessages
    ADD CONSTRAINT push_message_pr_key PRIMARY KEY (id);


--
-- Name: configurations qrcodekey_uniq; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurations
    ADD CONSTRAINT qrcodekey_uniq UNIQUE (qrcodekey);


--
-- Name: userroles roles_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userroles
    ADD CONSTRAINT roles_pr_key PRIMARY KEY (id);


--
-- Name: settings settings_customer_unique; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.settings
    ADD CONSTRAINT settings_customer_unique UNIQUE (customerid);


--
-- Name: settings settings_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.settings
    ADD CONSTRAINT settings_pr_key PRIMARY KEY (id);


--
-- Name: trialkey trialkey_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.trialkey
    ADD CONSTRAINT trialkey_pr_key PRIMARY KEY (id);


--
-- Name: uploadedfiles uploadedfiles_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.uploadedfiles
    ADD CONSTRAINT uploadedfiles_pr_key PRIMARY KEY (id);


--
-- Name: userconfigurationaccess userconfigurationaccess_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userconfigurationaccess
    ADD CONSTRAINT userconfigurationaccess_pr_key PRIMARY KEY (id);


--
-- Name: userdevicegroupsaccess userdevicegroupsaccess_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userdevicegroupsaccess
    ADD CONSTRAINT userdevicegroupsaccess_pr_key PRIMARY KEY (id);


--
-- Name: userhints userhints_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userhints
    ADD CONSTRAINT userhints_pr_key PRIMARY KEY (id);


--
-- Name: userhints userhints_userid_hintkey_unique; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userhints
    ADD CONSTRAINT userhints_userid_hintkey_unique UNIQUE (userid, hintkey);


--
-- Name: userhinttypes userhinttypes_hintkey_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userhinttypes
    ADD CONSTRAINT userhinttypes_hintkey_key UNIQUE (hintkey);


--
-- Name: userrolesettings userrolesettings_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userrolesettings
    ADD CONSTRAINT userrolesettings_pr_key PRIMARY KEY (id);


--
-- Name: userrolesettings userrolesettings_role_customer_uniq; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userrolesettings
    ADD CONSTRAINT userrolesettings_role_customer_uniq UNIQUE (roleid, customerid);


--
-- Name: users users_login_unique; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_login_unique UNIQUE (login);


--
-- Name: users users_pr_key; Type: CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pr_key PRIMARY KEY (id);


--
-- Name: applications_customerid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX applications_customerid_idx ON public.applications USING btree (customerid);


--
-- Name: applications_pkg_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX applications_pkg_idx ON public.applications USING btree (pkg);


--
-- Name: applicationversionss_applicationid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX applicationversionss_applicationid_idx ON public.applicationversions USING btree (applicationid);


--
-- Name: configurationfiles_configurationid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX configurationfiles_configurationid_idx ON public.configurationfiles USING btree (configurationid);


--
-- Name: configurations_contentappid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX configurations_contentappid_idx ON public.configurations USING btree (contentappid);


--
-- Name: configurations_customerid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX configurations_customerid_idx ON public.configurations USING btree (customerid);


--
-- Name: configurations_mainappid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX configurations_mainappid_idx ON public.configurations USING btree (mainappid);


--
-- Name: devices_configurationid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX devices_configurationid_idx ON public.devices USING btree (configurationid);


--
-- Name: devices_customerid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX devices_customerid_idx ON public.devices USING btree (customerid);


--
-- Name: devices_deviceid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX devices_deviceid_idx ON public.devicegroups USING btree (deviceid);


--
-- Name: devices_fastsearch_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX devices_fastsearch_idx ON public.devices USING btree (fastsearch);


--
-- Name: devices_groupid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX devices_groupid_idx ON public.devicegroups USING btree (groupid);


--
-- Name: devices_number_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX devices_number_idx ON public.devices USING btree (number);


--
-- Name: icons_customerid_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX icons_customerid_idx ON public.icons USING btree (customerid);


--
-- Name: plugin_devicelocations_history_device; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX plugin_devicelocations_history_device ON public.plugin_devicelocations_history USING btree (deviceid);


--
-- Name: plugin_devicelocations_history_device_ts; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX plugin_devicelocations_history_device_ts ON public.plugin_devicelocations_history USING btree (deviceid, ts);


--
-- Name: plugin_devicelocations_history_ts; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX plugin_devicelocations_history_ts ON public.plugin_devicelocations_history USING btree (ts);


--
-- Name: plugin_devicelocations_latest_device; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX plugin_devicelocations_latest_device ON public.plugin_devicelocations_latest USING btree (deviceid);


--
-- Name: plugin_knox_rules_ruletype_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX plugin_knox_rules_ruletype_idx ON public.plugin_knox_rules USING btree (ruletype);


--
-- Name: plugin_knox_rules_tabletype_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX plugin_knox_rules_tabletype_idx ON public.plugin_knox_rules USING btree (tabletype);


--
-- Name: plugin_photo_photo_devicecreatetime_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE INDEX plugin_photo_photo_devicecreatetime_idx ON public.plugin_photo_photo USING btree (deviceid, createtime);


--
-- Name: plugin_photo_photo_places_uniq_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE UNIQUE INDEX plugin_photo_photo_places_uniq_idx ON public.plugin_photo_photo_places USING btree (photoid);


--
-- Name: plugin_photo_places_uniq_idx; Type: INDEX; Schema: public; Owner: hmdm
--

CREATE UNIQUE INDEX plugin_photo_places_uniq_idx ON public.plugin_photo_places USING btree (customerid, lower((placeid)::text));


--
-- Name: applications applications_iconid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_iconid_fkey FOREIGN KEY (iconid) REFERENCES public.icons(id) ON DELETE SET NULL;


--
-- Name: applications applications_latestversion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_latestversion_fkey FOREIGN KEY (latestversion) REFERENCES public.applicationversions(id) ON DELETE SET NULL;


--
-- Name: applicationversions applicationversions_applicationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.applicationversions
    ADD CONSTRAINT applicationversions_applicationid_fkey FOREIGN KEY (applicationid) REFERENCES public.applications(id) ON DELETE CASCADE;


--
-- Name: configurationapplicationparameters configurationapplicationparameters_applicationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplicationparameters
    ADD CONSTRAINT configurationapplicationparameters_applicationid_fkey FOREIGN KEY (applicationid) REFERENCES public.applications(id) ON DELETE CASCADE;


--
-- Name: configurationapplicationparameters configurationapplicationparameters_configurationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplicationparameters
    ADD CONSTRAINT configurationapplicationparameters_configurationid_fkey FOREIGN KEY (configurationid) REFERENCES public.configurations(id) ON DELETE CASCADE;


--
-- Name: configurationapplications configurationapplications_applicationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplications
    ADD CONSTRAINT configurationapplications_applicationid_fkey FOREIGN KEY (applicationid) REFERENCES public.applications(id) ON DELETE CASCADE;


--
-- Name: configurationapplications configurationapplications_applicationversionid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplications
    ADD CONSTRAINT configurationapplications_applicationversionid_fkey FOREIGN KEY (applicationversionid) REFERENCES public.applicationversions(id) ON DELETE RESTRICT;


--
-- Name: configurationapplications configurationapplications_configurationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplications
    ADD CONSTRAINT configurationapplications_configurationid_fkey FOREIGN KEY (configurationid) REFERENCES public.configurations(id) ON DELETE CASCADE;


--
-- Name: configurationapplicationsettings configurationapplicationsettings_applicationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplicationsettings
    ADD CONSTRAINT configurationapplicationsettings_applicationid_fkey FOREIGN KEY (applicationid) REFERENCES public.applications(id) ON DELETE CASCADE;


--
-- Name: configurationapplicationsettings configurationapplicationsettings_extrefid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationapplicationsettings
    ADD CONSTRAINT configurationapplicationsettings_extrefid_fkey FOREIGN KEY (extrefid) REFERENCES public.configurations(id) ON DELETE CASCADE;


--
-- Name: configurationfiles configurationfiles_configurationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationfiles
    ADD CONSTRAINT configurationfiles_configurationid_fkey FOREIGN KEY (configurationid) REFERENCES public.configurations(id) ON DELETE CASCADE;


--
-- Name: configurationfiles configurationfiles_fileid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurationfiles
    ADD CONSTRAINT configurationfiles_fileid_fkey FOREIGN KEY (fileid) REFERENCES public.uploadedfiles(id) ON DELETE CASCADE;


--
-- Name: configurations configurations_contentappid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurations
    ADD CONSTRAINT configurations_contentappid_fkey FOREIGN KEY (contentappid) REFERENCES public.applicationversions(id) ON DELETE RESTRICT;


--
-- Name: configurations configurations_mainappid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurations
    ADD CONSTRAINT configurations_mainappid_fkey FOREIGN KEY (mainappid) REFERENCES public.applicationversions(id) ON DELETE RESTRICT;


--
-- Name: deviceapplicationsettings deviceapplicationsettings_applicationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.deviceapplicationsettings
    ADD CONSTRAINT deviceapplicationsettings_applicationid_fkey FOREIGN KEY (applicationid) REFERENCES public.applications(id) ON DELETE CASCADE;


--
-- Name: deviceapplicationsettings deviceapplicationsettings_extrefid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.deviceapplicationsettings
    ADD CONSTRAINT deviceapplicationsettings_extrefid_fkey FOREIGN KEY (extrefid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: devicegroups devicegroups_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devicegroups
    ADD CONSTRAINT devicegroups_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: devicegroups devicegroups_groupid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devicegroups
    ADD CONSTRAINT devicegroups_groupid_fkey FOREIGN KEY (groupid) REFERENCES public.groups(id) ON DELETE CASCADE;


--
-- Name: devicestatuses devicestatuses_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devicestatuses
    ADD CONSTRAINT devicestatuses_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: applications fk_customer_1; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT fk_customer_1 FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: configurations fk_customer_2; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.configurations
    ADD CONSTRAINT fk_customer_2 FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: devices fk_customer_3; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.devices
    ADD CONSTRAINT fk_customer_3 FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: groups fk_customer_4; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT fk_customer_4 FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: settings fk_customer_5; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.settings
    ADD CONSTRAINT fk_customer_5 FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: users fk_customer_6; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_customer_6 FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: icons icons_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.icons
    ADD CONSTRAINT icons_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: icons icons_fileid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.icons
    ADD CONSTRAINT icons_fileid_fkey FOREIGN KEY (fileid) REFERENCES public.uploadedfiles(id) ON DELETE CASCADE;


--
-- Name: pendingpushes pendingpushes_messageid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.pendingpushes
    ADD CONSTRAINT pendingpushes_messageid_fkey FOREIGN KEY (messageid) REFERENCES public.pushmessages(id) ON DELETE CASCADE;


--
-- Name: plugin_deviceinfo_deviceparams plugin_deviceinfo_deviceparams_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_deviceinfo_deviceparams_device plugin_deviceinfo_deviceparams_device_recordid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_device
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_device_recordid_fkey FOREIGN KEY (recordid) REFERENCES public.plugin_deviceinfo_deviceparams(id) ON DELETE CASCADE;


--
-- Name: plugin_deviceinfo_deviceparams plugin_deviceinfo_deviceparams_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: plugin_deviceinfo_deviceparams_gps plugin_deviceinfo_deviceparams_gps_recordid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_gps
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_gps_recordid_fkey FOREIGN KEY (recordid) REFERENCES public.plugin_deviceinfo_deviceparams(id) ON DELETE CASCADE;


--
-- Name: plugin_deviceinfo_deviceparams_mobile2 plugin_deviceinfo_deviceparams_mobile2_recordid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_mobile2
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_mobile2_recordid_fkey FOREIGN KEY (recordid) REFERENCES public.plugin_deviceinfo_deviceparams(id) ON DELETE CASCADE;


--
-- Name: plugin_deviceinfo_deviceparams_mobile plugin_deviceinfo_deviceparams_mobile_recordid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_mobile
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_mobile_recordid_fkey FOREIGN KEY (recordid) REFERENCES public.plugin_deviceinfo_deviceparams(id) ON DELETE CASCADE;


--
-- Name: plugin_deviceinfo_deviceparams_wifi plugin_deviceinfo_deviceparams_wifi_recordid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_deviceparams_wifi
    ADD CONSTRAINT plugin_deviceinfo_deviceparams_wifi_recordid_fkey FOREIGN KEY (recordid) REFERENCES public.plugin_deviceinfo_deviceparams(id) ON DELETE CASCADE;


--
-- Name: plugin_deviceinfo_settings plugin_deviceinfo_settings_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_deviceinfo_settings
    ADD CONSTRAINT plugin_deviceinfo_settings_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelocations_history plugin_devicelocations_history_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_history
    ADD CONSTRAINT plugin_devicelocations_history_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelocations_latest plugin_devicelocations_latest_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_latest
    ADD CONSTRAINT plugin_devicelocations_latest_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelocations_settings plugin_devicelocations_settings_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelocations_settings
    ADD CONSTRAINT plugin_devicelocations_settings_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_log plugin_devicelog_log_applicationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_log
    ADD CONSTRAINT plugin_devicelog_log_applicationid_fkey FOREIGN KEY (applicationid) REFERENCES public.applications(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_log plugin_devicelog_log_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_log
    ADD CONSTRAINT plugin_devicelog_log_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_log plugin_devicelog_log_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_log
    ADD CONSTRAINT plugin_devicelog_log_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_setting_rule_devices plugin_devicelog_setting_rule_devices_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_setting_rule_devices
    ADD CONSTRAINT plugin_devicelog_setting_rule_devices_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_setting_rule_devices plugin_devicelog_setting_rule_devices_ruleid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_setting_rule_devices
    ADD CONSTRAINT plugin_devicelog_setting_rule_devices_ruleid_fkey FOREIGN KEY (ruleid) REFERENCES public.plugin_devicelog_settings_rules(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_settings plugin_devicelog_settings_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_settings
    ADD CONSTRAINT plugin_devicelog_settings_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_settings_rules plugin_devicelog_settings_rules_applicationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_settings_rules
    ADD CONSTRAINT plugin_devicelog_settings_rules_applicationid_fkey FOREIGN KEY (applicationid) REFERENCES public.applications(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_settings_rules plugin_devicelog_settings_rules_configurationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_settings_rules
    ADD CONSTRAINT plugin_devicelog_settings_rules_configurationid_fkey FOREIGN KEY (configurationid) REFERENCES public.configurations(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_settings_rules plugin_devicelog_settings_rules_groupid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_settings_rules
    ADD CONSTRAINT plugin_devicelog_settings_rules_groupid_fkey FOREIGN KEY (groupid) REFERENCES public.groups(id) ON DELETE CASCADE;


--
-- Name: plugin_devicelog_settings_rules plugin_devicelog_settings_rules_settingid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicelog_settings_rules
    ADD CONSTRAINT plugin_devicelog_settings_rules_settingid_fkey FOREIGN KEY (settingid) REFERENCES public.plugin_devicelog_settings(id) ON DELETE CASCADE;


--
-- Name: plugin_devicereset_status plugin_devicereset_status_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_devicereset_status
    ADD CONSTRAINT plugin_devicereset_status_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: plugin_knox_rules plugin_knox_rules_configurationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_knox_rules
    ADD CONSTRAINT plugin_knox_rules_configurationid_fkey FOREIGN KEY (configurationid) REFERENCES public.configurations(id) ON DELETE CASCADE;


--
-- Name: plugin_messaging_messages plugin_messaging_messages_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_messaging_messages
    ADD CONSTRAINT plugin_messaging_messages_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_messaging_messages plugin_messaging_messages_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_messaging_messages
    ADD CONSTRAINT plugin_messaging_messages_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: plugin_openvpn_defaults plugin_openvpn_defaults_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_openvpn_defaults
    ADD CONSTRAINT plugin_openvpn_defaults_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_photo_photo plugin_photo_photo_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_photo
    ADD CONSTRAINT plugin_photo_photo_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_photo_photo plugin_photo_photo_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_photo
    ADD CONSTRAINT plugin_photo_photo_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: plugin_photo_photo_places plugin_photo_photo_places_photoid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_photo_places
    ADD CONSTRAINT plugin_photo_photo_places_photoid_fkey FOREIGN KEY (photoid) REFERENCES public.plugin_photo_photo(id) ON DELETE CASCADE;


--
-- Name: plugin_photo_places plugin_photo_places_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_places
    ADD CONSTRAINT plugin_photo_places_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_photo_settings plugin_photo_settings_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_photo_settings
    ADD CONSTRAINT plugin_photo_settings_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_push_messages plugin_push_messages_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_push_messages
    ADD CONSTRAINT plugin_push_messages_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: plugin_push_messages plugin_push_messages_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.plugin_push_messages
    ADD CONSTRAINT plugin_push_messages_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: pluginsdisabled pluginsdisabled_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.pluginsdisabled
    ADD CONSTRAINT pluginsdisabled_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: pluginsdisabled pluginsdisabled_pluginid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.pluginsdisabled
    ADD CONSTRAINT pluginsdisabled_pluginid_fkey FOREIGN KEY (pluginid) REFERENCES public.plugins(id) ON DELETE CASCADE;


--
-- Name: pushmessages pushmessages_deviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.pushmessages
    ADD CONSTRAINT pushmessages_deviceid_fkey FOREIGN KEY (deviceid) REFERENCES public.devices(id) ON DELETE CASCADE;


--
-- Name: uploadedfiles uploadedfiles_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.uploadedfiles
    ADD CONSTRAINT uploadedfiles_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: userconfigurationaccess userconfigurationaccess_configurationid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userconfigurationaccess
    ADD CONSTRAINT userconfigurationaccess_configurationid_fkey FOREIGN KEY (configurationid) REFERENCES public.configurations(id) ON DELETE CASCADE;


--
-- Name: userconfigurationaccess userconfigurationaccess_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userconfigurationaccess
    ADD CONSTRAINT userconfigurationaccess_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: userdevicegroupsaccess userdevicegroupsaccess_groupid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userdevicegroupsaccess
    ADD CONSTRAINT userdevicegroupsaccess_groupid_fkey FOREIGN KEY (groupid) REFERENCES public.groups(id) ON DELETE CASCADE;


--
-- Name: userdevicegroupsaccess userdevicegroupsaccess_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userdevicegroupsaccess
    ADD CONSTRAINT userdevicegroupsaccess_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: userhints userhints_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userhints
    ADD CONSTRAINT userhints_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: userrolepermissions userrolepermissions_permissionid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userrolepermissions
    ADD CONSTRAINT userrolepermissions_permissionid_fkey FOREIGN KEY (permissionid) REFERENCES public.permissions(id) ON DELETE CASCADE;


--
-- Name: userrolepermissions userrolepermissions_roleid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userrolepermissions
    ADD CONSTRAINT userrolepermissions_roleid_fkey FOREIGN KEY (roleid) REFERENCES public.userroles(id) ON DELETE CASCADE;


--
-- Name: userrolesettings userrolesettings_customerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userrolesettings
    ADD CONSTRAINT userrolesettings_customerid_fkey FOREIGN KEY (customerid) REFERENCES public.customers(id) ON DELETE CASCADE;


--
-- Name: userrolesettings userrolesettings_roleid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.userrolesettings
    ADD CONSTRAINT userrolesettings_roleid_fkey FOREIGN KEY (roleid) REFERENCES public.userroles(id) ON DELETE CASCADE;


--
-- Name: users users_userroleid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hmdm
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_userroleid_fkey FOREIGN KEY (userroleid) REFERENCES public.userroles(id) ON DELETE RESTRICT;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: hmdm
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


--
-- PostgreSQL database dump complete
--

