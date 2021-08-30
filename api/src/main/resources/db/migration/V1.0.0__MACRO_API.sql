CREATE TABLE MACRO_SAGA
(
    SAGA_ID     RAW(16)              NOT NULL,
    MACRO_ID    RAW(16),
    SAGA_NAME   VARCHAR2(50)         NOT NULL,
    SAGA_STATE  VARCHAR2(100)        NOT NULL,
    PAYLOAD     BLOB                 NOT NULL,
    STATUS      VARCHAR2(20)         NOT NULL,
    RETRY_COUNT NUMBER,
    CREATE_USER VARCHAR2(32)         NOT NULL,
    CREATE_DATE DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER VARCHAR2(32)         NOT NULL,
    UPDATE_DATE DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT MACRO_SAGA_PK PRIMARY KEY (SAGA_ID)
)
LOB (PAYLOAD) STORE AS PAYLOAD (TABLESPACE API_MACRO_BLOB_DATA);

CREATE INDEX MACRO_SAGA_STATUS_IDX ON MACRO_SAGA (STATUS);
CREATE INDEX MACRO_SAGA_MACRO_ID_IDX ON MACRO_SAGA (MACRO_ID);

ALTER INDEX API_MACRO.MACRO_SAGA_PK REBUILD TABLESPACE API_MACRO_IDX;
ALTER INDEX API_MACRO.MACRO_SAGA_STATUS_IDX REBUILD TABLESPACE API_MACRO_IDX;
ALTER INDEX API_MACRO.MACRO_SAGA_MACRO_ID_IDX REBUILD TABLESPACE API_MACRO_IDX;


CREATE TABLE MACRO_SAGA_EVENT_STATES
(
    SAGA_EVENT_ID       RAW(16)              NOT NULL,
    SAGA_ID             RAW(16)              NOT NULL,
    SAGA_EVENT_STATE    VARCHAR2(100)        NOT NULL,
    SAGA_EVENT_OUTCOME  VARCHAR2(100)        NOT NULL,
    SAGA_STEP_NUMBER    NUMBER(4)            NOT NULL,
    SAGA_EVENT_RESPONSE BLOB                 NOT NULL,
    CREATE_USER         VARCHAR2(32)         NOT NULL,
    CREATE_DATE         DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER         VARCHAR2(32)         NOT NULL,
    UPDATE_DATE         DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT MACRO_SAGA_EVENT_STATES_PK PRIMARY KEY (SAGA_EVENT_ID)
)
LOB (SAGA_EVENT_RESPONSE) STORE AS SAGA_EVENT_RESPONSE (TABLESPACE API_MACRO_BLOB_DATA);

ALTER TABLE MACRO_SAGA_EVENT_STATES
    ADD CONSTRAINT MACRO_SAGA_EVENT_STATES_SAGA_ID_FK FOREIGN KEY (SAGA_ID) REFERENCES MACRO_SAGA (SAGA_ID);
ALTER INDEX API_MACRO.MACRO_SAGA_EVENT_STATES_PK REBUILD TABLESPACE API_MACRO_IDX;

CREATE TABLE MACRO_EVENT
(
    EVENT_ID      RAW(16)                             NOT NULL,
    EVENT_PAYLOAD BLOB                                NOT NULL,
    EVENT_STATUS  VARCHAR2(50)                        NOT NULL,
    EVENT_TYPE    VARCHAR2(100)                       NOT NULL,
    SAGA_ID       RAW(16),
    EVENT_OUTCOME VARCHAR2(100)                       NOT NULL,
    REPLY_CHANNEL VARCHAR2(100),
    CREATE_USER   VARCHAR(32)                         NOT NULL,
    CREATE_DATE   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATE_USER   VARCHAR(32)                         NOT NULL,
    UPDATE_DATE   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT MACRO_EVENT_PK PRIMARY KEY (EVENT_ID)
)
LOB (EVENT_PAYLOAD) STORE AS EVENT_PAYLOAD (TABLESPACE API_MACRO_BLOB_DATA);

CREATE INDEX MACRO_EVENT_EVENT_STATUS_IDX ON MACRO_EVENT (EVENT_STATUS);
CREATE INDEX MACRO_EVENT_EVENT_TYPE_IDX ON MACRO_EVENT (EVENT_TYPE);

ALTER INDEX API_MACRO.MACRO_EVENT_PK REBUILD TABLESPACE API_MACRO_IDX;
ALTER INDEX API_MACRO.MACRO_EVENT_EVENT_STATUS_IDX REBUILD TABLESPACE API_MACRO_IDX;
ALTER INDEX API_MACRO.MACRO_EVENT_EVENT_TYPE_IDX REBUILD TABLESPACE API_MACRO_IDX;

CREATE TABLE MACRO_SHEDLOCK
(
    NAME       VARCHAR(64),
    LOCK_UNTIL TIMESTAMP(3) NULL,
    LOCKED_AT  TIMESTAMP(3) NULL,
    LOCKED_BY  VARCHAR(255),
    CONSTRAINT MACRO_SHEDLOCK_PK PRIMARY KEY (NAME)
);
COMMENT ON TABLE MACRO_SHEDLOCK IS 'This table is used to achieve distributed lock between pods, for schedulers.';
ALTER INDEX API_MACRO.MACRO_SHEDLOCK_PK REBUILD TABLESPACE API_MACRO_IDX;

CREATE TABLE MACRO
(
    MACRO_ID                       RAW(16)              NOT NULL,
    MACRO_CODE                     VARCHAR2(10)         NOT NULL,
    MACRO_TEXT                     VARCHAR2(4000)       NOT NULL,
    BUSINESS_USE_TYPE_CODE         VARCHAR2(10)         NOT NULL,
    MACRO_TYPE_CODE                VARCHAR2(10)         NOT NULL,
    CREATE_USER                    VARCHAR2(32)         NOT NULL,
    CREATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                    VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT MACRO_PK PRIMARY KEY (MACRO_ID)
);
COMMENT ON TABLE MACRO IS 'List of text macros used as standard messages by PEN Staff when completing tasks.';
COMMENT ON COLUMN MACRO.MACRO_ID IS 'Unique surrogate key for each macro.';
COMMENT ON COLUMN MACRO.MACRO_CODE IS 'A short text string that identifies the macro and when identified will be replaced by the macro text.';
COMMENT ON COLUMN MACRO.MACRO_TEXT IS 'A standard text string that will be substituted for the macro code by the application.';
COMMENT ON COLUMN MACRO.MACRO_TYPE_CODE IS 'A code value indicating the type, or class, of the text macro.';
COMMENT ON COLUMN MACRO.BUSINESS_USE_TYPE_CODE IS 'A code value indicating the business use type of the text macro.';

ALTER TABLE MACRO
    ADD CONSTRAINT MACRO_CODE_MACRO_TYPE_CODE_BUSINESS_USE_TYPE_CODE_UK UNIQUE (MACRO_CODE, BUSINESS_USE_TYPE_CODE, MACRO_TYPE_CODE);

ALTER INDEX API_MACRO.MACRO_PK REBUILD TABLESPACE API_MACRO_IDX;
ALTER INDEX API_MACRO.MACRO_CODE_MACRO_TYPE_CODE_BUSINESS_USE_TYPE_CODE_UK REBUILD TABLESPACE API_MACRO_IDX;


-- PEN Retrieval Request Macros
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'PCN',
        'A PEN number can not be located using the information in your PEN request.' || CHR(10) || CHR(10) ||
        'Please provide all other given names or surnames you have previously used or advise if you have never used any other names.',
        'MOREINFO', 'GMP', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'PID',
        'To continue with your PEN request upload an IMG or PDF of your current Government Issued photo Identification (ID).' ||
        CHR(10) || CHR(10) ||
        'NOTE: If the name listed on the ID you upload is different from what''s in the PEN system, we will update our data to match. ID is covered by the B.C. Freedom of Information Protection of Privacy.',
        'MOREINFO', 'GMP', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'SOA',
        'To continue with your PEN request please confirm the last B.C. Schools you attended or graduated from, including any applications to B.C. Post Secondary Institutions',
        'MOREINFO', 'GMP', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'MID',
        'You have not declared any middle names. Please provide all other given names or middle names that you may have previously used or advise if you have never used any other given names.',
        'MOREINFO', 'GMP', 'IDIR/JOCOX', to_date('2021-04-28 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2021-04-28 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NPF',
        'A PEN number cannot be located using the information in your PEN request.' || CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=74E29C67215B4988ABCD778F453A3129.' ||
        CHR(10) || CHR(10) ||
        'You do not require a PEN for an application to a B.C. school or PSI, a PEN will be assigned upon registration.',
        'REJECT', 'GMP', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'OOP',
        'The information provided in your PEN request indicates you may not have attended a B.C. School or public Post-Secondary Institution (PSI).' ||
        CHR(10) || CHR(10) ||
        'You do not require a PEN for an application to a B.C. school or PSI, a PEN will be assigned upon registration.' ||
        CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=74E29C67215B4988ABCD778F453A3129',
        'REJECT', 'GMP', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'XPR',
        'The identity of the person making the request cannot be confirmed as the same as the PEN owner.' || CHR(10) ||
        CHR(10) ||
        'Under the B.C. Freedom of Information Protection of Privacy Act, the PEN number can only be provided to the person assigned the PEN, that person''s current or future school, or that person''s parent or guardian.' ||
        CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=74E29C67215B4988ABCD778F453A3129',
        'REJECT', 'GMP', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));


INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NME',
        'Based on the information you have provided, we have updated your Legal Name format in the PEN system now.',
        'COMPLETE', 'GMP', 'IDIR/MVILLENE', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NMG',
        'Based on the information you have provided, we have updated your Legal Name format and Gender in the PEN system now.',
        'COMPLETE', 'GMP', 'IDIR/MVILLENE', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'DOB',
        'Based on the information you have provided, we have updated your Date of Birth in the PEN system now.',
        'COMPLETE', 'GMP', 'IDIR/MVILLENE', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));


-- Student Profile Request Macros
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'PCN',
        'Your information cannot be located using the details provided in your request.' || CHR(10) || CHR(10) ||
        'Please provide all other given names or surnames you have previously used or advise if you have never used any other names.',
        'MOREINFO', 'UMP', 'IDIR/MINYANG', to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'PID',
        'To continue with your update request upload an IMG or PDF of your current Government Issued photo Identification (ID).' ||
        CHR(10) || CHR(10) ||
        'NOTE: If the name listed on the ID you upload is different from what''s in the PEN system, we will update our data to match. ID is covered by the B.C. Freedom of Information Protection of Privacy.',
        'MOREINFO', 'UMP', 'IDIR/MINYANG', to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'SOA',
        'To continue with your update request please confirm the last B.C. Schools you attended or graduated from, including any applications to B.C. Post Secondary Institutions',
        'MOREINFO', 'UMP', 'IDIR/MINYANG', to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NPF',
        'Your information cannot be updated using the details in your update request.' || CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=74E29C67215B4988ABCD778F453A3129',
        'REJECT', 'UMP', 'IDIR/MINYANG', to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2021-04-28 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'XPR',
        'The identity of the person making the request cannot be confirmed as the same as the PEN owner.' || CHR(10) ||
        CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=74E29C67215B4988ABCD778F453A3129',
        'REJECT', 'UMP', 'IDIR/MINYANG', to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NME',
        'Based on the information you have provided, we have updated your Legal Name format in the PEN system now.',
        'COMPLETE', 'UMP', 'IDIR/MINYANG', to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NMG',
        'Based on the information you have provided, we have updated your Legal Name format and Gender in the PEN system now.',
        'COMPLETE', 'UMP', 'IDIR/MINYANG', to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'DOB',
        'Based on the information you have provided, we have updated your Date of Birth in the PEN system now.',
        'COMPLETE', 'UMP', 'IDIR/MINYANG', to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2020-06-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));


-- PEN Request Batch Info Request Macros

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'BN', 'Surname and given name appear to be entered backwards, If correct, please confirm by sending legal documents.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'BTO', 'Unable to assign PEN, student appears to be to old.  If correct, please confirm by sending legal documents.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'BTY', 'Unable to assign PEN, birthdate too young for type of institution.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'CB', 'Please confirm the students birthdate with legal documentation and get back to us with the outcome.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'DF', 'This student appears to have a default birthdate entered.  If correct, please confirm by sending legal document.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'DFN', 'First name is duplicated in middle name.  Please remove and resubmit.  If student does not have a legal middle name, please leave the field blank.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'DFS', 'Student appears to have the first name duplicated in surname field. Please correct and resubmit or supply legal documentation to the ministry.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'DLN', 'Student appears to have the same Surname and Middle Name.  Please confirm the information and re-submit.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'DR', 'This record appears in the file more then once', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'DS', 'Student in this file more then once.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'DSN', 'This student appears to be in the system with a different surname. Please contact the ministry to confirm.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'IC', 'Unable to assign PEN, invalid code or characters in record.  Please remove and resubmit.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'ILN', 'Invalid Surname', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'IN', 'You have entered an invalid name in one of the fields.  Please re-submit.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'INF', 'Invalid name formatting. Please fix and resubmit.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'LEG', 'This record does not appear to be entered in your system using legal documentation, please correct your system and resubmit to the ministry to PEN.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'LNF', 'Unable to assign PEN student correct demographics using legal document and resubmit record to the ministry. If correct contact Pens.coordinator@gov.bc.ca', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MAN', 'Legal name appears to be missing or incomplete. If correct, please confirm by sending legal document.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MB', 'Unable to assign PEN, missing or invalid birthdate. Please correct and resubmit.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MDN', 'Possible match found, please contact pens.coordinator@gov.bc.ca with students maiden name or any previously used name.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MFN', 'Unable to assign PEN, missing or invalid first name.  Please correct and resubmit. If correct, please confirm by sending legal document.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MG', 'Unable to assign PEN, missing or invalid gender.  Please correct and resubmit.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MGB', 'Unable to assign PEN, missing or invalid birthdate and gender.  Please correct and resubmit.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MLN', 'Unable to assign PEN, missing or invalid surname.  Please correct and resubmit. If correct, please confirm by sending legal document.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MMN', 'Possible match found, please supply missing middle name to confirm match. If student has no legal middle name, please confirm by sending legal document.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MP', 'Possible match found, please supply postal code to confirm match.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'MSD', 'There appears to be two different students entered in this record, please confirm the legal and usual names in your student admin system and resubmit.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'NAN', 'Please provide the legal Asian name or legal documentation for this student.  There are several identical matches in our system.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'PCB', 'Possible match found, please confirm birthdate.  If correct, please confirm by sending legal document.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'PCG', 'Possible match found, please confirm gender. If correct, please confirm by sending legal document.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'PCN', 'Possible match found, please confirm legal name. If correct, please confirm by sending legal document.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'SIB', 'This student is already in BCeSIS. Please do a restricted query to find the student and admit them to your school. Contact your BCeSIS Lvl 1 for assistance.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'SIS', 'There are two records in BCeSIS for this student.  Contact your BCeSIS L1 support for help in finding the duplicate and deleting the incorrect record.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'TEST', 'This appears to be a test record, please delete from your system.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'TMFN', 'Too many names entered in first name field, please reformat and resubmit.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES (sys_guid(), 'TMUN', 'Too many names enterd in the usual name field.  This field should only contain names that the student wants to be called by.  Legal names should not appear.', 'INFOREQ', 'PENREG', 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX', to_date('2020-10-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

-- Student Merge Macros
INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'MID',
        'Merged Due to Ministry Identified Duplicate.' ,
        'MERGE', 'PENREG', 'IDIR/MINYANG', to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO MACRO (MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE, BUSINESS_USE_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'SID',
        'Merged Due to School Identified Duplicate.' ,
        'MERGE', 'PENREG', 'IDIR/MINYANG', to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MINYANG',
        to_date('2021-07-05 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
