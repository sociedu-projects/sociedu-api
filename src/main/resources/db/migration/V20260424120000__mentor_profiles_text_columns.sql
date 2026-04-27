-- PostgreSQL: headline/expertise từng có thể là bytea → LOWER(bytea) lỗi.
-- Chỉ đổi khi information_schema báo bytea.

DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = current_schema()
                     AND table_name = 'mentor_profiles'
                     AND column_name = 'headline'
                     AND data_type = 'bytea') THEN
            ALTER TABLE mentor_profiles
                ALTER COLUMN headline TYPE text USING convert_from(headline, 'UTF8');
        END IF;

        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = current_schema()
                     AND table_name = 'mentor_profiles'
                     AND column_name = 'expertise'
                     AND data_type = 'bytea') THEN
            ALTER TABLE mentor_profiles
                ALTER COLUMN expertise TYPE text USING convert_from(expertise, 'UTF8');
        END IF;
    END
$$;
