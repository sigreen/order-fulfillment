DROP TABLE IF EXISTS stock;

CREATE TABLE stock (
  item_id integer NOT NULL,
  item_name varchar(100) NOT NULL,
  inventory integer NOT NULL
);

INSERT INTO stock (item_id,item_name,inventory) VALUES (1,'Hammer',10);
INSERT INTO stock (item_id,item_name,inventory) VALUES (2,'Nails',25);
INSERT INTO stock (item_id,item_name,inventory) VALUES (3,'Light Bulb',13);
INSERT INTO stock (item_id,item_name,inventory) VALUES (4,'Ceiling Fan',2);
INSERT INTO stock (item_id,item_name,inventory) VALUES (5,'Wrench',6);