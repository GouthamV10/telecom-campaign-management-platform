CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE campaign (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_date DATETIME,
    end_date DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    manager_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_campaign_manager FOREIGN KEY (manager_id) REFERENCES users (id)
);

CREATE INDEX idx_campaign_status ON campaign (status);
CREATE INDEX idx_campaign_manager ON campaign (manager_id);
