-- NCATS Inxight Drugs FRDB external-data staging schema.
-- This schema is intentionally isolated from drug_info, drug_risk_record,
-- and adverse_reaction_record. It never deletes or modifies demonstration data.

USE drug_safety;

CREATE TABLE IF NOT EXISTS ext_data_import_batch (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'External import batch ID',
    source_name VARCHAR(100) NOT NULL COMMENT 'Source provider and dataset name',
    source_url TEXT NOT NULL COMMENT 'Official download URL',
    dataset_version VARCHAR(64) NOT NULL COMMENT 'Publisher dataset version',
    downloaded_at DATETIME NOT NULL COMMENT 'Local download time',
    archive_file VARCHAR(255) NOT NULL COMMENT 'Downloaded archive filename',
    archive_sha256 CHAR(64) NOT NULL COMMENT 'SHA-256 of downloaded archive',
    drugs_source_file VARCHAR(255) DEFAULT NULL,
    ddi_source_file VARCHAR(255) DEFAULT NULL,
    adverseevents_source_file VARCHAR(255) DEFAULT NULL,
    toxicity_source_file VARCHAR(255) DEFAULT NULL,
    drugs_row_count INT DEFAULT 0,
    ddi_row_count INT DEFAULT 0,
    adverseevents_row_count INT DEFAULT 0,
    toxicity_row_count INT DEFAULT 0,
    import_status VARCHAR(20) NOT NULL DEFAULT 'STARTED' COMMENT 'STARTED, COMPLETED or FAILED',
    imported_at DATETIME DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ext_data_import_archive_sha256 (archive_sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='External data import provenance batches';

CREATE TABLE IF NOT EXISTS ext_frdb_drug_staging (
    id BIGINT NOT NULL AUTO_INCREMENT,
    import_batch_id BIGINT NOT NULL,
    frdb_compound_id BIGINT NOT NULL,
    compound_name VARCHAR(500) DEFAULT NULL,
    compound_unii VARCHAR(100) DEFAULT NULL,
    imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ext_frdb_drug_batch_compound (import_batch_id, frdb_compound_id),
    KEY idx_ext_frdb_drug_name (compound_name),
    KEY idx_ext_frdb_drug_unii (compound_unii),
    CONSTRAINT fk_ext_frdb_drug_batch FOREIGN KEY (import_batch_id) REFERENCES ext_data_import_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NCATS FRDB frdb-drugs.tsv raw staging';

CREATE TABLE IF NOT EXISTS ext_frdb_ddi_staging (
    id BIGINT NOT NULL AUTO_INCREMENT,
    import_batch_id BIGINT NOT NULL,
    frdb_ddi_id BIGINT NOT NULL,
    frdb_compound_id BIGINT DEFAULT NULL,
    ddi_activity TEXT DEFAULT NULL,
    ddi_clin_comment TEXT DEFAULT NULL,
    ddi_clin_evidence TEXT DEFAULT NULL,
    ddi_clin_support TEXT DEFAULT NULL,
    ddi_comment TEXT DEFAULT NULL,
    ddi_concentration TEXT DEFAULT NULL,
    ddi_metabolyte TEXT DEFAULT NULL,
    ddi_relation TEXT DEFAULT NULL,
    ddi_reported_magnitude TEXT DEFAULT NULL,
    ddi_magnitude TEXT DEFAULT NULL,
    ddi_target TEXT DEFAULT NULL,
    ddi_type TEXT DEFAULT NULL,
    ddi_url TEXT DEFAULT NULL,
    ddi_page TEXT DEFAULT NULL,
    imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ext_frdb_ddi_batch_row (import_batch_id, frdb_ddi_id),
    KEY idx_ext_frdb_ddi_compound (frdb_compound_id),
    KEY idx_ext_frdb_ddi_target (ddi_target(191)),
    CONSTRAINT fk_ext_frdb_ddi_batch FOREIGN KEY (import_batch_id) REFERENCES ext_data_import_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NCATS FRDB frdb-ddi.tsv raw staging';

CREATE TABLE IF NOT EXISTS ext_frdb_toxicity_staging (
    id BIGINT NOT NULL AUTO_INCREMENT,
    import_batch_id BIGINT NOT NULL,
    frdb_tox_id BIGINT NOT NULL,
    frdb_compound_id BIGINT DEFAULT NULL,
    toxicity_age_group TEXT DEFAULT NULL,
    toxicity_dose_value TEXT DEFAULT NULL,
    toxicity_dose_units TEXT DEFAULT NULL,
    toxicity_routes TEXT DEFAULT NULL,
    toxicity_sex TEXT DEFAULT NULL,
    toxicity_source_type TEXT DEFAULT NULL,
    toxicity_source_uri TEXT DEFAULT NULL,
    toxicity_species TEXT DEFAULT NULL,
    toxicity_duration TEXT DEFAULT NULL,
    toxicity_duration_units TEXT DEFAULT NULL,
    imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ext_frdb_toxicity_batch_row (import_batch_id, frdb_tox_id),
    KEY idx_ext_frdb_toxicity_compound (frdb_compound_id),
    CONSTRAINT fk_ext_frdb_toxicity_batch FOREIGN KEY (import_batch_id) REFERENCES ext_data_import_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NCATS FRDB toxicity linkage staging for adverse events';

CREATE TABLE IF NOT EXISTS ext_frdb_adverse_event_staging (
    id BIGINT NOT NULL AUTO_INCREMENT,
    import_batch_id BIGINT NOT NULL,
    frdb_adverse_event_id BIGINT NOT NULL,
    adverseevents_frequency TEXT DEFAULT NULL,
    adverseevents_frequency_units TEXT DEFAULT NULL,
    adverseevents_liability TEXT DEFAULT NULL,
    adverseevents_severity TEXT DEFAULT NULL,
    adverseevents_type TEXT DEFAULT NULL,
    adverseevents_comment TEXT DEFAULT NULL,
    adverseevents_dlt TEXT DEFAULT NULL,
    frdb_tox_id BIGINT DEFAULT NULL,
    imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ext_frdb_adverse_batch_row (import_batch_id, frdb_adverse_event_id),
    KEY idx_ext_frdb_adverse_toxicity (frdb_tox_id),
    KEY idx_ext_frdb_adverse_type (adverseevents_type(191)),
    CONSTRAINT fk_ext_frdb_adverse_batch FOREIGN KEY (import_batch_id) REFERENCES ext_data_import_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NCATS FRDB frdb-adverseevents.tsv raw staging';
