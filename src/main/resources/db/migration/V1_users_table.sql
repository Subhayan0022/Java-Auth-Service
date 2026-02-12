CREATE TABLE user (
    id UUID PRIMIARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL INTEGER,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'normal_user',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
);