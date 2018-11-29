DROP TABLE IF EXISTS stock;

CREATE TABLE stock (
  item_id integer NOT NULL,
  item_name varchar(60) NOT NULL,
  item_description varchar(100) NOT NULL,
  inventory integer NOT NULL
);

INSERT INTO stock (item_id,item_name,item_description,inventory) VALUES (1,'Hammer','A large hammer',10);
INSERT INTO stock (item_id,item_name,item_description,inventory) VALUES (2,'Nails','A set of 100 nails',25);
INSERT INTO stock (item_id,item_name,item_description,inventory) VALUES (3,'Light Bulb','T30 3000K light bulb',13);
INSERT INTO stock (item_id,item_name,item_description,inventory) VALUES (4,'Ceiling Fan','A very large ceiling fan',2);
INSERT INTO stock (item_id,item_name,item_description,inventory) VALUES (5,'Wrench','Small wrench for 3/4 size bolts',6);