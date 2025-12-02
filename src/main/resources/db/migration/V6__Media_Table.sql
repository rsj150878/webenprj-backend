-- Create posts table for Motivise study blogging platform
CREATE TABLE media (
    id  BINARY(16) PRIMARY KEY DEFAULT (UUID()),
    content_type varchar(150),
    name varchar(256),
    external_id varchar(256)
);