\connect identity

CREATE TABLE users (
    id uuid PRIMARY KEY,
    email text NOT NULL CHECK (length(btrim(email)) BETWEEN 3 AND 254),
    password_hash text NOT NULL CHECK (length(password_hash) > 0),
    status text NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'disabled')),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX users_email_normalized_uidx ON users (lower(btrim(email)));

CREATE TABLE roles (
    code text PRIMARY KEY,
    description text NOT NULL
);
INSERT INTO roles (code, description) VALUES
    ('user', 'Registered user'),
    ('editor', 'System template editor');

CREATE TABLE user_roles (
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_code text NOT NULL REFERENCES roles(code),
    PRIMARY KEY (user_id, role_code)
);

\connect prompts

CREATE TABLE categories (
    id uuid PRIMARY KEY,
    name text NOT NULL CHECK (length(btrim(name)) > 0),
    description text,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE templates (
    id uuid PRIMARY KEY,
    category_id uuid NOT NULL REFERENCES categories(id),
    slug text NOT NULL UNIQUE CHECK (length(btrim(slug)) > 0),
    status text NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'archived')),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX templates_category_idx ON templates (category_id);

CREATE TABLE template_versions (
    id uuid PRIMARY KEY,
    template_id uuid NOT NULL REFERENCES templates(id),
    version_number integer NOT NULL CHECK (version_number > 0),
    title text NOT NULL CHECK (length(btrim(title)) > 0),
    description text,
    body text NOT NULL CHECK (length(btrim(body)) > 0),
    status text NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'retired')),
    created_at timestamptz NOT NULL DEFAULT now(),
    published_at timestamptz,
    CONSTRAINT template_versions_number_unique UNIQUE (template_id, version_number),
    CONSTRAINT published_version_has_date CHECK (status = 'draft' OR published_at IS NOT NULL)
);
-- One current published version; retired versions remain available to history.
CREATE UNIQUE INDEX template_versions_current_uidx ON template_versions (template_id)
    WHERE status = 'published';

CREATE TABLE template_parameters (
    id uuid PRIMARY KEY,
    template_version_id uuid NOT NULL REFERENCES template_versions(id) ON DELETE CASCADE,
    name text NOT NULL CHECK (name ~ '^[a-zA-Z][a-zA-Z0-9_]*$'),
    label text NOT NULL CHECK (length(btrim(label)) > 0),
    type text NOT NULL CHECK (type IN ('text', 'choice')),
    required boolean NOT NULL DEFAULT true,
    max_length integer CHECK (max_length > 0),
    options jsonb,
    display_order integer NOT NULL DEFAULT 0 CHECK (display_order >= 0),
    CONSTRAINT template_parameters_name_unique UNIQUE (template_version_id, name),
    CONSTRAINT choice_options_array CHECK (type <> 'choice' OR (options IS NOT NULL AND jsonb_typeof(options) = 'array' AND options <> '[]'::jsonb))
);

-- user_id belongs to the separate identity database and is not a foreign key.
CREATE TABLE prompt_runs (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    template_version_id uuid NOT NULL REFERENCES template_versions(id),
    parameters jsonb NOT NULL CHECK (jsonb_typeof(parameters) = 'object'),
    result text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX prompt_runs_user_created_id_idx ON prompt_runs (user_id, created_at DESC, id DESC);
