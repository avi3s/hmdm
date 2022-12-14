CREATE TABLE users
(
    id       serial      NOT NULL
        CONSTRAINT users_pr_key PRIMARY KEY,
    login    varchar(30) NOT NULL
        CONSTRAINT login_key UNIQUE,
    email    varchar(50),
    name     varchar(50),
    password varchar(32) NOT NULL
);

CREATE TABLE configurations
(
    id          serial       NOT NULL
        CONSTRAINT configurations_pr_key PRIMARY KEY,
    name        varchar(100) NOT NULL,
    description text,
    type        int          NOT NULL DEFAULT 0
);

CREATE TABLE applications
(
    id      serial       NOT NULL
        CONSTRAINT applications_pr_key PRIMARY KEY,
    pkg     varchar(100) NOT NULL,
    name    varchar(100) NOT NULL,
    version varchar(10)  NOT NULL,
    url     varchar(500)
);

CREATE TABLE configurationApplications
(
    id              serial NOT NULL
        CONSTRAINT configuration_applications_pr_key PRIMARY KEY,
    configurationId int    NOT NULL REFERENCES configurations (id) ON DELETE CASCADE,
    applicationId   int    NOT NULL REFERENCES applications (id) ON DELETE CASCADE
);

CREATE TABLE settings
(
    id                 serial NOT NULL
        CONSTRAINT settings_pr_key PRIMARY KEY,
    backgroundColor    varchar(20),
    textColor          varchar(20),
    backgroundImageUrl varchar(500)
);

CREATE TABLE customers
(
    id          serial      NOT NULL
        CONSTRAINT customers_pr_key PRIMARY KEY,
    name        varchar(50) NOT NULL,
    description TEXT,
    filesDir    TEXT        NOT NULL,
    master      BOOLEAN     NOT NULL DEFAULT false
);

CREATE TABLE plugin_deviceinfo_settings
(
    id                 serial NOT NULL
        CONSTRAINT plugin_deviceinfo_settings_pr_key PRIMARY KEY,
    customerId         INT    NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    dataPreservePeriod INT    NOT NULL DEFAULT 30,
    intervalmins    INT          NOT NULL,
    senddata           varchar(20)
);

CREATE TABLE plugin_devicelog_settings_rules
(
    id              serial       NOT NULL
        CONSTRAINT plugin_devicelog_settings_rules_pr_key PRIMARY KEY,
    settingId       INT          NOT NULL REFERENCES plugin_deviceinfo_settings (id) ON DELETE CASCADE,
    name            VARCHAR(120) NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    applicationId   INT          NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    severity        TEXT         NOT NULL,
    filter          TEXT,
    groupId         INT REFERENCES groups (id) ON DELETE CASCADE,
    configurationId INT REFERENCES configurations (id) ON DELETE CASCADE
);

CREATE TABLE groups
(
    id   serial       NOT NULL
        CONSTRAINT groups_pr_key PRIMARY KEY,
    name varchar(100) NOT NULL
);

CREATE TABLE devices
(
    id                 serial       NOT NULL
        CONSTRAINT devices_pr_key PRIMARY KEY,
    number             varchar(100) NOT NULL,
    description        text,
    lastUpdate         bigint       NOT NULL DEFAULT 0,
    configurationId    int          NOT NULL DEFAULT 1,
    oldConfigurationId int,
    groupId            int          NOT NULL DEFAULT 1
);

CREATE TABLE plugins
(
    id                        serial      NOT NULL
        CONSTRAINT plugins_pr_key PRIMARY KEY,
    identifier                VARCHAR(50) NOT NULL
        CONSTRAINT plugin_identifier_unq UNIQUE,
    name                      VARCHAR(50) NOT NULL,
    description               TEXT,
    createTime                TIMESTAMP   NOT NULL DEFAULT NOW(),
    disabled                  BOOLEAN     NOT NULL DEFAULT FALSE,
    javascriptModuleFile      VARCHAR(200),
    functionsViewTemplate     VARCHAR(200),
    namelocalizationkey       VARCHAR(200),
    settingsViewTemplate      VARCHAR(200),
    settingsPermission        VARCHAR(200),
    functionsPermission       VARCHAR(200),
    deviceFunctionsPermission VARCHAR(200),
    enabledForDevice          VARCHAR(200)
);

CREATE TABLE pluginsDisabled
(
    pluginId   INT NOT NULL REFERENCES plugins (id) ON DELETE CASCADE,
    customerId INT NOT NULL REFERENCES customers (id) ON DELETE CASCADE
);

CREATE TABLE permissions
(
    id          serial      NOT NULL
        CONSTRAINT permissions_pr_key PRIMARY KEY,
    name        varchar(50) NOT NULL,
    description TEXT,
    superadmin  BOOLEAN     NOT NULL DEFAULT false
);

CREATE TABLE userRoles
(
    id          serial      NOT NULL
        CONSTRAINT roles_pr_key PRIMARY KEY,
    name        varchar(50) NOT NULL,
    description TEXT,
    superadmin  BOOLEAN     NOT NULL DEFAULT false
);

CREATE TABLE userRolePermissions
(
    roleId       INT NOT NULL REFERENCES userRoles (id) ON DELETE CASCADE,
    permissionId INT NOT NULL REFERENCES permissions (id) ON DELETE CASCADE
);

CREATE TABLE deviceGroups
(
    id       serial NOT NULL
        CONSTRAINT deviceGroups_pr_key PRIMARY KEY,
    deviceId INT    NOT NULL REFERENCES devices (id) ON DELETE CASCADE,
    groupId  INT    NOT NULL REFERENCES groups (id) ON DELETE CASCADE
);

CREATE TABLE userDeviceGroupsAccess
(
    id      serial NOT NULL
        CONSTRAINT userDeviceGroupsAccess_pr_key PRIMARY KEY,
    userId  INT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    groupId INT    NOT NULL REFERENCES groups (id) ON DELETE CASCADE
);

CREATE TABLE applicationVersions
(
    id            serial      NOT NULL
        CONSTRAINT applicationVersions_pr_key PRIMARY KEY,
    applicationId INT         NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    version       varchar(50) NOT NULL,
    url           varchar(500)
);

CREATE TABLE deviceApplicationSettings
(
    id            serial       NOT NULL
        CONSTRAINT deviceApplicationSettings_pr_key PRIMARY KEY,
    applicationID INT          NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    name          VARCHAR(200) NOT NULL,
    type          VARCHAR(20)  NOT NULL,
    value         TEXT,
    comment       TEXT,
    readonly      BOOLEAN      NOT NULL DEFAULT FALSE,
    extRefId      INT          NOT NULL REFERENCES devices (id) ON DELETE CASCADE,
    lastUpdate    BIGINT       NOT NULL
);

CREATE TABLE configurationApplicationSettings
(
    id            serial       NOT NULL
        CONSTRAINT configurationApplicationSettings_pr_key PRIMARY KEY,
    applicationID INT          NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    name          VARCHAR(200) NOT NULL,
    type          VARCHAR(20)  NOT NULL,
    value         TEXT,
    comment       TEXT,
    readonly      BOOLEAN      NOT NULL DEFAULT FALSE,
    extRefId      INT          NOT NULL REFERENCES configurations (id) ON DELETE CASCADE,
    lastUpdate    BIGINT       NOT NULL
);

CREATE TABLE configurationApplicationParameters
(
    id               serial  NOT NULL
        CONSTRAINT configuration_application_parameters_pr_key PRIMARY KEY,
    configurationId  int     NOT NULL REFERENCES configurations (id) ON DELETE CASCADE,
    applicationId    int     NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    skipVersionCheck BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE userHints
(
    id      serial       NOT NULL
        CONSTRAINT userHints_pr_key PRIMARY KEY,
    userId  INT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    hintKey VARCHAR(100) NOT NULL,
    created TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE userHintTypes
(
    hintKey VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE userRoleSettings
(
    id                                     serial NOT NULL
        CONSTRAINT userRoleSettings_pr_key PRIMARY KEY,
    roleId                                 INT    NOT NULL REFERENCES userRoles (id) ON DELETE CASCADE,
    customerId                             INT    NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    columnDisplayedDeviceStatus            BOOLEAN,
    columnDisplayedDeviceDate              BOOLEAN,
    columnDisplayedDeviceNumber            BOOLEAN,
    columnDisplayedDeviceModel             BOOLEAN,
    columnDisplayedDevicePermissionsStatus BOOLEAN,
    columnDisplayedDeviceAppInstallStatus  BOOLEAN,
    columnDisplayedDeviceConfiguration     BOOLEAN,
    columnDisplayedDeviceImei              BOOLEAN,
    columnDisplayedDevicePhone             BOOLEAN,
    columnDisplayedDeviceDesc              BOOLEAN,
    columnDisplayedDeviceGroup             BOOLEAN,
    columnDisplayedLauncherVersion         BOOLEAN
);

CREATE TABLE uploadedFiles
(
    id         SERIAL NOT NULL
        CONSTRAINT uploadedFiles_pr_key PRIMARY KEY,
    customerId INT    NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    filePath   TEXT   NOT NULL,
    uploadTime BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000
);

CREATE TABLE icons
(
    id         SERIAL      NOT NULL
        CONSTRAINT icons_pr_key PRIMARY KEY,
    customerId INT         NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    name       VARCHAR(64) NOT NULL,
    fileId     INT         NOT NULL REFERENCES uploadedFiles (id) ON DELETE CASCADE
);

CREATE TABLE configurationFiles
(
    id              SERIAL  NOT NULL
        CONSTRAINT configurationFiles_pr_key PRIMARY KEY,
    configurationId INT     NOT NULL REFERENCES configurations (id) ON DELETE CASCADE,
    name            TEXT    NOT NULL,
    description     TEXT    NOT NULL,
    devicePath      TEXT    NOT NULL,
    externalUrl     TEXT,
    filePath        TEXT,
    checksum        TEXT    NOT NULL,
    remove          BOOLEAN NOT NULL DEFAULT FALSE,
    lastUpdate      BIGINT  NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000,
    fileId          INT REFERENCES uploadedFiles (id) ON DELETE CASCADE
);

CREATE TABLE deviceStatuses
(
    deviceId           INT NOT NULL REFERENCES devices (id) ON DELETE CASCADE
        CONSTRAINT deviceStatuses_pr_key PRIMARY KEY,
    configFilesStatus  VARCHAR(100),
    applicationsStatus VARCHAR(100)
);

CREATE TABLE trialkey
(
    id      serial      NOT NULL
        CONSTRAINT trialkey_pr_key PRIMARY KEY,
    keycode VARCHAR(50) NOT NULL,
    created TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE userConfigurationAccess
(
    id              serial NOT NULL
        CONSTRAINT userConfigurationAccess_pr_key PRIMARY KEY,
    userId          INT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    configurationId INT    NOT NULL REFERENCES configurations (id) ON DELETE CASCADE
);



ALTER TABLE configurations
    ADD COLUMN password varchar(100);
ALTER TABLE devices
    ADD COLUMN info text;
ALTER TABLE applications
    ALTER COLUMN name TYPE VARCHAR(500);
ALTER TABLE applications
    ALTER COLUMN version TYPE VARCHAR(100);
ALTER TABLE applications
    ADD COLUMN showIcon BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDeviceStatus BOOLEAN;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDeviceDate BOOLEAN;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDeviceNumber BOOLEAN;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDeviceModel BOOLEAN;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDevicePermissionsStatus BOOLEAN;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDeviceAppInstallStatus BOOLEAN;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDeviceConfiguration BOOLEAN;
ALTER TABLE settings
    ADD COLUMN iconSize TEXT NOT NULL DEFAULT 'SMALL';
ALTER TABLE settings
    ADD COLUMN desktopHeader TEXT NOT NULL DEFAULT 'NO_HEADER';
ALTER TABLE devices
    ADD COLUMN imei VARCHAR(50);
ALTER TABLE devices
    ADD COLUMN phone VARCHAR(20);
ALTER TABLE settings
    ADD COLUMN columnDisplayedDeviceImei BOOLEAN;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDevicePhone BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN backgroundcolor VARCHAR(20);
ALTER TABLE configurations
    ADD COLUMN textcolor VARCHAR(20);
ALTER TABLE configurations
    ADD COLUMN backgroundimageurl VARCHAR(500);
ALTER TABLE configurations
    ADD COLUMN iconsize TEXT DEFAULT 'SMALL'::TEXT NOT NULL;
ALTER TABLE configurations
    ADD COLUMN desktopheader TEXT DEFAULT 'NO_HEADER'::TEXT NOT NULL;
ALTER TABLE configurations
    ADD COLUMN useDefaultDesignSettings BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDeviceDesc BOOLEAN;
ALTER TABLE settings
    ADD COLUMN columnDisplayedDeviceGroup BOOLEAN;
ALTER TABLE customers
    ADD CONSTRAINT customer_filesdir_key UNIQUE (filesDir);
ALTER TABLE applications
    ADD COLUMN customerId BIGINT;
ALTER TABLE configurations
    ADD COLUMN customerId BIGINT;
ALTER TABLE devices
    ADD COLUMN customerId BIGINT;
ALTER TABLE groups
    ADD COLUMN customerId BIGINT;
ALTER TABLE settings
    ADD COLUMN customerId BIGINT;
ALTER TABLE users
    ADD COLUMN customerId BIGINT;
INSERT INTO customers (id, name, description, master, filesDir)
VALUES (1, 'DEFAULT',
        'Default customer account used for managing the already existing application data in SHARED usage scenario',
        FALSE, 'DEFAULT');
UPDATE users
SET customerId = currval('customers_id_seq');

INSERT INTO customers (id, name, description, master, filesDir)
VALUES (2, 'ADMIN',
        'Global customer account used for managing the application data for all customers in SHARED usage scenario',
        TRUE, 'ADMIN');
INSERT INTO users (login, name, password, customerId)
SELECT 'superadmin', 'Super Admin', password, currval('customers_id_seq')
FROM users
WHERE login = 'admin';

/*INSERT INTO customers (id, name, description, master, filesDir)
VALUES (3, 'DEFAULT', 'Default customer account used for managing the application data in PRIVATE usage scenario', FALSE,
        '');*/

ALTER TABLE customers
    ADD CONSTRAINT customer_name_key UNIQUE (name);
UPDATE users
SET customerId = currval('customers_id_seq');

UPDATE applications
SET customerId = (SELECT id FROM customers WHERE name = 'DEFAULT');
UPDATE configurations
SET customerId = (SELECT id FROM customers WHERE name = 'DEFAULT');
UPDATE devices
SET customerId = (SELECT id FROM customers WHERE name = 'DEFAULT');
UPDATE groups
SET customerId = (SELECT id FROM customers WHERE name = 'DEFAULT');
UPDATE settings
SET customerId = (SELECT id FROM customers WHERE name = 'DEFAULT');

ALTER TABLE applications
    ADD CONSTRAINT fk_customer_1 FOREIGN KEY (customerId) REFERENCES customers (id) ON DELETE CASCADE;
ALTER TABLE configurations
    ADD CONSTRAINT fk_customer_2 FOREIGN KEY (customerId) REFERENCES customers (id) ON DELETE CASCADE;
ALTER TABLE devices
    ADD CONSTRAINT fk_customer_3 FOREIGN KEY (customerId) REFERENCES customers (id) ON DELETE CASCADE;
ALTER TABLE groups
    ADD CONSTRAINT fk_customer_4 FOREIGN KEY (customerId) REFERENCES customers (id) ON DELETE CASCADE;
ALTER TABLE settings
    ADD CONSTRAINT fk_customer_5 FOREIGN KEY (customerId) REFERENCES customers (id) ON DELETE CASCADE;
ALTER TABLE users
    ADD CONSTRAINT fk_customer_6 FOREIGN KEY (customerId) REFERENCES customers (id) ON DELETE CASCADE;

ALTER TABLE users
    ADD COLUMN userrole VARCHAR(50) NOT NULL DEFAULT 'USER';
ALTER TABLE users
    ADD CONSTRAINT users_login_unique UNIQUE (login);

UPDATE users
SET userrole = 'SUPER_ADMIN'
WHERE customerId IN (SELECT id FROM customers WHERE name = 'ADMIN');
UPDATE users
SET userrole = 'ORG_ADMIN'
WHERE customerId IN (SELECT id FROM customers WHERE name = 'DEFAULT');

ALTER TABLE devices
    ADD CONSTRAINT devices_number_unique UNIQUE (number);

ALTER TABLE devices
    ALTER COLUMN configurationId DROP DEFAULT;
ALTER TABLE devices
    ALTER COLUMN groupId DROP DEFAULT;

INSERT INTO permissions (id, name, description, superadmin)
VALUES (1, 'superadmin', 'Функции супер-администратора всего приложения', TRUE);
INSERT INTO permissions (id, name, description)
VALUES (2, 'settings', 'Имеет доступ к настройкам и видит их в меню');
INSERT INTO permissions (id, name, description)
VALUES (3, 'configurations', 'Имеет доступ к конфигурациям, приложениям и файлам и видит их в меню');
INSERT INTO permissions (id, name, description)
VALUES (4, 'edit_devices', 'Имеет доступ к редактированию и добавлению устройств');

INSERT INTO userRoles (id, name, description, superadmin)
VALUES (1, 'Супер-Администратор', 'Всевидящее око Саурона', TRUE);
INSERT INTO userRoles (id, name, description)
VALUES (2, 'Администратор', 'Выполняет функции администратора для одной клиентской записи');
INSERT INTO userRoles (id, name, description)
VALUES (3, 'Пользователь', 'Пользователь для одной клиентской записи');

INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (1, 1);

INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (2, 2);
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (2, 3);
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (2, 4);

INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (3, 3);
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (3, 4);

ALTER SEQUENCE customers_id_seq RESTART WITH 100;
ALTER SEQUENCE permissions_id_seq RESTART WITH 100;
ALTER SEQUENCE userroles_id_seq RESTART WITH 100;

create sequence plugin_devicelog_settings_rules_id_seq
    as integer;

alter sequence plugin_devicelog_settings_rules_id_seq owner to hmdm;

alter sequence plugin_devicelog_settings_rules_id_seq owned by plugin_deviceinfo_settings.id;

ALTER SEQUENCE plugin_devicelog_settings_rules_id_seq RESTART WITH 100;

ALTER TABLE users
    ADD COLUMN userRoleId INT REFERENCES userRoles (id) ON DELETE RESTRICT;

UPDATE users
SET userRoleId = 1
WHERE userRole = 'SUPER_ADMIN';
UPDATE users
SET userRoleId = 2
WHERE userRole = 'ORG_ADMIN';
UPDATE users
SET userRoleId = 3
WHERE userRole = 'USER';

ALTER TABLE users
    DROP COLUMN userRole;

ALTER TABLE configurations
    ADD COLUMN gps BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN bluetooth BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN wifi BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN mobileData BOOLEAN;

ALTER TABLE configurationapplications
    ADD COLUMN remove BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE configurations
    ADD COLUMN mainAppId INT REFERENCES applications (id) ON DELETE CASCADE;
ALTER TABLE configurations
    ADD COLUMN eventReceivingComponent VARCHAR(512);
ALTER TABLE configurations
    ADD COLUMN kioskMode BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE configurations
    ADD COLUMN qrCodeKey TEXT NOT NULL DEFAULT MD5(RANDOM()::TEXT)
        CONSTRAINT qrCodeKey_uniq UNIQUE;

INSERT INTO userRoles (name, description)
VALUES ('Наблюдатель', 'Наблюдатель зорко наблюдает');

ALTER TABLE configurations
    ADD COLUMN contentAppId INT REFERENCES applications (id) ON DELETE RESTRICT;
ALTER TABLE configurations
    DROP CONSTRAINT configurations_mainappid_fkey;
ALTER TABLE configurations
    ADD CONSTRAINT configurations_mainappid_fkey FOREIGN KEY (mainappid) REFERENCES applications (id) ON DELETE RESTRICT;

ALTER TABLE devices
    ALTER COLUMN groupId DROP NOT NULL;



INSERT INTO deviceGroups (deviceId, groupId)
SELECT id, groupId
FROM devices
WHERE NOT groupId IS NULL;

ALTER TABLE devices
    DROP COLUMN groupId;

ALTER TABLE users
    ADD COLUMN allDevicesAvailable BOOLEAN NOT NULL DEFAULT TRUE;



ALTER TABLE settings
    ADD COLUMN useDefaultLanguage BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE settings
    ADD COLUMN language VARCHAR(20);
ALTER TABLE applications
    ADD COLUMN system BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE configurationapplications
    ADD COLUMN showIcon BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN autoUpdate BOOLEAN NOT NULL DEFAULT FALSE;



ALTER TABLE applicationVersions
    ADD CONSTRAINT applicationVersions_app_version_key UNIQUE (applicationId, version);
DROP FUNCTION IF EXISTS mdm_app_version_comparison_index(TEXT);

CREATE OR REPLACE FUNCTION mdm_app_version_comparison_index(
    version_text TEXT
)
    RETURNS TEXT AS
$$

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
$$
    LANGUAGE 'plpgsql';

ALTER TABLE applications
    ADD COLUMN latestVersion INT REFERENCES applicationVersions (id) ON DELETE SET NULL;
ALTER TABLE configurationapplications
    ADD COLUMN applicationVersionId INT;

DROP TABLE IF EXISTS applicationVersionsTemp;
DROP TABLE IF EXISTS applicationFilesToCopyTemp;

SELECT FALSE                                                    AS to_be_deleted,
       FALSE                                                    AS to_be_replaced,
       applications.id                                          AS id,
       COALESCE(
               (SELECT apps2.id
                FROM applications apps2
                         INNER JOIN customers cust2 ON cust2.id = apps2.customerId
                WHERE apps2.pkg = applications.pkg
                  AND cust2.master IS TRUE),
               COALESCE((SELECT id
                         FROM applications ou2
                         WHERE pkg = applications.pkg
                           AND ou2.customerid = applications.customerid
                           AND (SELECT count(*)
                                FROM applications inr2
                                WHERE inr2.pkg = ou2.pkg
                                  AND ou2.customerid = inr2.customerid
                                  AND mdm_app_version_comparison_index(inr2.version) >
                                      mdm_app_version_comparison_index(ou2.version)) = 0),
                        applications.id))                       AS newApplicationId,
       COALESCE((SELECT apps2.id
                 FROM applications apps2
                          INNER JOIN customers cust2 ON cust2.id = apps2.customerId
                 WHERE apps2.pkg = applications.pkg
                   AND apps2.version = applications.version
                   AND cust2.master IS TRUE),
                applications.id)                                AS newApplicationVersionId,
       COALESCE((SELECT apps2.url
                 FROM applications apps2
                          INNER JOIN customers cust2 ON cust2.id = apps2.customerId
                 WHERE apps2.pkg = applications.pkg
                   AND apps2.version = applications.version
                   AND cust2.master IS TRUE), applications.url) AS newUrl,
       applications.name,
       applications.pkg,
       applications.version,
       applications.url,
       applications.customerId,
       customers.master                                         AS isMasterCustomer,
       COALESCE((SELECT TRUE
                 FROM applications apps2
                          INNER JOIN customers cust2 ON cust2.id = apps2.customerId
                 WHERE apps2.pkg = applications.pkg
                   AND cust2.master IS TRUE),
                FALSE)                                          AS masterAppExists,
       COALESCE((SELECT TRUE
                 FROM applications apps2
                          INNER JOIN customers cust2 ON cust2.id = apps2.customerId
                 WHERE apps2.pkg = applications.pkg
                   AND apps2.version = applications.version
                   AND cust2.master IS TRUE),
                FALSE)                                          AS masterVersionExists
INTO TABLE applicationVersionsTemp
FROM applications
         INNER JOIN customers ON customers.id = applications.customerId
ORDER BY pkg, version, customerid;

-- 2. Mark the application versions which must be deleted since there is exact version found in common applications
UPDATE applicationVersionsTemp
SET to_be_deleted = TRUE
WHERE id <> newApplicationVersionId;

-- 2.0 For multiple same versions of common app
UPDATE applicationVersionsTemp ou
SET to_be_replaced          = TRUE,
    to_be_deleted           = TRUE,
    newApplicationVersionId = (SELECT id
                               FROM applicationVersionsTemp ou2
                               WHERE masterAppExists
                                 AND NOT masterVersionExists
                                 AND newApplicationId = ou.newApplicationId
                                 AND pkg = ou.pkg
                                 AND version = ou.version
                                 AND (SELECT count(*)
                                      FROM applicationVersionsTemp inr2
                                      WHERE inr2.newApplicationId = ou2.newApplicationId
                                        AND inr2.pkg = ou2.pkg
                                        AND inr2.version = ou2.version
                                        AND inr2.id > ou2.id) = 0)
WHERE masterAppExists
  AND NOT masterVersionExists
  AND (SELECT count(*)
       FROM applicationVersionsTemp inr
       WHERE inr.newApplicationId = ou.newApplicationId
         AND inr.pkg = ou.pkg
         AND inr.version = ou.version
         AND inr.id > ou.id) > 0;


-- 2.1 Update the newUrls for application versions related to common applications
UPDATE applicationVersionsTemp
SET newUrl = '${file.resource.url}' || '/ADMIN/' || SUBSTRING(url, LENGTH('${file.resource.url}' || '/') + 1)
WHERE masterappexists
  AND NOT masterversionexists
  AND NOT url IS NULL
  AND LEFT(url, LENGTH('${file.resource.url}')) = '${file.resource.url}';

-- 3. Drop referential constraints for tables referencing the applications table
ALTER TABLE configurations
    DROP CONSTRAINT IF EXISTS configurations_mainappid_fkey;
ALTER TABLE configurations
    DROP CONSTRAINT IF EXISTS configurations_contentappid_fkey;
-- ALTER TABLE configurationapplications DROP CONSTRAINT IF EXISTS configurationapplications_applicationid_fkey;

-- 3. Update configurations#mainAppId, contentAppId table records to refer to records in applicationVersions according to
--    evaluated state
UPDATE configurations
SET mainAppId = (SELECT app.newApplicationVersionId
                 FROM applicationVersionsTemp app
                 WHERE app.id = configurations.mainappid)
WHERE NOT mainAppId IS NULL;
UPDATE configurations
SET contentAppId = (SELECT app.newApplicationVersionId
                    FROM applicationVersionsTemp app
                    WHERE app.id = configurations.contentAppId)
WHERE NOT contentAppId IS NULL;

-- 6. Update configurationApplications#applicationVersionId table records to refer to records in applicationVersions according to
--    evaluated state
UPDATE configurationApplications
SET applicationVersionId = (SELECT app.newApplicationVersionId
                            FROM applicationVersionsTemp app
                            WHERE app.id = configurationApplications.applicationId);
UPDATE configurationApplications
SET applicationId = (SELECT app.newApplicationId
                     FROM applicationVersionsTemp app
                     WHERE app.id = configurationApplications.applicationId);

-- 7. Delete redundant records from applicationVersions table
DELETE
FROM applicationVersionsTemp
WHERE to_be_deleted IS TRUE;

-- 8. Build the table with list of files which must be copied/moved to Master customer directory
SELECT url, ' -> ', newUrl
INTO TABLE applicationFilesToCopyTemp
FROM applicationVersionsTemp
WHERE newUrl <> url;

-- 9. Delete records from applications table which are no longer referenced from the applicationVersions table
DELETE
FROM applications
WHERE NOT EXISTS(SELECT 1 FROM applicationVersionsTemp temp WHERE temp.newApplicationId = applications.id);

-- 10. Copy valid records to applicationVersions table
INSERT INTO applicationVersions (id, applicationid, version, url) (SELECT newApplicationVersionId, newApplicationId, version, newUrl
                                                                   FROM applicationVersionsTemp);

-- 11. Update the ID generation sequence
ALTER SEQUENCE applicationVersions_id_seq RESTART WITH 10000;

-- 12. Add referential constraints for tables referencing the new applicationVersions table
ALTER TABLE configurations
    ADD CONSTRAINT configurations_mainappid_fkey FOREIGN KEY (mainappid) REFERENCES applicationversions (id) ON DELETE RESTRICT;
ALTER TABLE configurations
    ADD CONSTRAINT configurations_contentappid_fkey FOREIGN KEY (contentappid) REFERENCES applicationversions (id) ON DELETE RESTRICT;

ALTER TABLE configurationApplications
    ADD CONSTRAINT configurationapplications_applicationversionid_fkey FOREIGN KEY (applicationVersionId) REFERENCES applicationversions (id) ON DELETE RESTRICT;

-- 13. Delete version, url columns from applications table
ALTER TABLE applications
    DROP COLUMN version;
ALTER TABLE applications
    DROP COLUMN url;

-- 14. Set the reference to most recent version for applications records
UPDATE applications
SET latestVersion = (SELECT id
                     FROM applicationVersions apv1
                     WHERE apv1.applicationId = applications.id
                       AND mdm_app_version_comparison_index(apv1.version)
                         = (SELECT MAX(mdm_app_version_comparison_index(apv2.version))
                            FROM applicationVersions apv2
                            WHERE apv2.applicationId = applications.id)
                     LIMIT 1)
WHERE 1 = 1;

-- 15. Set he name of the application based on the name of the most recent version
UPDATE applications
SET name = (SELECT name FROM applicationVersionsTemp WHERE applicationVersionsTemp.id = applications.latestVersion);

ALTER TABLE configurations
    ADD COLUMN blockStatusBar BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE configurations
    ADD COLUMN systemUpdateType INT NOT NULL DEFAULT 0;
ALTER TABLE configurations
    ADD COLUMN systemUpdateFrom VARCHAR(10);
ALTER TABLE configurations
    ADD COLUMN systemUpdateTo VARCHAR(10);
UPDATE configurationapplications
SET showIcon = (SELECT showIcon FROM applications WHERE applications.id = configurationapplications.applicationId);
ALTER TABLE configurationapplications
    ALTER COLUMN showIcon SET DEFAULT FALSE;
ALTER TABLE configurationapplications
    ALTER COLUMN showIcon SET NOT NULL;
ALTER TABLE configurationapplications
    ADD COLUMN action INT NOT NULL DEFAULT 1;
UPDATE configurationapplications
SET action = 2
WHERE remove IS TRUE;
ALTER TABLE applications
    ADD COLUMN runAfterInstall BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE configurationApplications
SET action = 1
WHERE action = 3;



ALTER TABLE settings
    ADD COLUMN columnDisplayedLauncherVersion BOOLEAN;



ALTER TABLE configurationApplicationParameters
    ADD CONSTRAINT cap_config_application_unique UNIQUE (configurationId, applicationId);

DROP FUNCTION IF EXISTS mdm_config_app_upgrade(BIGINT, BIGINT);

CREATE OR REPLACE FUNCTION mdm_config_app_upgrade(configId BIGINT, appId BIGINT)
    RETURNS INT
AS
$$

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
$$
    LANGUAGE 'plpgsql';

ALTER TABLE settings
    ADD CONSTRAINT settings_customer_unique UNIQUE (customerId);

ALTER TABLE userHints
    ADD CONSTRAINT userHints_userId_hintKey_unique UNIQUE (userId, hintKey);


INSERT INTO userHintTypes (hintKey)
VALUES ('hint.step.1');
INSERT INTO userHintTypes (hintKey)
VALUES ('hint.step.2');
INSERT INTO userHintTypes (hintKey)
VALUES ('hint.step.3');
INSERT INTO userHintTypes (hintKey)
VALUES ('hint.step.4');

ALTER TABLE customers
    ADD COLUMN prefix VARCHAR(100);
UPDATE customers
SET prefix = 'e' || id || '-';
ALTER TABLE customers
    ALTER COLUMN prefix SET NOT NULL;
ALTER TABLE customers
    ADD CONSTRAINT customers_prefix_key UNIQUE (prefix);

ALTER TABLE customers
    ADD COLUMN registrationTime BIGINT;
ALTER TABLE customers
    ADD COLUMN lastLoginTime BIGINT;
CREATE INDEX configurations_mainAppId_idx ON configurations (mainAppId);
CREATE INDEX configurations_contentAppId_idx ON configurations (contentAppId);
CREATE INDEX configurations_customerId_idx ON configurations (customerId);
CREATE INDEX devices_configurationId_idx ON devices (configurationId);
CREATE INDEX devices_customerId_idx ON devices (customerId);
CREATE INDEX applications_pkg_idx ON applications (pkg);
CREATE INDEX applications_customerId_idx ON applications (customerId);
CREATE INDEX applicationversionss_applicationId_idx ON applicationVersions (applicationId);
CREATE INDEX devices_groupId_idx ON deviceGroups (groupId);
CREATE INDEX devices_deviceId_idx ON deviceGroups (deviceId);
ALTER TABLE applicationVersions
    ADD COLUMN apkHash VARCHAR(100);
INSERT INTO permissions (name, description, superadmin)
VALUES ('edit_device_desc', 'Иммет доступ к редактированию описания устройства', FALSE);
INSERT INTO userRolePermissions (roleId, permissionId)
SELECT id, CURRVAL('permissions_id_seq')
FROM userRoles;



ALTER TABLE userRoleSettings
    ADD CONSTRAINT userRoleSettings_role_customer_uniq UNIQUE (roleId, customerId);

INSERT INTO userRoleSettings (roleId,
                              customerId,
                              columnDisplayedDeviceStatus,
                              columnDisplayedDeviceDate,
                              columnDisplayedDeviceNumber,
                              columnDisplayedDeviceModel,
                              columnDisplayedDevicePermissionsStatus,
                              columnDisplayedDeviceAppInstallStatus,
                              columnDisplayedDeviceConfiguration,
                              columnDisplayedDeviceImei,
                              columnDisplayedDevicePhone,
                              columnDisplayedDeviceDesc,
                              columnDisplayedDeviceGroup,
                              columnDisplayedLauncherVersion)
SELECT userRoles.id,
       settings.customerId,
       settings.columnDisplayedDeviceStatus,
       settings.columnDisplayedDeviceDate,
       settings.columnDisplayedDeviceNumber,
       settings.columnDisplayedDeviceModel,
       settings.columnDisplayedDevicePermissionsStatus,
       settings.columnDisplayedDeviceAppInstallStatus,
       settings.columnDisplayedDeviceConfiguration,
       settings.columnDisplayedDeviceImei,
       settings.columnDisplayedDevicePhone,
       settings.columnDisplayedDeviceDesc,
       settings.columnDisplayedDeviceGroup,
       settings.columnDisplayedLauncherVersion
FROM userRoles
         INNER JOIN settings ON 1 = 1
WHERE userRoles.superAdmin IS FALSE;

ALTER TABLE settings
    DROP COLUMN columnDisplayedDeviceStatus;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDeviceDate;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDeviceNumber;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDeviceModel;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDevicePermissionsStatus;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDeviceAppInstallStatus;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDeviceConfiguration;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDeviceImei;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDevicePhone;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDeviceDesc;
ALTER TABLE settings
    DROP COLUMN columnDisplayedDeviceGroup;
ALTER TABLE settings
    DROP COLUMN columnDisplayedLauncherVersion;
INSERT INTO permissions (name, description)
VALUES ('edit_device_app_settings', 'Имеет доступ к редактированию и добавлению настроек приложения для устройства');
INSERT INTO userRolePermissions (roleId, permissionId)
SELECT id, currval('permissions_id_seq')
FROM userroles
WHERE name IN ('Администратор', 'Супер-Администратор');
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedBatteryLevel BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN usbStorage BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN requestUpdates VARCHAR(20) NOT NULL DEFAULT 'DONOTTRACK';
UPDATE configurations
SET autoUpdate = FALSE;


CREATE INDEX icons_customerId_idx ON icons (customerId);

ALTER TABLE applications
    ADD COLUMN type VARCHAR(10) NOT NULL DEFAULT 'app';
ALTER TABLE applications
    ADD COLUMN iconText VARCHAR(256);
ALTER TABLE applications
    ADD COLUMN iconId INT REFERENCES icons (id) ON DELETE SET NULL;

ALTER TABLE configurations
    ADD COLUMN pushOptions VARCHAR(20) NOT NULL DEFAULT 'mqttWorker';
ALTER TABLE configurations
    ADD COLUMN autoBrightness BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN brightness INT DEFAULT 180;
ALTER TABLE configurations
    ADD COLUMN manageTimeout BOOLEAN DEFAULT false;
ALTER TABLE configurations
    ADD COLUMN timeout INT DEFAULT 60;
ALTER TABLE configurations
    ADD COLUMN lockVolume BOOLEAN DEFAULT false;
ALTER TABLE configurations
    ADD COLUMN wifiSSID VARCHAR(256);
ALTER TABLE configurations
    ADD COLUMN wifiPassword VARCHAR(256);
ALTER TABLE configurations
    ADD COLUMN wifiSecurityType VARCHAR(16);


CREATE INDEX configurationFiles_configurationId_idx ON configurationFiles (configurationId);
ALTER TABLE configurationFiles
    ALTER COLUMN description DROP NOT NULL;
ALTER TABLE configurationFiles
    DROP COLUMN name;
ALTER TABLE configurationFiles
    ALTER COLUMN checksum DROP NOT NULL;
DROP FUNCTION IF EXISTS mdm_device_permissions_index(TEXT);

CREATE OR REPLACE function mdm_device_permissions_index(device_info text) RETURNS INT
    language plpgsql
as
$$
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

DROP FUNCTION IF EXISTS mdm_resolve_device_property(TEXT, TEXT);

CREATE OR REPLACE FUNCTION mdm_resolve_device_property(server_data TEXT, device_data TEXT) returns TEXT
    LANGUAGE plpgsql
AS
$$
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

DROP FUNCTION IF EXISTS mdm_device_launcher_version(TEXT, TEXT);

CREATE OR REPLACE FUNCTION mdm_device_launcher_version(launcherAppPkg text, device_info text) returns TEXT
    LANGUAGE plpgsql
AS
$$
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



ALTER TABLE configurations
    ADD COLUMN passwordMode VARCHAR(50);

ALTER TABLE settings
    ADD COLUMN createNewDevices BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE settings
    ADD COLUMN newDeviceGroupId INT;
ALTER TABLE settings
    ADD COLUMN newDeviceConfigurationId INT;



ALTER TABLE settings
    ADD COLUMN phoneNumberFormat VARCHAR(50) DEFAULT '+9 (999) 999-99-99';
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedDeviceFilesStatus BOOLEAN;
ALTER TABLE applications
    ADD COLUMN runAtBoot BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE devices
    ADD COLUMN imeiUpdateTs BIGINT;
ALTER TABLE configurations
    ADD COLUMN kioskHome BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN kioskRecents BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN kioskNotifications BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN kioskSystemInfo BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN kioskKeyguard BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN orientation INT;
ALTER TABLE configurationapplications
    ADD COLUMN screenOrder INT;
ALTER TABLE configurations
    ADD COLUMN runDefaultLauncher BOOLEAN;
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedDefaultLauncher BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN timeZone VARCHAR(200);
ALTER TABLE configurations
    ADD COLUMN allowedClasses TEXT;
ALTER TABLE configurations
    ADD COLUMN newServerUrl TEXT;
ALTER TABLE configurationapplications
    ADD COLUMN keyCode INT;
ALTER TABLE configurations
    ADD COLUMN lockSafeSettings BOOLEAN;
ALTER TABLE settings
    ADD COLUMN customPropertyName1 VARCHAR(200);
ALTER TABLE settings
    ADD COLUMN customPropertyName2 VARCHAR(200);
ALTER TABLE settings
    ADD COLUMN customPropertyName3 VARCHAR(200);
ALTER TABLE devices
    ADD COLUMN custom1 TEXT;
ALTER TABLE devices
    ADD COLUMN custom2 TEXT;
ALTER TABLE devices
    ADD COLUMN custom3 TEXT;
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedCustom1 BOOLEAN;
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedCustom2 BOOLEAN;
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedCustom3 BOOLEAN;
ALTER TABLE customers
    ADD COLUMN accountType INT NOT NULL DEFAULT 0;
ALTER TABLE customers
    ADD COLUMN expiryTime BIGINT;
ALTER TABLE customers
    ADD COLUMN deviceLimit INT NOT NULL DEFAULT 3;
ALTER TABLE configurations
    ADD COLUMN disableScreenshots BOOLEAN;
ALTER TABLE customers
    ADD COLUMN customerStatus VARCHAR(100);
ALTER TABLE applications
    ADD COLUMN useKiosk BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE devices
    ADD COLUMN oldNumber VARCHAR(100);
ALTER TABLE configurations
    ADD COLUMN restrictions TEXT;
ALTER TABLE configurationApplications
    ADD COLUMN bottom BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE configurations
    ADD COLUMN defaultFilePath TEXT NOT NULL DEFAULT '/';
ALTER TABLE users
    ADD COLUMN allConfigAvailable BOOLEAN NOT NULL DEFAULT TRUE;



INSERT INTO permissions (id, name, description, superadmin)
VALUES (5, 'add_config', 'Add new empty configurations', FALSE);
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (1, 5);
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (2, 5);

INSERT INTO permissions (id, name, description, superadmin)
VALUES (6, 'copy_config', 'Duplicate/copy configurations', FALSE);
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (1, 6);
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (2, 6);
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (3, 6);
ALTER TABLE configurations
    ADD COLUMN keepaliveTime INT;
ALTER TABLE settings
    ADD COLUMN customMultiline1 BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE settings
    ADD COLUMN customMultiline2 BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE settings
    ADD COLUMN customMultiline3 BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE settings
    ADD COLUMN customSend1 BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE settings
    ADD COLUMN customSend2 BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE settings
    ADD COLUMN customSend3 BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE configurationFiles
    ADD COLUMN replaceVariables BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE configurations
    ADD COLUMN manageVolume BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN volume INT;
ALTER TABLE applicationVersions
    ADD COLUMN split BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE applicationVersions
    ADD COLUMN urlArmeabi TEXT;
ALTER TABLE applicationVersions
    ADD COLUMN urlArm64 TEXT;
ALTER TABLE configurations
    ADD COLUMN showWifi BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN mobileEnrollment BOOLEAN NOT NULL DEFAULT false;
INSERT INTO permissions (name, description)
VALUES ('push_api', 'Send Push messages to devices via REST API');
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (1, currval('permissions_id_seq'));
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (2, currval('permissions_id_seq'));
INSERT INTO userRolePermissions (roleId, permissionId)
VALUES (3, currval('permissions_id_seq'));
ALTER TABLE settings
    ADD COLUMN desktopHeaderTemplate TEXT;
ALTER TABLE settings
    ADD COLUMN sendDescription BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE configurations
    ADD COLUMN desktopHeaderTemplate TEXT;
ALTER TABLE configurations
    ADD COLUMN kioskLockButtons BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN scheduleAppUpdate BOOLEAN;
ALTER TABLE configurations
    ADD COLUMN appUpdateFrom VARCHAR(10);
ALTER TABLE configurations
    ADD COLUMN appUpdateTo VARCHAR(10);
ALTER TABLE settings
    ADD COLUMN passwordReset BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE settings
    ADD COLUMN passwordLength INT NOT NULL DEFAULT 0;
ALTER TABLE settings
    ADD COLUMN passwordStrength INT NOT NULL DEFAULT 0;
ALTER TABLE users
    ADD COLUMN passwordReset BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE customers
    ADD COLUMN email varchar(50);
ALTER TABLE users
    ALTER COLUMN password TYPE varchar(40);
ALTER TABLE users
    ADD COLUMN authToken varchar(40);
ALTER TABLE users
    ADD COLUMN passwordResetToken varchar(40);
ALTER TABLE devices
    ADD COLUMN fastSearch VARCHAR(100);
CREATE INDEX devices_number_idx ON devices (number);
CREATE INDEX devices_fastSearch_idx ON devices (fastSearch);
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedMdmMode BOOLEAN;
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedKioskMode BOOLEAN;
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedAndroidVersion BOOLEAN;
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedEnrollmentDate BOOLEAN;
ALTER TABLE userRoleSettings
    ADD COLUMN columnDisplayedSerial BOOLEAN;
ALTER TABLE devices
    ADD COLUMN IF NOT EXISTS enrollTime BIGINT;
ALTER TABLE applicationVersions
    ADD COLUMN versionCode INT NOT NULL DEFAULT 0;
ALTER TABLE configurations
    ADD COLUMN disableLocation BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE configurations
    ADD COLUMN appPermissions VARCHAR(20) NOT NULL DEFAULT 'GRANTALL';



UPDATE userroles
SET name='Super-Admin',
    description='Can sign in as any user. In shared mode, manages corporate accounts'
WHERE id = 1;
UPDATE userroles
SET name='Admin',
    description='Full access to the control panel'
WHERE id = 2;
UPDATE userroles
SET name='User',
    description='Limited access to the control panel'
WHERE id = 3;
UPDATE userroles
SET name='Observer',
    description='Read-only access to the control panel'
WHERE id = 100;

UPDATE users
SET email='_ADMIN_EMAIL_',
    passwordReset= true,
    passwordResetToken=md5(random()::text)
WHERE id = 1;

UPDATE groups
SET name='General'
WHERE id = 1;

UPDATE permissions
SET description='Super-administrator functions for the whole system'
WHERE id = 1;
UPDATE permissions
SET description='Access to system settings'
WHERE id = 2;
UPDATE permissions
SET description='Access to configurations, applications and files'
WHERE id = 3;
UPDATE permissions
SET description='Access to devices'
WHERE id = 4;
UPDATE permissions
SET description='Access to image removal (image plugin)'
WHERE id = 100;

UPDATE plugins
SET name='Images',
    description='Retrieve images from devices'
WHERE id = 1;

INSERT INTO settings (id, backgroundcolor, textcolor, backgroundimageurl, iconsize, desktopheader, customerid,
                      usedefaultlanguage, language)
VALUES (1, '#1c40e3', '#fcfcfc', NULL, 'SMALL', 'NO_HEADER', 1, true, NULL);

INSERT INTO userrolesettings (id, roleid, customerid, columndisplayeddevicestatus, columndisplayeddevicedate,
                              columndisplayeddevicenumber, columndisplayeddevicemodel,
                              columndisplayeddevicepermissionsstatus, columndisplayeddeviceappinstallstatus,
                              columndisplayeddeviceconfiguration, columndisplayeddeviceimei, columndisplayeddevicephone,
                              columndisplayeddevicedesc, columndisplayeddevicegroup, columndisplayedlauncherversion)
VALUES (1, 1, 1, true, true, true, NULL, true, true, true, NULL, NULL, NULL, NULL, NULL),
       (2, 2, 1, true, true, true, NULL, true, true, true, NULL, NULL, NULL, NULL, NULL),
       (3, 3, 1, true, true, true, NULL, true, true, true, NULL, NULL, NULL, NULL, NULL),
       (4, 100, 1, true, true, true, NULL, true, true, true, NULL, NULL, NULL, NULL, NULL);

SELECT pg_catalog.setval('public.settings_id_seq', 1, true);

ALTER TABLE applications
    DROP CONSTRAINT applications_latestversion_fkey;

INSERT INTO applications (id, pkg, name, showicon, customerid, system, latestversion, runafterinstall)
VALUES (1, 'com.android.systemui', 'System UI', false, 1, true, 10000, false),
       (2, 'com.android.bluetooth', 'Bluetooth Service', false, 1, true, 10001, false),
       (3, 'com.google.android.gms', 'Google Services', false, 1, true, 10002, false),
       (34, 'com.android.email', 'Email client', true, 1, true, 10033, false),
       (9, 'com.android.chrome', 'Chrome Browser', true, 1, true, 10008, false),
       (10, 'com.sec.android.app.browser', 'Browser (Samsung)', true, 1, true, 10009, false),
       (11, 'com.samsung.android.video', 'Samsung Video', false, 1, true, 10010, false),
       (12, 'com.android.providers.media', 'Media Service', false, 1, true, 10011, false),
       (13, 'com.android.gallery3d', 'Gallery', true, 1, true, 10012, false),
       (14, 'com.sec.android.gallery3d', 'Gallery (Samsung)', true, 1, true, 10013, false),
       (15, 'com.android.vending', 'Google Payment support', false, 1, true, 10014, false),
       (16, 'com.samsung.android.app.memo', 'Notes (Samsung)', true, 1, true, 10015, false),
       (35, 'com.android.documentsui', 'File manager extension', false, 1, true, 10034, false),
       (5, 'com.google.android.packageinstaller', 'Package installer (Google)', false, 1, true, 10004, false),
       (17, 'com.android.packageinstaller', 'Package Installer', false, 1, true, 10016, false),
       (18, 'com.samsung.android.calendar', 'Calendar (Samsung)', true, 1, true, 10017, false),
       (19, 'com.android.calculator2', 'Calculator (generic)', true, 1, true, 10018, false),
       (20, 'com.sec.android.app.popupcalculator', 'Calculator (Samsung)', true, 1, true, 10019, false),
       (21, 'com.android.camera', 'Camera (generic)', true, 1, true, 10020, false),
       (22, 'com.huawei.camera', 'Camera (Huawei)', true, 1, true, 10021, false),
       (23, 'org.codeaurora.snapcam', 'Camera (Lenovo)', true, 1, true, 10022, false),
       (24, 'com.mediatek.camera', 'Camera (Mediatek)', true, 1, true, 10023, false),
       (25, 'com.sec.android.app.camera', 'Camera (Samsung, legacy)', true, 1, true, 10024, false),
       (26, 'com.sec.android.camera', 'Camera (Samsung)', true, 1, true, 10025, false),
       (27, 'com.google.android.apps.maps', 'Google Maps', true, 1, true, 10026, false),
       (28, 'com.touchtype.swiftkey', 'Swiftkey keyboard extension', false, 1, true, 10027, false),
       (29, 'com.android.contacts', 'Contacts', true, 1, true, 10028, false),
       (31, 'com.sec.android.app.myfiles', 'File Manager (Samsung)', true, 1, true, 10030, false),
       (32, 'com.android.settings', 'Settings (usually must be disabled!)', false, 1, true, 10031, false),
       (33, 'com.sec.android.inputmethod', 'Keyboard settings (Samsung)', false, 1, true, 10032, false),
       (36, 'com.samsung.android.email.provider', 'Email service (Samsung)', false, 1, true, 10035, false),
       (37, 'android', 'Android system package', false, 1, true, 10036, false),
       (38, 'com.android.mms', 'Messaging (generic)', true, 1, true, 10037, false),
       (39, 'com.google.android.apps.messaging', 'Messaging (Google)', true, 1, true, 10038, false),
       (40, 'com.android.dialer', 'Phone (generic UI)', true, 1, true, 10039, false),
       (41, 'com.sec.phone', 'Phone (Samsung)', true, 1, true, 10040, false),
       (42, 'com.android.phone', 'Phone (generic service)', true, 1, true, 10041, false),
       (43, 'com.huaqin.filemanager', 'File manager (Lenovo)', true, 1, true, 10042, false),
       (6, 'com.google.android.apps.photos', 'Gallery (Google)', true, 1, true, 10005, false),
       (4, 'com.google.android.apps.docs', 'Google Drive', true, 1, true, 10003, false),
       (30, 'com.huawei.android.launcher', 'Default launcher (Huawei)', false, 1, true, 10029, false),
       (8, 'com.android.browser', 'Browser (generic)', true, 1, true, 10007, false),
       (46, 'com.hmdm.launcher', 'Headwind MDM', false, 1, false, 10045, false),
       (47, 'com.huawei.android.internal.app', 'Huawei Launcher Selector', false, 1, true, 10046, false),
       (48, 'com.hmdm.pager', 'Headwind MDM Pager Plugin', true, 1, false, 10047, false),
       (49, 'com.hmdm.phoneproxy', 'Dialer Helper', true, 1, false, 10048, false),
       (50, 'com.hmdm.emuilauncherrestarter', 'Headwind MDM update helper', false, 1, false, 10049, false),
       (51, 'com.miui.cleanmaster', 'MIUI Clean Master', false, 1, true, 10050, false),
       (52, 'com.miui.gallery', 'MIUI Gallery', true, 1, true, 10051, false),
       (53, 'com.miui.notes', 'MIUI Notes', true, 1, true, 10052, false),
       (54, 'com.miui.global.packageinstaller', 'MIUI Package Installer', false, 1, true, 10053, false),
       (55, 'com.miui.msa.global', 'MIUI Permissions Manager', false, 1, true, 10054, false),
       (56, 'com.miui.securitycenter', 'MIUI Security Center', false, 1, true, 10055, false),
       (57, 'com.xiaomi.discover', 'Xiaomi Updater', false, 1, true, 10056, false),
       (58, 'com.google.android.permissioncontroller', 'Permission Controller', false, 1, true, 10057, false),
       (59, 'com.samsung.accessibility', 'Samsung Accessibility', false, 1, true, 10058, false),
       (60, 'com.android.updater', 'System Update Service', false, 1, true, 10059, false),
       (61, 'com.android.printspooler', 'Print Service', false, 1, true, 10060, false),
       (62, 'com.google.android.documentsui', 'File Manager Extension (Google)', false, 1, true, 10061, false),
       (63, 'com.google.android.contacts', 'Contacts (Google)', true, 1, true, 10062, false),
       (64, 'com.google.android.dialer', 'Dialer (Google)', true, 1, true, 10063, false),
       (65, 'com.samsung.android.app.notes', 'Samsung Notes', true, 1, true, 10064, false),
       (66, 'com.hmdglobal.camera2', 'Nokia Camera (new)', true, 1, true, 10065, false),
       (67, 'com.hmdglobal.app.camera', 'Nokia Camera', true, 1, true, 10066, false),
       (68, 'com.samsung.android.dialer', 'Samsung Dialer', true, 1, true, 10067, false),
       (69, 'com.samsung.android.app.contacts', 'Samsung Contacts', true, 1, true, 10068, false),
       (70, 'com.samsung.android.messaging', 'Samsung Messaging', false, 1, true, 10069, false),
       (71, 'com.sec.android.app.launcher', 'Samsung Launcher (for Recents)', false, 1, true, 10070, false),
       (72, 'com.google.android.apps.photos', 'Photos (Google)', true, 1, true, 10071, false),
       (73, 'com.google.android.apps.nbu.files', 'File Manager (Google)', true, 1, true, 10072, false),
       (74, 'com.android.settings.intelligence', 'Samsung Search Settings', false, 1, true, 10073, false),
       (75, 'com.huawei.bluetooth', 'Huawei Bluetooth', false, 1, true, 10074, false),
       (76, 'com.google.android.gms.setup', 'Google Services Setup', false, 1, true, 10075, false),
       (77, 'com.samsung.android.app.telephonyui', 'Samsung Telephony', false, 1, true, 10076, false);

SELECT pg_catalog.setval('public.applications_id_seq', 77, true);

INSERT INTO applicationversions (id, applicationid, version, url)
VALUES (10000, 1, '0', NULL),
       (10001, 2, '0', NULL),
       (10002, 3, '0', NULL),
       (10003, 4, '0', NULL),
       (10004, 5, '0', NULL),
       (10005, 6, '0', NULL),
       (10007, 8, '0', NULL),
       (10008, 9, '0', NULL),
       (10009, 10, '0', NULL),
       (10010, 11, '0', NULL),
       (10011, 12, '0', NULL),
       (10012, 13, '0', NULL),
       (10013, 14, '0', NULL),
       (10014, 15, '0', NULL),
       (10015, 16, '0', NULL),
       (10016, 17, '0', NULL),
       (10017, 18, '0', NULL),
       (10018, 19, '0', NULL),
       (10019, 20, '0', NULL),
       (10020, 21, '0', NULL),
       (10021, 22, '0', NULL),
       (10022, 23, '0', NULL),
       (10023, 24, '0', NULL),
       (10024, 25, '0', NULL),
       (10025, 26, '0', NULL),
       (10026, 27, '0', NULL),
       (10027, 28, '0', NULL),
       (10028, 29, '0', NULL),
       (10029, 30, '0', NULL),
       (10030, 31, '0', NULL),
       (10031, 32, '0', NULL),
       (10032, 33, '0', NULL),
       (10033, 34, '0', NULL),
       (10034, 35, '0', NULL),
       (10035, 36, '0', NULL),
       (10036, 37, '0', NULL),
       (10037, 38, '0', NULL),
       (10038, 39, '0', NULL),
       (10039, 40, '0', NULL),
       (10040, 41, '0', NULL),
       (10041, 42, '0', NULL),
       (10042, 43, '0', NULL),
       (10045, 46, '_HMDM_VERSION_', 'https://h-mdm.com/files/_HMDM_APK_'),
       (10046, 47, '0', NULL),
       (10047, 48, '1.02', 'https://h-mdm.com/files/pager-1.02.apk'),
       (10048, 49, '1.02', 'https://h-mdm.com/files/phoneproxy-1.02.apk'),
       (10049, 50, '1.04', 'https://h-mdm.com/files/LauncherRestarter-1.04.apk'),
       (10050, 51, '0', NULL),
       (10051, 52, '0', NULL),
       (10052, 53, '0', NULL),
       (10053, 54, '0', NULL),
       (10054, 55, '0', NULL),
       (10055, 56, '0', NULL),
       (10056, 57, '0', NULL),
       (10057, 58, '0', NULL),
       (10058, 59, '0', NULL),
       (10059, 60, '0', NULL),
       (10060, 61, '0', NULL),
       (10061, 62, '0', NULL),
       (10062, 63, '0', NULL),
       (10063, 64, '0', NULL),
       (10064, 65, '0', NULL),
       (10065, 66, '0', NULL),
       (10066, 67, '0', NULL),
       (10067, 68, '0', NULL),
       (10068, 69, '0', NULL),
       (10069, 70, '0', NULL),
       (10070, 71, '0', NULL),
       (10071, 72, '0', NULL),
       (10072, 73, '0', NULL),
       (10073, 74, '0', NULL),
       (10074, 75, '0', NULL),
       (10075, 76, '0', NULL),
       (10076, 77, '0', NULL);

SELECT pg_catalog.setval('public.applicationversions_id_seq', 10076, true);

ALTER TABLE applications
    ADD CONSTRAINT applications_latestversion_fkey FOREIGN KEY (latestversion) REFERENCES applicationversions (id) ON DELETE SET NULL;

DELETE
FROM configurations;
INSERT INTO configurations (id, name, description, type, password, backgroundcolor, textcolor, backgroundimageurl,
                            iconsize, desktopheader, usedefaultdesignsettings, customerid, gps, bluetooth, wifi,
                            mobiledata, mainappid, eventreceivingcomponent, kioskmode, qrcodekey, contentappid,
                            autoupdate, blockstatusbar, systemupdatetype, systemupdatefrom, systemupdateto, pushoptions,
                            keepalivetime)
VALUES (1, 'Common - Minimal', 'Suitable for generic Android devices; minimum of apps installed', 0, '12345678', '', '',
        NULL, 'SMALL', 'NO_HEADER', true, 1, NULL, NULL, NULL, NULL, 10045, 'com.hmdm.launcher.AdminReceiver', false,
        '6fb9c8dc81483173a0c0e9f8b2e46be1', NULL, false, false, 0, NULL, NULL, 'mqttAlarm', 300),
       (2, 'MIUI (Xiaomi Redmi)', 'Optimized for MIUI-running devices', 0, '12345678', '', '', NULL, 'SMALL',
        'NO_HEADER', true, 1, NULL, NULL, NULL, NULL, 10045, 'com.hmdm.launcher.AdminReceiver', false,
        '8e6ca072ddb926a1af61578dfa9fc334', NULL, false, false, 0, NULL, NULL, 'mqttAlarm', 300);

SELECT pg_catalog.setval('public.configurations_id_seq', 2, true);

INSERT INTO configurationapplications (id, configurationid, applicationid, remove, showicon, applicationversionid)
VALUES (2, 1, 8, false, true, 10007),
       (3, 1, 37, false, false, 10036),
       (4, 1, 2, false, false, 10001),
       (5, 1, 10, false, true, 10009),
       (6, 1, 19, false, false, 10018),
       (7, 1, 20, false, false, 10019),
       (8, 1, 18, false, false, 10017),
       (9, 1, 21, false, true, 10020),
       (10, 1, 22, false, true, 10021),
       (11, 1, 23, false, true, 10022),
       (12, 1, 24, false, true, 10023),
       (13, 1, 26, false, true, 10025),
       (14, 1, 25, false, true, 10024),
       (15, 1, 9, false, true, 10008),
       (16, 1, 29, false, true, 10028),
       (17, 1, 30, false, false, 10029),
       (18, 1, 34, false, true, 10033),
       (19, 1, 36, false, false, 10035),
       (20, 1, 35, false, false, 10034),
       (21, 1, 43, false, false, 10042),
       (22, 1, 31, false, false, 10030),
       (23, 1, 13, false, false, 10012),
       (24, 1, 6, false, false, 10005),
       (25, 1, 14, false, false, 10013),
       (26, 1, 4, false, false, 10003),
       (27, 1, 27, false, false, 10026),
       (28, 1, 15, false, false, 10014),
       (29, 1, 3, false, false, 10002),
       (30, 1, 33, false, false, 10032),
       (31, 1, 12, false, false, 10011),
       (32, 1, 38, false, true, 10037),
       (33, 1, 39, false, true, 10038),
       (34, 1, 16, false, false, 10015),
       (35, 1, 5, false, false, 10004),
       (36, 1, 17, false, false, 10016),
       (37, 1, 42, false, true, 10041),
       (38, 1, 40, false, true, 10039),
       (39, 1, 41, false, true, 10040),
       (40, 1, 11, false, false, 10010),
       (41, 1, 28, false, false, 10027),
       (42, 1, 1, false, false, 10000),
       (43, 1, 46, false, false, 10045),
       (44, 1, 47, false, false, 10046),
       (45, 1, 48, false, true, 10047),
       (46, 1, 50, false, false, 10049),
       (48, 2, 8, false, true, 10007),
       (49, 2, 37, false, false, 10036),
       (50, 2, 2, false, false, 10001),
       (51, 2, 21, false, true, 10020),
       (52, 2, 9, false, true, 10008),
       (53, 2, 29, false, true, 10028),
       (54, 2, 34, false, true, 10033),
       (55, 2, 35, false, false, 10034),
       (56, 2, 13, false, false, 10012),
       (57, 2, 6, false, false, 10005),
       (58, 2, 4, false, false, 10003),
       (59, 2, 27, false, false, 10026),
       (60, 2, 15, false, false, 10014),
       (61, 2, 3, false, false, 10002),
       (62, 2, 12, false, false, 10011),
       (63, 2, 38, false, true, 10037),
       (64, 2, 39, false, true, 10038),
       (65, 2, 5, false, false, 10004),
       (66, 2, 17, false, false, 10016),
       (67, 2, 42, false, true, 10041),
       (68, 2, 40, false, true, 10039),
       (69, 2, 28, false, false, 10027),
       (70, 2, 1, false, false, 10000),
       (71, 2, 46, false, false, 10045),
       (72, 2, 48, false, true, 10047),
       (73, 2, 49, false, true, 10048),
       (74, 2, 50, false, false, 10049),
       (75, 2, 51, false, false, 10050),
       (76, 2, 52, false, false, 10051),
       (77, 2, 53, false, false, 10052),
       (78, 2, 54, false, false, 10053),
       (79, 2, 55, false, false, 10054),
       (80, 2, 56, false, false, 10055),
       (81, 2, 57, false, false, 10056),
       (82, 1, 59, false, false, 10058),
       (83, 1, 60, false, false, 10059),
       (84, 1, 61, false, false, 10060),
       (85, 1, 62, false, false, 10061),
       (86, 1, 63, false, true, 10062),
       (87, 1, 64, false, true, 10063),
       (88, 1, 66, false, true, 10065),
       (89, 1, 67, false, true, 10066),
       (90, 1, 68, false, true, 10067),
       (91, 1, 69, false, true, 10068),
       (92, 1, 71, false, false, 10070),
       (93, 1, 74, false, false, 10073),
       (94, 1, 75, false, false, 10074),
       (95, 1, 76, false, false, 10075),
       (96, 1, 77, false, false, 10076);

SELECT pg_catalog.setval('public.configurationapplications_id_seq', 96, true);

INSERT INTO devices (id, number, description, lastupdate, configurationid, oldconfigurationid, info, imei, phone,
                     customerid)
VALUES (1, 'h0001', 'My first Android device', 0, 1, NULL, NULL, NULL, NULL, 1);

SELECT pg_catalog.setval('public.devices_id_seq', 1, true);

INSERT INTO plugin_devicelog_settings_rules (id, settingid, name, active, applicationid, severity)
VALUES (1, 1, 'Headwind MDM', true, 46, 'VERBOSE');
SELECT pg_catalog.setval('plugin_devicelog_settings_rules_id_seq', 1, true);

INSERT INTO plugin_deviceinfo_settings (id, customerid, datapreserveperiod, senddata, intervalmins)
VALUES (1, 1, 30, true, 15);
SELECT pg_catalog.setval('plugin_deviceinfo_settings_id_seq', 1, true);

INSERT INTO users (login, email, name, password, customerId, userRoleId) VALUES ('avi3s', 'avirup.pal@gmail.com', 'Avirup Pal', '4FCB896DD168BC377EABB01E34D2AB0CF77FE82C', '2', '1');