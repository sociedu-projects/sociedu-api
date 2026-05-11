ALTER TABLE files
    ADD COLUMN IF NOT EXISTS public_id TEXT,
    ADD COLUMN IF NOT EXISTS resource_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS storage_provider VARCHAR(255) DEFAULT 'cloudinary',
    ADD COLUMN IF NOT EXISTS visibility VARCHAR(255) DEFAULT 'private',
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

ALTER TABLE files
    ALTER COLUMN entity_type TYPE VARCHAR(255)
    USING entity_type::TEXT;

UPDATE files
SET storage_provider = 'cloudinary'
WHERE storage_provider IS NULL;

UPDATE files
SET visibility = 'private'
WHERE visibility IS NULL;

UPDATE files
SET file_name = 'file'
WHERE file_name IS NULL;

UPDATE files
SET file_url = ''
WHERE file_url IS NULL;

UPDATE files
SET mime_type = 'application/octet-stream'
WHERE mime_type IS NULL;

UPDATE files
SET file_size = 0
WHERE file_size IS NULL;

ALTER TABLE files
    ALTER COLUMN file_name SET NOT NULL,
    ALTER COLUMN file_url SET NOT NULL,
    ALTER COLUMN mime_type SET NOT NULL,
    ALTER COLUMN file_size SET NOT NULL,
    ALTER COLUMN storage_provider SET NOT NULL;

ALTER TABLE service_packages
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE service_packages
SET updated_at = created_at
WHERE updated_at IS NULL;

UPDATE service_packages
SET name = 'Untitled package'
WHERE name IS NULL;

UPDATE service_packages
SET is_active = TRUE
WHERE is_active IS NULL;

ALTER TABLE service_packages
    ALTER COLUMN mentor_id SET NOT NULL,
    ALTER COLUMN name SET NOT NULL;

CREATE TABLE IF NOT EXISTS service_package_versions
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    package_id    UUID           NOT NULL REFERENCES service_packages (id) ON DELETE CASCADE,
    price         DECIMAL(19, 2) NOT NULL,
    duration      INT            NOT NULL,
    delivery_type VARCHAR(255),
    is_default    BOOLEAN        DEFAULT TRUE,
    created_at    TIMESTAMP      DEFAULT NOW()
);

INSERT INTO service_package_versions (package_id, price, duration, delivery_type, is_default, created_at)
SELECT sp.id,
       COALESCE(sp.price, 0),
       COALESCE(sp.duration, 0),
       NULL,
       TRUE,
       COALESCE(sp.created_at, NOW())
FROM service_packages sp
WHERE NOT EXISTS (
    SELECT 1
    FROM service_package_versions v
    WHERE v.package_id = sp.id
);

CREATE INDEX IF NOT EXISTS idx_service_package_versions_package
    ON service_package_versions (package_id);

CREATE INDEX IF NOT EXISTS idx_service_package_versions_default
    ON service_package_versions (package_id, is_default);

ALTER TABLE package_curriculums
    ADD COLUMN IF NOT EXISTS package_version_id UUID;

UPDATE package_curriculums pc
SET package_version_id = v.id
FROM service_package_versions v
WHERE pc.package_version_id IS NULL
  AND pc.package_id = v.package_id
  AND v.is_default = TRUE;

ALTER TABLE package_curriculums
    ALTER COLUMN package_version_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_package_curriculums_version_order
    ON package_curriculums (package_version_id, order_index);
