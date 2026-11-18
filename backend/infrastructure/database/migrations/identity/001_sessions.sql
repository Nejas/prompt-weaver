-- Run once against the identity database. Access and refresh tokens are stored
-- only as SHA-256 digests. Rotation and revocation are application operations.
BEGIN;
CREATE TABLE auth_sessions (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_token_hash bytea NOT NULL UNIQUE CHECK (octet_length(access_token_hash) = 32),
    refresh_token_hash bytea NOT NULL UNIQUE CHECK (octet_length(refresh_token_hash) = 32),
    access_expires_at timestamptz NOT NULL,
    refresh_expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    CHECK (access_expires_at <= refresh_expires_at)
);
CREATE INDEX auth_sessions_user_idx ON auth_sessions (user_id);
COMMIT;
