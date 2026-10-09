-- External catalog provenance and FRDB business-boundary repair.
-- Scope: preserves ext_frdb_* raw data; never restores or inserts the legacy
-- five Chinese demonstration records and never invokes init-test-data.sql.
USE drug_safety;

ALTER TABLE drug_info
    ADD COLUMN data_source VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL or FRDB',
    ADD COLUMN external_batch_id BIGINT DEFAULT NULL,
    ADD COLUMN external_compound_id BIGINT DEFAULT NULL,
    ADD COLUMN external_unii VARCHAR(100) DEFAULT NULL,
    ADD COLUMN source_read_only TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN data_quality_status VARCHAR(32) NOT NULL DEFAULT 'MANUAL';
CREATE INDEX idx_drug_info_source ON drug_info (data_source, source_read_only);
CREATE UNIQUE INDEX uk_drug_info_external_source ON drug_info (data_source, external_batch_id, external_compound_id);

ALTER TABLE adverse_reaction_record
    ADD COLUMN data_source VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL only after boundary repair';
CREATE INDEX idx_adverse_reaction_source ON adverse_reaction_record (data_source);

CREATE TABLE IF NOT EXISTS ext_dailymed_import_batch (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_name VARCHAR(100) NOT NULL, source_url TEXT NOT NULL, release_name VARCHAR(255) NOT NULL,
    published_at DATETIME DEFAULT NULL, downloaded_at DATETIME DEFAULT NULL, archive_file VARCHAR(255) DEFAULT NULL,
    official_checksum_type VARCHAR(20) DEFAULT NULL, official_checksum VARCHAR(128) DEFAULT NULL,
    archive_sha256 CHAR(64) DEFAULT NULL, parser_version VARCHAR(64) DEFAULT NULL,
    label_row_count INT NOT NULL DEFAULT 0, product_row_count INT NOT NULL DEFAULT 0, section_row_count INT NOT NULL DEFAULT 0,
    import_status VARCHAR(20) NOT NULL DEFAULT 'STARTED', imported_at DATETIME DEFAULT NULL, notes TEXT DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE KEY uk_dailymed_archive_sha256 (archive_sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Official DailyMed SPL import provenance';

CREATE TABLE IF NOT EXISTS ext_dailymed_spl_label (
    id BIGINT NOT NULL AUTO_INCREMENT, import_batch_id BIGINT NOT NULL, spl_set_id CHAR(36) NOT NULL,
    spl_version VARCHAR(64) NOT NULL, effective_time VARCHAR(32) DEFAULT NULL, product_type VARCHAR(100) DEFAULT NULL,
    labeler_name VARCHAR(500) DEFAULT NULL, source_url TEXT NOT NULL, raw_file_path VARCHAR(500) DEFAULT NULL,
    raw_file_sha256 CHAR(64) DEFAULT NULL, imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE KEY uk_dailymed_label_version (import_batch_id, spl_set_id, spl_version), KEY idx_dailymed_label_set (spl_set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DailyMed SPL label versions';

CREATE TABLE IF NOT EXISTS ext_dailymed_spl_product (
    id BIGINT NOT NULL AUTO_INCREMENT, label_id BIGINT NOT NULL, product_identifier VARCHAR(100) DEFAULT NULL,
    product_name VARCHAR(1000) DEFAULT NULL, active_ingredients TEXT DEFAULT NULL, dosage_form VARCHAR(500) DEFAULT NULL,
    strength_text VARCHAR(1000) DEFAULT NULL, route_text VARCHAR(1000) DEFAULT NULL, package_text TEXT DEFAULT NULL,
    rxcui VARCHAR(32) DEFAULT NULL, imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE KEY uk_dailymed_product (label_id, product_identifier), KEY idx_dailymed_product_rxcui (rxcui),
    CONSTRAINT fk_dailymed_product_label FOREIGN KEY (label_id) REFERENCES ext_dailymed_spl_label(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DailyMed structured product fields';

CREATE TABLE IF NOT EXISTS ext_dailymed_spl_section (
    id BIGINT NOT NULL AUTO_INCREMENT, label_id BIGINT NOT NULL, section_code VARCHAR(64) DEFAULT NULL,
    section_title VARCHAR(500) NOT NULL, section_text LONGTEXT DEFAULT NULL, section_sha256 CHAR(64) DEFAULT NULL,
    imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE KEY uk_dailymed_section (label_id, section_title), KEY idx_dailymed_section_title (section_title),
    CONSTRAINT fk_dailymed_section_label FOREIGN KEY (label_id) REFERENCES ext_dailymed_spl_label(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DailyMed SPL source sections';

CREATE TABLE IF NOT EXISTS ext_frdb_dailymed_candidate (
    id BIGINT NOT NULL AUTO_INCREMENT, frdb_import_batch_id BIGINT NOT NULL, frdb_compound_id BIGINT NOT NULL,
    frdb_compound_name VARCHAR(500) DEFAULT NULL, frdb_unii VARCHAR(100) DEFAULT NULL, standardized_name VARCHAR(1000) DEFAULT NULL,
    rxcui VARCHAR(32) DEFAULT NULL, dailymed_import_batch_id BIGINT DEFAULT NULL, spl_set_id CHAR(36) DEFAULT NULL,
    spl_version VARCHAR(64) DEFAULT NULL, product_identifier VARCHAR(100) DEFAULT NULL, labeler_name VARCHAR(500) DEFAULT NULL,
    candidate_method VARCHAR(40) NOT NULL, candidate_confidence DECIMAL(5,4) DEFAULT NULL, source_url TEXT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE KEY uk_frdb_dailymed_candidate (frdb_import_batch_id, frdb_compound_id, spl_set_id, spl_version, product_identifier),
    KEY idx_frdb_candidate_compound (frdb_import_batch_id, frdb_compound_id), KEY idx_frdb_candidate_rxcui (rxcui)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='All auditable FRDB-to-DailyMed candidates';

CREATE TABLE IF NOT EXISTS ext_frdb_dailymed_mapping (
    id BIGINT NOT NULL AUTO_INCREMENT, frdb_import_batch_id BIGINT NOT NULL, frdb_compound_id BIGINT NOT NULL,
    frdb_compound_name VARCHAR(500) NOT NULL, frdb_unii VARCHAR(100) DEFAULT NULL, standardized_name VARCHAR(1000) DEFAULT NULL,
    mapping_status VARCHAR(20) NOT NULL, match_method VARCHAR(40) NOT NULL, match_confidence DECIMAL(5,4) DEFAULT NULL,
    candidate_count INT NOT NULL DEFAULT 0, dailymed_import_batch_id BIGINT DEFAULT NULL, spl_set_id CHAR(36) DEFAULT NULL,
    spl_version VARCHAR(64) DEFAULT NULL, product_identifier VARCHAR(100) DEFAULT NULL, rxcui VARCHAR(32) DEFAULT NULL,
    source_url TEXT DEFAULT NULL, review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', reviewer VARCHAR(100) DEFAULT NULL,
    reviewed_at DATETIME DEFAULT NULL, review_reason TEXT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE KEY uk_frdb_mapping_current (frdb_import_batch_id, frdb_compound_id), KEY idx_frdb_mapping_status (mapping_status, review_status),
    CONSTRAINT chk_frdb_mapping_status CHECK (mapping_status IN ('MATCHED', 'AMBIGUOUS', 'UNMATCHED', 'NEEDS_REVIEW'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Current auditable mapping state for every FRDB drug';

CREATE TABLE IF NOT EXISTS ext_data_coverage_metric (
    id BIGINT NOT NULL AUTO_INCREMENT, frdb_import_batch_id BIGINT NOT NULL, dailymed_import_batch_id BIGINT DEFAULT NULL,
    metric_name VARCHAR(100) NOT NULL, numerator INT NOT NULL, denominator INT NOT NULL, metric_value DECIMAL(9,6) NOT NULL,
    calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE KEY uk_coverage_metric (frdb_import_batch_id, dailymed_import_batch_id, metric_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recomputable external-data coverage metrics';

CREATE TABLE IF NOT EXISTS migration_backup_frdb_adverse_20260924 LIKE adverse_reaction_record;
INSERT IGNORE INTO migration_backup_frdb_adverse_20260924 SELECT * FROM adverse_reaction_record WHERE reporter = 'NCATS FRDB';

START TRANSACTION;
UPDATE drug_info d JOIN ext_frdb_drug_staging f
  ON (d.approval_number = CONCAT('FRDB:', f.frdb_compound_id)
      OR (d.data_source = 'FRDB' AND d.external_batch_id = f.import_batch_id AND d.external_compound_id = f.frdb_compound_id))
SET d.data_source = 'FRDB', d.external_batch_id = f.import_batch_id, d.external_compound_id = f.frdb_compound_id,
    d.external_unii = f.compound_unii, d.source_read_only = 1, d.data_quality_status = 'FRDB_SOURCE_ONLY',
    d.drug_category = NULL, d.category_code = NULL, d.approval_number = NULL, d.drug_description = NULL,
    d.risk_level = 'UNKNOWN', d.risk_score = 0.00
WHERE f.import_batch_id = 1;
DELETE FROM adverse_reaction_record WHERE reporter = 'NCATS FRDB';
INSERT INTO ext_frdb_dailymed_mapping
    (frdb_import_batch_id, frdb_compound_id, frdb_compound_name, frdb_unii, standardized_name, mapping_status, match_method, candidate_count, review_status, review_reason)
SELECT import_batch_id, frdb_compound_id, compound_name, compound_unii, compound_name, 'NEEDS_REVIEW', 'PENDING', 0, 'PENDING', 'DailyMed baseline not imported yet.'
FROM ext_frdb_drug_staging WHERE import_batch_id = 1
ON DUPLICATE KEY UPDATE frdb_compound_name = VALUES(frdb_compound_name), frdb_unii = VALUES(frdb_unii), standardized_name = VALUES(standardized_name);
COMMIT;

CREATE OR REPLACE VIEW v_ext_frdb_drug_catalog AS
SELECT d.id AS drug_id, d.drug_name, d.generic_name, d.data_source, d.external_batch_id, d.external_compound_id,
       d.external_unii, d.source_read_only, d.data_quality_status, m.mapping_status, m.match_method,
       m.match_confidence, m.candidate_count, m.spl_set_id, m.spl_version, m.product_identifier, m.rxcui, m.review_status
FROM drug_info d LEFT JOIN ext_frdb_dailymed_mapping m
  ON m.frdb_import_batch_id = d.external_batch_id AND m.frdb_compound_id = d.external_compound_id
WHERE d.data_source = 'FRDB';

CREATE OR REPLACE VIEW v_ext_frdb_adverse_evidence AS
SELECT d.frdb_compound_id, d.compound_name, a.frdb_adverse_event_id, a.adverseevents_frequency,
       a.adverseevents_frequency_units, a.adverseevents_liability, a.adverseevents_severity, a.adverseevents_type,
       a.adverseevents_comment, a.adverseevents_dlt, t.frdb_tox_id, t.toxicity_routes, t.toxicity_species, t.toxicity_source_uri
FROM ext_frdb_adverse_event_staging a JOIN ext_frdb_toxicity_staging t
  ON t.import_batch_id = a.import_batch_id AND t.frdb_tox_id = a.frdb_tox_id
JOIN ext_frdb_drug_staging d ON d.import_batch_id = t.import_batch_id AND d.frdb_compound_id = t.frdb_compound_id;

CREATE OR REPLACE VIEW v_ext_frdb_ddi_evidence AS
SELECT d.frdb_compound_id, d.compound_name, x.frdb_ddi_id, x.ddi_activity, x.ddi_clin_comment,
       x.ddi_clin_evidence, x.ddi_clin_support, x.ddi_comment, x.ddi_concentration, x.ddi_metabolyte,
       x.ddi_relation, x.ddi_reported_magnitude, x.ddi_magnitude, x.ddi_target, x.ddi_type, x.ddi_url, x.ddi_page
FROM ext_frdb_ddi_staging x JOIN ext_frdb_drug_staging d
  ON d.import_batch_id = x.import_batch_id AND d.frdb_compound_id = x.frdb_compound_id;
