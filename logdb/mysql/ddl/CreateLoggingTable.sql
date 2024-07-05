/*create logging table if it does not exist.*/
CREATE TABLE IF NOT EXISTS logs (
          id                 integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
          log_date            datetime,
          location_info       varchar(1000),
          message             varchar(4000),
          priority            varchar(50),
          thread_name         varchar(200),
          user_name           varchar(200),
          session_id          varchar(100),
          action_uri          varchar(200),
          client_ip_address   varchar(50),
          server_ip_address   varchar(50),
          application_id      varchar(50)
        );