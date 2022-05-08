CREATE TABLE IF NOT EXISTS chat_global (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    dtime DATETIME,
    mcid VARCHAR(20),
    uuid VARCHAR(36),
    nick VARCHAR(32),
    content VARCHAR(256) CHARACTER SET utf8mb4
);

CREATE TABLE IF NOT EXISTS chat_channel (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    dtime DATETIME,
    mcid VARCHAR(20),
    uuid VARCHAR(36),
    nick VARCHAR(32),
    ch VARCHAR(20),
    content VARCHAR(256) CHARACTER SET utf8mb4
);

CREATE TABLE IF NOT EXISTS chat_private (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    dtime DATETIME,
    mcid VARCHAR(20),
    uuid VARCHAR(36),
    nick VARCHAR(32),
    to_mcid VARCHAR(20),
    to_uuid VARCHAR(36),
    to_nick VARCHAR(32),
    command VARCHAR(16),
    content VARCHAR(256) CHARACTER SET utf8mb4
);
