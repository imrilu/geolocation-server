DROP TABLE IF EXISTS distances;

CREATE TABLE distances (
  id INT AUTO_INCREMENT PRIMARY KEY,
  distance DOUBLE NOT NULL,
  source VARCHAR(250) NOT NULL,
  destination VARCHAR(250) NOT NULL,
  hits INT NOT NULL
);
